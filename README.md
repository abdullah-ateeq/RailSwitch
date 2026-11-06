# ⚡ RailSwitch

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**RailSwitch** is a fast-paced, high-octane reflex arcade game built for Android using **100% Jetpack Compose**, custom 2D Canvas rendering, and procedural audio synthesis.

Players switch between two parallel electromagnetic tracks to dodge incoming energy blocks moving downward at escalating speeds. With smooth transition curves, dynamic streak multipliers, screen shake effects, and tactile haptic feedback, RailSwitch delivers a pure, thrilling mobile arcade experience.

---

## ✨ Features

- 🎮 **Dynamic Dual-Rail Gameplay**: Tap anywhere or swipe to instantly transition between left and right tracks.
- 🎨 **Hardware-Accelerated 2D Canvas**: Custom-rendered `RailCanvas` with glowing neon tracks, player trail animations, pulse indicators, and dynamic screen shake.
- 🔊 **Procedural PCM Audio Synthesis**: Ultra-low latency sound effects synthesized mathematically in Kotlin code without external audio assets.
- 📳 **Tactile Haptic Engine**: Custom vibration feedback patterns for rail switching, successful dodges, combo milestones, and game-over impacts.
- ⚙️ **3 Balanced Difficulty Tiers**:
  - **Easy**: Relaxed pace, wider spacing, gentle acceleration.
  - **Medium**: Standard tempo, progressive speed increments, dynamic spawner logic.
  - **Hard**: Razor-thin reaction windows, high velocity, maximum challenge.
- 🔥 **Streak Multipliers & Combo Milestones**: Build consecutive dodges to stack score bonuses and trigger milestone sounds.
- 🛡️ **Guaranteed Solvability**: Procedural `BlockSpawner` enforces mathematical safety gaps ensuring zero unavoidable block placements.
- 🎨 **Multiple Neon Themes**: Choose between **Dark**, **Electric Cyber**, and **OLED Black** visual themes with Material 3 integration.
- 📊 **Persistent Statistics**: Reactive `StateFlow` storage tracks your high scores, longest streaks, total games played, and total blocks avoided.
- 🛡️ **100% Offline & Private**: Zero ads, zero telemetry, zero permissions required beyond `VIBRATE`.

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3 components
- **Graphics**: Compose `Canvas` & `DrawScope` hardware-accelerated 2D graphics
- **Architecture**: MVVM with decoupled `GameEngine` core for pure unit testability
- **State Management**: Kotlin Coroutines & `StateFlow`
- **Audio**: Custom `AudioTrack` PCM signal generator (`SoundManager`)
- **Persistence**: `SharedPreferences` wrapped in reactive `PreferencesManager`
- **Navigation**: Jetpack Compose Navigation (`RailSwitchNavHost`)

```
com.example.railswitch
├── audio/          # Procedural PCM sound & Haptics engines
├── data/           # SharedPreferences & StateFlow persistence
├── logic/          # Core GameEngine physics loop & BlockSpawner algorithms
├── model/          # Data models (GameState, Block, Rail, Difficulty, Settings)
├── ui/
│   ├── components/ # Custom arcade UI buttons, panels, and badges
│   ├── navigation/ # Compose Navigation host & screen routes
│   ├── screens/    # Home, Game, Result, Settings, About screens
│   └── theme/      # Color palettes, typography, and Material 3 theme setups
└── MainActivity.kt # Main Activity entry point with edge-to-edge layout
```

---

## 🎮 How to Play

1. **Start Game**: Select your difficulty from the main menu and hit **PLAY**.
2. **Switch Rails**:
   - **Tap Anywhere**: Instantly toggles between the left and right rails.
   - **Swipe Gesture**: Swipe left or right for directional rail switching.
3. **Build Streaks**: Each successfully avoided block increases your streak count and point multiplier.
4. **Avoid Collisions**: Touching any block results in an instant game over with screen shake and haptic feedback!

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer recommended
- **JDK**: Java 11 / 17
- **Android SDK**: API 37 (Build Tools & SDK Platform)
- **Min Android SDK**: API 24 (Android 7.0 Nougat)

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/RailSwitch.git
   cd RailSwitch
   ```

2. Open the project in Android Studio.

3. Build and run on an emulator or physical device:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run automated unit tests:
   ```bash
   ./gradlew test
   ```

---

## 🧪 Unit Tests

RailSwitch includes a comprehensive test suite in `RailSwitchGameTest.kt` verifying:
- Rail transition physics & input locks during transitions
- Block spawner algorithm & pattern safety gap calculations
- Vertical collision detection accuracy
- Score multiplier math and streak milestone calculations
- Difficulty progression scaling

---

## 📄 License

```
MIT License

Copyright (c) 2025 Abdullah Ateeq

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restrictionlimitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software.
```
