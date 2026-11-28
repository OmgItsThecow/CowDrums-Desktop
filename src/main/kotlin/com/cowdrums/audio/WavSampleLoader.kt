package com.cowdrums.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.min

/**
 * Pure-Kotlin WAV audio sample loader supporting 8/16/24/32-bit PCM and 32-bit IEEE float WAVs
 * (mono or stereo), linearly resampled to 44.1 kHz for single-pad custom sample loading.
 */
object WavSampleLoader {
    private const val MAX_SECONDS = 4.0

    fun decodeWavToMono44k(bytes: ByteArray, targetSampleRate: Int = BuiltInKitSynthesizer.SAMPLE_RATE): FloatArray? {
        if (bytes.size < 44) return null
        val riff = String(bytes, 0, 4, Charsets.US_ASCII)
        val wave = String(bytes, 8, 4, Charsets.US_ASCII)
        if (riff != "RIFF" || wave != "WAVE") return null

        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var pos = 12
        var audioFormat = 0
        var numChannels = 0
        var sampleRate = 0
        var bitsPerSample = 0
        var dataOffset = -1
        var dataSize = 0

        while (pos + 8 <= bytes.size) {
            val chunkId = String(bytes, pos, 4, Charsets.US_ASCII)
            val chunkSize = bb.getInt(pos + 4)
            if (chunkSize < 0 || pos + 8 + chunkSize > bytes.size) break
            when (chunkId) {
                "fmt " -> if (chunkSize >= 16) {
                    audioFormat = bb.getShort(pos + 8).toInt() and 0xFFFF
                    numChannels = bb.getShort(pos + 10).toInt() and 0xFFFF
                    sampleRate = bb.getInt(pos + 12)
                    bitsPerSample = bb.getShort(pos + 22).toInt() and 0xFFFF
                }
                "data" -> {
                    dataOffset = pos + 8
                    dataSize = chunkSize
                }
            }
            pos += 8 + chunkSize + (chunkSize and 1)
        }

        if (dataOffset < 0 || numChannels <= 0 || sampleRate <= 0 || bitsPerSample <= 0) return null
        val bytesPerSample = bitsPerSample / 8
        val frameSize = bytesPerSample * numChannels
        if (frameSize <= 0) return null

        val maxInputFrames = (sampleRate * MAX_SECONDS).toInt()
        val totalFrames = min(dataSize / frameSize, maxInputFrames)
        if (totalFrames < 16) return null

        val mono = FloatArray(totalFrames)
        var p = dataOffset
        for (f in 0 until totalFrames) {
            var sum = 0f
            for (ch in 0 until numChannels) {
                val sample = when {
                    audioFormat == 3 && bitsPerSample == 32 -> bb.getFloat(p)
                    audioFormat == 1 && bitsPerSample == 8 -> ((bytes[p].toInt() and 0xFF) - 128) / 128f
                    audioFormat == 1 && bitsPerSample == 16 -> bb.getShort(p) / 32768f
                    audioFormat == 1 && bitsPerSample == 24 -> {
                        val b0 = bytes[p].toInt() and 0xFF
                        val b1 = bytes[p + 1].toInt() and 0xFF
                        val b2 = bytes[p + 2].toInt()
                        val v = (b2 shl 16) or (b1 shl 8) or b0
                        v / 8388608f
                    }
                    audioFormat == 1 && bitsPerSample == 32 -> bb.getInt(p) / 2147483648f
                    else -> return null
                }
                sum += sample
                p += bytesPerSample
            }
            mono[f] = sum / numChannels
        }

        val resampled = if (sampleRate == targetSampleRate) {
            mono
        } else {
            val ratio = sampleRate.toDouble() / targetSampleRate.toDouble()
            val outLen = (mono.size / ratio).toInt().coerceAtLeast(16)
            FloatArray(outLen) { i ->
                val srcPos = i * ratio
                val idx = srcPos.toInt().coerceIn(0, mono.size - 1)
                val next = (idx + 1).coerceAtMost(mono.size - 1)
                val frac = (srcPos - idx).toFloat()
                mono[idx] * (1f - frac) + mono[next] * frac
            }
        }

        var peak = 0f
        for (v in resampled) {
            val a = abs(v)
            if (a > peak) peak = a
        }
        if (peak < 1e-5f) return null
        val gain = 0.92f / peak
        for (i in resampled.indices) {
            resampled[i] = (resampled[i] * gain).coerceIn(-1f, 1f)
        }
        return resampled
    }
}
