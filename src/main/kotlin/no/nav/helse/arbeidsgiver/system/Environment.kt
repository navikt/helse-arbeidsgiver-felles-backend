package no.nav.helse.arbeidsgiver.system

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

enum class AppEnv {
    TEST,
    LOCAL,
    PREPROD,
    PROD
}

@KtorExperimentalAPI
fun ApplicationConfig.getEnvironment(): AppEnv {
    return AppEnv.valueOf(getString("koin.profile"))
}

@KtorExperimentalAPI
fun ApplicationConfig.getString(path: String): String {
    return property(path).getString()
}
