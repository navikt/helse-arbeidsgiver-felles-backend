package no.nav.helse.arbeidsgiver.integrasjoner.altinn

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import no.nav.helse.arbeidsgiver.utils.loadFromResources

internal fun buildClient(status: HttpStatusCode, content: String): AltinnRestClient {
    return AltinnRestClient(
        "url",
        "",
        "",
        "",
        mockHttpClient(status, content),
        5
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

val validAltinnResponse = "altinn-mock-data/organisasjoner-med-rettighet.json".loadFromResources()

val identitetsnummer = "01020354321"
val serviceCode = "4444"

