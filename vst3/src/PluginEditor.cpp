#include "PluginEditor.h"

namespace CowPalette {
    const juce::Colour BgDark{0xff212529};
    const juce::Colour SurfaceCard{0xff1b1e22};
    const juce::Colour SurfaceElevated{0xff2b3036};
    const juce::Colour BorderSubtle{0xff343a40};
    const juce::Colour Orange{0xffef6c00};
    const juce::Colour TextPrimary{0xfff1f3f5};
    const juce::Colour TextSecondary{0xffa0a8b0};
}

CowDrumsEditor::CowDrumsEditor(CowDrumsProcessor& p)
    : AudioProcessorEditor(&p), processor(p)
{
    setSize(860, 560);

    // Top Transport Bar
    addAndMakeVisible(playButton);
    playButton.onClick = [this] {
        processor.setInternalPlaying(!processor.isPlayingNow());
        refreshButtonColors();
    };

    addAndMakeVisible(hostSyncToggle);
    hostSyncToggle.setToggleState(processor.isHostSync(), juce::dontSendNotification);
    hostSyncToggle.setColour(juce::ToggleButton::textColourId, CowPalette::TextSecondary);
    hostSyncToggle.setColour(juce::ToggleButton::tickColourId, CowPalette::Orange);
    hostSyncToggle.onClick = [this] {
        processor.setHostSync(hostSyncToggle.getToggleState());
    };

    addAndMakeVisible(bpmSlider);
    bpmSlider.setRange(40.0, 240.0, 1.0);
    bpmSlider.setValue(processor.getEffectiveBpm(), juce::dontSendNotification);
    bpmSlider.setSliderStyle(juce::Slider::LinearHorizontal);
    bpmSlider.setTextBoxStyle(juce::Slider::NoTextBox, false, 0, 0);
    bpmSlider.setColour(juce::Slider::thumbColourId, CowPalette::Orange);
    bpmSlider.setColour(juce::Slider::trackColourId, CowPalette::Orange);
    bpmSlider.setColour(juce::Slider::backgroundColourId, CowPalette::BorderSubtle);
    bpmSlider.onValueChange = [this] {
        processor.setInternalBpm(static_cast<float>(bpmSlider.getValue()));
    };

    addAndMakeVisible(bpmLabel);
    bpmLabel.setJustificationType(juce::Justification::centred);
    bpmLabel.setColour(juce::Label::textColourId, CowPalette::TextPrimary);

    addAndMakeVisible(prevKitButton);
    prevKitButton.onClick = [this] {
        int next = (processor.getActiveKitIndex() - 1 + CowDrumsProcessor::NUM_KITS) % CowDrumsProcessor::NUM_KITS;
        processor.selectKit(next);
        refreshButtonColors();
    };

    addAndMakeVisible(kitLabel);
    kitLabel.setJustificationType(juce::Justification::centred);
    kitLabel.setColour(juce::Label::textColourId, CowPalette::Orange);
    kitLabel.setFont(juce::FontOptions(13.0f, juce::Font::bold));

    addAndMakeVisible(nextKitButton);
    nextKitButton.onClick = [this] {
        int next = (processor.getActiveKitIndex() + 1) % CowDrumsProcessor::NUM_KITS;
        processor.selectKit(next);
        refreshButtonColors();
    };

    addAndMakeVisible(loadSf2Button);
    loadSf2Button.onClick = [this] {
        fileChooser = std::make_unique<juce::FileChooser>("Select SoundFont 2 (.sf2)", juce::File{}, "*.sf2");
        fileChooser->launchAsync(juce::FileBrowserComponent::openMode | juce::FileBrowserComponent::canSelectFiles,
            [this](const juce::FileChooser& fc) {
                auto file = fc.getResult();
                if (file.existsAsFile()) {
                    processor.loadExternalSoundFont(file);
                    refreshButtonColors();
                }
            });
    };

    // Left Preset Browser
    addAndMakeVisible(genreBox);
    genreBox.setColour(juce::ComboBox::backgroundColourId, CowPalette::SurfaceCard);
    genreBox.setColour(juce::ComboBox::textColourId, CowPalette::TextPrimary);
    genreBox.setColour(juce::ComboBox::outlineColourId, CowPalette::BorderSubtle);
    genreBox.onChange = [this] {
        rebuildSubcategoryFilters();
        rebuildFilteredPresets();
    };

    addAndMakeVisible(subcatBox);
    subcatBox.setColour(juce::ComboBox::backgroundColourId, CowPalette::SurfaceCard);
    subcatBox.setColour(juce::ComboBox::textColourId, CowPalette::TextPrimary);
    subcatBox.setColour(juce::ComboBox::outlineColourId, CowPalette::BorderSubtle);
    subcatBox.onChange = [this] {
        rebuildFilteredPresets();
    };

    addAndMakeVisible(presetList);
    presetList.setModel(this);
    presetList.setRowHeight(34);
    presetList.setColour(juce::ListBox::backgroundColourId, CowPalette::SurfaceCard);
    presetList.setColour(juce::ListBox::outlineColourId, CowPalette::BorderSubtle);
    presetList.setOutlineThickness(1);

    // 12 Monochrome Drum Pads (4x3 grid matching SF Drum Machine)
    for (int i = 0; i < CowDrumsProcessor::NUM_INSTRUMENTS; ++i) {
        addAndMakeVisible(padButtons[static_cast<size_t>(i)]);
        padButtons[static_cast<size_t>(i)].setButtonText(CowDrumsProcessor::PAD_LABELS[static_cast<size_t>(i)]);
        padButtons[static_cast<size_t>(i)].onClick = [this, i] {
            processor.triggerPadManual(i, 0.92f);
            padFlashTicks[static_cast<size_t>(i)] = 4;
            refreshButtonColors();
        };
    }

    // Slot & Fill Controls
    addAndMakeVisible(mainAButton);
    mainAButton.onClick = [this] { processor.setSlot(0); refreshButtonColors(); };

    addAndMakeVisible(varBButton);
    varBButton.onClick = [this] { processor.setSlot(1); refreshButtonColors(); };

    addAndMakeVisible(fillButton);
    fillButton.onClick = [this] { processor.requestFill(); refreshButtonColors(); };

    addAndMakeVisible(autoFillButton);
    autoFillButton.onClick = [this] {
        processor.setAutoFill4B(!processor.isAutoFill4B());
        refreshButtonColors();
    };

    // Swing & Master Sliders
    addAndMakeVisible(swingSlider);
    swingSlider.setRange(0.0, 0.70, 0.01);
    swingSlider.setValue(processor.getSwing(), juce::dontSendNotification);
    swingSlider.setSliderStyle(juce::Slider::LinearHorizontal);
    swingSlider.setTextBoxStyle(juce::Slider::NoTextBox, false, 0, 0);
    swingSlider.setColour(juce::Slider::thumbColourId, CowPalette::Orange);
    swingSlider.setColour(juce::Slider::trackColourId, CowPalette::Orange);
    swingSlider.setColour(juce::Slider::backgroundColourId, CowPalette::BorderSubtle);
    swingSlider.onValueChange = [this] {
        processor.setSwing(static_cast<float>(swingSlider.getValue()));
    };

    addAndMakeVisible(swingLabel);
    swingLabel.setColour(juce::Label::textColourId, CowPalette::TextSecondary);
    swingLabel.setFont(juce::FontOptions(11.5f));

    addAndMakeVisible(gainSlider);
    gainSlider.setRange(0.0, 1.4, 0.01);
    gainSlider.setValue(processor.getMasterGain(), juce::dontSendNotification);
    gainSlider.setSliderStyle(juce::Slider::LinearHorizontal);
    gainSlider.setTextBoxStyle(juce::Slider::NoTextBox, false, 0, 0);
    gainSlider.setColour(juce::Slider::thumbColourId, CowPalette::Orange);
    gainSlider.setColour(juce::Slider::trackColourId, CowPalette::Orange);
    gainSlider.setColour(juce::Slider::backgroundColourId, CowPalette::BorderSubtle);
    gainSlider.onValueChange = [this] {
        processor.setMasterGain(static_cast<float>(gainSlider.getValue()));
    };

    addAndMakeVisible(gainLabel);
    gainLabel.setColour(juce::Label::textColourId, CowPalette::TextSecondary);
    gainLabel.setFont(juce::FontOptions(11.5f));

    rebuildGenreFilters();
    rebuildSubcategoryFilters();
    rebuildFilteredPresets();
    refreshButtonColors();

    startTimerHz(30);
}

CowDrumsEditor::~CowDrumsEditor() {
    stopTimer();
}

void CowDrumsEditor::rebuildGenreFilters() {
    genreBox.clear(juce::dontSendNotification);
    genreBox.addItem("All Genres", 1);
    juce::StringArray seen;
    int id = 2;
    for (const auto& p : processor.getPresets()) {
        if (!seen.contains(p.genre)) {
            seen.add(p.genre);
            genreBox.addItem(p.genre, id++);
        }
    }
    genreBox.setSelectedId(1, juce::dontSendNotification);
}

void CowDrumsEditor::rebuildSubcategoryFilters() {
    subcatBox.clear(juce::dontSendNotification);
    subcatBox.addItem("All Subcategories", 1);
    juce::String selectedGenre = genreBox.getSelectedId() <= 1 ? "" : genreBox.getText();
    juce::StringArray seen;
    int id = 2;
    for (const auto& p : processor.getPresets()) {
        if (selectedGenre.isNotEmpty() && p.genre != selectedGenre) continue;
        if (!seen.contains(p.subcategory)) {
            seen.add(p.subcategory);
            subcatBox.addItem(p.subcategory, id++);
        }
    }
    subcatBox.setSelectedId(1, juce::dontSendNotification);
}

void CowDrumsEditor::rebuildFilteredPresets() {
    filteredPresetIndices.clear();
    juce::String selectedGenre = genreBox.getSelectedId() <= 1 ? "" : genreBox.getText();
    juce::String selectedSub = subcatBox.getSelectedId() <= 1 ? "" : subcatBox.getText();
    const auto& all = processor.getPresets();
    for (int i = 0; i < static_cast<int>(all.size()); ++i) {
        if (selectedGenre.isNotEmpty() && all[static_cast<size_t>(i)].genre != selectedGenre) continue;
        if (selectedSub.isNotEmpty() && all[static_cast<size_t>(i)].subcategory != selectedSub) continue;
        filteredPresetIndices.push_back(i);
    }
    presetList.updateContent();
    presetList.repaint();
}

int CowDrumsEditor::getNumRows() {
    return static_cast<int>(filteredPresetIndices.size());
}

void CowDrumsEditor::paintListBoxItem(int rowNumber, juce::Graphics& g, int width, int height, bool /*rowIsSelected*/) {
    if (rowNumber < 0 || rowNumber >= static_cast<int>(filteredPresetIndices.size())) return;
    int presetIdx = filteredPresetIndices[static_cast<size_t>(rowNumber)];
    const auto& p = processor.getPresets()[static_cast<size_t>(presetIdx)];
    bool isActive = (presetIdx == processor.getActivePresetIndex());

    g.fillAll(isActive ? CowPalette::SurfaceElevated : CowPalette::SurfaceCard);
    g.setColour(CowPalette::BorderSubtle);
    g.drawHorizontalLine(height - 1, 0.0f, static_cast<float>(width));

    // Signature orange indicator circle on left
    g.setColour(isActive ? CowPalette::Orange : CowPalette::BorderSubtle);
    g.fillEllipse(8.0f, (height - 12.0f) * 0.5f, 12.0f, 12.0f);

    g.setColour(isActive ? CowPalette::Orange : CowPalette::TextPrimary);
    g.setFont(juce::FontOptions(13.0f, isActive ? juce::Font::bold : juce::Font::plain));
    g.drawText(p.name, 26, 2, width - 90, 17, juce::Justification::centredLeft, true);

    g.setColour(CowPalette::TextSecondary);
    g.setFont(juce::FontOptions(10.5f));
    g.drawText(p.subcategory, 26, 18, width - 90, 14, juce::Justification::centredLeft, true);

    g.drawText(juce::String(p.bpm) + " BPM", width - 64, 0, 58, height, juce::Justification::centredRight, false);
}

void CowDrumsEditor::listBoxItemClicked(int row, const juce::MouseEvent&) {
    if (row < 0 || row >= static_cast<int>(filteredPresetIndices.size())) return;
    int presetIdx = filteredPresetIndices[static_cast<size_t>(row)];
    processor.selectPreset(presetIdx, true);
    bpmSlider.setValue(processor.getEffectiveBpm(), juce::dontSendNotification);
    swingSlider.setValue(processor.getSwing(), juce::dontSendNotification);
    presetList.repaint();
    refreshButtonColors();
}

void CowDrumsEditor::refreshButtonColors() {
    auto styleBtn = [](juce::TextButton& btn, bool active) {
        btn.setColour(juce::TextButton::buttonColourId, active ? CowPalette::Orange : CowPalette::SurfaceCard);
        btn.setColour(juce::TextButton::textColourOffId, active ? juce::Colours::white : CowPalette::TextPrimary);
    };

    bool playing = processor.isPlayingNow();
    playButton.setButtonText(playing ? "STOP" : "PLAY");
    styleBtn(playButton, playing);
    styleBtn(prevKitButton, false);
    styleBtn(nextKitButton, false);
    styleBtn(loadSf2Button, false);

    int slot = processor.getActiveSlot();
    styleBtn(mainAButton, slot == 0);
    styleBtn(varBButton, slot == 1);
    styleBtn(fillButton, slot == 2);
    styleBtn(autoFillButton, processor.isAutoFill4B());

    kitLabel.setText(CowDrumsProcessor::BUILTIN_KITS[static_cast<size_t>(processor.getActiveKitIndex())].name,
                     juce::dontSendNotification);
    bpmLabel.setText(juce::String(static_cast<int>(processor.getEffectiveBpm())) + " BPM", juce::dontSendNotification);
    swingLabel.setText("SWING " + juce::String(static_cast<int>(processor.getSwing() * 100.0f)) + "%",
                       juce::dontSendNotification);
    gainLabel.setText("VOL " + juce::String(static_cast<int>(processor.getMasterGain() * 100.0f)) + "%",
                      juce::dontSendNotification);

    for (int i = 0; i < CowDrumsProcessor::NUM_INSTRUMENTS; ++i) {
        bool lit = padFlashTicks[static_cast<size_t>(i)] > 0;
        padButtons[static_cast<size_t>(i)].setColour(
            juce::TextButton::buttonColourId,
            lit ? CowPalette::Orange : CowPalette::SurfaceCard
        );
        padButtons[static_cast<size_t>(i)].setColour(
            juce::TextButton::textColourOffId,
            lit ? juce::Colours::white : CowPalette::TextPrimary
        );
    }
}

void CowDrumsEditor::timerCallback() {
    uint32_t mask = processor.consumePadFlashMask();
    for (int i = 0; i < CowDrumsProcessor::NUM_INSTRUMENTS; ++i) {
        if ((mask & (1u << static_cast<uint32_t>(i))) != 0u) {
            padFlashTicks[static_cast<size_t>(i)] = 3;
        } else if (padFlashTicks[static_cast<size_t>(i)] > 0) {
            padFlashTicks[static_cast<size_t>(i)]--;
        }
    }
    refreshButtonColors();
    repaint();
}

void CowDrumsEditor::paint(juce::Graphics& g) {
    g.fillAll(CowPalette::BgDark);

    // Header bar separator
    g.setColour(CowPalette::BorderSubtle);
    g.drawHorizontalLine(52, 0.0f, static_cast<float>(getWidth()));

    // Brand title
    g.setColour(CowPalette::Orange);
    g.setFont(juce::FontOptions(15.0f, juce::Font::bold));
    g.drawText("COWDRUMS VST3", 14, 14, 150, 24, juce::Justification::centredLeft);

    // Step LED strip on right side above pads
    int stepCount = processor.getCurrentStepCount();
    int curStep = processor.getCurrentStep();
    int stripX = 330;
    int stripY = 66;
    int stripW = getWidth() - stripX - 14;
    float cellW = static_cast<float>(stripW) / static_cast<float>(std::max(1, stepCount));
    for (int s = 0; s < stepCount; ++s) {
        bool active = (s == curStep) && processor.isPlayingNow();
        g.setColour(active ? CowPalette::Orange : ((s % 4 == 0) ? CowPalette::SurfaceElevated : CowPalette::SurfaceCard));
        g.fillRoundedRectangle(stripX + s * cellW + 1.0f, static_cast<float>(stripY), cellW - 3.0f, 10.0f, 2.0f);
    }
}

void CowDrumsEditor::resized() {
    // Top bar
    playButton.setBounds(172, 11, 70, 30);
    hostSyncToggle.setBounds(250, 12, 104, 28);
    bpmLabel.setBounds(358, 12, 68, 28);
    bpmSlider.setBounds(426, 14, 100, 24);
    prevKitButton.setBounds(536, 12, 28, 28);
    kitLabel.setBounds(566, 12, 174, 28);
    nextKitButton.setBounds(742, 12, 28, 28);
    loadSf2Button.setBounds(778, 12, 70, 28);

    // Left Preset Browser (Genre -> Subcategory -> 95-preset List)
    genreBox.setBounds(12, 64, 148, 28);
    subcatBox.setBounds(166, 64, 150, 28);
    presetList.setBounds(12, 98, 304, getHeight() - 110);

    // Right 4x3 Monochrome Drum Pads
    int padAreaX = 330;
    int padAreaY = 86;
    int padAreaW = getWidth() - padAreaX - 14;
    int padAreaH = getHeight() - padAreaY - 118;
    int cols = 4;
    int rows = 3;
    int gap = 6;
    int padW = (padAreaW - gap * (cols - 1)) / cols;
    int padH = (padAreaH - gap * (rows - 1)) / rows;

    const std::array<int, 12> padOrder = {0, 1, 4, 5, 8, 7, 6, 10, 2, 3, 9, 11};
    for (int idx = 0; idx < 12; ++idx) {
        int r = idx / cols;
        int c = idx % cols;
        int inst = padOrder[static_cast<size_t>(idx)];
        padButtons[static_cast<size_t>(inst)].setBounds(
            padAreaX + c * (padW + gap),
            padAreaY + r * (padH + gap),
            padW,
            padH
        );
    }

    // Slot & Fill Controls row
    int ctrlY = getHeight() - 104;
    int btnW = (padAreaW - gap * 3) / 4;
    mainAButton.setBounds(padAreaX, ctrlY, btnW, 34);
    varBButton.setBounds(padAreaX + (btnW + gap), ctrlY, btnW, 34);
    fillButton.setBounds(padAreaX + (btnW + gap) * 2, ctrlY, btnW, 34);
    autoFillButton.setBounds(padAreaX + (btnW + gap) * 3, ctrlY, btnW, 34);

    // Swing & Master Volume row
    int sliderY = getHeight() - 54;
    int halfW = (padAreaW - 12) / 2;
    swingLabel.setBounds(padAreaX, sliderY, 92, 26);
    swingSlider.setBounds(padAreaX + 92, sliderY, halfW - 92, 26);

    int rightX = padAreaX + halfW + 12;
    gainLabel.setBounds(rightX, sliderY, 76, 26);
    gainSlider.setBounds(rightX + 76, sliderY, halfW - 76, 26);
}
