package ru.altmanea.edu.ktor.server.repos

import ru.altmanea.edu.ktor.model.Lesson

val lessonsRepo = listOf(
    Lesson("Math", listOf(0, 1, 2).map { studentsRepo[it] }.toMutableList()),
    Lesson("Phys", listOf(0, 1).map { studentsRepo[it] }.toMutableList()),
    Lesson("Story", listOf(0, 1, 3).map { studentsRepo[it] }.toMutableList())
).toMutableList()