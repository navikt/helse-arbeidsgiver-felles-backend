package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv

import com.fasterxml.jackson.databind.MapperFeature
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
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

internal class DokarkivKlientImplTest {
    val validResponse = "dokarkiv-mock-data/dokarkiv-success-response.json".loadFromResources()
    val ikkeFerdigstiltResponse = "dokarkiv-mock-data/dokarkiv-ikke-ferdigstilt-response.json".loadFromResources()
    val errorResponse = "dokarkiv-mock-data/dokarkiv-error-response.json".loadFromResources()

    val mockStsClient = mockk<AccessTokenProvider>(relaxed = true)
    private val badRequest = "bad request"

    val client = HttpClient(MockEngine) {

        install(JsonFeature) {
            serializer = JacksonSerializer {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }
        }

        engine {
            addHandler { request ->
                val body = (request.body as TextContent).text
                when {
                    body.contains("ALTINN") -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(validResponse, headers = responseHeaders)
                    }
                    body.contains("ugyldig for ferdigstillelse") -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(ikkeFerdigstiltResponse, headers = responseHeaders)
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

    val dokarkivKlient = DokarkivKlientImpl(
        "url",
        client,
        mockStsClient
    )

    private val request = JournalpostRequest(
        tittel = "Dette er en tittel for journalposten",

        bruker = Bruker(
            id = "01020312345",
            idType = IdType.FNR
        ),
        avsenderMottaker = AvsenderMottaker(
            id = "123456785",
            navn = "Arbeidsgivernavn",
            idType = IdType.ORGNR
        ),
        dokumenter = listOf(
            Dokument(
                brevkode = "test_brevkode",
                tittel = "Tittel på dokumentet",
                dokumentVarianter = listOf(
                    DokumentVariant(
                        fysiskDokument = "base64EncodedDocument"
                    )
                )
            )
        ),
        eksternReferanseId = "ref",
        kanal = "ALTINN",
        journalposttype = Journalposttype.UTGAAENDE,
        datoMottatt = LocalDate.now()
    )

    @Test
    internal fun `Returnerer id og et responsobjekt ved suksess av ferdigstilling`() {
        val response = dokarkivKlient.journalførDokument(request, true, "call-id")

        assertThat(response).isNotNull
        assertThat(response.journalpostId).isGreaterThan("0")
        assertThat(response.journalpostFerdigstilt).isEqualTo(true)
    }

    @Test
    internal fun `Kaster FerdigstillingFeiletException ved 200 OK men ikke ferdistilt når man ville ferdigstille`() {
        val exception = assertThrows<DokarkivKlientImpl.FerdigstillingFeiletException> {
            dokarkivKlient.journalførDokument(request.copy(kanal = "ugyldig for ferdigstillelse"), true, "call-id")
        }

        assertThat(exception.journalpostId).isGreaterThan("0")
    }
    @Test
    internal fun `Kaster ClientRequestException ved feil i requesten`() {
        assertThrows<ClientRequestException> {
            dokarkivKlient.journalførDokument(request.copy(kanal = badRequest), true, "call-id")
        }
    }
}
