package no.nav.helse.arbeidsgiver.processing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.test.TestCoroutineScope
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.time.LocalDateTime
import java.util.*

internal class AutoCleanJobbProcessorTest {

    val now = LocalDateTime.now()
    val uuid = UUID.randomUUID()
    lateinit var autoCleanJobbProcessor : AutoCleanJobbProcessor
    lateinit var bakgrunnsjobbRepository :  BakgrunnsjobbRepository
    val bakgrunnsjobbSlettEldreEnn10 = Bakgrunnsjobb(
            uuid,
            AutoCleanJobbProcessor.JOB_TYPE,
            now,
            now,
            BakgrunnsjobbStatus.OPPRETTET,
            now,
            0,
            3,
            "{\"slettEldre\": \"10\",\"interval\": \"3\"}"

    )
    val bakgrunnsjobbSlettEldreEnn2 = Bakgrunnsjobb(
            uuid,
            AutoCleanJobbProcessor.JOB_TYPE,
            now,
            now,
            BakgrunnsjobbStatus.OPPRETTET,
            now,
            0,
            3,
            "{\"slettEldre\": \"2\",\"interval\": \"3\"}"
    )
    val bakgrunnsjobb3mndGammel = Bakgrunnsjobb(
            uuid,
            "test",
            now.minusMonths(3),
            now.minusMonths(3),
            BakgrunnsjobbStatus.OK,
            now.minusMonths(3),
            0,
            3,
            "{}"
    )

    @BeforeEach
    fun setUp(){
        bakgrunnsjobbRepository = MockBakgrunnsjobbRepository()
        val testCoroutineScope = TestCoroutineScope()
        val bakgrunnsjobbService = BakgrunnsjobbService(bakgrunnsjobbRepository, 1,testCoroutineScope)
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        autoCleanJobbProcessor = AutoCleanJobbProcessor(bakgrunnsjobbRepository,bakgrunnsjobbService,objectMapper)
    }

    @Test
    fun getType() {
        assertThat(AutoCleanJobbProcessor.JOB_TYPE == autoCleanJobbProcessor.type).isTrue()
    }

    @Test
    fun jobbSomErNyereEnnSlettEldreBlirIkkeSlettet() {
        bakgrunnsjobbRepository.save(bakgrunnsjobb3mndGammel)
        assertThat(bakgrunnsjobb3mndGammel.uuid == (bakgrunnsjobbRepository.getById(bakgrunnsjobb3mndGammel.uuid))?.uuid).isTrue()
        autoCleanJobbProcessor.prosesser(bakgrunnsjobbSlettEldreEnn10)
        assertThat(bakgrunnsjobb3mndGammel.uuid == (bakgrunnsjobbRepository.getById(bakgrunnsjobb3mndGammel.uuid))?.uuid).isTrue()
    }

    @Test
    fun jobbSomErEldreEnnSlettEldreBlirIkkeSlettet() {
        bakgrunnsjobbRepository.save(bakgrunnsjobb3mndGammel)
        assertThat(bakgrunnsjobb3mndGammel.uuid == (bakgrunnsjobbRepository.getById(bakgrunnsjobb3mndGammel.uuid))?.uuid).isTrue()
        autoCleanJobbProcessor.prosesser(bakgrunnsjobbSlettEldreEnn2)
        assertThat(bakgrunnsjobb3mndGammel.uuid == (bakgrunnsjobbRepository.getById(bakgrunnsjobb3mndGammel.uuid))?.uuid).isFalse()
    }
}