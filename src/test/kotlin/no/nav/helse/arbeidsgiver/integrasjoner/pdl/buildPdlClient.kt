package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.mockk.mockk
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.integrasjoner.mockHttpClient
import no.nav.helse.arbeidsgiver.utils.loadFromResources

internal fun buildClient(status: HttpStatusCode, content: String): PdlClient {
    return PdlClient(
        "url",
        mockk<AccessTokenProvider>(relaxed = true),
        mockHttpClient(status, content),
        ObjectMapper()
    )
}

val testFnr = "test-ident"
val validPdlNavnResponse = "pdl-mock-data/pdl-person-response.json".loadFromResources()
val validPdlFullPersonResponse = "pdl-mock-data/pdl-hentFullPerson-response.json".loadFromResources()
val errorPdlResponse = "pdl-mock-data/pdl-error-response.json".loadFromResources()
