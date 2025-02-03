package ru.killwolfvlad.redis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import redis.clients.jedis.JedisPooled
import ru.killwolfvlad.redis.commands.get
import ru.killwolfvlad.redis.commands.ping
import ru.killwolfvlad.redis.commands.set

class RedisClientTest :
    DescribeSpec({
        val redisConnectionString = "redis://localhost:6379"

        val jedis = JedisPooled(redisConnectionString)
        val redisClient = RedisClient(redisConnectionString)

        beforeSpec {
            redisClient.init()
        }

        beforeEach {
            jedis.flushDB()
        }

        afterSpec {
            redisClient.close()
        }

        it("ping without arguments") {
            redisClient.ping() shouldBe "PONG"
        }

        it("ping with arguments") {
            redisClient.ping("Привет! \uD83D\uDC4B") shouldBe "Привет! \uD83D\uDC4B"
        }

        it("set when key doesn't exist") {
            redisClient.set("key1", "value1") shouldBe "OK"

            jedis.get("key1") shouldBe "value1"
        }

        it("set empty value") {
            redisClient.set("key1", "") shouldBe "OK"

            jedis.get("key1") shouldBe ""
        }

        it("set when key exists") {
            jedis.set("key1", "value2")

            redisClient.set("key1", "value1") shouldBe "OK"

            jedis.get("key1") shouldBe "value1"
        }

        it("get when key doesn't exists") {
            redisClient.get("key1") shouldBe null
        }

        it("get empty value") {
            jedis.set("key1", "")

            redisClient.get("key1") shouldBe ""
        }

        it("get when key exists") {
            jedis.set("key1", "value1")

            redisClient.get("key1") shouldBe "value1"
        }
    })
