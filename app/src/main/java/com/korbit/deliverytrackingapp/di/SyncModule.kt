package com.korbit.deliverytrackingapp.di

import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import com.korbit.deliverytrackingapp.data.repository.DeliveryRepositoryImpl
import com.korbit.deliverytrackingapp.data.repository.OutboxRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    abstract fun bindDeliveryRepository(impl: DeliveryRepositoryImpl): DeliveryRepository

    @Binds
    abstract fun bindOutboxRepository(impl: OutboxRepositoryImpl): OutboxRepository
}
