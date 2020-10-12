package no.nav.helse.arbeidsgiver.integrasjoner.altinn

/**
 * En organisasjon slik de blir returnert fra Altinn.

 * Name	String	The name of the reportee.
 * Type	String	The type of reportee. Value depends on the language choice of the user.
 * English: Enterprise | Business | Person
 * Bokmål: Foretak | Bedrift | Person
 * Nynorsk: Føretak | Bedrift | Person
 * Status	String	Indicates whether the organization is active or inactive. An organization can be inactive if it for some reason (e.g. bankruptcy) has ceased. This is populated only if the reportee is an organization.
 * OrganizationNumber	String	The organization number of the reportee. This is populated only if the reportee is an organization.
 * ParentOrganizationNumber	String	The parent organization number of the reportee. This is populated only if the reportee is an organization, and the organization is a suborganization.
 * TypeOfOrganization	String	The type of organization for the reportee. This is populated only if the reportee is an organization. E.g ENK, AS, ORGL.
 * SocialSecurityNumber	String	The social security number of the reportee. This is populated only if the reportee is a person.
 */
data class AltinnOrganisasjon(
        val name: String,
        val type: String,
        val parentOrganizationNumber: String? = null,
        val organizationForm: String? = null,
        val organizationNumber: String? = null,
        val socialSecurityNumber: String? = null,
        val status: String? = null
)