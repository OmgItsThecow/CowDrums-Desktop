package com.cowdrums.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Exports all 95 built-in presets (285 patterns) to vst3/src/presets.json
 * so the native C++ VST3 plugin stays 100% in sync with CowDrums Desktop & Android.
 */
fun main(args: Array<String>) {
    val outPath = args.firstOrNull() ?: "vst3/src/presets.json"
    val json = Json { prettyPrint = false; encodeDefaults = true }
    val outFile = File(outPath)
    outFile.parentFile?.mkdirs()
    outFile.writeText(json.encodeToString(PresetLibrary.builtInPresets))
    println("Exported ${PresetLibrary.builtInPresets.size} presets to ${outFile.absolutePath} (${outFile.length()} bytes)")
}
