package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider

class OppgaveKlient(
    private val url: String,
    private val stsClient: AccessTokenProvider,
    private val httpClient: HttpClient
) {

    suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest, callId: String): OpprettOppgaveResponse {
        val stsToken = stsClient.getToken()
        val httpResponse = httpClient.post<HttpStatement>(url) {
            contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
            this.header("Authorization", "Bearer $stsToken")
            this.header("X-Correlation-ID", callId)
            body = opprettOppgaveRequest
        }.execute()
        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                httpResponse.call.response.receive()
            }
            HttpStatusCode.Created -> {
                httpResponse.call.response.receive()
            }
            else -> {
                throw OpprettOppgaveUnauthorizedException(opprettOppgaveRequest, httpResponse.status)
            }
        }
    }

    fun opprettOppgaveSync(
        opprettOppgaveRequest: OpprettOppgaveRequest,
        callId: String
    ): OpprettOppgaveResponse {
        return runBlocking { opprettOppgave(opprettOppgaveRequest, callId) }
    }

    suspend fun hentOppgave(oppgaveId: Int, callId: String): OppgaveResponse {
        val stsToken = stsClient.getToken()
        val httpResponse = httpClient.get<HttpStatement>("$url/$oppgaveId") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $stsToken")
            header("X-Correlation-ID", callId)
        }.execute()
        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                httpResponse.call.response.receive()
            }
            else -> {
                throw HentOppgaveUnauthorizedException(oppgaveId, httpResponse.status)
            }
        }
    }

    fun hentOppgaveSync(
        oppgaveId: Int,
        callId: String
    ): OppgaveResponse {
        return runBlocking { hentOppgave(oppgaveId, callId) }
    }
}

const val OPPGAVETYPE_FORDELINGSOPPGAVE = "FDR"
