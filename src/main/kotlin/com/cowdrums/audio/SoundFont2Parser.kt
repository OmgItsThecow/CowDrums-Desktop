package com.cowdrums.audio

import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.DrumInstrument
import com.cowdrums.model.SoundFontPresetInfo
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

/**
 * Pure-Kotlin SoundFont 2 (.sf2) RIFF binary parser with full support for:
 * - Multi-zone layering (e.g. close + room snare, open hat + attack)
 * - Velocity range filtering (gen 44)
 * - Coarse/fine tuning (gen 51/52), overriding root key (gen 58), scale tuning (gen 56)
 * - Initial attenuation (gen 48, centibels)
 * - Global preset & instrument zones
 */
object SoundFont2Parser {

    private const val GEN_START_ADDR_OFFSET = 0
    private const val GEN_END_ADDR_OFFSET = 1
    private const val GEN_START_ADDR_COARSE_OFFSET = 4
    private const val GEN_END_ADDR_COARSE_OFFSET = 12
    private const val GEN_DECAY_VOL_ENV = 36
    private const val GEN_RELEASE_VOL_ENV = 38
    private const val GEN_INSTRUMENT = 41
    private const val GEN_KEY_RANGE = 43
    private const val GEN_VEL_RANGE = 44
    private const val GEN_INITIAL_ATTENUATION = 48
    private const val GEN_COARSE_TUNE = 51
    private const val GEN_FINE_TUNE = 52
    private const val GEN_SAMPLE_ID = 53
    private const val GEN_SAMPLE_MODES = 54
    private const val GEN_SCALE_TUNING = 56
    private const val GEN_OVERRIDING_ROOT_KEY = 58

    private const val REFERENCE_VELOCITY = 118

    private data class Phdr(
        val name: String,
        val preset: Int,
        val bank: Int,
        val bagNdx: Int
    )

    private data class Bag(val genNdx: Int)
    private data class Gen(val oper: Int, val amount: Short)
    private data class Inst(val name: String, val bagNdx: Int)

    private data class Shdr(
        val name: String,
        val start: Int,
        val end: Int,
        val startLoop: Int,
        val endLoop: Int,
        val sampleRate: Int,
        val originalPitch: Int,
        val pitchCorrection: Int,
        val sampleType: Int
    )

    private data class ParsedSf2(
        val smplOffset: Int,
        val smplLength: Int,
        val phdrs: List<Phdr>,
        val pbags: List<Bag>,
        val pgens: List<Gen>,
        val insts: List<Inst>,
        val ibags: List<Bag>,
        val igens: List<Gen>,
        val shdrs: List<Shdr>,
        val bytes: ByteArray
    )

    private data class SampleZone(
        val shdr: Shdr,
        val rootKeyOverride: Int?,
        val coarseTune: Int,
        val fineTune: Int,
        val scaleTuning: Int,
        val attenuationCb: Int,
        val sampleModes: Int,
        val decaySeconds: Double,
        val startOffset: Int,
        val endOffset: Int
    )

    fun listPresets(file: File): List<SoundFontPresetInfo> {
        if (!file.exists() || !file.canRead()) return emptyList()
        val bytes = runCatching { file.readBytes() }.getOrNull() ?: return emptyList()
        return listPresets(bytes)
    }

    fun listPresets(bytes: ByteArray): List<SoundFontPresetInfo> {
        val parsed = parse(bytes) ?: return emptyList()
        val list = parsed.phdrs.dropLast(1).map {
            SoundFontPresetInfo(
                bank = it.bank,
                preset = it.preset,
                name = it.name.ifBlank { "Bank ${it.bank} Preset ${it.preset}" }
            )
        }
        return list.sortedWith(
            compareByDescending<SoundFontPresetInfo> { it.bank == 128 }
                .thenBy { it.bank }
                .thenBy { it.preset }
        )
    }

    fun loadKitSamples(
        file: File,
        bank: Int,
        preset: Int,
        targetSampleRate: Int = BuiltInKitSynthesizer.SAMPLE_RATE
    ): Map<DrumInstrument, FloatArray> {
        val bytes = if (file.exists() && file.canRead()) runCatching { file.readBytes() }.getOrNull() else null
        return if (bytes != null) {
            loadKitSamples(bytes, bank, preset, targetSampleRate)
        } else {
            BuiltInKitSynthesizer.renderKit(BuiltInKit.ACOUSTIC)
        }
    }

    fun loadKitSamples(
        bytes: ByteArray,
        bank: Int,
        preset: Int,
        targetSampleRate: Int = BuiltInKitSynthesizer.SAMPLE_RATE,
        applyLoFi: Boolean = false
    ): Map<DrumInstrument, FloatArray> {
        val fallback = BuiltInKitSynthesizer.renderKit(BuiltInKit.ACOUSTIC)
        val parsed = parse(bytes) ?: return fallback

        val phdrIndex = parsed.phdrs.dropLast(1).indexOfFirst { it.bank == bank && it.preset == preset }
        if (phdrIndex < 0) return fallback

        // Map MIDI key -> list of active SampleZones at REFERENCE_VELOCITY
        val keyToZones = HashMap<Int, MutableList<SampleZone>>()
        val pStartBag = parsed.phdrs[phdrIndex].bagNdx
        val pEndBag = parsed.phdrs[phdrIndex + 1].bagNdx

        var gPresetKeyLo = 0
        var gPresetKeyHi = 127
        var gPresetVelLo = 0
        var gPresetVelHi = 127
        var gPresetCoarse = 0
        var gPresetFine = 0
        var gPresetAtten = 0

        for (b in pStartBag until pEndBag) {
            if (b + 1 >= parsed.pbags.size) break
            val gStart = parsed.pbags[b].genNdx
            val gEnd = parsed.pbags[b + 1].genNdx

            var keyLo = gPresetKeyLo
            var keyHi = gPresetKeyHi
            var velLo = gPresetVelLo
            var velHi = gPresetVelHi
            var coarse = gPresetCoarse
            var fine = gPresetFine
            var atten = gPresetAtten
            var instIdx = -1

            for (g in gStart until gEnd) {
                if (g >= parsed.pgens.size) break
                val gen = parsed.pgens[g]
                when (gen.oper) {
                    GEN_KEY_RANGE -> {
                        val u = gen.amount.toInt() and 0xFFFF
                        keyLo = u and 0xFF
                        keyHi = (u shr 8) and 0xFF
                    }
                    GEN_VEL_RANGE -> {
                        val u = gen.amount.toInt() and 0xFFFF
                        velLo = u and 0xFF
                        velHi = (u shr 8) and 0xFF
                    }
                    GEN_COARSE_TUNE -> coarse += gen.amount.toInt()
                    GEN_FINE_TUNE -> fine += gen.amount.toInt()
                    GEN_INITIAL_ATTENUATION -> atten += gen.amount.toInt()
                    GEN_INSTRUMENT -> instIdx = gen.amount.toInt() and 0xFFFF
                }
            }

            if (instIdx < 0 && b == pStartBag) {
                gPresetKeyLo = keyLo
                gPresetKeyHi = keyHi
                gPresetVelLo = velLo
                gPresetVelHi = velHi
                gPresetCoarse = coarse
                gPresetFine = fine
                gPresetAtten = atten
                continue
            }

            if (REFERENCE_VELOCITY !in velLo..velHi) continue

            if (instIdx in 0 until (parsed.insts.size - 1)) {
                collectInstrumentZones(
                    parsed = parsed,
                    instIdx = instIdx,
                    presetKeyLo = keyLo,
                    presetKeyHi = keyHi,
                    presetCoarse = coarse,
                    presetFine = fine,
                    presetAtten = atten,
                    out = keyToZones
                )
            }
        }

        val result = LinkedHashMap<DrumInstrument, FloatArray>(DrumInstrument.ALL.size)
        for (inst in DrumInstrument.ALL) {
            val candidateNotes = listOf(inst.gmNote) + inst.fallbackGmNotes
            var loaded: FloatArray? = null
            for (note in candidateNotes) {
                val zones = keyToZones[note]
                if (!zones.isNullOrEmpty()) {
                    val mixed = renderLayeredNote(parsed, zones, note, targetSampleRate, targetPeakFor(inst))
                    if (mixed != null && mixed.isNotEmpty()) {
                        loaded = if (applyLoFi) applyWarmLoFi(mixed) else mixed
                        break
                    }
                }
            }
            result[inst] = loaded ?: fallback[inst]!!
        }
        return result
    }

    /**
     * Renders pitched melodic bass notes (e.g. MIDI 24..59, C1..B3) from an SF2 melodic preset (Bank 0).
     */
    fun loadBassNotes(
        bytes: ByteArray,
        bank: Int = 0,
        preset: Int = 32,
        midiRange: IntRange = 24..59,
        targetSampleRate: Int = BuiltInKitSynthesizer.SAMPLE_RATE
    ): Map<Int, FloatArray> {
        val parsed = parse(bytes) ?: return emptyMap()
        val phdrIndex = parsed.phdrs.dropLast(1).indexOfFirst { it.bank == bank && it.preset == preset }
        if (phdrIndex < 0) return emptyMap()

        val keyToZones = HashMap<Int, MutableList<SampleZone>>()
        val pStartBag = parsed.phdrs[phdrIndex].bagNdx
        val pEndBag = parsed.phdrs[phdrIndex + 1].bagNdx

        var gPresetKeyLo = 0
        var gPresetKeyHi = 127
        var gPresetVelLo = 0
        var gPresetVelHi = 127
        var gPresetCoarse = 0
        var gPresetFine = 0
        var gPresetAtten = 0

        for (b in pStartBag until pEndBag) {
            if (b + 1 >= parsed.pbags.size) break
            val gStart = parsed.pbags[b].genNdx
            val gEnd = parsed.pbags[b + 1].genNdx

            var keyLo = gPresetKeyLo
            var keyHi = gPresetKeyHi
            var velLo = gPresetVelLo
            var velHi = gPresetVelHi
            var coarse = gPresetCoarse
            var fine = gPresetFine
            var atten = gPresetAtten
            var instIdx = -1

            for (g in gStart until gEnd) {
                if (g >= parsed.pgens.size) break
                val gen = parsed.pgens[g]
                when (gen.oper) {
                    GEN_KEY_RANGE -> {
                        val u = gen.amount.toInt() and 0xFFFF
                        keyLo = u and 0xFF
                        keyHi = (u shr 8) and 0xFF
                    }
                    GEN_VEL_RANGE -> {
                        val u = gen.amount.toInt() and 0xFFFF
                        velLo = u and 0xFF
                        velHi = (u shr 8) and 0xFF
                    }
                    GEN_COARSE_TUNE -> coarse += gen.amount.toInt()
                    GEN_FINE_TUNE -> fine += gen.amount.toInt()
                    GEN_INITIAL_ATTENUATION -> atten += gen.amount.toInt()
                    GEN_INSTRUMENT -> instIdx = gen.amount.toInt() and 0xFFFF
                }
            }

            if (instIdx < 0 && b == pStartBag) {
                gPresetKeyLo = keyLo
                gPresetKeyHi = keyHi
                gPresetVelLo = velLo
                gPresetVelHi = velHi
                gPresetCoarse = coarse
                gPresetFine = fine
                gPresetAtten = atten
                continue
            }

            if (REFERENCE_VELOCITY !in velLo..velHi) continue

            if (instIdx in 0 until (parsed.insts.size - 1)) {
                collectInstrumentZones(
                    parsed = parsed,
                    instIdx = instIdx,
                    presetKeyLo = keyLo,
                    presetKeyHi = keyHi,
                    presetCoarse = coarse,
                    presetFine = fine,
                    presetAtten = atten,
                    out = keyToZones
                )
            }
        }

        val out = HashMap<Int, FloatArray>()
        for (midi in midiRange) {
            val zones = keyToZones[midi] ?: continue
            val rendered = renderLayeredNote(parsed, zones, midi, targetSampleRate, targetPeak = 0.85f)
            if (rendered != null && rendered.isNotEmpty()) {
                out[midi] = rendered
            }
        }
        return out
    }

    private fun targetPeakFor(inst: DrumInstrument): Float = when (inst) {
        DrumInstrument.KICK -> 0.96f
        DrumInstrument.SNARE -> 0.92f
        DrumInstrument.RIM -> 0.85f
        DrumInstrument.CLAP -> 0.88f
        DrumInstrument.CHH -> 0.72f
        DrumInstrument.OHH -> 0.76f
        DrumInstrument.LOW_TOM, DrumInstrument.MID_TOM, DrumInstrument.HIGH_TOM -> 0.88f
        DrumInstrument.CRASH -> 0.80f
        DrumInstrument.RIDE -> 0.74f
        DrumInstrument.PERC -> 0.78f
    }

    private fun collectInstrumentZones(
        parsed: ParsedSf2,
        instIdx: Int,
        presetKeyLo: Int,
        presetKeyHi: Int,
        presetCoarse: Int,
        presetFine: Int,
        presetAtten: Int,
        out: MutableMap<Int, MutableList<SampleZone>>
    ) {
        val iStartBag = parsed.insts[instIdx].bagNdx
        val iEndBag = parsed.insts[instIdx + 1].bagNdx

        var gKeyLo = 0
        var gKeyHi = 127
        var gVelLo = 0
        var gVelHi = 127
        var gRoot: Int? = null
        var gCoarse = 0
        var gFine = 0
        var gScale = 100
        var gAtten = 0
        var gModes = 0
        var gDecayTc = -12000

        for (b in iStartBag until iEndBag) {
            if (b + 1 >= parsed.ibags.size) break
            val gStart = parsed.ibags[b].genNdx
            val gEnd = parsed.ibags[b + 1].genNdx

            var keyLo = gKeyLo
            var keyHi = gKeyHi
            var velLo = gVelLo
            var velHi = gVelHi
            var rootOverride = gRoot
            var coarse = gCoarse
            var fine = gFine
            var scale = gScale
            var atten = gAtten
            var modes = gModes
            var decayTc = gDecayTc
            var sampleId = -1
            var startOff = 0
            var endOff = 0

            for (g in gStart until gEnd) {
                if (g >= parsed.igens.size) break
                val gen = parsed.igens[g]
                when (gen.oper) {
                    GEN_KEY_RANGE -> {
                        val u = gen.amount.toInt() and 0xFFFF
                        keyLo = u and 0xFF
                        keyHi = (u shr 8) and 0xFF
                    }
                    GEN_VEL_RANGE -> {
                        val u = gen.amount.toInt() and 0xFFFF
                        velLo = u and 0xFF
                        velHi = (u shr 8) and 0xFF
                    }
                    GEN_OVERRIDING_ROOT_KEY -> {
                        val r = gen.amount.toInt()
                        if (r in 0..127) rootOverride = r
                    }
                    GEN_COARSE_TUNE -> coarse = gen.amount.toInt()
                    GEN_FINE_TUNE -> fine = gen.amount.toInt()
                    GEN_SCALE_TUNING -> scale = gen.amount.toInt()
                    GEN_INITIAL_ATTENUATION -> atten = gen.amount.toInt()
                    GEN_SAMPLE_MODES -> modes = gen.amount.toInt() and 0xFFFF
                    GEN_DECAY_VOL_ENV -> decayTc = gen.amount.toInt()
                    GEN_RELEASE_VOL_ENV -> if (decayTc <= -12000) decayTc = gen.amount.toInt()
                    GEN_SAMPLE_ID -> sampleId = gen.amount.toInt() and 0xFFFF
                    GEN_START_ADDR_OFFSET -> startOff += gen.amount.toInt()
                    GEN_START_ADDR_COARSE_OFFSET -> startOff += gen.amount.toInt() * 32768
                    GEN_END_ADDR_OFFSET -> endOff += gen.amount.toInt()
                    GEN_END_ADDR_COARSE_OFFSET -> endOff += gen.amount.toInt() * 32768
                }
            }

            if (sampleId < 0 && b == iStartBag) {
                gKeyLo = keyLo
                gKeyHi = keyHi
                gVelLo = velLo
                gVelHi = velHi
                gRoot = rootOverride
                gCoarse = coarse
                gFine = fine
                gScale = scale
                gAtten = atten
                gModes = modes
                gDecayTc = decayTc
                continue
            }

            if (REFERENCE_VELOCITY !in velLo..velHi) continue

            if (sampleId in 0 until (parsed.shdrs.size - 1)) {
                val shdr = parsed.shdrs[sampleId]
                val lo = maxOf(keyLo, presetKeyLo)
                val hi = minOf(keyHi, presetKeyHi)
                if (lo > hi) continue

                val decaySec = if (decayTc > -12000) {
                    2.0.pow(decayTc / 1200.0).coerceIn(0.18, 1.5)
                } else {
                    0.40
                }

                val zone = SampleZone(
                    shdr = shdr,
                    rootKeyOverride = rootOverride,
                    coarseTune = coarse + presetCoarse,
                    fineTune = fine + presetFine,
                    scaleTuning = scale,
                    attenuationCb = (atten + presetAtten).coerceAtLeast(0),
                    sampleModes = modes,
                    decaySeconds = decaySec,
                    startOffset = startOff,
                    endOffset = endOff
                )
                for (k in lo..hi) {
                    val list = out.getOrPut(k) { ArrayList(2) }
                    if (list.none { it.shdr.start == shdr.start && it.shdr.end == shdr.end && it.coarseTune == zone.coarseTune }) {
                        list.add(zone)
                    }
                }
            }
        }
    }

    private fun renderLayeredNote(
        parsed: ParsedSf2,
        zones: List<SampleZone>,
        targetMidiNote: Int,
        targetSampleRate: Int,
        targetPeak: Float
    ): FloatArray? {
        val renderedLayers = ArrayList<Pair<FloatArray, Float>>(zones.size)
        var maxLen = 0
        for (z in zones) {
            val pcm = extractSample(parsed, z, targetMidiNote, targetSampleRate) ?: continue
            val gain = 10.0.pow(-z.attenuationCb / 200.0).toFloat().coerceIn(0.02f, 1.0f)
            renderedLayers.add(pcm to gain)
            if (pcm.size > maxLen) maxLen = pcm.size
        }
        if (renderedLayers.isEmpty() || maxLen == 0) return null

        val mix = FloatArray(maxLen)
        for ((pcm, gain) in renderedLayers) {
            for (i in pcm.indices) {
                mix[i] += pcm[i] * gain
            }
        }

        var peak = 0f
        for (s in mix) {
            val a = abs(s)
            if (a > peak) peak = a
        }
        if (peak < 1e-5f) return null
        val scale = targetPeak / peak
        for (i in mix.indices) {
            mix[i] *= scale
        }
        return mix
    }

    private fun extractSample(
        parsed: ParsedSf2,
        zone: SampleZone,
        targetMidiNote: Int,
        targetSampleRate: Int
    ): FloatArray? {
        val shdr = zone.shdr
        val totalSamples = parsed.smplLength / 2
        val start = (shdr.start + zone.startOffset).coerceIn(0, totalSamples)
        val end = (shdr.end + zone.endOffset).coerceIn(start, totalSamples)
        val srcLen = end - start
        if (srcLen <= 4) return null

        val srcRate = shdr.sampleRate.coerceAtLeast(8000).toDouble()
        val rootKey = zone.rootKeyOverride ?: if (shdr.originalPitch in 0..127) shdr.originalPitch else targetMidiNote
        val keyDeltaSemitones = (targetMidiNote - rootKey) * (zone.scaleTuning / 100.0)
        val totalSemitones = keyDeltaSemitones + zone.coarseTune + ((zone.fineTune + shdr.pitchCorrection) / 100.0)
        val pitchRatio = 2.0.pow(totalSemitones / 12.0).coerceIn(0.25, 4.0)
        val step = (srcRate / targetSampleRate.toDouble()) * pitchRatio

        val loopStartRel = (shdr.startLoop - start).coerceIn(0, srcLen - 1)
        val loopEndRel = (shdr.endLoop - start).coerceIn(0, srcLen - 1)
        val isLooped = (zone.sampleModes == 1 || zone.sampleModes == 3) && (loopEndRel > loopStartRel + 2)

        val bytes = parsed.bytes
        val baseByte = parsed.smplOffset + start * 2

        if (isLooped) {
            val outLen = (zone.decaySeconds * targetSampleRate).toInt().coerceIn(targetSampleRate / 6, targetSampleRate * 2)
            val out = FloatArray(outLen)
            val loopLen = (loopEndRel - loopStartRel).toDouble()
            var pos = 0.0
            for (i in 0 until outLen) {
                while (pos >= loopEndRel) pos -= loopLen
                val idx = pos.toInt().coerceIn(0, srcLen - 2)
                val frac = (pos - idx).toFloat()
                val b0 = baseByte + idx * 2
                val s0 = ((bytes[b0].toInt() and 0xFF) or (bytes[b0 + 1].toInt() shl 8)).toShort() / 32768f
                val s1 = ((bytes[b0 + 2].toInt() and 0xFF) or (bytes[b0 + 3].toInt() shl 8)).toShort() / 32768f
                val t = i.toDouble() / outLen.toDouble()
                val env = kotlin.math.exp(-t * 5.2).toFloat()
                out[i] = (s0 + (s1 - s0) * frac) * env
                pos += step
            }
            return out
        }

        val maxSrcLen = min(srcLen, (srcRate * 3.0).toInt())
        val outLen = (maxSrcLen / step).toInt().coerceIn(1, targetSampleRate * 3)
        val out = FloatArray(outLen)
        var pos = 0.0
        for (i in 0 until outLen) {
            val idx = pos.toInt()
            if (idx + 1 >= maxSrcLen) break
            val frac = (pos - idx).toFloat()
            val b0 = baseByte + idx * 2
            val s0 = ((bytes[b0].toInt() and 0xFF) or (bytes[b0 + 1].toInt() shl 8)).toShort() / 32768f
            val s1 = ((bytes[b0 + 2].toInt() and 0xFF) or (bytes[b0 + 3].toInt() shl 8)).toShort() / 32768f
            out[i] = s0 + (s1 - s0) * frac
            pos += step
        }
        return out
    }

    private fun applyWarmLoFi(samples: FloatArray): FloatArray {
        val out = FloatArray(samples.size)
        var lp = 0f
        for (i in samples.indices) {
            lp += 0.36f * (samples[i] - lp)
            out[i] = lp
        }
        return out
    }

    private fun parse(bytes: ByteArray): ParsedSf2? {
        if (bytes.size < 12) return null
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        if (readTag(bytes, 0) != "RIFF" || readTag(bytes, 8) != "sfbk") return null

        var smplOffset = -1
        var smplLength = 0
        var phdrs = emptyList<Phdr>()
        var pbags = emptyList<Bag>()
        var pgens = emptyList<Gen>()
        var insts = emptyList<Inst>()
        var ibags = emptyList<Bag>()
        var igens = emptyList<Gen>()
        var shdrs = emptyList<Shdr>()

        var offset = 12
        while (offset + 8 <= bytes.size) {
            val chunkId = readTag(bytes, offset)
            val chunkSize = buf.getInt(offset + 4)
            val dataStart = offset + 8
            if (chunkSize < 0 || dataStart + chunkSize > bytes.size) break

            if (chunkId == "LIST" && chunkSize >= 4) {
                val listType = readTag(bytes, dataStart)
                var sub = dataStart + 4
                val listEnd = dataStart + chunkSize
                while (sub + 8 <= listEnd) {
                    val subId = readTag(bytes, sub)
                    val subSize = buf.getInt(sub + 4)
                    val subData = sub + 8
                    if (subSize < 0 || subData + subSize > listEnd) break

                    when {
                        listType == "sdta" && subId == "smpl" -> {
                            smplOffset = subData
                            smplLength = subSize
                        }
                        listType == "pdta" -> when (subId) {
                            "phdr" -> phdrs = parsePhdr(bytes, buf, subData, subSize)
                            "pbag" -> pbags = parseBag(buf, subData, subSize)
                            "pgen" -> pgens = parseGen(buf, subData, subSize)
                            "inst" -> insts = parseInst(bytes, buf, subData, subSize)
                            "ibag" -> ibags = parseBag(buf, subData, subSize)
                            "igen" -> igens = parseGen(buf, subData, subSize)
                            "shdr" -> shdrs = parseShdr(bytes, buf, subData, subSize)
                        }
                    }
                    sub = subData + subSize + (subSize and 1)
                }
            }
            offset = dataStart + chunkSize + (chunkSize and 1)
        }

        if (smplOffset < 0 || phdrs.size < 2 || shdrs.size < 2) return null
        return ParsedSf2(
            smplOffset = smplOffset,
            smplLength = smplLength,
            phdrs = phdrs,
            pbags = pbags,
            pgens = pgens,
            insts = insts,
            ibags = ibags,
            igens = igens,
            shdrs = shdrs,
            bytes = bytes
        )
    }

    private fun readTag(bytes: ByteArray, offset: Int): String =
        String(bytes, offset, 4, Charsets.US_ASCII)

    private fun readCString(bytes: ByteArray, offset: Int, maxLen: Int): String {
        var len = 0
        while (len < maxLen && offset + len < bytes.size && bytes[offset + len] != 0.toByte()) {
            len++
        }
        return String(bytes, offset, len, Charsets.US_ASCII).trim()
    }

    private fun parsePhdr(bytes: ByteArray, buf: ByteBuffer, start: Int, size: Int): List<Phdr> {
        val count = size / 38
        return List(count) { i ->
            val o = start + i * 38
            Phdr(
                name = readCString(bytes, o, 20),
                preset = buf.getShort(o + 20).toInt() and 0xFFFF,
                bank = buf.getShort(o + 22).toInt() and 0xFFFF,
                bagNdx = buf.getShort(o + 24).toInt() and 0xFFFF
            )
        }
    }

    private fun parseBag(buf: ByteBuffer, start: Int, size: Int): List<Bag> {
        val count = size / 4
        return List(count) { i ->
            val o = start + i * 4
            Bag(genNdx = buf.getShort(o).toInt() and 0xFFFF)
        }
    }

    private fun parseGen(buf: ByteBuffer, start: Int, size: Int): List<Gen> {
        val count = size / 4
        return List(count) { i ->
            val o = start + i * 4
            Gen(
                oper = buf.getShort(o).toInt() and 0xFFFF,
                amount = buf.getShort(o + 2)
            )
        }
    }

    private fun parseInst(bytes: ByteArray, buf: ByteBuffer, start: Int, size: Int): List<Inst> {
        val count = size / 22
        return List(count) { i ->
            val o = start + i * 22
            Inst(
                name = readCString(bytes, o, 20),
                bagNdx = buf.getShort(o + 20).toInt() and 0xFFFF
            )
        }
    }

    private fun parseShdr(bytes: ByteArray, buf: ByteBuffer, start: Int, size: Int): List<Shdr> {
        val count = size / 46
        return List(count) { i ->
            val o = start + i * 46
            Shdr(
                name = readCString(bytes, o, 20),
                start = buf.getInt(o + 20),
                end = buf.getInt(o + 24),
                startLoop = buf.getInt(o + 28),
                endLoop = buf.getInt(o + 32),
                sampleRate = buf.getInt(o + 36),
                originalPitch = bytes[o + 40].toInt() and 0xFF,
                pitchCorrection = bytes[o + 41].toInt(),
                sampleType = buf.getShort(o + 44).toInt() and 0xFFFF
            )
        }
    }
}
