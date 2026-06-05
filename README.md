# Area Problem Reporter — KMP Offline-First App

A **Kotlin Multiplatform (KMP)** app for reporting local area problems (potholes, garbage, broken lights, etc.) with a **full offline-first architecture**.

---

## 📱 Features

- Report area problems with title, description, category, GPS location, and photo
- 6 categories: Pothole, Garbage, Water Leakage, Broken Street Light, Traffic Signal, Drainage
- Full offline-first — works without internet
- Automatic sync when internet returns
- Exponential backoff retry for failed uploads
- Sync status per report: PENDING → SYNCING → SYNCED / FAILED
- Optimistic UI updates (report appears instantly in list)
- Material 3 UI with dark mode support
- WorkManager for background sync

---

## 🏗️ Architecture

```
MVVM + Offline-First + Repository Pattern
```

```
UI Layer (Compose)
     │
     ▼
ViewModel (StateFlow, ScreenModel)
     │
     ▼
Repository (single source of truth)
     │          │
     ▼          ▼
  Local DB    SyncEngine
(SQLDelight)      │
                  ▼
            MockApiService
             (→ Real API)
```

---

## 🔌 Offline-First Flow

When a user submits a report:

```
1. User taps "Submit Report"
   │
2. Report saved to local SQLite DB instantly
   │   (status = PENDING)
   │
3. UI updates immediately from local DB stream
   │   (optimistic update — no waiting)
   │
4. SyncEngine queued in background
   │
5. If internet available → upload to server
   │   • Success → status = SYNCED
   │   • Failure → status = FAILED, retry later
   │
6. No internet → wait, observe connectivity
   │
7. Internet restored → auto-sync all PENDING/FAILED
      • Exponential backoff: 1s, 2s, 4s, 8s, 16s...
      • Max 5 retries before marking FAILED
```

**Key principle: UI always reads from local DB. Never from network.**

---

## 🛠️ Tech Stack

| Layer | Library |
|-------|---------|
| UI | Compose Multiplatform |
| Navigation | Voyager |
| ViewModel | Voyager ScreenModel |
| Database | SQLDelight |
| Network | Ktor |
| DI | Koin |
| Background Sync | WorkManager |
| Serialization | Kotlinx Serialization |
| Date/Time | Kotlinx DateTime |
| Connectivity | Android NetworkCallback |
| Image Loading | Kamel (KMP) / Coil (Android) |

---

## 📁 Project Structure

```
AreaReporter/
├── androidApp/
│   └── src/main/
│       ├── java/com/areareporter/android/
│       │   ├── MainActivity.kt
│       │   └── AreaReporterApplication.kt
│       ├── res/
│       └── AndroidManifest.xml
│
├── shared/
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/com/areareporter/shared/
│       │   │   ├── App.kt                    ← Compose entry point
│       │   │   ├── model/
│       │   │   │   └── Report.kt             ← Domain models
│       │   │   ├── database/
│       │   │   │   ├── DatabaseDriverFactory.kt
│       │   │   │   └── ReportDao.kt
│       │   │   ├── network/
│       │   │   │   └── MockApiService.kt     ← Fake backend
│       │   │   ├── repository/
│       │   │   │   └── ReportRepository.kt   ← Source of truth
│       │   │   ├── sync/
│       │   │   │   └── SyncEngine.kt         ← Retry + backoff
│       │   │   ├── viewmodel/
│       │   │   │   ├── ReportListViewModel.kt
│       │   │   │   ├── CreateReportViewModel.kt
│       │   │   │   └── ReportDetailViewModel.kt
│       │   │   ├── ui/
│       │   │   │   ├── AppTheme.kt
│       │   │   │   ├── components/
│       │   │   │   └── screens/
│       │   │   │       ├── ReportListScreen.kt
│       │   │   │       ├── CreateReportScreen.kt
│       │   │   │       └── ReportDetailScreen.kt
│       │   │   ├── di/
│       │   │   │   └── CommonModule.kt       ← Koin DI
│       │   │   └── utils/
│       │   │       └── ConnectivityObserver.kt
│       │   └── sqldelight/
│       │       └── Reports.sq                ← DB schema
│       └── androidMain/
│           └── kotlin/com/areareporter/shared/
│               ├── DatabaseDriverFactory.kt  ← Android driver
│               ├── di/
│               │   └── AndroidModule.kt
│               ├── sync/
│               │   └── SyncWorker.kt         ← WorkManager
│               └── utils/
│                   └── ConnectivityObserver.kt
│
└── gradle/
    ├── libs.versions.toml
    └── wrapper/
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 26+

### Build & Run

```bash
# Clone the project
git clone <repo-url>
cd AreaReporter

# Open in Android Studio
# Let Gradle sync complete
# Run the 'androidApp' configuration on an emulator or device
```

### Mock Backend

The app uses `MockApiService` which simulates:
- 500–1500ms network latency
- 70% success rate (to demonstrate retry behavior)
- Realistic error messages on failure

To connect a real backend, replace `MockApiService.uploadReport()` with a Ktor HTTP call:

```kotlin
val response: ReportUploadResponse = client.post("https://your-api.com/reports") {
    contentType(ContentType.Application.Json)
    setBody(request)
}.body()
```

---

## 🔄 Sync Status Lifecycle

```
NEW REPORT
    │
    ▼
PENDING ──────────────────────┐
    │                         │
    │ (sync attempt)          │ (no internet)
    ▼                         │
SYNCING                       │
    │                         │
    ├─── success ──► SYNCED   │
    │                         │
    └─── failure ─► FAILED ───┘
                   (retry after backoff)
```

---

## 📋 Database Schema

```sql
CREATE TABLE reports (
    id              TEXT PRIMARY KEY,
    title           TEXT NOT NULL,
    description     TEXT NOT NULL,
    category        TEXT NOT NULL,
    latitude        REAL NOT NULL,
    longitude       REAL NOT NULL,
    imagePath       TEXT,
    status          TEXT NOT NULL DEFAULT 'PENDING',
    createdAt       INTEGER NOT NULL,
    retryCount      INTEGER NOT NULL DEFAULT 0,
    lastSyncAttempt INTEGER
);
```

---

## 🔐 Permissions

| Permission | Purpose |
|------------|---------|
| INTERNET | Upload reports to server |
| ACCESS_NETWORK_STATE | Monitor connectivity |
| ACCESS_FINE_LOCATION | Capture GPS coordinates |
| CAMERA | Take photo of problem |
| READ_MEDIA_IMAGES | Pick from gallery |

---

## 🌟 Key Design Decisions

1. **SQLDelight over Room** — Works in commonMain, generates type-safe Kotlin APIs
2. **Voyager over Navigation Component** — Proper KMP support, simple API
3. **Koin over Hilt** — KMP-compatible DI, no annotation processing needed
4. **StateFlow + ScreenModel** — Lifecycle-aware, KMP-compatible reactive state
5. **Mock backend** — Demonstrates offline-first without requiring a real server
6. **Exponential backoff** — Prevents hammering a struggling server

---

## 📄 License

MIT License — feel free to use as a starting point for your own offline-first KMP app.
