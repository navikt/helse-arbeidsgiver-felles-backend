package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import io.ktor.client.features.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class OppgaveKlientTest {

    @Test
    fun `Returnerer id og et responsobjekt ved suksess`() {
        val response1 = runBlocking { buildClient(HttpStatusCode.Created, validResponse).opprettOppgave(oppgaveRequest, "call-id") }
        Assertions.assertThat(response1).isNotNull
        Assertions.assertThat(response1.id).isGreaterThan(0)
        val response2 = runBlocking { buildClient(HttpStatusCode.OK, validResponse).opprettOppgave(oppgaveRequest, "call-id") }
        Assertions.assertThat(response2).isNotNull
        Assertions.assertThat(response2.id).isGreaterThan(0)
    }

    @Test
    fun `Kaster ClientRequestException ved feil i requesten`() {
        org.junit.jupiter.api.assertThrows<ClientRequestException> {
            runBlocking { buildClient(HttpStatusCode.Unauthorized, errorResponse).opprettOppgave(oppgaveRequest, "call-id") }
        }
    }

    @Test
    fun `Kaster OpprettOppgaveUnauthorizedException ved feil i requesten`() {
        org.junit.jupiter.api.assertThrows<OpprettOppgaveUnauthorizedException> {
            runBlocking { buildClient(HttpStatusCode.Accepted, "").opprettOppgave(oppgaveRequest, "call-id") }
        }
    }

    @Test
    fun `Returnerer responsobjekt ved suksess`() {
        val response = runBlocking { buildClient(HttpStatusCode.OK, validResponse).hentOppgave(1, "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.id).isGreaterThan(0)
    }
}
