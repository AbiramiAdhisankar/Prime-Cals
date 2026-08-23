# PrimeCals 🧮

A modern, intuitive Android calculator application designed for standard arithmetic and essential discrete mathematical operations. Built with native Java, Android Jetpack, and Room Database for persistent calculation history.

---

## ✨ Features

- **Standard Arithmetic**: Addition, subtraction, multiplication, and division with clean decimal formatting.
- **Advanced Mathematical Utilities**:
  - **GCD & LCM**: Multi-number calculations using comma separation (e.g., `12,18`).
  - **Prime Checker**: Instant primality test for entered integers.
  - **Roots**: One-tap square root (`√`) and cube root (`∛`) calculations.
- **Dual-Line Display**: Real-time expression preview above active input and evaluated results.
- **Calculation History (Room Database)**:
  - Local persistence across sessions using SQLite and Android Jetpack Room.
  - Interactive **BottomSheet dialog** displaying recent calculations.
  - Tap any past calculation to instantly reload its result into the active input.
  - One-tap history clear option.
- **Tactile User Experience**:
  - Soft pastel color-coded button layout for clear visual separation.
  - Haptic feedback on button presses for an authentic keypad feel.
  - Custom launcher and in-app brand identity.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Java
- **UI Framework**: Android SDK, XML Layouts, Material Components (`BottomSheetDialog`, `RecyclerView`)
- **Database / Persistence**: Android Jetpack Room Database (Entity, DAO, RoomDatabase)
- **Concurrency**: Java `ExecutorService` (background threads for database read/write)
- **Formatting**: `DecimalFormat` for dynamic floating-point cleanup
- **Minimum SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 34+

---

## 📂 Project Structure

```text
app/src/main/
├── java/com/example/primecals/
│   ├── MainActivity.java           # Core calculation logic, UI event listeners & haptics
│   ├── CalculationHistory.java     # Room Entity definition
│   ├── HistoryDao.java             # Data Access Object with queries
│   ├── AppDatabase.java            # Room Database holder & thread executor
│   └── HistoryAdapter.java         # RecyclerView Adapter for BottomSheet history
└── res/
    ├── layout/
    │   ├── activity_main.xml       # Main dual-display & keypad interface
    │   ├── layout_history_sheet.xml# History BottomSheet container
    │   └── item_history.xml        # Individual history item row layout
    ├── drawable/                   # Custom button shapes & color themes
    ├── values/                     # Strings, themes, and color palettes
    └── mipmap/                     # Adaptive launcher icons
