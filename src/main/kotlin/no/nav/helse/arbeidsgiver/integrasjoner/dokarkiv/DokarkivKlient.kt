package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import org.slf4j.LoggerFactory

interface DokarkivKlient {
    fun journalførDokument(
        journalpost: JournalpostRequest,
        forsoekFerdigstill: Boolean,
        callId: String
    ): JournalpostResponse
}

/**
 * Oppretter en journalpost i dokarkiv @see JournalpostRequest
 *
 * Servicebrukeren i Token provdieren må være i AD-gruppen 0000-GA-joark-journalpostapi-skriv
 * Joark tilgangskontroll-dok: https://confluence.adeo.no/pages/viewpage.action?pageId=315962195
 */
class DokarkivKlientImpl(
    private val dokarkivBaseUrl: String,
    private val httpClient: HttpClient,
    private val accessTokenProvider: AccessTokenProvider
) : DokarkivKlient {

    private val logger: org.slf4j.Logger = LoggerFactory.getLogger("DokarkivClient")

    override fun journalførDokument(
        journalpost: JournalpostRequest,
        forsoekFerdigstill: Boolean,
        callId: String
    ): JournalpostResponse {
        logger.debug("Journalfører dokument")
        val url = "$dokarkivBaseUrl/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=$forsoekFerdigstill"
        val response = runBlocking {
            httpClient.post<JournalpostResponse> {
                url(url)
                contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                headers.append("Authorization", "Bearer " + accessTokenProvider.getToken())
                headers.append("Nav-Call-Id", callId)
                body = journalpost
            }
        }
        if (forsoekFerdigstill && !response.journalpostFerdigstilt) {
            throw FerdigstillingFeiletException(response.journalpostId, response.melding)
        }

        return response
    }

    class FerdigstillingFeiletException(
        val journalpostId: String,
        feilmelding: String?
    ) : Exception("Ferdigstillelse av journalposten feilet: $feilmelding")
}
