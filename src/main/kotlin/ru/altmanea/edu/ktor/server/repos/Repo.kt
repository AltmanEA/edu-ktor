package ru.altmanea.edu.ktor.server.repos

interface Repo<E> {

    operator fun get(index: Int): E
    operator fun set(index: Int, value: E)

    fun find(predicate: (E) -> Boolean): Pair<E, Long>?
    fun findIndex(predicate: (E) -> Boolean): Int?
    fun findAll(): List<E>

    fun add(element: E): Boolean

    fun removeIf(predicate: (E) -> Boolean): Boolean

    fun isEmpty(): Boolean
}