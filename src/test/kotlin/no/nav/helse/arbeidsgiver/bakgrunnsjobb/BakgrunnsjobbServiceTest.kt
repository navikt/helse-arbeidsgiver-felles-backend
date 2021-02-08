package no.nav.helse.arbeidsgiver.bakgrunnsjobb

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.test.TestCoroutineScope
import no.nav.helse.arbeidsgiver.processing.AutoCleanJobb
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
    fun `autoClean opprettes med riktig kjøretid`() {
        service.startAutoClean(2, 3)
        assertThat(repoMock.findAutoCleanJobs()).hasSize(1)
        assert(repoMock.findAutoCleanJobs().get(0).kjoeretid > now.plusHours(1) &&
                repoMock.findAutoCleanJobs().get(0).kjoeretid < now.plusHours(3)
        )
    }
    @Test
    fun `autoClean får parametre med til data`() {
        service.startAutoClean(2, 3)
        val objectMapper = ObjectMapper()
        val autocleanrequest: AutoCleanJobb = objectMapper.readValue(repoMock.findAutoCleanJobs().get(0).data, AutoCleanJobb::class.java)
        assert(autocleanrequest.slettEldre.equals(3))
        assert(autocleanrequest.interval.equals(2))
    }
    @Test
    fun `autoClean opprettes med interval under 1 blir ikke lagret`() {
        service.startAutoClean(0, 3)
        assertThat(repoMock.findAutoCleanJobs()).hasSize(0)
    }

    @Test
    fun `autoClean oppretter jobb med riktig antall måneeder`(){
        service.startAutoClean(2,3)
        assertThat(repoMock.findAutoCleanJobs()).hasSize(1)
    }

    @Test
    fun `hvis vi allerede har en autoclean jobb så blir den oppdatert med nye parametre`(){
        service.startAutoClean(2, 3)
        assertThat(repoMock.findAutoCleanJobs()).hasSize(1)
        service.startAutoClean(5, 6)
        assertThat(repoMock.findAutoCleanJobs()).hasSize(1)
        val objectMapper = ObjectMapper()
        val autocleanrequest: AutoCleanJobb = objectMapper.readValue(repoMock.findAutoCleanJobs().get(0).data, AutoCleanJobb::class.java)
        assert(autocleanrequest.slettEldre.equals(6))
        assert(autocleanrequest.interval.equals(5))
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
