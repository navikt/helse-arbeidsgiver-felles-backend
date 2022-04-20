package no.nav.helse.arbeidsgiver.integrasjoner.altinn

import io.ktor.http.*
import no.nav.helse.arbeidsgiver.integrasjoner.mockHttpClient
import no.nav.helse.arbeidsgiver.utils.loadFromResources

internal fun buildClient(status: HttpStatusCode, content: String): AltinnRestClient {
    return AltinnRestClient(
        "url",
        "",
        "",
        "",
        mockHttpClient(status, content),
        5
    )
}

val validAltinnResponse = "altinn-mock-data/organisasjoner-med-rettighet.json".loadFromResources()

val identitetsnummer = "01020354321"
