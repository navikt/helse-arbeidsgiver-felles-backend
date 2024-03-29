package no.nav.helse.arbeidsgiver.utils

import no.nav.helse.arbeidsgiver.system.TimeProvider
import java.time.Duration
import java.time.LocalDateTime

@Deprecated("Bruk LocalCache fra https://github.com/navikt/helsearbeidsgiver-utils")
class SimpleHashMapCache<T>(
    private val cacheDuration: Duration,
    private val maxCachedItems: Int,
    private val timeProvider: TimeProvider
) {
    constructor(cacheDuration: Duration, maxCachedItems: Int) : this(cacheDuration, maxCachedItems, object : TimeProvider {})

    private val cache = mutableMapOf<String, Entry<T>>()
    val size: Int
        get() { return cache.size }

    fun hasValidCacheEntry(key: String): Boolean {
        return cache[key]?.isValid() ?: false
    }

    fun get(key: String): T {
        return cache[key]!!.value
    }

    fun put(key: String, value: T) {
        if (cache.keys.size >= maxCachedItems) {
            cache.filterValues { !it.isValid() }.keys
                .forEach { cache.remove(it) }
        }

        if (cache.keys.size < maxCachedItems) {
            cache[key] = Entry(timeProvider.now().plus(cacheDuration), value)
        }
    }

    fun clearCache() {
        cache.clear()
    }

    private data class Entry<T>(val expiryTime: LocalDateTime, val value: T)

    private fun Entry<T>.isValid() = this.expiryTime.isAfter(timeProvider.now())
}
