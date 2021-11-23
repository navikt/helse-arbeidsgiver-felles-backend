package no.nav.helse.arbeidsgiver.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

abstract class RecurringJob(
    private val coroutineScope: CoroutineScope,
    private val waitMillisBetweenRuns: Long
) {

    protected val logger = LoggerFactory.getLogger(this::class.java)

    protected var isRunning = false

    fun startAsync(retryOnFail: Boolean = false) {
        logger.debug("Starter opp")
        isRunning = true
        scheduleAsyncJobRun(retryOnFail)
    }

    private fun scheduleAsyncJobRun(retryOnFail: Boolean) {
        coroutineScope.launch {
            try {
                doJob()
            } catch (t: Throwable) {
                if (retryOnFail)
                    logger.error("Jobben feilet, men forsøker på nytt etter ${waitMillisBetweenRuns / 1000} s ", t)
                else {
                    isRunning = false
                    throw t
                }
            }

            if (isRunning) {
                delay(waitMillisBetweenRuns)
                if (isRunning) {
                    scheduleAsyncJobRun(retryOnFail)
                }
            } else {
                logger.debug("Stoppet.")
            }
        }
    }

    fun stop() {
        logger.debug("Stopper jobben...")
        isRunning = false
    }

    abstract fun doJob()
}
