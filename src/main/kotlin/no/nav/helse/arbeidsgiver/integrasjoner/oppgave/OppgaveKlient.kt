package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.*
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.text.charset

interface OppgaveKlient {
    suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse
}

class OppgaveKlientImpl(
        private val url: String, private val stsClient: AccessTokenProvider, private val httpClient: HttpClient
) : OppgaveKlient {

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
}

data class OpprettOppgaveRequest(
        val tildeltEnhetsnr: String? = null,
        val aktoerId: String? = null,
        val journalpostId: String? = null,
        val behandlesAvApplikasjon: String? = null,
        val saksreferanse: String? = null,
        val beskrivelse: String? = null,
        val tema: String? = null,
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

data class OpprettOppgaveResponse(
        val id: Int
)

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
    prioritet = "NORM"
)