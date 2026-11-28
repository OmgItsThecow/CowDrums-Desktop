package com.cowdrums.audio

import com.cowdrums.model.MidiDeviceEntry
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiMessage
import javax.sound.midi.MidiSystem
import javax.sound.midi.Receiver
import javax.sound.midi.ShortMessage

/**
 * Cross-platform Desktop MIDI input manager (Windows & Linux) using javax.sound.midi.
 * Automatically discovers both USB MIDI controllers and OS-paired Bluetooth LE (BLE) MIDI
 * controllers (Windows BLE MIDI / Korg BLE-MIDI / Linux BlueZ MIDI / PipeWire / ALSA Seq).
 */
class DesktopMidiManager(
    private val onNoteOn: (channel: Int, note: Int, velocity: Float) -> Unit,
    private val onDevicesChanged: (List<MidiDeviceEntry>) -> Unit
) {
    private val openDevices = LinkedHashMap<String, MidiDevice>()
    private val deviceInfos = LinkedHashMap<String, MidiDevice.Info>()

    @Synchronized
    fun refreshAndAutoConnect(autoConnectNew: Boolean = true): List<MidiDeviceEntry> {
        // Close any devices that disappeared
        val infos = runCatching { MidiSystem.getMidiDeviceInfo() }.getOrDefault(emptyArray())
        val validIds = HashSet<String>()
        deviceInfos.clear()

        for (info in infos) {
            val name = info.name ?: continue
            val desc = info.description ?: ""
            // Skip internal JVM software synths/sequencers that aren't external controllers
            if (name.contains("Real Time Sequencer", ignoreCase = true) ||
                name.contains("Gervill", ignoreCase = true) ||
                desc.contains("Software sequencer", ignoreCase = true) ||
                desc.contains("Software synthesizer", ignoreCase = true)
            ) {
                continue
            }

            val dev = runCatching { MidiSystem.getMidiDevice(info) }.getOrNull() ?: continue
            if (dev.maxTransmitters == 0) continue // Output-only device

            val id = "${info.name}::${info.vendor}::${info.description}"
            validIds.add(id)
            deviceInfos[id] = info

            if (autoConnectNew && !openDevices.containsKey(id)) {
                connectInternal(id, info)
            }
        }

        val stale = openDevices.keys.filter { it !in validIds }
        for (id in stale) {
            disconnectInternal(id)
        }

        val list = snapshotEntries()
        onDevicesChanged(list)
        return list
    }

    @Synchronized
    fun toggleDevice(deviceId: String): Boolean {
        val result = if (openDevices.containsKey(deviceId)) {
            disconnectInternal(deviceId)
            false
        } else {
            val info = deviceInfos[deviceId]
            if (info != null) connectInternal(deviceId, info) else false
        }
        onDevicesChanged(snapshotEntries())
        return result
    }

    @Synchronized
    fun connectAll(): Int {
        refreshAndAutoConnect(autoConnectNew = true)
        return openDevices.size
    }

    @Synchronized
    fun disconnectAll() {
        for (id in openDevices.keys.toList()) {
            disconnectInternal(id)
        }
        onDevicesChanged(snapshotEntries())
    }

    @Synchronized
    fun close() {
        disconnectAll()
    }

    fun openSystemBluetoothSettings(): String {
        val os = System.getProperty("os.name").lowercase()
        return runCatching {
            when {
                os.contains("win") -> {
                    ProcessBuilder("cmd", "/c", "start", "ms-settings:bluetooth").start()
                    "Opened Windows Bluetooth settings — pair your BLE MIDI controller, then click Refresh."
                }
                os.contains("mac") -> {
                    ProcessBuilder("open", "/System/Applications/Utilities/Audio MIDI Setup.app").start()
                    "Opened macOS Audio MIDI Setup — connect Bluetooth MIDI, then click Refresh."
                }
                else -> {
                    val candidates = listOf(
                        listOf("gnome-control-center", "bluetooth"),
                        listOf("blueman-manager"),
                        listOf("kcmshell6", "kcm_bluetooth"),
                        listOf("kcmshell5", "kcm_bluetooth")
                    )
                    var launched = false
                    for (cmd in candidates) {
                        val ok = runCatching {
                            ProcessBuilder(cmd).start()
                            true
                        }.getOrDefault(false)
                        if (ok) {
                            launched = true
                            break
                        }
                    }
                    if (launched) {
                        "Opened Bluetooth manager — pair your BLE MIDI controller, then click Refresh."
                    } else {
                        "Pair your Bluetooth MIDI controller in system Bluetooth settings (BlueZ MIDI), then click Refresh."
                    }
                }
            }
        }.getOrElse {
            "Open your OS Bluetooth settings to pair your BLE MIDI controller, then click Refresh."
        }
    }

    private fun connectInternal(id: String, info: MidiDevice.Info): Boolean {
        return runCatching {
            val device = MidiSystem.getMidiDevice(info)
            if (!device.isOpen) device.open()
            val transmitter = device.transmitter
            transmitter.receiver = object : Receiver {
                override fun send(message: MidiMessage?, timeStamp: Long) {
                    if (message is ShortMessage) {
                        val cmd = message.command
                        val vel = message.data2
                        if (cmd == ShortMessage.NOTE_ON && vel > 0) {
                            onNoteOn(message.channel, message.data1, (vel / 127f).coerceIn(0.05f, 1.0f))
                        }
                    }
                }
                override fun close() {}
            }
            openDevices[id] = device
            true
        }.getOrDefault(false)
    }

    private fun disconnectInternal(id: String) {
        val dev = openDevices.remove(id) ?: return
        runCatching { dev.close() }
    }

    private fun snapshotEntries(): List<MidiDeviceEntry> {
        return deviceInfos.map { (id, info) ->
            val combined = "${info.name} ${info.vendor} ${info.description}"
            val isBt = combined.contains("bluetooth", ignoreCase = true) ||
                combined.contains("ble", ignoreCase = true) ||
                combined.contains("widi", ignoreCase = true) ||
                combined.contains("bt", ignoreCase = true) ||
                combined.contains("air", ignoreCase = true)
            MidiDeviceEntry(
                id = id,
                name = info.name.ifBlank { "MIDI Controller" },
                isBluetooth = isBt,
                connected = openDevices.containsKey(id),
                detail = listOfNotNull(
                    info.vendor?.takeIf { it.isNotBlank() && it != "Unknown vendor" },
                    info.description?.takeIf { it.isNotBlank() && it != "No details available" }
                ).joinToString(" · ")
            )
        }
    }
}
