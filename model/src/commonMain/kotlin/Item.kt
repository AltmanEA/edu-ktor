package ru.altmanea.edu.ktor.model

interface Item<E> {
    val elem: E
    val uuid: String
    val etag: Long
}