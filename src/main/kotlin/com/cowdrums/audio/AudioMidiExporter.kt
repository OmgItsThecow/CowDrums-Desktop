package com.cowdrums.audio

import com.cowdrums.model.ChannelMix
import com.cowdrums.model.DrumInstrument
import com.cowdrums.model.DrumPreset
import com.cowdrums.model.PatternSlot
import com.cowdrums.model.StepVelocity
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Offline WAV (44.1 kHz 16-bit stereo PCM) and Standard MIDI File (Format 0, GM Channel 10) exporter.
 */
object AudioMidiExporter {

    private const val SAMPLE_RATE = BuiltInKitSynthesizer.SAMPLE_RATE
    private const val TPQN = 480 // Ticks per quarter note

    fun exportPatternToWav(
        output: OutputStream,
        preset: DrumPreset,
        slot: PatternSlot,
        bpm: Int,
        swing: Float,
        bars: Int = 2,
        kitSamples: Map<DrumInstrument, FloatArray>,
        mixer: Map<DrumInstrument, ChannelMix>
    ) {
        val pattern = preset.patternFor(slot)
        val stepCount = pattern.stepCount.coerceAtLeast(1)
        val safeBpm = bpm.coerceIn(40, 240).toDouble()
        val baseStepFrames = (60.0 / safeBpm / 4.0) * SAMPLE_RATE
        val swingOffset = (swing.coerceIn(0f, 0.70f) * 0.45) * baseStepFrames

        // Compute exact frame offset of each step across requested bars
        val totalSteps = stepCount * bars.coerceAtLeast(1)
        val stepStartFrames = IntArray(totalSteps + 1)
        var cursor = 0.0
        for (i in 0 until totalSteps) {
            stepStartFrames[i] = cursor.roundToInt()
            val stepInBar = i % stepCount
            val dur = if (stepInBar % 2 == 0) {
                baseStepFrames + swingOffset
            } else {
                (baseStepFrames - swingOffset).coerceAtLeast(baseStepFrames * 0.25)
            }
            cursor += dur
        }
        val tailFrames = (SAMPLE_RATE * 1.2).toInt() // Let last crash/cymbal ring out cleanly
        val totalFrames = cursor.roundToInt() + tailFrames
        stepStartFrames[totalSteps] = cursor.roundToInt()

        val mixL = FloatArray(totalFrames)
        val mixR = FloatArray(totalFrames)
        val anySolo = mixer.values.any { it.solo }

        // Precompute step events to handle hi-hat choke cleanly
        val chokeTimes = ArrayList<Int>()
        for (i in 0 until totalSteps) {
            val step = i % stepCount
            if (pattern.velocityAt(DrumInstrument.CHH, step) > 0) {
                chokeTimes.add(stepStartFrames[i])
            }
        }

        for (i in 0 until totalSteps) {
            val step = i % stepCount
            val startFrame = stepStartFrames[i]
            for (inst in DrumInstrument.ALL) {
                val velLevel = pattern.velocityAt(inst, step)
                if (velLevel <= 0) continue
                val ch = mixer[inst] ?: ChannelMix()
                if (ch.muted || (anySolo && !ch.solo)) continue
                val samples = kitSamples[inst] ?: continue
                if (samples.size < 2) continue

                val gain = (StepVelocity.toGain(velLevel) * ch.volume).coerceIn(0f, 1.5f)
                val panNorm = ((ch.pan.coerceIn(-1f, 1f) + 1f) * 0.5f) * (Math.PI / 2.0)
                val gainL = (gain * cos(panNorm)).toFloat()
                val gainR = (gain * sin(panNorm)).toFloat()
                val pitchRate = 2.0.pow(ch.pitchSemitones.coerceIn(-12f, 12f) / 12.0)

                // Find next choke frame if Open HH
                val nextChokeFrame = if (inst == DrumInstrument.OHH) {
                    chokeTimes.firstOrNull { it > startFrame } ?: Int.MAX_VALUE
                } else {
                    Int.MAX_VALUE
                }

                var pos = 0.0
                var frame = startFrame
                while (frame < totalFrames) {
                    val idx = pos.toInt()
                    if (idx + 1 >= samples.size) break
                    val frac = (pos - idx).toFloat()
                    var s = samples[idx] + (samples[idx + 1] - samples[idx]) * frac

                    if (frame >= nextChokeFrame) {
                        val fade = 1f - ((frame - nextChokeFrame) / 220f)
                        if (fade <= 0f) break
                        s *= fade
                    }

                    mixL[frame] += s * gainL
                    mixR[frame] += s * gainR
                    pos += pitchRate
                    frame++
                }
            }
        }

        // Write 44-byte RIFF WAV header + 16-bit stereo PCM
        val dataBytes = totalFrames * 4
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(36 + dataBytes)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16) // PCM fmt chunk size
            putShort(1) // PCM format
            putShort(2) // Stereo
            putInt(SAMPLE_RATE)
            putInt(SAMPLE_RATE * 4) // Byte rate
            putShort(4) // Block align
            putShort(16) // Bits per sample
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataBytes)
        }.array()
        output.write(header)

        val chunk = ByteArray(4096)
        var chunkPos = 0
        for (f in 0 until totalFrames) {
            val l = (tanh(mixL[f] * 0.85) * 32767.0).toInt().coerceIn(-32768, 32767)
            val r = (tanh(mixR[f] * 0.85) * 32767.0).toInt().coerceIn(-32768, 32767)
            chunk[chunkPos++] = (l and 0xFF).toByte()
            chunk[chunkPos++] = ((l shr 8) and 0xFF).toByte()
            chunk[chunkPos++] = (r and 0xFF).toByte()
            chunk[chunkPos++] = ((r shr 8) and 0xFF).toByte()
            if (chunkPos == chunk.size) {
                output.write(chunk)
                chunkPos = 0
            }
        }
        if (chunkPos > 0) {
            output.write(chunk, 0, chunkPos)
        }
        output.flush()
    }

    fun exportPatternToMidi(
        output: OutputStream,
        preset: DrumPreset,
        slot: PatternSlot,
        bpm: Int,
        swing: Float,
        bars: Int = 2
    ) {
        val pattern = preset.patternFor(slot)
        val stepCount = pattern.stepCount.coerceAtLeast(1)
        val baseStepTicks = TPQN / 4 // 120 ticks per 16th note
        val swingTicks = (swing.coerceIn(0f, 0.70f) * 0.45 * baseStepTicks).roundToInt()

        data class MidiEvent(val tick: Int, val bytes: ByteArray)

        val events = ArrayList<MidiEvent>()
        // Tempo meta-event (microseconds per quarter note)
        val usPerQuarter = (60_000_000.0 / bpm.coerceIn(40, 240)).roundToInt()
        events.add(
            MidiEvent(
                0,
                byteArrayOf(
                    0xFF.toByte(), 0x51, 0x03,
                    ((usPerQuarter shr 16) and 0xFF).toByte(),
                    ((usPerQuarter shr 8) and 0xFF).toByte(),
                    (usPerQuarter and 0xFF).toByte()
                )
            )
        )

        var tickCursor = 0
        val totalSteps = stepCount * bars.coerceAtLeast(1)
        for (i in 0 until totalSteps) {
            val step = i % stepCount
            for (inst in DrumInstrument.ALL) {
                val velLevel = pattern.velocityAt(inst, step)
                if (velLevel <= 0) continue
                val midiVel = when (velLevel) {
                    StepVelocity.SOFT -> 52
                    StepVelocity.MEDIUM -> 94
                    else -> 124
                }
                val note = inst.gmNote.toByte()
                // Channel 10 (0-indexed channel 9): 0x99 Note On, 0x89 Note Off
                events.add(MidiEvent(tickCursor, byteArrayOf(0x99.toByte(), note, midiVel.toByte())))
                events.add(MidiEvent(tickCursor + 40, byteArrayOf(0x89.toByte(), note, 0)))
            }
            val dur = if (step % 2 == 0) baseStepTicks + swingTicks else (baseStepTicks - swingTicks).coerceAtLeast(30)
            tickCursor += dur
        }

        events.sortBy { it.tick }

        val trackBuf = ByteArrayOutputStream()
        var lastTick = 0
        for (ev in events) {
            val delta = (ev.tick - lastTick).coerceAtLeast(0)
            writeVarLen(trackBuf, delta)
            trackBuf.write(ev.bytes)
            lastTick = ev.tick
        }
        // End of Track meta-event
        writeVarLen(trackBuf, 0)
        trackBuf.write(byteArrayOf(0xFF.toByte(), 0x2F, 0x00))

        val trackBytes = trackBuf.toByteArray()

        // MThd Header (Format 0, 1 track, TPQN)
        val header = ByteBuffer.allocate(14).order(ByteOrder.BIG_ENDIAN).apply {
            put("MThd".toByteArray(Charsets.US_ASCII))
            putInt(6)
            putShort(0) // Format 0
            putShort(1) // 1 track
            putShort(TPQN.toShort())
        }.array()

        // MTrk Header
        val trackHeader = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).apply {
            put("MTrk".toByteArray(Charsets.US_ASCII))
            putInt(trackBytes.size)
        }.array()

        output.write(header)
        output.write(trackHeader)
        output.write(trackBytes)
        output.flush()
    }

    private fun writeVarLen(out: ByteArrayOutputStream, value: Int) {
        var v = value
        var buffer = v and 0x7F
        while (true) {
            v = v ushr 7
            if (v <= 0) break
            buffer = (buffer shl 8) or ((v and 0x7F) or 0x80)
        }
        while (true) {
            out.write(buffer and 0xFF)
            if ((buffer and 0x80) != 0) {
                buffer = buffer ushr 8
            } else {
                break
            }
        }
    }
}
