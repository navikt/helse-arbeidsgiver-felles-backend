package no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.graphql

import java.time.LocalDateTime

enum class Journalstatus {
    /**
     * Journalposten er mottatt, men ikke journalført.
     */
    MOTTATT,

    /**
     * Journalposten er ferdigstilt og ansvaret for videre behandling av forsendelsen er overført til fagsystemet. Journalen er i prinsippet låst for videre endringer.
     */
    JOURNALFOERT,

    /**
     * Journalposten med tilhørende dokumenter er ferdigstilt, og journalen er i prinsippet låst for videre endringer.
     *  FERDIGSTILT tilsvarer statusen JOURNALFØRT for inngående dokumenter.
     */
    FERDIGSTILT,

    /**
     * Dokumentet er sendt til bruker.
     */
    EKSPEDERT,

    /**
     * Journalposten er opprettet i arkivet, men fremdeles under arbeid.
     */
    UNDER_ARBEID,

    /**
     * Journalposten har blitt arkivavgrenset etter at den feilaktig har blitt knyttet til en sak.
     */
    FEILREGISTRERT,

    /**
     * Journalposten er arkivavgrenset grunnet en feilsituasjon, ofte knyttet til skanning eller journalføring.
     */
    UTGAAR,

    /**
     * Utgående dokumenter og notater kan avbrytes mens de er under arbeid, og ikke enda er ferdigstilt.
     */
    AVBRUTT,

    /**
     *  Journalposten har ikke noen kjent bruker.
     */
    UKJENT_BRUKER,

    /**
     * Statusen benyttes bl.a. i forbindelse med brevproduksjon for å reservere 'plass' i journalen for dokumenter som skal populeres på et senere tidspunkt.
     */
    RESERVERT,

    /**
     * Midlertidig status på vei mot MOTTATT.
     */
    OPPLASTING_DOKUMENT,

    /**
     * Dersom statusfeltet i Joark er tomt, mappes dette til "UKJENT"
     */
    UKJENT
}

enum class Tema {
    /**Arbeidsavklaringspenger*/
    AAP,
    /**	Aa-registeret*/
    AAR,
    /**Ajourhold - Grunnopplysninger*/
    AGR,
    /**	Barnetrygd*/
    BAR,
    /**Bidrag*/
    BID,
    /**Bil*/
    BIL,
    /**Dagpenger*/
    DAG,
    /**Enslig forsørger*/
    ENF,
    /**Erstatning*/
    ERS,
    /**Farskap*/
    FAR,
    /**Feilutbetaling*/
    FEI,
    /**Foreldre- og svangerskapspenger*/
    FOR,
    /**Forsikring*/
    FOS,
    /**Fullmakt*/
    FUL,
    /**Kompensasjon for selvstendig næringsdrivende/frilansere*/
    FRI,
    /**Generell*/
    GEN,
    /**Gravferdsstønad*/
    GRA,
    /**Grunn- og hjelpestønad*/
    GRU,
    /**Helsetjenester og ortopediske hjelpemidler*/
    HEL,
    /**Hjelpemidler*/
    HJE,
    /**Inkluderende arbeidsliv*/
    IAR,
    /**Tiltakspenger*/
    IND,
    /**Kontantstøtte*/
    KON,
    /**Kontroll*/
    KTR,
    /**Medlemskap*/
    MED,
    /**Mobilitetsfremmende stønad*/
    MOB,
    /**Omsorgspenger, pleiepenger og opplæringspenger*/
    OMS,
    /**Oppfølging - Arbeidsgiver*/
    OPA,
    /**Oppfølging*/
    OPP,
    /**Pensjon*/
    PEN,
    /**Permittering og masseoppsigelser*/
    PER,
    /**Rehabilitering*/
    REH,
    /**Rekruttering og stilling*/
    REK,
    /**Retting av personopplysninger*/
    RPO,
    /**Rettferdsvederlag*/
    RVE,
    /**Sanksjon - Arbeidsgiver*/
    SAA,
    /**Saksomkostninger*/
    SAK,
    /**Sanksjon - Person*/
    SAP,
    /**Serviceklager*/
    SER,
    /**Sikkerhetstiltak*/
    SIK,
    /**Regnskap/utbetaling	Returneres for tema OKO og STO*/
    STO,
    /**Supplerende stønad*/
    SUP,
    /**Sykepenger*/
    SYK,
    /**Sykmeldinger*/
    SYM,
    /**Tiltak*/
    TIL,
    /**Trekkhåndtering*/
    TRK,
    /**Trygdeavgift*/
    TRY,
    /**Tilleggsstønad*/
    TSO,
    /**Tilleggsstønad arbeidssøkere*/
    TSR,
    /**Unntak fra medlemskap*/
    UFM,
    /**Uføretrygd*/
    UFO,
    /**Ukjent*/
    UKJ,
    /**Ventelønn*/
    VEN,
    /**Yrkesrettet attføring*/
    YRA,
    /**Yrkesskade / Menerstatning*/
    YRK
}

/**
 * https://confluence.adeo.no/display/BOA/Enum:+Kanal
 */
enum class Kanal {
    ALTINN,
    EIA,
    NAV_NO,
    NAV_NO_UINNLOGGET,
    NAV_NO_CHAT,
    SKAN_NETS,
    SKAN_PEN,
    SKAN_IM,
    INNSENDT_NAV_ANSATT,
    EESSI,
    EKST_OPPS,
    SENTRAL_UTSKRIFT,
    LOKAL_UTSKRIFT,
    SDP,
    TRYGDERETTEN,
    HELSENETTET,
    INGEN_DISTRIBUSJON,
    UKJENT
}

//https://confluence.adeo.no/display/BOA/Enum:+Skjermingtype
enum class Skjermingtype{
    /**
     * Indikerer at det er fattet et vedtak etter personopplysningsloven (GDPR - brukers rett til å bli glemt).
     */
    POL,

    /**
     * Indikerer at det har blitt gjort en feil under mottak, journalføring eller brevproduksjon, slik at journalposten eller dokumentene er markert for sletting.
     */
    FEIL
}

//https://confluence.adeo.no/display/BOA/Enum:+Datotype
enum class Datotype{
    /**
     * Tidspunktet dokumentene på journalposten ble sendt til print.Returneres for utgående journalposter
     */
    DATO_SENDT_PRINT,
    /**
     * Tidspunktet dokumentene på journalposten ble sendt til bruker. Returneres for utgående journalposter
     */
    DATO_EKSPEDERT,
    /**
     * Tidspunktet journalposten ble journalført (inngående) eller ferdigstilt (utgående).Returneres for alle journalposttyper
     */
    DATO_JOURNALFOERT,
    /**
     * Tidspunkt dokumentene i journalposten ble registrert i NAV sine systemer. Returneres for inngående journalposter
     */
    DATO_REGISTRERT,
    /**
     * Tidspunkt som dokumentene i journalposten ble sendt på nytt, grunnet retur av opprinnelig forsendelse. Returneres for utgående journalposter
     */
    DATO_AVS_RETUR,
    /**
     * Dato på hoveddokumentet i forsendelsen. Registreres i noen tilfeller manuelt av saksbehandler.Returneres for alle journalposter
     */
    DATO_DOKUMENT
}
//https://confluence.adeo.no/display/BOA/Enum:+Dokumentstatus
enum class Dokumentstatus{
    /**
     * Dokumentet er ferdigstilt. Benyttes for redigerbare brev.
     */
    FERDIGSTILT,

    /**
     * Dokumentet ble opprettet, men ble avbrutt under redigering. Benyttes for redigerbare brev.
     */
    AVBRUTT,

    /**
     * Dokumentet er under arbeid. Benyttes for redigerbare brev.
     */
    UNDER_REDIGERING,

    /**
     * Dokumentet er kassert, altså slettet eller makulert. I en periode kan et dokument være 'logisk kassert'.
     */
    KASSERT
}

//https://confluence.adeo.no/display/BOA/Enum:+Variantformat
enum class Variantformat{
    ARKIV, FULLVERSJON, PRODUKSJON, PRODUKSJON_DLF, SLADDET, ORIGINAL
}

enum class BrukerIdType {
    /**
     * NAV aktørid for en person.
     */
    AKTOERID,
    /**
     * Folkeregisterets fødselsnummer eller d-nummer for en person.
     */
    FNR,
    /**
     * Foretaksregisterets organisasjonsnummer for en juridisk person.
     */
    ORGNR
}