package ru.killwolfvlad.redis.internal.extensions

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readByte
import kotlinx.io.Buffer
import kotlinx.io.readString
import ru.killwolfvlad.redis.internal.consts.CARRIAGE_RETURN_BYTE
import ru.killwolfvlad.redis.internal.consts.NEWLINE_BYTE

internal suspend inline fun ByteReadChannel.readRedisResponse(): Any? {
    val type = readLineCRLF().readString(Charsets.UTF_8)
    val typeArgument = type.slice(1 until type.length)

    return when (type.first()) {
        '+' -> typeArgument
        '$' -> readBlobString(typeArgument.toLong())?.readString(Charsets.UTF_8)
        else -> throw UnsupportedOperationException("unsupported response type: $type!")
    }
}

internal suspend inline fun ByteReadChannel.readLineCRLF(): Buffer {
    val buffer = Buffer()

    while (true) {
        val byte = readByte()

        if (byte == CARRIAGE_RETURN_BYTE) {
            val nextByte = readByte()
            if (nextByte == NEWLINE_BYTE) {
                break
            } else {
                buffer.writeByte(CARRIAGE_RETURN_BYTE)
                buffer.writeByte(nextByte)
                continue
            }
        }

        buffer.writeByte(byte)
    }

    return buffer
}

internal suspend inline fun ByteReadChannel.readBlobString(size: Long): Buffer? {
    if (size == -1L) {
        return null
    }

    val buffer = Buffer()

    (0 until size).forEach {
        val byte = readByte()

        buffer.writeByte(byte)
    }

    val carriageReturnByte = readByte()
    val newlineByte = readByte()

    if (carriageReturnByte != CARRIAGE_RETURN_BYTE || newlineByte != NEWLINE_BYTE) {
        throw IllegalStateException("blob string must ended with \\r\\n!")
    }

    return buffer
}
