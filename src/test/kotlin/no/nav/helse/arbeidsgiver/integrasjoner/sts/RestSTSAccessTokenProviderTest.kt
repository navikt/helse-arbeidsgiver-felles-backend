package no.nav.helse.arbeidsgiver.integrasjoner.sts

import io.ktor.client.features.*
import io.ktor.http.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RestSTSAccessTokenProviderTest {

    @Test
    internal fun `valid answer from STS returns valid token, second call gives cached answer`() {
        val token = buildClient(HttpStatusCode.OK, validStsResponse).getToken()
        assertThat(token).isNotNull()
        val token2 = buildClient(HttpStatusCode.OK, validStsResponse).getToken()
        assertThat(token).isEqualTo(token2)
    }

    @Test
    internal fun `Error response (5xx) from STS throws exception`() {
        assertThrows(ServerResponseException::class.java) {
            buildClient(HttpStatusCode.InternalServerError, "")
        }
    }
}
