import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    application
    alias(local.plugins.shadow)
}

val mainVerticleName = "com.github.thorlauridsen.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val doOnChange = "${projectDir}/gradlew classes"

application {
    mainClass.set(launcherClassName)
}

dependencies {
    implementation(projects.model)
    implementation(projects.persistence)

    implementation(platform(local.vertx.stack.depchain))
    implementation(local.vertx.web.validation)
    implementation(local.vertx.web)
    implementation(local.vertx.web.openapi)
    implementation(local.vertx.pg.client)
    implementation(local.vertx.jdbc.client)
    implementation(local.vertx.lang.kotlin.coroutines)
    implementation(local.vertx.json.schema)
    implementation(local.vertx.lang.kotlin)
    implementation(local.vertx.config)
    implementation(local.vertx.config.yaml)
    implementation(local.jackson.databind)

    runtimeOnly(local.h2database)
    runtimeOnly(local.postgresql)

    // Liquibase for database migrations
    implementation(local.liquibase.core)

    implementation(local.agroal.pool)

    implementation(kotlin("stdlib-jdk8"))
    implementation(local.ongres.scram.client)
    testImplementation(local.vertx.junit5)
    testImplementation(local.vertx.web.client)
    testImplementation(local.junit.jupiter)
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
