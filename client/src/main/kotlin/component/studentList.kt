package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.js.onClickFunction
import modelExt.fullname
import react.Props
import react.dom.*
import react.fc
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.dom.Link
import react.useRef
import ru.altmanea.edu.ktor.model.Config.Companion.studentsURL
import ru.altmanea.edu.ktor.model.Item
import ru.altmanea.edu.ktor.model.Student
import wrappers.AxiosResponse
import wrappers.QueryError
import wrappers.axios
import kotlin.js.json

external interface StudentListProps : Props {
    var students: List<Item<Student>>
    var addStudent: (String, String) -> Unit
    var deleteStudent: (Int) -> Unit
}

fun fcStudentList() = fc("StudentList") { props: StudentListProps ->

    val firstnameRef = useRef<INPUT>()
    val surnameRef = useRef<INPUT>()

    span {
        p {
            +"Firstname: "
            input {
                ref = firstnameRef
            }
        }
        p {
            +"Surname: "
            input {
                ref = surnameRef
            }
        }
        button {
            +"Add student"
            attrs.onClickFunction = {
                firstnameRef.current?.value?.let { firstname ->
                    surnameRef.current?.value?.let { surname ->
                        props.addStudent(firstname, surname)
                    }
                }
            }
        }
    }

    h3 { +"Students" }
    ol {
        props.students.mapIndexed { index, studentItem ->
            li {
                Link {
                    attrs.to = "/student/${studentItem.uuid}"
                    +"${studentItem.elem.fullname} \t"
                }
                button {
                    +"X"
                    attrs.onClickFunction = {
                        props.deleteStudent(index)
                    }
                }
            }
        }
    }
}

fun fcContainerStudentList() = fc("QueryStudentList") { props: AuthContainerOwnProps ->
    val queryClient = useQueryClient()
    val token = "Bearer ${props.token}"

    val query = useQuery<Any, QueryError, AxiosResponse<Array<Item<Student>>>, Any>(
        "studentList",
        {
            axios<Array<Student>>(jso {
                url = studentsURL
                headers = json(
                    "Authorization" to token
                )
            })
        }
    )

    val addStudentMutation = useMutation<Any, Any, Any, Any>(
        { student: Student ->
            axios<String>(jso {
                url = studentsURL
                method = "Post"
                headers = json(
                    "Content-Type" to "application/json",
                    "Authorization" to token
                )
                data = JSON.stringify(student)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("studentList")
            }
        }
    )

    val deleteStudentMutation = useMutation<Any, Any, Any, Any>(
        { studentItem: Item<Student> ->
            axios<String>(jso {
                url = "$studentsURL/${studentItem.uuid}"
                method = "Delete"
                headers = json(
                    "Authorization" to token
                )
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("studentList")
            }
        }
    )



    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val items = query.data?.data?.toList() ?: emptyList()
        child(fcStudentList()) {
            attrs.students = items
            attrs.addStudent = { f, s ->
                addStudentMutation.mutate(Student(f, s), null)
            }
            attrs.deleteStudent = {
                deleteStudentMutation.mutate(items[it], null)
            }
        }
    }
}

