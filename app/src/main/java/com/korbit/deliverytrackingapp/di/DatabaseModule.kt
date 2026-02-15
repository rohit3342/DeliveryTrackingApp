package com.korbit.deliverytrackingapp.di

import android.content.Context
import androidx.room.Room
import com.korbit.deliverytrackingapp.data.local.AppDatabase
import com.korbit.deliverytrackingapp.data.local.MIGRATION_2_3
import com.korbit.deliverytrackingapp.data.local.MIGRATION_3_4
import com.korbit.deliverytrackingapp.data.local.MIGRATION_4_5
import com.korbit.deliverytrackingapp.data.local.MIGRATION_5_6
import com.korbit.deliverytrackingapp.data.local.MIGRATION_6_7
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryDao
import com.korbit.deliverytrackingapp.data.local.dao.DeliveryTaskDao
import com.korbit.deliverytrackingapp.data.local.dao.TaskActionEventDao
import com.korbit.deliverytrackingapp.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "delivery_tracking_db")
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDeliveryDao(db: AppDatabase): DeliveryDao = db.deliveryDao()

    @Provides
    fun provideDeliveryTaskDao(db: AppDatabase): DeliveryTaskDao = db.deliveryTaskDao()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideTaskActionEventDao(db: AppDatabase): TaskActionEventDao = db.taskActionEventDao()
}
