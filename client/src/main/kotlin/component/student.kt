package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.useNavigate
import react.router.useParams
import ru.altmanea.edu.ktor.model.Config
import ru.altmanea.edu.ktor.model.Student
import wrappers.AxiosResponse
import wrappers.QueryError
import wrappers.axios
import kotlin.js.Promise
import kotlin.js.json

external interface StudentProps : Props {
    var students: Student
    var updateStudent: (String, String) -> Unit
}


fun fcStudent() = fc("Student") { props: StudentProps ->
    val firstnameRef = useRef<INPUT>()
    val surnameRef = useRef<INPUT>()

    val (firstname, setFirstname) = useState(props.students.firstname)
    val (surname, setSurname) = useState(props.students.surname)

    fun onInputEdit(setter: StateSetter<String>, ref: MutableRefObject<INPUT>) =
        { _: Event ->
            setter(ref.current?.value ?: "ERROR!")
        }

    span {
        p {
            +"Firstname: "
            input {
                ref = firstnameRef
                attrs.value = firstname
                attrs.onChangeFunction = onInputEdit(setFirstname, firstnameRef)
            }
        }
        p {
            +"Surname: "
            input {
                ref = surnameRef
                attrs.value = surname
                attrs.onChangeFunction = onInputEdit(setSurname, surnameRef)
            }
        }
        button {
            +"Update student"
            attrs.onClickFunction = {
                firstnameRef.current?.value?.let { firstname ->
                    surnameRef.current?.value?.let { surname ->
                        props.updateStudent(firstname, surname)
                    }
                }
            }
        }
    }
}

fun fcContainerStudent() = fc("ContainerStudent") { props: AuthContainerOwnProps ->
    val studentParams = useParams()
    val navigate = useNavigate()

    val studentId = studentParams["id"] ?: "Route param error"
    val token = "Bearer ${props.token}"

    val query = useQuery<Any, QueryError, AxiosResponse<Student>, Any>(
        studentId,
        {
            axios<Array<Student>>(jso {
                url = Config.studentsPath + studentId
                headers = json(
                    "Authorization" to token
                )
            })
        }
    )

    val updateStudentMutation = useMutation<Any, Any, Pair<Student, Student>, Any>(
        { oldAndNewStudent ->
            axios<String>(jso {
                url = "${Config.studentsURL}/${oldAndNewStudent.first.idName}"
                method = "Put"
                headers = json(
                    "Content-Type" to "application/json",
                    "Authorization" to token
                )
                data = JSON.stringify(oldAndNewStudent.second)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                Promise { _, _ -> navigate("/") }
            }
        }
    )

    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val data = query.data?.data!!
        val oldStudent = Student(data.firstname, data.surname)
        child(fcStudent()) {
            attrs.students = oldStudent
            attrs.updateStudent = { f, s ->
                updateStudentMutation.mutate(Pair(oldStudent, Student(f, s)), null)
            }
        }
    }
}


