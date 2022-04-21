package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import io.ktor.http.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class PdlClientTest {

    @Test
    internal fun `Returnerer en person ved gyldig respons fra PDL`() {
        val response = buildClient(HttpStatusCode.OK, validPdlNavnResponse).personNavn(testFnr)
        val name = response
            ?.navn
            ?.firstOrNull()
        assertThat(name?.fornavn).isEqualTo("Ola")
        assertThat(name?.metadata?.master).isEqualTo("Freg")
    }

    @Test
    internal fun `Full Person returnerer en person ved gyldig respons fra PDL`() {
        val response = buildClient(HttpStatusCode.OK, validPdlFullPersonResponse).fullPerson(testFnr)
        val name = response
            ?.hentPerson
            ?.navn
            ?.firstOrNull()
            ?.fornavn

        assertThat(name).isEqualTo("NILS")
        assertThat(response?.hentIdenter?.identer).hasSize(2)
        assertThat(response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.AKTORID }).hasSize(1)
        assertThat(response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.FOLKEREGISTERIDENT }).hasSize(1)
        assertThat(response?.hentGeografiskTilknytning?.gtType).isEqualTo(PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.KOMMUNE)
        assertThat(response?.hentPerson?.foedsel?.firstOrNull()?.foedselsdato).isEqualTo(LocalDate.of(1984, 1, 31))
        assertThat(response?.hentPerson?.doedsfall).hasSize(0)
        assertThat(response?.hentPerson?.kjoenn?.firstOrNull()?.kjoenn).isEqualTo("MANN")
    }

    @Test
    internal fun `Kaster PdlException ved feilrespons fra PDL`() {
        assertThrows<PdlClient.PdlException> {
            buildClient(HttpStatusCode.OK, errorPdlResponse).personNavn(testFnr)
        }
    }
}
