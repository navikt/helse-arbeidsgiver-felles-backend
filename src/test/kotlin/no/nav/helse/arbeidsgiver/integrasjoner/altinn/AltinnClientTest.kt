package no.nav.helse.arbeidsgiver.integrasjoner.altinn

import com.fasterxml.jackson.databind.MapperFeature
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.utils.loadFromResources
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AltinnRestClientTests {

    val validAltinnResponse = "altinn-mock-data/organisasjoner-med-rettighet.json".loadFromResources()

    private val identitetsnummer = "01020354321"
    private val serviceCode = "4444"

    val client = HttpClient(MockEngine) {

        install(JsonFeature) {
            serializer = JacksonSerializer {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }
        }

        engine {
            addHandler { request ->
                val url = request.url.toString()
                when {
                    url.startsWith("http://juice") -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(validAltinnResponse, headers = responseHeaders)
                    }
                    url.startsWith("http://timeout") -> {
                        respond("Timed out", HttpStatusCode.GatewayTimeout)
                    }
                    url.startsWith("http://altinn-timeout") -> {
                        respond("Timed out", HttpStatusCode.BadGateway)
                    }
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

    @Test
    internal fun `valid answer from altinn returns properly serialized list of all active org forms`() {
        val altinnClient = AltinnRestClient("http://juice", "api-gw-key", "altinn-key", serviceCode, client)
        val authList = altinnClient.hentOrgMedRettigheterForPerson(identitetsnummer)
        assertThat(authList).hasSize(4)
    }

    @Test
    internal fun `timeout from altinn throws exception`() {
        val altinnClient = AltinnRestClient("http://timeout", "api-gw-key", "altinn-key", serviceCode, client)

        assertThrows(ServerResponseException::class.java) {
            altinnClient.hentOrgMedRettigheterForPerson(identitetsnummer)
        }
    }

    @Test
    internal fun `timeout from altinn throws AltinnBrukteForLangTidException`() {
        val altinnClient = AltinnRestClient("http://altinn-timeout", "api-gw-key", "altinn-key", serviceCode, client)
        assertThrows(AltinnBrukteForLangTidException::class.java) {
            runBlocking { altinnClient.hentOrgMedRettigheterForPerson(identitetsnummer) }
        }
    }
}
