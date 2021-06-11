package no.nav.helse.arbeidsgiver.integrasjoner.aareg

import com.fasterxml.jackson.databind.MapperFeature
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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class AaregArbeidsforholdClientImplTest {
    val validResponse = "aareg-mock-data/aareg-arbeidsforhold.json".loadFromResources()

    val mockStsClient = mockk<AccessTokenProvider>(relaxed = true)
    private val successRequest = "success request"

    val client = HttpClient(MockEngine) {

        install(JsonFeature) { serializer = JacksonSerializer {
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        } }

        engine {
            addHandler { request ->
                val body = (request.body as TextContent).text
                when {
                    body.contains(successRequest) -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(validResponse, headers = responseHeaders)
                    }
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

    val aaregClient = AaregArbeidsforholdClientImpl(
            "url",
            mockStsClient,
            client
    )

    @Disabled
    @Test
    fun `Returnerer gyldig objekt når alt er oK`() {
        val response = runBlocking { aaregClient.hentArbeidsforhold("ident",  "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.find { it.arbeidsgiver.organisasjonsnummer == "896929119" }).isNotNull
    }
}