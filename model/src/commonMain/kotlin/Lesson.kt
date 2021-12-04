package ru.altmanea.edu.ktor.model

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val name: String,
    val students: MutableList<Student> = ArrayList(),
    val marks: MutableList<Pair<String, String>> = ArrayList()
)