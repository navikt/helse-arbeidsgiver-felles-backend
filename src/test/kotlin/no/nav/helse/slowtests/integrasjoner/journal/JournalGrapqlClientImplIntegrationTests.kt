package no.nav.helse.slowtests.integrasjoner.journal

import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.graphql.DokarkivGraphQLClientImpl
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
internal class JournalGraphqlClientImplIntegrationTests {

    val journalKlient = DokarkivGraphQLClientImpl(
            "https://saf-q1.nais.preprod.local/graphiql",
            object: AccessTokenProvider {
                override fun getToken() = "" // Sett inn token fra STS
            },
            TestUtils.commonHttpClient(),
            TestUtils.commonObjectMapper()
    )

    @Test
    internal fun hentJournalPost() {
    /*    val journalpost = journalKlient.getJournalpost()
        assertThat(journalpost).isNotNull()*/
    }
}