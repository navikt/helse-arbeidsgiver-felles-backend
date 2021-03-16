package no.nav.helse.arbeidsgiver.integrasjoner.aareg

import java.time.LocalDate

data class Arbeidsforhold(
    val arbeidsgiver: Arbeidsgiver,
    val opplysningspliktig: Opplysningspliktig,
    val arbeidsavtaler: List<Arbeidsavtale>,
    val ansettelsesperiode: Ansettelsesperiode
)

class Arbeidsavtale(
    val stillingsprosent: Double,
    val gyldighetsperiode: Periode
)

class Ansettelsesperiode(
    val periode: Periode
)

data class Arbeidsgiver(
    val type: String,
    val organisasjonsnummer: String?
)

data class Periode(
    val fom: LocalDate?,
    val tom: LocalDate?
)

data class Opplysningspliktig(
    val type: String,
    val organisasjonsnummer: String?
)
