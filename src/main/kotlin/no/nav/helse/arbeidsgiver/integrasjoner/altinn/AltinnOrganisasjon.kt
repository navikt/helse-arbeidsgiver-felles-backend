package no.nav.helse.arbeidsgiver.integrasjoner.altinn

data class AltinnOrganisasjon(
        val name: String,
        val type: String,
        val parentOrganizationNumber: String? = null,
        val organizationForm: String? = null,
        val organizationNumber: String? = null,
        val socialSecurityNumber: String? = null,
        val status: String? = null
)