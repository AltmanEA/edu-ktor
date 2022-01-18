package ru.altmanea.edu.ktor.server.repos

import java.lang.System.currentTimeMillis

class ListRepo<E> : Repo<E> {
    private val list = ArrayList<Pair<E, Long>>()

    override fun get(index: Int): E = list[index].first
    override fun set(index: Int, value: E) {
        list[index] = Pair(value, currentTimeMillis())
    }

    override fun find(predicate: (E) -> Boolean): Pair<E, Long>? = list.find { predicate(it.first) }
    override fun findIndex(predicate: (E) -> Boolean): Int? =
        list.indexOf(find(predicate)).let {
            if (it < 0) null else it
        }

    override fun findAll(): List<E> = list.map { it.first }

    override fun add(element: E): Boolean = list.add(Pair(element, currentTimeMillis()))

    override fun removeIf(predicate: (E) -> Boolean): Boolean = list.removeIf { predicate(it.first) }

    override fun isEmpty(): Boolean = list.isEmpty()

}