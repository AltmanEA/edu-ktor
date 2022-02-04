package modelExt

import ru.altmanea.edu.ktor.model.Student

val Student.fullname: String
    get() = "${this.firstname} ${this.surname}"