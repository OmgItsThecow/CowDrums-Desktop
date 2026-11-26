package com.cowdrums.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cowdrums.model.SoundSource
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.viewmodel.DrumMachineUiState

@Composable
fun TransportBar(
    title: String,
    showBackArrow: Boolean,
    onBackClick: () -> Unit,
    state: DrumMachineUiState,
    onTogglePlay: () -> Unit,
    onBpmChange: (Int) -> Unit,
    onTapTempo: () -> Unit,
    onSwingChange: (Float) -> Unit,
    onOpenKitPicker: () -> Unit,
    onToggleMixer: () -> Unit,
    mixerOpen: Boolean,
    onExportWav: () -> Unit,
    onExportMidi: () -> Unit,
    onImportSf2: () -> Unit,
    onInstallVst3: () -> Unit
) {
    var showBpmDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    Surface(
        color = CowColors.SurfacePanel,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackArrow) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CowColors.TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    color = CowColors.TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(30.dp)
                        .background(CowColors.BorderSubtle)
                )

                IconButton(onClick = onTogglePlay, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Stop" else "Play",
                        tint = if (state.isPlaying) CowColors.AccentOrange else CowColors.TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showBpmDialog = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "BPM",
                        color = CowColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 11.sp
                    )
                    Text(
                        text = "${state.bpm}",
                        color = CowColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 15.sp
                    )
                }

                val sfLabel = when (val src = state.soundSource) {
                    is SoundSource.BuiltIn -> "Default (${src.kit.displayName})"
                    is SoundSource.SoundFont -> src.displayName
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenKitPicker() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (state.isLoadingSound) "Loading..." else sfLabel,
                        color = CowColors.TextPrimary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onToggleMixer, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Mixer",
                        tint = if (mixerOpen) CowColors.AccentOrange else CowColors.TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showOverflowMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = CowColors.TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false },
                        containerColor = CowColors.SurfaceElevated
                    ) {
                        DropdownMenuItem(
                            text = { Text("Load SoundFont (.sf2)", color = CowColors.TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onImportSf2()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Choose Drum Kit / SF2", color = CowColors.TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onOpenKitPicker()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export WAV Audio", color = CowColors.TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onExportWav()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export MIDI (.mid)", color = CowColors.TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onExportMidi()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Install VST3 Plugin to DAW", color = CowColors.AccentOrange, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                showOverflowMenu = false
                                onInstallVst3()
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = CowColors.BorderSubtle, thickness = 1.dp)
        }
    }

    if (showBpmDialog) {
        AlertDialog(
            onDismissRequest = { showBpmDialog = false },
            containerColor = CowColors.SurfacePanel,
            title = { Text("Tempo & Swing", color = CowColors.TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onBpmChange(state.bpm - 1) }) {
                            Icon(Icons.Default.Remove, contentDescription = "-1", tint = CowColors.TextPrimary)
                        }
                        Text(
                            text = "${state.bpm} BPM",
                            color = CowColors.AccentOrange,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { onBpmChange(state.bpm + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = "+1", tint = CowColors.TextPrimary)
                        }
                    }

                    Slider(
                        value = state.bpm.toFloat(),
                        onValueChange = { onBpmChange(it.toInt()) },
                        valueRange = 40f..240f,
                        colors = SliderDefaults.colors(
                            thumbColor = CowColors.AccentOrange,
                            activeTrackColor = CowColors.AccentOrange,
                            inactiveTrackColor = CowColors.BorderSubtle
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Swing: ${(state.swing * 100).toInt()}%", color = CowColors.TextSecondary, fontSize = 13.sp)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CowColors.AccentOrange)
                                .clickable { onTapTempo() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("TAP TEMPO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Slider(
                        value = state.swing,
                        onValueChange = onSwingChange,
                        valueRange = 0f..0.70f,
                        colors = SliderDefaults.colors(
                            thumbColor = CowColors.AccentOrange,
                            activeTrackColor = CowColors.AccentOrange,
                            inactiveTrackColor = CowColors.BorderSubtle
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBpmDialog = false }) {
                    Text("DONE", color = CowColors.AccentOrange, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
