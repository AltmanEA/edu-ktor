package ru.altmanea.edu.ktor.server.repos

import ru.altmanea.edu.ktor.model.Student

val studentsRepo = listOf(
    Student("Sheldon", "Cooper"),
    Student("Leonard", "Hofstadter"),
    Student("Howard", "Wolowitz"),
    Student("Penny", "Hofstadter"),
).toMutableList()