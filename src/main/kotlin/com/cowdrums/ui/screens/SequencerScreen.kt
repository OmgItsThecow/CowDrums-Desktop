package com.cowdrums.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cowdrums.model.DrumInstrument
import com.cowdrums.model.PatternSlot
import com.cowdrums.model.StepVelocity
import com.cowdrums.model.TimeSignature
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.viewmodel.DrumMachineUiState

/**
 * Minimalist charcoal + orange step sequencer grid matching SF Drum Machine.
 */
@Composable
fun SequencerScreen(
    state: DrumMachineUiState,
    onSelectSlot: (PatternSlot) -> Unit,
    onCycleStep: (DrumInstrument, Int) -> Unit,
    onAuditionInstrument: (DrumInstrument) -> Unit,
    onClearSlot: () -> Unit,
    onToggleBarLength: () -> Unit,
    onSetTimeSignature: (TimeSignature) -> Unit,
    onSaveCustomPreset: (String) -> Unit
) {
    var showTimeSigMenu by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("${state.activePreset.name} Edit") }

    val pattern = state.activePreset.patternFor(state.selectedSlot)
    val ts = state.activePreset.timeSignature
    val bars = (pattern.stepCount / ts.stepsPerBar).coerceAtLeast(1)
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CowColors.BgDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Compact Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CowColors.SurfaceCard)
                .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SeqPill("A", state.selectedSlot == PatternSlot.MAIN_A) { onSelectSlot(PatternSlot.MAIN_A) }
            SeqPill("B", state.selectedSlot == PatternSlot.VAR_B) { onSelectSlot(PatternSlot.VAR_B) }
            SeqPill("FILL", state.selectedSlot == PatternSlot.FILL) { onSelectSlot(PatternSlot.FILL) }

            Box {
                SeqPill(ts.label, false) { showTimeSigMenu = true }
                DropdownMenu(
                    expanded = showTimeSigMenu,
                    onDismissRequest = { showTimeSigMenu = false },
                    containerColor = CowColors.SurfaceElevated
                ) {
                    TimeSignature.entries.forEach { sig ->
                        DropdownMenuItem(
                            text = { Text("${sig.label} (${sig.stepsPerBar} steps)", color = CowColors.TextPrimary) },
                            onClick = {
                                showTimeSigMenu = false
                                onSetTimeSignature(sig)
                            }
                        )
                    }
                }
            }

            SeqPill("${bars}B", false, onToggleBarLength)

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onClearSlot() }
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", tint = CowColors.TextSecondary, modifier = Modifier.size(19.dp))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        saveName = "${state.activePreset.name} Custom"
                        showSaveDialog = true
                    }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Preset", tint = CowColors.AccentOrange, modifier = Modifier.size(19.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Step Numbers Header
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .width(52.dp)
                    .height(22.dp)
            ) {
                Text("KIT", color = CowColors.TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(hScroll),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (step in 0 until pattern.stepCount) {
                    val isBeatStart = step % ts.stepsPerBeat == 0
                    val isPlayhead = state.isPlaying && state.currentStep == step
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(34.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isPlayhead -> CowColors.AccentOrange
                                    isBeatStart -> CowColors.SurfaceElevated
                                    else -> Color.Transparent
                                }
                            )
                    ) {
                        Text(
                            text = "${step + 1}",
                            color = when {
                                isPlayhead -> Color.White
                                isBeatStart -> CowColors.TextPrimary
                                else -> CowColors.TextMuted
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // 12 Instrument Rows (Clean Monochrome + Orange Velocity Cells)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(vScroll),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DrumInstrument.ALL.forEach { inst ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .width(52.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CowColors.SurfaceCard)
                            .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable { onAuditionInstrument(inst) }
                            .padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = inst.shortName,
                            color = CowColors.TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(hScroll),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (step in 0 until pattern.stepCount) {
                            val vel = pattern.velocityAt(inst, step)
                            val isBeatGroupOdd = ((step / ts.stepsPerBeat) % 2) == 1
                            val isPlayhead = state.isPlaying && state.currentStep == step

                            val cellBg = when (vel) {
                                StepVelocity.ACCENT -> CowColors.AccentOrange
                                StepVelocity.MEDIUM -> CowColors.AccentOrange.copy(alpha = 0.65f)
                                StepVelocity.SOFT -> CowColors.AccentOrange.copy(alpha = 0.30f)
                                else -> if (isBeatGroupOdd) CowColors.SurfaceElevated.copy(alpha = 0.55f) else CowColors.SurfaceCard
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(34.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(cellBg)
                                    .border(
                                        width = if (isPlayhead) 1.5.dp else 1.dp,
                                        color = if (isPlayhead) Color.White else CowColors.BorderSubtle,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                    .clickable { onCycleStep(inst, step) }
                            ) {
                                if (vel == StepVelocity.ACCENT) {
                                    Text("●", color = Color.White, fontSize = 9.sp)
                                } else if (vel == StepVelocity.SOFT) {
                                    Text("○", color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = CowColors.SurfacePanel,
            title = { Text("Save Custom Preset", color = CowColors.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    label = { Text("Preset Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveCustomPreset(saveName)
                        showSaveDialog = false
                    }
                ) {
                    Text("SAVE", color = CowColors.AccentOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("CANCEL", color = CowColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SeqPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) CowColors.AccentOrange else CowColors.BgDark)
            .border(1.dp, if (selected) CowColors.AccentOrange else CowColors.BorderSubtle, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 9.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else CowColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
