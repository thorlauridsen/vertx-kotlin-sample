import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode.IGNORE
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    application
    alias(local.plugins.shadow)
}

val mainVerticleName = "com.github.thorlauridsen.MainVerticle"
val launcherClassName = "io.vertx.launcher.application.VertxApplication"

application {
    mainClass.set(launcherClassName)
}

dependencies {
    // The api subproject needs access to both the model and persistence subproject
    implementation(projects.model)
    implementation(projects.persistence)

    // Vert.x dependencies
    implementation(local.vertx.config)
    implementation(local.vertx.config.yaml)
    implementation(local.vertx.jdbc.client)
    implementation(local.vertx.json.schema)
    implementation(local.vertx.lang.kotlin)
    implementation(local.vertx.lang.kotlin.coroutines)
    implementation(local.vertx.launcher)
    implementation(local.vertx.pg.client)
    implementation(local.vertx.web)
    implementation(local.vertx.web.openapi)
    implementation(local.vertx.web.validation)

    // FasterXML Jackson databind for JSON serialization/deserialization
    implementation(local.jackson.databind)
    implementation(local.jackson.datatype.jsr310)
    implementation(local.jackson.module.kotlin)

    // H2 in-memory database
    runtimeOnly(local.h2database)

    // PostgreSQL database driver
    runtimeOnly(local.postgresql)

    // Liquibase for database migrations
    implementation(local.liquibase.core)

    // Test dependencies
    testImplementation(local.vertx.junit5)
    testImplementation(local.vertx.web.client)
    testImplementation(local.junit.jupiter)
    testImplementation(local.kotlin.coroutines.test)
}

tasks.withType<KotlinJvmCompile>().configureEach {
    jvmTargetValidationMode.set(IGNORE)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("fat")
    manifest {
        attributes(mapOf("Main-Verticle" to mainVerticleName))
    }
    mergeServiceFiles()
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
    }
}

tasks.withType<JavaExec> {
    args = listOf(mainVerticleName)
}
