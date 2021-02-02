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


    @BeforeEach
    internal fun setup() {
        service.leggTilBakgrunnsjobbProsesserer("test", testProsesserer())
        repoMock.deleteAll()
        service.startAsync(true)
    }

    @Test
    fun `sett jobb til ok hvis ingen feil `() {
        val testJobb = Bakgrunnsjobb(
                type = "test",
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
    fun `sett jobb til stoppet hvis feiler for mye `() {
        val testJobb = Bakgrunnsjobb(
                type = "test",
                opprettet = now.minusHours(1),
                maksAntallForsoek = 3,
                data = "fail"
        )
        repoMock.save(testJobb)
        testCoroutineScope.advanceTimeBy(1)

        //Den går rett til stoppet i denne testen
        assertThat(repoMock.findByKjoeretidBeforeAndStatusIn(now.plusMinutes(1), setOf(BakgrunnsjobbStatus.STOPPET)))
                .hasSize(1)
    }

    @Test
    fun `jobb som er ok og eldre enn tre månder blir slettet`(){
        val testJobb = Bakgrunnsjobb(
                type = "test",
                behandlet = now.minusMonths(3),
                data = "ok",
                status = BakgrunnsjobbStatus.OK
        )
        repoMock.save(testJobb)
        testCoroutineScope.advanceTimeBy(1000)
        repoMock.deleteOldOkJobs(3)
        assertThat(repoMock.findByKjoeretidBeforeAndStatusIn(now.plusMinutes(1), setOf(BakgrunnsjobbStatus.OK)))
                .hasSize(0)
    }
}


class testProsesserer : BakgrunnsjobbProsesserer {
    override fun prosesser(jobbData: String) {
        if (jobbData == "fail")
            throw RuntimeException()

    }

    override fun nesteForsoek(forsoek: Int, forrigeForsoek: LocalDateTime): LocalDateTime {
        return LocalDateTime.now()
    }
}
