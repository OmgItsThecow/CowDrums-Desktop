package com.cowdrums.data

import com.cowdrums.model.DrumGenre
import com.cowdrums.model.DrumPreset

/**
 * Built-in catalog of 95 handcrafted, ready-to-play drum presets (285 patterns total: Main A, Var B, Fill),
 * cleanly organized into top-level Genres and Subcategories (e.g., Rock -> Indie Rock, Post-Punk, Metal;
 * Indie & Pop -> Indie Pop, Dream Pop, Synthwave, New Wave, Pop & Disco; etc.).
 */
object PresetLibrary {
    val builtInPresets: List<DrumPreset> =
        (PresetLibraryPart1.presets +
            PresetLibraryPart2.presets +
            PresetLibraryPart3.presets +
            PresetLibraryPart4.presets)
            .map(::normalizeCategory)

    private fun normalizeCategory(p: DrumPreset): DrumPreset {
        if (p.subcategory.isNotBlank()) return p

        return when {
            // ROCK -> Indie Rock / Post-Punk / Punk & Grunge / Metal / Classic & Hard
            p.id.startsWith("indie_") -> p.copy(genre = DrumGenre.ROCK, subcategory = "Indie Rock")
            p.id.startsWith("postpunk_") -> p.copy(genre = DrumGenre.ROCK, subcategory = "Post-Punk")
            p.id.startsWith("metal_") -> p.copy(genre = DrumGenre.ROCK, subcategory = "Metal")
            p.id in setOf("rock_punk_fast", "rock_grunge_halftime") ->
                p.copy(genre = DrumGenre.ROCK, subcategory = "Punk & Grunge")
            p.genre == DrumGenre.ROCK -> p.copy(genre = DrumGenre.ROCK, subcategory = "Classic & Hard")

            // INDIE & POP -> Indie Pop / Dream Pop / Synthwave / New Wave / Pop & Disco
            p.id.startsWith("newwave_") -> p.copy(genre = DrumGenre.POP, subcategory = "New Wave")
            p.id == "pop_synth_80s" -> p.copy(genre = DrumGenre.POP, subcategory = "Synthwave")
            p.id == "pop_indie_dance" -> p.copy(genre = DrumGenre.POP, subcategory = "Indie Pop")
            p.genre == DrumGenre.POP -> p.copy(genre = DrumGenre.POP, subcategory = "Pop & Disco")

            // ELECTRONIC -> Techno / Psytrance / Trance / House / DnB & Bass
            p.id.startsWith("psytrance_") -> p.copy(genre = DrumGenre.EDM, subcategory = "Psytrance")
            p.id.startsWith("techno_") || p.id == "edm_peak_techno" ->
                p.copy(genre = DrumGenre.EDM, subcategory = "Techno")
            p.id.startsWith("trance_") || p.id == "edm_trance_driving" ->
                p.copy(genre = DrumGenre.EDM, subcategory = "Trance")
            p.id in setOf("edm_deep_house", "edm_tech_house") ->
                p.copy(genre = DrumGenre.EDM, subcategory = "House")
            p.genre == DrumGenre.EDM -> p.copy(genre = DrumGenre.EDM, subcategory = "DnB & Bass")

            // RAP & HIP-HOP -> Boom-Bap & Rap / Trap & Drill / Phonk & Bounce / Lo-Fi
            p.id in setOf("hiphop_modern_trap", "hiphop_uk_drill") ->
                p.copy(genre = DrumGenre.HIP_HOP, subcategory = "Trap & Drill")
            p.id in setOf("rap_memphis_phonk", "rap_southern_bounce", "hiphop_electro_808") ->
                p.copy(genre = DrumGenre.HIP_HOP, subcategory = "Phonk & Bounce")
            p.id == "hiphop_lofi_chill" ->
                p.copy(genre = DrumGenre.HIP_HOP, subcategory = "Lo-Fi")
            p.genre == DrumGenre.HIP_HOP ->
                p.copy(genre = DrumGenre.HIP_HOP, subcategory = "Rap & Boom-Bap")

            // REGGAE & SKA -> Reggae & Dub / Ska / Ska Fusion
            p.id.startsWith("reggae_") || p.id == "latin_reggae_onedrop" ->
                p.copy(genre = DrumGenre.REGGAE_SKA, subcategory = "Reggae & Dub")
            p.id in setOf("ska_fusion_groove", "ska_punk_thirdwave") ->
                p.copy(genre = DrumGenre.REGGAE_SKA, subcategory = "Ska Fusion")
            p.genre == DrumGenre.REGGAE_SKA ->
                p.copy(genre = DrumGenre.REGGAE_SKA, subcategory = "Ska")

            // FUNK & R&B -> Funk & Soul / Afrobeats & R&B
            p.genre == DrumGenre.AFRO_RNB ->
                p.copy(genre = DrumGenre.FUNK, subcategory = "Afrobeats & R&B")
            p.genre == DrumGenre.FUNK ->
                p.copy(genre = DrumGenre.FUNK, subcategory = "Funk & Soul")

            // JAZZ & BLUES -> Jazz / Blues
            p.id in setOf("jazz_slow_blues", "jazz_chicago_shuffle") ->
                p.copy(genre = DrumGenre.JAZZ, subcategory = "Blues")
            p.genre == DrumGenre.JAZZ ->
                p.copy(genre = DrumGenre.JAZZ, subcategory = "Jazz")

            // LATIN -> Brazilian / Latin & Caribbean
            p.id in setOf("latin_bossa_nova", "latin_samba") ->
                p.copy(genre = DrumGenre.LATIN, subcategory = "Brazilian")
            p.genre == DrumGenre.LATIN ->
                p.copy(genre = DrumGenre.LATIN, subcategory = "Latin & Caribbean")

            // ODD TIME -> 5/4 & 7/8 / 6/8
            p.id == "odd_ballad_68" ->
                p.copy(genre = DrumGenre.ODD_TIME, subcategory = "6/8")
            p.genre == DrumGenre.ODD_TIME ->
                p.copy(genre = DrumGenre.ODD_TIME, subcategory = "5/4 & 7/8")

            else -> p.copy(subcategory = p.genre.label)
        }
    }
}
