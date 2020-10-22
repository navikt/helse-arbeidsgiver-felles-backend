package no.nav.helse.arbeidsgiver.integrasjoner.pdl

data class PdlPersonResponse(
        val errors: List<PdlError>?,
        val data: PdlHentPerson?
)

data class PdlError(
        val message: String,
        val locations: List<PdlErrorLocation>,
        val path: List<String>?,
        val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
        val line: Int?,
        val column: Int?
)

data class PdlErrorExtension(
        val code: String?,
        val classification: String
)

data class PdlHentPerson(
        val hentPerson: PdlPerson?
)

data class PdlPerson(
        val navn: List<PdlPersonNavn>
)

data class PdlPersonNavn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val metadata: PdlPersonNavnMetadata
)

data class PdlPersonNavnMetadata(
        /**
         * Inneholder "Freg" dersom "eieren" av informasjonen er folkeregisteret
         */
        val master: String
)