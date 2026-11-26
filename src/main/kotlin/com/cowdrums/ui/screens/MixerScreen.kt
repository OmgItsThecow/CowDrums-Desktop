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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cowdrums.model.ChannelMix
import com.cowdrums.model.DrumInstrument
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.viewmodel.DrumMachineUiState
import kotlin.math.roundToInt

@Composable
fun MixerScreen(
    state: DrumMachineUiState,
    onUpdateChannel: (DrumInstrument, (ChannelMix) -> ChannelMix) -> Unit,
    onAudition: (DrumInstrument) -> Unit,
    onResetMixer: () -> Unit,
    onOpenKitPicker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CowColors.BgDark)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "12-Channel Drum Mixer",
                    color = CowColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Source: ${state.soundSource.label}",
                    color = CowColors.AccentCyan,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { onOpenKitPicker() }
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CowColors.SurfaceCard)
                    .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(8.dp))
                    .clickable { onResetMixer() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CowColors.TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("RESET", color = CowColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(DrumInstrument.ALL, key = { it.name }) { inst ->
                val mix = state.mixer[inst] ?: ChannelMix()
                MixerChannelCard(
                    instrument = inst,
                    mix = mix,
                    onAudition = { onAudition(inst) },
                    onVolumeChange = { v -> onUpdateChannel(inst) { it.copy(volume = v) } },
                    onPanChange = { p -> onUpdateChannel(inst) { it.copy(pan = p) } },
                    onPitchDelta = { d ->
                        onUpdateChannel(inst) { it.copy(pitchSemitones = (it.pitchSemitones + d).coerceIn(-12f, 12f)) }
                    },
                    onToggleMute = { onUpdateChannel(inst) { it.copy(muted = !it.muted) } },
                    onToggleSolo = { onUpdateChannel(inst) { it.copy(solo = !it.solo) } }
                )
            }
        }
    }
}

@Composable
private fun MixerChannelCard(
    instrument: DrumInstrument,
    mix: ChannelMix,
    onAudition: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onPanChange: (Float) -> Unit,
    onPitchDelta: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit
) {
    val accent = CowColors.AccentOrange

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CowColors.SurfaceCard)
            .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tap name to audition drum sound
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onAudition() }
                    .padding(vertical = 2.dp, horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accent)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = instrument.displayName,
                    color = CowColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "GM ${instrument.gmNote}",
                    color = CowColors.TextMuted,
                    fontSize = 10.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pitch stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CowColors.SurfacePanel)
                ) {
                    IconButton(onClick = { onPitchDelta(-1f) }, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Pitch down", tint = CowColors.TextSecondary, modifier = Modifier.size(13.dp))
                    }
                    val st = mix.pitchSemitones.roundToInt()
                    Text(
                        text = "${if (st > 0) "+" else ""}${st}st",
                        color = if (st != 0) CowColors.AccentCyan else CowColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onPitchDelta(1f) }, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Pitch up", tint = CowColors.TextSecondary, modifier = Modifier.size(13.dp))
                    }
                }

                // Mute
                SmallToggleBadge("M", mix.muted, CowColors.AccentCoral, onToggleMute)
                // Solo
                SmallToggleBadge("S", mix.solo, CowColors.AccentAmber, onToggleSolo)
            }
        }

        // Volume Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VOL ${(mix.volume * 100).roundToInt()}%",
                color = CowColors.TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.width(58.dp)
            )
            Slider(
                value = mix.volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1.25f,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = CowColors.SurfaceElevated
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
            )
            Spacer(Modifier.width(8.dp))
            val panLabel = when {
                mix.pan < -0.05f -> "L${(-mix.pan * 100).roundToInt()}"
                mix.pan > 0.05f -> "R${(mix.pan * 100).roundToInt()}"
                else -> "C"
            }
            Text(
                text = "PAN $panLabel",
                color = CowColors.TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.width(50.dp)
            )
            Slider(
                value = mix.pan,
                onValueChange = onPanChange,
                valueRange = -1f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = CowColors.AccentCyan,
                    activeTrackColor = CowColors.AccentCyan,
                    inactiveTrackColor = CowColors.SurfaceElevated
                ),
                modifier = Modifier
                    .width(90.dp)
                    .height(28.dp)
            )
        }
    }
}

@Composable
private fun SmallToggleBadge(label: String, active: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) color else CowColors.SurfacePanel)
            .border(1.dp, if (active) color else CowColors.BorderSubtle, RoundedCornerShape(6.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (active) Color(0xFF0D0F14) else CowColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}
