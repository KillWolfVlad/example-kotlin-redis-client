package ru.killwolfvlad.redis.commands

import ru.killwolfvlad.redis.RedisClient
import ru.killwolfvlad.redis.execute

suspend inline fun RedisClient.set(
    key: String,
    value: String,
) = execute<String>("SET", key, value)

suspend inline fun RedisClient.get(key: String) = execute<String?>("GET", key)
