package com.cowdrums.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.DrumInstrument
import com.cowdrums.model.GmDrumCatalog
import com.cowdrums.model.NoteRepeatRate
import com.cowdrums.model.PadConfig
import com.cowdrums.model.PatternSlot
import com.cowdrums.ui.theme.CowColors

private enum class PadMenuTab(val label: String) {
    SOUND("Sound & Kit"),
    PERF("MIDI & Perf"),
    PATTERN("Pattern")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PadContextMenuDialog(
    instrument: DrumInstrument,
    config: PadConfig,
    activeSlot: PatternSlot,
    isMidiLearning: Boolean,
    onDismiss: () -> Unit,
    onSelectGmSound: (Int) -> Unit,
    onSelectKitOverride: (BuiltInKit) -> Unit,
    onPickCustomWav: () -> Unit,
    onResetPadSound: () -> Unit,
    onSetChokeGroup: (Int) -> Unit,
    onToggleMidiLearn: () -> Unit,
    onResetMidiNote: () -> Unit,
    onSetNoteRepeat: (NoteRepeatRate) -> Unit,
    onClearPadRow: () -> Unit,
    onFillPadEveryN: (Int) -> Unit,
    onHumanizePadRow: () -> Unit
) {
    var activeTab by remember { mutableStateOf(PadMenuTab.SOUND) }
    var soundSubMode by remember { mutableStateOf(0) } // 0 = GM Notes, 1 = Swap From Kit

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = CowColors.SurfacePanel,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(10.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${instrument.displayName} Pad",
                            color = CowColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val activeSummary = when {
                            config.customWavName != null -> "WAV: ${config.customWavName}"
                            config.kitOverride != null && config.soundGmNote != null ->
                                "${config.kitOverride.displayName} · ${GmDrumCatalog.nameFor(config.soundGmNote)}"
                            config.kitOverride != null -> "Kit: ${config.kitOverride.displayName}"
                            config.soundGmNote != null -> "GM ${config.soundGmNote}: ${GmDrumCatalog.nameFor(config.soundGmNote)}"
                            else -> "Kit Default (GM ${instrument.gmNote})"
                        }
                        Text(
                            text = activeSummary,
                            color = CowColors.AccentOrange,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CowColors.TextSecondary)
                    }
                }

                // 3-tab selector (1. Sound & Sample, 3. MIDI & Performance, 4. Pattern)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PadMenuTab.entries.forEach { tab ->
                        val selected = activeTab == tab
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) CowColors.AccentOrange else CowColors.SurfaceCard)
                                .border(
                                    1.dp,
                                    if (selected) CowColors.AccentOrange else CowColors.BorderSubtle,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { activeTab = tab }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = tab.label,
                                color = if (selected) Color.White else CowColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider(color = CowColors.BorderSubtle)

                when (activeTab) {
                    PadMenuTab.SOUND -> {
                        // Quick actions: Load WAV + Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionOutlineButton(
                                label = "Load Custom .WAV",
                                icon = Icons.Default.FolderOpen,
                                accent = true,
                                modifier = Modifier.weight(1f),
                                onClick = onPickCustomWav
                            )
                            ActionOutlineButton(
                                label = "Reset Default",
                                icon = Icons.Default.Refresh,
                                accent = false,
                                modifier = Modifier.weight(0.85f),
                                onClick = onResetPadSound
                            )
                        }

                        // Sub-toggle: GM Sound vs Swap From Kit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SubTabChip(
                                label = "Change Sound (GM Notes)",
                                selected = soundSubMode == 0,
                                modifier = Modifier.weight(1f),
                                onClick = { soundSubMode = 0 }
                            )
                            SubTabChip(
                                label = "Swap From Another Kit",
                                selected = soundSubMode == 1,
                                modifier = Modifier.weight(1f),
                                onClick = { soundSubMode = 1 }
                            )
                        }

                        if (soundSubMode == 0) {
                            val activeGm = config.soundGmNote ?: instrument.gmNote
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(GmDrumCatalog.ALL, key = { it.note }) { sound ->
                                    val selected = config.customWavName == null && activeGm == sound.note
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (selected) CowColors.AccentOrange.copy(alpha = 0.2f)
                                                else CowColors.SurfaceCard
                                            )
                                            .border(
                                                1.dp,
                                                if (selected) CowColors.AccentOrange else CowColors.BorderSubtle,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { onSelectGmSound(sound.note) }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${sound.note}",
                                            color = if (selected) CowColors.AccentOrange else CowColors.TextMuted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(30.dp)
                                        )
                                        Text(
                                            text = sound.name,
                                            color = if (selected) CowColors.TextPrimary else CowColors.TextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (sound.note == instrument.gmNote) {
                                            Text(
                                                text = "DEFAULT",
                                                color = CowColors.TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(BuiltInKit.entries, key = { it.name }) { kit ->
                                    val selected = config.kitOverride == kit
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (selected) CowColors.AccentOrange.copy(alpha = 0.2f)
                                                else CowColors.SurfaceCard
                                            )
                                            .border(
                                                1.dp,
                                                if (selected) CowColors.AccentOrange else CowColors.BorderSubtle,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { onSelectKitOverride(kit) }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${kit.sf2Preset}.",
                                            color = if (selected) CowColors.AccentOrange else CowColors.TextMuted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(32.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = kit.displayName,
                                                color = if (selected) CowColors.TextPrimary else CowColors.TextSecondary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = kit.subtitle,
                                                color = CowColors.TextMuted,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PadMenuTab.PERF -> {
                        // Choke Group
                        SectionHeader("Choke Group")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val labels = listOf("None", "Grp 1 (HH)", "Grp 2", "Grp 3")
                            labels.forEachIndexed { idx, lbl ->
                                SelectableChip(
                                    label = lbl,
                                    selected = config.chokeGroup == idx,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onSetChokeGroup(idx) }
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // MIDI Input Note & Learn
                        SectionHeader("MIDI Controller Trigger Note")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CowColors.SurfaceCard)
                                .border(
                                    1.dp,
                                    if (isMidiLearning) CowColors.AccentOrange else CowColors.BorderSubtle,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isMidiLearning) "Listening for MIDI..."
                                    else GmDrumCatalog.midiNoteLabel(config.midiInputNote),
                                    color = if (isMidiLearning) CowColors.AccentOrange else CowColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isMidiLearning) "Hit any pad or key on your USB / Bluetooth controller"
                                    else "Default: ${GmDrumCatalog.midiNoteLabel(instrument.gmNote)}",
                                    color = CowColors.TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isMidiLearning) CowColors.AccentOrange else CowColors.SurfaceElevated)
                                    .border(1.dp, CowColors.AccentOrange, RoundedCornerShape(6.dp))
                                    .clickable { onToggleMidiLearn() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isMidiLearning) "Cancel" else "MIDI Learn",
                                    color = if (isMidiLearning) Color.White else CowColors.AccentOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (config.midiInputNote != instrument.gmNote) {
                                TextButton(onClick = onResetMidiNote) {
                                    Text("Reset", color = CowColors.TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Note Repeat / Roll
                        SectionHeader("Note Repeat / Roll (Hold Pad)")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NoteRepeatRate.entries.forEach { rate ->
                                SelectableChip(
                                    label = rate.label,
                                    selected = config.noteRepeat == rate,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onSetNoteRepeat(rate) }
                                )
                            }
                        }
                        Text(
                            text = "Press and hold this pad to play tempo-synced rolls at the selected rate.",
                            color = CowColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    PadMenuTab.PATTERN -> {
                        SectionHeader("Quick Actions for ${instrument.displayName} in ${activeSlot.label}")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PatternActionChip("Fill Every 4 (1/4)") {
                                onFillPadEveryN(4)
                                onDismiss()
                            }
                            PatternActionChip("Fill Every 2 (1/8)") {
                                onFillPadEveryN(2)
                                onDismiss()
                            }
                            PatternActionChip("Fill Every 8 (1/2)") {
                                onFillPadEveryN(8)
                                onDismiss()
                            }
                            PatternActionChip("Fill All (1/16)") {
                                onFillPadEveryN(1)
                                onDismiss()
                            }
                            PatternActionChip("Humanize Velocity") {
                                onHumanizePadRow()
                                onDismiss()
                            }
                            PatternActionChip("Clear Pad Row", danger = true) {
                                onClearPadRow()
                                onDismiss()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = CowColors.TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SubTabChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) CowColors.SurfaceElevated else Color.Transparent)
            .border(
                1.dp,
                if (selected) CowColors.AccentOrange else CowColors.BorderSubtle,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) CowColors.AccentOrange else CowColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) CowColors.AccentOrange else CowColors.SurfaceCard)
            .border(
                1.dp,
                if (selected) CowColors.AccentOrange else CowColors.BorderSubtle,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else CowColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun ActionOutlineButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CowColors.SurfaceCard)
            .border(1.dp, if (accent) CowColors.AccentOrange else CowColors.BorderSubtle, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (accent) CowColors.AccentOrange else CowColors.TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = if (accent) CowColors.AccentOrange else CowColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PatternActionChip(
    label: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CowColors.SurfaceCard)
            .border(
                1.dp,
                if (danger) CowColors.AccentRed.copy(alpha = 0.7f) else CowColors.BorderSubtle,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            color = if (danger) CowColors.AccentRed else CowColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
