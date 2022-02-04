package ru.altmanea.edu.ktor.model

import kotlinx.serialization.*

@Serializable
class Student(
    val firstname: String,
    val surname: String
)