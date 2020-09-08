import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*


val logback_version = "1.2.1"
val logback_contrib_version = "0.1.5"
val jacksonVersion = "2.10.3"
val hikariVersion = "3.3.1"
val vaultJdbcVersion = "1.3.1"
val mainClass = "no.nav.helse.spion.web.AppKt"
val junitJupiterVersion = "5.5.0-RC2"
val assertJVersion = "3.12.2"
val mockKVersion = "1.9.3"
val koinVersion = "2.0.1"
val valiktorVersion = "0.9.0"
val cxfVersion = "3.3.7"
val jaxwsVersion = "2.3.1"
val jaxwsToolsVersion = "2.3.1"


val dateFormat = SimpleDateFormat("yyyy.MM.dd-hh-mm")
dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("Europe/Oslo"))
val gitHash = System.getenv("GITHUB_SHA")?.takeLast(5) ?: "local-build"
val javaTimeAdapterVersion = "1.1.3"
group = "no.nav.sykepenger.kontrakter"
version = "${dateFormat.format(Date())}-$gitHash"
version = "0.1"


plugins {
    kotlin("jvm") version "1.4.0"
    id("org.sonarqube") version "2.8"
    id("com.github.ben-manes.versions") version "0.27.0"
    id("maven-publish")
    jacoco
}

buildscript {
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

dependencies {
    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external-basic:1.2019.09.25-00.21-49b69f0625e0")

    implementation("javax.xml.ws:jaxws-api:$jaxwsVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion") {
        exclude(group = "commons-collections", module = "commons-collections")
    }
    implementation("org.apache.ws.xmlschema:xmlschema-core:2.2.4") // Force newer version of XMLSchema to fix illegal reflective access warning
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }


    implementation("org.koin:koin-core:$koinVersion")
    implementation("org.koin:koin-ktor:$koinVersion")

    implementation(kotlin("stdlib"))

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("ch.qos.logback.contrib:logback-jackson:$logback_contrib_version")
    implementation("ch.qos.logback.contrib:logback-json-classic:$logback_contrib_version")
    implementation("net.logstash.logback:logstash-logback-encoder:4.9")
    implementation("org.codehaus.janino:janino:3.0.6")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("no.nav:vault-jdbc:$vaultJdbcVersion")
    implementation("com.github.tomakehurst:wiremock-standalone:2.25.1")
    implementation("org.postgresql:postgresql:42.2.13")


    testImplementation("org.koin:koin-test:$koinVersion")
    implementation("com.github.javafaker:javafaker:1.0.1") // flytt denne til test når generatorene ikke er nødvendige i prod-koden lenger
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.2")
    testImplementation("io.mockk:mockk:$mockKVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")

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
    gradleVersion = "6.0.1"
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
                name.set("inntektsmelding-kontrakt")
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
