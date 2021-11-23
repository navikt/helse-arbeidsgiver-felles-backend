package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.ktor.http.content.*
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

    val mockStsClient = mockk<AccessTokenProvider>(relaxed = true)
    private val badRequest = "bad request"
    private val successRequest = "success request"

    val client = HttpClient(MockEngine) {

        install(JsonFeature) {
            serializer = JacksonSerializer {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                registerModule(JavaTimeModule())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }
        }

        engine {
            addHandler { request ->
                val body = (request.body as TextContent).text
                when {
                    body.contains(successRequest) -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(validResponse, headers = responseHeaders)
                    }
                    body.contains(badRequest) -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(errorResponse, status = HttpStatusCode.BadRequest)
                    }
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

    val dokarkivKlient = OppgaveKlientImpl(
        "url",
        mockStsClient,
        client
    )

    private val request = OpprettOppgaveRequest(
        aktoerId = "aktørId",
        journalpostId = "journalpostId",
        beskrivelse = successRequest,
        tema = "SYK",
        oppgavetype = "ROB_BEH",
        behandlingstema = "ab0433",
        aktivDato = LocalDate.now(),
        fristFerdigstillelse = LocalDate.now().plusDays(7),
        prioritet = "NORM"
    )

    @Test
    fun `Returnerer id og et responsobjekt ved suksess`() {
        val response = runBlocking { dokarkivKlient.opprettOppgave(request, "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.id).isGreaterThan(0)
    }

    @Test
    fun `Kaster ClientRequestException ved feil i requesten`() {
        org.junit.jupiter.api.assertThrows<ClientRequestException> {
            runBlocking { dokarkivKlient.opprettOppgave(request.copy(beskrivelse = badRequest), "call-id") }
        }
    }

    @Test
    fun `Returnerer responsobjekt ved suksess`() {
        val response = runBlocking { dokarkivKlient.hentOppgave(1, "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.id).isGreaterThan(0)
    }
}
