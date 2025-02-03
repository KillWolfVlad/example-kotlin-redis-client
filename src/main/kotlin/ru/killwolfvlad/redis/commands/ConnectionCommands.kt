package ru.killwolfvlad.redis.commands

import ru.killwolfvlad.redis.RedisClient
import ru.killwolfvlad.redis.execute

suspend inline fun RedisClient.ping(message: String? = null) = execute<String>("PING", *listOfNotNull(message).toTypedArray())
