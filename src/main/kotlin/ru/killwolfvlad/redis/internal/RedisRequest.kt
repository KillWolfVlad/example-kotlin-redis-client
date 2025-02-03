package ru.killwolfvlad.redis.internal

import kotlinx.coroutines.channels.Channel

internal data class RedisRequest(
    val command: String,
    val arguments: List<String>,
    val responseChannel: Channel<Any?>,
)

/**
 * Example raw request for `SET key1 value1` (spaces and new lines only for readability)
 *
 * ```
 * *3\r\n
 *   $3\r\nSET\r\n
 *   $4\r\nkey1\r\n
 *   $6\r\nvalue1\r\n
 * ```
 */
internal fun RedisRequest.toRawRequest(): String =
    "*${1 + arguments.size}\r\n${
        listOf(
            command,
            *arguments.toTypedArray(),
        ).joinToString("") { "$${it.toByteArray().size}\r\n$it\r\n" }
    }"
