package com.korbit.deliverytrackingapp.data.sync

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configurable sync parameters. Batch size applies to outbox event processing.
 */
@Singleton
class SyncConfig @Inject constructor() {
    /** Max pending events to process per sync run. Default 50 for faster drain. */
    var outboxBatchSize: Int = 50
        set(value) { field = value.coerceAtLeast(1).coerceAtMost(200) }

    /** Max deliveries to insert per chunk when syncing from GET /deliveries. Avoids one huge transaction. */
    var syncInsertChunkSize: Int = 300
        set(value) { field = value.coerceAtLeast(1).coerceAtMost(2000) }
}
