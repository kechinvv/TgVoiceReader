package server

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

class ServerVert : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        vertx
            .createHttpServer()
            .requestHandler { req ->
                req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x!")
            }
            .listen(8080) { http ->
                if (http.succeeded()) {
                    startPromise.complete()
                    println("HTTP server started on port 8080")
                } else {
                    startPromise.fail(http.cause())
                }
            }
    }
}