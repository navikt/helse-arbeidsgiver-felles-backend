package no.nav.helse.arbeidsgiver.integrasjoner.aareg

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Arbeidsforhold(
    val navArbeidsforholdId: Int?,
    val arbeidsforholdId: String?,
    val arbeidstaker: Any?,
    val arbeidsgiver: Arbeidsgiver,
    val opplysningspliktig: Opplysningspliktig,
    val arbeidsavtaler: List<Arbeidsavtale>,
    val ansettelsesperiode: Ansettelsesperiode,
    val registrert: LocalDateTime
)

data class Arbeidsavtale(
    val type: String,
    val arbeidstidsordning: String,
    val yrke: String,
    val stillingsprosent: Double,
    val antallTimerPrUke: Double,
    val beregnetAntallTimerPrUke: Double,
    val bruksperiode: Periode,
    val gyldighetsperiode: Periode,
    val sporingsinformasjon: Any
)

data class Ansettelsesperiode(
    val periode: Periode,
    val bruksperiode: Periode,
    val sporingsinformasjon: Any?
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
