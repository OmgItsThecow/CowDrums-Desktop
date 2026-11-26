package com.cowdrums.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.LoadedSoundFont
import com.cowdrums.model.SoundSource
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.viewmodel.DrumMachineUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitSoundFontSheet(
    state: DrumMachineUiState,
    onDismiss: () -> Unit,
    onSelectBuiltInKit: (BuiltInKit) -> Unit,
    onImportSoundFontClick: () -> Unit,
    onSelectSoundFontPreset: (LoadedSoundFont, Int, Int, String) -> Unit,
    onDeleteSoundFont: (LoadedSoundFont) -> Unit,
    onToggleLockKit: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CowColors.SurfacePanel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Drum Kits & SoundFonts (.sf2)",
                        color = CowColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Switch built-in kits or load custom SF2 banks",
                        color = CowColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = onImportSoundFontClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CowColors.AccentCyan,
                        contentColor = Color(0xFF0A1014)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Load .SF2", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Lock Kit Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CowColors.SurfaceCard)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lock Kit When Switching Presets",
                        color = CowColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Keep your selected kit/SF2 when trying different patterns",
                        color = CowColors.TextMuted,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = state.lockKitOnPresetChange,
                    onCheckedChange = { onToggleLockKit() }
                )
            }

            Spacer(Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Imported SoundFonts section
                if (state.loadedSoundFonts.isNotEmpty()) {
                    item {
                        Text(
                            text = "IMPORTED SOUNDFONTS (.SF2)",
                            color = CowColors.AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    items(state.loadedSoundFonts, key = { it.fileName }) { sf ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CowColors.SurfaceCard)
                                .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AudioFile,
                                        contentDescription = null,
                                        tint = CowColors.AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = sf.displayName,
                                        color = CowColors.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSoundFont(sf) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete SF2",
                                        tint = CowColors.TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            sf.presets.take(12).forEach { p ->
                                val active = (state.soundSource as? SoundSource.SoundFont)?.let {
                                    it.fileName == sf.fileName && it.bank == p.bank && it.preset == p.preset
                                } == true
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (active) CowColors.AccentCyan.copy(alpha = 0.18f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            onSelectSoundFontPreset(sf, p.bank, p.preset, p.name)
                                            onDismiss()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "[B${p.bank}:P${p.preset}] ${p.name}",
                                        color = if (active) CowColors.AccentCyan else CowColors.TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (active) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = CowColors.AccentCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        HorizontalDivider(color = CowColors.BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                // Built-in kits
                item {
                    Text(
                        text = "BUILT-IN STUDIO KITS",
                        color = CowColors.AccentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(BuiltInKit.entries, key = { it.name }) { kit ->
                    val selected = (state.soundSource as? SoundSource.BuiltIn)?.kit == kit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) CowColors.AccentAmber.copy(alpha = 0.16f)
                                else CowColors.SurfaceCard
                            )
                            .border(
                                1.dp,
                                if (selected) CowColors.AccentAmber else CowColors.BorderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectBuiltInKit(kit)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = kit.displayName,
                                color = if (selected) CowColors.AccentAmber else CowColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = kit.subtitle,
                                color = CowColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        if (selected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = CowColors.AccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
