package com.cowdrums.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cowdrums.model.MidiDeviceEntry
import com.cowdrums.ui.theme.CowColors

@Composable
fun MidiBluetoothDialog(
    devices: List<MidiDeviceEntry>,
    lastMidiActivity: String?,
    onRefresh: () -> Unit,
    onToggleDevice: (String) -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onDismiss: () -> Unit
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "MIDI & Bluetooth",
                        tint = CowColors.AccentOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Bluetooth & USB MIDI Controllers",
                        color = CowColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CowColors.TextSecondary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CowColors.AccentOrange)
                            .clickable { onRefresh() }
                            .padding(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Scan / Refresh", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CowColors.SurfaceCard)
                            .border(1.dp, CowColors.AccentOrange, RoundedCornerShape(6.dp))
                            .clickable { onOpenBluetoothSettings() }
                            .padding(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Bluetooth", tint = CowColors.AccentOrange, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pair Bluetooth MIDI", color = CowColors.AccentOrange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Live MIDI activity bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CowColors.SurfaceCard)
                        .border(1.dp, CowColors.BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = if (lastMidiActivity != null) "Last MIDI Input: $lastMidiActivity"
                        else "Hit any pad or key on your controller to test MIDI input...",
                        color = if (lastMidiActivity != null) CowColors.AccentOrange else CowColors.TextMuted,
                        fontSize = 12.sp
                    )
                }

                HorizontalDivider(color = CowColors.BorderSubtle)

                if (devices.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CowColors.SurfaceCard)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No MIDI controllers detected yet.",
                            color = CowColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Bluetooth MIDI setup (Windows & Linux):\n" +
                                "1. Click \"Pair Bluetooth MIDI\" above and pair your BLE MIDI controller (WIDI, Korg microKEY Air, Akai, Roland, Yamaha MD-BT01, etc.).\n" +
                                "2. Click \"Scan / Refresh\" — CowDrums auto-connects immediately!\n" +
                                "Tip: Right-click any drum pad to use MIDI Learn and map any pad/key.",
                            color = CowColors.TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(devices, key = { it.id }) { dev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (dev.connected) CowColors.AccentOrange.copy(alpha = 0.16f)
                                        else CowColors.SurfaceCard
                                    )
                                    .border(
                                        1.dp,
                                        if (dev.connected) CowColors.AccentOrange else CowColors.BorderSubtle,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onToggleDevice(dev.id) }
                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (dev.isBluetooth) Icons.Default.Bluetooth else Icons.Default.Piano,
                                    contentDescription = null,
                                    tint = if (dev.connected) CowColors.AccentOrange else CowColors.TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dev.name,
                                        color = CowColors.TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (dev.detail.isNotBlank()) {
                                        Text(
                                            text = dev.detail,
                                            color = CowColors.TextMuted,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Text(
                                    text = if (dev.connected) "CONNECTED" else "CONNECT",
                                    color = if (dev.connected) CowColors.AccentOrange else CowColors.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
