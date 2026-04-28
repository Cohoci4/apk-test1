package com.foodenhancer.core_ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Delegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageSegmenter(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: Delegate? = null
    private val inputSize = 256
    private val lock = Any()

    fun initialize() {
        synchronized(lock) {
            if (interpreter != null) return

            val model = loadModel()
            val options = Interpreter.Options()

            try {
                val gpuDelegateClass = Class.forName("org.tensorflow.lite.gpu.GpuDelegate")
                val delegate = gpuDelegateClass.getDeclaredConstructor().newInstance() as Delegate
                gpuDelegate = delegate
                options.addDelegate(delegate)
            } catch (_: Exception) {
                options.setNumThreads(4)
            }

            interpreter = try {
                Interpreter(model, options)
            } catch (e: Exception) {
                gpuDelegate?.close()
                gpuDelegate = null
                val cpuOptions = Interpreter.Options().apply { setNumThreads(4) }
                Interpreter(model, cpuOptions)
            }
        }
    }

    fun segment(bitmap: Bitmap): Bitmap {
        synchronized(lock) {
            val interp = interpreter
            if (interp == null) {
                return createFallbackMask(bitmap.width, bitmap.height)
            }

            return try {
                val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
                val inputBuffer = bitmapToByteBuffer(resized)
                val outputBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * 4)
                outputBuffer.order(ByteOrder.nativeOrder())

                interp.run(inputBuffer, outputBuffer)

                outputBuffer.rewind()
                val mask = byteBufferToMaskBitmap(outputBuffer, inputSize, inputSize)
                Bitmap.createScaledBitmap(mask, bitmap.width, bitmap.height, true)
            } catch (e: Exception) {
                createFallbackMask(bitmap.width, bitmap.height)
            }
        }
    }

    private fun createFallbackMask(width: Int, height: Int): Bitmap {
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val centerX = width / 2
        val centerY = height / 2
        val radius = minOf(width, height) * 0.4f

        for (y in 0 until height) {
            for (x in 0 until width) {
                val dx = (x - centerX).toFloat()
                val dy = (y - centerY).toFloat()
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                val alpha = if (dist < radius) {
                    255
                } else if (dist < radius * 1.3f) {
                    ((1f - (dist - radius) / (radius * 0.3f)) * 255).toInt().coerceIn(0, 255)
                } else {
                    0
                }
                mask.setPixel(x, y, Color.argb(255, alpha, alpha, alpha))
            }
        }
        return mask
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        for (pixel in pixels) {
            buffer.putFloat(Color.red(pixel) / 255.0f)
            buffer.putFloat(Color.green(pixel) / 255.0f)
            buffer.putFloat(Color.blue(pixel) / 255.0f)
        }
        buffer.rewind()
        return buffer
    }

    private fun byteBufferToMaskBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap {
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = (buffer.float * 255).toInt().coerceIn(0, 255)
                mask.setPixel(x, y, Color.argb(255, value, value, value))
            }
        }
        return mask
    }

    private fun loadModel(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun close() {
        synchronized(lock) {
            interpreter?.close()
            interpreter = null
            gpuDelegate?.close()
            gpuDelegate = null
        }
    }
}
