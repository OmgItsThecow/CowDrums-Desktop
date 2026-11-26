package com.cowdrums.data

import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.DrumGenre
import com.cowdrums.model.DrumInstrument.*
import com.cowdrums.model.DrumPreset
import com.cowdrums.model.StepPattern.Companion.fromAscii

/**
 * 16 new handcrafted presets for Indie Pop, Dream Pop, Synthwave, and Indie Rock.
 */
internal object PresetLibraryPart4 {

    val presets: List<DrumPreset> = listOf(
        // ========================== INDIE POP (4) ==========================
        DrumPreset(
            id = "indiepop_bedroom",
            name = "Bedroom Indie Pop",
            genre = DrumGenre.POP,
            subcategory = "Indie Pop",
            bpm = 112,
            swing = 0.14f,
            recommendedKit = BuiltInKit.ROOM,
            description = "Warm Clairo / Boy Pablo laid-back rimshot & gentle hat pocket",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                RIM   to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X..x|....",
                SNARE to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x...",
                OHH   to "....|....|....|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|..g.|x.X.",
                LOW_TOM to "....|....|....|..x."
            )
        ),
        DrumPreset(
            id = "indiepop_jangle",
            name = "Jangle Indie Pop",
            genre = DrumGenre.POP,
            subcategory = "Indie Pop",
            bpm = 134,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Alvvays / The Smiths bright driving indie-pop groove with shaker/perc",
            mainA = fromAscii(16,
                KICK  to "X...|....|X.x.|....",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X.x.",
                PERC  to "..x.|..x.|..x.|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "....|X...|....|X..g",
                RIDE  to "X.x.|X.x.|X.x.|X.x.",
                PERC  to "x.x.|x.x.|x.x.|x.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X...|..x.|X.XX",
                MID_TOM to "....|....|x...|...."
            )
        ),
        DrumPreset(
            id = "indiepop_electro",
            name = "Indie Electro-Pop",
            genre = DrumGenre.POP,
            subcategory = "Indie Pop",
            bpm = 120,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Phoenix / MGMT punchy four-on-the-floor indie electro-pop",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|x...|....|x...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                OHH   to "....|....|..x.|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X..x",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.xx|x.xx|x.xx|x.x.",
                OHH   to "..x.|....|..x.|...X"
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|....|x.x.|XXXX",
                CLAP  to "....|X...|....|X..."
            )
        ),
        DrumPreset(
            id = "indiepop_dance_upbeat",
            name = "Upbeat Indie Dance-Pop",
            genre = DrumGenre.POP,
            subcategory = "Indie Pop",
            bpm = 128,
            recommendedKit = BuiltInKit.ROOM,
            description = "Two Door Cinema Club / Foster the People snappy upbeat indie pulse",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CHH   to "x...|x...|x...|x...",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X..x|X...|X...",
                SNARE to "....|X...|....|X..g",
                CLAP  to "....|x...|....|x...",
                CHH   to "X.x.|X.x.|X.x.|X.x.",
                CRASH to "X...|....|....|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|X...|x.X.|X.XX",
                HIGH_TOM to "....|....|..x.|...."
            )
        ),

        // ========================== DREAM POP (4) ==========================
        DrumPreset(
            id = "dreampop_lush",
            name = "Lush Dream Pop",
            genre = DrumGenre.POP,
            subcategory = "Dream Pop",
            bpm = 102,
            recommendedKit = BuiltInKit.ROOM,
            description = "Beach House slow hypnotic ride & deep reverberant snare groove",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                SNARE to "....|X...|....|X...",
                RIDE  to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|....|X...|..x.",
                SNARE to "....|X..g|....|X...",
                CRASH to "X...|....|....|....",
                RIDE  to "X.x.|x.x.|X.x.|x.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|....|x...",
                MID_TOM to "....|....|x.x.|....",
                LOW_TOM to "....|....|....|..XX"
            )
        ),
        DrumPreset(
            id = "dreampop_cocteau",
            name = "Cocteau 80s Dream Pop",
            genre = DrumGenre.POP,
            subcategory = "Dream Pop",
            bpm = 118,
            recommendedKit = BuiltInKit.TR_808,
            description = "Cocteau Twins / Chromatics shimmering vintage drum-machine dream pop",
            mainA = fromAscii(16,
                KICK  to "X...|....|X..x|....",
                RIM   to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                OHH   to "....|..x.|....|..x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X..x|....",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                CHH   to "xxxx|xxxx|xxxx|xxxx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                RIM   to "....|X...|....|....",
                HIGH_TOM to "....|....|x.x.|....",
                LOW_TOM  to "....|....|....|x.X."
            )
        ),
        DrumPreset(
            id = "dreampop_slowcore",
            name = "Ethereal Slowcore",
            genre = DrumGenre.POP,
            subcategory = "Dream Pop",
            bpm = 82,
            swing = 0.18f,
            recommendedKit = BuiltInKit.BRUSH,
            description = "Cigarettes After Sex / Mazzy Star soft brushed-snare dream sway",
            mainA = fromAscii(16,
                KICK  to "X...|....|..x.|....",
                SNARE to "....|x...|....|x...",
                RIDE  to "x.g.|x.g.|x.g.|x.g."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..g.|..x.|....",
                SNARE to "....|X..g|....|X...",
                RIDE  to "x.x.|x.x.|x.x.|x.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|x...|..g.|x.x.",
                LOW_TOM to "....|....|....|...x"
            )
        ),
        DrumPreset(
            id = "dreampop_shoegaze_swell",
            name = "Hazy Shoegaze Swell",
            genre = DrumGenre.POP,
            subcategory = "Dream Pop",
            bpm = 126,
            recommendedKit = BuiltInKit.ROOM,
            description = "Slowdive / MBV driving floor-tom & cymbal wall-of-sound groove",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X..x|....",
                SNARE to "....|X...|....|X...",
                LOW_TOM to "x.x.|..x.|x.x.|..x.",
                OHH   to "X...|X...|X...|X..."
            ),
            varB = fromAscii(16,
                KICK  to "X.x.|....|X.x.|..x.",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|X...|....",
                RIDE  to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|X.x.|....",
                LOW_TOM to "....|....|....|XXXX"
            )
        ),

        // ========================== SYNTHWAVE (4) ==========================
        DrumPreset(
            id = "synthwave_outrun",
            name = "Outrun Night Drive",
            genre = DrumGenre.POP,
            subcategory = "Synthwave",
            bpm = 118,
            recommendedKit = BuiltInKit.TR_808,
            description = "Kavinsky / The Midnight driving 80s neon highway beat with big tom fills",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                CHH   to "x.x.|x.x.|x.x.|x.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X..x|X...|X...",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "xxxx|xxxx|xxxx|xxxx",
                OHH   to "..x.|....|..x.|...."
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
            id = "synthwave_darksynth",
            name = "Darksynth Cyberpunk",
            genre = DrumGenre.POP,
            subcategory = "Synthwave",
            bpm = 132,
            recommendedKit = BuiltInKit.ELECTRONIC,
            description = "Carpenter Brut / Perturbator heavy pounding industrial synthwave",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X..x|X...",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                OHH   to "..X.|..X.|..X.|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X.x.|X...|X.x.|X..x",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|X...|....",
                CHH   to "xxxx|xxxx|xxxx|xxxx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|X...|X.X.|XXXX",
                LOW_TOM to "....|....|..X.|...."
            )
        ),
        DrumPreset(
            id = "synthwave_chillwave",
            name = "Chillwave Sunset",
            genre = DrumGenre.POP,
            subcategory = "Synthwave",
            bpm = 96,
            swing = 0.16f,
            recommendedKit = BuiltInKit.TR_808,
            description = "Home / Tycho warm nostalgic VHS sunset electronic groove",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                SNARE to "....|X...|....|X...",
                CHH   to "x.x.|x.x.|x.x.|x.x.",
                PERC  to "....|..g.|....|..g."
            ),
            varB = fromAscii(16,
                KICK  to "X..x|....|X...|..x.",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|x...|....|x...",
                CHH   to "x.xx|x.x.|x.xx|x.x.",
                OHH   to "....|....|..x.|...."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|..x.|x.X.",
                HIGH_TOM to "....|....|x...|....",
                LOW_TOM  to "....|....|....|..x."
            )
        ),
        DrumPreset(
            id = "synthwave_retrowave",
            name = "Retrowave Arcade",
            genre = DrumGenre.POP,
            subcategory = "Synthwave",
            bpm = 124,
            recommendedKit = BuiltInKit.TR_909,
            description = "Upbeat 80s arcade retrowave with snappy claps & cowbell accents",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                CLAP  to "....|X...|....|X...",
                CHH   to "x.xx|x.xx|x.xx|x.xx",
                PERC  to "..x.|....|..x.|...."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X...|X..x",
                SNARE to "....|X...|....|X...",
                CLAP  to "....|X...|....|X...",
                OHH   to "..X.|..X.|..X.|..X.",
                PERC  to "x...|..x.|x...|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                HIGH_TOM to "....|....|XX..|....",
                MID_TOM  to "....|....|..XX|....",
                LOW_TOM  to "....|....|....|XXXX"
            )
        ),

        // ========================== INDIE ROCK / GENERAL EXPANSION (4) ==========================
        DrumPreset(
            id = "indie_folk_stomp",
            name = "Indie Folk Stomp",
            genre = DrumGenre.ROCK,
            subcategory = "Indie Rock",
            bpm = 116,
            recommendedKit = BuiltInKit.ACOUSTIC,
            description = "Mumford / Fleet Foxes acoustic kick stomp with rimshot & shaker",
            mainA = fromAscii(16,
                KICK  to "X...|X...|X...|X...",
                RIM   to "....|X...|....|X...",
                PERC  to "x.x.|X.x.|x.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|X...|X..x|X...",
                SNARE to "....|X...|....|X...",
                LOW_TOM to "X...|..x.|X...|..x.",
                PERC  to "xxxx|xxxx|xxxx|xxxx"
            ),
            fill = fromAscii(16,
                KICK  to "X...|X...|....|....",
                SNARE to "....|X...|x.x.|XXXX"
            )
        ),
        DrumPreset(
            id = "indie_post_rock",
            name = "Post-Rock Crescendo",
            genre = DrumGenre.ROCK,
            subcategory = "Indie Rock",
            bpm = 108,
            recommendedKit = BuiltInKit.ROOM,
            description = "Explosions in the Sky / Mogwai rolling floor-tom & cymbal swell",
            mainA = fromAscii(16,
                KICK  to "X...|....|X..x|....",
                SNARE to "....|X..g|....|X..g",
                LOW_TOM to "x.x.|x.x.|x.x.|x.x.",
                RIDE  to "X...|X...|X...|X..."
            ),
            varB = fromAscii(16,
                KICK  to "X.x.|..x.|X.x.|..x.",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|X...|....",
                RIDE  to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X.xx|....|XXXX",
                LOW_TOM to "....|....|XXXX|...."
            )
        ),
        DrumPreset(
            id = "indie_slacker_lofi",
            name = "Slacker Indie Rock",
            genre = DrumGenre.ROCK,
            subcategory = "Indie Rock",
            bpm = 104,
            swing = 0.24f,
            recommendedKit = BuiltInKit.LOFI,
            description = "Mac DeMarco / Pavement swung laid-back lo-fi indie groove",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X...|....",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X..x|....",
                SNARE to "....|X..g|....|X..g",
                CHH   to "X.x.|X.x.|X.x.|X...",
                OHH   to "....|....|....|..x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|....|....",
                SNARE to "....|X...|..x.|x.X.",
                LOW_TOM to "....|....|....|..x."
            )
        ),
        DrumPreset(
            id = "indie_britpop_anthem",
            name = "Britpop Indie Anthem",
            genre = DrumGenre.ROCK,
            subcategory = "Indie Rock",
            bpm = 122,
            recommendedKit = BuiltInKit.ROOM,
            description = "Oasis / Blur big swaggering 90s indie-rock groove with open hats",
            mainA = fromAscii(16,
                KICK  to "X...|..x.|X.x.|....",
                SNARE to "....|X...|....|X...",
                CHH   to "X.x.|X.x.|X.x.|X...",
                OHH   to "....|....|....|..X."
            ),
            varB = fromAscii(16,
                KICK  to "X...|..x.|X.x.|..x.",
                SNARE to "....|X...|....|X...",
                CRASH to "X...|....|....|....",
                OHH   to "X.x.|X.x.|X.x.|X.x."
            ),
            fill = fromAscii(16,
                KICK  to "X...|....|X...|....",
                SNARE to "....|X.x.|X...|....",
                MID_TOM to "....|....|..xx|....",
                LOW_TOM to "....|....|....|XXXX"
            )
        )
    )
}
