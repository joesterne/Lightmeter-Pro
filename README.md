# Lightmeter Pro

**Lightmeter Pro** is a high-performance photography lighting, exposure calculation, and gear inventory Android application designed for professional cinematographers, studio portrait photographers, and field shooters.

---

## 📸 Key Features

1. **Live Incident & Reflected Light Metering**:
   - Hardware ambient light sensor integration measuring illuminance in **Lux**, **Foot-Candles**, and **EV100** ($EV_{100} = \log_2(\text{Lux} / 2.5)$).
   - Real-time **CameraX preview stream** with spot metering reticle and real-time luminance histogram overlay.
   - Aperture Priority (**Av**), Shutter Priority (**Tv**), ISO Sensitivity Priority (**Sv**), and Manual Lock (**M**).
   - Handheld reciprocal rule calculator ($1 / (\text{Focal Length} \times \text{Crop Factor})$) with camera IBIS / lens OIS compensation.

2. **Studio Multi-Light Ratio Calculator (Key • Fill • Hair • Background)**:
   - Dedicated interactive multi-light ratio calculator tailored for studio portraiture.
   - **Meter Reading Inputs**: Direct input for Key, Fill, Hair / Rim, and Background lights with base f-stops ($f/1.4$ to $f/22$) and fractional tenths ($0.0$ to $0.9$ stops).
   - **Dual Ratio Metrics**:
     - *Source Output Ratio*: Light output of Key alone vs Fill alone ($2:1, 3:1, 4:1, 8:1$).
     - *Total Scene Reflectance Ratio*: Highlight side $(Key + Fill)$ vs Shadow side $Fill$ alone ($3:1, 5:1, 9:1$).
   - **Hair / Rim Light Control & Highlight Safety**:
     - Stop delta calculation relative to Key ($\Delta EV = 2 \times \log_2(f_{hair} / f_{key})$).
     - Intelligent warning system preventing highlight blowout on fair/blonde hair ($> +1.8$ EV) or insufficient separation on dark hair ($< -1.5$ EV).
   - **Calculated Working Camera Aperture**: Computes combined exposure $f_{camera} = \sqrt{f_{key}^2 + f_{fill}^2}$ with a 1-tap "Set Camera Aperture" action.
   - **Top-Down Studio Diagram**: Real-time canvas diagram rendering subject head, nose vector, camera axis, and 4-point light angles (+45° Key, -30° Fill, 135° Hair, 180° Backdrop).
   - **Classical Portrait Lighting Presets**: 1-tap switching for 1:1 Beauty/High-Key, 2:1 Soft Commercial, 3:1 Classic Rembrandt/Loop, 4:1 Editorial Character, 8:1 Film Noir/Low-Key, and Rim/Edge Kicker.
   - **Live Reading Import**: Instantly transfer measured ambient/spot readings directly into any light role.

3. **Room Database Gear Inventory Management**:
   - Dedicated local database schema storing user **Camera Bodies** and **Lenses**.
   - Interactive list view with tabs (**All Equipment**, **Camera Bodies**, **Lenses**, **Accessories**).
   - "Mount on Meter" action to instantly calibrate live exposure calculations to active gear.
   - Real-time shoot bag packing checklist and total luggage weight tally.

4. **Multimodal Gemini AI Integration**:
   - **Photo Exposure Analysis (`gemini-4-argon`)**: Zero-permission photo picker analyzing dynamic range, highlights/shadows clipping, and lighting ratios.
   - **Multi-Turn AI Advisor (`gemini-3.8-flash`)**: Multi-turn chat interface with specialized roles (*Master Lighting Technician*, *Studio Strobe Specialist*, *Astrophotography Guide*).

5. **Strobe Guide Number & Power Setup**:
   - Multi-strobe setup for Key, Fill, Rim, and Background lights.
   - Guide number math ($GN = f \times \text{distance}$ at ISO 100), power stepping (1/1 to 1/128), and camera sync speed validation.

6. **Bluetooth Remote Shutter & Intervalometer**:
   - Camera trigger release simulation and BLE pairing.
   - 2s/5s/10s countdown self-timers, Bulb mode long-exposure stopwatch, and timelapse intervalometer.

7. **Offline Camera Guide & Presets**:
   - Built-in field cheat sheet (Sunny 16, Reciprocal rule, Flash sync & HSS, Zone System, ND filters).
   - Save and apply custom shooting environment presets.

---

## ⚡ Performance & Speed Optimizations

The application is engineered for fluid 60/120 FPS performance, low battery consumption during long shoots, and minimal memory overhead:

1. **Zero-Allocation Canvas Rendering**:
   - `HistogramView` and `StudioLightingDiagramCard` utilize persistent, remembered `Path` instances (`remember { Path() }`) with `.reset()` reuse inside the Canvas draw scope.
   - Eliminates hundreds of transient `Path` and `FloatArray` allocations per second during animated state transitions and 120Hz display refresh cycles.

2. **CameraX Frame Capping & Subsampling**:
   - `CameraLuminanceAnalyzer` implements a hardware frame throttle capped at ~30 FPS (33ms minimum frame interval) to prevent CPU heating while maintaining high visual responsiveness.
   - Subsampled YUV planar scanning (step = 3) with fast bitshift luminance binning (`y ushr 2`) delivers sub-millisecond analysis per frame.
   - Hysteresis filter on luminance emissions: updates are published to Compose only when $\Delta EV \ge 0.05$ or spot luminance varies by $\ge 1.0$ unit, preventing unneeded UI recompositions.

3. **Light Sensor Deadband Filtering & 30Hz Rate Limiting**:
   - Hardware `SensorManager.SENSOR_DELAY_UI` events are throttled to a maximum sampling frequency of 30Hz.
   - Exponential Moving Average (EMA) filter with deadband suppression: changes smaller than $\pm 0.04$ EV or $1.5\%$ lux are filtered out, halting continuous recompositions when the phone is static under constant ambient light.

4. **Memory-Safe AI Multimodal Downscaling**:
   - `GeminiService.scaleDownBitmapIfNeeded(maxDimension = 1024)` performs aspect-ratio-preserving downscaling prior to JPEG compression and Base64 serialization.
   - Prevents memory spikes and OutOfMemory (OOM) crashes when analyzing high-resolution 48MP/50MP photos, decreasing upload payload sizes by up to 95% and slashing encoding time from seconds to milliseconds.

5. **Recomposition Guarding & Static Data Structures**:
   - Converted dynamic collections in `QuickAccessBar` and `LightingCalcScreen` to static constants (`EXPOSURE_MODES`, `QUICK_ISOS`, `ND_OPTIONS`, `STANDARD_APERTURES`, `STROBE_POWERS`).
   - Guarded `MainViewModel.recalculateExposure` against identical state mutations, cutting redundant StateFlow notification cascades.

6. **Thread Pool Lifecycle Hygiene**:
   - `CameraViewfinderOverlay` couples CameraX's executor service with Compose's `DisposableEffect(Unit)`, guaranteeing immediate thread shutdown and camera unbinding upon viewfinder dismissal to avoid background resource leaks.

---

## 🗄️ Room Database Schema

The app uses **Room Database** (`lightmeter_pro.db`) with reactive `Flow` queries and repository abstraction:

### 1. `camera_bodies` Table (`CameraBodyEntity`)
| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Unique camera ID |
| `brand` | `TEXT` | Manufacturer (Canon, Sony, Nikon, Leica, etc.) |
| `modelName` | `TEXT` | Camera model name (e.g. EOS R5, A7 IV, Z8) |
| `sensorFormat` | `TEXT` | Full Frame, APS-C, Micro 4/3, Medium Format |
| `cropFactor` | `REAL` | Sensor crop factor (1.0x, 1.5x, 1.6x, 2.0x, 0.79x) |
| `baseIso` | `INTEGER` | Native base ISO sensitivity (64, 100, 160) |
| `maxIso` | `INTEGER` | Maximum ISO sensitivity (up to 102400) |
| `maxShutterSpeed`| `TEXT` | Fastest mechanical/electronic shutter (1/8000s) |
| `hasIbis` | `INTEGER (Boolean)` | In-Body Image Stabilization presence |
| `serialNumber` | `TEXT` | Equipment serial number |
| `weightGrams` | `INTEGER` | Body weight in grams for pack tally |
| `inBag` | `INTEGER (Boolean)` | Packed in shoot bag status |
| `notes` | `TEXT` | User equipment notes |

### 2. `lenses` Table (`LensEntity`)
| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Unique lens ID |
| `brand` | `TEXT` | Lens brand (Canon, Sony, Nikon, Leica, etc.) |
| `lensName` | `TEXT` | Lens commercial designation (e.g. RF 24-70mm f/2.8L) |
| `minFocalLength`| `INTEGER` | Minimum focal length in millimeters (e.g. 24) |
| `maxFocalLength`| `INTEGER` | Maximum focal length in millimeters (e.g. 70) |
| `maxAperture` | `REAL` | Maximum wide-open aperture (f/1.2, f/1.4, f/2.8) |
| `minAperture` | `REAL` | Minimum stopped-down aperture (f/16, f/22) |
| `hasOis` | `INTEGER (Boolean)` | Optical Image Stabilization presence |
| `filterThreadMm`| `INTEGER` | Front filter thread diameter (e.g. 77mm, 82mm) |
| `serialNumber` | `TEXT` | Equipment serial number |
| `weightGrams` | `INTEGER` | Lens weight in grams |
| `inBag` | `INTEGER (Boolean)` | Packed status |
| `notes` | `TEXT` | User notes |

### 3. `gear_inventory` Table (`GearEntity`)
Stores strobes, light meters, radio triggers, tripods, and neutral density filters.

### 4. `shooting_presets` Table (`PresetEntity`)
Stores custom user-defined and built-in environment exposure baselines.

### 5. `exposure_logs` Table (`ExposureLogEntity`)
Stores historical exposure slates with export metadata and cloud sync markers.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Jetpack Compose (100% Kotlin)
- **Design System**: Material Design 3 (M3) with Obsidian Pro Dark, Astro Red Night Vision, and AMOLED Battery Saver
- **Local Persistence**: Android Room + KSP (Kotlin Symbol Processing)
- **Camera Streaming**: CameraX (`camera2`, `camera-lifecycle`, `camera-view`) with zero-copy YUV luminance histogram parsing
- **Networking & AI**: OkHttp + Gemini REST API (`gemini-4-argon`, `gemini-3.8-flash`)
- **State Management**: AndroidViewModel + Kotlin Coroutines & StateFlow

---

## 🚀 Building & Testing

Run compilation via Gradle:
```bash
gradle assembleDebug
```

Run test suite:
```bash
gradle :app:testDebugUnitTest
```
