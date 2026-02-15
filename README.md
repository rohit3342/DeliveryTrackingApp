# DeliveryTrackingApp

Production-grade offline-first rider delivery app built with **Kotlin**, **MVI**, and **Clean Architecture**.

## Architecture

- **Offline-first**: Room is the single source of truth; all writes go to Room first.
- **Outbox pattern**: Task action events are written to an `outbox` table with status `PENDING`; the sync engine reads PENDING and pushes to the API.
- **No direct UI → Network**: All API calls happen only in the sync layer (WorkManager + SyncEngine).

## Tech Stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material 3 |
| State | MVI (Intent → ViewModel → State → UI) |
| DI | Hilt |
| Local DB | Room |
| Network | Retrofit + OkHttp (mock interceptor in dev) |
| Background | WorkManager (periodic sync) |
| Async | Coroutines + Flow |

## Folder Structure

See [FOLDER_STRUCTURE.md](FOLDER_STRUCTURE.md) for the full layout.

- **core/logging**: Central `AppLogger` used by sync, repositories, and ViewModels.
- **domain**: Models, repository interfaces, use cases.
- **data**: Room entities/DAOs, Retrofit API/DTOs, repository implementations, **SyncEngine**, **SyncOrchestrator** (batch outbox sync, backoff, partial failure), **SyncWorker**.
- **presentation**: MVI (State, Intent, ViewModel), Compose screens, navigation, theme.
- **di**: Hilt modules (App, Database, Network, Sync).

## Data Flow

1. **Writes**  
   UI → ViewModel → UseCase → Repository → **Room** (and Outbox for task actions).

2. **Sync**  
   WorkManager → SyncWorker → **SyncEngine** → reads **PENDING** outbox → calls API → marks SYNCED/FAILED; then fetches deliveries from API and writes to Room.

3. **Reads**  
   UI ← ViewModel ← UseCase ← Repository ← **Room**.

## Build & Run

1. Open the project in Android Studio (Hedgehog or later recommended).
2. Use **File → Sync Project with Gradle Files** so the Gradle wrapper is created (if needed).
3. Run on an emulator or device (Run ▶️), or from the command line:
   ```bash
   cd /Users/rohit.saini/git_repo/DeliveryTrackingApp
   ./gradlew assembleDebug
   ```
   If `gradlew` reports that the wrapper JAR is missing, run **Sync Project with Gradle Files** in Android Studio once, or install [Gradle](https://gradle.org/install/) and run: `gradle wrapper --gradle-version 8.9`

## Mock API

The app uses `MockApiInterceptor` to simulate the backend: GET `/deliveries` and POST `/tasks/action` return in-memory responses. Replace or remove the interceptor and set a real `BASE_URL` in `NetworkModule` for production.

## Key Conventions

- All writes go to Room first; Outbox is used for actions that must be synced.
- Sync engine is the only component that performs network calls (no UI → API).
- Logging goes through `AppLogger` for consistency and future crash/sampling hooks.
