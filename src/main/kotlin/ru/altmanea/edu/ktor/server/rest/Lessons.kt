package ru.altmanea.edu.ktor.server.rest

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import ru.altmanea.edu.ktor.model.Config.Companion.lessonsPath
import ru.altmanea.edu.ktor.model.Lesson
import ru.altmanea.edu.ktor.server.auth.authorizedRoute
import ru.altmanea.edu.ktor.server.auth.roleAdmin
import ru.altmanea.edu.ktor.server.auth.roleUser
import ru.altmanea.edu.ktor.server.repos.RepoItem
import ru.altmanea.edu.ktor.server.repos.lessonsRepo
import ru.altmanea.edu.ktor.server.repos.studentsRepo
import ru.altmanea.edu.ktor.server.repos.urlByUUID

fun Route.lesson() =
    route(lessonsPath) {
        authenticate("auth-jwt") {
            authorizedRoute(setOf(roleAdmin, roleUser)) {
                get {
                    if (!lessonsRepo.isEmpty()) {
                        call.respond(lessonsRepo.findAll())
                    } else {
                        call.respondText("No lessons found", status = HttpStatusCode.NotFound)
                    }
                }
                get("{id}") {
                    val id = call.parameters["id"] ?: return@get call.respondText(
                        "Missing or malformed id",
                        status = HttpStatusCode.BadRequest
                    )
                    val lessonItem =
                        lessonsRepo[id] ?: return@get call.respondText(
                            "No lesson with name $id",
                            status = HttpStatusCode.NotFound
                        )
                    call.response.etag(lessonItem.etag.toString())
                    call.respond(lessonItem)
                }
            }
            authorizedRoute(setOf(roleAdmin)) {
                post {
                    val lesson = call.receive<Lesson>()
                    lessonsRepo.create(lesson)
                    call.respondText("Lesson stored correctly", status = HttpStatusCode.Created)
                }
                delete("{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (lessonsRepo.delete(id)) {
                        call.respondText("Lesson removed correctly", status = HttpStatusCode.Accepted)
                    } else {
                        call.respondText("Not Found", status = HttpStatusCode.NotFound)
                    }
                }

                val lsPath = "{lessonId}/students/{studentId}"
                post(lsPath) {
                    when (val lsResult = lsParameters()) {
                        is LSOk -> {
                            lsResult.lessonItem.elem.students += lsResult.studentLink
                            call.respond(lessonsRepo[lsResult.lessonItem.uuid]!!)
                        }
                        is LSFail -> call.respondText(lsResult.text, status = lsResult.code)
                    }
                }
                delete(lsPath) {
                    when (val lsResult = lsParameters()) {
                        is LSOk -> {
                            lsResult.lessonItem.elem.students -= lsResult.studentLink
                            lsResult.lessonItem.elem.marks -= lsResult.studentLink
                            call.respond(lessonsRepo[lsResult.lessonItem.uuid]!!)
                        }
                        is LSFail -> call.respondText(lsResult.text, status = lsResult.code)
                    }
                }

                post("$lsPath/marks") {
                    when (val lsResult = lsParameters()) {
                        is LSOk -> {
                            if (lsResult.studentLink !in lsResult.lessonItem.elem.students)
                                return@post call.respondText(
                                    "No student ${lsResult.studentLink} in lesson ${lsResult.lessonItem.elem.name}",
                                    status = HttpStatusCode.NotFound
                                )
                            val mark = call.receive<String>().toIntOrNull()
                                ?: return@post call.respondText(
                                    "Mark is wrong",
                                    status = HttpStatusCode.BadRequest
                                )
                            lsResult.lessonItem.elem.marks += lsResult.studentLink to mark
                            call.respond(lessonsRepo[lsResult.lessonItem.uuid]!!)
                        }
                        is LSFail -> call.respondText(lsResult.text, status = lsResult.code)
                    }
                }
                delete("$lsPath/marks") {
                    when (val lsResult = lsParameters()) {
                        is LSOk -> {
                            if (lsResult.studentLink !in lsResult.lessonItem.elem.students)
                                return@delete call.respondText(
                                    "No student ${lsResult.studentLink} in lesson ${lsResult.lessonItem.elem.name}",
                                    status = HttpStatusCode.NotFound
                                )
                            lsResult.lessonItem.elem.marks.remove(lsResult.studentLink)
                                ?: return@delete call.respondText(
                                    "No mark of student ${lsResult.studentLink} in lesson ${lsResult.lessonItem.elem.name}",
                                    status = HttpStatusCode.NotFound
                                )
                            call.respond(lessonsRepo[lsResult.lessonItem.uuid]!!)
                        }
                        is LSFail -> call.respondText(lsResult.text, status = lsResult.code)
                    }
                }
            }
        }
    }

private sealed interface LSResult
private class LSOk(
    val lessonItem: RepoItem<Lesson>,
    val studentLink: String
) : LSResult

private class LSFail(
    val text: String,
    val code: HttpStatusCode
) : LSResult

private fun PipelineContext<Unit, ApplicationCall>.lsParameters(): LSResult {
    val lessonId = call.parameters["lessonId"]
        ?: return LSFail("LessonId wrong", HttpStatusCode.BadRequest)
    val studentId = call.parameters["studentId"]
        ?: return LSFail("StudentId wrong", HttpStatusCode.BadRequest)
    val lessonItem = lessonsRepo[lessonId]
        ?: return LSFail("No lesson with id $lessonId", HttpStatusCode.NotFound)
    val studentLink = studentsRepo.urlByUUID(studentId)
        ?: return LSFail("No student with id $studentId", HttpStatusCode.NotFound)
    return LSOk(lessonItem, studentLink)
}