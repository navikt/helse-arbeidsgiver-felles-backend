package no.nav.helse.arbeidsgiver.kubernetes

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

internal class KubernetesProbeManagerTest {
    val readynessComponentMock = mockk<ReadynessComponent>(relaxed = true)
    val livenessComponentMock = mockk<LivenessComponent>(relaxed = true)

    val probeManager = KubernetesProbeManager()

    @Test
    internal fun `Kan registrere og kjøre probes på friske livenesskomponentner`() {
        probeManager.registerLivenessComponent(livenessComponentMock)

        val livenessResult = runBlocking { probeManager.runLivenessProbe() }

        assertThat(livenessResult).isNotNull
        assertThat(livenessResult.state).isEqualTo(ProbeState.HEALTHY)
        assertThat(livenessResult.healthyComponents).hasSize(1)
        assertThat(livenessResult.unhealthyComponents).hasSize(0)

        coVerify(exactly = 1) { livenessComponentMock.runLivenessCheck() }
    }
    
    @Test
    internal fun `Kan registrere og kjøre probes på friske readynesskomponenter`() {
        probeManager.registerReadynessComponent(readynessComponentMock)

        val readynessResult = runBlocking { probeManager.runReadynessProbe() }

        assertThat(readynessResult).isNotNull
        assertThat(readynessResult.state).isEqualTo(ProbeState.HEALTHY)
        assertThat(readynessResult.healthyComponents).hasSize(1)
        assertThat(readynessResult.unhealthyComponents).hasSize(0)

        coVerify(exactly = 1) { readynessComponentMock.runReadynessCheck() }
    }

    @Test
    internal fun `Kan registrere og kjøre probes på skadede readynesskomponenter`() {
        coEvery { readynessComponentMock.runReadynessCheck() } throws RuntimeException()

        probeManager.registerReadynessComponent(readynessComponentMock)

        val readynessResult = runBlocking { probeManager.runReadynessProbe() }

        assertThat(readynessResult).isNotNull
        assertThat(readynessResult.state).isEqualTo(ProbeState.UN_HEALTHY)
        assertThat(readynessResult.healthyComponents).hasSize(0)
        assertThat(readynessResult.unhealthyComponents).hasSize(1)
        assertThat(readynessResult.unhealthyComponents[0].error).isNotNull

        coVerify(exactly = 1) { readynessComponentMock.runReadynessCheck() }
    }


    @Test
    internal fun `Kan registrere og kjøre probes på u-friske livenesskomponentner`() {
        coEvery { livenessComponentMock.runLivenessCheck() } throws RuntimeException()

        probeManager.registerLivenessComponent(livenessComponentMock)

        val livenessResult = runBlocking { probeManager.runLivenessProbe() }

        assertThat(livenessResult).isNotNull
        assertThat(livenessResult.state).isEqualTo(ProbeState.UN_HEALTHY)
        assertThat(livenessResult.healthyComponents).hasSize(0)
        assertThat(livenessResult.unhealthyComponents).hasSize(1)
        assertThat(livenessResult.unhealthyComponents[0].error).isNotNull
        assertThat(livenessResult.unhealthyComponents[0].runTime).isNotNull

        coVerify(exactly = 1) { livenessComponentMock.runLivenessCheck() }
    }


}