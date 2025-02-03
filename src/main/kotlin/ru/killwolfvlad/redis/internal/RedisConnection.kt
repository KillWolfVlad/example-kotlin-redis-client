package ru.killwolfvlad.redis.internal

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.killwolfvlad.redis.internal.extensions.readRedisResponse
import java.net.URI

internal class RedisConnection(
    rootJob: Job,
    private val redisConnectionString: String,
) {
    private val coroutineScope =
        CoroutineScope(
            rootJob + Dispatchers.IO + CoroutineName(RedisConnection::class.simpleName + "Coroutine"),
        )

    private lateinit var selectorManager: SelectorManager
    private lateinit var socket: Socket
    private lateinit var readChannel: ByteReadChannel
    private lateinit var writeChannel: ByteWriteChannel

    private val requestsSharedFlow = MutableSharedFlow<RedisRequest>()
    private lateinit var requestsCollectJob: Job

    suspend fun init() {
        val url = URI(redisConnectionString)

        selectorManager = SelectorManager(Dispatchers.IO)

        socket = aSocket(selectorManager).tcp().connect(url.host, url.port)

        readChannel = socket.openReadChannel()
        writeChannel = socket.openWriteChannel()

        requestsCollectJob =
            coroutineScope.launch {
                requestsSharedFlow.collect { request ->
                    writeRequest(request)
                }
            }
    }

    fun close() {
        requestsCollectJob.cancel()

        socket.close()
        selectorManager.close()
    }

    suspend fun execute(
        command: String,
        vararg arguments: String,
    ): Any? {
        val responseChannel = Channel<Any?>(1)

        requestsSharedFlow.emit(RedisRequest(command, arguments.toList(), responseChannel))

        val response = responseChannel.receive()

        return response
    }

    private suspend inline fun writeRequest(request: RedisRequest) {
        val rawRequest = request.toRawRequest()

        writeChannel.writeStringUtf8(rawRequest)
        writeChannel.flush()

        readResponse(request)
    }

    private suspend inline fun readResponse(request: RedisRequest) {
        val response = readChannel.readRedisResponse()

        request.responseChannel.send(response)
    }
}
