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

void CowDrumsEditor::PadComponent::configure(
    const juce::String& titleText,
    const juce::String& subText,
    const juce::String& badgeText,
    bool litState,
    bool customState,
    bool learningState
) {
    title = titleText;
    subtitle = subText;
    badge = badgeText;
    isLit = litState;
    isCustom = customState;
    isLearning = learningState;
}

void CowDrumsEditor::PadComponent::paint(juce::Graphics& g) {
    auto bounds = getLocalBounds().toFloat().reduced(0.5f);
    juce::Colour bg = isLearning
        ? CowPalette::Orange.withAlpha(0.32f)
        : (isLit ? CowPalette::Orange.withAlpha(0.28f) : CowPalette::SurfaceCard);
    juce::Colour border = (isLit || isLearning) ? CowPalette::Orange : CowPalette::BorderSubtle;

    g.setColour(bg);
    g.fillRoundedRectangle(bounds, 5.0f);
    g.setColour(border);
    g.drawRoundedRectangle(bounds, 5.0f, (isLit || isLearning) ? 1.6f : 1.0f);

    // Top-left custom dot + repeat badge
    float x = 7.0f;
    if (isCustom) {
        g.setColour(CowPalette::Orange);
        g.fillEllipse(x, 7.0f, 6.0f, 6.0f);
        x += 10.0f;
    }
    if (badge.isNotEmpty()) {
        g.setColour(CowPalette::Orange);
        g.setFont(juce::FontOptions(10.0f, juce::Font::bold));
        g.drawText(badge, static_cast<int>(x), 4, 48, 13, juce::Justification::centredLeft, false);
    }

    // Subtle top-right "..." menu cue
    g.setColour(CowPalette::TextSecondary.withAlpha(0.65f));
    g.setFont(juce::FontOptions(11.0f, juce::Font::bold));
    g.drawText("...", getWidth() - 24, 2, 18, 14, juce::Justification::centredRight, false);

    // Center label
    g.setColour((isLit || isLearning) ? juce::Colours::white : CowPalette::TextPrimary);
    g.setFont(juce::FontOptions(15.0f, juce::Font::bold));
    int titleY = subtitle.isNotEmpty() ? (getHeight() / 2 - 14) : (getHeight() / 2 - 10);
    g.drawText(title, 6, titleY, getWidth() - 12, 20, juce::Justification::centred, true);

    if (subtitle.isNotEmpty()) {
        g.setColour(CowPalette::Orange);
        g.setFont(juce::FontOptions(10.0f));
        g.drawText(subtitle, 6, titleY + 19, getWidth() - 12, 14, juce::Justification::centred, true);
    }
}

void CowDrumsEditor::PadComponent::mouseDown(const juce::MouseEvent& e) {
    // Top-right "..." corner click or right-click opens context menu
    bool cornerClick = (e.x >= getWidth() - 24 && e.y <= 20);
    if (e.mods.isPopupMenu() || cornerClick) {
        if (onRightClick) onRightClick();
        return;
    }
    heldLeft = true;
    float relY = juce::jlimit(0.0f, 1.0f, static_cast<float>(e.y) / static_cast<float>(std::max(1, getHeight())));
    float vel = 0.50f + relY * 0.50f;
    if (onPadDown) onPadDown(vel);
}

void CowDrumsEditor::PadComponent::mouseUp(const juce::MouseEvent&) {
    if (heldLeft) {
        heldLeft = false;
        if (onPadUp) onPadUp();
    }
}

CowDrumsEditor::CowDrumsEditor(CowDrumsProcessor& p)
    : AudioProcessorEditor(&p), processor(p), midiDragHandle(p)
{
    setResizable(true, true);
    setResizeLimits(720, 460, 1600, 1100);
    setSize(880, 560);

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

    // 12 Monochrome Drum Pads (4x3 grid; Right-Click or '...' opens Pad Context Menu)
    for (int i = 0; i < CowDrumsProcessor::NUM_INSTRUMENTS; ++i) {
        auto& pad = padButtons[static_cast<size_t>(i)];
        addAndMakeVisible(pad);
        pad.onPadDown = [this, i](float vel) {
            processor.setPadHeld(i, true, vel);
            padFlashTicks[static_cast<size_t>(i)] = 4;
            refreshButtonColors();
        };
        pad.onPadUp = [this, i] {
            processor.setPadHeld(i, false, 0.0f);
        };
        pad.onRightClick = [this, i] {
            showPadContextMenu(i);
        };
    }

    // Slot, Fill & MIDI Drag Controls
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

    addAndMakeVisible(midiDragHandle);

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

void CowDrumsEditor::showPadContextMenu(int inst) {
    PadConfigData cfg = processor.getPadConfig(inst);
    bool isLearning = (processor.getMidiLearnPad() == inst);

    juce::PopupMenu menu;
    menu.addSectionHeader(juce::String(CowDrumsProcessor::PAD_LABELS[static_cast<size_t>(inst)]) + " Pad — Sound & Sample");

    // 1a. Change Sound (GM Note)
    juce::PopupMenu gmMenu;
    const auto& catalog = CowDrumsProcessor::getGmSoundCatalog();
    for (const auto& s : catalog) {
        bool tick = cfg.customWavName.isEmpty() && (cfg.soundGmNote == s.note);
        gmMenu.addItem(1000 + s.note, juce::String(s.note) + " - " + s.name, true, tick);
    }
    menu.addSubMenu("Change Sound (GM Note)", gmMenu);

    // 1b. Swap Sound From Another Kit
    juce::PopupMenu kitMenu;
    for (int k = 0; k < CowDrumsProcessor::NUM_KITS; ++k) {
        bool tick = cfg.customWavName.isEmpty() && (cfg.kitOverride == k);
        kitMenu.addItem(2000 + k, CowDrumsProcessor::BUILTIN_KITS[static_cast<size_t>(k)].name, true, tick);
    }
    menu.addSubMenu("Swap Sound From Kit", kitMenu);

    // 1c. Load Custom WAV + Reset
    menu.addItem(10, "Load Custom .WAV Sample...");
    menu.addItem(11, "Reset Pad Sound to Kit Default");

    menu.addSeparator();
    menu.addSectionHeader("Performance & MIDI");

    // 3a. Choke Group
    juce::PopupMenu chokeMenu;
    const char* chokeLabels[4] = {"None", "Group 1 (Hi-Hat)", "Group 2", "Group 3"};
    for (int g = 0; g < 4; ++g) {
        chokeMenu.addItem(3000 + g, chokeLabels[g], true, cfg.chokeGroup == g);
    }
    menu.addSubMenu("Choke Group", chokeMenu);

    // 3b. MIDI Input Learn / Remap
    menu.addItem(20, isLearning
        ? "Cancel MIDI Learn"
        : ("MIDI Learn (Current: Note " + juce::String(cfg.midiInputNote) + ")..."));
    if (cfg.midiInputNote != CowDrumsProcessor::GM_NOTES[static_cast<size_t>(inst)]) {
        menu.addItem(21, "Reset MIDI Note to Default (" + juce::String(CowDrumsProcessor::GM_NOTES[static_cast<size_t>(inst)]) + ")");
    }

    // 3c. Note Repeat / Roll
    juce::PopupMenu repMenu;
    const char* repLabels[5] = {"Off", "1/8 Notes", "1/16 Notes", "1/32 Notes", "1/16 Triplets"};
    for (int r = 0; r < 5; ++r) {
        repMenu.addItem(4000 + r, repLabels[r], true, cfg.noteRepeatMode == r);
    }
    menu.addSubMenu("Note Repeat / Roll (Hold Pad)", repMenu);

    menu.addSeparator();
    menu.addSectionHeader("Pattern Quick Actions (Active Slot)");

    // 4. Pattern Quick Actions
    menu.addItem(30, "Fill Every 4 Steps (1/4)");
    menu.addItem(31, "Fill Every 2 Steps (1/8)");
    menu.addItem(32, "Fill Every 8 Steps (1/2)");
    menu.addItem(33, "Fill All Steps (1/16)");
    menu.addItem(34, "Humanize Pad Velocity");
    menu.addItem(35, "Clear Pad from Pattern");

    menu.showMenuAsync(juce::PopupMenu::Options(), [this, inst, isLearning](int result) {
        if (result == 0) return;
        if (result >= 1000 && result < 1200) {
            processor.setPadSoundGmNote(inst, result - 1000);
        } else if (result >= 2000 && result < 2100) {
            processor.setPadKitOverride(inst, result - 2000);
        } else if (result == 10) {
            fileChooser = std::make_unique<juce::FileChooser>("Select Custom Drum Sample (.wav)", juce::File{}, "*.wav");
            fileChooser->launchAsync(juce::FileBrowserComponent::openMode | juce::FileBrowserComponent::canSelectFiles,
                [this, inst](const juce::FileChooser& fc) {
                    auto file = fc.getResult();
                    if (file.existsAsFile()) {
                        processor.loadCustomWavForPad(inst, file);
                        refreshButtonColors();
                    }
                });
        } else if (result == 11) {
            processor.resetPadSound(inst);
        } else if (result >= 3000 && result < 3010) {
            processor.setPadChokeGroup(inst, result - 3000);
        } else if (result == 20) {
            if (isLearning) processor.cancelMidiLearn();
            else processor.startMidiLearn(inst);
        } else if (result == 21) {
            processor.setPadMidiInputNote(inst, CowDrumsProcessor::GM_NOTES[static_cast<size_t>(inst)]);
        } else if (result >= 4000 && result < 4010) {
            processor.setPadNoteRepeat(inst, result - 4000);
        } else if (result == 30) {
            processor.fillPadEveryNSteps(inst, 4);
        } else if (result == 31) {
            processor.fillPadEveryNSteps(inst, 2);
        } else if (result == 32) {
            processor.fillPadEveryNSteps(inst, 8);
        } else if (result == 33) {
            processor.fillPadEveryNSteps(inst, 1);
        } else if (result == 34) {
            processor.humanizePadInActiveSlot(inst);
        } else if (result == 35) {
            processor.clearPadInActiveSlot(inst);
        }
        refreshButtonColors();
    });
}

void CowDrumsEditor::MidiDragHandle::paint(juce::Graphics& g) {
    auto bounds = getLocalBounds().toFloat().reduced(0.5f);
    g.setColour(CowPalette::Orange.withAlpha(0.18f));
    g.fillRoundedRectangle(bounds, 5.0f);
    g.setColour(CowPalette::Orange);
    g.drawRoundedRectangle(bounds, 5.0f, 1.2f);

    g.setColour(CowPalette::Orange);
    g.setFont(juce::FontOptions(11.0f, juce::Font::bold));
    g.drawText("DRAG A+B+FILL", getLocalBounds(), juce::Justification::centred, false);
}

void CowDrumsEditor::MidiDragHandle::mouseDrag(const juce::MouseEvent& e) {
    if (dragStarted || e.getDistanceFromDragStart() < 4) return;
    dragStarted = true;
    juce::File midiFile = processor.exportActivePatternToTempMidiFile();
    if (midiFile.existsAsFile()) {
        juce::DragAndDropContainer::performExternalDragDropOfFiles(
            juce::StringArray{midiFile.getFullPathName()},
            false,
            this
        );
    }
    dragStarted = false;
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
    hostSyncToggle.setToggleState(processor.isHostSync(), juce::dontSendNotification);
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

    int learnPad = processor.getMidiLearnPad();
    const char* repBadges[5] = {"", "1/8", "1/16", "1/32", "1/16T"};

    for (int i = 0; i < CowDrumsProcessor::NUM_INSTRUMENTS; ++i) {
        bool lit = padFlashTicks[static_cast<size_t>(i)] > 0;
        PadConfigData cfg = processor.getPadConfig(i);
        bool learning = (learnPad == i);
        bool isCustom = cfg.customWavName.isNotEmpty()
            || cfg.kitOverride >= 0
            || (cfg.soundGmNote != CowDrumsProcessor::GM_NOTES[static_cast<size_t>(i)]);

        juce::String sub;
        if (learning) {
            sub = "MIDI LEARN...";
        } else if (cfg.customWavName.isNotEmpty()) {
            sub = cfg.customWavName;
        } else if (cfg.kitOverride >= 0 && cfg.kitOverride < CowDrumsProcessor::NUM_KITS) {
            sub = CowDrumsProcessor::BUILTIN_KITS[static_cast<size_t>(cfg.kitOverride)].name;
        } else if (cfg.soundGmNote != CowDrumsProcessor::GM_NOTES[static_cast<size_t>(i)]) {
            sub = "GM " + juce::String(cfg.soundGmNote);
        }

        juce::String badge = repBadges[juce::jlimit(0, 4, cfg.noteRepeatMode)];
        padButtons[static_cast<size_t>(i)].configure(
            CowDrumsProcessor::PAD_LABELS[static_cast<size_t>(i)],
            sub,
            badge,
            lit,
            isCustom,
            learning
        );
        padButtons[static_cast<size_t>(i)].repaint();
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

    g.setColour(CowPalette::BorderSubtle);
    g.drawHorizontalLine(52, 0.0f, static_cast<float>(getWidth()));

    g.setColour(CowPalette::Orange);
    g.setFont(juce::FontOptions(15.0f, juce::Font::bold));
    g.drawText("COWDRUMS VST3", 14, 14, 144, 24, juce::Justification::centredLeft);

    // Responsive Step LED strip on right side above pads
    int leftColW = juce::jlimit(260, 400, static_cast<int>(getWidth() * 0.36f));
    int stripX = leftColW + 24;
    int stripY = 66;
    int stripW = getWidth() - stripX - 14;
    int stepCount = processor.getCurrentStepCount();
    int curStep = processor.getCurrentStep();
    float cellW = static_cast<float>(stripW) / static_cast<float>(std::max(1, stepCount));
    for (int s = 0; s < stepCount; ++s) {
        bool active = (s == curStep) && processor.isPlayingNow();
        g.setColour(active ? CowPalette::Orange : ((s % 4 == 0) ? CowPalette::SurfaceElevated : CowPalette::SurfaceCard));
        g.fillRoundedRectangle(stripX + s * cellW + 1.0f, static_cast<float>(stripY), cellW - 3.0f, 10.0f, 2.0f);
    }

    // Subtle hint below pads
    g.setColour(CowPalette::TextSecondary.withAlpha(0.7f));
    g.setFont(juce::FontOptions(10.5f));
    g.drawText("Right-click any pad (or '...') to change sound, swap kit, load WAV, MIDI Learn, choke, or fill",
               stripX, getHeight() - 122, stripW, 14, juce::Justification::centredLeft, true);
}

void CowDrumsEditor::resized() {
    const int w = getWidth();
    const int h = getHeight();

    // Responsive top bar
    playButton.setBounds(162, 11, 66, 30);
    hostSyncToggle.setBounds(234, 12, 98, 28);
    bpmLabel.setBounds(336, 12, 62, 28);

    int rightEdge = w - 12;
    loadSf2Button.setBounds(rightEdge - 72, 12, 72, 28);
    rightEdge -= 78;
    nextKitButton.setBounds(rightEdge - 26, 12, 26, 28);
    rightEdge -= 28;
    int kitW = juce::jlimit(120, 210, w / 5);
    kitLabel.setBounds(rightEdge - kitW, 12, kitW, 28);
    rightEdge -= (kitW + 2);
    prevKitButton.setBounds(rightEdge - 26, 12, 26, 28);
    rightEdge -= 34;

    int bpmSliderW = std::max(50, rightEdge - 402);
    bpmSlider.setBounds(400, 14, bpmSliderW, 24);

    // Responsive Left Preset Browser (36% of window width, clamped 260..400px)
    int leftColW = juce::jlimit(260, 400, static_cast<int>(w * 0.36f));
    int halfLeft = (leftColW - 6) / 2;
    genreBox.setBounds(12, 64, halfLeft, 28);
    subcatBox.setBounds(12 + halfLeft + 6, 64, leftColW - halfLeft - 6, 28);
    presetList.setBounds(12, 98, leftColW, h - 110);

    // Responsive Right 4x3 Monochrome Drum Pads
    int padAreaX = leftColW + 24;
    int padAreaY = 86;
    int padAreaW = w - padAreaX - 14;
    int padAreaH = std::max(130, h - padAreaY - 134);
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

    // Slot, Fill & DRAG MIDI row (5 buttons across)
    int ctrlY = h - 104;
    int btnW = (padAreaW - gap * 4) / 5;
    mainAButton.setBounds(padAreaX, ctrlY, btnW, 34);
    varBButton.setBounds(padAreaX + (btnW + gap), ctrlY, btnW, 34);
    fillButton.setBounds(padAreaX + (btnW + gap) * 2, ctrlY, btnW, 34);
    autoFillButton.setBounds(padAreaX + (btnW + gap) * 3, ctrlY, btnW, 34);
    midiDragHandle.setBounds(padAreaX + (btnW + gap) * 4, ctrlY, padAreaW - (btnW + gap) * 4, 34);

    // Swing & Master Volume row (leave bottom-right 18px clear for JUCE corner resize handle)
    int sliderY = h - 54;
    int sliderAreaW = padAreaW - 18;
    int halfW = (sliderAreaW - 12) / 2;
    swingLabel.setBounds(padAreaX, sliderY, 86, 26);
    swingSlider.setBounds(padAreaX + 86, sliderY, std::max(40, halfW - 86), 26);

    int rightX = padAreaX + halfW + 12;
    gainLabel.setBounds(rightX, sliderY, 72, 26);
    gainSlider.setBounds(rightX + 72, sliderY, std::max(40, halfW - 72), 26);
}
