package no.nav.helse.arbeidsgiver.integrasjoner.sts

import io.ktor.http.*
import no.nav.helse.arbeidsgiver.integrasjoner.RestSTSAccessTokenProvider
import no.nav.helse.arbeidsgiver.integrasjoner.mockHttpClient
import no.nav.helse.arbeidsgiver.utils.loadFromResources

fun buildClient(status: HttpStatusCode, content: String): RestSTSAccessTokenProvider {
    return RestSTSAccessTokenProvider(
        username = "",
        password = "",
        stsEndpoint = "",
        httpClient = mockHttpClient(status, content)
    )
}

val validStsResponse = "sts-mock-data/valid-sts-token.json".loadFromResources()
