package ru.altmanea.edu.ktor.model

import kotlinx.serialization.*

@Serializable
class Student(
    val firstname: String,
    val surname: String
) {
    val shortName
        get() = "${firstname[0]}. $surname"
    val fullName
        get() = "$firstname $surname"
    val idName
        get() = "$firstname$surname"
}