package com.cowdrums.data

import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.DrumGenre
import com.cowdrums.model.DrumInstrument.*
import com.cowdrums.model.DrumPreset
import com.cowdrums.model.StepPattern.Companion.fromAscii
import com.cowdrums.model.TimeSignature

internal object PresetLibraryPart2 {

    val presets: List<DrumPreset> = listOf(
        // ========================== JAZZ & BLUES (5) ==========================
        DrumPreset(
            id = "jazz_bebop_swing",
            name = "Bebop Ride Swing",
            genre = DrumGenre.JAZZ,
            bpm = 136,
            swing = 0.58f,
            recommendedKit = BuiltInKit.JAZZ_BRUSH,
            description = "Classic spang-a-lang ride cymbal with 2 & 4 hi-hat chick",
            mainA = fromAscii(16,
                KICK  to "g...|....|g...|....",
                SNARE to "....|..g.|....|..g.",
                CHH   to "....|X...|....|X...",
                RIDE  to "X...|X..x|X...|X..x"
            ),
            varB = fromAscii(16,
                KICK  to "g...|...g|....|...g",
                SNARE to "...g|..g.|..g.|....",
                CHH   to "....|X...|....|X...",
                RIDE  to "X..x|X..x|X..x|X..x"
            ),
            fill = fromAscii(16,
                SNARE to "....|x..x|x...|x.x.",
                HIGH_TOM to "....|....|..x.|....",
                LOW_TOM  to "....|....|....|...X",
                CHH   to "....|X...|....|X...",
                RIDE  to "X...|....|....|...."
            )
        ),
        DrumPreset(
            id = "jazz_waltz_34",
            name = "Jazz Waltz (3/4)",
            genre = DrumGenre.JAZZ,
            bpm = 130,
            swing = 0.52f,
            timeSignature = TimeSignature.THREE_FOUR,
            recommendedKit = BuiltInKit.JAZZ_BRUSH,
            description = "Elegant 3/4 jazz waltz with swung ride & brush snare",
            mainA = fromAscii(12,
                KICK  to "x...|....|....",
                SNARE to "....|..g.|....",
                CHH   to "....|X...|....",
                RIDE  to "X...|X..x|X..x"
            ),
            varB = fromAscii(12,
                KICK  to "x...|....|..g.",
                SNARE to "....|g..g|..g.",
                CHH   to "....|X...|X...",
                RIDE  to "X..x|X..x|X..x"
            ),
            fill = fromAscii(12,
                SNARE to "....|x..x|x.x.",
                HIGH_TOM to "....|....|..x.",
                LOW_TOM  to "....|....|...X",
                RIDE  to "X...|....|...."
            )
        ),
        DrumPreset(
            id = "jazz_slow_blues",
            name = "Slow Blues (6/8)",
            genre = DrumGenre.JAZZ,
            bpm = 68,
            timeSignature = TimeSignature.SIX_EIGHT,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Soulful 6/8 slow blues with gentle ride shimmer",
            mainA = fromAscii(12,
                KICK  to "X...|....|..x.",
                SNARE to "....|..X.|....",
                RIDE  to "X.x.|x.X.|x.x."
            ),
            varB = fromAscii(12,
                KICK  to "X...|..x.|..x.",
                SNARE to "....|..X.|...g",
                CRASH to "X...|....|....",
                RIDE  to "X.x.|x.X.|x.x."
            ),
            fill = fromAscii(12,
                KICK  to "X...|....|....",
                SNARE to "....|..X.|x.x.",
                MID_TOM to "....|....|.x..",
                LOW_TOM to "....|....|..XX"
            )
        ),
        DrumPreset(
            id = "jazz_chicago_shuffle",
            name = "Chicago Blues Shuffle",
            genre = DrumGenre.JAZZ,
            bpm = 112,
            swing = 0.60f,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Classic swung snare shuffle & walking kick",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "...g|X..g|...g|X..g",
                RIDE  to "X..x|X..x|X..x|X..x"
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "g..g|X..g|g..g|X..g",
                CHH   to "X..x|X..x|X..x|X..x"
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X..x|X..x|X.XX"
            )
        ),
        DrumPreset(
            id = "jazz_fusion_groove",
            name = "Jazz Fusion 16ths",
            genre = DrumGenre.JAZZ,
            bpm = 108,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Crisp syncopated fusion pocket with intricate ghost notes",
            mainA = fromAscii(16,
                KICK  to "X..x|..x.|...x|....",
                SNARE to "....|X..g|.g..|X.g.",
                CHH   to "Xxxx|Xxxx|Xxxx|Xxx.",
                OHH   to "....|....|....|...x"
            ),
            varB = fromAscii(16,
                KICK  to "X..x|....|.x.x|..x.",
                SNARE to "....|X.g.|..g.|X..g",
                RIDE  to "X.xx|X.xx|X.xx|X.xx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|..x.|....|..x.",
                SNARE to "....|X...|g.x.|....",
                HIGH_TOM to "....|..x.|....|....",
                MID_TOM  to "....|....|..xx|....",
                LOW_TOM  to "....|....|....|xxX."
            )
        ),

        // ========================== LATIN & REGGAE (6) ==========================
        DrumPreset(
            id = "latin_bossa_nova",
            name = "Bossa Nova Classic",
            genre = DrumGenre.LATIN,
            bpm = 128,
            recommendedKit = BuiltInKit.LATIN,
            description = "Authentic Rio de Janeiro 2-bar feel with rimshot clave & surdo kick",
            mainA = fromAscii(16,
                KICK  to "X..x|X..x|X..x|X..x",
                RIM   to "X..X|..X.|..X.|.X..",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|X..x|X..x|X..x",
                RIM   to "X..X|..X.|..X.|.X..",
                RIDE  to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "x.x.|x.x.|x.x.|x.x."
            ),
            fill = fromAscii(16,
                KICK  to "X..x|X...|....|....",
                RIM   to "X..X|....|....|....",
                HIGH_TOM to "....|..x.|x...|....",
                MID_TOM  to "....|....|..x.|x...",
                LOW_TOM  to "....|....|....|..X."
            )
        ),
        DrumPreset(
            id = "latin_samba",
            name = "Samba Batucada",
            genre = DrumGenre.LATIN,
            bpm = 104,
            recommendedKit = BuiltInKit.LATIN,
            description = "Upbeat Brazilian samba with accented surdo & tambourim clave",
            mainA = fromAscii(16,
                KICK  to "x..X|x..X|x..X|x..X",
                RIM   to "X.x.|.xX.|X.x.|.xX.",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                PERC  to "X..x|..x.|X..x|..x."
            ),
            varB = fromAscii(16,
                KICK  to "x..X|x..X|x..X|x..X",
                SNARE to "g.gg|X.gg|g.gg|X.gg",
                RIDE  to "X.xx|X.xx|X.xx|X.xx",
                PERC  to "X..x|..x.|..x.|x..."
            ),
            fill = fromAscii(16,
                KICK  to "x..X|....|....|..X.",
                SNARE to "....|X.xx|X.xx|....",
                LOW_TOM to "....|....|....|XX.."
            )
        ),
        DrumPreset(
            id = "latin_reggaeton_dembow",
            name = "Reggaeton Dembow",
            genre = DrumGenre.LATIN,
            bpm = 96,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Iconic 3+3+2 dembow bounce with punchy snare & kick",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "...X|..X.|...X|..X.",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "...X|..X.|...X|..X.",
                CLAP  to "...x|..x.|...x|..x.",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                PERC  to "x...|..x.|x...|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "...X|..X.|X.X.|XXXX"
            )
        ),
        DrumPreset(
            id = "latin_reggae_onedrop",
            name = "Reggae One-Drop",
            genre = DrumGenre.REGGAE_SKA,
            bpm = 76,
            swing = 0.25f,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Roots Kingston one-drop with kick & rim landing together on beat 3",
            mainA = fromAscii(16,
                KICK  to "....|....|X...|....",
                RIM   to "....|....|X...|....",
                CHH   to "x.x.|X.x.|x.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "....|....|X...|..x.",
                RIM   to "....|..g.|X...|....",
                SNARE to "....|....|x...|....",
                CHH   to "x.x.|X.xx|x.x.|X.x.",
                OHH   to "....|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "....|....|X...|....",
                SNARE to "....|....|X..x|x...",
                HIGH_TOM to "....|....|....|.xx.",
                LOW_TOM  to "....|....|....|...X"
            )
        ),
        DrumPreset(
            id = "latin_salsa_cascara",
            name = "Salsa Cascara",
            genre = DrumGenre.LATIN,
            bpm = 180,
            recommendedKit = BuiltInKit.LATIN,
            description = "Energetic 2-3 son clave with timbale/cowbell montuno",
            mainA = fromAscii(16,
                KICK  to "...x|..x.|...x|..x.",
                RIM   to "..X.|..X.|X..X|..X.",
                PERC  to "X...|X.x.|X...|X.x.",
                HIGH_TOM to "....|..x.|....|..x."
            ),
            varB = fromAscii(16,
                KICK  to "...x|..x.|...x|..x.",
                RIM   to "..X.|..X.|X..X|..X.",
                PERC  to "X.x.|X.x.|X.x.|X.x.",
                MID_TOM to "....|x...|....|x...",
                LOW_TOM to "....|..X.|....|..X."
            ),
            fill = fromAscii(16,
                SNARE to "X.x.|X.x.|....|....",
                HIGH_TOM to "....|....|X.x.|....",
                LOW_TOM  to "....|....|....|X.X."
            )
        ),
        DrumPreset(
            id = "latin_cumbia",
            name = "Cumbia Groove",
            genre = DrumGenre.LATIN,
            bpm = 96,
            recommendedKit = BuiltInKit.LATIN,
            description = "Infectious Colombian cumbia pulse with güiro-style hats",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|..x.",
                RIM   to "..X.|....|..X.|....",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                LOW_TOM to "....|..X.|....|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X...|..x.",
                RIM   to "..X.|....|..X.|....",
                PERC  to "X...|X...|X...|X...",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                MID_TOM to "....|x...|....|x..."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                HIGH_TOM to "....|xx..|....|....",
                MID_TOM  to "....|..xx|....|....",
                LOW_TOM  to "....|....|xx..|X.X."
            )
        ),

        // ========================== METAL (4) ==========================
        DrumPreset(
            id = "metal_thrash_double",
            name = "Thrash Double-Kick",
            genre = DrumGenre.METAL,
            bpm = 164,
            recommendedKit = BuiltInKit.ROCK_METAL,
            description = "Relentless 16th-note double bass thrash attack",
            mainA = fromAscii(16,
                KICK  to "xxxx|xxxx|xxxx|xxxx",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|X...|....",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "Xxxx|Xxxx|Xxxx|Xxxx",
                SNARE to "..X.|..X.|..X.|..X.",
                CRASH to "X...|X...|X...|X..."
            ),
            fill = fromAscii(16,
                KICK  to "xxxx|xxxx|....|xxxx",
                SNARE to "....|....|XXXX|....",
                LOW_TOM to "....|....|....|X.X."
            )
        ),
        DrumPreset(
            id = "metal_breakdown",
            name = "Metalcore Breakdown",
            genre = DrumGenre.METAL,
            bpm = 136,
            recommendedKit = BuiltInKit.ROCK_METAL,
            description = "Crushing half-time china/crash breakdown with syncopated kicks",
            mainA = fromAscii(16,
                KICK  to "X.xx|..X.|..xx|....",
                SNARE to "....|....|X...|....",
                CRASH to "X...|X...|X...|X..."
            ),
            varB = fromAscii(16,
                KICK  to "X.xx|X.x.|..xx|X...",
                SNARE to "....|....|X...|....",
                CRASH to "X...|X...|X...|X...",
                RIDE  to "..x.|..x.|..x.|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X.xx|....|....|....",
                SNARE to "....|X...|X.X.|....",
                HIGH_TOM to "....|....|....|xx..",
                LOW_TOM  to "....|....|....|..XX"
            )
        ),
        DrumPreset(
            id = "metal_blast_beat",
            name = "Extreme Blast Beat",
            genre = DrumGenre.METAL,
            bpm = 184,
            recommendedKit = BuiltInKit.ROCK_METAL,
            description = "Alternating kick/snare extreme metal blast beat",
            mainA = fromAscii(16,
                KICK  to "X.X.|X.X.|X.X.|X.X.",
                SNARE to ".X.X|.X.X|.X.X|.X.X",
                RIDE  to "X.X.|X.X.|X.X.|X.X."
            ),
            varB = fromAscii(16,
                KICK  to "XXXX|XXXX|XXXX|XXXX",
                SNARE to "X.X.|X.X.|X.X.|X.X.",
                CRASH to "X...|X...|X...|X..."
            ),
            fill = fromAscii(16,
                KICK  to "XXXX|....|XXXX|....",
                HIGH_TOM to "....|XXXX|....|....",
                LOW_TOM  to "....|....|....|XXXX"
            )
        ),
        DrumPreset(
            id = "metal_groove",
            name = "Groove Metal Stomp",
            genre = DrumGenre.METAL,
            bpm = 112,
            recommendedKit = BuiltInKit.ROCK_METAL,
            description = "Heavy headbanging mid-tempo riff groove",
            mainA = fromAscii(16,
                KICK  to "X..x|..xx|X...|..x.",
                SNARE to "....|X...|....|X...",
                OHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..xx|X..x|..xx",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|X...|....",
                RIDE  to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X..x|....|....|xx..",
                SNARE to "....|X...|X.X.|....",
                LOW_TOM to "....|....|....|..XX"
            )
        ),

        // ========================== ODD TIME (4) ==========================
        DrumPreset(
            id = "odd_take_five_54",
            name = "Take Five (5/4)",
            genre = DrumGenre.ODD_TIME,
            bpm = 156,
            swing = 0.50f,
            timeSignature = TimeSignature.FIVE_FOUR,
            recommendedKit = BuiltInKit.JAZZ_BRUSH,
            description = "Iconic 3+2 five-four jazz groove",
            mainA = fromAscii(20,
                KICK  to "x...|....|....|x...|....",
                SNARE to "....|X...|....|....|X...",
                CHH   to "....|x...|....|x...|....",
                RIDE  to "X...|X..x|X...|X...|X..x"
            ),
            varB = fromAscii(20,
                KICK  to "x...|....|..g.|x...|....",
                SNARE to "....|X..g|....|....|X..g",
                RIDE  to "X..x|X..x|X..x|X..x|X..x"
            ),
            fill = fromAscii(20,
                KICK  to "x...|....|....|x...|....",
                SNARE to "....|X...|..x.|....|x.XX",
                MID_TOM to "....|....|....|..x.|....",
                RIDE  to "X...|....|....|....|...."
            )
        ),
        DrumPreset(
            id = "odd_prog_rock_78",
            name = "Prog Rock (7/8)",
            genre = DrumGenre.ODD_TIME,
            bpm = 132,
            timeSignature = TimeSignature.SEVEN_EIGHT,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Driving 2+2+3 progressive rock groove in 7/8",
            mainA = fromAscii(14,
                KICK  to "X...|X...|X.x.|..",
                SNARE to "..X.|..X.|....|X.",
                CHH   to "X.x.|X.x.|X.x.|x."
            ),
            varB = fromAscii(14,
                KICK  to "X...|X...|X.x.|..",
                SNARE to "..X.|..X.|....|X.",
                CRASH to "X...|....|X...|..",
                RIDE  to "X.x.|X.x.|X.x.|x."
            ),
            fill = fromAscii(14,
                KICK  to "X...|....|X...|..",
                SNARE to "..X.|x.x.|....|..",
                HIGH_TOM to "....|....|..x.|..",
                MID_TOM  to "....|....|...x|..",
                LOW_TOM  to "....|....|....|XX"
            )
        ),
        DrumPreset(
            id = "odd_balkan_78",
            name = "Balkan Dance (7/8)",
            genre = DrumGenre.ODD_TIME,
            bpm = 148,
            timeSignature = TimeSignature.SEVEN_EIGHT,
            recommendedKit = BuiltInKit.LATIN,
            description = "Lively 3+2+2 asymmetric folk-fusion rhythm",
            mainA = fromAscii(14,
                KICK  to "X...|..X.|..X.|..",
                RIM   to "..x.|....|....|X.",
                LOW_TOM to "X...|....|..x.|..",
                PERC  to "X.x.|x.X.|x.X.|x."
            ),
            varB = fromAscii(14,
                KICK  to "X.x.|..X.|..X.|..",
                SNARE to "....|....|....|X.",
                CHH   to "xxxx|xxxx|xxxx|xx",
                PERC  to "X...|..X.|..X.|.."
            ),
            fill = fromAscii(14,
                KICK  to "X...|....|....|..",
                HIGH_TOM to "..xx|....|....|..",
                MID_TOM  to "....|xx..|....|..",
                LOW_TOM  to "....|..xx|XX..|XX"
            )
        ),
        DrumPreset(
            id = "odd_ballad_68",
            name = "Epic 6/8 Rock Ballad",
            genre = DrumGenre.ODD_TIME,
            bpm = 84,
            timeSignature = TimeSignature.SIX_EIGHT,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Sweeping compound-time 6/8 rock groove",
            mainA = fromAscii(12,
                KICK  to "X...|....|..x.",
                SNARE to "....|..X.|....",
                CHH   to "X.x.|x.X.|x.x."
            ),
            varB = fromAscii(12,
                KICK  to "X..x|....|..x.",
                SNARE to "....|..X.|....",
                CRASH to "X...|....|....",
                RIDE  to "X.x.|x.X.|x.x."
            ),
            fill = fromAscii(12,
                KICK  to "X...|....|....",
                SNARE to "....|..X.|x...",
                HIGH_TOM to "....|....|.x..",
                MID_TOM  to "....|....|..x.",
                LOW_TOM  to "....|....|...X"
            )
        ),

        // ========================== AFRO & R&B (4) ==========================
        DrumPreset(
            id = "afro_log_groove",
            name = "Afrobeats Bounce",
            genre = DrumGenre.AFRO_RNB,
            bpm = 104,
            swing = 0.14f,
            recommendedKit = BuiltInKit.LATIN,
            description = "Infectious Lagos Afrobeats rimshot & shaker pocket",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|..X.|....",
                RIM   to "...X|..X.|...X|..X.",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "g.x.|g.x.|g.x.|g.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|..X.|..x.",
                RIM   to "...X|..X.|...X|..X.",
                CLAP  to "....|X...|....|X...",
                LOW_TOM to "....|....|...x|..x.",
                PERC  to "x.xx|x.xx|x.xx|x.xx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                RIM   to "...X|..X.|....|....",
                HIGH_TOM to "....|....|x.x.|....",
                MID_TOM  to "....|....|....|x.x.",
                LOW_TOM  to "....|....|....|...X"
            )
        ),
        DrumPreset(
            id = "afro_amapiano",
            name = "Amapiano Log Drum",
            genre = DrumGenre.AFRO_RNB,
            bpm = 113,
            swing = 0.12f,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "South African Amapiano shaker drive & deep tom/log bounce",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                RIM   to "...x|..x.|...x|..x.",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                LOW_TOM to "....|..X.|...x|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                LOW_TOM to "X..x|..X.|...x|..X.",
                MID_TOM to "....|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                LOW_TOM to "....|....|X.xx|X.XX"
            )
        ),
        DrumPreset(
            id = "rnb_contemporary",
            name = "Smooth Contemporary R&B",
            genre = DrumGenre.AFRO_RNB,
            bpm = 88,
            swing = 0.22f,
            recommendedKit = BuiltInKit.TR_808,
            description = "Silky slow-jam 808 groove with rimshot & finger-snap clap",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|....|..x.",
                CLAP  to "....|X...|....|X...",
                RIM   to "....|....|..g.|....",
                CHH   to "x.x.|x.x.|x.xx|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..x.|...x|....",
                SNARE to "....|x...|....|x...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.xx|x.x.|x.xx|x.x.",
                OHH   to "....|....|....|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                CLAP  to "....|X...|....|....",
                SNARE to "....|....|..gg|x.X."
            )
        ),
        DrumPreset(
            id = "afro_dancehall",
            name = "Dancehall Riddim",
            genre = DrumGenre.AFRO_RNB,
            bpm = 102,
            recommendedKit = BuiltInKit.TR_808,
            description = "Punchy Jamaican dancehall riddim with rim & sub kick",
            mainA = fromAscii(16,
                KICK  to "X..x|..X.|X..x|....",
                SNARE to "....|....|..X.|....",
                RIM   to "....|..x.|....|..x.",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|..X.|X..x|..x.",
                SNARE to "....|....|..X.|....",
                CLAP  to "....|....|..X.|....",
                CHH   to "x.xx|x.xx|x.xx|x.xx"
            ),
            fill = fromAscii(16,
                KICK  to "X..x|....|....|....",
                SNARE to "....|..x.|X.x.|XXXX"
            )
        )
    )
}
