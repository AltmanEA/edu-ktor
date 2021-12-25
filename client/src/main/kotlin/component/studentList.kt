package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.js.onClickFunction
import react.Props
import react.dom.*
import react.fc
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.useContext
import react.useRef
import ru.altmanea.edu.ktor.model.Config.Companion.studentsURL
import ru.altmanea.edu.ktor.model.Student
import ru.altmanea.edu.ktor.model.User
import userInfo
import wrappers.AxiosResponse
import wrappers.QueryError
import wrappers.axios
import kotlin.js.json

interface StudentListProps : Props {
    var students: List<Student>
    var addStudent: (String, String) -> Unit
    var deleteStudent: (Int) -> Unit
}

fun fcStudentList() = fc("StudentList") { props: StudentListProps ->
    h3 { +"Students" }
    ol {
        props.students.mapIndexed { index, student ->
            li {
                +"${student.fullName} \t"
                button {
                    +"X"
                    attrs.onClickFunction = {
                        props.deleteStudent(index)
                    }
                }
            }
        }
    }

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
}

typealias QueryData = Array<Student>

interface StudentListContainerOwnProps : Props {
    var token: String
}

fun qcStudentListAuth() = fc("AuthStudentList") { _: Props ->
    val user = useContext(userInfo)
    if(user.first == null)
        p { +"Authentication is required"}
    else
        child(qcStudentList()){
            attrs.token = user.second
        }
}

fun qcStudentList() = fc("QueryStudentList") { props: StudentListContainerOwnProps ->
    val queryClient = useQueryClient()
    val token = "Bearer ${props.token}"

    val query = useQuery<Any, QueryError, AxiosResponse<QueryData>, Any>(
        "studentList",
        {
            axios<QueryData>(jso {
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
        { student: Student ->
            axios<String>(jso {
                url = "$studentsURL/${student.idName}"
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
        val data = query.data?.data.unsafeCast<QueryData>()
        val students = data.map { Student(it.firstname, it.surname) }
        child(fcStudentList()) {
            attrs.students = students
            attrs.addStudent = { f, s ->
                addStudentMutation.mutate(Student(f, s), null)
            }
            attrs.deleteStudent = {
                deleteStudentMutation.mutate(students[it], null)
            }
        }
    }
}