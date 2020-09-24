import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

val kotlinVersion = "1.4.0"

val logbackVersion = "1.2.1"
val logbackContribVersion = "0.1.5"
val coroutinesVersion = "1.3.9"
val jacksonVersion = "2.11.2"
val hikariVersion = "3.3.1"
val vaultJdbcVersion = "1.3.1"
val junitJupiterVersion = "5.5.0-RC2"
val assertJVersion = "3.12.2"
val mockKVersion = "1.9.3"
val ktorVersion = "1.4.0"


// Versjonering av artifakten
val dateFormat = SimpleDateFormat("yyyy.MM.dd-HH-mm")
dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("Europe/Oslo"))
val gitHash = System.getenv("GITHUB_SHA")?.takeLast(5) ?: "local-build"
group = "no.nav.helsearbeidsgiver"
version = "${dateFormat.format(Date())}-$gitHash"
// -- Versjonering


plugins {
    kotlin("jvm") version "1.4.0"
    id("org.sonarqube") version "2.8"
    id("com.github.ben-manes.versions") version "0.27.0"
    id("maven-publish")
    jacoco
}

sonarqube {
    properties {
        property("sonar.projectKey", "navikt_helse-arbeidsgiver-felles-backend")
        property("sonar.organization", "navit")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", System.getenv("SONAR_TOKEN"))
        property("sonar.exclusions", "**Mock**,**/App**")
    }
}

tasks.jacocoTestReport {
    executionData("build/jacoco/test.exec", "build/jacoco/slowTests.exec")
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}


buildscript {
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation(kotlin("stdlib-jdk8", "$kotlinVersion"))
    implementation(kotlin("reflect", "$kotlinVersion"))

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback.contrib:logback-jackson:$logbackContribVersion")
    implementation("ch.qos.logback.contrib:logback-json-classic:$logbackContribVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:4.9")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("no.nav:vault-jdbc:$vaultJdbcVersion")
    implementation("org.postgresql:postgresql:42.2.13")
    implementation("com.nimbusds:nimbus-jose-jwt:8.15")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("io.mockk:mockk:$mockKVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks.named<KotlinCompile>("compileKotlin")

tasks.named<KotlinCompile>("compileKotlin") {
    kotlinOptions.jvmTarget = "11"
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "11"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://kotlin.bintray.com/ktor")
}

tasks.named<Jar>("jar") {
    baseName = ("app")

    manifest {
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
            it.name
        }
    }

    doLast {
        configurations.runtimeClasspath.get().forEach {
            val file = File("$buildDir/libs/${it.name}")
            if (!file.exists())
                it.copyTo(file)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.named<Test>("test") {
    include("no/nav/helse/**")
    exclude("no/nav/helse/slowtests/**")
}

task<Test>("slowTests") {
    include("no/nav/helse/slowtests/**")
    outputs.upToDateWhen { false }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.5.1"
}

configure<PublishingExtension> {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/helse-arbeidsgiver-felles-backend")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {

            pom {
                name.set("helse-arbeidsgiver-felles-backend")
                description.set("Felleskomponenter for backend for team arbeidsgiver i PO Helse")
                url.set("https://github.com/navikt/helse-arbeidsgiver-felles-backend")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/navikt/helse-arbeidsgiver-felles-backend")
                    developerConnection.set("scm:git:https://github.com/navikt/helse-arbeidsgiver-felles-backend")
                    url.set("https://github.com/navikt/helse-arbeidsgiver-felles-backend")
                }
            }
            from(components["java"])
        }
    }
}
