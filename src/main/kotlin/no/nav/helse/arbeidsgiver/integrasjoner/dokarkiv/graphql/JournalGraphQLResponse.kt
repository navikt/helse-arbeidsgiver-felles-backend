package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.graphql

import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlError
import java.time.LocalDateTime

//https://confluence.adeo.no/display/BOA/Query:+journalpost
data class JournalPost (
    val journalpostId: String?,
    val tittel: String,
    val journalposttype: Journalposttype,
    val journalstatus: Journalstatus,
    val tema: Tema,
    val temanavn: String,
    val behandlingstema: String,
    val behandlingstemanavn: String,
    val sak: Sak,
    val bruker: Bruker,
    val avsenderMottaker: AvsenderMottaker,
    val journalfoerendeEnhet: String,
    val journalfortAvNavn: String,
    val opprettetAvNavn: String,
    val kanal: Kanal,
    val kanalnavn: String,
    val skjerming: String,
    val datoOpprettet: LocalDateTime?,
    val relevanteDatoer: List<RelevantDato>,
    val antallRetur: String,
    val eksternReferanseId: String,
    val tilleggsopplysninger: List<Tilleggsopplysning>,
    val dokumenter: List<DokumentInfo>) {
    enum class Journalposttype {I,U,N}

    data class Bruker(val id: String,val type: BrukerIdType)
    data class Sak(val datoOpprettet: LocalDateTime, val fagsakId: String, val fagsaksystem: String,
                   val sakstype: Sakstype, val tema: Tema
    ) {
        enum class Sakstype { GENERELL_SAK, FAGSAK}
    }
    data class AvsenderMottaker (
        val id: String,
        val type: AvsenderMottakerIdType,
        val navn: String,
        val land: String,
        val erLikBruker: Boolean?) {
        enum class AvsenderMottakerIdType { FNR, ORGNR, HPRNR, UTL_ORG, NULL,UKJENT }
    }


}

//https://confluence.adeo.no/display/BOA/Type:+RelevantDato
data class RelevantDato(
    /**
     * En kalenderdato med tid, trunkert til nærmeste sekund. YYYY-MM-DD'T'hh:mm:ss. Eksempel: 2018-01-01T12:00:00.
     */
    val dato : LocalDateTime,
    /**
     * datotype
     */
    val datotype : Datotype
)
//https://confluence.adeo.no/display/BOA/Type:+Tilleggsopplysning
data class Tilleggsopplysning(
    /**
     * Nøkkelen til det fagspesifikke attributtet.
     */
    val nokkel : String,
    /**
     * Verdien til det fagspesifikke attributtet.
     */
    val verdi: String)

//https://confluence.adeo.no/display/BOA/Type:+DokumentInfo
data class DokumentInfo(
    /**
     * Unik identifikator per dokumentinfo
     */
    val dokumentInfoId : String,
    /**
     * Kode som sier noe om dokumentets innhold og oppbygning.
     */
    val brevkode: String,
    /**
     * Dokumentstatus gir et indikasjon på hvorvidt dokumentet er ferdigstilt eller under arbeid, eventuelt avbrutt.
     */
    val dokumentstatus: Dokumentstatus,
    /**
     * Dato dokumentet ble ferdigstilt.
     */
    val datoFerdigstilt: LocalDateTime,
    /**
     * peker på den journalposten som dokumentene var knyttet til på arkiveringstidspunktet
     */
    val originalJournalpostId: String,
    /**
     * Uttrykker at dokumentet er unntatt ordinær saksbehandling.
     */
    val skjerming : Skjermingtype,
    val logiskeVedlegg: LogiskVedlegg,
    val dokumentvarianter : Dokumentvariant
)
//https://confluence.adeo.no/display/BOA/Type:+LogiskVedlegg
data class LogiskVedlegg(
    /**
     * Unik identifikator per logisk vedlegg
     */
    val logiskVedleggId : String,
    /**
     * Tittel på det logiske vedlegget
     */
    val tittel : String
)

//https://confluence.adeo.no/display/BOA/Type:+Dokumentvariant
data class Dokumentvariant(
    val variantformat : Variantformat,
    val filnavn : String,
    val saksbehandlerHarTilgang : String,
    val skjerming: Skjermingtype
)

open class JournalPostResponse<T>(
    open val errors: List<JournalPostError>?,
    open val data: T?
)
data class JournalPostError(
    val message: String,
    val locations: List<JournalPostErrorLocation>,
    val path: List<String>?,
    val exceptionType: String,
    val exception: String
)
data class JournalPostErrorLocation(
    val line: Int?,
    val column: Int?
)