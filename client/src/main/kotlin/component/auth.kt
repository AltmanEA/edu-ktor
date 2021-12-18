package component

import react.Props
import react.fc
import ru.altmanea.edu.ktor.model.User

typealias AuthHeader = String

interface AuthProps: Props {
    var user: User?
    var signIn: ()-> Pair<User, AuthHeader>
    var signOff: () -> Unit
}

fun fAuth() = fc("Auth"){props: AuthProps ->

}