import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val vertxVersion = "4.5.13"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "com.github.thorlauridsen.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val doOnChange = "${projectDir}/gradlew classes"

application {
    mainClass.set(launcherClassName)
}

dependencies {
    implementation(projects.model)
    implementation(projects.persistence)

    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-web-validation")
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-web-openapi")
    implementation("io.vertx:vertx-pg-client")
    implementation("io.vertx:vertx-jdbc-client")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")
    implementation("io.vertx:vertx-json-schema")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation(local.jackson.databind)

    runtimeOnly(local.h2database)

    // Liquibase for database migrations
    implementation(local.liquibase.core)

    implementation("io.agroal:agroal-pool:2.5")

    implementation(kotlin("stdlib-jdk8"))
    implementation("com.ongres.scram:client:2.1")
    testImplementation("io.vertx:vertx-junit5")
    testImplementation("io.vertx:vertx-web-client")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("fat")
    manifest {
        attributes(mapOf("Main-Verticle" to mainVerticleName))
    }
    mergeServiceFiles()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
    }
}

tasks.withType<JavaExec> {
    args = listOf(
        "run",
        mainVerticleName,
        "--launcher-class=$launcherClassName",
        "--on-redeploy=$doOnChange"
    )
}
