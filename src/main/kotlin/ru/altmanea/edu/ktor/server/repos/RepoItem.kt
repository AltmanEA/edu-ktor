package ru.altmanea.edu.ktor.server.repos

import kotlinx.serialization.Serializable
import ru.altmanea.edu.ktor.model.Item
import java.util.*

@Serializable
class RepoItem<E>(
    override val elem: E,
    override val uuid: String = UUID.randomUUID().toString(),
    override val etag: Long = System.currentTimeMillis()
) : Item<E>
