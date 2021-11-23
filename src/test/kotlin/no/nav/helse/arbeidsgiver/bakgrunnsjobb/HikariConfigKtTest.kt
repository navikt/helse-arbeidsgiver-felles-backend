package no.nav.helse.arbeidsgiver.bakgrunnsjobb

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class HikariConfigKtTest {

    @Test
    fun createHikariConfig() {
        val hikariConfig = createHikariConfig("jdbc:postgresql://localhost:5432/harbeidsgiverbackend", "harbeidsgiverbackend", "harbeidsgiverbacken")
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/harbeidsgiverbackend", hikariConfig.jdbcUrl)
    }

    @Test
    fun createLocalHikariConfigWithCorrectParametersOKTest() {
        val localHiariConfig = createLocalHikariConfig()
        Assertions.assertEquals("org.postgresql.Driver", localHiariConfig.driverClassName)
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/harbeidsgiverbackend", localHiariConfig.jdbcUrl)
        Assertions.assertEquals("harbeidsgiverbackend", localHiariConfig.username)
        Assertions.assertEquals("harbeidsgiverbackend", localHiariConfig.password)
    }
    @Test
    fun createLocalHikariConfigWithIncorrectUserKOTest() {
        val localHiariConfig = createLocalHikariConfig()
        Assertions.assertEquals("org.postgresql.Driver", localHiariConfig.driverClassName)
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/harbeidsgiverbackend", localHiariConfig.jdbcUrl)
        Assertions.assertNotEquals("feilbruker", localHiariConfig.username)
        Assertions.assertEquals("harbeidsgiverbackend", localHiariConfig.password)
    }
}
