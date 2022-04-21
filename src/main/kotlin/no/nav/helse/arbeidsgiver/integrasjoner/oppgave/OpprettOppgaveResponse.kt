package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import java.time.LocalDate

// https://oppgave.dev.adeo.no/#/Oppgave/opprettOppgave
data class OpprettOppgaveResponse(
    val id: Int,
    val tildeltEnhetsnr: String,
    val tema: String,
    val oppgavetype: String,
    val versjon: Int,
    val aktivDato: LocalDate,
    val prioritet: Prioritet,
    val status: Status
)

enum class Status { OPPRETTET, AAPNET, UNDER_BEHANDLING, FERDIGSTILT, FEILREGISTRERT }
enum class Prioritet { HOY, NORM, LAV }
