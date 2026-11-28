#define TSF_IMPLEMENTATION
#include "tsf.h"

#include "PluginProcessor.h"
#include "PluginEditor.h"
#include "BinaryData.h"

#include <cmath>

const std::array<int, CowDrumsProcessor::NUM_INSTRUMENTS> CowDrumsProcessor::GM_NOTES = {
    36, 38, 37, 39, 42, 46, 45, 47, 50, 49, 51, 56
};

const std::array<const char*, CowDrumsProcessor::NUM_INSTRUMENTS> CowDrumsProcessor::PAD_LABELS = {
    "BD", "SN", "RM", "CL", "HH", "OH", "HFT", "LMT", "HT", "CR", "RD", "CB"
};

const std::array<KitInfo, CowDrumsProcessor::NUM_KITS> CowDrumsProcessor::BUILTIN_KITS = {{
    {"0. STANDARD STUDIO", 0},
    {"8. LIVE ROOM ROCK", 8},
    {"16. POWER ROCK & METAL", 16},
    {"32. JAZZ ACOUSTIC", 32},
    {"40. JAZZ BRUSH", 40},
    {"25. TR-808 / 909", 25},
    {"26. CLUB DANCE", 26},
    {"24. ELECTRONIC", 24},
    {"1. STANDARD 2 / LATIN", 1},
    {"48. ORCHESTRAL", 48},
    {"0. LO-FI WARM", 0},
    {"56. INDIE & POST-PUNK", 56},
    {"57. 80S SYNTHWAVE", 57},
    {"58. REGGAE / DUB", 58},
    {"59. TECHNO / PSY 909", 59},
    {"60. BOOM-BAP MPC", 60}
}};

const std::vector<GmSoundInfo>& CowDrumsProcessor::getGmSoundCatalog() {
    static const std::vector<GmSoundInfo> catalog = {
        {35, "Acoustic Bass Drum"}, {36, "Bass Drum 1"}, {37, "Side Stick / Rimshot"},
        {38, "Acoustic Snare"}, {39, "Hand Clap"}, {40, "Electric Snare"},
        {41, "Low Floor Tom"}, {42, "Closed Hi-Hat"}, {43, "High Floor Tom"},
        {44, "Pedal Hi-Hat"}, {45, "Low Tom"}, {46, "Open Hi-Hat"},
        {47, "Low-Mid Tom"}, {48, "Hi-Mid Tom"}, {49, "Crash Cymbal 1"},
        {50, "High Tom"}, {51, "Ride Cymbal 1"}, {52, "Chinese Cymbal"},
        {53, "Ride Bell"}, {54, "Tambourine"}, {55, "Splash Cymbal"},
        {56, "Cowbell"}, {57, "Crash Cymbal 2"}, {59, "Ride Cymbal 2"},
        {60, "Hi Bongo"}, {61, "Low Bongo"}, {62, "Mute Hi Conga"},
        {63, "Open Hi Conga"}, {64, "Low Conga"}, {65, "High Timbale"},
        {66, "Low Timbale"}, {69, "Cabasa"}, {70, "Maracas"},
        {75, "Claves"}, {81, "Open Triangle"}
    };
    return catalog;
}

CowDrumsProcessor::CowDrumsProcessor()
    : AudioProcessor(BusesProperties()
          .withOutput("Output", juce::AudioChannelSet::stereo(), true))
{
    for (int i = 0; i < NUM_INSTRUMENTS; ++i) {
        padConfigs[static_cast<size_t>(i)].soundGmNote = GM_NOTES[static_cast<size_t>(i)];
        padConfigs[static_cast<size_t>(i)].midiInputNote = GM_NOTES[static_cast<size_t>(i)];
        padConfigs[static_cast<size_t>(i)].chokeGroup = (i == 4 || i == 5) ? 1 : 0; // CHH / OHH
        padConfigs[static_cast<size_t>(i)].kitOverride = -1;
        padConfigs[static_cast<size_t>(i)].noteRepeatMode = 0;
    }

    loadPresetsFromBinary();
    initBundledSoundFont();
    if (!presets.empty()) {
        selectPreset(0, true);
    }
}

CowDrumsProcessor::~CowDrumsProcessor() {
    std::lock_guard<std::mutex> lock(synthMutex);
    if (soundFont != nullptr) {
        tsf_close(soundFont);
        soundFont = nullptr;
    }
}

void CowDrumsProcessor::syncSingleChannelPresetLocked(int inst) {
    if (soundFont == nullptr || inst < 0 || inst >= NUM_INSTRUMENTS) return;
    int kitIdx = padConfigs[static_cast<size_t>(inst)].kitOverride;
    if (kitIdx < 0 || kitIdx >= NUM_KITS) kitIdx = activeKitIdx.load();
    int presetNum = BUILTIN_KITS[static_cast<size_t>(kitIdx)].sf2Preset;
    if (tsf_get_presetindex(soundFont, 128, presetNum) >= 0) {
        tsf_channel_set_bank_preset(soundFont, inst, 128, presetNum);
    } else if (tsf_get_presetindex(soundFont, 128, 0) >= 0) {
        tsf_channel_set_bank_preset(soundFont, inst, 128, 0);
    } else {
        tsf_channel_set_presetindex(soundFont, inst, 0);
    }
}

void CowDrumsProcessor::syncAllChannelPresetsLocked() {
    if (soundFont == nullptr) return;
    for (int i = 0; i < NUM_INSTRUMENTS; ++i) {
        syncSingleChannelPresetLocked(i);
    }
    // Channel 12: active kit for raw/unmapped GM MIDI drum notes
    int presetNum = BUILTIN_KITS[static_cast<size_t>(activeKitIdx.load())].sf2Preset;
    if (tsf_get_presetindex(soundFont, 128, presetNum) >= 0) {
        tsf_channel_set_bank_preset(soundFont, 12, 128, presetNum);
    } else if (tsf_get_presetindex(soundFont, 128, 0) >= 0) {
        tsf_channel_set_bank_preset(soundFont, 12, 128, 0);
    } else {
        tsf_channel_set_presetindex(soundFont, 12, 0);
    }
}

void CowDrumsProcessor::initBundledSoundFont() {
    std::lock_guard<std::mutex> lock(synthMutex);
    if (soundFont != nullptr) {
        tsf_close(soundFont);
        soundFont = nullptr;
    }
    soundFont = tsf_load_memory(
        BinaryData::GeneralUser_GS_Drums_sf2,
        BinaryData::GeneralUser_GS_Drums_sf2Size
    );
    if (soundFont != nullptr) {
        tsf_set_output(soundFont, TSF_STEREO_INTERLEAVED, static_cast<int>(currentSampleRate), 0.0f);
        tsf_set_max_voices(soundFont, 64);
        syncAllChannelPresetsLocked();
    }
    customSf2Name = "Default (GeneralUser GS)";
}

bool CowDrumsProcessor::loadExternalSoundFont(const juce::File& file) {
    if (!file.existsAsFile()) return false;
    tsf* loaded = tsf_load_filename(file.getFullPathName().toRawUTF8());
    if (loaded == nullptr) return false;

    std::lock_guard<std::mutex> lock(synthMutex);
    if (soundFont != nullptr) tsf_close(soundFont);
    soundFont = loaded;
    tsf_set_output(soundFont, TSF_STEREO_INTERLEAVED, static_cast<int>(currentSampleRate), 0.0f);
    tsf_set_max_voices(soundFont, 64);
    syncAllChannelPresetsLocked();
    customSf2Name = file.getFileNameWithoutExtension();
    return true;
}

juce::String CowDrumsProcessor::getActiveSf2Label() const {
    return customSf2Name;
}

void CowDrumsProcessor::prepareToPlay(double sampleRate, int /*samplesPerBlock*/) {
    currentSampleRate = sampleRate > 8000.0 ? sampleRate : 44100.0;
    std::lock_guard<std::mutex> lock(synthMutex);
    if (soundFont != nullptr) {
        tsf_set_output(soundFont, TSF_STEREO_INTERLEAVED, static_cast<int>(currentSampleRate), 0.0f);
    }
}

void CowDrumsProcessor::releaseResources() {}

bool CowDrumsProcessor::isBusesLayoutSupported(const BusesLayout& layouts) const {
    return layouts.getMainOutputChannelSet() == juce::AudioChannelSet::stereo();
}

void CowDrumsProcessor::selectPreset(int index, bool autoMatchKit) {
    if (presets.empty()) return;
    int safeIdx = juce::jlimit(0, static_cast<int>(presets.size()) - 1, index);
    activePresetIdx.store(safeIdx);
    const auto& p = presets[static_cast<size_t>(safeIdx)];
    internalBpm.store(static_cast<float>(p.bpm));
    swingAmount.store(p.swing);
    baseSlot.store(0);
    playingSlot.store(0);
    if (autoMatchKit) {
        selectKit(p.recommendedKitIdx);
    }
}

void CowDrumsProcessor::selectKit(int kitIndex) {
    int safeKit = juce::jlimit(0, NUM_KITS - 1, kitIndex);
    activeKitIdx.store(safeKit);
    std::lock_guard<std::mutex> lock(synthMutex);
    // Clear per-pad sound overrides when user selects a whole new kit
    for (int i = 0; i < NUM_INSTRUMENTS; ++i) {
        padConfigs[static_cast<size_t>(i)].kitOverride = -1;
        padConfigs[static_cast<size_t>(i)].soundGmNote = GM_NOTES[static_cast<size_t>(i)];
        padConfigs[static_cast<size_t>(i)].customWavName.clear();
        padCustomWavs[static_cast<size_t>(i)].clear();
    }
    syncAllChannelPresetsLocked();
}

void CowDrumsProcessor::setSlot(int slot) {
    int s = juce::jlimit(0, 2, slot);
    if (s == 2 && isCurrentlyPlaying.load()) {
        fillQueued.store(true);
        return;
    }
    baseSlot.store(s == 2 ? 0 : s);
    playingSlot.store(s);
}

void CowDrumsProcessor::requestFill() {
    if (isCurrentlyPlaying.load()) {
        fillQueued.store(true);
    } else {
        playingSlot.store(2);
    }
}

void CowDrumsProcessor::setInternalPlaying(bool play) {
    internalPlaying.store(play);
    if (!play) {
        fillQueued.store(false);
    }
}

void CowDrumsProcessor::triggerPadManual(int instrumentIndex, float velocity) {
    if (instrumentIndex < 0 || instrumentIndex >= NUM_INSTRUMENTS) return;
    padFlashMask.fetch_or(1u << static_cast<uint32_t>(instrumentIndex));
    std::lock_guard<std::mutex> lock(synthMutex);
    triggerInstrumentLocked(instrumentIndex, velocity);
}

void CowDrumsProcessor::setPadHeld(int instrumentIndex, bool held, float velocity) {
    if (instrumentIndex < 0 || instrumentIndex >= NUM_INSTRUMENTS) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    padHeld[static_cast<size_t>(instrumentIndex)] = held;
    padHeldVel[static_cast<size_t>(instrumentIndex)] = velocity;
    if (held) {
        triggerInstrumentLocked(instrumentIndex, velocity);
        padFlashMask.fetch_or(1u << static_cast<uint32_t>(instrumentIndex));
        int mode = padConfigs[static_cast<size_t>(instrumentIndex)].noteRepeatMode;
        if (mode > 0) {
            double divisor = (mode == 1) ? 2.0 : (mode == 2 ? 4.0 : (mode == 3 ? 8.0 : 6.0));
            double bpm = juce::jlimit(40.0, 240.0, static_cast<double>(effectiveBpm.load()));
            padRepeatCountdown[static_cast<size_t>(instrumentIndex)] = (60.0 / bpm / divisor) * currentSampleRate;
        }
    } else {
        padRepeatCountdown[static_cast<size_t>(instrumentIndex)] = 0.0;
    }
}

// ==================== PAD CONTEXT MENU API (1, 3, 4) ====================

PadConfigData CowDrumsProcessor::getPadConfig(int inst) {
    std::lock_guard<std::mutex> lock(synthMutex);
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return {};
    return padConfigs[static_cast<size_t>(inst)];
}

void CowDrumsProcessor::setPadSoundGmNote(int inst, int gmNote) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    padConfigs[static_cast<size_t>(inst)].soundGmNote = juce::jlimit(0, 127, gmNote);
    padConfigs[static_cast<size_t>(inst)].customWavName.clear();
    padCustomWavs[static_cast<size_t>(inst)].clear();
    triggerInstrumentLocked(inst, 0.92f);
    padFlashMask.fetch_or(1u << static_cast<uint32_t>(inst));
}

void CowDrumsProcessor::setPadKitOverride(int inst, int kitIdx) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    padConfigs[static_cast<size_t>(inst)].kitOverride = juce::jlimit(-1, NUM_KITS - 1, kitIdx);
    padConfigs[static_cast<size_t>(inst)].customWavName.clear();
    padCustomWavs[static_cast<size_t>(inst)].clear();
    syncSingleChannelPresetLocked(inst);
    triggerInstrumentLocked(inst, 0.92f);
    padFlashMask.fetch_or(1u << static_cast<uint32_t>(inst));
}

bool CowDrumsProcessor::loadCustomWavForPad(int inst, const juce::File& file) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS || !file.existsAsFile()) return false;
    juce::AudioFormatManager fm;
    fm.registerBasicFormats();
    std::unique_ptr<juce::AudioFormatReader> reader(fm.createReaderFor(file));
    if (reader == nullptr || reader->lengthInSamples <= 0) return false;

    const int maxInSamples = static_cast<int>(std::min<juce::int64>(reader->lengthInSamples, static_cast<juce::int64>(reader->sampleRate * 4.0)));
    juce::AudioBuffer<float> tmp(static_cast<int>(reader->numChannels), maxInSamples);
    reader->read(&tmp, 0, maxInSamples, 0, true, true);

    std::vector<float> mono(static_cast<size_t>(maxInSamples), 0.0f);
    for (int ch = 0; ch < tmp.getNumChannels(); ++ch) {
        const float* src = tmp.getReadPointer(ch);
        for (int s = 0; s < maxInSamples; ++s) {
            mono[static_cast<size_t>(s)] += src[s] / static_cast<float>(tmp.getNumChannels());
        }
    }

    // Resample linearly to currentSampleRate if needed
    std::vector<float> finalPcm;
    double ratio = reader->sampleRate / (currentSampleRate > 8000.0 ? currentSampleRate : 44100.0);
    if (std::abs(ratio - 1.0) > 1e-3) {
        size_t outLen = std::max<size_t>(32, static_cast<size_t>(mono.size() / ratio));
        finalPcm.resize(outLen);
        for (size_t i = 0; i < outLen; ++i) {
            double pos = static_cast<double>(i) * ratio;
            size_t idx = std::min(mono.size() - 1, static_cast<size_t>(pos));
            size_t next = std::min(mono.size() - 1, idx + 1);
            float frac = static_cast<float>(pos - static_cast<double>(idx));
            finalPcm[i] = mono[idx] * (1.0f - frac) + mono[next] * frac;
        }
    } else {
        finalPcm = std::move(mono);
    }

    // Normalize
    float peak = 0.0f;
    for (float v : finalPcm) peak = std::max(peak, std::fabs(v));
    if (peak < 1e-5f) return false;
    float norm = 0.92f / peak;
    for (float& v : finalPcm) v *= norm;

    std::lock_guard<std::mutex> lock(synthMutex);
    padCustomWavs[static_cast<size_t>(inst)] = std::move(finalPcm);
    padConfigs[static_cast<size_t>(inst)].customWavName = file.getFileNameWithoutExtension();
    padConfigs[static_cast<size_t>(inst)].kitOverride = -1;
    triggerInstrumentLocked(inst, 0.92f);
    padFlashMask.fetch_or(1u << static_cast<uint32_t>(inst));
    return true;
}

void CowDrumsProcessor::resetPadSound(int inst) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    padConfigs[static_cast<size_t>(inst)].soundGmNote = GM_NOTES[static_cast<size_t>(inst)];
    padConfigs[static_cast<size_t>(inst)].kitOverride = -1;
    padConfigs[static_cast<size_t>(inst)].customWavName.clear();
    padCustomWavs[static_cast<size_t>(inst)].clear();
    syncSingleChannelPresetLocked(inst);
    triggerInstrumentLocked(inst, 0.90f);
    padFlashMask.fetch_or(1u << static_cast<uint32_t>(inst));
}

void CowDrumsProcessor::setPadChokeGroup(int inst, int group) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    padConfigs[static_cast<size_t>(inst)].chokeGroup = juce::jlimit(0, 3, group);
}

void CowDrumsProcessor::startMidiLearn(int inst) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return;
    midiLearnPad.store(inst);
}

void CowDrumsProcessor::cancelMidiLearn() {
    midiLearnPad.store(-1);
}

void CowDrumsProcessor::setPadMidiInputNote(int inst, int midiNote) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    padConfigs[static_cast<size_t>(inst)].midiInputNote = juce::jlimit(0, 127, midiNote);
    if (midiLearnPad.load() == inst) midiLearnPad.store(-1);
}

void CowDrumsProcessor::setPadNoteRepeat(int inst, int mode) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    padConfigs[static_cast<size_t>(inst)].noteRepeatMode = juce::jlimit(0, 4, mode);
}

void CowDrumsProcessor::clearPadInActiveSlot(int inst) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS || presets.empty()) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    int pIdx = juce::jlimit(0, static_cast<int>(presets.size()) - 1, activePresetIdx.load());
    int slot = playingSlot.load();
    auto& preset = presets[static_cast<size_t>(pIdx)];
    auto& pat = (slot == 0) ? preset.mainA : (slot == 1 ? preset.varB : preset.fill);
    pat.grid[static_cast<size_t>(inst)].fill(0);
}

void CowDrumsProcessor::fillPadEveryNSteps(int inst, int everyN) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS || presets.empty()) return;
    int stride = std::max(1, everyN);
    std::lock_guard<std::mutex> lock(synthMutex);
    int pIdx = juce::jlimit(0, static_cast<int>(presets.size()) - 1, activePresetIdx.load());
    int slot = playingSlot.load();
    auto& preset = presets[static_cast<size_t>(pIdx)];
    auto& pat = (slot == 0) ? preset.mainA : (slot == 1 ? preset.varB : preset.fill);
    int stepCount = std::max(1, pat.stepCount);
    for (int s = 0; s < stepCount; ++s) {
        if (s % stride == 0) {
            pat.grid[static_cast<size_t>(inst)][static_cast<size_t>(s)] = (s % 4 == 0) ? 3 : 2;
        } else {
            pat.grid[static_cast<size_t>(inst)][static_cast<size_t>(s)] = 0;
        }
    }
}

void CowDrumsProcessor::humanizePadInActiveSlot(int inst) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS || presets.empty()) return;
    std::lock_guard<std::mutex> lock(synthMutex);
    int pIdx = juce::jlimit(0, static_cast<int>(presets.size()) - 1, activePresetIdx.load());
    int slot = playingSlot.load();
    auto& preset = presets[static_cast<size_t>(pIdx)];
    auto& pat = (slot == 0) ? preset.mainA : (slot == 1 ? preset.varB : preset.fill);
    int stepCount = std::max(1, pat.stepCount);
    for (int s = 0; s < stepCount; ++s) {
        int& v = pat.grid[static_cast<size_t>(inst)][static_cast<size_t>(s)];
        if (v <= 0) continue;
        humanizeSeed ^= humanizeSeed << 13;
        humanizeSeed ^= humanizeSeed >> 17;
        humanizeSeed ^= humanizeSeed << 5;
        uint32_t r = humanizeSeed % 100u;
        if (s % 4 == 0) {
            v = (r < 72u) ? 3 : 2;
        } else {
            v = (r < 35u) ? 1 : ((r < 85u) ? 2 : 3);
        }
    }
}

int CowDrumsProcessor::getCurrentStepCount() const {
    if (presets.empty()) return 16;
    int idx = juce::jlimit(0, static_cast<int>(presets.size()) - 1, activePresetIdx.load());
    return presets[static_cast<size_t>(idx)].mainA.stepCount;
}

juce::File CowDrumsProcessor::exportActivePatternToTempMidiFile() {
    if (presets.empty()) return {};
    int idx = juce::jlimit(0, static_cast<int>(presets.size()) - 1, activePresetIdx.load());
    const auto& preset = presets[static_cast<size_t>(idx)];

    // Export a complete 4-bar phrase containing ALL 3 sections:
    // Bar 1: MAIN A | Bar 2: MAIN A | Bar 3: VAR B | Bar 4: FILL
    const std::array<const StepPatternData*, 4> bars = {
        &preset.mainA, &preset.mainA, &preset.varB, &preset.fill
    };

    const int tpqn = 480;
    const int baseStepTicks = tpqn / 4; // 120 ticks per 16th note
    const double swingTicks = static_cast<double>(swingAmount.load()) * 0.45 * baseStepTicks;
    const double bpm = juce::jlimit(40.0, 240.0, static_cast<double>(effectiveBpm.load()));

    juce::MidiMessageSequence seq;
    seq.addEvent(juce::MidiMessage::tempoMetaEvent(static_cast<int>(60000000.0 / bpm)), 0.0);

    double tickCursor = 0.0;
    for (const StepPatternData* patPtr : bars) {
        const StepPatternData& pat = *patPtr;
        int stepCount = std::max(1, pat.stepCount);
        for (int s = 0; s < stepCount; ++s) {
            for (int inst = 0; inst < NUM_INSTRUMENTS; ++inst) {
                int velLevel = pat.grid[static_cast<size_t>(inst)][static_cast<size_t>(s)];
                if (velLevel <= 0) continue;
                juce::uint8 vel = (velLevel == 1) ? 52 : (velLevel == 2 ? 94 : 124);
                int note = padConfigs[static_cast<size_t>(inst)].midiInputNote;
                // MIDI Channel 10 (1-indexed in JUCE) = GM Drums
                seq.addEvent(juce::MidiMessage::noteOn(10, note, vel), tickCursor);
                seq.addEvent(juce::MidiMessage::noteOff(10, note), tickCursor + 40.0);
            }
            double dur = (s % 2 == 0)
                ? (baseStepTicks + swingTicks)
                : std::max(30.0, baseStepTicks - swingTicks);
            tickCursor += dur;
        }
    }

    seq.sort();
    seq.updateMatchedPairs();

    juce::MidiFile midiFile;
    midiFile.setTicksPerQuarterNote(tpqn);
    midiFile.addTrack(seq);

    juce::String safeName = preset.name.retainCharacters("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-");
    if (safeName.isEmpty()) safeName = "CowDrums_Pattern";
    juce::String fileName = "CowDrums_" + safeName + "_A-B-Fill_" + juce::String(static_cast<int>(bpm)) + "bpm.mid";

    juce::File outDir = juce::File::getSpecialLocation(juce::File::tempDirectory).getChildFile("CowDrums_MIDI");
    outDir.createDirectory();
    juce::File outFile = outDir.getChildFile(fileName);
    outFile.deleteFile();

    juce::FileOutputStream fos(outFile);
    if (fos.openedOk()) {
        midiFile.writeTo(fos, 0);
        fos.flush();
    }

    // Automatically turn off internal sequencer / host sync when dragging MIDI to DAW
    // so the dropped MIDI clip plays cleanly without conflicting with the internal pattern!
    hostSync.store(false);
    internalPlaying.store(false);
    fillQueued.store(false);

    return outFile;
}

void CowDrumsProcessor::triggerInstrumentLocked(int inst, float velocity) {
    if (inst < 0 || inst >= NUM_INSTRUMENTS) return;
    const auto& cfg = padConfigs[static_cast<size_t>(inst)];
    float vel = juce::jlimit(0.05f, 1.0f, velocity);

    // Choke all instruments sharing the same non-zero choke group
    if (cfg.chokeGroup > 0) {
        if (soundFont != nullptr) {
            for (int other = 0; other < NUM_INSTRUMENTS; ++other) {
                if (padConfigs[static_cast<size_t>(other)].chokeGroup == cfg.chokeGroup) {
                    tsf_channel_note_off_all(soundFont, other);
                }
            }
        }
        for (auto& v : wavVoices) {
            if (v.active && v.chokeGroup == cfg.chokeGroup) {
                v.active = false;
            }
        }
    }

    // Play custom WAV if loaded on this pad
    const auto& wav = padCustomWavs[static_cast<size_t>(inst)];
    if (!wav.empty()) {
        WavVoice* target = nullptr;
        for (size_t i = 0; i < wavVoices.size(); ++i) {
            size_t idx = (static_cast<size_t>(wavVoiceCursor) + i) % wavVoices.size();
            if (!wavVoices[idx].active) {
                target = &wavVoices[idx];
                wavVoiceCursor = static_cast<int>((idx + 1) % wavVoices.size());
                break;
            }
        }
        if (target == nullptr) {
            target = &wavVoices[static_cast<size_t>(wavVoiceCursor)];
            wavVoiceCursor = (wavVoiceCursor + 1) % static_cast<int>(wavVoices.size());
        }
        target->active = true;
        target->inst = inst;
        target->pos = 0;
        target->gain = vel;
        target->chokeGroup = cfg.chokeGroup;
        return;
    }

    if (soundFont != nullptr) {
        tsf_channel_note_on(soundFont, inst, cfg.soundGmNote, vel);
    }
}

void CowDrumsProcessor::triggerRawNoteLocked(int midiNote, float velocity) {
    if (soundFont == nullptr) return;
    if (midiNote == 42) {
        tsf_channel_note_off(soundFont, 12, 46);
    }
    tsf_channel_note_on(soundFont, 12, midiNote, juce::jlimit(0.05f, 1.0f, velocity));
}

void CowDrumsProcessor::processBlock(juce::AudioBuffer<float>& buffer, juce::MidiBuffer& midiMessages) {
    juce::ScopedNoDenormals noDenormals;
    buffer.clear();

    const int numSamples = buffer.getNumSamples();
    if (numSamples <= 0) return;

    // Query DAW playhead if Host Sync is active
    bool hostIsPlaying = false;
    double bpmNow = static_cast<double>(internalBpm.load());
    if (auto* ph = getPlayHead()) {
        if (auto pos = ph->getPosition()) {
            if (hostSync.load() && pos->getBpm().hasValue() && *pos->getBpm() > 20.0) {
                bpmNow = *pos->getBpm();
            }
            hostIsPlaying = pos->getIsPlaying();
        }
    }

    std::lock_guard<std::mutex> lock(synthMutex);

    // Handle incoming DAW / Bluetooth / USB MIDI controller notes
    for (const auto metadata : midiMessages) {
        const auto msg = metadata.getMessage();
        if (msg.isNoteOn()) {
            int note = msg.getNoteNumber();
            float vel = msg.getFloatVelocity();

            // Check MIDI Learn first
            int learnIdx = midiLearnPad.load();
            if (learnIdx >= 0 && learnIdx < NUM_INSTRUMENTS) {
                padConfigs[static_cast<size_t>(learnIdx)].midiInputNote = note;
                midiLearnPad.store(-1);
                triggerInstrumentLocked(learnIdx, vel);
                padFlashMask.fetch_or(1u << static_cast<uint32_t>(learnIdx));
                continue;
            }

            bool matched = false;
            for (int i = 0; i < NUM_INSTRUMENTS; ++i) {
                if (padConfigs[static_cast<size_t>(i)].midiInputNote == note) {
                    triggerInstrumentLocked(i, vel);
                    padFlashMask.fetch_or(1u << static_cast<uint32_t>(i));
                    matched = true;
                }
            }
            if (!matched) {
                triggerRawNoteLocked(note, vel);
            }
        }
    }

    bool playNow = internalPlaying.load() || (hostSync.load() && hostIsPlaying);
    isCurrentlyPlaying.store(playNow);
    effectiveBpm.store(static_cast<float>(bpmNow));

    if (playNow && !wasPlaying) {
        stepIndex = 0;
        barCounter = 0;
        samplesUntilNextStep = 0.0;
        activeFillThisBar = false;
    } else if (!playNow && wasPlaying) {
        currentStep.store(-1);
        activeFillThisBar = false;
        fillQueued.store(false);
    }
    wasPlaying = playNow;

    // Render in small 64-sample sub-blocks so step triggers & note repeat are sample-tight (<1.5ms)
    const int chunkSize = 64;
    float interleaved[chunkSize * 2];
    float* outL = buffer.getWritePointer(0);
    float* outR = buffer.getNumChannels() > 1 ? buffer.getWritePointer(1) : nullptr;
    const float gain = masterGain.load();
    const float swing = swingAmount.load();

    int offset = 0;
    while (offset < numSamples) {
        int todo = std::min(chunkSize, numSamples - offset);

        // Process hold-to-repeat pads
        for (int inst = 0; inst < NUM_INSTRUMENTS; ++inst) {
            int mode = padConfigs[static_cast<size_t>(inst)].noteRepeatMode;
            if (padHeld[static_cast<size_t>(inst)] && mode > 0) {
                padRepeatCountdown[static_cast<size_t>(inst)] -= static_cast<double>(todo);
                if (padRepeatCountdown[static_cast<size_t>(inst)] <= 0.0) {
                    triggerInstrumentLocked(inst, padHeldVel[static_cast<size_t>(inst)]);
                    padFlashMask.fetch_or(1u << static_cast<uint32_t>(inst));
                    double divisor = (mode == 1) ? 2.0 : (mode == 2 ? 4.0 : (mode == 3 ? 8.0 : 6.0));
                    double interval = (60.0 / juce::jlimit(40.0, 240.0, bpmNow) / divisor) * currentSampleRate;
                    padRepeatCountdown[static_cast<size_t>(inst)] += std::max(512.0, interval);
                }
            }
        }

        if (playNow && !presets.empty()) {
            samplesUntilNextStep -= static_cast<double>(todo);
            if (samplesUntilNextStep <= 0.0) {
                int pIdx = juce::jlimit(0, static_cast<int>(presets.size()) - 1, activePresetIdx.load());
                const auto& preset = presets[static_cast<size_t>(pIdx)];

                bool autoFill = autoFill4B.load() && (((barCounter + 1) % 4) == 0);
                if (fillQueued.load() || (autoFill && stepIndex == 0)) {
                    activeFillThisBar = true;
                }
                int slotToPlay = activeFillThisBar ? 2 : baseSlot.load();
                playingSlot.store(slotToPlay);

                const StepPatternData& pat = (slotToPlay == 0) ? preset.mainA : (slotToPlay == 1 ? preset.varB : preset.fill);
                int stepCount = std::max(1, pat.stepCount);
                int safeStep = stepIndex % stepCount;

                for (int inst = 0; inst < NUM_INSTRUMENTS; ++inst) {
                    int velLevel = pat.grid[static_cast<size_t>(inst)][static_cast<size_t>(safeStep)];
                    if (velLevel > 0) {
                        float baseV = (velLevel == 1) ? 0.38f : (velLevel == 2 ? 0.74f : 1.0f);
                        humanizeSeed ^= humanizeSeed << 13;
                        humanizeSeed ^= humanizeSeed >> 17;
                        humanizeSeed ^= humanizeSeed << 5;
                        float h = (((humanizeSeed & 0xFFFFu) / 65535.0f) * 2.0f - 1.0f) * 0.05f;
                        triggerInstrumentLocked(inst, baseV * (1.0f + h));
                        padFlashMask.fetch_or(1u << static_cast<uint32_t>(inst));
                    }
                }

                currentStep.store(safeStep);

                double baseStepFrames = (60.0 / juce::jlimit(40.0, 240.0, bpmNow) / 4.0) * currentSampleRate;
                double swingOffset = (swing * 0.45) * baseStepFrames;
                double stepDur = (safeStep % 2 == 0)
                    ? (baseStepFrames + swingOffset)
                    : std::max(baseStepFrames * 0.25, baseStepFrames - swingOffset);
                samplesUntilNextStep += stepDur;

                stepIndex = (safeStep + 1) % stepCount;
                if (stepIndex == 0) {
                    barCounter++;
                    activeFillThisBar = false;
                    fillQueued.store(false);
                }
            }
        }

        if (soundFont != nullptr) {
            tsf_render_float(soundFont, interleaved, todo, 0);
        } else {
            std::fill_n(interleaved, todo * 2, 0.0f);
        }

        // Mix any active custom WAV voices
        for (auto& v : wavVoices) {
            if (!v.active || v.inst < 0 || v.inst >= NUM_INSTRUMENTS) continue;
            const auto& pcm = padCustomWavs[static_cast<size_t>(v.inst)];
            if (pcm.empty()) { v.active = false; continue; }
            for (int i = 0; i < todo; ++i) {
                if (v.pos >= pcm.size()) {
                    v.active = false;
                    break;
                }
                float s = pcm[v.pos++] * v.gain;
                interleaved[i * 2] += s;
                interleaved[i * 2 + 1] += s;
            }
        }

        for (int i = 0; i < todo; ++i) {
            float l = std::tanh(interleaved[i * 2] * gain);
            float r = std::tanh(interleaved[i * 2 + 1] * gain);
            outL[offset + i] = l;
            if (outR != nullptr) outR[offset + i] = r;
        }
        offset += todo;
    }
}

void CowDrumsProcessor::getStateInformation(juce::MemoryBlock& destData) {
    juce::DynamicObject::Ptr obj = new juce::DynamicObject();
    obj->setProperty("presetIdx", activePresetIdx.load());
    obj->setProperty("kitIdx", activeKitIdx.load());
    obj->setProperty("baseSlot", baseSlot.load());
    obj->setProperty("autoFill4B", autoFill4B.load());
    obj->setProperty("hostSync", hostSync.load());
    obj->setProperty("internalBpm", internalBpm.load());
    obj->setProperty("swing", swingAmount.load());
    obj->setProperty("masterGain", masterGain.load());

    juce::Array<juce::var> padArr;
    {
        std::lock_guard<std::mutex> lock(synthMutex);
        for (int i = 0; i < NUM_INSTRUMENTS; ++i) {
            const auto& c = padConfigs[static_cast<size_t>(i)];
            juce::DynamicObject::Ptr po = new juce::DynamicObject();
            po->setProperty("gm", c.soundGmNote);
            po->setProperty("kit", c.kitOverride);
            po->setProperty("choke", c.chokeGroup);
            po->setProperty("midi", c.midiInputNote);
            po->setProperty("rep", c.noteRepeatMode);
            padArr.add(juce::var(po.get()));
        }
    }
    obj->setProperty("padConfigs", padArr);

    juce::String json = juce::JSON::toString(juce::var(obj.get()));
    destData.replaceAll(json.toRawUTF8(), json.getNumBytesAsUTF8());
}

void CowDrumsProcessor::setStateInformation(const void* data, int sizeInBytes) {
    if (data == nullptr || sizeInBytes <= 0) return;
    juce::String text = juce::String::fromUTF8(static_cast<const char*>(data), sizeInBytes);
    auto parsed = juce::JSON::parse(text);
    if (auto* obj = parsed.getDynamicObject()) {
        selectPreset(static_cast<int>(obj->getProperty("presetIdx")), false);
        selectKit(static_cast<int>(obj->getProperty("kitIdx")));
        setSlot(static_cast<int>(obj->getProperty("baseSlot")));
        setAutoFill4B(static_cast<bool>(obj->getProperty("autoFill4B")));
        setHostSync(static_cast<bool>(obj->getProperty("hostSync")));
        setInternalBpm(static_cast<float>(obj->getProperty("internalBpm")));
        setSwing(static_cast<float>(obj->getProperty("swing")));
        setMasterGain(static_cast<float>(obj->getProperty("masterGain")));

        auto padVar = obj->getProperty("padConfigs");
        if (auto* arr = padVar.getArray()) {
            std::lock_guard<std::mutex> lock(synthMutex);
            int n = std::min(NUM_INSTRUMENTS, arr->size());
            for (int i = 0; i < n; ++i) {
                if (auto* po = (*arr)[i].getDynamicObject()) {
                    auto& c = padConfigs[static_cast<size_t>(i)];
                    c.soundGmNote = juce::jlimit(0, 127, static_cast<int>(po->getProperty("gm")));
                    c.kitOverride = juce::jlimit(-1, NUM_KITS - 1, static_cast<int>(po->getProperty("kit")));
                    c.chokeGroup = juce::jlimit(0, 3, static_cast<int>(po->getProperty("choke")));
                    c.midiInputNote = juce::jlimit(0, 127, static_cast<int>(po->getProperty("midi")));
                    c.noteRepeatMode = juce::jlimit(0, 4, static_cast<int>(po->getProperty("rep")));
                }
            }
            syncAllChannelPresetsLocked();
        }
    }
}

juce::AudioProcessorEditor* CowDrumsProcessor::createEditor() {
    return new CowDrumsEditor(*this);
}

void CowDrumsProcessor::loadPresetsFromBinary() {
    presets.clear();
    juce::String jsonStr = juce::String::fromUTF8(BinaryData::presets_json, BinaryData::presets_jsonSize);
    auto root = juce::JSON::parse(jsonStr);
    if (!root.isArray()) return;

    auto parsePattern = [](const juce::var& pv) -> StepPatternData {
        StepPatternData pat;
        if (auto* po = pv.getDynamicObject()) {
            pat.stepCount = juce::jlimit(1, 32, static_cast<int>(po->getProperty("stepCount")));
            auto rowsVar = po->getProperty("rows");
            if (auto* ro = rowsVar.getDynamicObject()) {
                for (const auto& prop : ro->getProperties()) {
                    int idx = instrumentNameToIndex(prop.name.toString());
                    if (idx < 0 || !prop.value.isArray()) continue;
                    auto* arr = prop.value.getArray();
                    int n = std::min(32, arr->size());
                    for (int s = 0; s < n; ++s) {
                        pat.grid[static_cast<size_t>(idx)][static_cast<size_t>(s)] = static_cast<int>((*arr)[s]);
                    }
                }
            }
        }
        return pat;
    };

    for (const auto& item : *root.getArray()) {
        auto* obj = item.getDynamicObject();
        if (obj == nullptr) continue;
        DrumPresetData p;
        p.id = obj->getProperty("id").toString();
        p.name = obj->getProperty("name").toString();
        p.genre = genreEnumToLabel(obj->getProperty("genre").toString());
        p.subcategory = obj->getProperty("subcategory").toString();
        if (p.subcategory.isEmpty()) p.subcategory = p.genre;
        p.bpm = static_cast<int>(obj->getProperty("bpm"));
        p.swing = static_cast<float>(obj->getProperty("swing"));
        p.recommendedKitIdx = kitNameToIndex(obj->getProperty("recommendedKit").toString());
        p.mainA = parsePattern(obj->getProperty("mainA"));
        p.varB = parsePattern(obj->getProperty("varB"));
        p.fill = parsePattern(obj->getProperty("fill"));
        presets.push_back(std::move(p));
    }
}

int CowDrumsProcessor::instrumentNameToIndex(const juce::String& name) {
    if (name == "KICK") return 0;
    if (name == "SNARE") return 1;
    if (name == "RIM") return 2;
    if (name == "CLAP") return 3;
    if (name == "CHH") return 4;
    if (name == "OHH") return 5;
    if (name == "LOW_TOM") return 6;
    if (name == "MID_TOM") return 7;
    if (name == "HIGH_TOM") return 8;
    if (name == "CRASH") return 9;
    if (name == "RIDE") return 10;
    if (name == "PERC") return 11;
    return -1;
}

int CowDrumsProcessor::kitNameToIndex(const juce::String& name) {
    if (name == "ACOUSTIC") return 0;
    if (name == "ROOM") return 1;
    if (name == "ROCK_METAL") return 2;
    if (name == "JAZZ_BRUSH") return 3;
    if (name == "BRUSH") return 4;
    if (name == "TR_808") return 5;
    if (name == "TR_909") return 6;
    if (name == "ELECTRONIC") return 7;
    if (name == "LATIN") return 8;
    if (name == "ORCHESTRAL") return 9;
    if (name == "LOFI") return 10;
    if (name == "INDIE_GARAGE") return 11;
    if (name == "SYNTHWAVE_80S") return 12;
    if (name == "REGGAE_DUB") return 13;
    if (name == "TECHNO_PSY") return 14;
    if (name == "BOOM_BAP") return 15;
    return 1;
}

juce::String CowDrumsProcessor::genreEnumToLabel(const juce::String& g) {
    if (g == "ROCK") return "Rock";
    if (g == "POP") return "Indie & Pop";
    if (g == "EDM") return "Electronic";
    if (g == "HIP_HOP") return "Rap & Hip-Hop";
    if (g == "REGGAE_SKA") return "Reggae & Ska";
    if (g == "FUNK") return "Funk & R&B";
    if (g == "JAZZ") return "Jazz & Blues";
    if (g == "LATIN") return "Latin";
    if (g == "ODD_TIME") return "Odd Time";
    return g;
}

juce::AudioProcessor* JUCE_CALLTYPE createPluginFilter() {
    return new CowDrumsProcessor();
}
