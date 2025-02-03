package ru.killwolfvlad.redis

import kotlinx.coroutines.SupervisorJob
import ru.killwolfvlad.redis.internal.RedisConnection

class RedisClient(
    redisConnectionString: String,
) {
    private val rootJob = SupervisorJob()

    private var connection: RedisConnection =
        RedisConnection(rootJob, redisConnectionString)

    suspend fun init() {
        connection.init()
    }

    fun close() {
        connection.close()
    }

    suspend fun executeRaw(
        command: String,
        vararg arguments: String,
    ): Any? = connection.execute(command, *arguments)
}

suspend inline fun <reified T> RedisClient.execute(
    command: String,
    vararg arguments: String,
): T = executeRaw(command, *arguments) as T
