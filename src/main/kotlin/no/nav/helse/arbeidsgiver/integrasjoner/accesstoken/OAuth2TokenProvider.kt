package no.nav.helse.arbeidsgiver.integrasjoner.accesstoken

import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService

/**
 * OAuth2 Token-klient for å hente access token for bruk i andre tjenester, feks joark, PDL eller Oppgave.
 */
class OAuth2TokenProvider(
    private val oauth2Service: OAuth2AccessTokenService,
    private val clientProperties: ClientProperties
) : AccessTokenProvider {
    override fun getToken(): String {
        return oauth2Service.getAccessToken(clientProperties).accessToken
    }
}
