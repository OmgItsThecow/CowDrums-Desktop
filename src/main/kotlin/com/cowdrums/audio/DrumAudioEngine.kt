package com.cowdrums.audio

import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.ChannelMix
import com.cowdrums.model.DrumInstrument
import com.cowdrums.model.DrumPreset
import com.cowdrums.model.PatternSlot
import com.cowdrums.model.SongSection
import com.cowdrums.model.StepPattern
import com.cowdrums.model.StepVelocity
import com.cowdrums.model.TimeSignature
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Sample-accurate low-latency stereo PCM drum + bass mixer & step sequencer for Windows & Linux Desktop.
 * Uses standard JVM javax.sound.sampled.SourceDataLine (44.1 kHz, 16-bit signed little-endian stereo).
 */
class DrumAudioEngine(
    private val onStepChanged: (step: Int, bar: Int, activeSlot: PatternSlot, fillActive: Boolean, songSectionIdx: Int) -> Unit
) {
    companion object {
        const val SAMPLE_RATE = BuiltInKitSynthesizer.SAMPLE_RATE
        private const val BUFFER_FRAMES = 512
        private const val MAX_VOICES = 32
        private const val BASS_CHOKE_GROUP = 99
    }

    private data class Voice(
        var active: Boolean = false,
        var samples: FloatArray = FloatArray(0),
        var position: Double = 0.0,
        var pitchRate: Double = 1.0,
        var gainL: Float = 0f,
        var gainR: Float = 0f,
        var chokeGroup: Int = 0,
        var choking: Boolean = false,
        var chokeGain: Float = 1f
    )

    private data class PadTrigger(
        val instrument: DrumInstrument,
        val velocity: Float
    )

    private data class BassTrigger(
        val midiNote: Int,
        val velocity: Float
    )

    data class SequencerSnapshot(
        val preset: DrumPreset,
        val baseSlot: PatternSlot,
        val bpm: Int,
        val swing: Float,
        val metronomeEnabled: Boolean,
        val autoFillEvery4Bars: Boolean,
        val songMode: Boolean,
        val songSections: List<SongSection>,
        val songPresetsById: Map<String, DrumPreset>
    )

    private val running = AtomicBoolean(false)
    private val playing = AtomicBoolean(false)
    private val fillRequested = AtomicBoolean(false)
    private val restartRequested = AtomicBoolean(false)

    private val kitSamples = AtomicReference<Map<DrumInstrument, FloatArray>>(
        BuiltInKitSynthesizer.renderKit(BuiltInKit.ACOUSTIC)
    )
    private val padChokeGroups = AtomicReference<Map<DrumInstrument, Int>>(
        DrumInstrument.ALL.associateWith { it.chokeGroup }
    )
    private val bassSamples = AtomicReference<Map<Int, FloatArray>>(emptyMap())
    @Volatile
    private var bassVolume: Float = 0.80f

    private val mixerState = AtomicReference<Map<DrumInstrument, ChannelMix>>(
        DrumInstrument.ALL.associateWith { ChannelMix() }
    )
    private val sequencerState = AtomicReference<SequencerSnapshot?>(null)

    private val metroAccent = BuiltInKitSynthesizer.renderMetronomeClick(accent = true)
    private val metroNormal = BuiltInKitSynthesizer.renderMetronomeClick(accent = false)

    private val padQueue = ConcurrentLinkedQueue<PadTrigger>()
    private val bassQueue = ConcurrentLinkedQueue<BassTrigger>()
    private val voices = Array(MAX_VOICES) { Voice() }
    private var voiceCursor = 0

    private var audioThread: Thread? = null
    private val currentStepAtomic = AtomicInteger(-1)
    private var humanizeSeed: Int = 0x5A3C91E7

    fun startEngine() {
        if (running.getAndSet(true)) return
        audioThread = Thread(::audioLoop, "CowDrums-Desktop-Audio").apply {
            priority = Thread.MAX_PRIORITY
            isDaemon = true
            start()
        }
    }

    fun stopEngine() {
        running.set(false)
        playing.set(false)
        audioThread?.join(500)
        audioThread = null
    }

    fun setKitSamples(samples: Map<DrumInstrument, FloatArray>) {
        kitSamples.set(samples)
    }

    fun setSinglePadSample(instrument: DrumInstrument, sample: FloatArray) {
        val updated = HashMap(kitSamples.get())
        updated[instrument] = sample
        kitSamples.set(updated)
    }

    fun setPadChokeGroups(groups: Map<DrumInstrument, Int>) {
        padChokeGroups.set(groups)
    }

    fun currentKitSamples(): Map<DrumInstrument, FloatArray> = kitSamples.get()

    fun setBassSamples(samples: Map<Int, FloatArray>) {
        bassSamples.set(samples)
    }

    fun setBassVolume(vol: Float) {
        bassVolume = vol.coerceIn(0f, 1.25f)
    }

    fun triggerBassNote(midiNote: Int, velocity: Float = 0.9f) {
        bassQueue.add(BassTrigger(midiNote, velocity.coerceIn(0.05f, 1.0f)))
    }

    fun setMixerState(mix: Map<DrumInstrument, ChannelMix>) {
        mixerState.set(mix)
    }

    fun updateSequencerState(snapshot: SequencerSnapshot) {
        sequencerState.set(snapshot)
    }

    fun setPlaying(play: Boolean, restart: Boolean = false) {
        if (restart) restartRequested.set(true)
        playing.set(play)
        if (!play) {
            fillRequested.set(false)
            currentStepAtomic.set(-1)
            val snap = sequencerState.get()
            onStepChanged(-1, 0, snap?.baseSlot ?: PatternSlot.MAIN_A, false, 0)
        }
    }

    fun requestFill() {
        if (playing.get()) fillRequested.set(true)
    }

    fun triggerPad(instrument: DrumInstrument, velocity: Float = 0.9f) {
        padQueue.add(PadTrigger(instrument, velocity.coerceIn(0.05f, 1.0f)))
    }

    fun currentStep(): Int = currentStepAtomic.get()

    private fun audioLoop() {
        val format = AudioFormat(
            SAMPLE_RATE.toFloat(),
            16,
            2,
            true,
            false
        )
        val line: SourceDataLine = runCatching {
            AudioSystem.getSourceDataLine(format).apply {
                open(format, BUFFER_FRAMES * 4 * 4)
                start()
            }
        }.getOrNull() ?: return

        val byteBuffer = ByteArray(BUFFER_FRAMES * 4)

        var stepIndex = 0
        var barCounter = 0
        var songSectionIndex = 0
        var songBarInSection = 0
        var framesUntilNextStep = 0.0
        var wasPlaying = false
        var activeFillThisBar = false

        try {
            while (running.get()) {
                while (true) {
                    val hit = padQueue.poll() ?: break
                    spawnDrumVoice(hit.instrument, hit.velocity)
                }
                while (true) {
                    val bass = bassQueue.poll() ?: break
                    spawnBassVoice(bass.midiNote, bass.velocity)
                }

                val isPlay = playing.get()
                val snap = sequencerState.get()

                if (isPlay && (!wasPlaying || restartRequested.getAndSet(false))) {
                    stepIndex = 0
                    barCounter = 0
                    songSectionIndex = 0
                    songBarInSection = 0
                    framesUntilNextStep = 0.0
                    activeFillThisBar = false
                }
                wasPlaying = isPlay

                for (frame in 0 until BUFFER_FRAMES) {
                    if (isPlay && snap != null) {
                        if (framesUntilNextStep <= 0.0) {
                            val resolved = resolvePlaybackTarget(
                                snap = snap,
                                songSectionIndex = songSectionIndex,
                                songBarInSection = songBarInSection,
                                barCounter = barCounter,
                                stepIndex = stepIndex,
                                activeFillThisBar = activeFillThisBar
                            )
                            val activePreset = resolved.preset
                            val activeSlot = resolved.slot
                            activeFillThisBar = resolved.fillActive

                            val pattern = activePreset.patternFor(activeSlot)
                            val stepCount = pattern.stepCount.coerceAtLeast(1)
                            val safeStep = stepIndex % stepCount

                            triggerStep(pattern, safeStep)

                            if (snap.metronomeEnabled) {
                                val spb = activePreset.timeSignature.stepsPerBeat.coerceAtLeast(1)
                                if (safeStep % spb == 0) {
                                    spawnRawVoice(
                                        if (safeStep == 0) metroAccent else metroNormal,
                                        gain = if (safeStep == 0) 0.55f else 0.35f
                                    )
                                }
                            }

                            currentStepAtomic.set(safeStep)
                            onStepChanged(
                                safeStep,
                                barCounter,
                                activeSlot,
                                activeFillThisBar,
                                songSectionIndex
                            )

                            val baseStepFrames = stepDurationFrames(snap.bpm, activePreset.timeSignature)
                            val swingOffset = (snap.swing.coerceIn(0f, 0.70f) * 0.45) * baseStepFrames
                            val stepDur = if (safeStep % 2 == 0) {
                                baseStepFrames + swingOffset
                            } else {
                                (baseStepFrames - swingOffset).coerceAtLeast(baseStepFrames * 0.25)
                            }
                            framesUntilNextStep += stepDur

                            stepIndex = (safeStep + 1) % stepCount
                            if (stepIndex == 0) {
                                barCounter++
                                activeFillThisBar = false
                                fillRequested.set(false)

                                if (snap.songMode && snap.songSections.isNotEmpty()) {
                                    val sec = snap.songSections[songSectionIndex % snap.songSections.size]
                                    songBarInSection++
                                    if (songBarInSection >= sec.bars.coerceAtLeast(1)) {
                                        songBarInSection = 0
                                        songSectionIndex = (songSectionIndex + 1) % snap.songSections.size
                                    }
                                }
                            }
                        }
                        framesUntilNextStep -= 1.0
                    }

                    var outL = 0f
                    var outR = 0f
                    for (v in voices) {
                        if (!v.active) continue
                        val posInt = v.position.toInt()
                        if (posInt >= v.samples.size - 1) {
                            v.active = false
                            continue
                        }
                        val frac = (v.position - posInt).toFloat()
                        val s0 = v.samples[posInt]
                        val s1 = v.samples[posInt + 1]
                        var sample = s0 + (s1 - s0) * frac

                        if (v.choking) {
                            v.chokeGain -= 0.005f
                            if (v.chokeGain <= 0f) {
                                v.active = false
                                continue
                            }
                            sample *= v.chokeGain
                        }

                        outL += sample * v.gainL
                        outR += sample * v.gainR
                        v.position += v.pitchRate
                    }

                    val l = (tanh(outL * 0.85) * 32767.0).toInt().coerceIn(-32768, 32767)
                    val r = (tanh(outR * 0.85) * 32767.0).toInt().coerceIn(-32768, 32767)
                    val bo = frame * 4
                    byteBuffer[bo] = (l and 0xFF).toByte()
                    byteBuffer[bo + 1] = ((l shr 8) and 0xFF).toByte()
                    byteBuffer[bo + 2] = (r and 0xFF).toByte()
                    byteBuffer[bo + 3] = ((r shr 8) and 0xFF).toByte()
                }

                line.write(byteBuffer, 0, byteBuffer.size)
            }
        } finally {
            runCatching {
                line.stop()
                line.close()
            }
        }
    }

    private data class ResolvedTarget(
        val preset: DrumPreset,
        val slot: PatternSlot,
        val fillActive: Boolean
    )

    private fun resolvePlaybackTarget(
        snap: SequencerSnapshot,
        songSectionIndex: Int,
        songBarInSection: Int,
        barCounter: Int,
        stepIndex: Int,
        activeFillThisBar: Boolean
    ): ResolvedTarget {
        if (snap.songMode && snap.songSections.isNotEmpty()) {
            val section = snap.songSections[songSectionIndex % snap.songSections.size]
            val preset = snap.songPresetsById[section.presetId] ?: snap.preset
            val isLastBar = songBarInSection == (section.bars.coerceAtLeast(1) - 1)
            val useFill = (section.playFillOnLastBar && isLastBar) || fillRequested.get() || activeFillThisBar
            return ResolvedTarget(
                preset = preset,
                slot = if (useFill) PatternSlot.FILL else section.slot,
                fillActive = useFill
            )
        }

        val autoFill = snap.autoFillEvery4Bars && ((barCounter + 1) % 4 == 0)
        val useFill = activeFillThisBar || fillRequested.get() || (autoFill && stepIndex == 0)
        return ResolvedTarget(
            preset = snap.preset,
            slot = if (useFill) PatternSlot.FILL else snap.baseSlot,
            fillActive = useFill
        )
    }

    private fun stepDurationFrames(bpm: Int, timeSignature: TimeSignature): Double {
        val safeBpm = bpm.coerceIn(40, 240).toDouble()
        val sixteenthSeconds = (60.0 / safeBpm) / 4.0
        return (sixteenthSeconds * SAMPLE_RATE).coerceAtLeast(256.0)
    }

    private fun nextHumanizeFactor(): Float {
        var x = humanizeSeed
        x = x xor (x shl 13)
        x = x xor (x ushr 17)
        x = x xor (x shl 5)
        humanizeSeed = x
        val norm = ((x and 0xFFFF) / 65535f) * 2f - 1f
        return 1f + norm * 0.055f
    }

    private fun triggerStep(pattern: StepPattern, step: Int) {
        for (inst in DrumInstrument.ALL) {
            val velLevel = pattern.velocityAt(inst, step)
            if (velLevel > 0) {
                val baseGain = StepVelocity.toGain(velLevel)
                spawnDrumVoice(inst, (baseGain * nextHumanizeFactor()).coerceIn(0.08f, 1.0f))
            }
        }
        val bassMidi = pattern.bassNotes[step]
        if (bassMidi != null) {
            spawnBassVoice(bassMidi, 0.90f)
        }
    }

    private fun spawnBassVoice(midiNote: Int, velocity: Float) {
        val samples = bassSamples.get()[midiNote] ?: return
        if (samples.size < 2) return
        for (v in voices) {
            if (v.active && v.chokeGroup == BASS_CHOKE_GROUP) {
                v.choking = true
            }
        }
        val gain = (velocity * bassVolume).coerceIn(0f, 1.4f)
        allocateVoice(
            samples = samples,
            pitchRate = 1.0,
            gainL = gain,
            gainR = gain,
            chokeGroup = BASS_CHOKE_GROUP
        )
    }

    private fun spawnDrumVoice(instrument: DrumInstrument, velocity: Float) {
        val mixMap = mixerState.get()
        val anySolo = mixMap.values.any { it.solo }
        val ch = mixMap[instrument] ?: ChannelMix()
        if (ch.muted || (anySolo && !ch.solo)) return

        val samples = kitSamples.get()[instrument] ?: return
        if (samples.size < 2) return

        val activeChokeGroup = padChokeGroups.get()[instrument] ?: instrument.chokeGroup
        if (activeChokeGroup != 0) {
            for (v in voices) {
                if (v.active && v.chokeGroup == activeChokeGroup) {
                    v.choking = true
                }
            }
        }

        val gain = (velocity * ch.volume).coerceIn(0f, 1.5f)
        val panNorm = ((ch.pan.coerceIn(-1f, 1f) + 1f) * 0.5f) * (Math.PI / 2.0)
        val gainL = (gain * cos(panNorm)).toFloat()
        val gainR = (gain * sin(panNorm)).toFloat()
        val pitchRate = 2.0.pow(ch.pitchSemitones.coerceIn(-12f, 12f) / 12.0)

        allocateVoice(
            samples = samples,
            pitchRate = pitchRate,
            gainL = gainL,
            gainR = gainR,
            chokeGroup = activeChokeGroup
        )
    }

    private fun spawnRawVoice(samples: FloatArray, gain: Float) {
        allocateVoice(
            samples = samples,
            pitchRate = 1.0,
            gainL = gain,
            gainR = gain,
            chokeGroup = 0
        )
    }

    private fun allocateVoice(
        samples: FloatArray,
        pitchRate: Double,
        gainL: Float,
        gainR: Float,
        chokeGroup: Int
    ) {
        var target: Voice? = null
        for (i in 0 until MAX_VOICES) {
            val idx = (voiceCursor + i) % MAX_VOICES
            if (!voices[idx].active) {
                target = voices[idx]
                voiceCursor = (idx + 1) % MAX_VOICES
                break
            }
        }
        if (target == null) {
            target = voices[voiceCursor]
            voiceCursor = (voiceCursor + 1) % MAX_VOICES
        }
        target.active = true
        target.samples = samples
        target.position = 0.0
        target.pitchRate = pitchRate
        target.gainL = gainL
        target.gainR = gainR
        target.chokeGroup = chokeGroup
        target.choking = false
        target.chokeGain = 1f
    }
}
