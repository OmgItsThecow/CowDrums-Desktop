package com.cowdrums.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cowdrums.model.BuiltInKit
import com.cowdrums.model.DrumInstrument
import com.cowdrums.model.PatternSlot
import com.cowdrums.model.SoundSource
import com.cowdrums.ui.theme.CowColors
import com.cowdrums.viewmodel.DrumMachineUiState
import kotlinx.coroutines.delay

/**
 * Minimalist drum pad screen modeled directly after SF Drum Machine:
 * - < 8. ROOM > kit switcher bar
 * - Compact tool strip (Clear, A/B/FILL, /16, Metronome, Rec dot)
 * - Clean dark charcoal pads with thin hairline borders and minimalist white line-art drum icons
 */
@Composable
fun PerformScreen(
    state: DrumMachineUiState,
    onTriggerPad: (DrumInstrument, Float) -> Unit,
    onSelectSlot: (PatternSlot) -> Unit,
    onTriggerFill: () -> Unit,
    onToggleMetronome: () -> Unit,
    onToggleLiveRecord: () -> Unit,
    onClearSlot: () -> Unit,
    onPrevKit: () -> Unit,
    onNextKit: () -> Unit,
    onOpenKitPicker: () -> Unit
) {
    val flashMap = remember { mutableStateMapOf<DrumInstrument, Long>() }

    LaunchedEffect(flashMap.size) {
        if (flashMap.isNotEmpty()) {
            delay(100)
            val now = System.currentTimeMillis()
            val expired = flashMap.filterValues { now - it > 90 }.keys.toList()
            expired.forEach { flashMap.remove(it) }
        }
    }

    val activePattern = state.activePreset.patternFor(state.playingSlot)
    val kitTitle = when (val src = state.soundSource) {
        is SoundSource.BuiltIn -> "${src.kit.sf2Preset}. ${src.kit.displayName.uppercase()}"
        is SoundSource.SoundFont -> "${src.preset}. ${src.presetName.uppercase()}"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CowColors.BgDark)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Framed Kit Switcher + Tool Strip container (exact SF Drum Machine layout)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CowColors.SurfaceCard)
                .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            // <  8. ROOM  > Kit Switcher Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrevKit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Kit",
                        tint = CowColors.TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = kitTitle,
                    color = CowColors.AccentOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenKitPicker() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                IconButton(onClick = onNextKit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Kit",
                        tint = CowColors.TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            HorizontalDivider(color = CowColors.BorderSubtle, thickness = 1.dp)

            // Minimal Tool Strip: Trash | A | B | FILL | /16 | Metronome | Rec Dot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onClearSlot, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear",
                        tint = CowColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                MinimalSlotChip(
                    label = "A",
                    active = state.playingSlot == PatternSlot.MAIN_A && !state.fillActive,
                    onClick = { onSelectSlot(PatternSlot.MAIN_A) }
                )
                MinimalSlotChip(
                    label = "B",
                    active = state.playingSlot == PatternSlot.VAR_B && !state.fillActive,
                    onClick = { onSelectSlot(PatternSlot.VAR_B) }
                )
                MinimalSlotChip(
                    label = "FILL",
                    active = state.fillActive,
                    onClick = onTriggerFill
                )

                Text(
                    text = "/${activePattern.stepCount}",
                    color = CowColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                IconButton(onClick = onToggleMetronome, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Metronome",
                        tint = if (state.metronomeEnabled) CowColors.AccentOrange else CowColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Minimal Record Dot
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
        }

        // 12 Clean Monochrome Charcoal Pads (4 columns x 3 rows — matches SF Drum Machine's 4-column grid)
        val padOrder = listOf(
            DrumInstrument.KICK, DrumInstrument.SNARE, DrumInstrument.CHH, DrumInstrument.OHH,
            DrumInstrument.HIGH_TOM, DrumInstrument.MID_TOM, DrumInstrument.LOW_TOM, DrumInstrument.RIDE,
            DrumInstrument.RIM, DrumInstrument.CLAP, DrumInstrument.CRASH, DrumInstrument.PERC
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            padOrder.chunked(4).forEach { rowPads ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    rowPads.forEach { inst ->
                        val seqHit = state.isPlaying &&
                            state.currentStep >= 0 &&
                            activePattern.velocityAt(inst, state.currentStep) > 0
                        val isLit = seqHit || flashMap.containsKey(inst)
                        MinimalDrumPad(
                            instrument = inst,
                            isLit = isLit,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            onHit = { vel ->
                                flashMap[inst] = System.currentTimeMillis()
                                onTriggerPad(inst, vel)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalSlotChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) CowColors.AccentOrange else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (active) Color.White else CowColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun MinimalDrumPad(
    instrument: DrumInstrument,
    isLit: Boolean,
    modifier: Modifier = Modifier,
    onHit: (Float) -> Unit
) {
    val label = when (instrument) {
        DrumInstrument.KICK -> "BD"
        DrumInstrument.SNARE -> "SN"
        DrumInstrument.CHH -> "HH"
        DrumInstrument.OHH -> "OH"
        DrumInstrument.HIGH_TOM -> "HT"
        DrumInstrument.MID_TOM -> "LMT"
        DrumInstrument.LOW_TOM -> "HFT"
        DrumInstrument.RIDE -> "RD"
        DrumInstrument.RIM -> "RM"
        DrumInstrument.CLAP -> "CL"
        DrumInstrument.CRASH -> "CR"
        DrumInstrument.PERC -> "CB"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isLit) CowColors.AccentOrange.copy(alpha = 0.24f) else CowColors.SurfaceCard)
            .border(
                width = if (isLit) 1.5.dp else 1.dp,
                color = if (isLit) CowColors.AccentOrange else CowColors.BorderSubtle,
                shape = RoundedCornerShape(6.dp)
            )
            .pointerInput(instrument) {
                detectTapGestures(
                    onPress = { offset ->
                        val relY = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val vel = 0.50f + relY * 0.50f
                        onHit(vel)
                    }
                )
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DrumLineIcon(
                instrument = instrument,
                tint = if (isLit) CowColors.AccentOrange else CowColors.TextPrimary,
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                color = if (isLit) CowColors.AccentOrange else CowColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Crisp minimalist white line-art drum icons matching SF Drum Machine's hardware look.
 */
@Composable
private fun DrumLineIcon(
    instrument: DrumInstrument,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx())

        when (instrument) {
            DrumInstrument.KICK -> {
                // Bass drum circle + beater + two legs
                val r = w * 0.34f
                val cx = w * 0.5f
                val cy = h * 0.48f
                drawCircle(tint, radius = r, center = Offset(cx, cy), style = stroke)
                drawLine(tint, Offset(cx, cy + r), Offset(cx, cy + r * 0.15f), strokeWidth = 1.8.dp.toPx())
                drawCircle(tint, radius = 2.5.dp.toPx(), center = Offset(cx, cy + r * 0.1f))
                drawLine(tint, Offset(cx - r * 0.7f, cy + r * 0.7f), Offset(cx - r * 1.05f, cy + r * 1.1f), strokeWidth = 1.6.dp.toPx())
                drawLine(tint, Offset(cx + r * 0.7f, cy + r * 0.7f), Offset(cx + r * 1.05f, cy + r * 1.1f), strokeWidth = 1.6.dp.toPx())
            }
            DrumInstrument.SNARE -> {
                // Snare cylinder with top oval & lugs
                val left = w * 0.16f
                val right = w * 0.84f
                val top = h * 0.34f
                val bot = h * 0.68f
                drawOval(tint, Offset(left, top - h * 0.08f), Size(right - left, h * 0.18f), style = stroke)
                drawLine(tint, Offset(left, top), Offset(left, bot), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(right, top), Offset(right, bot), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(left, bot), Offset(right, bot), strokeWidth = 1.8.dp.toPx())
                for (i in 1..4) {
                    val x = left + (right - left) * (i / 5f)
                    drawLine(tint, Offset(x, top + h * 0.10f), Offset(x, bot), strokeWidth = 1.1.dp.toPx())
                }
            }
            DrumInstrument.CHH -> {
                // Closed Hi-Hat stand
                val cx = w * 0.5f
                drawLine(tint, Offset(cx, h * 0.22f), Offset(cx, h * 0.85f), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(w * 0.16f, h * 0.44f), Offset(w * 0.84f, h * 0.44f), strokeWidth = 2.0.dp.toPx())
                drawLine(tint, Offset(w * 0.32f, h * 0.39f), Offset(w * 0.68f, h * 0.39f), strokeWidth = 1.4.dp.toPx())
            }
            DrumInstrument.OHH -> {
                // Open Hi-Hat (two separated cymbals on stand)
                val cx = w * 0.5f
                drawLine(tint, Offset(cx, h * 0.20f), Offset(cx, h * 0.85f), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(w * 0.16f, h * 0.37f), Offset(w * 0.84f, h * 0.37f), strokeWidth = 2.0.dp.toPx())
                drawLine(tint, Offset(w * 0.16f, h * 0.50f), Offset(w * 0.84f, h * 0.50f), strokeWidth = 2.0.dp.toPx())
            }
            DrumInstrument.HIGH_TOM, DrumInstrument.MID_TOM, DrumInstrument.LOW_TOM -> {
                val scale = when (instrument) {
                    DrumInstrument.HIGH_TOM -> 0.55f
                    DrumInstrument.MID_TOM -> 0.72f
                    else -> 0.82f
                }
                val halfW = (w * 0.40f) * scale
                val cx = w * 0.5f
                val top = h * 0.34f
                val bot = top + h * 0.34f * scale
                drawOval(tint, Offset(cx - halfW, top - h * 0.06f), Size(halfW * 2, h * 0.12f), style = stroke)
                drawLine(tint, Offset(cx - halfW, top), Offset(cx - halfW, bot), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(cx + halfW, top), Offset(cx + halfW, bot), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(cx - halfW, bot), Offset(cx + halfW, bot), strokeWidth = 1.8.dp.toPx())
                if (instrument == DrumInstrument.LOW_TOM) {
                    drawLine(tint, Offset(cx - halfW * 0.8f, bot), Offset(cx - halfW, bot + h * 0.14f), strokeWidth = 1.5.dp.toPx())
                    drawLine(tint, Offset(cx + halfW * 0.8f, bot), Offset(cx + halfW, bot + h * 0.14f), strokeWidth = 1.5.dp.toPx())
                }
            }
            DrumInstrument.RIDE, DrumInstrument.CRASH -> {
                // Tilted cymbal disc + bell
                drawOval(tint, Offset(w * 0.14f, h * 0.28f), Size(w * 0.72f, h * 0.44f), style = stroke)
                drawCircle(tint, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
            }
            DrumInstrument.RIM -> {
                // Crossed drumsticks
                drawLine(tint, Offset(w * 0.22f, h * 0.76f), Offset(w * 0.78f, h * 0.24f), strokeWidth = 2.0.dp.toPx())
                drawLine(tint, Offset(w * 0.22f, h * 0.24f), Offset(w * 0.78f, h * 0.76f), strokeWidth = 2.0.dp.toPx())
            }
            DrumInstrument.CLAP -> {
                // Concentric clap waves
                drawCircle(tint, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(tint, radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
            }
            DrumInstrument.PERC -> {
                // Cowbell trapezoid
                drawLine(tint, Offset(w * 0.36f, h * 0.24f), Offset(w * 0.64f, h * 0.24f), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(w * 0.36f, h * 0.24f), Offset(w * 0.24f, h * 0.76f), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(w * 0.64f, h * 0.24f), Offset(w * 0.76f, h * 0.76f), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(w * 0.24f, h * 0.76f), Offset(w * 0.76f, h * 0.76f), strokeWidth = 1.8.dp.toPx())
            }
        }
    }
}
