package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv

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

internal fun buildClient(status: HttpStatusCode, content: String): DokarkivKlientImpl {
    return DokarkivKlientImpl(
        "url",
        mockHttpClient(status, content),
        mockk<AccessTokenProvider>(relaxed = true)
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

val validResponse = "dokarkiv-mock-data/dokarkiv-success-response.json".loadFromResources()
val ikkeFerdigstiltResponse = "dokarkiv-mock-data/dokarkiv-ikke-ferdigstilt-response.json".loadFromResources()
val errorResponse = "dokarkiv-mock-data/dokarkiv-error-response.json".loadFromResources()

val request = JournalpostRequest(
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
