package no.nav.helse.arbeidsgiver.web.auth

import no.nav.helse.arbeidsgiver.integrasjoner.altinn.AltinnOrganisasjon

interface AltinnAuthorizer {
    /**
     * Sjekker om den angitte identiteten har rettighet til å se refusjoner for den angitte arbeidsgiverId
     * En arbeidsgiverId kan være en virksomhet, en hovedenhet, et identitetsnummer på en privatperson eller et
     * organisasjonsledd.
     */
    fun hasAccess(identitetsnummer: String, arbeidsgiverId: String): Boolean
}

/**
 * Standard Altinn Authorizer som sjekker at
 *  * Den angitte brukeren har rettigheter til den angitte arbeidsgiverIDen
 *  * Den angitte arbeidsgiver IDen er en underenhet (virksomhet)
 */
class DefaultAltinnAuthorizer(private val authListRepo: AltinnOrganisationsRepository) : AltinnAuthorizer {
    override fun hasAccess(identitetsnummer: String, arbeidsgiverId: String): Boolean {
        return authListRepo.hentOrgMedRettigheterForPerson(identitetsnummer)
            .any {
                it.organizationNumber == arbeidsgiverId && it.parentOrganizationNumber != null
            }
    }
}

interface AltinnOrganisationsRepository {
    fun hentOrgMedRettigheterForPerson(identitetsnummer: String): Set<AltinnOrganisasjon>
}
