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

CowDrumsProcessor::CowDrumsProcessor()
    : AudioProcessor(BusesProperties()
          .withOutput("Output", juce::AudioChannelSet::stereo(), true))
{
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
        int presetNum = BUILTIN_KITS[static_cast<size_t>(activeKitIdx.load())].sf2Preset;
        tsf_channel_set_bank_preset(soundFont, 9, 128, presetNum);
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
    int presetNum = BUILTIN_KITS[static_cast<size_t>(activeKitIdx.load())].sf2Preset;
    if (tsf_get_presetindex(soundFont, 128, presetNum) >= 0) {
        tsf_channel_set_bank_preset(soundFont, 9, 128, presetNum);
    } else if (tsf_get_presetindex(soundFont, 128, 0) >= 0) {
        tsf_channel_set_bank_preset(soundFont, 9, 128, 0);
    } else {
        tsf_channel_set_presetindex(soundFont, 9, 0);
    }
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
    if (soundFont != nullptr) {
        int presetNum = BUILTIN_KITS[static_cast<size_t>(safeKit)].sf2Preset;
        if (tsf_get_presetindex(soundFont, 128, presetNum) >= 0) {
            tsf_channel_set_bank_preset(soundFont, 9, 128, presetNum);
        } else if (tsf_get_presetindex(soundFont, 128, 0) >= 0) {
            tsf_channel_set_bank_preset(soundFont, 9, 128, 0);
        }
    }
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
    triggerNoteLocked(GM_NOTES[static_cast<size_t>(instrumentIndex)], velocity);
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
                int note = GM_NOTES[static_cast<size_t>(inst)];
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

void CowDrumsProcessor::triggerNoteLocked(int midiNote, float velocity) {
    if (soundFont == nullptr) return;
    // Closed HH (42) cleanly chokes Open HH (46)
    if (midiNote == 42) {
        tsf_channel_note_off(soundFont, 9, 46);
    }
    tsf_channel_note_on(soundFont, 9, midiNote, juce::jlimit(0.05f, 1.0f, velocity));
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

    // Handle incoming DAW / MIDI pad controller notes (always playable on top of patterns)
    for (const auto metadata : midiMessages) {
        const auto msg = metadata.getMessage();
        if (msg.isNoteOn()) {
            int note = msg.getNoteNumber();
            triggerNoteLocked(note, msg.getFloatVelocity());
            for (int i = 0; i < NUM_INSTRUMENTS; ++i) {
                if (GM_NOTES[static_cast<size_t>(i)] == note) {
                    padFlashMask.fetch_or(1u << static_cast<uint32_t>(i));
                    break;
                }
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

    if (soundFont == nullptr) return;

    // Render in small 64-sample sub-blocks so step triggers are sample-tight (<1.5ms)
    const int chunkSize = 64;
    float interleaved[chunkSize * 2];
    float* outL = buffer.getWritePointer(0);
    float* outR = buffer.getNumChannels() > 1 ? buffer.getWritePointer(1) : nullptr;
    const float gain = masterGain.load();
    const float swing = swingAmount.load();

    int offset = 0;
    while (offset < numSamples) {
        int todo = std::min(chunkSize, numSamples - offset);

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
                        // Subtle +-5% drummer humanization
                        humanizeSeed ^= humanizeSeed << 13;
                        humanizeSeed ^= humanizeSeed >> 17;
                        humanizeSeed ^= humanizeSeed << 5;
                        float h = (((humanizeSeed & 0xFFFFu) / 65535.0f) * 2.0f - 1.0f) * 0.05f;
                        triggerNoteLocked(GM_NOTES[static_cast<size_t>(inst)], baseV * (1.0f + h));
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

        tsf_render_float(soundFont, interleaved, todo, 0);
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
