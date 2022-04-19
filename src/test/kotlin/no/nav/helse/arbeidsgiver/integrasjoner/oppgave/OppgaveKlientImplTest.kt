package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.utils.loadFromResources
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OppgaveKlientImplTest {
    val validResponse = "oppgave-mock-data/oppgave-success-response.json".loadFromResources()
    val errorResponse = "oppgave-mock-data/oppgave-error-response.json".loadFromResources()

    fun buildOppgaveKlient(status: HttpStatusCode, content: String): OppgaveKlientImpl {
        return OppgaveKlientImpl(
            "url",
            mockk<AccessTokenProvider>(relaxed = true),
            mockHttpClient(status, content)
        )
    }

    fun mockHttpClient(status: HttpStatusCode, content: String): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = content,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(JsonFeature) {
                serializer = JacksonSerializer {
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
        }
    }

    private val request = OpprettOppgaveRequest(
        aktoerId = "aktørId",
        journalpostId = "journalpostId",
        beskrivelse = "beskrivelse",
        tema = "SYK",
        oppgavetype = "ROB_BEH",
        behandlingstema = "ab0433",
        aktivDato = LocalDate.now(),
        fristFerdigstillelse = LocalDate.now().plusDays(7),
        prioritet = "NORM"
    )

    @Test
    fun `Returnerer id og et responsobjekt ved suksess`() {
        val oppgaveKlientImpl = buildOppgaveKlient(HttpStatusCode.Created, validResponse)
        val response = runBlocking { oppgaveKlientImpl.opprettOppgave(request, "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.id).isGreaterThan(0)
    }

    @Test
    fun `Kaster ClientRequestException ved feil i requesten`() {
        val oppgaveKlientImpl = buildOppgaveKlient(HttpStatusCode.BadRequest, errorResponse)
        org.junit.jupiter.api.assertThrows<ClientRequestException> {
            runBlocking { oppgaveKlientImpl.opprettOppgave(request, "call-id") }
        }
    }

    @Test
    fun `Returnerer responsobjekt ved suksess`() {
        val oppgaveKlientImpl = buildOppgaveKlient(HttpStatusCode.OK, validResponse)
        val response = runBlocking { oppgaveKlientImpl.hentOppgave(1, "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.id).isGreaterThan(0)
    }
}
