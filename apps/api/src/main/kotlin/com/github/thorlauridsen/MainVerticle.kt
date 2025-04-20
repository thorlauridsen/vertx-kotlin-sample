package com.github.thorlauridsen

import com.github.thorlauridsen.config.DatabaseInitializer
import com.github.thorlauridsen.persistence.CustomerRepo
import com.github.thorlauridsen.route.CustomerRouter.setupCustomerRouter
import com.github.thorlauridsen.service.CustomerService
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.coroutineRouter

class MainVerticle : CoroutineVerticle() {

    override suspend fun start() {

        val database = DatabaseInitializer(vertx)
        val pool = database.initialize()

        val router = Router.router(vertx)
        val customerRepo = CustomerRepo(pool)
        val customerService = CustomerService(customerRepo)

        coroutineRouter {
            setupCustomerRouter(router, customerService)
        }
        vertx
            .createHttpServer()
            .requestHandler { request -> router.handle(request) }
            .listen(8080)
            .coAwait()
    }
}
