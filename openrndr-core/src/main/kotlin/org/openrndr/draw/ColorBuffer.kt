package org.openrndr.draw

import org.openrndr.color.ColorRGBa
import org.openrndr.internal.Driver
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import java.io.File
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.nio.ByteBuffer

/**
 * File format used while saving to file
 */
enum class FileFormat {
    JPG,
    PNG,
}

/**
 * Texture wrapping mode
 */
enum class WrapMode {
    CLAMP_TO_EDGE,
    REPEAT,
    MIRRORED_REPEAT
}

/**
 * Texture filters used for minification
 */
enum class MinifyingFilter {
    NEAREST,
    LINEAR,
    NEAREST_MIPMAP_NEAREST,
    LINEAR_MIPMAP_NEAREST,
    NEAREST_MIPMAP_LINEAR,
    LINEAR_MIPMAP_LINEAR
}

/**
 * Texture filters for magnification
 */
enum class MagnifyingFilter {
    /** nearest neighbour, blocky */
    NEAREST,
    LINEAR
}

/**
 * Buffer multisample options
 */
sealed class BufferMultisample {
    /**
     * Disable multisampling
     */
    object Disabled : BufferMultisample()

    /**
     * Enable multisampling with a given [sampleCount]
     */
    data class SampleCount(val sampleCount: Int) : BufferMultisample()
}


interface ColorBuffer {

    /** the width of the [ColorBuffer] in device units */
    val width: Int

    /** the height of the [ColorBuffer] in device units */
    val height: Int

    /** the content scale of the [ColorBuffer] */
    val contentScale: Double
    val format: ColorFormat
    val type: ColorType

    /** the multisampling method used for this [ColorBuffer] */
    val multisample: BufferMultisample

    val bounds: Rectangle get() = Rectangle(Vector2.ZERO, width * 1.0, height * 1.0)

    /** the width of the [ColorBuffer] in pixels */
    val effectiveWidth: Int get() = (width * contentScale).toInt()

    /** the height of the [ColorBuffer] in pixels */
    val effectiveHeight: Int get() = (height * contentScale).toInt()

    /** save the [ColorBuffer] to [File] */
    fun saveToFile(file: File, fileFormat: FileFormat = guessFromExtension(file), async: Boolean = true)

    private fun guessFromExtension(file: File): FileFormat {
        val extension = file.extension.toLowerCase()
        return when (extension) {
            "jpg", "jpeg" -> FileFormat.JPG
            "png" -> FileFormat.PNG
            else -> throw IllegalArgumentException("unsupported format: \"$extension\"")
        }
    }

    /** destroys the underlying [ColorBuffer] resources */
    fun destroy()

    /** binds the colorbuffer to a texture unit, internal API */
    fun bind(unit: Int)

    fun write(buffer: ByteBuffer, sourceFormat:ColorFormat = format, sourceType:ColorType = type)
    fun read(buffer: ByteBuffer)

    /** generates mipmaps from the top-level mipmap */
    fun generateMipmaps()

    /**
     * resolves contents to a non-multisampled color buffer
     */
    fun resolveTo(target: ColorBuffer)

    /**
     * copies contents to a target color buffer
     */
    fun copyTo(target: ColorBuffer)

    fun copyTo(target: ArrayTexture, layer:Int)

    fun fill(color: ColorRGBa)

    /** the wrapping mode to use in the horizontal direction */
    var wrapU: WrapMode

    /** the wrapping mode to use in the vertical direction */
    var wrapV: WrapMode

    /** the filter to use when displaying at sizes smaller than the original */
    var filterMin: MinifyingFilter

    /** the filter to use when display at sizes larger than the original */
    var filterMag: MagnifyingFilter

    val shadow: ColorBufferShadow

    /**
     * should the v coordinate be flipped because the [ColorBuffer] contents are stored upside-down?
     */
    var flipV: Boolean

    /**
     * sets the [ColorBuffer] filter for minifying and magnification
     */
    fun filter(filterMin: MinifyingFilter, filterMag: MagnifyingFilter) {
        this.filterMin = filterMin
        this.filterMag = filterMag
    }

    companion object {
        @Deprecated("use the colorBuffer() builder function instead")
        fun create(width: Int, height: Int,
                   contentScale: Double = 1.0,
                   format: ColorFormat = ColorFormat.RGBa, type: ColorType = ColorType.UINT8,
                   multisample: BufferMultisample = BufferMultisample.Disabled
        ) = Driver.instance.createColorBuffer(width, height, contentScale, format, type, multisample)

        fun fromUrl(url: String): ColorBuffer {
            val colorBuffer = Driver.instance.createColorBufferFromUrl(url)
            Session.active.track(colorBuffer)
            return colorBuffer
        }

        fun fromFile(file: File): ColorBuffer {
            val colorBuffer = Driver.instance.createColorBufferFromFile(file.absolutePath)
            Session.active.track(colorBuffer)
            return colorBuffer
        }

        fun fromFile(filename: String): ColorBuffer {
            val colorBuffer = Driver.instance.createColorBufferFromFile(filename)
            Session.active.track(colorBuffer)
            return colorBuffer
        }

        fun fromStream(stream: InputStream, formatHint:String? = null): ColorBuffer {
            val colorBuffer = Driver.instance.createColorBufferFromStream(stream)
            return colorBuffer
        }
    }
}


/**
 * creates a [ColorBuffer]
 * @param width the width in device units
 * @param height the height in device units
 * @param contentScale content scale used for denoting hi-dpi content
 * @param format the color format
 * @param type the color type
 * @param format the color format
 */
fun colorBuffer(width: Int, height: Int, contentScale: Double = 1.0, format: ColorFormat = ColorFormat.RGBa, type: ColorType = ColorType.UINT8, multisample: BufferMultisample = BufferMultisample.Disabled): ColorBuffer {
    val colorBuffer = Driver.driver.createColorBuffer(width, height, contentScale, format, type, multisample)
    Session.active.track(colorBuffer)
    return colorBuffer
}

/**
 * loads an image from a file or url encoded as [String]
 */
fun loadImage(fileOrUrl: String): ColorBuffer {
    return try {
        URL(fileOrUrl)
        ColorBuffer.fromUrl(fileOrUrl)
    } catch (e: MalformedURLException) {
        ColorBuffer.fromFile(fileOrUrl)
    }
}

/**
 * loads an image from [file]
 */
fun loadImage(file: File): ColorBuffer {
    return ColorBuffer.fromFile(file)
}

/**
 * loads an image from an [url]
 */
fun loadImage(url: URL): ColorBuffer {
    return ColorBuffer.fromUrl(url.toExternalForm())
}

