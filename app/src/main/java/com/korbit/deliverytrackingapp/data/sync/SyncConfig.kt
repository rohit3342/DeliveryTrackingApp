package com.korbit.deliverytrackingapp.data.sync

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configurable sync parameters. Batch size applies to outbox event processing.
 */
@Singleton
class SyncConfig @Inject constructor() {
    /** Max pending events to process per sync run. Default 10 when network comes online. */
    var outboxBatchSize: Int = 10
        set(value) { field = value.coerceAtLeast(1).coerceAtMost(100) }
}
