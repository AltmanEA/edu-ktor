package ru.altmanea.edu.ktor.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.util.*

@Serializable
class User(
    val username: String,
    val password: String
)

data class UserSession(val name: String, val count: Int) : Principal

val users = listOf("tutor", "admin").map { User(it, it) }

fun getMd5Digest(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray())
const val digestRealm = "Access to the '/digest' path"
val usersHash: Map<String, ByteArray> = users.associate {
    it.username to getMd5Digest("${it.username}:$digestRealm:${it.password}")
}

val secret = "secret"
val issuer = "http://0.0.0.0:8080/"
val audience = "http://0.0.0.0:8080/jwt"
val jwtRealm = "Access to 'jwt'"

fun Application.auth() {
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60
        }
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
                call.respondRedirect("/from-login")
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
                call.sessions.set(UserSession(name = userName, count = 1))
                call.respondText("Hello, $userName!")
            }
        }
        post("/jwt-login") {
            val user = call.receive<User>()
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
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
            get("/hello") {
                val userSession = call.principal<UserSession>()
                call.sessions.set(userSession?.copy(count = userSession.count + 1))
                call.respondText("Hello, ${userSession?.name}! Visit count is ${userSession?.count}.")
            }
        }

    }

}

private fun validator(): suspend ApplicationCall.(UserPasswordCredential) -> Principal? =
    { credentials ->
        if (
            users.find {
                it.username == credentials.name
            }?.password == credentials.password
        ) {
            UserIdPrincipal(credentials.name)
        } else {
            null
        }
    }


