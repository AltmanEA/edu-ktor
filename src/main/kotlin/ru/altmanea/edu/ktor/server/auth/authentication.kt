package ru.altmanea.edu.ktor.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import ru.altmanea.edu.ktor.model.User
import java.security.MessageDigest
import java.util.*

//  For digest auth
fun getMd5Digest(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray())
const val digestRealm = "Access to the '/digest' path"
val usersHash: Map<String, ByteArray> = userList.associate {
    it.username to getMd5Digest("${it.username}:$digestRealm:${it.password}")
}

// For jwt auth
val secret = "secret"
val issuer = "http://0.0.0.0:8080/"
val audience = "http://0.0.0.0:8080/jwt"
val jwtRealm = "Access to 'jwt'"

fun Application.authentication() {
    install(Sessions) {
        header<UserSession>("user_session") { }
    }
    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to the '/basic' path"
            validate(validator())
        }
        digest("auth-digest") {
            realm = digestRealm
            digestProvider { userName, realm ->
                usersHash[userName]
            }
        }
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate(validator())
        }
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
        session<UserSession>("auth-session") {
            validate { session ->
                session
            }
            challenge {
                call.respondRedirect("/form-login")
            }
        }
    }

    routing {
        authenticate("auth-basic") {
            get("/basic") {
                call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
            }
        }
        authenticate("auth-digest") {
            get("/digest") {
                call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
            }
        }
        authenticate("auth-form") {
            post("/form-login") {
                val userName = call.principal<UserIdPrincipal>()?.name.toString()
                val userRole =
                    userList
                        .find { it.username == userName }
                        ?.let {
                            userRoles[it]
                        } ?: emptySet()
                call.sessions.set(UserSession(userName, 1))
                call.respondText("Hello, $userName!")
            }
        }
        post("jwt-login") {
            val user = call.receive<User>()
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 600000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token))
        }
        authenticate("auth-jwt") {
            get("/jwt") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
            }
        }
        authenticate("auth-session") {
            get("/session") {
                val userSession = call.principal<UserSession>()
                call.sessions.set(userSession?.copy(count = userSession.count + 1))
                call.respondText("Hello, ${userSession?.name}")
            }
        }

    }
}

private fun validator(): suspend ApplicationCall.(UserPasswordCredential) -> Principal? =
    { credentials ->
        if (
            userList.find {
                it.username == credentials.name
            }?.password == credentials.password
        ) {
            UserIdPrincipal(credentials.name)
        } else {
            null
        }
    }


