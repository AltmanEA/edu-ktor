package ru.altmanea.edu.ktor.server.face

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import ru.altmanea.edu.ktor.server.main
import kotlin.test.assertContains
import kotlin.test.assertEquals


internal class IndexKtTest {
    @Test
    fun testRoot() {
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assert(response.content?.contains( "Ktor App Example")?:false)
            }
        }
    }
}