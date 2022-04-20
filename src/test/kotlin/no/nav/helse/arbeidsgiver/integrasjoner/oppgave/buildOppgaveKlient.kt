package no.nav.helse.arbeidsgiver.integrasjoner.oppgave

import io.ktor.http.*
import io.mockk.mockk
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.integrasjoner.mockHttpClient
import no.nav.helse.arbeidsgiver.utils.loadFromResources
import java.time.LocalDate

internal fun buildClient(status: HttpStatusCode, content: String): OppgaveKlientImpl {
    return OppgaveKlientImpl(
        "url",
        mockk<AccessTokenProvider>(relaxed = true),
        mockHttpClient(status, content)
    )
}

val validResponse = "oppgave-mock-data/oppgave-success-response.json".loadFromResources()
val errorResponse = "oppgave-mock-data/oppgave-error-response.json".loadFromResources()

val oppgaveRequest = OpprettOppgaveRequest(
    aktoerId = "aktørId",
    journalpostId = "journalpostId",
    beskrivelse = "beskrivelse",
    tema = "SYK",
    oppgavetype = "ROB_BEH",
    behandlingstema = "ab0433",
    aktivDato = LocalDate.now(),
    fristFerdigstillelse = LocalDate.now().plusDays(7),
    prioritet = "NORM"
)
