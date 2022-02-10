package ru.altmanea.edu.ktor.server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import ru.altmanea.edu.ktor.model.Config
import ru.altmanea.edu.ktor.model.Lesson
import ru.altmanea.edu.ktor.model.Student
import ru.altmanea.edu.ktor.server.main
import ru.altmanea.edu.ktor.server.repos.RepoItem
import kotlin.test.assertEquals


internal class LessonsKtTest {
    @Test
    fun testLessonRoute() {
        withTestApplication(Application::main) {
            val token = handleRequest(HttpMethod.Post, "/jwt-login") {
                setBodyAndHeaders("""{ "username": "admin", "password": "admin" }""")
            }.run {
                "Bearer ${decodeBody<Token>().token}"
            }

            val studentItems = handleRequest(HttpMethod.Get, Config.studentsPath) {
                addHeader("Authorization", token)
            }.run {
                decodeBody<List<RepoItem<Student>>>()
            }
            val sheldon = studentItems.find { it.elem.firstname == "Sheldon" }
            check(sheldon != null)

            val lessonItems = handleRequest(HttpMethod.Get, Config.lessonsPath) {
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.OK, response.status())
                decodeBody<List<RepoItem<Lesson>>>()
            }
            assertEquals(3, lessonItems.size)
            val math = lessonItems.find { it.elem.name == "Math" }
            check(math != null)

            handleRequest(HttpMethod.Get, Config.lessonsPath + math.uuid) {
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Math", decodeBody<RepoItem<Lesson>>().elem.name)
            }
            handleRequest(HttpMethod.Get, Config.lessonsPath + sheldon.uuid) {
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handleRequest(HttpMethod.Post, Config.lessonsPath) {
                setBodyAndHeaders(
                    Json.encodeToString(
                        Lesson("Theology")
                    )
                )
                addHeader("Authorization", token)
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
            val lessonItemsWithTheo = handleRequest(HttpMethod.Get, Config.lessonsPath) {
                addHeader("Authorization", token)
            }.run {
                decodeBody<List<RepoItem<Lesson>>>()
            }
            assertEquals(4, lessonItemsWithTheo.size)
            val theology = lessonItemsWithTheo.find { it.elem.name == "Theology" }
            check(theology != null)

            handleRequest(HttpMethod.Delete, Config.lessonsPath + theology.uuid) {
                addHeader("Authorization", token)
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }

            val lsPath = Config.lessonsPath + math.uuid + "/students/" + sheldon.uuid

            val mathWithSheldon = handleRequest(HttpMethod.Post, lsPath) {
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.OK, response.status())
                decodeBody<RepoItem<Lesson>>()
            }
            assertEquals(1, mathWithSheldon.elem.students.size)
            assertEquals(0, mathWithSheldon.elem.marks.size)

            val mathWithSheldonMark = handleRequest(HttpMethod.Post, "$lsPath/marks") {
                setBodyAndHeaders(
                    Json.encodeToString(5)
                )
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.OK, response.status())
                decodeBody<RepoItem<Lesson>>()
            }
            assertEquals(1, mathWithSheldonMark.elem.students.size)
            assertEquals(1, mathWithSheldonMark.elem.marks.size)

            val mathWithoutSheldon = handleRequest(HttpMethod.Delete, lsPath) {
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.OK, response.status())
                decodeBody<RepoItem<Lesson>>()
            }
            assertEquals(0, mathWithoutSheldon.elem.students.size)
            assertEquals(0, mathWithoutSheldon.elem.marks.size)
        }
    }
}