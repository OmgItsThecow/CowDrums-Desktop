package com.cowdrums.data

import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.DrumGenre
import com.cowdrums.model.DrumInstrument.*
import com.cowdrums.model.DrumPreset
import com.cowdrums.model.StepPattern.Companion.fromAscii

/**
 * 28 additional ready-to-play drum presets covering:
 * Indie Rock, Post-Punk, New Wave, Reggae, Ska, Ska Fusion, Rap, Psytrance, Progressive Trance, and Techno.
 */
internal object PresetLibraryPart3 {

    val presets: List<DrumPreset> = listOf(
        // ========================== INDIE ROCK (4) ==========================
        DrumPreset(
            id = "indie_rock_driving",
            name = "Indie Rock Driving",
            genre = DrumGenre.ROCK,
            bpm = 138,
            recommendedKit = BuiltInKit.ROOM,
            description = "Crisp Strokes / Arctic Monkeys style 8th-note indie rock pulse",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X..x|X...|X...",
                SNARE to "....|X...|....|X..g",
                CRASH to "X...|....|....|....",
                OHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|X...|x.x.|XXXX",
                LOW_TOM to "....|....|..x.|...."
            )
        ),
        DrumPreset(
            id = "indie_surf_garage",
            name = "Indie Surf & Garage",
            genre = DrumGenre.ROCK,
            bpm = 154,
            recommendedKit = BuiltInKit.ROOM,
            description = "Upbeat garage-rock tom & snare bounce with splashy hats",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                SNARE to "....|X...|..x.|X...",
                CHH   to "X.x.|X.x.|X.x.|X...",
                OHH   to "....|....|....|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X.x.|....|X.x.|....",
                SNARE to "....|X...|....|X...",
                LOW_TOM to "x.x.|..x.|x.x.|..x.",
                CRASH to "X...|....|....|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X.x.|....|X.XX",
                MID_TOM to "....|....|xx..|....",
                LOW_TOM to "....|....|..xx|...."
            )
        ),
        DrumPreset(
            id = "indie_shoegaze",
            name = "Dream Pop / Shoegaze",
            genre = DrumGenre.ROCK,
            bpm = 114,
            recommendedKit = BuiltInKit.ROOM,
            description = "Washed-out booming room drums with hypnotic ride shimmer",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|..x.",
                SNARE to "....|X...|....|X..g",
                RIDE  to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|X...|..x.",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|X...|....",
                RIDE  to "x.x.|x.x.|x.x.|x.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|....|x...",
                HIGH_TOM to "....|....|xx..|....",
                MID_TOM  to "....|....|..xx|....",
                LOW_TOM  to "....|....|....|.XXX"
            )
        ),
        DrumPreset(
            id = "indie_math_pocket",
            name = "Midwest Indie Groove",
            genre = DrumGenre.ROCK,
            bpm = 128,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Syncopated indie-rock kick & crisp ghost-note snare pocket",
            mainA = fromAscii(16,
                KICK  to "X..x|....|..X.|.x..",
                SNARE to "....|X..g|....|X.g.",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|...x|....",
                SNARE to "....|X..g|.g..|X..g",
                RIDE  to "X.x.|X.x.|X.x.|X.x.",
                OHH   to "....|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|..x.|....|..x.",
                SNARE to "....|X...|gx..|X.X.",
                HIGH_TOM to "....|....|..x.|....",
                LOW_TOM  to "....|....|...x|...."
            )
        ),

        // ========================== POST-PUNK & NEW WAVE (6) ==========================
        DrumPreset(
            id = "postpunk_motorik",
            name = "Post-Punk Motorik",
            genre = DrumGenre.POST_PUNK,
            bpm = 146,
            recommendedKit = BuiltInKit.ROOM,
            description = "Joy Division / Interpol hypnotic driving 4/4 snare & floor-tom pulse",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                LOW_TOM to "x.x.|....|x.x.|....",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X..x|X...",
                SNARE to "....|X...|....|X...",
                HIGH_TOM to "....|..x.|....|..x.",
                LOW_TOM to "x...|....|x...|....",
                RIDE  to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|X...|X.x.|X.X.",
                LOW_TOM to "....|....|..x.|..XX"
            )
        ),
        DrumPreset(
            id = "postpunk_darkwave",
            name = "Darkwave / Goth Rock",
            genre = DrumGenre.POST_PUNK,
            bpm = 124,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Brooding Sisters of Mercy / Bauhaus steady snare & deep tom pulse",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                SNARE to "....|X...|....|X...",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                LOW_TOM to "....|....|..x.|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                OHH   to "..x.|....|..x.|....",
                CHH   to "xx.x|xxxx|xx.x|xxxx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X...|....|X.X.",
                MID_TOM to "....|....|xx..|....",
                LOW_TOM to "....|....|..XX|...."
            )
        ),
        DrumPreset(
            id = "postpunk_coldwave",
            name = "Coldwave Synth Post-Punk",
            genre = DrumGenre.POST_PUNK,
            bpm = 134,
            recommendedKit = BuiltInKit.TR_808,
            description = "Molchat Doma / New Order crisp coldwave beat with snappy hats",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                OHH   to "....|..x.|....|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X..x|X...|X...",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                CHH   to "xxxx|xxxx|xxxx|xxxx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|X...|x.x.|XXXX"
            )
        ),
        DrumPreset(
            id = "newwave_80s_dance",
            name = "New Wave 80s Dance",
            genre = DrumGenre.POST_PUNK,
            bpm = 126,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Depeche Mode / Talking Heads punchy 80s new wave groove",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "..x.|....|..x.|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                OHH   to "..x.|..x.|..x.|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|....|....",
                HIGH_TOM to "....|..XX|....|....",
                MID_TOM  to "....|....|XX..|....",
                LOW_TOM  to "....|....|..XX|XX.."
            )
        ),
        DrumPreset(
            id = "newwave_offbeat",
            name = "New Wave Pop-Rock",
            genre = DrumGenre.POST_PUNK,
            bpm = 136,
            recommendedKit = BuiltInKit.ROOM,
            description = "Blondie / The Cars bright new-wave snare & energetic open hats",
            mainA = fromAscii(16,
                KICK  to "X...|....|X.x.|....",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X...",
                OHH   to "....|....|....|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                RIDE  to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X.x.|..x.|X.XX"
            )
        ),
        DrumPreset(
            id = "postpunk_dancepunk",
            name = "Dance-Punk Cowbell",
            genre = DrumGenre.POST_PUNK,
            bpm = 130,
            recommendedKit = BuiltInKit.ROOM,
            description = "Gang of Four / LCD Soundsystem angular disco-punk with cowbell",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CHH   to "x...|x...|x...|x...",
                OHH   to "..X.|..X.|..X.|..X.",
                PERC  to "X..x|..x.|X..x|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X..x|X...",
                SNARE to "....|X..g|....|X..g",
                OHH   to "..X.|..X.|..X.|..X.",
                PERC  to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|X...|x.X.|XXXX",
                PERC  to "X.X.|....|....|...."
            )
        ),

        // ========================== REGGAE, SKA & SKA FUSION (7) ==========================
        DrumPreset(
            id = "reggae_steppers",
            name = "Roots Reggae Steppers",
            genre = DrumGenre.REGGAE_SKA,
            bpm = 136,
            swing = 0.22f,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Militant four-on-the-floor Kingston steppers kick with beat-3 rimshot",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                RIM   to "....|....|X...|....",
                CHH   to "x.x.|X.x.|x.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X..x",
                RIM   to "....|..g.|X...|....",
                SNARE to "....|....|x...|....",
                CHH   to "x.xx|X.x.|x.xx|X.x.",
                OHH   to "....|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|....|X..x|x...",
                HIGH_TOM to "....|....|....|.xx.",
                LOW_TOM  to "....|....|....|...X"
            )
        ),
        DrumPreset(
            id = "reggae_rockers",
            name = "Reggae Rockers Groove",
            genre = DrumGenre.REGGAE_SKA,
            bpm = 78,
            swing = 0.24f,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Sly & Robbie rockers pattern with heavy kick on 1 & 3 + cross-stick",
            mainA = fromAscii(16,
                KICK  to "X...|....|X...|..x.",
                RIM   to "....|....|X...|....",
                CHH   to "x.x.|X.x.|x.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X...|..x.",
                SNARE to "....|....|X...|....",
                RIM   to "....|..g.|....|..g.",
                CHH   to "x.xx|X.x.|x.xx|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|....|X.xx|....",
                MID_TOM to "....|....|....|xx..",
                LOW_TOM to "....|....|....|..XX"
            )
        ),
        DrumPreset(
            id = "reggae_dub_spacious",
            name = "Dub Reggae Deep",
            genre = DrumGenre.REGGAE_SKA,
            bpm = 72,
            swing = 0.20f,
            recommendedKit = BuiltInKit.ROOM,
            description = "Deep hypnotic dub space with echoing rimshot & open-hat accents",
            mainA = fromAscii(16,
                KICK  to "....|....|X...|....",
                RIM   to "....|....|X...|..g.",
                CHH   to "x...|X.x.|x...|X...",
                OHH   to "....|....|....|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|....|X...|....",
                RIM   to "....|..x.|....|....",
                CHH   to "x.g.|X.x.|x.g.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "....|....|X...|....",
                SNARE to "....|..x.|X...|x.X.",
                HIGH_TOM to "....|....|..x.|...."
            )
        ),
        DrumPreset(
            id = "ska_traditional_60s",
            name = "Traditional 60s Ska",
            genre = DrumGenre.REGGAE_SKA,
            bpm = 122,
            swing = 0.45f,
            recommendedKit = BuiltInKit.JAZZ_BRUSH,
            description = "Classic Skatalites swung off-beat hi-hat & rimshot on 2 & 4",
            mainA = fromAscii(16,
                KICK  to "....|X...|....|X...",
                RIM   to "....|X...|....|X...",
                CHH   to "....|X...|....|X...",
                RIDE  to "x..x|X..x|x..x|X..x"
            ),
            varB = fromAscii(16,
                KICK  to "g...|X...|g...|X...",
                SNARE to "....|X..g|....|X..g",
                CHH   to "..x.|X..x|..x.|X..x"
            ),
            fill = fromAscii(16,
                KICK  to "....|X...|....|....",
                SNARE to "....|X..x|x..x|X.X.",
                LOW_TOM to "....|....|....|...X"
            )
        ),
        DrumPreset(
            id = "ska_2tone_bounce",
            name = "2-Tone Ska Bounce",
            genre = DrumGenre.REGGAE_SKA,
            bpm = 156,
            recommendedKit = BuiltInKit.ROOM,
            description = "The Specials / Madness energetic upbeat 2-Tone ska groove",
            mainA = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X...|....|X...",
                CHH   to "x...|x...|x...|x...",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X...|..x.",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X.x.",
                CRASH to "X...|....|....|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X...|..X.|XXXX"
            )
        ),
        DrumPreset(
            id = "ska_punk_thirdwave",
            name = "Ska Punk Third-Wave",
            genre = DrumGenre.REGGAE_SKA,
            bpm = 180,
            recommendedKit = BuiltInKit.ROCK_METAL,
            description = "Upbeat ska verse (Main A) switching into fast punk chorus (Var B)",
            mainA = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X...|....|X...",
                CHH   to "x...|x...|x...|x...",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "..X.|..X.|..X.|..X.",
                CRASH to "X...|....|X...|....",
                OHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X.X.|..X.|XXXX"
            )
        ),
        DrumPreset(
            id = "ska_fusion_groove",
            name = "Ska Fusion / Jazz-Ska",
            genre = DrumGenre.REGGAE_SKA,
            bpm = 142,
            swing = 0.16f,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Syncopated ska-funk-latin fusion with ghost snares, cowbell & ride",
            mainA = fromAscii(16,
                KICK  to "X..x|....|X...|..x.",
                SNARE to "....|X..g|....|X..g",
                RIM   to "..x.|....|..x.|....",
                CHH   to "x.X.|x.X.|x.X.|x.X.",
                PERC  to "..x.|....|..x.|...."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|X...|..x.",
                SNARE to "....|X..g|.g..|X..g",
                RIDE  to "x.X.|x.X.|x.X.|x.X.",
                LOW_TOM to "....|....|...x|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|..x.|....",
                SNARE to "....|X.gx|....|X...",
                HIGH_TOM to "....|....|xx..|....",
                MID_TOM  to "....|....|..xx|....",
                LOW_TOM  to "....|....|....|..XX"
            )
        ),

        // ========================== RAP / HIP-HOP EXPANSION (4) ==========================
        DrumPreset(
            id = "rap_eastcoast_hardcore",
            name = "East Coast Hardcore Rap",
            genre = DrumGenre.HIP_HOP,
            bpm = 93,
            swing = 0.30f,
            recommendedKit = BuiltInKit.LOFI,
            description = "Gritty Nas / Wu-Tang style hard-hitting rap bounce",
            mainA = fromAscii(16,
                KICK  to "X..x|....|X.x.|..x.",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|...x|X.x.|..x.",
                SNARE to "....|X..g|....|X..g",
                CHH   to "X.x.|X.x.|X.x.|X...",
                OHH   to "....|....|....|..X."
            ),
            fill = fromAscii(16,
                KICK  to "X..x|....|X...|....",
                SNARE to "....|X...|..xx|X.XX"
            )
        ),
        DrumPreset(
            id = "rap_memphis_phonk",
            name = "Memphis Phonk Rap",
            genre = DrumGenre.HIP_HOP,
            bpm = 132,
            recommendedKit = BuiltInKit.TR_808,
            description = "Iconic 808 cowbell phonk groove with booming sub-kick & sharp hats",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|....|..x.",
                CLAP  to "....|....|X...|....",
                CHH   to "xxxx|x.xx|xxxx|x.xx",
                PERC  to "X..x|..x.|X..x|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|....|..x.",
                SNARE to "....|....|X...|....",
                CLAP  to "....|....|X...|....",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                PERC  to "X..x|..x.|.x.x|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|....|X.xx|XXXX",
                PERC  to "X.X.|....|....|...."
            )
        ),
        DrumPreset(
            id = "rap_southern_bounce",
            name = "Southern Bounce / Crunk",
            genre = DrumGenre.HIP_HOP,
            bpm = 98,
            recommendedKit = BuiltInKit.TR_808,
            description = "Heavy ATL 808 kick with rapid snare rolls & crisp hi-hats",
            mainA = fromAscii(16,
                KICK  to "X..x|..x.|X...|....",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.xx|x.xx|x.xx|x.xx"
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|X..x|....",
                SNARE to "....|X...|....|X.xx",
                CLAP  to "....|X...|....|X...",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                OHH   to "....|..x.|....|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|xxxx|XXXX",
                CLAP  to "....|X...|....|X..."
            )
        ),
        DrumPreset(
            id = "rap_jazz_hop",
            name = "Jazz-Rap Golden Era",
            genre = DrumGenre.HIP_HOP,
            bpm = 88,
            swing = 0.36f,
            recommendedKit = BuiltInKit.JAZZ_BRUSH,
            description = "Tribe Called Quest warm jazz-ride & swung boom-bap pocket",
            mainA = fromAscii(16,
                KICK  to "X...|...x|..X.|....",
                SNARE to "....|X...|....|X...",
                RIDE  to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|...x|..X.|.x..",
                RIM   to "....|..g.|....|..g.",
                SNARE to "....|X...|....|X...",
                RIDE  to "X.xx|X.x.|X.xx|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|..x.",
                SNARE to "....|X...|..gx|X.X.",
                RIDE  to "X.x.|....|....|...."
            )
        ),

        // ========================== PSYTRANCE, PROG TRANCE & TECHNO (7) ==========================
        DrumPreset(
            id = "psytrance_fullon",
            name = "Full-On Psytrance",
            genre = DrumGenre.EDM,
            bpm = 142,
            recommendedKit = BuiltInKit.TR_909,
            description = "Relentless rolling 16th-note psytrance drive with crisp off-beat hats",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CHH   to ".xxx|.xxx|.xxx|.xxx",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                CHH   to ".xxx|.xxx|.xxx|.xxx",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|....|xxxx|XXXX",
                CLAP  to "....|X...|....|X..."
            )
        ),
        DrumPreset(
            id = "psytrance_dark_forest",
            name = "Dark / Forest Psytrance",
            genre = DrumGenre.EDM,
            bpm = 148,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Fast 148 BPM hypnotic rolling percussion & tight sub kick",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                PERC  to "...x|..x.|...x|.x..",
                RIM   to "..g.|....|..g.|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                OHH   to "..X.|..X.|..X.|..X.",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                PERC  to ".x.x|..x.|.x.x|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|X.X.|....",
                SNARE to "....|....|....|XXXX",
                HIGH_TOM to "....|....|xx..|...."
            )
        ),
        DrumPreset(
            id = "psytrance_progressive",
            name = "Progressive Psytrance",
            genre = DrumGenre.EDM,
            bpm = 136,
            recommendedKit = BuiltInKit.TR_909,
            description = "Groovy 136 BPM prog-psy with rolling 16th hats & percussive accents",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                OHH   to "..X.|..X.|..X.|..X.",
                LOW_TOM to "...x|....|...x|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to ".xxx|.xxx|.xxx|.xxx",
                OHH   to "..X.|..X.|..X.|..X.",
                PERC  to "..x.|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|....|x.xx|XXXX"
            )
        ),
        DrumPreset(
            id = "trance_progressive",
            name = "Progressive Trance",
            genre = DrumGenre.EDM,
            bpm = 130,
            recommendedKit = BuiltInKit.TR_909,
            description = "Hypnotic Anjunabeats / progressive trance pulse with airy 16th hats",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "g.xx|g.xx|g.xx|g.xx",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CRASH to "X...|....|....|....",
                RIDE  to "..x.|..x.|..x.|..x.",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|X...|....",
                SNARE to "....|....|..xx|XXXX"
            )
        ),
        DrumPreset(
            id = "techno_melodic",
            name = "Melodic Techno",
            genre = DrumGenre.EDM,
            bpm = 124,
            recommendedKit = BuiltInKit.TR_909,
            description = "Deep Afterlife-style melodic techno with tight rim & rolling tom",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                OHH   to "..X.|..X.|..X.|..X.",
                RIM   to "...g|....|...g|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                OHH   to "..X.|..X.|..X.|..X.",
                LOW_TOM to "..x.|...x|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                LOW_TOM to "....|....|x.xx|X...",
                SNARE to "....|....|....|.XXX"
            )
        ),
        DrumPreset(
            id = "techno_industrial_hard",
            name = "Industrial Hard Techno",
            genre = DrumGenre.EDM,
            bpm = 144,
            recommendedKit = BuiltInKit.TR_909,
            description = "Pounding Berlin warehouse hard techno with relentless metallic ride",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                OHH   to "..X.|..X.|..X.|..X.",
                RIDE  to "x.X.|x.X.|x.X.|x.X.",
                CHH   to "x.xx|x.xx|x.xx|x.xx"
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X..x",
                CLAP  to "....|X...|....|X...",
                SNARE to "....|x...|....|x...",
                OHH   to "..X.|..X.|..X.|..X.",
                RIDE  to "X.X.|X.X.|X.X.|X.X."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|X.X.|XXXX",
                SNARE to "....|....|X.X.|XXXX"
            )
        ),
        DrumPreset(
            id = "techno_minimal_dub",
            name = "Minimal Dub Techno",
            genre = DrumGenre.EDM,
            bpm = 126,
            swing = 0.14f,
            recommendedKit = BuiltInKit.TR_808,
            description = "Stripped-back hypnotic minimal dub techno with crisp 808 clicks",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                RIM   to "....|X...|....|X...",
                CHH   to "..x.|..x.|..x.|..x.",
                PERC  to "...g|....|..g.|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "g.xx|g.xx|g.xx|g.xx",
                OHH   to "..x.|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                RIM   to "....|X...|x.x.|x.XX"
            )
        )
    )
}
