#pragma once

#include "PluginProcessor.h"
#include <functional>

class CowDrumsEditor : public juce::AudioProcessorEditor,
                       private juce::Timer,
                       private juce::ListBoxModel {
public:
    explicit CowDrumsEditor(CowDrumsProcessor& p);
    ~CowDrumsEditor() override;

    void paint(juce::Graphics& g) override;
    void resized() override;

private:
    void timerCallback() override;

    // ListBoxModel for 100-preset list
    int getNumRows() override;
    void paintListBoxItem(int rowNumber, juce::Graphics& g, int width, int height, bool rowIsSelected) override;
    void listBoxItemClicked(int row, const juce::MouseEvent&) override;

    void rebuildGenreFilters();
    void rebuildSubcategoryFilters();
    void rebuildFilteredPresets();
    void refreshButtonColors();
    void showPadContextMenu(int inst);

    CowDrumsProcessor& processor;

    // Top Transport & Kit Bar
    juce::TextButton playButton{"PLAY"};
    juce::ToggleButton hostSyncToggle{"HOST SYNC"};
    juce::Slider bpmSlider;
    juce::Label bpmLabel;
    juce::TextButton prevKitButton{"<"};
    juce::Label kitLabel;
    juce::TextButton nextKitButton{">"};
    juce::TextButton loadSf2Button{"Load .SF2"};

    // Left Preset Browser (Genre -> Subcategory -> List)
    juce::ComboBox genreBox;
    juce::ComboBox subcatBox;
    juce::ListBox presetList;
    std::vector<int> filteredPresetIndices;

    // Custom Pad Component supporting left-click hold (velocity + note repeat) & right-click context menu
    class PadComponent : public juce::Component {
    public:
        void configure(
            const juce::String& titleText,
            const juce::String& subText,
            const juce::String& badgeText,
            bool litState,
            bool customState,
            bool learningState
        );
        void paint(juce::Graphics& g) override;
        void mouseDown(const juce::MouseEvent& e) override;
        void mouseUp(const juce::MouseEvent& e) override;

        std::function<void(float)> onPadDown;
        std::function<void()> onPadUp;
        std::function<void()> onRightClick;

    private:
        juce::String title;
        juce::String subtitle;
        juce::String badge;
        bool isLit = false;
        bool isCustom = false;
        bool isLearning = false;
        bool heldLeft = false;
    };

    std::array<PadComponent, CowDrumsProcessor::NUM_INSTRUMENTS> padButtons;
    std::array<int, CowDrumsProcessor::NUM_INSTRUMENTS> padFlashTicks{};

    class MidiDragHandle : public juce::Component {
    public:
        explicit MidiDragHandle(CowDrumsProcessor& proc) : processor(proc) {
            setMouseCursor(juce::MouseCursor::DraggingHandCursor);
        }
        void paint(juce::Graphics& g) override;
        void mouseDrag(const juce::MouseEvent& e) override;
        void mouseUp(const juce::MouseEvent&) override { dragStarted = false; }
    private:
        CowDrumsProcessor& processor;
        bool dragStarted = false;
    };

    juce::TextButton mainAButton{"MAIN A"};
    juce::TextButton varBButton{"VAR B"};
    juce::TextButton fillButton{"FILL"};
    juce::TextButton autoFillButton{"AUTO 4B"};
    MidiDragHandle midiDragHandle;

    juce::Slider swingSlider;
    juce::Label swingLabel;
    juce::Slider gainSlider;
    juce::Label gainLabel;

    std::unique_ptr<juce::FileChooser> fileChooser;

    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR(CowDrumsEditor)
};
