package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import java.time.LocalDate

open class PdlResponse<T>(
        open val errors: List<PdlError>?,
        open val data: T?
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

/**
 * Tilsvarer graphql-spørringen hentPersonNavn.graphql
 */
data class PdlHentPersonNavn(val hentPerson: PdlPersonNavneliste?) {
        data class PdlPersonNavneliste(val navn: List<PdlPersonNavn>) {
                data class PdlPersonNavn(val fornavn: String,val mellomnavn: String?,val etternavn: String,val metadata: PdlPersonNavnMetadata)
        }
}


/**
 * Tilsvarer graphql-spørringen hentFullPerson.graphql
 */
data class PdlHentFullPerson(val hentPerson: PdlFullPersonliste?, val hentIdenter: PdlIdentResponse?) {

        data class PdlIdentResponse(val identer: List<PdlIdent>)

        data class PdlFullPersonliste(
                val navn: List<PdlFullPerson>,
                val foedsel: List<PdlFoedsel>,
                val doedsfall: List<PdlDoedsfall>,
                val adressebeskyttelse: List<PdlAdressebeskyttelse>,
                val kjoenn: List<PdlKjoenn>) {

                data class PdlFullPerson(
                        val fornavn: String,
                        val mellomnavn: String?,
                        val etternavn: String,
                        val metadata: PdlPersonNavnMetadata
                )

                data class PdlKjoenn(val kjoenn: String)
                data class PdlAdressebeskyttelse(val gradering: String)
                data class PdlFoedsel(val foedselsdato: LocalDate)
                data class PdlDoedsfall(val doedsdato: LocalDate)
        }
}

data class PdlIdent(val ident: String, val gruppe: PdlIdentGruppe) {
        enum class PdlIdentGruppe { AKTORID, FOLKEREGISTERIDENT, NPID }
}

data class PdlPersonNavnMetadata(
        /**
         * Inneholder "Freg" dersom "eieren" av informasjonen er folkeregisteret
         */
        val master: String
)