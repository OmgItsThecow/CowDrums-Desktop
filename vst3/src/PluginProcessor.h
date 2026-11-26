#pragma once

#include <juce_audio_processors/juce_audio_processors.h>
#include <array>
#include <atomic>
#include <mutex>
#include <string>
#include <vector>

struct tsf;

struct StepPatternData {
    int stepCount = 16;
    // 12 instruments x up to 32 steps (velocity 0..3)
    std::array<std::array<int, 32>, 12> grid{};
};

struct DrumPresetData {
    juce::String id;
    juce::String name;
    juce::String genre;
    juce::String subcategory;
    int bpm = 120;
    float swing = 0.0f;
    int recommendedKitIdx = 1; // Room default
    StepPatternData mainA;
    StepPatternData varB;
    StepPatternData fill;
};

struct KitInfo {
    const char* name;
    int sf2Preset;
};

class CowDrumsProcessor : public juce::AudioProcessor {
public:
    static constexpr int NUM_INSTRUMENTS = 12;
    static constexpr int NUM_KITS = 11;

    static const std::array<int, NUM_INSTRUMENTS> GM_NOTES;
    static const std::array<const char*, NUM_INSTRUMENTS> PAD_LABELS;
    static const std::array<KitInfo, NUM_KITS> BUILTIN_KITS;

    CowDrumsProcessor();
    ~CowDrumsProcessor() override;

    void prepareToPlay(double sampleRate, int samplesPerBlock) override;
    void releaseResources() override;
    bool isBusesLayoutSupported(const BusesLayout& layouts) const override;
    void processBlock(juce::AudioBuffer<float>&, juce::MidiBuffer&) override;

    juce::AudioProcessorEditor* createEditor() override;
    bool hasEditor() const override { return true; }

    const juce::String getName() const override { return "CowDrums"; }
    bool acceptsMidi() const override { return true; }
    bool producesMidi() const override { return false; }
    bool isMidiEffect() const override { return false; }
    double getTailLengthSeconds() const override { return 1.5; }

    int getNumPrograms() override { return 1; }
    int getCurrentProgram() override { return 0; }
    void setCurrentProgram(int) override {}
    const juce::String getProgramName(int) override { return "Default"; }
    void changeProgramName(int, const juce::String&) override {}

    void getStateInformation(juce::MemoryBlock& destData) override;
    void setStateInformation(const void* data, int sizeInBytes) override;

    // UI Control API
    const std::vector<DrumPresetData>& getPresets() const { return presets; }
    void selectPreset(int index, bool autoMatchKit = true);
    int getActivePresetIndex() const { return activePresetIdx.load(); }

    void selectKit(int kitIndex);
    int getActiveKitIndex() const { return activeKitIdx.load(); }

    void setSlot(int slot); // 0 = Main A, 1 = Var B, 2 = Fill
    int getActiveSlot() const { return playingSlot.load(); }
    void requestFill();

    void setAutoFill4B(bool enabled) { autoFill4B.store(enabled); }
    bool isAutoFill4B() const { return autoFill4B.load(); }

    void setHostSync(bool enabled) { hostSync.store(enabled); }
    bool isHostSync() const { return hostSync.load(); }

    void setInternalPlaying(bool play);
    bool isPlayingNow() const { return isCurrentlyPlaying.load(); }

    void setInternalBpm(float bpm) { internalBpm.store(juce::jlimit(40.0f, 240.0f, bpm)); }
    float getEffectiveBpm() const { return effectiveBpm.load(); }

    void setSwing(float s) { swingAmount.store(juce::jlimit(0.0f, 0.70f, s)); }
    float getSwing() const { return swingAmount.load(); }

    void setMasterGain(float g) { masterGain.store(juce::jlimit(0.0f, 1.5f, g)); }
    float getMasterGain() const { return masterGain.load(); }

    void triggerPadManual(int instrumentIndex, float velocity = 0.92f);
    bool loadExternalSoundFont(const juce::File& file);
    juce::String getActiveSf2Label() const;
    juce::File exportActivePatternToTempMidiFile(int bars = 2) const;

    int getCurrentStep() const { return currentStep.load(); }
    int getCurrentStepCount() const;
    uint32_t consumePadFlashMask() { return padFlashMask.exchange(0); }

private:
    void loadPresetsFromBinary();
    void initBundledSoundFont();
    void triggerNoteLocked(int midiNote, float velocity);
    static int instrumentNameToIndex(const juce::String& name);
    static int kitNameToIndex(const juce::String& name);
    static juce::String genreEnumToLabel(const juce::String& genreEnum);

    std::vector<DrumPresetData> presets;
    std::mutex synthMutex;
    tsf* soundFont = nullptr;
    double currentSampleRate = 44100.0;
    juce::String customSf2Name;

    std::atomic<int> activePresetIdx{0};
    std::atomic<int> activeKitIdx{1}; // Room
    std::atomic<int> baseSlot{0};
    std::atomic<int> playingSlot{0};
    std::atomic<bool> fillQueued{false};
    std::atomic<bool> autoFill4B{false};
    std::atomic<bool> hostSync{true};
    std::atomic<bool> internalPlaying{false};
    std::atomic<bool> isCurrentlyPlaying{false};
    std::atomic<float> internalBpm{120.0f};
    std::atomic<float> effectiveBpm{120.0f};
    std::atomic<float> swingAmount{0.0f};
    std::atomic<float> masterGain{0.90f};
    std::atomic<int> currentStep{-1};
    std::atomic<uint32_t> padFlashMask{0};

    // Audio-thread sequencer state
    int stepIndex = 0;
    int barCounter = 0;
    double samplesUntilNextStep = 0.0;
    bool wasPlaying = false;
    bool activeFillThisBar = false;
    uint32_t humanizeSeed = 0x5A3C91E7u;

    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR(CowDrumsProcessor)
};
