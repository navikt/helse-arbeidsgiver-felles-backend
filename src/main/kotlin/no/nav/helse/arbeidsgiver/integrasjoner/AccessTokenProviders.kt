package no.nav.helse.arbeidsgiver.integrasjoner

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTParser
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*



interface AccessTokenProvider {
    fun getToken(): String
}

/**
 * STS-klient for å hente access token for bruk i andre tjenester, feks joark, PDL eller Oppgave.
 *
 * Det returnerte tokenet representerer den angitte servicebrukeren (username, password)
 *
 * Cacher tokenet til det 5 minutter unna å bli ugyldig.
 */
@Deprecated("STS skal fases ut til fordel for OAuth2 Client Credentials og Token Exchange (TokenX)")
class RestSTSAccessTokenProvider(username: String,
                                 password: String,
                                 stsEndpoint: String,
                                 private val httpClient: HttpClient) : AccessTokenProvider {

    private val endpointURI: String
    private val basicAuth: String

    private var currentToken: JwtToken

    init {
        basicAuth = basicAuth(username, password)
        endpointURI = "$stsEndpoint?grant_type=client_credentials&scope=openid"
        currentToken = requestToken()
    }


    override fun getToken(): String {
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
        private val log = LoggerFactory.getLogger(RestSTSAccessTokenProvider::class.java)
    }
}

/**
 * OAuth2 Token-klient for å hente access token for bruk i andre tjenester, feks joark, PDL eller Oppgave.
 */
class OAuth2TokenProvider(
        private val oauth2Service: OAuth2AccessTokenService,
        private val clientProperties: ClientProperties) : AccessTokenProvider {
    override fun getToken(): String {
        return oauth2Service.getAccessToken(clientProperties).accessToken
    }
}