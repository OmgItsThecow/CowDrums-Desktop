package com.cowdrums.audio

import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.DrumInstrument
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh
import kotlin.random.Random

/**
 * Generates punchy, characterful 44.1 kHz mono FloatArray drum samples for all 8 built-in kits.
 * Deterministic seeded RNG ensures identical sound quality across runs.
 */
object BuiltInKitSynthesizer {

    const val SAMPLE_RATE = 44100

    fun renderKit(kit: BuiltInKit): Map<DrumInstrument, FloatArray> {
        val map = LinkedHashMap<DrumInstrument, FloatArray>(DrumInstrument.ALL.size)
        for (inst in DrumInstrument.ALL) {
            val raw = when (inst) {
                DrumInstrument.KICK -> synthKick(kit)
                DrumInstrument.SNARE -> synthSnare(kit)
                DrumInstrument.RIM -> synthRim(kit)
                DrumInstrument.CLAP -> synthClap(kit)
                DrumInstrument.CHH -> synthHiHat(kit, open = false)
                DrumInstrument.OHH -> synthHiHat(kit, open = true)
                DrumInstrument.LOW_TOM -> synthTom(kit, baseFreq = when (kit) {
                    BuiltInKit.LATIN -> 175.0
                    BuiltInKit.TR_808 -> 92.0
                    else -> 88.0
                })
                DrumInstrument.MID_TOM -> synthTom(kit, baseFreq = when (kit) {
                    BuiltInKit.LATIN -> 240.0
                    BuiltInKit.TR_808 -> 132.0
                    else -> 125.0
                })
                DrumInstrument.HIGH_TOM -> synthTom(kit, baseFreq = when (kit) {
                    BuiltInKit.LATIN -> 320.0
                    BuiltInKit.TR_808 -> 185.0
                    else -> 175.0
                })
                DrumInstrument.CRASH -> synthCymbal(kit, ride = false)
                DrumInstrument.RIDE -> synthCymbal(kit, ride = true)
                DrumInstrument.PERC -> synthPerc(kit)
            }
            map[inst] = if (kit == BuiltInKit.LOFI) applyLoFiFlavor(raw) else normalizePeak(raw, 0.92f)
        }
        return map
    }

    fun renderMetronomeClick(accent: Boolean): FloatArray {
        val dur = 0.035
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        val freq = if (accent) 1680.0 else 1120.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 140.0)
            out[i] = (sin(2.0 * PI * freq * t) * env * 0.65).toFloat()
        }
        return out
    }

    private fun synthKick(kit: BuiltInKit): FloatArray {
        val dur = when (kit) {
            BuiltInKit.TR_808 -> 0.72
            BuiltInKit.ELECTRONIC -> 0.48
            BuiltInKit.JAZZ_BRUSH -> 0.24
            else -> 0.38
        }
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        val rng = Random(1001 + kit.ordinal)

        val startFreq = when (kit) {
            BuiltInKit.TR_808 -> 145.0
            BuiltInKit.TR_909 -> 220.0
            BuiltInKit.ROCK_METAL -> 260.0
            BuiltInKit.JAZZ_BRUSH, BuiltInKit.BRUSH -> 150.0
            BuiltInKit.ELECTRONIC -> 210.0
            BuiltInKit.LATIN -> 160.0
            BuiltInKit.LOFI -> 155.0
            else -> 185.0
        }
        val endFreq = when (kit) {
            BuiltInKit.TR_808 -> 41.0
            BuiltInKit.TR_909 -> 49.0
            BuiltInKit.ROCK_METAL -> 54.0
            BuiltInKit.JAZZ_BRUSH, BuiltInKit.BRUSH -> 64.0
            BuiltInKit.ELECTRONIC -> 44.0
            BuiltInKit.LATIN -> 58.0
            BuiltInKit.LOFI -> 50.0
            else -> 52.0
        }
        val pitchDecay = when (kit) {
            BuiltInKit.TR_808 -> 24.0
            BuiltInKit.TR_909 -> 42.0
            BuiltInKit.ROCK_METAL -> 55.0
            else -> 38.0
        }
        val ampDecay = when (kit) {
            BuiltInKit.TR_808 -> 4.2
            BuiltInKit.JAZZ_BRUSH -> 14.0
            BuiltInKit.ELECTRONIC -> 6.8
            else -> 9.5
        }
        val clickAmount = when (kit) {
            BuiltInKit.ROCK_METAL -> 0.55
            BuiltInKit.TR_909 -> 0.42
            BuiltInKit.ACOUSTIC -> 0.30
            BuiltInKit.TR_808 -> 0.14
            BuiltInKit.JAZZ_BRUSH -> 0.18
            else -> 0.28
        }
        val drive = when (kit) {
            BuiltInKit.TR_808 -> 1.8
            BuiltInKit.TR_909 -> 2.1
            BuiltInKit.ROCK_METAL -> 2.3
            BuiltInKit.ELECTRONIC -> 2.0
            else -> 1.5
        }

        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = endFreq + (startFreq - endFreq) * exp(-t * pitchDecay)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val body = sin(phase)
            val env = exp(-t * ampDecay)
            val clickEnv = exp(-t * 320.0)
            val click = (sin(2.0 * PI * 1400.0 * t) * 0.6 + (rng.nextDouble() * 2.0 - 1.0) * 0.4) * clickEnv * clickAmount
            val sample = tanh((body * env + click) * drive)
            out[i] = sample.toFloat()
        }
        return out
    }

    private fun synthSnare(kit: BuiltInKit): FloatArray {
        val dur = if (kit == BuiltInKit.JAZZ_BRUSH) 0.32 else 0.28
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        val rng = Random(2002 + kit.ordinal)

        val toneFreq = when (kit) {
            BuiltInKit.TR_808 -> 180.0
            BuiltInKit.TR_909 -> 195.0
            BuiltInKit.ROCK_METAL -> 215.0
            BuiltInKit.JAZZ_BRUSH -> 230.0
            BuiltInKit.LOFI -> 175.0
            else -> 190.0
        }
        val noiseDecay = when (kit) {
            BuiltInKit.TR_808 -> 16.0
            BuiltInKit.ROCK_METAL -> 12.0
            BuiltInKit.JAZZ_BRUSH -> 9.0
            else -> 14.5
        }
        val toneMix = if (kit == BuiltInKit.JAZZ_BRUSH) 0.22 else 0.55
        val noiseMix = if (kit == BuiltInKit.JAZZ_BRUSH) 0.85 else 0.72

        var hpPrev = 0.0
        var hpOut = 0.0
        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val f = toneFreq + 90.0 * exp(-t * 60.0)
            phase += 2.0 * PI * f / SAMPLE_RATE
            val tone = (sin(phase) + 0.35 * sin(phase * 1.62)) * exp(-t * 24.0)

            val white = rng.nextDouble() * 2.0 - 1.0
            hpOut = 0.82 * (hpOut + white - hpPrev)
            hpPrev = white

            val nEnv = if (kit == BuiltInKit.JAZZ_BRUSH) {
                (1.0 - exp(-t * 140.0)) * exp(-t * noiseDecay)
            } else {
                exp(-t * noiseDecay)
            }
            val snap = exp(-t * 180.0) * (rng.nextDouble() * 2.0 - 1.0) * 0.35
            val mixed = tanh((tone * toneMix + hpOut * nEnv * noiseMix + snap) * 1.45)
            out[i] = mixed.toFloat()
        }
        return out
    }

    private fun synthRim(kit: BuiltInKit): FloatArray {
        val dur = 0.09
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        val rng = Random(3003 + kit.ordinal)
        val f1 = when (kit) {
            BuiltInKit.TR_808 -> 1680.0
            BuiltInKit.LATIN -> 1850.0
            else -> 1420.0
        }
        val f2 = f1 * 0.36
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 82.0)
            val body = sin(2.0 * PI * f1 * t) * 0.65 + sin(2.0 * PI * f2 * t) * 0.55
            val click = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 260.0) * 0.45
            out[i] = tanh((body * env + click) * 1.6).toFloat()
        }
        return out
    }

    private fun synthClap(kit: BuiltInKit): FloatArray {
        val dur = 0.26
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        val rng = Random(4004 + kit.ordinal)

        // Classic 4-burst staggered handclap envelope + reverb tail
        val bursts = doubleArrayOf(0.0, 0.011, 0.022, 0.034)
        var lp = 0.0
        var hp = 0.0
        var prev = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val white = rng.nextDouble() * 2.0 - 1.0
            // Bandpass around ~1.2kHz
            hp = 0.78 * (hp + white - prev)
            prev = white
            lp += 0.32 * (hp - lp)

            var env = 0.0
            for (b in bursts) {
                if (t >= b) {
                    val dt = t - b
                    env += exp(-dt * 115.0) * 0.65
                }
            }
            if (t >= 0.032) {
                env += exp(-(t - 0.032) * if (kit == BuiltInKit.TR_808) 15.0 else 19.0) * 0.55
            }
            out[i] = tanh(lp * env * 2.2).toFloat()
        }
        return out
    }

    private fun synthHiHat(kit: BuiltInKit, open: Boolean): FloatArray {
        val dur = when {
            open && kit == BuiltInKit.TR_808 -> 0.52
            open -> 0.42
            kit == BuiltInKit.TR_808 -> 0.065
            else -> 0.075
        }
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        val rng = Random((if (open) 5100 else 5000) + kit.ordinal)

        // Classic Roland 6-oscillator inharmonic metallic cluster
        val ratios = doubleArrayOf(205.3, 304.4, 369.6, 522.7, 540.0, 800.0)
        val decay = if (open) {
            if (kit == BuiltInKit.TR_808) 7.5 else 9.0
        } else {
            if (kit == BuiltInKit.TR_808) 58.0 else 52.0
        }
        val metallicWeight = when (kit) {
            BuiltInKit.TR_808, BuiltInKit.TR_909, BuiltInKit.ELECTRONIC -> 0.65
            else -> 0.38
        }

        var hp1 = 0.0
        var prev1 = 0.0
        var hp2 = 0.0
        var prev2 = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            var metal = 0.0
            for (f in ratios) {
                val sq = if (sin(2.0 * PI * f * t) >= 0.0) 1.0 else -1.0
                metal += sq
            }
            metal /= ratios.size
            val noise = rng.nextDouble() * 2.0 - 1.0
            val raw = metal * metallicWeight + noise * (1.0 - metallicWeight)

            // 2-stage high-pass to make it crisp & airy
            hp1 = 0.90 * (hp1 + raw - prev1)
            prev1 = raw
            hp2 = 0.88 * (hp2 + hp1 - prev2)
            prev2 = hp1

            val env = exp(-t * decay)
            out[i] = tanh(hp2 * env * 1.8).toFloat()
        }
        return out
    }

    private fun synthTom(kit: BuiltInKit, baseFreq: Double): FloatArray {
        val dur = if (kit == BuiltInKit.LATIN) 0.24 else 0.34
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        val rng = Random(6000 + baseFreq.toInt() + kit.ordinal)

        val pitchDrop = if (kit == BuiltInKit.LATIN) 1.45 else 1.85
        val ampDecay = if (kit == BuiltInKit.LATIN) 13.5 else 9.2
        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = baseFreq + baseFreq * (pitchDrop - 1.0) * exp(-t * 28.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val body = sin(phase) + 0.22 * sin(phase * 2.01)
            val slap = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 160.0) * 0.22
            val env = exp(-t * ampDecay)
            out[i] = tanh((body * env + slap) * 1.4).toFloat()
        }
        return out
    }

    private fun synthCymbal(kit: BuiltInKit, ride: Boolean): FloatArray {
        val dur = if (ride) 0.75 else 1.15
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        val rng = Random((if (ride) 7100 else 7000) + kit.ordinal)

        val freqs = if (ride) {
            doubleArrayOf(312.0, 487.0, 651.0, 912.0, 1340.0, 2240.0)
        } else {
            doubleArrayOf(240.0, 355.0, 498.0, 712.0, 1085.0, 1620.0)
        }
        val decay = if (ride) 4.8 else 3.1
        var hp = 0.0
        var prev = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            var metal = 0.0
            for (f in freqs) {
                metal += sin(2.0 * PI * f * t + 1.4 * sin(2.0 * PI * (f * 1.41) * t))
            }
            metal /= freqs.size
            val noise = rng.nextDouble() * 2.0 - 1.0
            val mix = if (ride) (metal * 0.58 + noise * 0.42) else (metal * 0.35 + noise * 0.65)
            hp = 0.87 * (hp + mix - prev)
            prev = mix

            val ping = if (ride) sin(2.0 * PI * 2850.0 * t) * exp(-t * 36.0) * 0.45 else 0.0
            val attack = 1.0 - exp(-t * 240.0)
            val env = attack * exp(-t * decay)
            out[i] = tanh((hp * env + ping) * 1.25).toFloat()
        }
        return out
    }

    private fun synthPerc(kit: BuiltInKit): FloatArray {
        val dur = 0.22
        val n = (SAMPLE_RATE * dur).toInt()
        val out = FloatArray(n)
        when (kit) {
            BuiltInKit.LATIN -> {
                // Bright wooden clave
                for (i in 0 until n) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = exp(-t * 55.0)
                    val sig = sin(2.0 * PI * 2520.0 * t) * 0.7 + sin(2.0 * PI * 3780.0 * t) * 0.3
                    out[i] = (sig * env).toFloat()
                }
            }
            BuiltInKit.JAZZ_BRUSH, BuiltInKit.LOFI -> {
                // Crisp shaker / tambourine
                val rng = Random(8008 + kit.ordinal)
                var hp = 0.0
                var prev = 0.0
                for (i in 0 until n) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val white = rng.nextDouble() * 2.0 - 1.0
                    hp = 0.88 * (hp + white - prev)
                    prev = white
                    val env = (1.0 - exp(-t * 120.0)) * exp(-t * 24.0)
                    out[i] = (hp * env * 1.3).toFloat()
                }
            }
            else -> {
                // Iconic 808/Rock Cowbell (540 Hz + 800 Hz square waves band-shaped)
                var lp = 0.0
                for (i in 0 until n) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val s1 = if (sin(2.0 * PI * 545.0 * t) >= 0.0) 1.0 else -1.0
                    val s2 = if (sin(2.0 * PI * 815.0 * t) >= 0.0) 1.0 else -1.0
                    val raw = (s1 + s2) * 0.5
                    lp += 0.28 * (raw - lp)
                    val env = exp(-t * 17.5)
                    out[i] = tanh(lp * env * 1.4).toFloat()
                }
            }
        }
        return out
    }

    private fun applyLoFiFlavor(samples: FloatArray): FloatArray {
        val out = FloatArray(samples.size)
        var lp = 0f
        var hold = 0f
        val crushSteps = 28f // ~5-6 bit warmth
        for (i in samples.indices) {
            if (i % 2 == 0) {
                hold = samples[i]
            }
            val crushed = floor(hold * crushSteps + 0.5f) / crushSteps
            lp += 0.38f * (crushed - lp)
            out[i] = lp
        }
        return normalizePeak(out, 0.90f)
    }

    private fun normalizePeak(samples: FloatArray, targetPeak: Float): FloatArray {
        var maxAbs = 0f
        for (s in samples) {
            val a = abs(s)
            if (a > maxAbs) maxAbs = a
        }
        if (maxAbs < 1e-6f) return samples
        val scale = targetPeak / maxAbs
        return FloatArray(samples.size) { i -> samples[i] * scale }
    }
}
