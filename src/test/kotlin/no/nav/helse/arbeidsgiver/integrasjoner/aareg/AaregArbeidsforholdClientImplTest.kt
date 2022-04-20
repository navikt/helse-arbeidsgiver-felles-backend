package no.nav.helse.arbeidsgiver.integrasjoner.aareg

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AaregArbeidsforholdClientImplTest {

    @Test
    fun `Returnerer gyldig objekt når alt er oK`() {
        val aaregClient = buildClient(HttpStatusCode.OK, validResponse)
        val response = runBlocking { aaregClient.hentArbeidsforhold("ident", "call-id") }
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.find { it.arbeidsgiver.organisasjonsnummer == "896929119" }).isNotNull
    }

    @Test
    fun `Returnerer ugyldig objekt ved feil`() {
        val aaregClient = buildClient(HttpStatusCode.InternalServerError, "")
        assertThrows<Exception> {
            runBlocking { aaregClient.hentArbeidsforhold("ident", "call-id") }
        }
    }
}
