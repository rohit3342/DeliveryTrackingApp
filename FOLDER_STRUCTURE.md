# DeliveryTrackingApp – Folder Structure

Offline-first rider delivery app: MVI + Clean Architecture.

## Root

```
DeliveryTrackingApp/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── libs.versions.toml
├── README.md
├── FOLDER_STRUCTURE.md
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── java/com/korbit/deliverytrackingapp/
        │   │   ├── DeliveryTrackingApplication.kt   # @HiltAndroidApp, schedules periodic sync, exposes deps for SyncWorker
        │   │   ├── MainActivity.kt                   # Compose setContent, AppNavigation
        │   │   │
        │   │   ├── core/
        │   │   │   ├── logging/
        │   │   │   │   ├── AppLogger.kt              # Interface + AppLoggerImpl (android.util.Log)
        │   │   │   │   └── LogLevel.kt
        │   │   │   └── monitoring/
        │   │   │       └── Monitor.kt               # Interface + MonitorImpl (Log.i events/metrics/breadcrumbs)
        │   │   │
        │   │   ├── di/
        │   │   │   ├── AppModule.kt                 # AppLogger, Monitor, WorkManager
        │   │   │   ├── DatabaseModule.kt            # Room DB, DAOs, migrations
        │   │   │   ├── NetworkModule.kt             # OkHttp, Retrofit, DeliveryApi, MockApiInterceptor
        │   │   │   └── SyncModule.kt                # Binds DeliveryRepository, OutboxRepository
        │   │   │
        │   │   ├── domain/
        │   │   │   ├── model/
        │   │   │   │   ├── Delivery.kt
        │   │   │   │   ├── DeliveryTask.kt
        │   │   │   │   ├── TaskAction.kt
        │   │   │   │   ├── TaskActionType.kt
        │   │   │   │   └── TaskWithDelivery.kt
        │   │   │   ├── repository/
        │   │   │   │   ├── DeliveryRepository.kt
        │   │   │   │   └── OutboxRepository.kt
        │   │   │   └── usecase/
        │   │   │       ├── CreatePickupTaskUseCase.kt
        │   │   │       ├── EnsureSeedDataUseCase.kt
        │   │   │       ├── GetDeliveryWithTasksUseCase.kt
        │   │   │       ├── ObserveAllTasksUseCase.kt
        │   │   │       ├── ObserveOutboxPendingUseCase.kt
        │   │   │       ├── RunFullSyncUseCase.kt
        │   │   │       ├── TriggerSyncUseCase.kt
        │   │   │       └── UpdateTaskStatusUseCase.kt
        │   │   │
        │   │   ├── data/
        │   │   │   ├── local/
        │   │   │   │   ├── AppDatabase.kt           # Room DB, migrations 2→3…→8
        │   │   │   │   ├── dao/
        │   │   │   │   │   ├── DeliveryDao.kt
        │   │   │   │   │   ├── DeliveryTaskDao.kt
        │   │   │   │   │   ├── TaskActionEventDao.kt
        │   │   │   │   │   └── TaskDao.kt
        │   │   │   │   └── entity/
        │   │   │   │       ├── DeliveryEntity.kt
        │   │   │   │       ├── DeliveryTaskEntity.kt
        │   │   │   │       ├── TaskActionEventEntity.kt
        │   │   │   │       └── TaskEntity.kt
        │   │   │   ├── remote/
        │   │   │   │   ├── api/
        │   │   │   │   │   └── DeliveryApi.kt
        │   │   │   │   ├── dto/
        │   │   │   │   │   ├── DeliveryDto.kt
        │   │   │   │   │   └── TaskActionRequestDto.kt
        │   │   │   │   └── MockApiInterceptor.kt
        │   │   │   ├── repository/
        │   │   │   │   ├── DeliveryRepositoryImpl.kt
        │   │   │   │   └── OutboxRepositoryImpl.kt
        │   │   │   └── sync/
        │   │   │       ├── SyncConfig.kt             # outboxBatchSize (default 10)
        │   │   │       ├── SyncEngine.kt             # sync(fetchDeliveries), outbox + optional GET /deliveries
        │   │   │       ├── SyncOrchestrator.kt      # Batch outbox sync, retries, CREATE_PICKUP vs actions
        │   │   │       ├── SyncWorker.kt            # WorkManager CoroutineWorker, reads full_sync input
        │   │   │       └── OutboxProcessor.kt
        │   │   │
        │   │   └── presentation/
        │   │       ├── navigation/
        │   │       │   └── AppNavigation.kt         # Routes: tasks, create_task, delivery/{deliveryId}
        │   │       ├── theme/
        │   │       │   ├── Color.kt
        │   │       │   ├── Theme.kt
        │   │       │   └── Type.kt
        │   │       ├── util/
        │   │       │   └── TimeFormat.kt
        │   │       ├── tasks/
        │   │       │   ├── TasksScreen.kt
        │   │       │   ├── TasksViewModel.kt
        │   │       │   ├── TasksState.kt
        │   │       │   ├── TasksIntent.kt
        │   │       │   ├── TaskFilter.kt
        │   │       │   └── components/
        │   │       │       └── HomeTaskCard.kt
        │   │       ├── task/
        │   │       │   ├── TaskDetailScreen.kt
        │   │       │   ├── TaskDetailViewModel.kt
        │   │       │   ├── TaskDetailState.kt
        │   │       │   └── TaskDetailIntent.kt
        │   │       └── createtask/
        │   │           ├── CreateNewDeliveryTaskScreen.kt
        │   │           ├── CreateTaskViewModel.kt
        │   │           └── CreateTaskState.kt
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

1. **Writes**: UI → ViewModel → UseCase → Repository → **Room** (+ Outbox for task actions).
2. **Sync**: WorkManager → SyncWorker → SyncEngine → SyncOrchestrator → reads **PENDING** outbox → calls API → marks SYNCED / FAILED; optional GET /deliveries → Room.
3. **Reads**: UI ← ViewModel ← UseCase ← Repository ← **Room** (single source of truth).
4. **No** direct UI → Network; all network access is in the sync layer.

## Key files

| Layer | Role |
|-------|------|
| `core/logging` | Central AppLogger; used by sync, repo, ViewModels. |
| `core/monitoring` | Generic Monitor (events, metrics, breadcrumbs); Log.i for now, extensible. |
| `data/local/entity/TaskActionEventEntity.kt` | Outbox table: PENDING → SYNCED/FAILED. |
| `data/sync/SyncEngine.kt` | Orchestrates outbox sync + optional GET /deliveries; only place that calls API. |
| `data/sync/SyncOrchestrator.kt` | Batch outbox sync, retries, CREATE_PICKUP vs action APIs. |
| `data/sync/SyncWorker.kt` | WorkManager periodic and one-time sync. |
| `di/*` | Hilt modules: App, Database, Network, Sync (repository bindings). |
