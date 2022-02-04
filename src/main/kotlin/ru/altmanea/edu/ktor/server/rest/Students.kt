package ru.altmanea.edu.ktor.server.rest

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.altmanea.edu.ktor.model.Config
import ru.altmanea.edu.ktor.model.Student
import ru.altmanea.edu.ktor.server.auth.authorizedRoute
import ru.altmanea.edu.ktor.server.auth.roleAdmin
import ru.altmanea.edu.ktor.server.auth.roleUser
import ru.altmanea.edu.ktor.server.repos.RepoItem
import ru.altmanea.edu.ktor.server.repos.studentsRepo

fun Route.student() =
    route(Config.studentsPath) {
        authenticate("auth-jwt") {
            authorizedRoute(setOf(roleAdmin, roleUser)) {
                get {
                    if (!studentsRepo.isEmpty()) {
                        call.respond(studentsRepo.findAll())
                    } else {
                        call.respondText("No students found", status = HttpStatusCode.NotFound)
                    }
                }
                get("{id}") {
                    val id = call.parameters["id"] ?: return@get call.respondText(
                        "Missing or malformed id",
                        status = HttpStatusCode.BadRequest
                    )
                    val studentItem =
                        studentsRepo[id] ?: return@get call.respondText(
                            "No student with id $id",
                            status = HttpStatusCode.NotFound
                        )
                    call.response.etag(studentItem.etag.toString())
                    call.respond(studentItem)
                }
            }
            authorizedRoute(setOf(roleAdmin)) {
                post {
                    val student = call.receive<Student>()
                    studentsRepo.create(student)
                    call.respondText("Student stored correctly", status = HttpStatusCode.Created)
                }
                delete("{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (studentsRepo.delete(id)) {
                        call.respondText("Student removed correctly", status = HttpStatusCode.Accepted)
                    } else {
                        call.respondText("Not Found", status = HttpStatusCode.NotFound)
                    }
                }
                put("{id}") {
                    val id = call.parameters["id"] ?: return@put call.respondText(
                        "Missing or malformed id",
                        status = HttpStatusCode.BadRequest
                    )
                    val oldStudentItem = studentsRepo[id] ?: return@put call.respondText(
                        "No student with id $id",
                        status = HttpStatusCode.NotFound
                    )
                    val newStudent = call.receive<Student>()
                    val clientEtag = call.request.headers["etag"]?.toLong()
                    call.application.log.info("Update ${oldStudentItem.uuid} student. Server etag is ${oldStudentItem.etag}, client etag is $clientEtag")
                    if (oldStudentItem.etag == clientEtag) {
                        studentsRepo.update(id, newStudent)
                        call.respondText("Student updates correctly", status = HttpStatusCode.Created)
                    } else {
                        call.respondText(
                            "Element had updated on server",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }
            }
        }
    }
