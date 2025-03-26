val vertxVersion = "4.5.13"

dependencies {
    // Persistence subproject needs to know about the model subproject
    implementation(projects.model)

    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))

    implementation("io.vertx:vertx-pg-client")
    implementation("io.vertx:vertx-jdbc-client")

    implementation("io.vertx:vertx-lang-kotlin-coroutines")
    implementation("io.vertx:vertx-lang-kotlin")
}
