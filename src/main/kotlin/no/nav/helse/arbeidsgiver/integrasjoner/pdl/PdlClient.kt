package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import org.slf4j.LoggerFactory

interface PdlClient {
    /**
     * Ident kan være enten FNR eller AktørID
     */
    fun personNavn(ident: String): PdlHentPersonNavn.PdlPersonNavneliste?
    fun fullPerson(ident: String): PdlHentFullPerson?
}

/**
 * Enkel GraphQL-klient for PDL som kan enten hente navn fra aktør eller fnr (ident)
 * eller hente mer fullstendig data om en person via fnr eller aktørid (ident)
 *
 * Authorisasjon gjøres via den gitte Token prodvideren, og servicebrukeren som er angitt i token provideren må være i
 * i AD-gruppen 0000-GA-TEMA_SYK som dokumentert her
 * https://navikt.github.io/pdl/index-intern.html#_konsumentroller_basert_p%C3%A5_tema
 *
 * Klienten vil alltid gi PDL-Temaet 'SYK', så om du trenger et annet tema må du endre denne klienten.
 */
class PdlClientImpl(
        private val pdlUrl: String,
        private val stsClient: AccessTokenProvider,
        private val httpClient: HttpClient,
        private val om: ObjectMapper
) : PdlClient {
    private val personNavnQuery = this::class.java.getResource("/pdl/hentPersonNavn.graphql").readText().replace(Regex("[\n\r]"), "")
    private val fullPersonQuery = this::class.java.getResource("/pdl/hentFullPerson.graphql").readText().replace(Regex("[\n\r]"), "")


    override fun personNavn(ident: String): PdlHentPersonNavn.PdlPersonNavneliste? {
        val entity = PdlQueryObject(personNavnQuery, Variables(ident))
        val response = queryPdl<PdlHentPersonNavn?, PdlResponse<PdlHentPersonNavn?>>(entity)
        return response?.hentPerson
    }

    override fun fullPerson(ident: String): PdlHentFullPerson? {
        val queryObject = PdlQueryObject(fullPersonQuery, Variables(ident))
        val response = queryPdl<PdlHentFullPerson?, PdlResponse<PdlHentFullPerson?>>(queryObject)
        return response
    }


    private inline fun <K, reified T: PdlResponse<K>> queryPdl(graphqlQuery: PdlQueryObject): K? {
        val stsToken = stsClient.getToken()
        val pdlPersonReponse = runBlocking {
            httpClient.post<T> {
                url(pdlUrl)
                body = TextContent(om.writeValueAsString(graphqlQuery), contentType = ContentType.Application.Json)
                header("Tema", "SYK")
                header("Authorization", "Bearer $stsToken")
                header("Nav-Consumer-Token", "Bearer $stsToken")
            }
        }

        if (pdlPersonReponse.errors != null && pdlPersonReponse.errors!!.isNotEmpty()) {
            throw PdlException(pdlPersonReponse.errors)
        }

        return pdlPersonReponse.data

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlClient::class.java)
    }

    class PdlException(val pdlErrors: List<PdlError>?) : RuntimeException()
}