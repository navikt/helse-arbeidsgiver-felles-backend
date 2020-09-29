package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv

import java.time.LocalDate

/**
 * Oppretter en journalpost i Joark/dokarkiv, med eller uten dokumenter.
 *
 * Fullstendig dokumentasjon her: https://confluence.adeo.no/display/BOA/opprettJournalpost
 */
data class JournalpostRequest(
        val tema: String = "SYK",
        val bruker: Bruker,
        val journalposttype: Journalposttype,
        val avsenderMottaker: AvsenderMottaker,

        /**
         * Tittel som beskriver forsendelsen samlet, feks "Ettersendelse til søknad om foreldrepenger".
         */
        val tittel: String,

        /**
         * NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen.
         * Ved automatisk journalføring uten mennesker involvert skal enhet settes til "9999".
         * Konsument må sette journalfoerendeEnhet dersom tjenesten skal ferdigstille journalføringen.
         */
        val journalfoerendeEnhet: String = "9999",

        /**
         * Hvilken kanal kommunikasjonen har foregått i, feks ALTINN, NAV_NO.
         * Liste over gyldige verdier:
         * https://confluence.adeo.no/display/BOA/Mottakskanal
         * https://confluence.adeo.no/display/BOA/Utsendingskanal
         */
        val kanal: String,

        /**
         * Unik id for forsendelsen som kan brukes til sporing gjennom verdikjeden.
         */
        val eksternReferanseId: String,

        val dokumenter: List<Dokument>,
        val sak: Sak = Sak.GENERELL,
        val datoMottatt: LocalDate
)

data class Dokument(
        /**
         * Kode som sier noe om dokumentets innhold og oppbygning.
         * For inngående dokumenter kan brevkoden være en NAV-skjemaID f.eks. "NAV 14-05.09" eller en SED-id.
         */
        val brevkode: String,

        /**
         * Dokumentets tittel, f.eks. "Søknad om foreldrepenger ved fødsel" eller "Legeerklæring".
         * Dokumentets tittel blir synlig i brukers journal på nav.no, samt i NAVs fagsystemer.
         */
        val tittel: String,

        /**
         * De forskjellige varientene av samme dokument, feks kan et dokument ha en XML variant og en PDF-variant.
         */
        val dokumentVarianter: List<DokumentVariant>
)

/**
 * Holder et dokument som skal journalføres som en Base64 enkodet string
 */
data class DokumentVariant(

        /**
         * Gyldige filtyper: https://confluence.adeo.no/display/BOA/Filtype
         */
        val filtype: String = "PDFA",

        /**
         * Dokumentet  som en Base64-enkodet string
         */
        val fysiskDokument: String,

        /**
         * Gyldige verdier: https://confluence.adeo.no/display/BOA/Variantformat
         */
        val variantFormat: String = "ARKIV"
)

/**
 * Bruker er den posteringen gjelder
 */
data class Bruker(
        /**
         * Org nummer eller FNR
         */
        val id: String,

        /**
         * Hva som er i id-feltet
         */
        val idType: IdType
)

/**
 * AvsenderMottaker er den som enten har mottatt (NAV er ansender)
 * eller den som har sendt (NAV er mottaker)
 *
 * Dette avgjøres av feltet
 */
data class AvsenderMottaker(
        /**
         * Org nummer eller FNR
         */
        val id: String,

        /**
         * Hva som er i id-feltet
         */
        val idType: IdType,

        /**
         * Navn er påkrevd for ferdigstilling, enten personnavn eller virksomhetsnavn
         */
        val navn: String
)

enum class IdType {
    FNR, ORGNR, HPRNR, UTL_ORG
}

enum class Journalposttype {
    /**
     * INNGAAENDE brukes for dokumentasjon som NAV har mottatt fra en ekstern part.
     * Dette kan være søknader, ettersendelser av dokumentasjon til sak eller meldinger fra arbeidsgivere.
     */
    INNGAAENDE,

    /**
     * UTGAAENDE brukes for dokumentasjon som NAV har produsert og sendt ut til en ekstern part.
     * Dette kan for eksempel være informasjons- eller vedtaksbrev til privatpersoner eller organisasjoner.
     */
    UTGAAENDE,

    /**
     * NOTAT brukes for dokumentasjon som NAV har produsert selv og uten mål om å distribuere dette ut av NAV.
     * Eksempler på dette er forvaltningsnotater og referater fra telefonsamtaler med brukere.
     */
    NOTAT
}

data class Sak(
        val sakstype: SaksType,
        val fagsakId: String?,
        /**
         * Liste over gyldige verdier: https://confluence.adeo.no/display/BOA/opprettJournalpost
         */
        val fagsaksystem: String?
) {
    companion object  {
        val GENERELL = Sak(SaksType.GENERELL_SAK, null, null)
    }


    enum class SaksType {
        /**
         * FAGSAK vil si at dokumentene tilhører en sak i et fagsystem.
         * Dersom FAGSAK velges, må fagsakid og fagsaksystem oppgis.
         */
        FAGSAK,

        /**
         * GENERELL_SAK kan brukes for dokumenter som skal journalføres, men som ikke tilhører en konkret fagsak.
         * Generell sak kan ses på som brukerens "mappe" på et gitt tema.
         */
        GENERELL_SAK
    }
}