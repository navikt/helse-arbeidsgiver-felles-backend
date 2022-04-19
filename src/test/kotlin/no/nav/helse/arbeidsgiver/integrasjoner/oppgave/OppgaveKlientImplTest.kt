package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import io.ktor.client.features.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class OppgaveKlientImplTest {

    @Test
    fun `Returnerer id og et responsobjekt ved suksess`() {
        val oppgaveKlientImpl = buildClient(HttpStatusCode.Created, validResponse)
        val response = runBlocking { oppgaveKlientImpl.opprettOppgave(oppgaveRequest, "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.id).isGreaterThan(0)
    }

    @Test
    fun `Kaster ClientRequestException ved feil i requesten`() {
        val oppgaveKlientImpl = buildClient(HttpStatusCode.BadRequest, errorResponse)
        org.junit.jupiter.api.assertThrows<ClientRequestException> {
            runBlocking { oppgaveKlientImpl.opprettOppgave(oppgaveRequest, "call-id") }
        }
    }

    @Test
    fun `Returnerer responsobjekt ved suksess`() {
        val oppgaveKlientImpl = buildClient(HttpStatusCode.OK, validResponse)
        val response = runBlocking { oppgaveKlientImpl.hentOppgave(1, "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.id).isGreaterThan(0)
    }
}
