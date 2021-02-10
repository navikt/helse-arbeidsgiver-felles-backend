package no.nav.helse.arbeidsgiver.processing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbProsesserer
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbRepository
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbService
import java.util.*

class AutoCleanJobbProcessor (
        private val bakgrunnsjobbRepository: BakgrunnsjobbRepository,
        private val bakgrunnsjobbService: BakgrunnsjobbService,
        private val om: ObjectMapper,
) : BakgrunnsjobbProsesserer {
    companion object {
        val JOB_TYPE = "bakgrunnsjobb-autoclean"
    }

    override fun prosesser(jobbDataString: String) {
        val autocleanrequest: AutoCleanJobb = om.readValue(jobbDataString, AutoCleanJobb::class.java)
        bakgrunnsjobbRepository.deleteOldOkJobs(autocleanrequest.slettEldre);
        bakgrunnsjobbService.startAutoClean(autocleanrequest.interval,autocleanrequest.slettEldre)
    }

    data class JobbData(val id: UUID)
}