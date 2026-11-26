package com.cowdrums.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cowdrums.model.BassPreset
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.viewmodel.DrumMachineUiState
import kotlinx.coroutines.delay

/**
 * Bass keyboard & preset section modeled directly after SF Drum Machine:
 * - Framed charcoal header: < C01 > Octave | < 32. ACOUSTIC BASS > Bass Preset (tap for full list)
 * - Tool strip: Trash (clear bassline), /16, Metronome, Rec dot
 * - Vivid orange Bass Volume slider
 * - Clean playable 1-octave piano keyboard (C..B) with live bassline recording
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BassScreen(
    state: DrumMachineUiState,
    onTriggerBassNote: (Int) -> Unit,
    onSelectBassPreset: (BassPreset) -> Unit,
    onStepBassPreset: (Int) -> Unit,
    onStepBassOctave: (Int) -> Unit,
    onBassVolumeChange: (Float) -> Unit,
    onClearBass: () -> Unit,
    onToggleMetronome: () -> Unit,
    onToggleLiveRecord: () -> Unit
) {
    var showBassPresetSheet by remember { mutableStateOf(false) }
    var lastPressedMidi by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(lastPressedMidi) {
        if (lastPressedMidi != null) {
            delay(140)
            lastPressedMidi = null
        }
    }

    val activePattern = state.activePreset.patternFor(state.playingSlot)
    val baseMidi = 12 * (state.bassOctave + 1) // Octave 1 -> C1 (24), Octave 2 -> C2 (36), Octave 3 -> C3 (48)
    val seqPlayingMidi = if (state.isPlaying && state.currentStep >= 0) {
        activePattern.bassNotes[state.currentStep]
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CowColors.BgDark)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Framed SF Drum Machine Bass Control Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CowColors.SurfaceCard)
                .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            // Row 1: < C01 >  |  < 32. ACOUSTIC BASS >
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Octave selector
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onStepBassOctave(-1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Octave Down", tint = CowColors.TextSecondary)
                    }
                    Text(
                        text = "C0${state.bassOctave}",
                        color = CowColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { onStepBassOctave(1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Octave Up", tint = CowColors.TextSecondary)
                    }
                }

                // Bass preset selector
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { onStepBassPreset(-1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Bass", tint = CowColors.TextSecondary)
                    }
                    Text(
                        text = "${state.bassPreset.gmProgram}. ${state.bassPreset.displayName}",
                        color = CowColors.AccentOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showBassPresetSheet = true }
                            .padding(vertical = 6.dp)
                    )
                    IconButton(onClick = { onStepBassPreset(1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Bass", tint = CowColors.TextSecondary)
                    }
                }
            }

            HorizontalDivider(color = CowColors.BorderSubtle, thickness = 1.dp)

            // Row 2: Tool strip (Trash | /16 | Metronome | Rec Dot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onClearBass, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Bassline", tint = CowColors.TextSecondary, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "/${activePattern.stepCount}",
                    color = CowColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onToggleMetronome, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = "Metronome",
                        tint = if (state.metronomeEnabled) CowColors.AccentOrange else CowColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onToggleLiveRecord() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(if (state.liveRecordEnabled) CowColors.AccentRed else CowColors.TextSecondary)
                    )
                }
            }

            HorizontalDivider(color = CowColors.BorderSubtle, thickness = 1.dp)

            // Row 3: Vivid Orange Bass Volume Slider (exact Screenshot 3!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Slider(
                    value = state.bassVolume,
                    onValueChange = onBassVolumeChange,
                    valueRange = 0f..1.25f,
                    colors = SliderDefaults.colors(
                        thumbColor = CowColors.AccentOrange,
                        activeTrackColor = CowColors.AccentOrange,
                        inactiveTrackColor = CowColors.BorderSubtle
                    )
                )
            }
        }

        // Playable 1-Octave Piano Keyboard (C..B) matching Screenshot 3
        val whiteSemitones = listOf(0 to "C", 2 to "D", 4 to "E", 5 to "F", 7 to "G", 9 to "A", 11 to "B")
        val blackKeys = listOf(
            Triple(1, "C#", 0.68f),
            Triple(3, "Eb", 1.72f),
            Triple(6, "F#", 3.66f),
            Triple(8, "Ab", 4.68f),
            Triple(10, "Bb", 5.72f)
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CowColors.SurfaceCard)
        ) {
            val whiteKeyWidth = maxWidth / whiteSemitones.size

            // White keys row
            Row(modifier = Modifier.fillMaxSize()) {
                whiteSemitones.forEach { (semi, label) ->
                    val midi = baseMidi + semi
                    val lit = lastPressedMidi == midi || seqPlayingMidi == midi
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 1.dp)
                            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .background(
                                if (lit) CowColors.AccentOrange.copy(alpha = 0.85f)
                                else Color(0xFFECEFF1)
                            )
                            .pointerInput(midi) {
                                detectTapGestures(
                                    onPress = {
                                        lastPressedMidi = midi
                                        onTriggerBassNote(midi)
                                    }
                                )
                            }
                            .padding(bottom = 12.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = label,
                            color = if (lit) Color.White else Color(0xFF212529),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Black keys overlay
            val blackKeyWidth = whiteKeyWidth * 0.64f
            blackKeys.forEach { (semi, label, posUnits) ->
                val midi = baseMidi + semi
                val lit = lastPressedMidi == midi || seqPlayingMidi == midi
                Box(
                    modifier = Modifier
                        .offset(x = whiteKeyWidth * posUnits)
                        .width(blackKeyWidth)
                        .fillMaxHeight(0.58f)
                        .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                        .background(
                            if (lit) {
                                Brush.verticalGradient(listOf(CowColors.AccentOrange, CowColors.AccentOrangeDim))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFF2B2F34), Color(0xFF0A0C0E)))
                            }
                        )
                        .border(
                            1.dp,
                            Color(0xFF000000),
                            RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)
                        )
                        .pointerInput(midi) {
                            detectTapGestures(
                                onPress = {
                                    lastPressedMidi = midi
                                    onTriggerBassNote(midi)
                                }
                            )
                        }
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Recorded bassline hint
        val noteCount = activePattern.bassNotes.size
        Text(
            text = if (state.liveRecordEnabled) {
                "REC ON: Tap keys while playing to record a looping bassline ($noteCount notes)"
            } else {
                "Tap keys to jam along with the drums · Enable ● REC to record ($noteCount notes)"
            },
            color = CowColors.TextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }

    // Flat SF Drum Machine style Bass Preset Picker Sheet ("32. ACOUSTIC BASS    SF:1 BANK:0")
    if (showBassPresetSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showBassPresetSheet = false },
            sheetState = sheetState,
            containerColor = CowColors.SurfacePanel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Bass Presets (SF2 Bank 0)",
                    color = CowColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
                HorizontalDivider(color = CowColors.BorderSubtle)
                LazyColumn {
                    items(BassPreset.entries, key = { it.name }) { bp ->
                        val selected = bp == state.bassPreset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selected) CowColors.SurfaceCard else CowColors.SurfacePanel)
                                .clickable {
                                    onSelectBassPreset(bp)
                                    showBassPresetSheet = false
                                }
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${bp.gmProgram}. ${bp.displayName}",
                                color = if (selected) CowColors.AccentOrange else CowColors.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Text(
                                text = "SF:1   BANK:${bp.bank}",
                                color = CowColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        HorizontalDivider(color = CowColors.BorderSubtle)
                    }
                }
            }
        }
    }
}
