dependencies {
    // Persistence subproject needs to know about the model subproject
    implementation(projects.model)

    implementation(platform(local.vertx.stack.depchain))

    implementation(local.vertx.pg.client)
    implementation(local.vertx.jdbc.client)

    implementation(local.vertx.lang.kotlin.coroutines)
    implementation(local.vertx.lang.kotlin)
}
