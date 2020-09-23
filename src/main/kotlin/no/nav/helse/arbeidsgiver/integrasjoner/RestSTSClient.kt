package no.nav.helse.arbeidsgiver.integrasjoner

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTParser
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*


interface RestStsClient {
    fun getOidcToken(): String
}

/**
 * STS-klient for å hente access token for bruk i andre tjenester, feks joark, PDL eller Oppgave.
 *
 * Det returnerte tokenet representerer den angitte servicebrukeren (username, password)
 *
 * Cacher tokenet til det 5 minutter unna å bli ugyldig.
 */
class RestStsClientImpl(username: String,
                        password: String,
                        stsEndpoint: String,
                        private val httpClient: HttpClient) : RestStsClient {

    private val endpointURI: String
    private val basicAuth: String

    private var currentToken: JwtToken

    init {
        basicAuth = basicAuth(username, password)
        endpointURI = "$stsEndpoint?grant_type=client_credentials&scope=openid"
        currentToken = requestToken()
    }


    override fun getOidcToken(): String {
        if (isExpired(currentToken, Date.from(Instant.now().plusSeconds(300)))) {
            log.debug("OIDC Token is expired, getting a new one from the STS")
            currentToken = requestToken()
            log.debug("Hentet nytt token fra sts som går ut ${currentToken.expirationTime}")
        }
        return currentToken.tokenAsString
    }


    private fun requestToken(): JwtToken {
        val response = runBlocking {
            httpClient.get<STSOidcResponse>(endpointURI) {
                headers.append("Authorization", basicAuth)
                headers.append("Accept", "application/json")
            }
        }

        return JwtToken(response.access_token)
    }

    private fun basicAuth(username: String, password: String): String {
        log.debug("basic auth username: $username")
        return "Basic " + Base64.getEncoder().encodeToString("$username:$password".toByteArray())
    }


    private fun isExpired(jwtToken: JwtToken, date: Date): Boolean {
        return date.after(jwtToken.expirationTime) &&
                jwtToken.expirationTime.before(date)
    }


    private class JwtToken(encodedToken: String) {
        val tokenAsString: String = encodedToken
        val jwt: JWT = JWTParser.parse(encodedToken)
        val expirationTime = jwt.jwtClaimsSet.expirationTime
    }

    private data class STSOidcResponse(
        val access_token: String
    )

    companion object {
        private val log = LoggerFactory.getLogger(RestStsClientImpl::class.java)
    }
}

