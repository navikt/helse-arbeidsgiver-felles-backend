package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.mockk.mockk
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.utils.loadFromResources
import java.time.LocalDate

internal fun buildClient(status: HttpStatusCode, content: String): OppgaveKlientImpl {
    return OppgaveKlientImpl(
        "url",
        mockk<AccessTokenProvider>(relaxed = true),
        mockHttpClient(status, content)
    )
}

private fun mockHttpClient(status: HttpStatusCode, content: String): HttpClient {
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

val validResponse = "oppgave-mock-data/oppgave-success-response.json".loadFromResources()
val errorResponse = "oppgave-mock-data/oppgave-error-response.json".loadFromResources()

val oppgaveRequest = OpprettOppgaveRequest(
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
