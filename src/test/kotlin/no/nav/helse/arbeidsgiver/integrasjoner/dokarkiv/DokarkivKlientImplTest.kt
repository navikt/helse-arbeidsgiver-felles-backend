package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv

import io.ktor.client.features.*
import io.ktor.http.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DokarkivKlientImplTest {

    @Test
    internal fun `Returnerer id og et responsobjekt ved suksess av ferdigstilling`() {
        val response = buildClient(HttpStatusCode.OK, validResponse).journalførDokument(request, true, "call-id")
        assertThat(response).isNotNull
        assertThat(response.journalpostId).isGreaterThan("0")
        assertThat(response.journalpostFerdigstilt).isEqualTo(true)
    }

    @Test
    internal fun `Kaster FerdigstillingFeiletException ved 200 OK men ikke ferdistilt når man ville ferdigstille`() {
        val exception = assertThrows<DokarkivKlientImpl.FerdigstillingFeiletException> {
            buildClient(HttpStatusCode.OK, ikkeFerdigstiltResponse).journalførDokument(request.copy(kanal = "ugyldig for ferdigstillelse"), true, "call-id")
        }

        assertThat(exception.journalpostId).isGreaterThan("0")
    }
    @Test
    internal fun `Kaster ClientRequestException ved feil i requesten`() {
        assertThrows<DokarkivKlientImpl.FerdigstillingFeiletException> {
            buildClient(HttpStatusCode.OK, errorResponse).journalførDokument(request, true, "call-id")
        }
    }
}
