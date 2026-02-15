package com.korbit.deliverytrackingapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryDao
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryTaskDao
import com.korbit.deliverytrackingapp.data.local.dao.TaskActionEventDao
import com.korbit.deliverytrackingapp.data.local.dao.TaskDao
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryEntity
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryTaskEntity
import com.korbit.deliverytrackingapp.data.local.entity.TaskActionEventEntity
import com.korbit.deliverytrackingapp.data.local.entity.TaskEntity

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE deliveries ADD COLUMN customerPhone TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [
        DeliveryEntity::class,
        DeliveryTaskEntity::class,
        TaskEntity::class,
        TaskActionEventEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deliveryDao(): DeliveryDao
    abstract fun deliveryTaskDao(): DeliveryTaskDao
    abstract fun taskDao(): TaskDao
    abstract fun taskActionEventDao(): TaskActionEventDao
}
