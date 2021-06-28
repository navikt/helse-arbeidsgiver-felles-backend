package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.graphql

data class JournalPostQueryObject(
    val query: String,
    val queryVariables: QueryVariables
)

data class QueryVariables(
    val journalPostId: String
)