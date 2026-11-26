package com.cowdrums

import com.cowdrums.audio.AudioMidiExporter
import com.cowdrums.audio.BuiltInKitSynthesizer
import com.cowdrums.audio.SoundFont2Parser
import com.cowdrums.data.PresetLibrary
import com.cowdrums.model.BassPreset
import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.ChannelMix
import com.cowdrums.model.DrumInstrument
import com.cowdrums.model.PatternSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.math.abs

class DesktopPresetAndAudioTest {

    @Test
    fun all95PresetsAreUniqueAndCategorized() {
        val presets = PresetLibrary.builtInPresets
        assertEquals("Expected 95 built-in presets", 95, presets.size)
        assertEquals("IDs must be unique", presets.size, presets.map { it.id }.toSet().size)
        for (p in presets) {
            assertTrue("Missing subcategory on ${p.id}", p.subcategory.isNotBlank())
            assertTrue("Invalid BPM on ${p.id}", p.bpm in 40..240)
            assertTrue("Empty mainA on ${p.id}", p.mainA.rows.isNotEmpty())
            assertTrue("Empty varB on ${p.id}", p.varB.rows.isNotEmpty())
            assertTrue("Empty fill on ${p.id}", p.fill.rows.isNotEmpty())
        }
    }

    @Test
    fun bundledSf2ResourceLoadsAll10DrumKitsAnd8BassPresets() {
        val stream = javaClass.getResourceAsStream("/soundfonts/GeneralUser_GS_Drums.sf2")
        assertNotNull("Bundled SF2 resource missing from classpath", stream)
        val bytes = stream!!.use { it.readBytes() }

        val presets = SoundFont2Parser.listPresets(bytes)
        assertEquals(18, presets.size)

        for (kit in BuiltInKit.entries) {
            val samples = SoundFont2Parser.loadKitSamples(bytes, bank = 128, preset = kit.sf2Preset, applyLoFi = kit.applyLoFiFilter)
            assertEquals(DrumInstrument.ALL.size, samples.size)
            for (inst in DrumInstrument.ALL) {
                val pcm = samples[inst]!!
                assertTrue("Too short: $kit/$inst", pcm.size > 500)
                assertTrue("Silent: $kit/$inst", pcm.maxOf { abs(it) } > 0.2f)
            }
        }

        for (bp in BassPreset.entries) {
            val notes = SoundFont2Parser.loadBassNotes(bytes, bank = bp.bank, preset = bp.gmProgram, midiRange = 36..47)
            assertEquals(12, notes.size)
            assertTrue("Bass $bp C2 too short", notes[36]!!.size > 2000)
        }
    }

    @Test
    fun wavAndMidiExportWorkOnDesktop() {
        val preset = PresetLibrary.builtInPresets.first()
        val kit = BuiltInKitSynthesizer.renderKit(BuiltInKit.ACOUSTIC)
        val mixer = DrumInstrument.ALL.associateWith { ChannelMix() }

        val wavOut = ByteArrayOutputStream()
        AudioMidiExporter.exportPatternToWav(wavOut, preset, PatternSlot.MAIN_A, preset.bpm, preset.swing, 1, kit, mixer)
        val wav = wavOut.toByteArray()
        assertEquals("RIFF", String(wav, 0, 4, Charsets.US_ASCII))
        assertEquals("WAVE", String(wav, 8, 4, Charsets.US_ASCII))

        val midOut = ByteArrayOutputStream()
        AudioMidiExporter.exportPatternToMidi(midOut, preset, PatternSlot.MAIN_A, preset.bpm, preset.swing, 2)
        val mid = midOut.toByteArray()
        assertEquals("MThd", String(mid, 0, 4, Charsets.US_ASCII))
        assertEquals("MTrk", String(mid, 14, 4, Charsets.US_ASCII))
    }
}
