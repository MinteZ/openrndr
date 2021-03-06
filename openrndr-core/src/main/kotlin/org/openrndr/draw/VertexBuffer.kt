package org.openrndr.draw

import org.openrndr.internal.Driver
import java.nio.ByteBuffer

interface VertexBuffer {
    companion object {
        fun createDynamic(format: VertexFormat, vertexCount: Int): VertexBuffer = Driver.instance.createDynamicVertexBuffer(format, vertexCount)

        fun createFromFloats(format: VertexFormat, data: FloatArray): VertexBuffer {
            require((data.size * 4) % format.size == 0) {
                "supplied data size doesn't match format size"
            }
            val vertexBuffer = createDynamic(format, (data.size * 4) / format.size)

            vertexBuffer.put {
                write(data)
            }
            Session.active.track(vertexBuffer)
            return vertexBuffer
        }
    }

    val vertexFormat: VertexFormat
    val vertexCount: Int

    /**
     * Gives a read/write shadow for the vertex buffer
     */
    val shadow: VertexBufferShadow

    fun write(data: ByteBuffer, offset: Int = 0)
    fun read(data: ByteBuffer, offset: Int = 0)

    /**
     * Destroy the vertex buffer
     */
    fun destroy()

    fun put(putter: BufferWriter.() -> Unit): Int {
        val w = shadow.writer()
        w.rewind()
        w.putter()
        if (w.position % vertexFormat.size != 0) {
            throw RuntimeException("incomplete vertices written. likely violating the specified vertex format $vertexFormat")
        }
        val count = w.positionElements
        shadow.uploadElements(0, count)
        w.rewind()
        return count
    }
}

/**
 * VertexBuffer builder function.
 * @param vertexFormat a VertexFormat object that describes the vertex layout
 * @param vertexCount the number of vertices the vertex buffer should hold
 */
fun vertexBuffer(vertexFormat: VertexFormat, vertexCount: Int): VertexBuffer {
    val vertexBuffer = VertexBuffer.createDynamic(vertexFormat, vertexCount)
    Session.active.track(vertexBuffer)
    return vertexBuffer
}