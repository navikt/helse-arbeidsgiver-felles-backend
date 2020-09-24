package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.mockk.mockk
import no.nav.helse.arbeidsgiver.integrasjoner.RestStsClient
import no.nav.helse.arbeidsgiver.utils.loadFromResources
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PdlClientImplTest {
    val validPdlResponse = "pdl-mock-data/pdl-person-response.json".loadFromResources()
    val errorPdlResponse = "pdl-mock-data/pdl-error-response.json".loadFromResources()

    val mockStsClient = mockk<RestStsClient>(relaxed = true)
    private val testFnr = "test-ident"

    val client = HttpClient(MockEngine) {

        install(JsonFeature) { serializer = JacksonSerializer {
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        } }

        engine {
            addHandler { request ->
                val body = (request.body as TextContent).text
                when {
                    body.contains(testFnr) -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(validPdlResponse, headers = responseHeaders)
                    }
                    body.contains("fail") -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(errorPdlResponse, headers = responseHeaders)
                    }
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }
    val objectMapper = ObjectMapper()

    val pdlClient = PdlClientImpl(
            "url",
            mockStsClient,
            client,
            objectMapper
    )

    @Test
    internal fun `Returnerer en person ved gyldig respons fra PDL`() {
        val response = pdlClient.person(testFnr)
        val name = response
                ?.navn
                ?.firstOrNull()
                ?.fornavn
        assertThat(name).isEqualTo("Ola")
    }

    @Test
    internal fun `Kaster PdlException ved feilrespons fra PDL`() {
        assertThrows<PdlClientImpl.PdlException> {
            pdlClient.person("fail")
        }
    }
}