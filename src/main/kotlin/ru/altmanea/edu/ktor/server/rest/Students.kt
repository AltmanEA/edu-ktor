package ru.altmanea.edu.ktor.server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.altmanea.edu.ktor.model.Config
import ru.altmanea.edu.ktor.model.Student
import ru.altmanea.edu.ktor.server.repos.studentsRepo

fun Route.student() =
    route(Config.studentsPath) {
        get {
            if (studentsRepo.isNotEmpty()) {
                call.respond(studentsRepo)
            } else {
                call.respondText("No students found", status = HttpStatusCode.NotFound)
            }
        }
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val student =
                studentsRepo.find { it.idName == id } ?: return@get call.respondText(
                    "No student with full name $id",
                    status = HttpStatusCode.NotFound
                )
            call.respond(student)
        }
        post {
            val student = call.receive<Student>()
            studentsRepo.add(student)
            call.respondText("Student stored correctly", status = HttpStatusCode.Created)
        }
        delete("{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (studentsRepo.removeIf { it.idName == id }) {
                call.respondText("Lesson removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
