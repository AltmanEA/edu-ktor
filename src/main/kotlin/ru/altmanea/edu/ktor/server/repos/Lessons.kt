package ru.altmanea.edu.ktor.server.repos

import ru.altmanea.edu.ktor.model.Lesson

val lessonsRepo = ListRepo<Lesson>()

val lessonsRepoTestData = listOf(
    Lesson("Math"), //, listOf("Sheldon","Leonard", "Howard").map { studentsRepo.urlByFirstname(it)!! }.toMutableSet()),
    Lesson("Phys"), //, listOf("Sheldon","Leonard").map { studentsRepo.urlByFirstname(it)!! }.toMutableSet()),
    Lesson("Story"), //, listOf("Sheldon","Leonard", "Penny").map { studentsRepo.urlByFirstname(it)!! }.toMutableSet())
)

