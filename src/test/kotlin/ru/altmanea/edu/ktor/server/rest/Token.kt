package ru.altmanea.edu.ktor.server.rest

import kotlinx.serialization.Serializable

@Serializable
internal class Token(
    val token: String
)