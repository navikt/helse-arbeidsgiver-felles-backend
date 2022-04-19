package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.mockk.mockk
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.utils.loadFromResources
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class PdlClientImplTest {
    val validPdlNavnResponse = "pdl-mock-data/pdl-person-response.json".loadFromResources()
    val validPdlFullPersonResponse = "pdl-mock-data/pdl-hentFullPerson-response.json".loadFromResources()
    val errorPdlResponse = "pdl-mock-data/pdl-error-response.json".loadFromResources()

    val mockStsClient = mockk<AccessTokenProvider>(relaxed = true)
    private val testFnr = "test-ident"

    val client = HttpClient(MockEngine) {

        install(JsonFeature) {
            serializer = JacksonSerializer {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                registerModule(JavaTimeModule())
                registerModule(KotlinModule())
            }
        }

        engine {
            addHandler { request ->
                val body = (request.body as TextContent).text
                when {
                    body.contains("hentIdent") -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(validPdlFullPersonResponse, headers = responseHeaders)
                    }
                    body.contains(testFnr) -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(validPdlNavnResponse, headers = responseHeaders)
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
        val response = pdlClient.personNavn(testFnr)
        val name = response
            ?.navn
            ?.firstOrNull()
        assertThat(name?.fornavn).isEqualTo("Ola")
        assertThat(name?.metadata?.master).isEqualTo("Freg")
    }

    @Test
    internal fun `Full Person returnerer en person ved gyldig respons fra PDL`() {
        val response = pdlClient.fullPerson(testFnr)
        val name = response
            ?.hentPerson
            ?.navn
            ?.firstOrNull()
            ?.fornavn

        assertThat(name).isEqualTo("TREIG")
        assertThat(response?.hentIdenter?.identer).hasSize(2)
        assertThat(response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.AKTORID }).hasSize(1)
        assertThat(response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.FOLKEREGISTERIDENT }).hasSize(1)

        assertThat(response?.hentPerson?.adressebeskyttelse?.firstOrNull()?.gradering).isNull()
        assertThat(response?.hentGeografiskTilknytning?.gtType).isEqualTo(PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.KOMMUNE)
        assertThat(response?.hentPerson?.foedsel?.firstOrNull()?.foedselsdato).isEqualTo(LocalDate.of(1978, 12, 9))
        assertThat(response?.hentPerson?.doedsfall).hasSize(0)
        assertThat(response?.hentPerson?.kjoenn?.firstOrNull()?.kjoenn).isEqualTo("MANN")
    }

    @Test
    internal fun `Kaster PdlException ved feilrespons fra PDL`() {
        assertThrows<PdlClientImpl.PdlException> {
            pdlClient.personNavn("fail")
        }
    }
}
