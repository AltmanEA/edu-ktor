package ru.altmanea.edu.ktor.model

import kotlinx.serialization.Serializable

@Serializable
class User(
    val username: String,
    val password: String
)