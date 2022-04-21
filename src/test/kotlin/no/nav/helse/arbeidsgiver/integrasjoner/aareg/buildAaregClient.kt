package no.nav.helse.arbeidsgiver.integrasjoner.aareg

import io.ktor.http.*
import io.mockk.mockk
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.integrasjoner.mockHttpClient
import no.nav.helse.arbeidsgiver.utils.loadFromResources

fun buildClient(status: HttpStatusCode, content: String): AaregArbeidsforholdClient {
    return AaregArbeidsforholdClient(
        "url",
        mockk<AccessTokenProvider>(relaxed = true),
        mockHttpClient(status, content)
    )
}

internal val validResponse = "aareg-mock-data/aareg-arbeidsforhold.json".loadFromResources()
