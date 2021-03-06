package ru.altmanea.edu.ktor.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.altmanea.edu.ktor.model.Config.Companion.serverDomain
import ru.altmanea.edu.ktor.model.Config.Companion.serverPort
import ru.altmanea.edu.ktor.server.auth.authentication
import ru.altmanea.edu.ktor.server.auth.authorization
import ru.altmanea.edu.ktor.server.face.index
import ru.altmanea.edu.ktor.server.repos.lessonsRepo
import ru.altmanea.edu.ktor.server.repos.lessonsRepoTestData
import ru.altmanea.edu.ktor.server.repos.studentsRepo
import ru.altmanea.edu.ktor.server.repos.studentsRepoTestData
import ru.altmanea.edu.ktor.server.rest.lesson
import ru.altmanea.edu.ktor.server.rest.student

fun main() {
    embeddedServer(
        Netty,
        port = serverPort,
        host = serverDomain,
        watchPaths = listOf("classes", "resources")
    ) {
        main()
    }.start(wait = true)
}

fun Application.main(test: Boolean = true) {
    if(test) {
        studentsRepoTestData.forEach{ studentsRepo.create(it)}
        lessonsRepoTestData.forEach { lessonsRepo.create(it) }
//        install(CORS) {
//            host("localhost:8080")
//            method(HttpMethod.Options)
//            method(HttpMethod.Put)
//            method(HttpMethod.Delete)
//            method(HttpMethod.Patch)
//            header(HttpHeaders.Authorization)
//            header(HttpHeaders.AccessControlAllowOrigin)
//            allowNonSimpleContentTypes = true
//            allowCredentials = true
//            allowSameOrigin = true
//        }
    }
    install(ContentNegotiation) {
        json()
    }
    authentication()
    authorization()
    routing {
        index()
        student()
        lesson()
    }
}

