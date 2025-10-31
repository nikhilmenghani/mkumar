// com.mkumar.di.ServicesModule.kt
package com.mkumar.di

import com.mkumar.domain.pricing.PricingService
import com.mkumar.domain.pricing.PricingServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServicesModule {
    @Binds
    @Singleton
    abstract fun bindPricingService(impl: PricingServiceImpl): PricingService
}
