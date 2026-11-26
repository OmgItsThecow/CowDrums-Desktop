package com.cowdrums.data

import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.DrumGenre
import com.cowdrums.model.DrumInstrument.*
import com.cowdrums.model.DrumPreset
import com.cowdrums.model.StepPattern.Companion.fromAscii
import com.cowdrums.model.TimeSignature

internal object PresetLibraryPart1 {

    val presets: List<DrumPreset> = listOf(
        // ========================== ROCK (6) ==========================
        DrumPreset(
            id = "rock_classic_8th",
            name = "Classic 8th Rock",
            genre = DrumGenre.ROCK,
            bpm = 120,
            recommendedKit = BuiltInKit.ROOM,
            description = "Driving 8th-note hi-hat rock groove with punchy acoustic backbeat",
            mainA = fromAscii(16,
                KICK  to "X...|....|X.x.|....",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "....|X...|....|X..g",
                RIDE  to "X.x.|X.x.|X.x.|X.x.",
                CRASH to "X...|....|....|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|..X.",
                SNARE to "....|X...|x.x.|....",
                HIGH_TOM to "....|....|..x.|....",
                MID_TOM  to "....|....|....|xx..",
                LOW_TOM  to "....|....|....|..XX",
                CHH   to "X.x.|X.x.|....|...."
            )
        ),
        DrumPreset(
            id = "rock_hard_driving",
            name = "Hard Rock Anthem",
            genre = DrumGenre.ROCK,
            bpm = 132,
            recommendedKit = BuiltInKit.ROCK_METAL,
            description = "Heavy open hi-hat crunch with double-kick pickups",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X.x.|..x.",
                SNARE to "....|X...|....|X...",
                OHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X.x.|..x.|X.x.|..x.",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|X...|....",
                RIDE  to "..x.|X.x.|..x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|x...|x...",
                SNARE to "....|X.xx|X...|....",
                HIGH_TOM to "....|....|.xx.|....",
                MID_TOM  to "....|....|....|xx..",
                LOW_TOM  to "....|....|....|..XX"
            )
        ),
        DrumPreset(
            id = "rock_punk_fast",
            name = "Skate Punk Drive",
            genre = DrumGenre.ROCK,
            bpm = 172,
            recommendedKit = BuiltInKit.ROCK_METAL,
            description = "High-energy D-beat / skate-punk gallop",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "..X.|..X.|..X.|..X.",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X.x.|....|X.x.|..x.",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|....|....",
                OHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X.X.|..X.|XXXX",
                LOW_TOM to "....|....|X...|...."
            )
        ),
        DrumPreset(
            id = "rock_grunge_halftime",
            name = "90s Grunge Half-Time",
            genre = DrumGenre.ROCK,
            bpm = 96,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Sludgy Seattle groove with ghost snares and sloshy hats",
            mainA = fromAscii(16,
                KICK  to "X..x|..x.|X...|..x.",
                SNARE to "....|X..g|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X...",
                OHH   to "....|....|....|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|X..x|....",
                SNARE to "....|X..g|....|X.g.",
                CRASH to "X...|....|....|....",
                RIDE  to "..x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X...|..xx|X...",
                MID_TOM to "....|....|....|.xx.",
                LOW_TOM to "....|....|....|...X"
            )
        ),
        DrumPreset(
            id = "rock_stadium_stomp",
            name = "Stadium Stomp",
            genre = DrumGenre.ROCK,
            bpm = 110,
            recommendedKit = BuiltInKit.ROCK_METAL,
            description = "Big floor-tom & clap arena rock groove",
            mainA = fromAscii(16,
                KICK  to "X.x.|....|X.x.|....",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                LOW_TOM to "X.x.|..x.|X.x.|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X.x.|..x.|X.x.|....",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|X...|....",
                LOW_TOM to "x.x.|x.x.|x.x.|x.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|X...",
                SNARE to "....|X...|X.X.|....",
                MID_TOM to "....|....|....|XX..",
                LOW_TOM to "....|....|....|..XX"
            )
        ),
        DrumPreset(
            id = "rock_shuffle",
            name = "Blues-Rock Shuffle",
            genre = DrumGenre.ROCK,
            bpm = 112,
            timeSignature = TimeSignature.SIX_EIGHT,
            recommendedKit = BuiltInKit.ROOM,
            description = "True 12/8 triplet blues-rock shuffle with swung ride, kick pickups & ghost snares",
            mainA = fromAscii(12,
                KICK  to "X.x|...|X.x|...",
                SNARE to "...|X.g|...|X.g",
                CHH   to "X.x|X.x|X.x|X.x"
            ),
            varB = fromAscii(12,
                KICK  to "X.x|..x|X.x|..x",
                SNARE to "..g|X.g|..g|X.g",
                RIDE  to "X.x|X.x|X.x|X.x",
                CRASH to "X..|...|...|..."
            ),
            fill = fromAscii(12,
                KICK  to "X..|...|X..|...",
                SNARE to "...|X.x|x.x|...",
                MID_TOM to "...|...|...|xx.",
                LOW_TOM to "...|...|...|..X"
            )
        ),

        // ========================== POP (5) ==========================
        DrumPreset(
            id = "pop_four_on_floor",
            name = "Modern Pop Pulse",
            genre = DrumGenre.POP,
            bpm = 122,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Radio-ready four-on-the-floor pop groove with layered claps",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|x...|....|x...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                OHH   to "..x.|....|..x.|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X..x",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.xx|x.xx|x.xx|x.x.",
                OHH   to "....|....|....|...X",
                PERC  to "..x.|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|....|x.x.|XXXX",
                CLAP  to "....|X...|....|X.X."
            )
        ),
        DrumPreset(
            id = "pop_synth_80s",
            name = "80s Synthwave Pop",
            genre = DrumGenre.POP,
            bpm = 118,
            recommendedKit = BuiltInKit.TR_808,
            description = "Nostalgic 80s gated-snare vibe with cowbell accents",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                SNARE to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "....|..x.|....|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                PERC  to "x...|..x.|x...|..x."
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
            id = "pop_disco_groove",
            name = "Nu-Disco Groove",
            genre = DrumGenre.POP,
            bpm = 120,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Classic off-beat open hi-hat disco pocket",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CHH   to "x...|x...|x...|x...",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                CHH   to "x.g.|x.g.|x.g.|x.g.",
                OHH   to "..X.|..X.|..X.|..X.",
                PERC  to ".x.x|.x.x|.x.x|.x.x"
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|X...|....",
                SNARE to "....|X...|..x.|X.XX",
                OHH   to "..X.|....|....|...."
            )
        ),
        DrumPreset(
            id = "pop_indie_dance",
            name = "Indie Pop Bounce",
            genre = DrumGenre.POP,
            bpm = 126,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Tight rimshot verse & energetic chorus groove",
            mainA = fromAscii(16,
                KICK  to "X...|....|X.x.|....",
                RIM   to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X...",
                OHH   to "....|....|....|..X."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|..x.|x.X.",
                HIGH_TOM to "....|....|x...|....",
                MID_TOM  to "....|....|...x|...."
            )
        ),
        DrumPreset(
            id = "pop_power_ballad",
            name = "Power Ballad",
            genre = DrumGenre.POP,
            bpm = 78,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Slow emotional stadium ballad with spacious snare",
            mainA = fromAscii(16,
                KICK  to "X...|....|X..x|....",
                SNARE to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X..x|....",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|....|....",
                RIDE  to "x.x.|x.x.|x.x.|x.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|..x.|....",
                HIGH_TOM to "....|....|...x|....",
                MID_TOM  to "....|....|....|xx..",
                LOW_TOM  to "....|....|....|..XX"
            )
        ),

        // ========================== FUNK & SOUL (5) ==========================
        DrumPreset(
            id = "funk_james_brown",
            name = "Cold Sweat Funk",
            genre = DrumGenre.FUNK,
            bpm = 104,
            swing = 0.18f,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Iconic 70s syncopated kick & crisp 16th ghost-note snare",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|..X.|....",
                SNARE to "....|X..g|.g..|X..g",
                CHH   to "X.x.|X.x.|X.x.|X.x.",
                OHH   to "....|....|....|...x"
            ),
            varB = fromAscii(16,
                KICK  to "X..x|....|..X.|.x..",
                SNARE to "....|X..g|.g..|X..g",
                CHH   to "Xxxx|Xxxx|Xxxx|Xxx.",
                OHH   to "....|....|....|...X"
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|..x.|....",
                SNARE to "....|X.gx|X.gx|X.XX",
                HIGH_TOM to "....|....|....|.x.."
            )
        ),
        DrumPreset(
            id = "funk_ghost_pocket",
            name = "Linear Funk Pocket",
            genre = DrumGenre.FUNK,
            bpm = 98,
            swing = 0.14f,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Tight linear funk groove with syncopated hi-hat barks",
            mainA = fromAscii(16,
                KICK  to "X..x|....|.x.x|....",
                SNARE to "....|X.g.|....|X.gg",
                CHH   to "x.x.|x.x.|x.x.|x...",
                OHH   to "....|....|....|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|.x..|..x.",
                SNARE to "....|X..g|..g.|X.g.",
                RIDE  to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|..x.|....|..x.",
                SNARE to "....|X...|gxx.|X...",
                LOW_TOM to "....|....|...x|..X."
            )
        ),
        DrumPreset(
            id = "funk_motown_classic",
            name = "Motown Soul",
            genre = DrumGenre.FUNK,
            bpm = 114,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Four-on-the-floor Motown stomp with tambourine/shaker",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "x.x.|X.x.|x.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X..x|X...",
                SNARE to "....|X...|....|X..g",
                RIDE  to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "xxxx|xxxx|xxxx|xxxx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|X...|X.X.|XXXX"
            )
        ),
        DrumPreset(
            id = "funk_neosoul_swung",
            name = "Neo-Soul Dilla Swing",
            genre = DrumGenre.FUNK,
            bpm = 86,
            swing = 0.42f,
            recommendedKit = BuiltInKit.LOFI,
            description = "Laid-back drunken swing pocket with rim & dusty snare",
            mainA = fromAscii(16,
                KICK  to "X...|...x|..X.|....",
                SNARE to "....|X...|....|X...",
                RIM   to "....|..g.|....|..g.",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|...x|..X.|...x",
                SNARE to "....|X..g|....|X..g",
                CHH   to "x.xx|x.x.|x.xx|x.x.",
                OHH   to "....|....|....|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|..x.",
                SNARE to "....|X...|..gg|X.X.",
                LOW_TOM to "....|....|....|...x"
            )
        ),
        DrumPreset(
            id = "funk_slap_groove",
            name = "Slap Bass Funk",
            genre = DrumGenre.FUNK,
            bpm = 110,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Crisp 16th hi-hat disco-funk built for bassists",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X..x|....",
                SNARE to "....|X...|....|X...",
                CHH   to "Xxxx|Xxxx|Xxxx|Xxx.",
                OHH   to "....|....|....|...X"
            ),
            varB = fromAscii(16,
                KICK  to "X.x.|..x.|X..x|..x.",
                SNARE to "....|X..g|....|X..g",
                CLAP  to "....|x...|....|x...",
                CHH   to "Xxxx|Xxxx|Xxxx|Xxxx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X.x.|..x.|X.XX",
                HIGH_TOM to "....|....|x...|....",
                LOW_TOM  to "....|....|...x|...."
            )
        ),

        // ========================== HIP-HOP & TRAP (6) ==========================
        DrumPreset(
            id = "hiphop_boombap_90s",
            name = "90s Boom-Bap",
            genre = DrumGenre.HIP_HOP,
            bpm = 90,
            swing = 0.28f,
            recommendedKit = BuiltInKit.LOFI,
            description = "Golden-era NYC dusty kick & head-nodding snare",
            mainA = fromAscii(16,
                KICK  to "X...|...x|..X.|.x..",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|....|X.x.|.x..",
                SNARE to "....|X..g|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X...",
                OHH   to "....|....|....|..X."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X.x.|....",
                SNARE to "....|X...|....|X.XX",
                CHH   to "X.x.|X.x.|....|...."
            )
        ),
        DrumPreset(
            id = "hiphop_modern_trap",
            name = "Modern Trap 808",
            genre = DrumGenre.HIP_HOP,
            bpm = 140,
            recommendedKit = BuiltInKit.TR_808,
            description = "Half-time 808 boom with rapid trap hi-hat rolls & clap",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|....|..x.",
                CLAP  to "....|....|X...|....",
                SNARE to "....|....|x...|..g.",
                CHH   to "x.x.|xxxx|x.x.|x.xx",
                OHH   to "....|....|....|x..."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|....|..x.|..x.",
                CLAP  to "....|....|X...|....",
                CHH   to "xxxx|x.xx|xxxx|xxxx",
                PERC  to "....|x...|....|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|....|X...|x.XX",
                CLAP  to "....|....|X...|....",
                CHH   to "xxxx|xxxx|....|...."
            )
        ),
        DrumPreset(
            id = "hiphop_westcoast_gfunk",
            name = "West Coast G-Funk",
            genre = DrumGenre.HIP_HOP,
            bpm = 94,
            swing = 0.22f,
            recommendedKit = BuiltInKit.BOOM_BAP,
            description = "Cruising LA bounce with punchy claps & cowbell",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "....|..x.|....|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|X...|..x.",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.xx|x.x.|x.xx|x.x.",
                OHH   to "....|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|..x.|....",
                SNARE to "....|X...|....|XXXX",
                CLAP  to "....|X...|....|X..."
            )
        ),
        DrumPreset(
            id = "hiphop_lofi_chill",
            name = "Lo-Fi Study Beats",
            genre = DrumGenre.HIP_HOP,
            bpm = 80,
            swing = 0.35f,
            recommendedKit = BuiltInKit.LOFI,
            description = "Chill late-night swung beat with soft rim & shaker",
            mainA = fromAscii(16,
                KICK  to "X...|....|..X.|....",
                RIM   to "....|X...|....|X...",
                CHH   to "x.g.|x.g.|x.g.|x.g.",
                PERC  to "..g.|..g.|..g.|..g."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..g.|..X.|.x..",
                SNARE to "....|x...|....|x...",
                RIM   to "....|X...|....|X...",
                CHH   to "x.g.|x.g.|x.g.|x.g."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                RIM   to "....|X...|..g.|x.x.",
                SNARE to "....|....|....|...X"
            )
        ),
        DrumPreset(
            id = "hiphop_uk_drill",
            name = "UK Drill Bounce",
            genre = DrumGenre.HIP_HOP,
            bpm = 142,
            recommendedKit = BuiltInKit.TR_808,
            description = "Signature syncopated 3-3-2 drill hi-hat & late snare",
            mainA = fromAscii(16,
                KICK  to "X...|....|....|..x.",
                SNARE to "....|....|X...|...X",
                CHH   to "x..x|..x.|x..x|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|....|..x.",
                SNARE to "....|....|X...|...X",
                RIM   to "....|..x.|....|....",
                CHH   to "x..x|..xx|x..x|..xx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|....|X..x|x.XX",
                CHH   to "x..x|..x.|....|...."
            )
        ),
        DrumPreset(
            id = "hiphop_electro_808",
            name = "Old-School Electro",
            genre = DrumGenre.HIP_HOP,
            bpm = 124,
            recommendedKit = BuiltInKit.TR_808,
            description = "Planet Rock style breakbeat 808 with cowbell",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|....|..x.",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "X..x|..x.|X..x|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|..x.|....",
                SNARE to "....|X...|....|X...",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                PERC  to "X..x|..x.|X..x|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X...|..x.|XXXX",
                PERC  to "X.X.|....|....|...."
            )
        ),

        // ========================== EDM & HOUSE (6) ==========================
        DrumPreset(
            id = "edm_deep_house",
            name = "Deep House 909",
            genre = DrumGenre.EDM,
            bpm = 122,
            swing = 0.20f,
            recommendedKit = BuiltInKit.TR_909,
            description = "Hypnotic 909 club groove with warm off-beat open hats",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "...x|...x|...x|...x",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                RIM   to "..g.|....|...g|....",
                CHH   to "g..x|g..x|g..x|g..x",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|....|x.x.|xxxx",
                CLAP  to "....|X...|....|X.X."
            )
        ),
        DrumPreset(
            id = "edm_tech_house",
            name = "Tech House Rolling",
            genre = DrumGenre.EDM,
            bpm = 126,
            swing = 0.16f,
            recommendedKit = BuiltInKit.TR_909,
            description = "Rolling percussive tech-house with snappy claps & toms",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                OHH   to "..X.|..X.|..X.|..X.",
                LOW_TOM to "...x|....|..x.|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X..g",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                OHH   to "..X.|..X.|..X.|..X.",
                PERC  to ".x..|...x|.x..|...x"
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|X.X.|....",
                SNARE to "....|....|....|XXXX",
                CLAP  to "....|X...|....|X..."
            )
        ),
        DrumPreset(
            id = "edm_peak_techno",
            name = "Peak-Time Techno",
            genre = DrumGenre.EDM,
            bpm = 134,
            recommendedKit = BuiltInKit.TR_909,
            description = "Relentless warehouse 909 rumble & sizzling 16th ride",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                OHH   to "..X.|..X.|..X.|..X.",
                RIDE  to "..x.|..x.|..x.|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X..x",
                CLAP  to "....|X...|....|X...",
                OHH   to "..X.|..X.|..X.|..X.",
                RIDE  to "x.X.|x.X.|x.X.|x.X."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|X.X.|XXXX",
                SNARE to "....|....|..X.|..XX"
            )
        ),
        DrumPreset(
            id = "edm_dnb_amen",
            name = "Drum & Bass Roller",
            genre = DrumGenre.EDM,
            bpm = 172,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Fast two-step DnB break with crisp ghost snares",
            mainA = fromAscii(16,
                KICK  to "X...|....|..X.|....",
                SNARE to "....|X..g|.g..|X...",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|....|..X.|...x",
                SNARE to "....|X..g|.g..|X..g",
                RIDE  to "X.x.|X.x.|X.x.|X.x.",
                OHH   to "....|..x.|....|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X.xx|X.xx|XXXX"
            )
        ),
        DrumPreset(
            id = "edm_dubstep_halftime",
            name = "Dubstep Half-Time",
            genre = DrumGenre.EDM,
            bpm = 140,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Massive beat-3 snare drop with shuffled hats",
            mainA = fromAscii(16,
                KICK  to "X...|....|....|..x.",
                SNARE to "....|....|X...|....",
                CLAP  to "....|....|X...|....",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|....|....|..x.",
                SNARE to "....|....|X...|....",
                CRASH to "X...|....|....|....",
                OHH   to "..x.|..x.|..x.|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|..x.|X.X.|XXXX"
            )
        ),
        DrumPreset(
            id = "edm_trance_driving",
            name = "Uplifting Trance",
            genre = DrumGenre.EDM,
            bpm = 138,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Euphoric 138 BPM 4/4 kick with rolling 16th hats",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CRASH to "X...|....|....|....",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|....|xxxx|XXXX"
            )
        )
    )
}
