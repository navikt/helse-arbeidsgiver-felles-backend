package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.mockk.mockk
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.utils.loadFromResources

internal fun buildClient(status: HttpStatusCode, content: String): PdlClientImpl {
    return PdlClientImpl(
        "url",
        mockk<AccessTokenProvider>(relaxed = true),
        mockHttpClient(status, content),
        ObjectMapper()
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

val testFnr = "test-ident"
val validPdlNavnResponse = "pdl-mock-data/pdl-person-response.json".loadFromResources()
val validPdlFullPersonResponse = "pdl-mock-data/pdl-hentFullPerson-response.json".loadFromResources()
val errorPdlResponse = "pdl-mock-data/pdl-error-response.json".loadFromResources()
