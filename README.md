# CowDrums Desktop version (Windows & Linux)

The **Windows & Linux** desktop edition of **CowDrums** — a minimalist SoundFont (`.sf2`) drum machine & bass workstation built with **Kotlin + Compose Multiplatform Desktop**, featuring **95 ready-to-play drum presets (285 patterns)**, **10 real studio-recorded drum kits**, and **8 real bass presets** bundled out of the box.

## Features

- **95 Ready-to-Play Drum Presets (285 Patterns)** organized in a 2-tier **Genre → Subcategory** browser:
  - **Rock** (`Classic & Hard`, `Indie Rock`, `Post-Punk`, `Punk & Grunge`, `Metal`)
  - **Indie & Pop** (`Indie Pop`, `Dream Pop`, `Synthwave`, `New Wave`, `Pop & Disco`)
  - **Electronic** (`Techno`, `Psytrance`, `Trance`, `House`, `DnB & Bass`)
  - **Rap & Hip-Hop** (`Rap & Boom-Bap`, `Trap & Drill`, `Phonk & Bounce`, `Lo-Fi`)
  - **Reggae & Ska** (`Reggae & Dub`, `Ska`, `Ska Fusion`)
  - **Funk & R&B**, **Jazz & Blues**, **Latin**, **Odd Time** (`5/4`, `7/8`, `6/8`)
- **Real Studio SoundFont Built-In (GeneralUser GS)** + **Custom `.sf2` Loader**:
  - 10 real studio drum kits (`Standard Studio`, `Live Room Rock`, `Power Rock & Metal`, `Jazz Acoustic`, `Jazz Brush`, `Orchestral`, `TR-808 / 909`, `Club Dance`, `Electronic`, `Standard 2 / Latin`) + `Lo-Fi Warm`.
  - 8 real studio bass presets (`32. Acoustic Bass` through `39. Synth Bass 2`) playable on the built-in piano keyboard with live looping bassline recording.
- **Desktop Keyboard Shortcuts**:
  - `Space` — Play / Stop
  - `F` — Trigger Drum Fill
  - `1`–`9` — Trigger Drum Pads
- **WAV & Standard MIDI Export**: Offline-render any pattern to 44.1 kHz stereo `.wav` or GM Channel 10 `.mid`.
- **Cross-Platform Persistence**: Custom presets, songs, and imported `.sf2` SoundFonts are stored in `~/.cowdrums/` on both Windows and Linux.

## Run or Build Locally

Requires **JDK 21** and **Gradle 8.8+**:

```bash
# Run directly on Linux or Windows
gradle run

# Run unit tests
gradle test

# Build standalone runnable UberJAR (works on your OS)
gradle packageUberJarForCurrentOS

# Build native installer (.deb on Linux, .msi/.exe on Windows)
gradle packageDistributionForCurrentOS
```

Prebuilt **Windows** (`.msi` + `.jar`) and **Linux** (`.deb` + `.jar`) artifacts are automatically built on every push via [GitHub Actions](https://github.com/OmgItsThecow/CowDrums-Desktop/actions).
