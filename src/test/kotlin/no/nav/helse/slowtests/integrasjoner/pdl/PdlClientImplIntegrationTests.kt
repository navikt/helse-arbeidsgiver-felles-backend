package no.nav.helse.slowtests.integrasjoner.pdl

import no.nav.helse.arbeidsgiver.integrasjoner.RestStsClient
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClientImpl
import no.nav.helse.slowtests.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Utforskningstestet ment for å kjøres mot faktisk PDL i Q på lokal maskin via NaisDevice.
 *
 * For å kjøre denne må du hente ut et STS token (access_token verdien) i VMWare ved å
 * bruke servicebrukeren og passordet til appen du tester for.
 *
 * Dette kan gjøres via curl:
 *
 * curl -X GET "https://security-token-service.nais.preprod.local/rest/v1/sts/token?grant_type=client_credentials&scope=openid" -H  "accept: application/json" --user srvusername:srvPASSWORD --insecure
 *
 * Servicebrukerens passord finner du i vault.
 *
 *
 * NB!! Ikke sjekk inn passord eller Tokens i Git.
 *
 */
@Disabled
internal class PdlClientImplIntegrationTests {

    val pdlKlient = PdlClientImpl(
            "https://pdl-api.dev-fss.nais.io/graphql",
            object: RestStsClient {
                override fun getOidcToken() = "eyJraWQiOiIzNmU3YjU2NC02YWJmLTRiYzgtYjA4Mi00ODQ3NTJiMTc2MzkiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZoZWxzZXNwaW9uIiwiYXVkIjpbInNydmhlbHNlc3Bpb24iLCJwcmVwcm9kLmxvY2FsIl0sInZlciI6IjEuMCIsIm5iZiI6MTYwMzQ0MzI4OCwiYXpwIjoic3J2aGVsc2VzcGlvbiIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE2MDM0NDMyODgsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTYwMzQ0Njg4OCwiaWF0IjoxNjAzNDQzMjg4LCJqdGkiOiJmNWQ1NWNkMy00MTVlLTQ0NTktYWI4MS1lZmNmNjA3ZGExMzMifQ.mskVuv7tv7wUDmvVFummd9qFHFZ_uVEFQPjjrAtdTX5LpobPv50hVufQjXqVMYM5eB09m4Ixk0qcy_LEDfPrQ0yTVhfj7JKrsCXKUS7l2xahRjiOkOxHOy0TLEUEYLGc8Q84fi-x4HBgHGpqsvyhER73tXVgXHtBLAEI15LHhf1SX8RAD1iv0AYOeCEvHwYYpcDOG3c6eA34jn32LT9RweqIvu5FK1lv9KEzLIXi_CLb6SC3iE2Z_C_Fb8ANIrH01zpRfd-qLZueeFu29PIx7I548moUEJ1mjwuyc42pqTwXSHaeSngAnoCkEfJ_rdI4uOVHqkIBAvsRO37MyUYpgQ" // Sett inn token fra STS
            },
            TestUtils.commonHttpClient(),
            TestUtils.commonObjectMapper()
    )

    @Test
    internal fun invokePersonName() {
        val person = pdlKlient.personNavn("2649500819544")
        assertThat(person).isNotNull()
    }

    @Test
    internal fun invokeFullPerson() {
        val person = pdlKlient.fullPerson("09127821914")
        assertThat(person).isNotNull()
    }

}