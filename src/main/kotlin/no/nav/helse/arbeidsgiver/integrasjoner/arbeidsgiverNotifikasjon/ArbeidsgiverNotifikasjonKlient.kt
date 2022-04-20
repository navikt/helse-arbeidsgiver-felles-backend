package no.nav.helse.arbeidsgiver.integrasjoner.arbeidsgiverNotifikasjon

import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders.Authorization
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.helsearbeidsgiver.graphql.generated.*
import no.nav.helse.helsearbeidsgiver.graphql.generated.enums.SaksStatus
import no.nav.helse.helsearbeidsgiver.graphql.generated.nystatussak.Konflikt
import no.nav.helse.helsearbeidsgiver.graphql.generated.nystatussak.NyStatusSakResultat
import no.nav.helse.helsearbeidsgiver.graphql.generated.nystatussak.NyStatusSakVellykket
import no.nav.helse.helsearbeidsgiver.graphql.generated.nystatussak.SakFinnesIkke
import no.nav.helse.helsearbeidsgiver.graphql.generated.oppgaveutfoert.OppgaveUtfoertResultat
import no.nav.helse.helsearbeidsgiver.graphql.generated.oppgaveutfoert.OppgaveUtfoertVellykket
import no.nav.helse.helsearbeidsgiver.graphql.generated.opprettnyoppgave.NyOppgaveResultat
import no.nav.helse.helsearbeidsgiver.graphql.generated.opprettnyoppgave.NyOppgaveVellykket
import no.nav.helse.helsearbeidsgiver.graphql.generated.opprettnysak.*
import no.nav.helse.helsearbeidsgiver.graphql.generated.softdeletesak.SoftDeleteSakResultat
import no.nav.helse.helsearbeidsgiver.graphql.generated.softdeletesak.SoftDeleteSakVellykket
import org.slf4j.LoggerFactory
import java.net.URL
import no.nav.helse.helsearbeidsgiver.graphql.generated.oppgaveutfoert.NotifikasjonFinnesIkke as OppgaveUtfoertNotifikasjonFinnesIkke
import no.nav.helse.helsearbeidsgiver.graphql.generated.oppgaveutfoert.UgyldigMerkelapp as OppgaveUtfoertUgyldigMerkelapp
import no.nav.helse.helsearbeidsgiver.graphql.generated.oppgaveutfoert.UkjentProdusent as OppgaveUtfoertUkjentProdusent
import no.nav.helse.helsearbeidsgiver.graphql.generated.opprettnyoppgave.DuplikatEksternIdOgMerkelapp as NyOppgaveDuplikatEksternIdOgMerkelapp
import no.nav.helse.helsearbeidsgiver.graphql.generated.opprettnyoppgave.UgyldigMerkelapp as NyOppgaveUgyldigMerkelapp
import no.nav.helse.helsearbeidsgiver.graphql.generated.opprettnyoppgave.UgyldigMottaker as NyOppgaveUgyldigMottaker
import no.nav.helse.helsearbeidsgiver.graphql.generated.opprettnyoppgave.UkjentProdusent as NyOppgaveUkjentProdusent
import no.nav.helse.helsearbeidsgiver.graphql.generated.opprettnyoppgave.UkjentRolle as NyOppgaveUkjentRolle
import no.nav.helse.helsearbeidsgiver.graphql.generated.softdeletesak.SakFinnesIkke as SoftDeleteSakFinnesIkke
import no.nav.helse.helsearbeidsgiver.graphql.generated.softdeletesak.UgyldigMerkelapp as SoftDeleteUgyldigMerkelapp
import no.nav.helse.helsearbeidsgiver.graphql.generated.softdeletesak.UkjentProdusent as SoftDeleteUkjentProdusent

/**
 * Lager notfikasjoner og saker for arbeidsgivere slik at de får med seg viktig informasjon og oppgaver som venter.
 * Se dokumentasjon: https://navikt.github.io/arbeidsgiver-notifikasjon-produsent-api/
 * https://github.com/navikt/arbeidsgiver-notifikasjon-produsent-api
 */

interface ArbeidsgiverNotifikasjonKlient {
    fun whoami(): String?

    fun opprettNySak(
        grupperingsid: String,
        merkelapp: String,
        virksomhetsnummer: String,
        tittel: String,
        lenke: String,
        tidspunkt: ISO8601DateTime? = null
    ): NySakResultat?

    fun nySakStatus(
        id: String,
        status: SaksStatus
    ): NyStatusSakResultat?

    fun oppgaveUtfoert(
        id: String
    ): OppgaveUtfoertResultat?

    fun opprettNyOppgave(
        eksternId: String,
        lenke: String,
        tekst: String,
        virksomhetsnummer: String,
        merkelapp: String,
        tidspunkt: ISO8601DateTime? = null
    ): NyOppgaveResultat?

    fun softDeleteSak(id: String): SoftDeleteSakResultat?
}

class ArbeidsgiverNotifikasjonKlientImpl(
    url: String = "https://notifikasjon-fake-produsent-api.labs.nais.io/",
    httpClient: HttpClient,
    private val tokenClient: AccessTokenProvider
) : ArbeidsgiverNotifikasjonKlient {
    private val logger: org.slf4j.Logger = LoggerFactory.getLogger("ArbeidsgiverNotifikasjonKlient")

    private val client = GraphQLKtorClient(
        url = URL(url),
        httpClient = httpClient
    )

    override fun whoami(): String? {
        val query = Whoami()
        val accessToken = tokenClient.getToken()

        logger.info("Henter 'whoami' info fra arbeidsgiver-notifikasjon-api")

        val resultat = runBlocking {
            client.execute(query) {
                header(Authorization, "Bearer $accessToken")
            }
        }
        logger.info("Whoami: ${resultat.data?.whoami}")
        return resultat.data?.whoami
    }

    override fun opprettNyOppgave(
        eksternId: String,
        lenke: String,
        tekst: String,
        virksomhetsnummer: String,
        merkelapp: String,
        tidspunkt: ISO8601DateTime?
    ): NyOppgaveResultat? {
        val accessToken = tokenClient.getToken()
        val query = OpprettNyOppgave(
            variables = OpprettNyOppgave.Variables(
                eksternId,
                lenke,
                tekst,
                virksomhetsnummer,
                merkelapp,
                tidspunkt
            )
        )

        logger.info("Forsøker å opprette ny oppgave mot arbeidsgiver-notifikasjoner")

        val resultat = runBlocking {
            client.execute(query) {
                header(Authorization, "Bearer $accessToken")
            }
        }
        val nyOppgave = resultat.data?.nyOppgave

        if (nyOppgave !is NyOppgaveVellykket) {
            when (nyOppgave) {
                is NyOppgaveUgyldigMottaker -> {
                    logger.error("Feilmelding {}", nyOppgave.feilmelding)
                    throw OpprettNyOppgaveFeiletException(nyOppgave.feilmelding)
                }
                is NyOppgaveUkjentRolle -> {
                    logger.error("Feilmelding {}", nyOppgave.feilmelding)
                    throw OpprettNyOppgaveFeiletException(nyOppgave.feilmelding)
                }
                is NyOppgaveUgyldigMerkelapp -> {
                    logger.error("Feilmelding {}", nyOppgave.feilmelding)
                    throw OpprettNyOppgaveFeiletException(nyOppgave.feilmelding)
                }
                is NyOppgaveUkjentProdusent -> {
                    logger.error("Feilmelding {}", nyOppgave.feilmelding)
                    throw OpprettNyOppgaveFeiletException(nyOppgave.feilmelding)
                }
                is NyOppgaveDuplikatEksternIdOgMerkelapp -> {
                    logger.error("Feilmelding {}", nyOppgave.feilmelding)
                    throw OpprettNyOppgaveFeiletException(nyOppgave.feilmelding)
                }
            }
            logger.error("Kunne ikke opprette ny oppgave", nyOppgave)
            throw OpprettNySakFeiletException("ukjent feil")
        }
        logger.info("Opprettet ny oppgave {}", nyOppgave.id)
        return nyOppgave
    }

    override fun oppgaveUtfoert(id: String): OppgaveUtfoertResultat? {
        val accessToken = tokenClient.getToken()
        val query = OppgaveUtfoert(
            variables = OppgaveUtfoert.Variables(id)
        )

        logger.info("Forsøker å opprette ny sak mot arbeidsgiver-notifikasjoner")

        val resultat = runBlocking {
            client.execute(query) {
                header(Authorization, "Bearer $accessToken")
            }
        }
        val utfoertOppgave = resultat.data?.oppgaveUtfoert

        if (utfoertOppgave !is OppgaveUtfoertVellykket) {
            when (utfoertOppgave) {
                is OppgaveUtfoertUkjentProdusent -> {
                    logger.error("Feilmelding {}", utfoertOppgave.feilmelding)
                    throw OppgaveUtfoertFeiletException(id, utfoertOppgave.feilmelding)
                }
                is OppgaveUtfoertUgyldigMerkelapp -> {
                    logger.error("Feilmelding {}", utfoertOppgave.feilmelding)
                    throw OppgaveUtfoertFeiletException(id, utfoertOppgave.feilmelding)
                }
                is OppgaveUtfoertNotifikasjonFinnesIkke -> {
                    logger.error("Feilmelding {}", utfoertOppgave.feilmelding)
                    throw OppgaveUtfoertFeiletException(id, utfoertOppgave.feilmelding)
                }
            }
            logger.error("Kunne ikke opprette ny sak", utfoertOppgave)
            throw OpprettNySakFeiletException("ukjent feil")
        }
        logger.info("Oppgave utført {}", utfoertOppgave.id)
        return utfoertOppgave
    }

    override fun opprettNySak(
        grupperingsid: String,
        merkelapp: String,
        virksomhetsnummer: String,
        tittel: String,
        lenke: String,
        tidspunkt: ISO8601DateTime?
    ): NySakResultat? {
        val accessToken = tokenClient.getToken()
        val query = OpprettNySak(
            variables = OpprettNySak.Variables(
                grupperingsid,
                merkelapp,
                virksomhetsnummer,
                tittel,
                lenke,
                tidspunkt
            )
        )

        logger.info("Forsøker å opprette ny sak mot arbeidsgiver-notifikasjoner")

        // TODO: Remove
        logger.info("Token: $accessToken}")

        val resultat = runBlocking {
            client.execute(query) {
                header(Authorization, "Bearer $accessToken")
            }
        }
        val nySak = resultat.data?.nySak

        // TODO: Remove
        logger.info("arbeidsgiver-notifikasjoner respons: $resultat")

        if (nySak !is NySakVellykket) {
            when (nySak) {
                is UgyldigMerkelapp -> {
                    logger.error("Feilmelding {}", nySak.feilmelding)
                    throw OpprettNySakFeiletException(nySak.feilmelding)
                }
                is UgyldigMottaker -> {
                    logger.error("Feilmelding {}", nySak.feilmelding)
                    throw OpprettNySakFeiletException(nySak.feilmelding)
                }
                is UkjentProdusent -> {
                    logger.error("Feilmelding {}", nySak.feilmelding)
                    throw OpprettNySakFeiletException(nySak.feilmelding)
                }
                is UkjentRolle -> {
                    logger.error("Feilmelding {}", nySak.feilmelding)
                    throw OpprettNySakFeiletException(nySak.feilmelding)
                }
                is DuplikatGrupperingsid -> {
                    logger.error("Feilmelding {}", nySak.feilmelding)
                    throw OpprettNySakFeiletException(nySak.feilmelding)
                }
            }
            logger.error("Kunne ikke opprette ny sak", nySak)
            throw OpprettNySakFeiletException("ukjent feil")
        }
        logger.info("Opprettet ny sak {}", nySak.id)
        return nySak
    }

    override fun nySakStatus(id: String, status: SaksStatus): NyStatusSakResultat? {
        val accessToken = tokenClient.getToken()

        logger.info("Forsøker å sette ny status $status for sak $id")

        val query = NyStatusSak(
            variables = NyStatusSak.Variables(
                id,
                status
            )
        )
        val resultat = runBlocking {
            client.execute(query) {
                header(Authorization, "Bearer $accessToken")
            }
        }

        val nyStatusSak = resultat.data?.nyStatusSak

        if (nyStatusSak !is NyStatusSakVellykket) {
            when (nyStatusSak) {
                is SakFinnesIkke -> {
                    logger.error("Feilmelding {}", nyStatusSak.feilmelding)
                    throw NySakStatusFeiletException(id, status, nyStatusSak.feilmelding)
                }
                is Konflikt -> {
                    logger.error("Feilmelding {}", nyStatusSak.feilmelding)
                    throw NySakStatusFeiletException(id, status, nyStatusSak.feilmelding)
                }
            }
            logger.error("Kunne ikke opprette ny sak", nyStatusSak)
            throw NySakStatusFeiletException(id, status, "ukjent feil")
        }
        logger.info("Satt ny status $status for sak {}", nyStatusSak.id)
        return nyStatusSak
    }

    override fun softDeleteSak(id: String): SoftDeleteSakResultat? {
        val accessToken = tokenClient.getToken()

        logger.info("Forsøker å slette sak $id")

        val query = SoftDeleteSak(
            variables = SoftDeleteSak.Variables(
                id
            )
        )
        val resultat = runBlocking {
            client.execute(query) {
                header(Authorization, "Bearer $accessToken")
            }
        }

        val deleteSak = resultat.data?.softDeleteSak
        if (deleteSak !is SoftDeleteSakVellykket) {
            when (deleteSak) {
                is SoftDeleteUgyldigMerkelapp -> {
                    logger.error("Feilmelding {}", deleteSak.feilmelding)
                    throw SoftDeleteSakFeiletException(id, deleteSak.feilmelding)
                }
                is SoftDeleteSakFinnesIkke -> {
                    logger.error("Feilmelding {}", deleteSak.feilmelding)
                    throw SoftDeleteSakFeiletException(id, deleteSak.feilmelding)
                }
                is SoftDeleteUkjentProdusent -> {
                    logger.error("Feilmelding {}", deleteSak.feilmelding)
                    throw SoftDeleteSakFeiletException(id, deleteSak.feilmelding)
                }
            }
        }
        logger.info("Slettet sak {}", deleteSak.id)
        return deleteSak
    }

    class OpprettNyOppgaveFeiletException(feilmelding: String?) :
        Exception("Opprettelse av ny oppgave mot arbeidsgiver-notifikasjon-api feilet: $feilmelding")

    class OppgaveUtfoertFeiletException(id: String, feilmelding: String?) :
        Exception("Utføring av oppgave $id mot arbeidsgiver-notifikasjon-api feilet: $feilmelding")

    class NySakStatusFeiletException(id: String, status: SaksStatus, feilmelding: String?) :
        Exception("Ny status $status for sak $id arbeidsgiver-notifikasjon-api feilet med: $feilmelding")

    class OpprettNySakFeiletException(feilmelding: String?) :
        Exception("Opprettelse av ny sak mot arbeidsgiver-notifikasjon-api feilet: $feilmelding")

    class SoftDeleteSakFeiletException(id: String, feilmelding: String?) :
        Exception("Sletting av sak $id mot arbeidsgiver-notifikasjon-api feilet: $feilmelding")
}
