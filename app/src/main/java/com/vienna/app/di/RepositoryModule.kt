package com.vienna.app.di

import com.vienna.app.data.repository.AnalysisRepositoryImpl
import com.vienna.app.data.repository.PortfolioRepositoryImpl
import com.vienna.app.data.repository.StockRepositoryImpl
import com.vienna.app.domain.repository.AnalysisRepository
import com.vienna.app.domain.repository.PortfolioRepository
import com.vienna.app.domain.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStockRepository(impl: StockRepositoryImpl): StockRepository

    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(impl: PortfolioRepositoryImpl): PortfolioRepository

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(impl: AnalysisRepositoryImpl): AnalysisRepository
}
