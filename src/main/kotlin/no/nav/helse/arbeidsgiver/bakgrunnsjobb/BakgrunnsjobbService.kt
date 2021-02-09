package no.nav.helse.arbeidsgiver.bakgrunnsjobb

import io.ktor.client.features.*
import io.ktor.utils.io.*
import io.prometheus.client.Counter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.utils.RecurringJob
import java.sql.Connection
import java.time.LocalDateTime
import kotlin.collections.HashMap

class BakgrunnsjobbService(
    val bakgrunnsjobbRepository: BakgrunnsjobbRepository,
    val delayMillis: Long = 30 * 1000L,
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    val bakgrunnsvarsler: Bakgrunnsvarsler = TomVarsler()
) : RecurringJob(coroutineScope, delayMillis) {

    val prossesserere = HashMap<String, BakgrunnsjobbProsesserer>()

    @Deprecated("Bruk registrer(..)")
    fun leggTilBakgrunnsjobbProsesserer(type: String, prosesserer: BakgrunnsjobbProsesserer) {
        prossesserere[type] = prosesserer
    }

    fun registrer(prosesserer: BakgrunnsjobbProsesserer) {
        prossesserere[prosesserer.JOB_TYPE] = prosesserer
    }

    inline fun <reified T : BakgrunnsjobbProsesserer> opprettJobb(
        kjoeretid: LocalDateTime = LocalDateTime.now(),
        forsoek: Int = 0,
        maksAntallForsoek: Int = 3,
        data: String,
        connection: Connection
    ) {
        val prosesserer = prossesserere.values.filterIsInstance<T>().firstOrNull()
            ?: throw IllegalArgumentException("Denne prosessereren er ukjent")

        bakgrunnsjobbRepository.save(
            Bakgrunnsjobb(
                type = prosesserer.JOB_TYPE,
                kjoeretid = kjoeretid,
                forsoek = forsoek,
                maksAntallForsoek = maksAntallForsoek,
                data = data
            ),
            connection
        )
    }

    override fun doJob() {
        do {
            val wasEmpty = finnVentende()
                .also { logger.debug("Fant ${it.size} bakgrunnsjobber å kjøre") }
                .onEach { prosesser(it) }
                .isEmpty()
        } while (!wasEmpty)
    }

    fun prosesser(jobb: Bakgrunnsjobb) {
        jobb.behandlet = LocalDateTime.now()
        jobb.forsoek++

        val prossessorForType = prossesserere[jobb.type]
            ?: throw IllegalArgumentException("Det finnes ingen prossessor for typen '${jobb.type}'. Dette må konfigureres.")

        try {
            jobb.kjoeretid = prossessorForType.nesteForsoek(jobb.forsoek, LocalDateTime.now())
            prossessorForType.prosesser(jobb.copy())
            jobb.status = BakgrunnsjobbStatus.OK
            OK_JOBB_COUNTER.labels(jobb.type).inc()
        } catch (ex: Throwable) {
            val responseBody = tryGetResponseBody(ex)
            val responseBodyMessage = if (responseBody != null) "Feil fra ekstern tjeneste: $responseBody" else ""
            jobb.status = if (jobb.forsoek >= jobb.maksAntallForsoek) BakgrunnsjobbStatus.STOPPET else BakgrunnsjobbStatus.FEILET
            if (jobb.status == BakgrunnsjobbStatus.STOPPET) {
                logger.error("Jobb ${jobb.uuid} feilet permanent. $responseBodyMessage", ex)
                STOPPET_JOBB_COUNTER.labels(jobb.type).inc()
                bakgrunnsvarsler.rapporterPermanentFeiletJobb()
                tryStopAction(prossessorForType, jobb)
            } else {
                logger.warn("Jobb ${jobb.uuid} feilet, forsøker igjen ${jobb.kjoeretid}. $responseBodyMessage", ex)
                FEILET_JOBB_COUNTER.labels(jobb.type).inc()
            }
        } finally {
            bakgrunnsjobbRepository.update(jobb)
        }
    }

    fun finnVentende(): List<Bakgrunnsjobb> =
        bakgrunnsjobbRepository.findByKjoeretidBeforeAndStatusIn(
            LocalDateTime.now(),
            setOf(BakgrunnsjobbStatus.OPPRETTET, BakgrunnsjobbStatus.FEILET)
        )


    private fun tryStopAction(prossessorForType: BakgrunnsjobbProsesserer, jobb: Bakgrunnsjobb) {
        try {
            prossessorForType.stoppet(jobb)
        } catch (ex: Throwable) {
            logger.error("Jobben ${jobb.uuid} feilet i sin opprydningsjobb!", ex)
        }
    }

    private fun tryGetResponseBody(jobException: Throwable): String? {
        if ( jobException is ResponseException) {
            return try {
                runBlocking { jobException.response?.content?.readUTF8Line() }
            } catch (readEx: Exception) {
                null
            }
        }
        return null
    }
}

/**
 * Interface for en klasse som kan prosessere en bakgrunnsjobbstype
 */
interface BakgrunnsjobbProsesserer {
    val JOB_TYPE: String

    /**
     * Logikken som skal håndtere jobben. Får inn en kopi av jobben med all metadata
     */
    fun prosesser(jobb: Bakgrunnsjobb)

    /**
     * Logikk som skal kjøres når jobben stoppes helt opp fordi maks antall forsøk er nådd.
     * Får inn en kopi av jobben med all metadata
     */
    fun stoppet(jobb: Bakgrunnsjobb) {

    }

    /**
     * Defaulter til en fibonacci-ish backoffløsning
     * ment for bruk med 10 forsøk, feiler permanent etter 230 timer = 9,5 dager
     */
    fun nesteForsoek(forsoek: Int, forrigeForsoek: LocalDateTime): LocalDateTime {
        val backoffWaitInHours = if (forsoek == 1) 1 else forsoek - 1 + forsoek
        return LocalDateTime.now().plusHours(backoffWaitInHours.toLong())
    }
}

val FEILET_JOBB_COUNTER = Counter.build()
    .namespace("helsearbeidsgiver")
    .name("feilet_jobb")
    .labelNames("jobbtype")
    .help("Teller jobber som har midlertidig feilet, men vil bli forsøkt igjen")
    .register()

val STOPPET_JOBB_COUNTER = Counter.build()
    .namespace("helsearbeidsgiver")
    .name("stoppet_jobb")
    .labelNames("jobbtype")
    .help("Teller jobber som har feilet permanent og må følges opp")
    .register()

val OK_JOBB_COUNTER = Counter.build()
    .namespace("helsearbeidsgiver")
    .name("jobb_ok")
    .labelNames("jobbtype")
    .help("Teller jobber som har blitt utført OK")
    .register()
