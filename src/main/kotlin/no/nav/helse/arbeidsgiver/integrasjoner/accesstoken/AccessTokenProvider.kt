package no.nav.helse.arbeidsgiver.integrasjoner

interface AccessTokenProvider {
    fun getToken(): String
}
