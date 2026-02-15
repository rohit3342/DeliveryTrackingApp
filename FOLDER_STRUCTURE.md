# DeliveryTrackingApp – Folder Structure

Production-grade offline-first rider delivery app: MVI + Clean Architecture.

## Root

```
DeliveryTrackingApp/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── libs.versions.toml
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── java/com/korbit/deliverytrackingapp/
        │   │   ├── DeliveryTrackingApplication.kt
        │   │   ├── MainActivity.kt
        │   │   │
        │   │   ├── core/
        │   │   │   └── logging/
        │   │   │       ├── AppLogger.kt
        │   │   │       └── LogLevel.kt
        │   │   │
        │   │   ├── di/
        │   │   │   ├── AppModule.kt
        │   │   │   ├── DatabaseModule.kt
        │   │   │   ├── NetworkModule.kt
        │   │   │   └── SyncModule.kt
        │   │   │
        │   │   ├── domain/
        │   │   │   ├── model/
        │   │   │   │   ├── Delivery.kt
        │   │   │   │   ├── DeliveryTask.kt
        │   │   │   │   └── TaskAction.kt
        │   │   │   ├── repository/
        │   │   │   │   ├── DeliveryRepository.kt
        │   │   │   │   └── OutboxRepository.kt
        │   │   │   └── usecase/
        │   │   │       ├── GetDeliveriesUseCase.kt
        │   │   │       ├── UpdateTaskStatusUseCase.kt
        │   │   │       └── ObserveOutboxPendingUseCase.kt
        │   │   │
        │   │   ├── data/
        │   │   │   ├── local/
        │   │   │   │   ├── dao/
        │   │   │   │   │   ├── DeliveryDao.kt
        │   │   │   │   │   ├── DeliveryTaskDao.kt
        │   │   │   │   │   └── OutboxDao.kt
        │   │   │   │   ├── entity/
        │   │   │   │   │   ├── DeliveryEntity.kt
        │   │   │   │   │   ├── DeliveryTaskEntity.kt
        │   │   │   │   │   └── OutboxEntity.kt
        │   │   │   │   └── AppDatabase.kt
        │   │   │   ├── remote/
        │   │   │   │   ├── api/
        │   │   │   │   │   └── DeliveryApi.kt
        │   │   │   │   └── dto/
        │   │   │   │       ├── DeliveryDto.kt
        │   │   │   │       └── TaskActionRequestDto.kt
        │   │   │   ├── repository/
        │   │   │   │   ├── DeliveryRepositoryImpl.kt
        │   │   │   │   └── OutboxRepositoryImpl.kt
        │   │   │   └── sync/
        │   │   │       ├── SyncEngine.kt
        │   │   │       ├── SyncWorker.kt
        │   │   │       └── OutboxProcessor.kt
        │   │   │
        │   │   └── presentation/
        │   │       ├── delivery/
        │   │       │   ├── DeliveryScreen.kt
        │   │       │   ├── DeliveryViewModel.kt
        │   │       │   ├── DeliveryState.kt
        │   │       │   ├── DeliveryIntent.kt
        │   │       │   └── components/
        │   │       │       └── DeliveryItem.kt
        │   │       ├── task/
        │   │       │   ├── TaskDetailScreen.kt
        │   │       │   ├── TaskDetailViewModel.kt
        │   │       │   ├── TaskDetailState.kt
        │   │       │   └── TaskDetailIntent.kt
        │   │       ├── navigation/
        │   │       │   └── AppNavigation.kt
        │   │       └── theme/
        │   │           ├── Theme.kt
        │   │           ├── Color.kt
        │   │           └── Type.kt
        │   ├── res/
        │   │   ├── values/
        │   │   │   ├── themes.xml
        │   │   │   └── strings.xml
        │   │   └── drawable/
        │   └── AndroidManifest.xml
        │
        └── test/
            └── java/com/korbit/deliverytrackingapp/
                └── (unit tests)
```

## Data flow (offline-first)

1. **Writes**: UI → ViewModel → UseCase → Repository → **Room only** ( + Outbox for actions).
2. **Sync**: WorkManager → SyncEngine → reads **PENDING** outbox → calls API → marks SYNCED / FAILED.
3. **Reads**: UI ← ViewModel ← UseCase ← Repository ← **Room** (single source of truth).
4. **No** direct UI → Network; all network access is in SyncEngine/Worker.

## Key files

| Layer        | Role |
|-------------|------|
| `core/logging` | Central logging; used by sync, repo, VM. |
| `data/local/entity/OutboxEntity.kt` | Outbox table: PENDING → SYNCED/FAILED. |
| `data/sync/SyncEngine.kt` | Reads PENDING outbox, calls API, updates DB. |
| `data/sync/SyncWorker.kt` | WorkManager periodic sync. |
| `di/*` | Hilt modules: DB, Retrofit, WorkManager, repositories. |
