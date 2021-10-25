package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import org.slf4j.LoggerFactory
import java.time.LocalDate

interface OppgaveKlient {
    suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse
    suspend fun hentOppgave(oppgaveId: Int, callId: String): OppgaveResponse
}

interface SyncOppgaveKlient {
    fun opprettOppgaveSync(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse
    fun hentOppgaveSync(oppgaveId: Int, callId: String): OppgaveResponse
}

class OppgaveKlientImpl(
        private val url: String, private val stsClient: AccessTokenProvider, private val httpClient: HttpClient
) : OppgaveKlient, SyncOppgaveKlient {

    companion object {
        private val log = LoggerFactory.getLogger(OppgaveKlientImpl::class.java)
    }

    override suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse {
        val stsToken = stsClient.getToken()
        return httpClient.post(url) {
            contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
            this.header("Authorization", "Bearer $stsToken")
            this.header("X-Correlation-ID", callId)
            body = opprettOppgaveRequest
        }
    }

    override fun opprettOppgaveSync(
        opprettOppgaveRequest: OpprettOppgaveRequest,
        callId: String
    ): OpprettOppgaveResponse {
        return runBlocking {  opprettOppgave(opprettOppgaveRequest, callId) }
    }

    override suspend fun hentOppgave(oppgaveId: Int, callId: String): OppgaveResponse {
        val httpResponse = httpClient.get<HttpStatement>("$url/$oppgaveId") {
            contentType(ContentType.Application.Json)
            val stsToken = stsClient.getToken()
            header("Authorization", "Bearer ${stsToken}")
            header("X-Correlation-ID", callId)
        }.execute()
        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                httpResponse.call.response.receive()
            }
            else -> {
                val msg = "OppgaveClient hentOppgave kastet feil ${httpResponse.status} ved hentOppgave av oppgave, response: ${httpResponse.call.response.receive<String>()}"
                throw RuntimeException(msg)
            }
        }
    }

    override fun hentOppgaveSync(
        oppgaveId: Int, callId: String
    ): OppgaveResponse {
        return runBlocking {  hentOppgave( oppgaveId, callId ) }
    }

}

data class OppgaveResponse(
    val id: Int? = null,
    val versjon: Int? = null,
    val tildeltEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val aktoerId: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val tema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val aktivDato: LocalDate,
    val fristFerdigstillelse: LocalDate? = null,
    val prioritet: String,
    val status: String? = null,
    val mappeId: Int? = null
)

data class OpprettOppgaveRequest(
        val tildeltEnhetsnr: String? = null,
        val opprettetAvEnhetsnr: String? = null,
        val aktoerId: String? = null,
        val orgnr: String? = null,
        val journalpostId: String? = null,
        val journalpostkilde: String? = null,
        val behandlesAvApplikasjon: String? = null,
        val tilordnetRessurs: String? = null,

        val saksreferanse: String? = null,
        val beskrivelse: String? = null,
        val temagruppe: String? = null,
        val tema: String,
        val oppgavetype: String,

        /**
         * https://kodeverk-web.nais.adeo.no/kodeverksoversikt/kodeverk/Behandlingstyper
         */
        val behandlingstype: String? = null,

        /**
         * https://kodeverk-web.nais.adeo.no/kodeverksoversikt/kodeverk/Behandlingstema
         */
        val behandlingstema: String? = null,
        val aktivDato: LocalDate,
        val fristFerdigstillelse: LocalDate? = null,
        val prioritet: String
)

// https://oppgave.dev.adeo.no/#/Oppgave/opprettOppgave
data class OpprettOppgaveResponse(
    val id: Int,
    val tildeltEnhetsnr : String,
    val tema: String,
    val oppgavetype: String,
    val versjon: Int,
    val aktivDato: LocalDate,
    val prioritet: Prioritet,
    val status: Status
)

enum class Status { OPPRETTET, AAPNET, UNDER_BEHANDLING, FERDIGSTILT, FEILREGISTRERT }
enum class Prioritet { HOY, NORM, LAV }

data class OppgaveResultat(
        val oppgaveId: Int,
        val duplikat: Boolean
)

const val OPPGAVETYPE_FORDELINGSOPPGAVE = "FDR"
fun createForedlingsOppgaveRequest(
    journalpostId: String,
    beskrivelse: String,
    behandlingstype: String,
    frist: LocalDate,
    behandlesAvApplikasjon: String
) = OpprettOppgaveRequest(
    journalpostId = journalpostId,
    behandlesAvApplikasjon = behandlesAvApplikasjon,
    beskrivelse = beskrivelse,
    tema = "SYK",
    oppgavetype = OPPGAVETYPE_FORDELINGSOPPGAVE,
    behandlingstype = behandlingstype,
    aktivDato = LocalDate.now(),
    fristFerdigstillelse = frist,
    prioritet = Prioritet.NORM.name
)









