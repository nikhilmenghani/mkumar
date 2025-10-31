// com.mkumar.di.InvoiceModule.kt
package com.mkumar.di

import com.mkumar.domain.invoice.InvoicePdfBuilder
import com.mkumar.domain.invoice.InvoicePdfBuilderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InvoiceModule {
    @Binds @Singleton
    abstract fun bindInvoiceBuilder(impl: InvoicePdfBuilderImpl): InvoicePdfBuilder
}
