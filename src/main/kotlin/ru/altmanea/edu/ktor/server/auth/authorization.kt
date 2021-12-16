package ru.altmanea.edu.ktor.server.auth

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.authorization() {
    install(RoleBasedAuthorization) {
        getRoles {principal ->
            val username = (principal as UserSession).name
            val user = userList.find { it.username== username }
            userRoles[user]?: emptySet()
        }
    }
    routing {
        authenticate ("auth-session") {
            authorizedRoute(setOf(roleAdmin)) {
                get("/admin"){
                    call.respondText ("Hello admin")
                }
            }
            authorizedRoute(setOf(roleAdmin, roleUser)) {
                get("/user"){
                    call.respondText("Hello, ${call.principal<UserSession>()?.name}!")
                }
            }
        }
    }
}
