package no.nav.helse.arbeidsgiver.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Deprecated("Bruk samme metode fra https://github.com/navikt/helsearbeidsgiver-utils")
fun <T : Any> T.logger(): Logger =
    LoggerFactory.getLogger(this.javaClass)
