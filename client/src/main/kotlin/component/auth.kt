package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.js.onClickFunction
import org.w3c.dom.url.URLSearchParams
import react.*
import react.dom.*
import ru.altmanea.edu.ktor.model.Config.Companion.serverUrl
import ru.altmanea.edu.ktor.model.User
import userInfo
import wrappers.axios
import kotlin.js.json

interface AuthProps : Props {
    var user: User?
    var signIn: (String, String) -> Unit
    var signOff: () -> Unit
}

fun fAuth() = fc("Auth") { props: AuthProps ->
    val user = props.user
    if (user == null) {
        val userInput = useRef<INPUT>()
        val passInput = useRef<INPUT>()
        div {
            span {
                p { +"Name:" }
                input { ref = userInput }
            }
            span {
                p { +"Pass:" }
                input { ref = passInput }
            }
            button {
                +"Sign in"
                attrs.onClickFunction = {
                    userInput.current?.value?.let { user ->
                        passInput.current?.value?.let { pass ->
                            props.signIn(user, pass)
                        }
                    }
                }
            }
        }
    } else {
        div {
            p {
                +user.username
            }
            button {
                +"Sign off"
                attrs.onClickFunction = { props.signOff() }
            }
        }
    }
}

interface AuthContainerProps : Props {
    var user: User?
    var signIn: (Pair<User, String>) -> Unit
    var signOff: () -> Unit
}

fun fAuthContainer() = fc("AuthContainer") { props: AuthContainerProps ->
    fun signInRequest(user: User) {
        val authParams = URLSearchParams()
        authParams.append("username", user.username)
        authParams.append("password", user.password)
        axios<String>(
            jso {
                url = "$serverUrl/form-login"
                method = "Post"
                headers = json(
                    "Content-Type" to "application/x-www-form-urlencoded"
                )
                data = authParams
            }
        ).then {
            val headers = it.headers
            console.log(headers)
        }
    }
    child(fAuth()) {
        attrs.user = props.user
        attrs.signOff = props.signOff
        attrs.signIn = { user, pass ->
            signInRequest(User(user, pass))
        }
    }
}


fun fAuthManager(render: Render) = fc<Props>("AuthManager") {
    val (user, setUser) = useState<User?>(null)
    val (header, setHeader) = useState("")
    child(fAuthContainer()) {
        attrs.user = user
        attrs.signOff = { setUser(user) }
        attrs.signIn = {
            setUser(it.first)
            setHeader(it.second)
        }
    }
    userInfo.Provider(Pair(user, header)) {
        render()
    }
}