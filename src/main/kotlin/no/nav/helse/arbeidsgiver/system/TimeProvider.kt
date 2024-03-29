package no.nav.helse.arbeidsgiver.system

import java.time.LocalDateTime

interface TimeProvider {
    fun now(): LocalDateTime = LocalDateTime.now()
}
