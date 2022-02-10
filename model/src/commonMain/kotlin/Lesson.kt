package ru.altmanea.edu.ktor.model

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val name: String,
    val students: MutableSet<String> = HashSet(),
    val marks: MutableMap<String, Int> = HashMap()
)