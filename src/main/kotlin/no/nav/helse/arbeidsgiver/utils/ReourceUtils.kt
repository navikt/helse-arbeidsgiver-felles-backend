package no.nav.helse.arbeidsgiver.utils

fun String.loadFromResources() : String {
    return ClassLoader.getSystemResource(this).readText()
}