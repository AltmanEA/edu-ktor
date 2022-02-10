package ru.altmanea.edu.ktor.server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import ru.altmanea.edu.ktor.model.Config
import ru.altmanea.edu.ktor.model.Student
import ru.altmanea.edu.ktor.server.main
import ru.altmanea.edu.ktor.server.repos.RepoItem
import kotlin.test.assertEquals

internal class StudentsKtTest {
    @Test
    fun testStudentRoute() {
        withTestApplication(Application::main) {
            val token = handleRequest(HttpMethod.Post, "/jwt-login") {
                setBodyAndHeaders("""{ "username": "admin", "password": "admin" }""")
            }.run {
                "Bearer ${decodeBody<Token>().token}"
            }

            val studentItems = handleRequest(HttpMethod.Get, Config.studentsPath) {
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.OK, response.status())
                decodeBody<List<RepoItem<Student>>>()
            }
            assertEquals(4, studentItems.size)
            val sheldon = studentItems.find { it.elem.firstname == "Sheldon" }
            check(sheldon != null)

            handleRequest(HttpMethod.Get, Config.studentsPath + sheldon.uuid) {
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Sheldon", decodeBody<RepoItem<Student>>().elem.firstname)
            }
            handleRequest(HttpMethod.Get, Config.studentsPath + "Jack") {
                addHeader("Authorization", token)
            }.run {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handleRequest(HttpMethod.Post, Config.studentsPath) {
                setBodyAndHeaders(
                    Json.encodeToString(
                        Student("Raj", "Koothrappali")
                    )
                )
                addHeader("Authorization", token)
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
            val studentItemsWithRaj = handleRequest(HttpMethod.Get, Config.studentsPath) {
                addHeader("Authorization", token)
            }.run {
                decodeBody<List<RepoItem<Student>>>()
            }
            assertEquals(5, studentItemsWithRaj.size)
            val raj = studentItemsWithRaj.find { it.elem.firstname == "Raj" }
            check(raj != null)
            assertEquals("Koothrappali", raj.elem.surname)

            handleRequest(HttpMethod.Delete, Config.studentsPath + raj.uuid) {
                addHeader("Authorization", token)
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }
            handleRequest(HttpMethod.Delete, Config.studentsPath + raj.uuid) {
                addHeader("Authorization", token)
            }.apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            val penny = studentItems.find { it.elem.firstname == "Penny" }
            check(penny != null)
            handleRequest(HttpMethod.Put, Config.studentsPath + penny.uuid) {
                setBodyAndHeaders(
                    Json.encodeToString(
                        Student("Penny", "Waitress")
                    )
                )
                addHeader("Authorization", token)
                addHeader("etag", penny.etag.toString())
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
            handleRequest(HttpMethod.Put, Config.studentsPath + penny.uuid) {
                setBodyAndHeaders(
                    Json.encodeToString(
                        Student("Penny", "Unknown")
                    )
                )
                addHeader("Authorization", token)
                addHeader("etag", penny.etag.toString())
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("Element had updated on server", response.content)
            }
        }
    }
}

