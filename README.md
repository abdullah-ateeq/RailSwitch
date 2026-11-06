# RailSwitch (EdgePulse) ⚡

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-757575?style=flat-square&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-blue.style=flat-square)](LICENSE)

**RailSwitch (EdgePulse)** is a minimalist, fast-paced reflex arcade game built natively for Android using **Jetpack Compose**, **Material 3**, and modern Compose `Canvas` graphics.

Navigate a glowing pulse along a square perimeter rail, anticipate procedural lethal hazards, reverse direction at split-second timing, and complete consecutive lap streaks to set high scores!

---

## 🎮 Gameplay Overview

In **RailSwitch**, your pulse constantly moves along a square perimeter rail. You cannot leave the edge. Danger approaches from all sides, and timing is everything.

* **Instant Direction Reversal:** Tap anywhere on the screen or swipe to instantly flip movement direction and avoid approaching hazards.
* **Procedural Hazard Generation:** Dynamic hazards spawn continuously with fair, difficulty-scaled telegraph windows before becoming lethal.
* **Lap Counter & Multiplier Streaks:** Completing a 360° lap awards **+100 bonus points** plus an escalating **consecutive lap streak multiplier**.
* **Dynamic Speed Progression:** As survival time increases, player speed grows gradually up to difficulty caps, testing your reflexes to the limit.

---

## ✨ Features

- ⚡ **Native Jetpack Compose Canvas**: Smooth 60 FPS 2D rendering powered by Compose graphics and custom vector geometries.
- 🔊 **Procedural PCM Audio Synthesizer**: Low-latency 16-bit PCM sound synthesis using Android `AudioTrack`—zero audio asset files required!
- 📳 **Haptic Feedback**: Tactile vibration feedback on direction switches, lap completions, and collisions.
- 🎯 **Multiple Difficulty Modes**:
  - **Easy**: Slower speed growth, longer telegraph windows (1.5s), max 2 concurrent hazards.
  - **Medium**: Balanced arcade action, 1.15s telegraph window, max 3 concurrent hazards.
  - **Hard**: Fast pace, short reaction window (0.85s), max 4 concurrent hazards.
- 🎨 **Adaptive Themes**: Seamless Dark, Light, and System theme modes with vibrant arcade neon palettes.
- 📊 **High Score & Stat Tracking**: Tracks best score, longest survival time, total laps completed, games played, and per-difficulty high scores.
- 🔒 **100% Offline & Private**: Zero telemetry, zero analytics, no network permissions requested.

---

## 💥 Hazard Types

| Hazard Type | Description |
| :--- | :--- |
| **Perimeter Block** | A moving block patrolling the outer perimeter. Requires switching direction before impact. |
| **Perimeter Surge** | A stationary edge segment that blinks during telegraph and pulses with lethal energy. |
| **Cross-Beam** | Geometric laser beams connecting opposing perimeter points, making contact points lethal zones. |

---

## 🛠️ Tech Stack & Architecture

- **Language:** 100% [Kotlin](https://kotlinlang.org/)
- **UI & Layout:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **State Management:** Android `ViewModel` + Kotlin `StateFlow` (Unidirectional Data Flow)
- **Math Engine:** Normalized 1D perimeter geometry `[0.0, 1.0)` mapped to 2D screen coordinates
- **Audio:** Real-time PCM sine sweep, chord, and noise synthesis via `AudioTrack`
- **Data Storage:** `SharedPreferences` (`GamePreferences`)
- **Testing:** `JUnit 4` unit tests covering perimeter normalization, collision detection, lap mechanics, and hazard fair-spawning

---

## 📁 Project Structure

```text
com.example.edgepulse/
├── audio/            # SoundManager (Procedural AudioTrack synthesizer) & HapticManager
├── data/             # GamePreferences (SharedPreferences persistence & stats flow)
├── engine/           # GameEngine (Core game loop, progression, collision detection)
├── model/            # GameState, Player, Hazard types, PerimeterGeometry, Difficulty
├── ui/
│   ├── components/   # ArcadeButton, GameArena, StatCard, DifficultySelector, etc.
│   ├── navigation/   # EdgePulseNavHost & screen routes
│   ├── screens/      # HomeScreen, GameScreen, ResultScreen, SettingsScreen, AboutScreen, SplashScreen
│   └── theme/        # Color, Type, Theme (Material 3 Dark/Light/System)
└── viewmodel/        # GameViewModel & SettingsViewModel
```

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio** Ladybug (2024.2.1) or newer
* **JDK 11** or higher
* **Android SDK**: Compile SDK 37 / Min SDK 24 (Android 7.0+)

### Building & Running

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/RailSwitch.git
   cd RailSwitch
   ```

2. Open the project in **Android Studio**.

3. Build the debug APK via CLI:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run unit tests:
   ```bash
   ./gradlew test
   ```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
