package com.cowdrums.model

import kotlinx.serialization.Serializable

/**
 * 12-piece drum kit mapped to General MIDI (GM) Percussion Note Numbers (Channel 10 / Bank 128).
 */
enum class DrumInstrument(
    val shortName: String,
    val displayName: String,
    val gmNote: Int,
    val fallbackGmNotes: List<Int>,
    val chokeGroup: Int, // 0 = none, 1 = hi-hat choke group
    val colorHex: Long
) {
    KICK(
        shortName = "KCK",
        displayName = "Kick",
        gmNote = 36,
        fallbackGmNotes = listOf(35),
        chokeGroup = 0,
        colorHex = 0xFFEF4444
    ),
    SNARE(
        shortName = "SNR",
        displayName = "Snare",
        gmNote = 38,
        fallbackGmNotes = listOf(40),
        chokeGroup = 0,
        colorHex = 0xFFF59E0B
    ),
    RIM(
        shortName = "RIM",
        displayName = "Rimshot",
        gmNote = 37,
        fallbackGmNotes = listOf(38),
        chokeGroup = 0,
        colorHex = 0xFFFBBF24
    ),
    CLAP(
        shortName = "CLP",
        displayName = "Clap",
        gmNote = 39,
        fallbackGmNotes = listOf(38),
        chokeGroup = 0,
        colorHex = 0xFFEC4899
    ),
    CHH(
        shortName = "CHH",
        displayName = "Closed HH",
        gmNote = 42,
        fallbackGmNotes = listOf(44),
        chokeGroup = 1,
        colorHex = 0xFF06B6D4
    ),
    OHH(
        shortName = "OHH",
        displayName = "Open HH",
        gmNote = 46,
        fallbackGmNotes = listOf(42),
        chokeGroup = 1,
        colorHex = 0xFF22D3EE
    ),
    LOW_TOM(
        shortName = "LTOM",
        displayName = "Low Tom",
        gmNote = 45,
        fallbackGmNotes = listOf(41, 43),
        chokeGroup = 0,
        colorHex = 0xFF8B5CF6
    ),
    MID_TOM(
        shortName = "MTOM",
        displayName = "Mid Tom",
        gmNote = 47,
        fallbackGmNotes = listOf(48),
        chokeGroup = 0,
        colorHex = 0xFFA855F7
    ),
    HIGH_TOM(
        shortName = "HTOM",
        displayName = "High Tom",
        gmNote = 50,
        fallbackGmNotes = listOf(48, 47),
        chokeGroup = 0,
        colorHex = 0xFFC084FC
    ),
    CRASH(
        shortName = "CRSH",
        displayName = "Crash",
        gmNote = 49,
        fallbackGmNotes = listOf(57, 55),
        chokeGroup = 0,
        colorHex = 0xFF10B981
    ),
    RIDE(
        shortName = "RIDE",
        displayName = "Ride",
        gmNote = 51,
        fallbackGmNotes = listOf(59, 53),
        chokeGroup = 0,
        colorHex = 0xFF34D399
    ),
    PERC(
        shortName = "PERC",
        displayName = "Cowbell/Perc",
        gmNote = 56,
        fallbackGmNotes = listOf(54, 75, 60, 62),
        chokeGroup = 0,
        colorHex = 0xFF3B82F6
    );

    companion object {
        val ALL = entries.toList()
        fun fromNameOrNull(name: String): DrumInstrument? = entries.firstOrNull { it.name == name }
    }
}

@Serializable
enum class TimeSignature(
    val label: String,
    val stepsPerBar: Int,
    val beatsPerBar: Int,
    val stepsPerBeat: Int
) {
    FOUR_FOUR("4/4", 16, 4, 4),
    THREE_FOUR("3/4", 12, 3, 4),
    SIX_EIGHT("6/8", 12, 6, 2),
    FIVE_FOUR("5/4", 20, 5, 4),
    SEVEN_EIGHT("7/8", 14, 7, 2)
}

@Serializable
enum class PatternSlot(val label: String) {
    MAIN_A("MAIN A"),
    VAR_B("VAR B"),
    FILL("FILL")
}

@Serializable
enum class DrumGenre(val label: String) {
    ROCK("Rock"),
    POP("Indie & Pop"),
    EDM("Electronic"),
    HIP_HOP("Rap & Hip-Hop"),
    REGGAE_SKA("Reggae & Ska"),
    FUNK("Funk & R&B"),
    JAZZ("Jazz & Blues"),
    LATIN("Latin"),
    ODD_TIME("Odd Time"),
    POST_PUNK("Post-Punk"),
    METAL("Metal"),
    AFRO_RNB("Afro & R&B"),
    CUSTOM("My Presets")
}

@Serializable
enum class BuiltInKit(
    val displayName: String,
    val subtitle: String,
    val sf2Preset: Int,
    val applyLoFiFilter: Boolean = false
) {
    ACOUSTIC("Standard Studio", "Real acoustic studio drum kit (SF2)", 0),
    ROOM("Live Room Rock", "Natural resonant room acoustic kit (SF2)", 8),
    ROCK_METAL("Power Rock & Metal", "Heavy punchy acoustic rock/metal kit (SF2)", 16),
    JAZZ_BRUSH("Jazz Acoustic", "Warm bebop & fusion acoustic kit (SF2)", 32),
    BRUSH("Jazz Brush", "Real brushed snare & mellow jazz kit (SF2)", 40),
    TR_808("TR-808 / 909", "Classic analog drum machine samples (SF2)", 25),
    TR_909("Club Dance", "Punchy house & club drum samples (SF2)", 26),
    ELECTRONIC("Electronic", "Crisp modern electronic kit (SF2)", 24),
    LATIN("Standard 2 / Latin", "Bright acoustic kit with rim & perc (SF2)", 1),
    ORCHESTRAL("Orchestral", "Concert bass drum, snare & cymbals (SF2)", 48),
    LOFI("Lo-Fi Warm", "Vintage tape-filtered acoustic kit (SF2)", 0, applyLoFiFilter = true),
    INDIE_GARAGE("Indie & Post-Punk", "Dry dampened tape-warm garage & post-punk kit (SF2)", 56),
    SYNTHWAVE_80S("80s Synthwave", "Linn/DMX punch with gated-reverb snare & toms (SF2)", 57),
    REGGAE_DUB("Reggae / Dub", "Deep kick, high-tuned snare & dub spring rimshot (SF2)", 58),
    TECHNO_PSY("Techno / Psy 909", "Laser-tight psytrance/techno kick & bright 909 hats (SF2)", 59),
    BOOM_BAP("Boom-Bap MPC", "Dusty 12-bit MPC60/SP-1200 hip-hop breakbeat kit (SF2)", 60)
}

@Serializable
enum class BassPreset(
    val gmProgram: Int,
    val displayName: String,
    val bank: Int = 0
) {
    ACOUSTIC_BASS(32, "ACOUSTIC BASS"),
    FINGERED_BASS(33, "FINGERED BASS"),
    PICKED_BASS(34, "PICKED BASS"),
    FRETLESS_BASS(35, "FRETLESS BASS"),
    SLAP_BASS_1(36, "SLAP BASS 1"),
    SLAP_BASS_2(37, "SLAP BASS 2"),
    SYNTH_BASS_1(38, "SYNTH BASS 1"),
    SYNTH_BASS_2(39, "SYNTH BASS 2")
}

data class SoundFontPresetInfo(
    val bank: Int,
    val preset: Int,
    val name: String
)

data class LoadedSoundFont(
    val fileName: String,
    val displayName: String,
    val filePath: String,
    val presets: List<SoundFontPresetInfo>
)

sealed interface SoundSource {
    val label: String

    data class BuiltIn(val kit: BuiltInKit) : SoundSource {
        override val label: String = kit.displayName
    }

    data class SoundFont(
        val fileName: String,
        val displayName: String,
        val bank: Int,
        val preset: Int,
        val presetName: String
    ) : SoundSource {
        override val label: String = "SF2: $presetName"
    }
}

/**
 * Velocity levels per step:
 * 0 = Off
 * 1 = Ghost / Soft (~0.36)
 * 2 = Normal (~0.72)
 * 3 = Accent / Loud (1.0)
 */
object StepVelocity {
    const val OFF = 0
    const val SOFT = 1
    const val MEDIUM = 2
    const val ACCENT = 3

    fun toGain(level: Int): Float = when (level) {
        SOFT -> 0.36f
        MEDIUM -> 0.72f
        ACCENT -> 1.0f
        else -> 0f
    }

    fun next(level: Int): Int = when (level) {
        OFF -> MEDIUM
        MEDIUM -> ACCENT
        ACCENT -> SOFT
        else -> OFF
    }
}

@Serializable
data class StepPattern(
    val stepCount: Int = 16,
    val rows: Map<String, List<Int>> = emptyMap(),
    val bassNotes: Map<Int, Int> = emptyMap() // step -> MIDI note (e.g. 24..59)
) {
    fun velocityAt(instrument: DrumInstrument, step: Int): Int {
        val row = rows[instrument.name] ?: return 0
        return if (step in row.indices) row[step] else 0
    }

    fun withVelocity(instrument: DrumInstrument, step: Int, velocity: Int): StepPattern {
        val current = rows[instrument.name]?.toMutableList() ?: MutableList(stepCount) { 0 }
        while (current.size < stepCount) current.add(0)
        if (step in 0 until stepCount) {
            current[step] = velocity.coerceIn(0, 3)
        }
        val nextRows = rows.toMutableMap()
        nextRows[instrument.name] = current
        return copy(rows = nextRows)
    }

    fun withBassNote(step: Int, midiNote: Int?): StepPattern {
        val next = bassNotes.toMutableMap()
        if (midiNote == null) next.remove(step) else next[step] = midiNote
        return copy(bassNotes = next)
    }

    fun clearBass(): StepPattern = copy(bassNotes = emptyMap())

    fun resized(newStepCount: Int): StepPattern {
        if (newStepCount == stepCount) return this
        val nextRows = rows.mapValues { (_, list) ->
            List(newStepCount) { i ->
                if (list.isEmpty()) 0 else list[i % list.size]
            }
        }
        val nextBass = LinkedHashMap<Int, Int>()
        for (i in 0 until newStepCount) {
            val src = bassNotes[i % stepCount]
            if (src != null) nextBass[i] = src
        }
        return StepPattern(stepCount = newStepCount, rows = nextRows, bassNotes = nextBass)
    }

    companion object {
        fun empty(stepCount: Int = 16): StepPattern = StepPattern(stepCount = stepCount, rows = emptyMap())

        /**
         * Compact ASCII pattern builder:
         * '.' = 0 (off)
         * 'g' or '1' = 1 (ghost/soft)
         * 'x' or '2' = 2 (normal)
         * 'X' or '3' = 3 (accent)
         * Spaces and '|' separators are ignored for readability.
         */
        fun fromAscii(stepCount: Int, vararg tracks: Pair<DrumInstrument, String>): StepPattern {
            val map = LinkedHashMap<String, List<Int>>()
            for ((inst, raw) in tracks) {
                val cleaned = raw.filter { it != ' ' && it != '|' }
                val values = List(stepCount) { i ->
                    val ch = if (i < cleaned.length) cleaned[i] else '.'
                    when (ch) {
                        'g', '1' -> StepVelocity.SOFT
                        'x', '2' -> StepVelocity.MEDIUM
                        'X', '3' -> StepVelocity.ACCENT
                        else -> StepVelocity.OFF
                    }
                }
                if (values.any { it > 0 }) {
                    map[inst.name] = values
                }
            }
            return StepPattern(stepCount = stepCount, rows = map)
        }
    }
}

@Serializable
data class DrumPreset(
    val id: String,
    val name: String,
    val genre: DrumGenre,
    val subcategory: String = "",
    val bpm: Int,
    val swing: Float = 0f,
    val timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
    val recommendedKit: BuiltInKit = BuiltInKit.ACOUSTIC,
    val description: String = "",
    val mainA: StepPattern,
    val varB: StepPattern,
    val fill: StepPattern,
    val isCustom: Boolean = false
) {
    fun patternFor(slot: PatternSlot): StepPattern = when (slot) {
        PatternSlot.MAIN_A -> mainA
        PatternSlot.VAR_B -> varB
        PatternSlot.FILL -> fill
    }

    fun withPattern(slot: PatternSlot, pattern: StepPattern): DrumPreset = when (slot) {
        PatternSlot.MAIN_A -> copy(mainA = pattern)
        PatternSlot.VAR_B -> copy(varB = pattern)
        PatternSlot.FILL -> copy(fill = pattern)
    }
}

@Serializable
data class ChannelMix(
    val volume: Float = 0.85f,
    val pan: Float = 0f, // -1f (left) to +1f (right)
    val pitchSemitones: Float = 0f, // -12f to +12f
    val muted: Boolean = false,
    val solo: Boolean = false
)

@Serializable
data class SongSection(
    val id: String,
    val presetId: String,
    val presetName: String,
    val slot: PatternSlot = PatternSlot.MAIN_A,
    val bars: Int = 4,
    val playFillOnLastBar: Boolean = true
)

@Serializable
enum class NoteRepeatRate(val label: String, val beatsDivisor: Double) {
    OFF("Off", 0.0),
    EIGHTH("1/8", 2.0),
    SIXTEENTH("1/16", 4.0),
    THIRTY_SECOND("1/32", 8.0),
    TRIPLET_16("1/16T", 6.0)
}

data class GmDrumSound(
    val note: Int,
    val name: String
)

object GmDrumCatalog {
    val ALL: List<GmDrumSound> = listOf(
        GmDrumSound(35, "Acoustic Bass Drum"),
        GmDrumSound(36, "Bass Drum 1"),
        GmDrumSound(37, "Side Stick / Rimshot"),
        GmDrumSound(38, "Acoustic Snare"),
        GmDrumSound(39, "Hand Clap"),
        GmDrumSound(40, "Electric Snare"),
        GmDrumSound(41, "Low Floor Tom"),
        GmDrumSound(42, "Closed Hi-Hat"),
        GmDrumSound(43, "High Floor Tom"),
        GmDrumSound(44, "Pedal Hi-Hat"),
        GmDrumSound(45, "Low Tom"),
        GmDrumSound(46, "Open Hi-Hat"),
        GmDrumSound(47, "Low-Mid Tom"),
        GmDrumSound(48, "Hi-Mid Tom"),
        GmDrumSound(49, "Crash Cymbal 1"),
        GmDrumSound(50, "High Tom"),
        GmDrumSound(51, "Ride Cymbal 1"),
        GmDrumSound(52, "Chinese Cymbal"),
        GmDrumSound(53, "Ride Bell"),
        GmDrumSound(54, "Tambourine"),
        GmDrumSound(55, "Splash Cymbal"),
        GmDrumSound(56, "Cowbell"),
        GmDrumSound(57, "Crash Cymbal 2"),
        GmDrumSound(59, "Ride Cymbal 2"),
        GmDrumSound(60, "Hi Bongo"),
        GmDrumSound(61, "Low Bongo"),
        GmDrumSound(62, "Mute Hi Conga"),
        GmDrumSound(63, "Open Hi Conga"),
        GmDrumSound(64, "Low Conga"),
        GmDrumSound(65, "High Timbale"),
        GmDrumSound(66, "Low Timbale"),
        GmDrumSound(69, "Cabasa"),
        GmDrumSound(70, "Maracas"),
        GmDrumSound(75, "Claves"),
        GmDrumSound(81, "Open Triangle")
    )

    fun nameFor(note: Int): String =
        ALL.firstOrNull { it.note == note }?.name ?: "GM Note $note"

    fun midiNoteLabel(note: Int): String {
        val names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val octave = (note / 12) - 1
        return "${names[note.coerceIn(0, 127) % 12]}$octave ($note)"
    }
}

@Serializable
data class PadConfig(
    val soundGmNote: Int? = null,
    val kitOverride: BuiltInKit? = null,
    val customWavName: String? = null,
    val chokeGroup: Int = 0, // 0 = None, 1 = Hi-Hat Group, 2 = Group 2, 3 = Group 3
    val midiInputNote: Int = 36,
    val noteRepeat: NoteRepeatRate = NoteRepeatRate.OFF
) {
    val hasCustomSound: Boolean
        get() = soundGmNote != null || kitOverride != null || customWavName != null
}

data class MidiDeviceEntry(
    val id: String,
    val name: String,
    val isBluetooth: Boolean = false,
    val connected: Boolean = false,
    val detail: String = ""
)

