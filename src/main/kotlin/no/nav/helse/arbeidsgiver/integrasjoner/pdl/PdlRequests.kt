package no.nav.helse.arbeidsgiver.integrasjoner.pdl

data class PdlQueryObject(
    val query: String,
    val variables: Variables
)

data class Variables(
    val ident: String
)
