package com.cowdrums

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.cowdrums.model.DrumInstrument
import com.cowdrums.ui.components.KitSoundFontSheet
import com.cowdrums.ui.components.MidiBluetoothDialog
import com.cowdrums.ui.components.TransportBar
import com.cowdrums.ui.screens.BassScreen
import com.cowdrums.ui.screens.MixerScreen
import com.cowdrums.ui.screens.PerformScreen
import com.cowdrums.ui.screens.PresetsScreen
import com.cowdrums.ui.screens.SequencerScreen
import com.cowdrums.ui.screens.SongScreen
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.ui.theme.CowDrumsTheme
import com.cowdrums.viewmodel.DrumMachineViewModel
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private enum class CowMode(val title: String, val icon: ImageVector) {
    PRESETS("Presets", Icons.Default.LibraryMusic),
    PADS("Pads", Icons.Default.Apps),
    SEQUENCER("Sequencer", Icons.Default.GridOn),
    BASS("Bass", Icons.Default.Piano),
    SONG("Song", Icons.AutoMirrored.Filled.QueueMusic)
}

fun main() = application {
    val vm = remember { DrumMachineViewModel() }
    val windowState = rememberWindowState(width = 1100.dp, height = 740.dp)
    val windowIcon = remember {
        runCatching {
            val bytes = vm.javaClass.getResourceAsStream("/icon/cowdrums_icon.png")?.use { it.readBytes() }
            bytes?.let { BitmapPainter(SkiaImage.makeFromEncoded(it).toComposeImageBitmap()) }
        }.getOrNull()
    }

    DisposableEffect(Unit) {
        onDispose { vm.dispose() }
    }

    Window(
        onCloseRequest = {
            vm.dispose()
            exitApplication()
        },
        title = "CowDrums Desktop",
        icon = windowIcon,
        state = windowState,
        onPreviewKeyEvent = { event ->
            if (event.type == KeyEventType.KeyDown) {
                when (event.key) {
                    Key.Spacebar -> {
                        vm.togglePlay()
                        true
                    }
                    Key.F -> {
                        vm.triggerFill()
                        true
                    }
                    Key.One -> { vm.triggerPad(DrumInstrument.KICK); true }
                    Key.Two -> { vm.triggerPad(DrumInstrument.SNARE); true }
                    Key.Three -> { vm.triggerPad(DrumInstrument.CHH); true }
                    Key.Four -> { vm.triggerPad(DrumInstrument.OHH); true }
                    Key.Five -> { vm.triggerPad(DrumInstrument.HIGH_TOM); true }
                    Key.Six -> { vm.triggerPad(DrumInstrument.MID_TOM); true }
                    Key.Seven -> { vm.triggerPad(DrumInstrument.LOW_TOM); true }
                    Key.Eight -> { vm.triggerPad(DrumInstrument.RIDE); true }
                    Key.Nine -> { vm.triggerPad(DrumInstrument.CRASH); true }
                    else -> false
                }
            } else false
        }
    ) {
        CowDrumsTheme {
            CowDrumsDesktopApp(vm)
        }
    }
}

@Composable
private fun CowDrumsDesktopApp(vm: DrumMachineViewModel) {
    val state by vm.uiState.collectAsState()
    var activeMode by remember { mutableStateOf(CowMode.PRESETS) }
    var showMixer by remember { mutableStateOf(false) }
    var showKitSheet by remember { mutableStateOf(false) }
    var showMidiDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            vm.clearStatusMessage()
        }
    }

    val headerTitle = when {
        showMixer -> "Mixer"
        activeMode == CowMode.SONG -> "Song"
        activeMode == CowMode.PRESETS -> "Presets"
        else -> state.activePreset.name
    }

    Scaffold(
        containerColor = CowColors.BgDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(CowColors.BgDark)) {
                TransportBar(
                    title = headerTitle,
                    showBackArrow = showMixer || activeMode != CowMode.PRESETS,
                    onBackClick = {
                        if (showMixer) showMixer = false else activeMode = CowMode.PRESETS
                    },
                    state = state,
                    onTogglePlay = vm::togglePlay,
                    onBpmChange = vm::setBpm,
                    onTapTempo = vm::tapTempo,
                    onSwingChange = vm::setSwing,
                    onOpenKitPicker = { showKitSheet = true },
                    onOpenMidiDialog = {
                        vm.refreshMidiDevices()
                        showMidiDialog = true
                    },
                    onToggleMixer = { showMixer = !showMixer },
                    mixerOpen = showMixer,
                    onExportWav = {
                        val slug = state.activePreset.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                        pickSaveFile("Export WAV Audio", "${slug}_${state.bpm}bpm.wav", "wav")?.let {
                            vm.exportActivePatternWav(it)
                        }
                    },
                    onExportMidi = {
                        val slug = state.activePreset.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                        pickSaveFile("Export Standard MIDI", "${slug}_${state.bpm}bpm.mid", "mid")?.let {
                            vm.exportActivePatternMidi(it)
                        }
                    },
                    onImportSf2 = {
                        pickOpenSf2File()?.let { vm.importSoundFont(it) }
                    },
                    onInstallVst3 = vm::installBundledVst3ToDaw
                )

                if (!showMixer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CowColors.BgDark)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CowMode.entries.forEach { mode ->
                            val selected = activeMode == mode
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(width = 48.dp, height = 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) CowColors.AccentOrange else Color.Transparent)
                                    .clickable { activeMode = mode }
                            ) {
                                Icon(
                                    imageVector = mode.icon,
                                    contentDescription = mode.title,
                                    tint = if (selected) Color.White else CowColors.TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Text(
                            text = "Right-click pad = Sound/MIDI/Pattern  ·  Space = Play  ·  F = Fill  ·  1–9 = Pads",
                            color = CowColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showMixer) {
                MixerScreen(
                    state = state,
                    onUpdateChannel = vm::updateChannelMix,
                    onAudition = { inst -> vm.triggerPad(inst, 0.9f) },
                    onResetMixer = vm::resetMixer,
                    onOpenKitPicker = { showKitSheet = true }
                )
            } else {
                when (activeMode) {
                    CowMode.PRESETS -> PresetsScreen(
                        state = state,
                        onSelectPreset = vm::selectPreset,
                        onTogglePlay = vm::togglePlay,
                        onSelectSlot = vm::selectSlot,
                        onTriggerFill = vm::triggerFill,
                        onToggleAutoFill = vm::toggleAutoFill,
                        onAddToSong = { preset -> vm.addSongSection(preset) },
                        onDeleteCustomPreset = vm::deleteCustomPreset
                    )
                    CowMode.PADS -> PerformScreen(
                        state = state,
                        onTriggerPad = vm::triggerPad,
                        onSelectSlot = vm::selectSlot,
                        onTriggerFill = vm::triggerFill,
                        onToggleMetronome = vm::toggleMetronome,
                        onToggleLiveRecord = vm::toggleLiveRecord,
                        onClearSlot = vm::clearActiveSlot,
                        onPrevKit = { vm.stepBuiltInKit(-1) },
                        onNextKit = { vm.stepBuiltInKit(1) },
                        onOpenKitPicker = { showKitSheet = true },
                        onSelectPadGmSound = vm::setPadGmSound,
                        onSelectPadKitOverride = vm::setPadKitOverride,
                        onPickCustomWavForPad = { inst ->
                            pickOpenWavFile()?.let { vm.loadCustomWavForPad(inst, it) }
                        },
                        onResetPadSound = vm::resetPadSound,
                        onSetPadChokeGroup = vm::setPadChokeGroup,
                        onStartMidiLearn = vm::startMidiLearn,
                        onCancelMidiLearn = vm::cancelMidiLearn,
                        onResetPadMidiNote = { inst -> vm.setPadMidiInputNote(inst, inst.gmNote) },
                        onSetPadNoteRepeat = vm::setPadNoteRepeat,
                        onClearPadRow = vm::clearPadInActiveSlot,
                        onFillPadEveryN = vm::fillPadEveryNSteps,
                        onHumanizePadRow = vm::humanizePadInActiveSlot
                    )
                    CowMode.SEQUENCER -> SequencerScreen(
                        state = state,
                        onSelectSlot = vm::selectSlot,
                        onCycleStep = vm::cycleStep,
                        onAuditionInstrument = { inst -> vm.triggerPad(inst, 0.9f) },
                        onClearSlot = vm::clearActiveSlot,
                        onToggleBarLength = vm::toggleBarLength,
                        onSetTimeSignature = vm::setTimeSignature,
                        onSaveCustomPreset = vm::saveCurrentAsCustomPreset
                    )
                    CowMode.BASS -> BassScreen(
                        state = state,
                        onTriggerBassNote = vm::triggerBassNote,
                        onSelectBassPreset = vm::selectBassPreset,
                        onStepBassPreset = vm::stepBassPreset,
                        onStepBassOctave = vm::stepBassOctave,
                        onBassVolumeChange = vm::setBassVolume,
                        onClearBass = vm::clearBassPattern,
                        onToggleMetronome = vm::toggleMetronome,
                        onToggleLiveRecord = vm::toggleLiveRecord
                    )
                    CowMode.SONG -> SongScreen(
                        state = state,
                        onPlaySong = vm::playSong,
                        onStopSong = vm::stopPlayback,
                        onAddCurrentPreset = { vm.addSongSection(state.activePreset, state.selectedSlot) },
                        onUpdateSection = vm::updateSongSection,
                        onRemoveSection = vm::removeSongSection,
                        onMoveSection = vm::moveSongSection
                    )
                }
            }
        }
    }

    if (showKitSheet) {
        KitSoundFontSheet(
            state = state,
            onDismiss = { showKitSheet = false },
            onSelectBuiltInKit = vm::selectBuiltInKit,
            onImportSoundFontClick = {
                pickOpenSf2File()?.let { vm.importSoundFont(it) }
            },
            onSelectSoundFontPreset = vm::selectSoundFontPreset,
            onDeleteSoundFont = vm::deleteSoundFont,
            onToggleLockKit = vm::toggleLockKit
        )
    }

    if (showMidiDialog) {
        MidiBluetoothDialog(
            devices = state.midiDevices,
            lastMidiActivity = state.lastMidiActivity,
            onRefresh = vm::refreshMidiDevices,
            onToggleDevice = vm::toggleMidiDevice,
            onOpenBluetoothSettings = vm::openBluetoothMidiSettings,
            onDismiss = { showMidiDialog = false }
        )
    }
}

private fun pickOpenSf2File(): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = "Select SoundFont 2 (.sf2) File"
        fileFilter = FileNameExtensionFilter("SoundFont 2 (*.sf2)", "sf2", "SF2")
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}

private fun pickOpenWavFile(): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = "Select Custom Drum Sample (.wav)"
        fileFilter = FileNameExtensionFilter("WAV Audio (*.wav)", "wav", "WAV")
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}

private fun pickSaveFile(title: String, suggestedName: String, ext: String): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = title
        selectedFile = File(System.getProperty("user.home"), suggestedName)
        fileFilter = FileNameExtensionFilter("${ext.uppercase()} files (*.$ext)", ext)
    }
    if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return null
    val chosen = chooser.selectedFile ?: return null
    return if (chosen.name.lowercase().endsWith(".$ext")) chosen else File(chosen.parentFile, "${chosen.name}.$ext")
}
