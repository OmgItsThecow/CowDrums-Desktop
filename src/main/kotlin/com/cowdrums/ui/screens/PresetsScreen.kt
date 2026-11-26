package com.cowdrums.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.cowdrums.model.DrumGenre
import com.cowdrums.model.DrumPreset
import com.cowdrums.model.PatternSlot
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.viewmodel.DrumMachineUiState

/**
 * Clean, minimal SF Drum Machine style preset list:
 * Flat charcoal rows with hairline dividers, vivid orange circular play badges,
 * genre filter strip, and bottom MAIN A / VAR B / FILL performance bar.
 */
@Composable
fun PresetsScreen(
    state: DrumMachineUiState,
    onSelectPreset: (DrumPreset, Boolean) -> Unit,
    onTogglePlay: () -> Unit,
    onSelectSlot: (PatternSlot) -> Unit,
    onTriggerFill: () -> Unit,
    onToggleAutoFill: () -> Unit,
    onAddToSong: (DrumPreset) -> Unit,
    onDeleteCustomPreset: (DrumPreset) -> Unit
) {
    var selectedGenre by remember { mutableStateOf<DrumGenre?>(null) }
    var selectedSubcategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val subcategoriesForGenre = remember(state.allPresets, selectedGenre) {
        if (selectedGenre == null) {
            emptyList()
        } else {
            state.allPresets
                .filter { it.genre == selectedGenre && it.subcategory.isNotBlank() }
                .map { it.subcategory }
                .distinct()
        }
    }

    val filtered = remember(state.allPresets, selectedGenre, selectedSubcategory, searchQuery) {
        state.allPresets.filter { preset ->
            val genreOk = selectedGenre == null || preset.genre == selectedGenre
            val subOk = selectedSubcategory == null || preset.subcategory == selectedSubcategory
            val queryOk = searchQuery.isBlank() ||
                preset.name.contains(searchQuery, ignoreCase = true) ||
                preset.description.contains(searchQuery, ignoreCase = true) ||
                preset.genre.label.contains(searchQuery, ignoreCase = true) ||
                preset.subcategory.contains(searchQuery, ignoreCase = true)
            genreOk && subOk && queryOk
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CowColors.BgDark)
    ) {
        // Minimal Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search ${state.allPresets.size} drum patterns...",
                    color = CowColors.TextMuted,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CowColors.TextMuted, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = CowColors.TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CowColors.SurfaceCard,
                unfocusedContainerColor = CowColors.SurfaceCard,
                focusedBorderColor = CowColors.AccentOrange,
                unfocusedBorderColor = CowColors.BorderSubtle,
                focusedTextColor = CowColors.TextPrimary,
                unfocusedTextColor = CowColors.TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )

        // Row 1: Main Genre Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                GenreChip(
                    label = "All (${state.allPresets.size})",
                    selected = selectedGenre == null,
                    onClick = {
                        selectedGenre = null
                        selectedSubcategory = null
                    }
                )
            }
            items(DrumGenre.entries) { genre ->
                val count = state.allPresets.count { it.genre == genre }
                if (count > 0) {
                    GenreChip(
                        label = "${genre.label} ($count)",
                        selected = selectedGenre == genre,
                        onClick = {
                            if (selectedGenre == genre) {
                                selectedGenre = null
                                selectedSubcategory = null
                            } else {
                                selectedGenre = genre
                                selectedSubcategory = null
                            }
                        }
                    )
                }
            }
        }

        // Row 2: Subcategory Filter Chips (appears automatically when a Genre with subcategories is selected)
        if (subcategoriesForGenre.size > 1) {
            Spacer(Modifier.height(6.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    SubcategoryChip(
                        label = "All ${selectedGenre?.label.orEmpty()}",
                        selected = selectedSubcategory == null,
                        onClick = { selectedSubcategory = null }
                    )
                }
                items(subcategoriesForGenre) { sub ->
                    val count = state.allPresets.count { it.genre == selectedGenre && it.subcategory == sub }
                    SubcategoryChip(
                        label = "$sub ($count)",
                        selected = selectedSubcategory == sub,
                        onClick = {
                            selectedSubcategory = if (selectedSubcategory == sub) null else sub
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = CowColors.BorderSubtle, thickness = 1.dp)

        // Flat SF Drum Machine style list rows
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(filtered, key = { it.id }) { preset ->
                val isCurrent = state.activePreset.id == preset.id && !state.songMode
                val isPlaying = isCurrent && state.isPlaying

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isCurrent) CowColors.SurfaceCard else CowColors.BgDark)
                        .clickable {
                            if (isCurrent) onTogglePlay() else onSelectPreset(preset, true)
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Orange Badge (matches Screenshot 1's orange circle!)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) CowColors.AccentOrange else CowColors.SurfaceCard)
                            .border(
                                width = 1.dp,
                                color = if (isCurrent) CowColors.AccentOrange else CowColors.BorderSubtle,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop" else "Play",
                            tint = if (isCurrent) Color.White else CowColors.AccentOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    val catLabel = preset.subcategory.ifBlank { preset.genre.label }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.name,
                            color = if (isCurrent) CowColors.AccentOrange else CowColors.TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$catLabel  ·  ${preset.bpm} BPM  ·  ${preset.timeSignature.label}  ·  ${preset.recommendedKit.displayName}",
                            color = CowColors.TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (preset.isCustom) {
                        IconButton(onClick = { onDeleteCustomPreset(preset) }, modifier = Modifier.size(34.dp)) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = CowColors.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = { onAddToSong(preset) }, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add to Song",
                            tint = CowColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                HorizontalDivider(color = CowColors.BorderSubtle, thickness = 1.dp)
            }
        }

        // Clean bottom A / B / FILL / AUTO-4B performance bar
        HorizontalDivider(color = CowColors.BorderSubtle, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CowColors.SurfaceCard)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlotButton(
                label = "MAIN A",
                selected = state.playingSlot == PatternSlot.MAIN_A && !state.fillActive,
                accent = CowColors.AccentOrange,
                modifier = Modifier.weight(1f),
                onClick = { onSelectSlot(PatternSlot.MAIN_A) }
            )
            SlotButton(
                label = "VAR B",
                selected = state.playingSlot == PatternSlot.VAR_B && !state.fillActive,
                accent = CowColors.AccentOrange,
                modifier = Modifier.weight(1f),
                onClick = { onSelectSlot(PatternSlot.VAR_B) }
            )
            SlotButton(
                label = if (state.fillActive) "FILLING" else "FILL",
                selected = state.fillActive,
                accent = CowColors.AccentOrange,
                modifier = Modifier.weight(1f),
                onClick = onTriggerFill
            )
            SlotButton(
                label = "AUTO 4B",
                selected = state.autoFillEvery4Bars,
                accent = CowColors.AccentOrange,
                modifier = Modifier.width(74.dp),
                onClick = onToggleAutoFill
            )
        }
    }
}

@Composable
private fun GenreChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) CowColors.AccentOrange else CowColors.SurfaceCard)
            .border(
                1.dp,
                if (selected) CowColors.AccentOrange else CowColors.BorderSubtle,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else CowColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SubcategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) CowColors.AccentOrange.copy(alpha = 0.22f) else Color.Transparent
            )
            .border(
                1.dp,
                if (selected) CowColors.AccentOrange else CowColors.BorderSubtle,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (selected) CowColors.AccentOrange else CowColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun SlotButton(
    label: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) accent else CowColors.BgDark)
            .border(1.dp, if (selected) accent else CowColors.BorderSubtle, RoundedCornerShape(6.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else CowColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}
