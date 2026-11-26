package com.cowdrums.data

import com.cowdrums.audio.SoundFont2Parser
import com.cowdrums.model.DrumPreset
import com.cowdrums.model.LoadedSoundFont
import com.cowdrums.model.SongSection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Desktop persistence repository storing user presets, songs, and imported SoundFonts
 * under ~/.cowdrums/ on both Windows and Linux.
 */
class CowDrumsRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val appDir: File
        get() = File(System.getProperty("user.home"), ".cowdrums").apply { mkdirs() }

    private val customPresetsFile: File
        get() = File(appDir, "custom_presets.json")

    private val songFile: File
        get() = File(appDir, "song_sections.json")

    private val soundFontsDir: File
        get() = File(appDir, "soundfonts").apply { mkdirs() }

    @Volatile
    private var bundledSf2Cache: ByteArray? = null

    fun loadBundledSf2Bytes(): ByteArray? {
        bundledSf2Cache?.let { return it }
        return runCatching {
            javaClass.getResourceAsStream("/soundfonts/GeneralUser_GS_Drums.sf2")?.use { it.readBytes() }
        }.getOrNull()?.also { bundledSf2Cache = it }
    }

    fun loadCustomPresets(): List<DrumPreset> {
        if (!customPresetsFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<DrumPreset>>(customPresetsFile.readText())
        }.getOrDefault(emptyList())
    }

    fun saveCustomPresets(presets: List<DrumPreset>) {
        runCatching {
            customPresetsFile.writeText(json.encodeToString(presets))
        }
    }

    fun loadSongSections(): List<SongSection> {
        if (!songFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<SongSection>>(songFile.readText())
        }.getOrDefault(emptyList())
    }

    fun saveSongSections(sections: List<SongSection>) {
        runCatching {
            songFile.writeText(json.encodeToString(sections))
        }
    }

    fun listLoadedSoundFonts(): List<LoadedSoundFont> {
        val files = soundFontsDir.listFiles { f ->
            f.isFile && f.name.lowercase().endsWith(".sf2")
        } ?: return emptyList()

        return files.sortedBy { it.name.lowercase() }.mapNotNull { file ->
            val presets = SoundFont2Parser.listPresets(file)
            if (presets.isEmpty()) null
            else LoadedSoundFont(
                fileName = file.name,
                displayName = file.nameWithoutExtension,
                filePath = file.absolutePath,
                presets = presets
            )
        }
    }

    fun importSoundFont(sourceFile: File): Result<LoadedSoundFont> {
        return runCatching {
            if (!sourceFile.exists() || !sourceFile.canRead()) {
                error("Could not read selected SoundFont file.")
            }
            val safeName = sourceFile.name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .let { if (it.lowercase().endsWith(".sf2")) it else "$it.sf2" }

            val target = File(soundFontsDir, safeName)
            sourceFile.copyTo(target, overwrite = true)

            val presets = SoundFont2Parser.listPresets(target)
            if (presets.isEmpty()) {
                target.delete()
                error("File is not a valid SoundFont 2 (.sf2) bank or contains no presets.")
            }

            LoadedSoundFont(
                fileName = target.name,
                displayName = target.nameWithoutExtension,
                filePath = target.absolutePath,
                presets = presets
            )
        }
    }

    fun deleteSoundFont(fileName: String) {
        val f = File(soundFontsDir, fileName)
        if (f.exists()) f.delete()
    }

    fun soundFontFile(fileName: String): File = File(soundFontsDir, fileName)
}
