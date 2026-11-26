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

- **Bundled Native VST3 Plugin (`CowDrums.vst3`, Windows & Linux)**:
  - Built with **JUCE 8 + TinySoundFont** in [vst3/](vst3/), embedding all **95 presets** and the **GeneralUser GS** SoundFont directly into the plugin binary.
  - Supports **DAW Host Sync** (tempo + playhead), **live MIDI input** (GM percussion), **Main A / Var B / Fill / Auto-Fill 4B**, custom `.sf2` loading, and full DAW project state save/restore.
  - Bundled inside both the **Windows `.msi`** and **Linux `.deb`** installers — open CowDrums Desktop and click **`⋮ → Install VST3 Plugin to DAW`** to install `CowDrums.vst3` into `~/.vst3` (Linux) or `%LOCALAPPDATA%\Programs\Common\VST3` (Windows) in one click!

## Run or Build Locally

Requires **JDK 21**, **Gradle 8.8+**, and **CMake 3.22+ / C++17** (for VST3):

```bash
# 1. Build the native VST3 plugin (Linux or Windows)
cmake -B vst3/build -S vst3 -DCMAKE_BUILD_TYPE=Release
cmake --build vst3/build --target CowDrumsVST3_VST3 --config Release

# 2. Run the Desktop app
gradle run

# 3. Run unit tests
gradle test

# 4. Build native installer (.deb on Linux, .msi on Windows) with CowDrums.vst3 bundled
gradle packageDistributionForCurrentOS
```

Prebuilt **Windows** (`.msi` with VST3 + standalone `CowDrums.vst3` + `.jar`) and **Linux** (`.deb` with VST3 + standalone `CowDrums.vst3` + `.jar`) artifacts are automatically built on every push via [GitHub Actions](https://github.com/OmgItsThecow/CowDrums-Desktop/actions).
