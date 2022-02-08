package ru.altmanea.edu.ktor.server.repos

import ru.altmanea.edu.ktor.model.Config
import ru.altmanea.edu.ktor.model.Link
import ru.altmanea.edu.ktor.model.Student

val studentsRepo = ListRepo<Student>()

fun ListRepo<Student>.urlByUUID(uuid: String) =
    this[uuid]?.let {
        Link(Config.Companion.studentsURL + it.uuid)
    }

fun ListRepo<Student>.urlByFirstname(firstname: String) =
    this.find { it.firstname == firstname }.let {
        if (it.size == 1)
            Link(Config.Companion.studentsURL + it.first().uuid)
        else
            null
    }


val studentsRepoTestData = listOf(
    Student("Sheldon", "Cooper"),
    Student("Leonard", "Hofstadter"),
    Student("Howard", "Wolowitz"),
    Student("Penny", "Hofstadter"),
)
