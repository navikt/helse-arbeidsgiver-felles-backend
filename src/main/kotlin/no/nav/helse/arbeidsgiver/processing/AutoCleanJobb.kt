package no.nav.helse.arbeidsgiver.processing

import java.time.LocalDateTime
import java.util.*

data class AutoCleanJobb(
    val id: UUID = UUID.randomUUID(),
    val opprettet: LocalDateTime = LocalDateTime.now(),
    val slettEldre: Long,
    var interval: Int,
)
