package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider

class DokarkivGraphQLClient(
    private val journalPostUrl: String,
    private val stsClient: AccessTokenProvider,
    private val httpClient: HttpClient,
    private val om: ObjectMapper
) {
    private val hentJournalPostQuery = this::class.java.getResource("/journal/hentJournalPost.graphql").readText().replace(Regex("[\n\r]"), "")

    //  return queryJournalPost<JournalPost?, JournalPostResponse<JournalPost?>>(entity)
    fun getJournalpost(journalpostId: String): JournalPost? {
        val entity = JournalPostQueryObject(hentJournalPostQuery, QueryVariables(journalpostId))
        return queryJournalPost(entity)
    }

    // queryJournalPost<JournalPost?, JournalPostResponse<JournalPost?>>(entity, userLoginToken)
    fun getJournalpost(journalpostId: String, userLoginToken: String): JournalPost? {
        val entity = JournalPostQueryObject(hentJournalPostQuery, QueryVariables(journalpostId))
        return queryJournalPost(entity, userLoginToken)
    }

    private inline fun <K, reified T : JournalPostResponse<K>> queryJournalPost(graphqlQuery: JournalPostQueryObject, loggedInUserToken: String? = null): K? {
        val stsToken = stsClient.getToken()
        val JournalResponse = runBlocking {
            httpClient.post<T> {
                url(journalPostUrl)
                body = TextContent(om.writeValueAsString(graphqlQuery), contentType = ContentType.Application.Json)
                header("Authorization", "Bearer ${loggedInUserToken ?: stsToken}")
                header("Nav-Consumer-Token", "Bearer $stsToken")
            }
        }

        return JournalResponse.data
    }
}
