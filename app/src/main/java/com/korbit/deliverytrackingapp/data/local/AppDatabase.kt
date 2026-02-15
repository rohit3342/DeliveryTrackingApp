package com.korbit.deliverytrackingapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryDao
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryTaskDao
import com.korbit.deliverytrackingapp.data.local.dao.OutboxDao
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryEntity
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryTaskEntity
import com.korbit.deliverytrackingapp.data.local.entity.OutboxEntity

@Database(
    entities = [
        DeliveryEntity::class,
        DeliveryTaskEntity::class,
        OutboxEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deliveryDao(): DeliveryDao
    abstract fun deliveryTaskDao(): DeliveryTaskDao
    abstract fun outboxDao(): OutboxDao
}
