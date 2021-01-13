package no.nav.helse.arbeidsgiver.bakgrunnsjobb

import io.ktor.client.features.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.utils.RecurringJob
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

class BakgrunnsjobbService(
        val bakgrunnsjobbRepository: BakgrunnsjobbRepository,
        val delayMillis: Long = 30 * 1000L,
        val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        val bakgrunnsvarsler: Bakgrunnsvarsler = TomVarsler()
) : RecurringJob(coroutineScope, delayMillis) {

    private val prossesserere = HashMap<String, BakgrunnsjobbProsesserer>()

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
        } catch (ex: Exception) {
            val responseBody = tryGetResponseBody(ex, jobb.uuid)
            val responseBodyMessage = if (responseBody != null) "Feil fra ekstern tjeneste: $responseBody" else ""
            jobb.status = if (jobb.forsoek >= jobb.maksAntallForsoek) BakgrunnsjobbStatus.STOPPET else BakgrunnsjobbStatus.FEILET
            if (jobb.status == BakgrunnsjobbStatus.STOPPET) {
                logger.error("Jobb ${jobb.uuid} feilet permanent. $responseBodyMessage", ex)
                bakgrunnsvarsler.rapporterPermanentFeiletJobb()
            } else {
                logger.warn("Jobb ${jobb.uuid} feilet, forsøker igjen ${jobb.kjoeretid}. $responseBodyMessage", ex)
            }
        } finally {
            bakgrunnsjobbRepository.update(jobb)
        }
    }

    private fun tryGetResponseBody(jobException: Exception, jobId: UUID): String? {
        if ( jobException is ResponseException) {
            return try {
                runBlocking { jobException.response?.content?.readUTF8Line() }
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
    fun nesteForsoek(forsoek: Int, forrigeForsoek: LocalDateTime): LocalDateTime
}
