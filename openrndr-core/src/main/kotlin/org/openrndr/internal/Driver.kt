package org.openrndr.internal

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import java.io.InputStream
import java.nio.Buffer

/**
 *  built-in shader generators
 */
interface ShaderGenerators {
    fun vertexBufferFragmentShader(shadeStructure: ShadeStructure): String
    fun vertexBufferVertexShader(shadeStructure: ShadeStructure): String

    fun imageFragmentShader(shadeStructure: ShadeStructure): String
    fun imageVertexShader(shadeStructure: ShadeStructure): String

    fun imageArrayTextureFragmentShader(shadeStructure: ShadeStructure): String
    fun imageArrayTextureVertexShader(shadeStructure: ShadeStructure): String


    fun pointFragmentShader(shadeStructure: ShadeStructure) : String
    fun pointVertexShader(shadeStructure: ShadeStructure) : String

    fun circleFragmentShader(shadeStructure: ShadeStructure): String
    fun circleVertexShader(shadeStructure: ShadeStructure): String

    fun fontImageMapFragmentShader(shadeStructure: ShadeStructure): String
    fun fontImageMapVertexShader(shadeStructure: ShadeStructure): String

    fun rectangleFragmentShader(shadeStructure: ShadeStructure): String
    fun rectangleVertexShader(shadeStructure: ShadeStructure): String

    fun expansionFragmentShader(shadeStructure: ShadeStructure): String
    fun expansionVertexShader(shadeStructure: ShadeStructure): String

    fun fastLineFragmentShader(shadeStructure: ShadeStructure): String
    fun fastLineVertexShader(shadeStructure: ShadeStructure): String

    fun meshLineFragmentShader(shadeStructure: ShadeStructure): String
    fun meshLineVertexShader(shadeStructure: ShadeStructure): String
}


/**
 * Driver interface. This is the internal interface
 */
interface Driver {

    val contextID: Long


    fun createShader(vsCode: String, fsCode: String): Shader

    fun createComputeShader(code: String): ComputeShader

    fun createShadeStyleManager(vertexShaderGenerator: (ShadeStructure) -> String,
                                fragmentShaderGenerator: (ShadeStructure) -> String): ShadeStyleManager

    fun createRenderTarget(width: Int, height: Int, contentScale: Double = 1.0, multisample: BufferMultisample = BufferMultisample.Disabled): RenderTarget


    fun createArrayTexture(width: Int, height: Int, layers: Int, format: ColorFormat, type: ColorType): ArrayTexture
    fun createAtomicCounterBuffer(counterCount : Int) : AtomicCounterBuffer

    fun createColorBuffer(width: Int, height: Int, contentScale: Double, format: ColorFormat, type: ColorType, multisample: BufferMultisample = BufferMultisample.Disabled): ColorBuffer
    fun createColorBufferFromUrl(url: String): ColorBuffer
    fun createColorBufferFromFile(filename: String): ColorBuffer
    fun createColorBufferFromStream(stream: InputStream, name: String? = null, formatHint: String? = null): ColorBuffer

    fun createDepthBuffer(width: Int, height: Int, format: DepthFormat, multisample: BufferMultisample = BufferMultisample.Disabled): DepthBuffer
    fun createBufferTexture(elementCount: Int, format: ColorFormat, type: ColorType): BufferTexture

    fun createCubemap(width: Int, format: ColorFormat, type: ColorType): Cubemap
    fun createCubemapFromUrls(urls: List<String>): Cubemap

    fun createResourceThread(f: () -> Unit): ResourceThread
    fun createDrawThread(): DrawThread

    fun clear(r: Double, g: Double, b: Double, a: Double)
    fun clear(color: ColorRGBa) {
        clear(color.r, color.g, color.b, color.a)
    }

    fun createDynamicVertexBuffer(format: VertexFormat, vertexCount: Int): VertexBuffer
    fun createStaticVertexBuffer(format: VertexFormat, buffer: Buffer): VertexBuffer

    fun createDynamicIndexBuffer(elementCount: Int, type: IndexType): IndexBuffer

    fun drawVertexBuffer(shader: Shader, vertexBuffers: List<VertexBuffer>,
                         drawPrimitive: DrawPrimitive,
                         vertexOffset: Int, vertexCount: Int)

    fun drawIndexedVertexBuffer(shader: Shader, indexBuffer: IndexBuffer, vertexBuffers: List<VertexBuffer>,
                                drawPrimitive: DrawPrimitive,
                                indexOffset: Int, indexCount: Int)


    fun drawInstances(shader: Shader, vertexBuffers: List<VertexBuffer>,
                      instanceAttributes: List<VertexBuffer>,
                      drawPrimitive: DrawPrimitive, vertexOffset: Int, vertexCount: Int, instanceCount: Int)

    fun drawIndexedInstances(shader: Shader, indexBuffer: IndexBuffer, vertexBuffers: List<VertexBuffer>,
                             instanceAttributes: List<VertexBuffer>,
                             drawPrimitive: DrawPrimitive, indexOffset: Int, indexCount: Int, instanceCount: Int)

    fun setState(drawStyle: DrawStyle)

    val fontImageMapManager: FontMapManager
    val fontVectorMapManager: FontMapManager
    val shaderGenerators: ShaderGenerators
    val activeRenderTarget: RenderTarget

    /**
     * waits for all drawing to complete
     */
    fun finish()


    fun internalShaderResource(resourceId: String): String

    companion object {
        lateinit var driver: Driver
        val instance: Driver get() = driver
    }
}

/**
 * Wait for the [Driver] to finish drawing
 */
fun finish() {
    Driver.instance.finish()
}