package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.nav.helse.arbeidsgiver.integrasjoner.RestStsClient
import org.slf4j.LoggerFactory
import java.time.LocalDate

interface OppgaveKlient {
    suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse
}

class OppgaveKlientImpl(
        private val url: String, private val stsClient: RestStsClient, private val httpClient: HttpClient
) : OppgaveKlient {

    override suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse {
        val stsToken = stsClient.getOidcToken()
        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
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

