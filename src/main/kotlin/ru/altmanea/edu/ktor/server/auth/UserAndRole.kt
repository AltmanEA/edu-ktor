package ru.altmanea.edu.ktor.server.auth

import io.ktor.auth.*
import ru.altmanea.edu.ktor.model.User

class Role(val name: String)

data class UserSession(val name: String, val count: Int) : Principal

val userAdmin = User("admin","admin")
val userTutor = User("tutor", "tutor")
val userList = listOf(userAdmin, userTutor)

val roleAdmin = Role("admin")
val roleUser = Role("user")

val userRoles = mapOf(
    userAdmin to setOf(roleAdmin, roleUser),
    userTutor to setOf(roleUser)
)