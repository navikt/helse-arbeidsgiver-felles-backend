package no.nav.helse.arbeidsgiver.integrasjoner.aareg

import java.time.LocalDate

data class Arbeidsforhold(
    val arbeidsgiver: Arbeidsgiver,
    val opplysningspliktig: Opplysningspliktig,
    val arbeidsavtaler: List<Arbeidsavtale>
)

class Arbeidsavtale(
    val stillingsprosent: Double,
    val gyldighetsperiode: Gyldighetsperiode
)

data class Arbeidsgiver(
    val type: String,
    val organisasjonsnummer: String?
)

data class Gyldighetsperiode(
    val fom: LocalDate?,
    val tom: LocalDate?
)

data class Opplysningspliktig(
    val type: String,
    val organisasjonsnummer: String?
)
