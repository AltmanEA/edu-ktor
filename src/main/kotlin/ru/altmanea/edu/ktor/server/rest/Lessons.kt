package ru.altmanea.edu.ktor.server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.altmanea.edu.ktor.model.Config.Companion.lessonsURL
import ru.altmanea.edu.ktor.model.Lesson
import ru.altmanea.edu.ktor.server.repos.lessonsRepo
import ru.altmanea.edu.ktor.server.repos.studentsRepo

fun Route.lesson() =
    route(lessonsURL) {
        get {
            if (lessonsRepo.isNotEmpty()) {
                call.respond(lessonsRepo)
            } else {
                call.respondText("No lessons found", status = HttpStatusCode.NotFound)
            }
        }
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val lesson =
                lessonsRepo.find { it.name == id } ?: return@get call.respondText(
                    "No lesson with name $id",
                    status = HttpStatusCode.NotFound
                )
            call.respond(lesson)
        }
        post {
            val lesson = call.receive<Lesson>()
            lessonsRepo.add(lesson)
            call.respondText("Lesson stored correctly", status = HttpStatusCode.Created)
        }
        delete("{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (lessonsRepo.removeIf { it.name == id }) {
                call.respondText("Lesson removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }

        post("{lessonId}/students/{studentId}") {
            val lessonId = call.parameters["lessonId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val studentId = call.parameters["studentId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val lesson = lessonsRepo.find { it.name == lessonId } ?: return@post call.respondText(
                "No lesson with name $lessonId",
                status = HttpStatusCode.NotFound
            )
            val student = studentsRepo.find { it.idName == studentId } ?: return@post call.respondText(
                "No student with full name $studentId",
                status = HttpStatusCode.NotFound
            )
            lesson.students += student
            call.respond(lesson)
        }
        delete("{lessonId}/students/{studentId}") {
            val lessonId = call.parameters["lessonId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val studentId = call.parameters["studentId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val lesson = lessonsRepo.find { it.name == lessonId } ?: return@delete call.respondText(
                "No lesson with name $lessonId",
                status = HttpStatusCode.NotFound
            )
            val student = studentsRepo.find { it.idName == studentId } ?: return@delete call.respondText(
                "No student with full name $studentId",
                status = HttpStatusCode.NotFound
            )
            lesson.students -= student
            call.respond(lesson)
        }

        post("{lessonId}/students/{studentId}/marks") {
            val lessonId = call.parameters["lessonId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val studentId = call.parameters["studentId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val mark = call.receive<String>()
            val lesson = lessonsRepo.find { it.name == lessonId } ?: return@post call.respondText(
                "No lesson with name $lessonId",
                status = HttpStatusCode.NotFound
            )
            val student = studentsRepo.find { it.idName == studentId } ?: return@post call.respondText(
                "No student with full name $studentId",
                status = HttpStatusCode.NotFound
            )
            if(student !in lesson.students)
                call.respondText(
                    "No student $studentId in lesson $lessonId",
                    status = HttpStatusCode.NotFound
                )
            lesson.marks += studentId to mark
            call.respond(lesson)
        }
        delete("{lessonId}/students/{studentId}/marks") {
            val lessonId = call.parameters["lessonId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val studentId = call.parameters["studentId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val lesson = lessonsRepo.find { it.name == lessonId } ?: return@delete call.respondText(
                "No lesson with name $lessonId",
                status = HttpStatusCode.NotFound
            )
            val student = studentsRepo.find { it.idName == studentId } ?: return@delete call.respondText(
                "No student with full name $studentId",
                status = HttpStatusCode.NotFound
            )
            if(student !in lesson.students)
                call.respondText(
                    "No student $studentId in lesson $lessonId",
                    status = HttpStatusCode.NotFound
                )
            val mark = lesson.marks.find { it.first == studentId }
            if(mark==null){
                call.respondText(
                    "Student $studentId has not mark in lesson $lessonId",
                    status = HttpStatusCode.NotFound
                )
            }
            lesson.marks.remove(mark)
            call.respond(lesson)
        }
    }
