dependencies {
    // Persistence subproject needs to know about the model subproject
    implementation(projects.model)

    // Vert.x dependencies
    implementation(local.vertx.lang.kotlin)
    implementation(local.vertx.lang.kotlin.coroutines)
    implementation(local.vertx.pg.client)
}
