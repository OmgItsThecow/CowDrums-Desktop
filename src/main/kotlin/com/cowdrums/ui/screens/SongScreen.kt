package com.cowdrums.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.cowdrums.model.PatternSlot
import com.cowdrums.model.SongSection
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.viewmodel.DrumMachineUiState

/**
 * Song screen styled directly after SF Drum Machine (Screenshot 1):
 * Flat charcoal rows, circular orange "1/4" badge on left, clean title, "x4" on right,
 * overflow menu per section, and a vivid orange circular '+' FAB at bottom right.
 */
@Composable
fun SongScreen(
    state: DrumMachineUiState,
    onPlaySong: () -> Unit,
    onStopSong: () -> Unit,
    onAddCurrentPreset: () -> Unit,
    onUpdateSection: (String, (SongSection) -> SongSection) -> Unit,
    onRemoveSection: (String) -> Unit,
    onMoveSection: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CowColors.BgDark)
    ) {
        if (state.songSections.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Tap + to add the active preset to your song",
                    color = CowColors.TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(state.songSections, key = { _, sec -> sec.id }) { idx, sec ->
                    val isActive = state.songMode && state.isPlaying && state.activeSongSectionIndex == idx
                    var menuOpen by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isActive) CowColors.SurfaceCard else CowColors.BgDark)
                            .clickable {
                                if (state.songMode && state.isPlaying) onStopSong() else onPlaySong()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular orange badge (exact Screenshot 1 look: "1/4")
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CowColors.AccentOrange)
                        ) {
                            Text(
                                text = "${idx + 1}/${state.songSections.size}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${idx + 1}. ${sec.presetName.uppercase()}",
                                color = if (isActive) CowColors.AccentOrange else CowColors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${sec.slot.label}${if (sec.playFillOnLastBar) "  ·  End Fill" else ""}",
                                color = CowColors.TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "x${sec.bars}",
                            color = CowColors.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    onUpdateSection(sec.id) {
                                        val next = if (it.bars >= 8) 1 else it.bars * 2
                                        it.copy(bars = next)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )

                        Box {
                            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Section Options",
                                    tint = CowColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false },
                                containerColor = CowColors.SurfaceElevated
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Switch Slot (${sec.slot.label})", color = CowColors.TextPrimary) },
                                    onClick = {
                                        menuOpen = false
                                        val nextSlot = when (sec.slot) {
                                            PatternSlot.MAIN_A -> PatternSlot.VAR_B
                                            PatternSlot.VAR_B -> PatternSlot.FILL
                                            PatternSlot.FILL -> PatternSlot.MAIN_A
                                        }
                                        onUpdateSection(sec.id) { it.copy(slot = nextSlot) }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bars +1 (currently ${sec.bars})", color = CowColors.TextPrimary) },
                                    onClick = {
                                        menuOpen = false
                                        onUpdateSection(sec.id) { it.copy(bars = (it.bars + 1).coerceAtMost(32)) }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bars -1", color = CowColors.TextPrimary) },
                                    onClick = {
                                        menuOpen = false
                                        onUpdateSection(sec.id) { it.copy(bars = (it.bars - 1).coerceAtLeast(1)) }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (sec.playFillOnLastBar) "Disable End Fill" else "Enable End Fill", color = CowColors.TextPrimary) },
                                    onClick = {
                                        menuOpen = false
                                        onUpdateSection(sec.id) { it.copy(playFillOnLastBar = !it.playFillOnLastBar) }
                                    }
                                )
                                if (idx > 0) {
                                    DropdownMenuItem(
                                        text = { Text("Move Up", color = CowColors.TextPrimary) },
                                        onClick = {
                                            menuOpen = false
                                            onMoveSection(idx, -1)
                                        }
                                    )
                                }
                                if (idx < state.songSections.size - 1) {
                                    DropdownMenuItem(
                                        text = { Text("Move Down", color = CowColors.TextPrimary) },
                                        onClick = {
                                            menuOpen = false
                                            onMoveSection(idx, 1)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Remove Section", color = CowColors.AccentRed) },
                                    onClick = {
                                        menuOpen = false
                                        onRemoveSection(sec.id)
                                    }
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = CowColors.BorderSubtle, thickness = 1.dp)
                }
            }
        }

        // Signature vivid orange '+' circular FAB (bottom right, matching Screenshot 1)
        FloatingActionButton(
            onClick = onAddCurrentPreset,
            shape = CircleShape,
            containerColor = CowColors.AccentOrange,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Preset to Song", modifier = Modifier.size(28.dp))
        }
    }
}
