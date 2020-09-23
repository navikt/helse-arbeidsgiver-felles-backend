package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.RestStsClient
import org.slf4j.LoggerFactory

interface PdlClient {
    fun person(ident: String): PdlPerson?
}

class PdlClientImpl(
        private val pdlUrl: String,
        private val stsClient: RestStsClient,
        private val httpClient: HttpClient,
        private val om: ObjectMapper
) : PdlClient {
    private val query = this::class.java.getResource("/pdl/hentPerson.graphql").readText().replace(Regex("[\n\r]"), "")

    init {
        LOG.debug("Query: $query")
    }

    override fun person(ident: String): PdlPerson? {
        val stsToken = stsClient.getOidcToken()
        val entity = PdlRequest(query, Variables(ident))
        val pdlPersonReponse = runBlocking {
            httpClient.post<PdlPersonResponse> {
                url(pdlUrl)
                body = TextContent(om.writeValueAsString(entity), contentType = ContentType.Application.Json)
                header("Tema", "SYK")
                header("Authorization", "Bearer $stsToken")
                header("Nav-Consumer-Token", "Bearer $stsToken")
            }
        }

        if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
            throw PdlException(pdlPersonReponse.errors)
        }

        return pdlPersonReponse.data?.hentPerson
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlClient::class.java)
    }

    class PdlException(val pdlErrors: List<PdlError>?) : Exception()
}