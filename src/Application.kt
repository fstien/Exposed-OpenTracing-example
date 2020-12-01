package com.github.fstien

import com.fasterxml.jackson.databind.SerializationFeature
import com.zopa.ktor.opentracing.OpenTracingServer
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(OpenTracingServer)

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val userRepository = UserRepository()

    routing {
        post("/user") {
            val user = call.receive<User>()
            userRepository.add(user)
            call.respond(HttpStatusCode.OK)
        }

        get("user/{username}") {
            val username = call.parameters["username"]
            if (username == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val user = userRepository.get(username)

            if (user == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(HttpStatusCode.OK, user)
        }
    }
}

