cmake_minimum_required(VERSION 3.22)

project(CowDrumsVST3 VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)

FetchContent_Declare(
    JUCE
    GIT_REPOSITORY https://github.com/juce-framework/JUCE.git
    GIT_TAG 8.0.4
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(JUCE)

juce_add_binary_data(CowDrumsBinaryData
    SOURCES
        "${CMAKE_CURRENT_SOURCE_DIR}/../src/main/resources/soundfonts/GeneralUser_GS_Drums.sf2"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/presets.json"
)
set_target_properties(CowDrumsBinaryData PROPERTIES POSITION_INDEPENDENT_CODE TRUE)

juce_add_plugin(CowDrumsVST3
    COMPANY_NAME "CowDrums"
    BUNDLE_ID "com.cowdrums.vst3"
    IS_SYNTH TRUE
    NEEDS_MIDI_INPUT TRUE
    NEEDS_MIDI_OUTPUT FALSE
    IS_MIDI_EFFECT FALSE
    EDITOR_WANTS_KEYBOARD_FOCUS FALSE
    COPY_PLUGIN_AFTER_BUILD FALSE
    PLUGIN_MANUFACTURER_CODE CowD
    PLUGIN_CODE CDr3
    FORMATS VST3
    PRODUCT_NAME "CowDrums"
)

target_sources(CowDrumsVST3
    PRIVATE
        src/PluginProcessor.cpp
        src/PluginEditor.cpp
)

target_include_directories(CowDrumsVST3
    PRIVATE
        src
)

target_compile_definitions(CowDrumsVST3
    PUBLIC
        JUCE_WEB_BROWSER=0
        JUCE_USE_CURL=0
        JUCE_VST3_CAN_REPLACE_VST2=0
        JUCE_DISPLAY_SPLASH_SCREEN=0
)

target_link_libraries(CowDrumsVST3
    PRIVATE
        CowDrumsBinaryData
        juce::juce_audio_utils
        juce::juce_audio_processors
        juce::juce_gui_extra
    PUBLIC
        juce::juce_recommended_config_flags
        juce::juce_recommended_lto_flags
        juce::juce_recommended_warning_flags
)
