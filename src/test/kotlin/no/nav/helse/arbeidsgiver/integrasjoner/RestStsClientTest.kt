package no.nav.helse.arbeidsgiver.integrasjoner

import no.nav.helse.arbeidsgiver.utils.loadFromResources
import com.fasterxml.jackson.databind.MapperFeature
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.ServerResponseException
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RestStsClientTests {
    val validStsResponse = "sts-mock-data/valid-sts-token.json".loadFromResources()

    private val successUrl = "https://success-url"
    private val timeoutUrl = "https://timeout-url"

    val client = HttpClient(MockEngine) {

        install(JsonFeature) { serializer = JacksonSerializer {
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        } }

        engine {
            addHandler { request ->
                val url = request.url.toString()
                when {
                    url.startsWith(successUrl) -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(validStsResponse, headers = responseHeaders)
                    }
                    url.startsWith(timeoutUrl) -> {
                        respond("Timed out", HttpStatusCode.GatewayTimeout)
                    }
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }


    @Test
    internal fun `valid answer from STS returns valid token, second call gives cached answer`() {
        val stsClient = RestStsClientImpl("username", "password", successUrl, client)
        val token = stsClient.getOidcToken()
        assertThat(token).isNotNull()

        val token2 = stsClient.getOidcToken()
        assertThat(token).isEqualTo(token2)
    }

    @Test
    internal fun `Error response (5xx) from STS throws exception`() {
        assertThrows(ServerResponseException::class.java) {
            val stsClient = RestStsClientImpl("username", "password", timeoutUrl, client)
        }
    }
}