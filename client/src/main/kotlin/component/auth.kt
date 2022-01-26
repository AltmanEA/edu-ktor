package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
            p{
                +"Name: "
                input { ref = userInput }
            }
            p {
                +"Pass: "
                input { ref = passInput }
            }
            p {
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

interface AxiosData {
    val token: String
}

fun fAuthContainer() = fc("AuthContainer") { props: AuthContainerProps ->
    fun signInRequest(user: User) {
        axios<AxiosData>(
            jso {
                url = "$serverUrl/jwt-login"
                method = "Post"
                headers = json(
                    "Content-Type" to "application/json"
                )
                data = Json.encodeToString(user)
            }
        ).then {
            val token = it.data.token
            console.log(token)
            props.signIn(Pair(user, token))
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
    val nullUserToken = Pair(null, "")
    val (userToken, setUserToken) = useState<Pair<User?, String>>(nullUserToken)
    child(fAuthContainer()) {
        attrs.user = userToken.first
        attrs.signOff = { setUserToken(nullUserToken) }
        attrs.signIn = { setUserToken(it) }
    }
    userInfo.Provider(userToken) {
        render()
    }
}

external interface AuthContainerOwnProps : Props {
    var token: String
}

fun qcStudentAuth(componentContainer: FC<AuthContainerOwnProps>) = fc("AuthStudent") { _: Props ->
    val user = useContext(userInfo)
    if (user.first == null)
        p { +"Authentication is required" }
    else
        child(componentContainer) {
            attrs.token = user.second
        }
}
