import component.qcStudentList
import kotlinx.browser.document
import react.dom.render
import react.query.QueryClient
import react.query.QueryClientProvider
import react.router.dom.HashRouter
import wrappers.cReactQueryDevtools

val queryClient = QueryClient()

fun main(){
    render(document.getElementById("root")!!) {
        HashRouter {
            QueryClientProvider {
                attrs.client = queryClient
                child(qcStudentList()) {}
                child(cReactQueryDevtools()) {}
            }
        }
    }
}