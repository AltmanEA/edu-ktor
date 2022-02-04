//package ru.altmanea.edu.ktor.server.rest
//
//import io.ktor.application.*
//import io.ktor.http.*
//import io.ktor.server.testing.*
//import kotlinx.serialization.decodeFromString
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import org.junit.Test
//import ru.altmanea.edu.ktor.model.Config
//import ru.altmanea.edu.ktor.model.Student
//import ru.altmanea.edu.ktor.server.main
//import kotlin.test.assertEquals
//
//internal class StudentsKtTest {
//    @Test
//    fun testStudentRoute() {
//        withTestApplication(Application::main) {
//            var students = handleRequest(HttpMethod.Get, Config.studentsURL).run {
//                assertEquals(HttpStatusCode.OK, response.status())
//                decodeBody<List<Student>>()
//            }
//            assertEquals(4, students.size)
//            val sheldon = students.find { it.firstname == "Sheldon" }
//            check(sheldon != null)
//
//            handleRequest(HttpMethod.Get, Config.studentsURL + sheldon.idName).run {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("Sheldon", decodeBody<Student>().firstname)
//            }
//            handleRequest(HttpMethod.Get, Config.studentsURL + "Jack").run {
//                assertEquals(HttpStatusCode.NotFound, response.status())
//            }
//
//            handleRequest(HttpMethod.Post, Config.studentsURL) {
//                setBodyAndHeaders(
//                    Json.encodeToString(
//                        Student("Raj", "Koothrappali")
//                    )
//                )
//            }.apply {
//                assertEquals(HttpStatusCode.Created, response.status())
//            }
//            students = handleRequest(HttpMethod.Get, Config.studentsURL).decodeBody()
//            assertEquals(5, students.size)
//            val raj = students.find { it.firstname == "Raj" }
//            check(raj != null)
//            assertEquals("Koothrappali",raj.surname)
//
//            handleRequest(HttpMethod.Delete, Config.studentsURL + raj.idName).apply{
//                assertEquals(HttpStatusCode.Accepted, response.status())
//            }
//            handleRequest(HttpMethod.Delete, Config.studentsURL + raj.idName).apply{
//                assertEquals(HttpStatusCode.NotFound, response.status())
//            }
//
//        }
//    }
//}
//
//private inline fun <reified T> TestApplicationCall.decodeBody() =
//    Json.decodeFromString<T>(response.content ?: "")
//
//fun TestApplicationRequest.setBodyAndHeaders(body: String) {
//    setBody(body)
//    addHeader("Content-Type", "application/json")
//    addHeader("Accept", "application/json")
//}