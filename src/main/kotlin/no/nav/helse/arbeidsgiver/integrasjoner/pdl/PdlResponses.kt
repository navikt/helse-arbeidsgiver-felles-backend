package no.nav.helse.arbeidsgiver.integrasjoner.pdl

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tilsvarer graphql-spørringen hentPersonNavn.graphql
 */
data class PdlHentPersonNavn(val hentPerson: PdlPersonNavneliste?) {
    data class PdlPersonNavneliste(val navn: List<PdlPersonNavn>) {
        data class PdlPersonNavn(val fornavn: String, val mellomnavn: String?, val etternavn: String, val metadata: PdlPersonNavnMetadata)
    }
}

/**
 * Tilsvarer graphql-spørringen hentFullPerson.graphql
 */
data class PdlHentFullPerson(val hentPerson: PdlFullPersonliste?, val hentIdenter: PdlIdentResponse?, val hentGeografiskTilknytning: PdlGeografiskTilknytning?) {

    data class PdlIdentResponse(val identer: List<PdlIdent>) {
        fun trekkUtIdent(gruppe: PdlIdent.PdlIdentGruppe): String? = identer.filter { it.gruppe == gruppe }.firstOrNull()?.ident
    }

    data class PdlGeografiskTilknytning(val gtType: PdlGtType, val gtKommune: String?, val gtBydel: String?, val gtLand: String?) {
        fun hentTilknytning(): String? {
            return when (gtType) {
                PdlGtType.KOMMUNE -> gtKommune
                PdlGtType.BYDEL -> gtBydel
                PdlGtType.UTLAND -> gtLand
                PdlGtType.UDEFINERT -> null
            }
        }
        enum class PdlGtType { KOMMUNE, BYDEL, UTLAND, UDEFINERT }
    }

    data class PdlFullPersonliste(
        val navn: List<PdlNavn>,
        val foedselsdato: List<PdlFoedsel>,
        val doedsfall: List<PdlDoedsfall>,
        val adressebeskyttelse: List<PdlAdressebeskyttelse>,
        val statsborgerskap: List<PdlStatsborgerskap>,
        val bostedsadresse: List<PdlBostedsadresse>,
        val kjoenn: List<PdlKjoenn>
    ) {

        fun trekkUtFulltNavn() = navn.map { "${it.fornavn} ${it.mellomnavn ?: ""} ${it.etternavn}".replace("  ", " ") }.firstOrNull()
        fun trekkUtKjoenn() = kjoenn.firstOrNull()?.kjoenn
        fun trekkUtDoedsfalldato() = doedsfall.firstOrNull()?.doedsdato
        fun trekkUtFoedselsdato() = foedselsdato.firstOrNull()?.foedselsdato
        fun trekkUtDiskresjonskode() = adressebeskyttelse.firstOrNull()?.getKodeverkDiskresjonskode()

        data class PdlNavn(
            val fornavn: String,
            val mellomnavn: String?,
            val etternavn: String,
            val metadata: PdlPersonNavnMetadata
        )

        data class PdlKjoenn(val kjoenn: String)
        data class PdlAdressebeskyttelse(val gradering: String) {
            fun getKodeverkDiskresjonskode(): String? {
                return when (gradering) {
                    "STRENGT_FORTROLIG" -> "SPSF"
                    "FORTROLIG" -> "SPFO"
                    else -> null
                }
            }
        }
        data class PdlFoedsel(val foedselsdato: LocalDate)
        data class PdlDoedsfall(val doedsdato: LocalDate)
        data class PdlStatsborgerskap(val land: String)
        data class PdlBostedsadresse(
            val gyldigFraOgMed: LocalDateTime?,
            val gyldigTilOgMed: LocalDateTime?,
            // For å hente ut om man er bosatt i norge hentes det ut om disse addressene finnes
            // dersom noden er null finnes ikke addressen
            val vegadresse: JsonNode?,
            val matrikkeladresse: JsonNode?,
            val ukjentBosted: JsonNode?,
            val angittFlyttedato: LocalDate? = null
        )
    }
}

data class PdlIdent(val ident: String, val gruppe: PdlIdentGruppe) {
    enum class PdlIdentGruppe { AKTORID, FOLKEREGISTERIDENT, NPID }
}

data class PdlPersonNavnMetadata(
    /**
     * Inneholder "freg" dersom "eieren" av informasjonen er folkeregisteret
     */
    val master: String
)

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
