import component.fAuthManager
import component.qcStudentList
import kotlinx.browser.document
import react.createContext
import react.dom.render
import react.query.QueryClient
import react.query.QueryClientProvider
import react.router.dom.HashRouter
import ru.altmanea.edu.ktor.model.User
import wrappers.cReactQueryDevtools

val queryClient = QueryClient()
val userInfo = createContext(Pair<User?, String>(null, ""))

fun main() {
    render(document.getElementById("root")!!) {
        HashRouter {
            child(fAuthManager {
                QueryClientProvider {
                    attrs.client = queryClient
                    child(qcStudentList()) {}
                    child(cReactQueryDevtools()) {}
                }
            })
        }
    }
}