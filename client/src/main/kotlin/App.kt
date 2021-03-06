import component.*
import kotlinx.browser.document
import react.createContext
import react.createElement
import react.dom.render
import react.query.QueryClient
import react.query.QueryClientProvider
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.dom.Link
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
                    Link {
                        attrs.to = "/"
                        +"Home"
                    }
                    Routes {
                        Route {
                            attrs.index = true
                            attrs.element =
                                createElement(
                                    qcStudentAuth(
                                        fcContainerStudentList()
                                    )
                                )
                        }
                        Route {
                            attrs.path = "/student/:id"
                            attrs.element =
                                createElement(
                                    qcStudentAuth(
                                        fcContainerStudent()
                                    )
                                )
                        }
                    }
                    child(cReactQueryDevtools()) {}
                }
            })
        }
    }
}