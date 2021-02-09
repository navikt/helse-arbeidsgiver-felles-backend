package no.nav.helse.arbeidsgiver.bakgrunnsjobb

import kotlinx.coroutines.test.TestCoroutineScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class BakgrunnsjobbServiceTest {

    val repoMock = MockBakgrunnsjobbRepository()
    val testCoroutineScope = TestCoroutineScope()
    val service = BakgrunnsjobbService(repoMock, 1, testCoroutineScope)

    val now = LocalDateTime.now()
    private val eksempelProsesserer = EksempelProsesserer()

    @BeforeEach
    internal fun setup() {
        service.registrer(eksempelProsesserer)
        repoMock.deleteAll()
        service.startAsync(true)
    }

    @Test
    fun `sett jobb til ok hvis ingen feil `() {
        val testJobb = Bakgrunnsjobb(
                type = EksempelProsesserer.JOBB_TYPE,
                data = "ok"
        )
        repoMock.save(testJobb)
        testCoroutineScope.advanceTimeBy(1)

        val resultSet = repoMock.findByKjoeretidBeforeAndStatusIn(LocalDateTime.now(), setOf(BakgrunnsjobbStatus.OK))
        assertThat(resultSet)
                .hasSize(1)

        val completeJob = resultSet[0]
        assertThat(completeJob.forsoek).isEqualTo(1)
    }

    @Test
    fun `sett jobb til stoppet og kjør stoppet-funksjonen hvis feiler for mye `() {
        val testJobb = Bakgrunnsjobb(
                type = EksempelProsesserer.JOBB_TYPE,
                opprettet = now.minusHours(1),
                maksAntallForsoek = 3,
                data = "fail"
        )
        repoMock.save(testJobb)
        testCoroutineScope.advanceTimeBy(1)

        //Den går rett til stoppet i denne testen
        assertThat(repoMock.findByKjoeretidBeforeAndStatusIn(now.plusMinutes(1), setOf(BakgrunnsjobbStatus.STOPPET)))
                .hasSize(1)

        assertThat(eksempelProsesserer.bleStoppet).isTrue()
    }
}


class EksempelProsesserer : BakgrunnsjobbProsesserer {
    companion object {
        val JOBB_TYPE: String = "TEST_TYPE"
    }

    var bleStoppet: Boolean = false

    override val type = JOBB_TYPE

    override fun prosesser(jobb: Bakgrunnsjobb) {
        if (jobb.data == "fail")
            throw RuntimeException()
    }

    override fun stoppet(jobb: Bakgrunnsjobb) {
        bleStoppet = true
        throw RuntimeException()
    }

    override fun nesteForsoek(forsoek: Int, forrigeForsoek: LocalDateTime): LocalDateTime {
        return LocalDateTime.now()
    }
}
