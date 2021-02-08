package no.nav.helse.arbeidsgiver.bakgrunnsjobb

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.features.*
import io.ktor.utils.io.*
import io.prometheus.client.Counter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.processing.AutoCleanJobb
import no.nav.helse.arbeidsgiver.processing.AutoCleanJobbProcessor
import no.nav.helse.arbeidsgiver.processing.AutoCleanJobbProcessor.Companion.JOB_TYPE
import no.nav.helse.arbeidsgiver.utils.RecurringJob
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap


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




class BakgrunnsjobbService(
        val bakgrunnsjobbRepository: BakgrunnsjobbRepository,
        val delayMillis: Long = 30 * 1000L,
        val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        val bakgrunnsvarsler: Bakgrunnsvarsler = TomVarsler(),

) : RecurringJob(coroutineScope, delayMillis) {

    private val prossesserere = HashMap<String, BakgrunnsjobbProsesserer>()
    val log = LoggerFactory.getLogger(BakgrunnsjobbService::class.java)

    fun startAutoClean(frekvensITimer: Int, slettEldreEnnMaaneder : Long){
        val om = ObjectMapper().apply {
            registerKotlinModule()
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            dateFormat = StdDateFormat()
        }
        if(frekvensITimer < 1 || slettEldreEnnMaaneder < 0 ){
            log.info("startautoclean forsøkt startet med ugyldige parametre.")
            return
        }


        val autocleanjobber = bakgrunnsjobbRepository.findAutoCleanJobs()

        if(autocleanjobber.size == 0) {
            val autoCleanJobb = AutoCleanJobb(interval = frekvensITimer, slettEldre = slettEldreEnnMaaneder)
                bakgrunnsjobbRepository.save(
                        Bakgrunnsjobb(
                                kjoeretid = LocalDateTime.now().plusHours(frekvensITimer.toLong()),
                                maksAntallForsoek = 10,
                                data = om.writeValueAsString(AutoCleanJobbProcessor.JobbData(autoCleanJobb.id)),
                                type = JOB_TYPE
                        )
                )
            }
        else {
            val ekisterendeAutoCleanJobb = autocleanjobber.get(0)
            bakgrunnsjobbRepository.delete(ekisterendeAutoCleanJobb.uuid)
            startAutoClean(frekvensITimer, slettEldreEnnMaaneder)
        }

    }

    fun leggTilBakgrunnsjobbProsesserer(type: String, prosesserer: BakgrunnsjobbProsesserer) {
        prossesserere[type] = prosesserer
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

        try {
            val prossessorForType = prossesserere[jobb.type]
                    ?: throw IllegalArgumentException("Det finnes ingen prossessor for typen '${jobb.type}'. Dette må konfigureres.")

            jobb.kjoeretid = prossessorForType.nesteForsoek(jobb.forsoek, LocalDateTime.now())
            prossessorForType.prosesser(jobb.data)
            jobb.status = BakgrunnsjobbStatus.OK
            OK_JOBB_COUNTER.labels(jobb.type).inc()
        } catch (ex: Exception) {
            val responseBody = tryGetResponseBody(ex, jobb.uuid)
            val responseBodyMessage = if (responseBody != null) "Feil fra ekstern tjeneste: $responseBody" else ""
            jobb.status = if (jobb.forsoek >= jobb.maksAntallForsoek) BakgrunnsjobbStatus.STOPPET else BakgrunnsjobbStatus.FEILET
            if (jobb.status == BakgrunnsjobbStatus.STOPPET) {
                logger.error("Jobb ${jobb.uuid} feilet permanent. $responseBodyMessage", ex)
                STOPPET_JOBB_COUNTER.labels(jobb.type).inc()
                bakgrunnsvarsler.rapporterPermanentFeiletJobb()
            } else {
                logger.warn("Jobb ${jobb.uuid} feilet, forsøker igjen ${jobb.kjoeretid}. $responseBodyMessage", ex)
                FEILET_JOBB_COUNTER.labels(jobb.type).inc()
            }
        } finally {
            bakgrunnsjobbRepository.update(jobb)
        }
    }

    private fun tryGetResponseBody(jobException: Exception, jobId: UUID): String? {
        if ( jobException is ResponseException) {
            return try {
                runBlocking { jobException.response.content.readUTF8Line() }
            } catch (readEx: Exception) {
                null
            }
        }
        return null
    }

    fun finnVentende(): List<Bakgrunnsjobb> =
            bakgrunnsjobbRepository.findByKjoeretidBeforeAndStatusIn(LocalDateTime.now(), setOf(BakgrunnsjobbStatus.OPPRETTET, BakgrunnsjobbStatus.FEILET))
}

/**
 * Interface for en klasse som kan prosessere en bakgrunnsjobbstype
 */
interface BakgrunnsjobbProsesserer {
    fun prosesser(jobbData: String)

    /**
     * Defaulter til en fibonacci-ish backoffløsning
     * ment for bruk med 10 forsøk, feiler permanent etter 230 timer = 9,5 dager
     */
    fun nesteForsoek(forsoek: Int, forrigeForsoek: LocalDateTime): LocalDateTime {
        val backoffWaitInHours = if (forsoek == 1) 1 else forsoek-1 + forsoek
        return LocalDateTime.now().plusHours(backoffWaitInHours.toLong())
    }
}