package ru.altmanea.edu.ktor.server.auth

// Based on https://github.com/ximedes/ktor-authorization

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import ru.altmanea.edu.ktor.server.auth.Role

class RoleBasedAuthorization(config: Configuration) {
    private val getRoles = config._getRoles

    class Configuration {
        internal var _getRoles: (Principal) -> Set<Role> = { emptySet() }

        fun getRoles(builder: (Principal) -> Set<Role>) {
            _getRoles = builder
        }
    }

    fun interceptPipeline(
        pipeline: ApplicationCallPipeline,
        allowedRoles: Set<Role>
    ) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Features, Authentication.ChallengePhase)
        pipeline.insertPhaseAfter(Authentication.ChallengePhase, AuthorizationPhase)

        pipeline.intercept(AuthorizationPhase) {
            val principal = call.authentication.principal<Principal>()
            if (principal == null)
                call.respond(HttpStatusCode.Forbidden, "Permission is denied")
            else
                if (allowedRoles.intersect(getRoles(principal)).isEmpty())
                    call.respond(HttpStatusCode.Forbidden, "Permission is denied")
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, RoleBasedAuthorization> {
        override val key = AttributeKey<RoleBasedAuthorization>("RoleBasedAuthorization")

        val AuthorizationPhase = PipelinePhase("Authorization")

        override fun install(
            pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit
        ): RoleBasedAuthorization {
            val configuration = Configuration().apply(configure)
            return RoleBasedAuthorization(configuration)
        }
    }

}

class AuthorizedRouteSelector(private val description: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant

    override fun toString(): String = "(authorize ${description})"
}

fun Route.authorizedRoute(
    allowedRoles: Set<Role>,
    build: Route.() -> Unit
): Route {
    val authorizedRoute = createChild(
        AuthorizedRouteSelector("Roles: ${allowedRoles.joinToString(", ")}")
    )
    application.feature(RoleBasedAuthorization).interceptPipeline(authorizedRoute, allowedRoles)
    authorizedRoute.build()
    return authorizedRoute
}
