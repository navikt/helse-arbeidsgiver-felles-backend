package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import org.slf4j.LoggerFactory
import java.time.LocalDate

interface OppgaveKlient {
    suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse
}

interface SyncOppgaveKlient {
    fun opprettOppgaveSync(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse
}

class OppgaveKlientImpl(
        private val url: String, private val stsClient: AccessTokenProvider, private val httpClient: HttpClient
) : OppgaveKlient, SyncOppgaveKlient {

    override suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse {
        val stsToken = stsClient.getToken()
        return httpClient.post(url) {
            contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
            this.header("Authorization", "Bearer $stsToken")
            this.header("X-Correlation-ID", callId)
            body = opprettOppgaveRequest
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OppgaveKlientImpl::class.java)
    }

    override fun opprettOppgaveSync(
        opprettOppgaveRequest: OpprettOppgaveRequest,
        callId: String
    ): OpprettOppgaveResponse {
        return runBlocking {  opprettOppgave(opprettOppgaveRequest, callId) }
    }
}

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