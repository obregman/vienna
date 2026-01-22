package com.vienna.app.di

import android.content.Context
import androidx.room.Room
import com.vienna.app.data.local.database.ViennaDatabase
import com.vienna.app.data.local.database.dao.AnalysisCacheDao
import com.vienna.app.data.local.database.dao.CachedStockDao
import com.vienna.app.data.local.database.dao.PortfolioDao
import com.vienna.app.data.local.database.dao.SearchHistoryDao
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
    fun provideDatabase(@ApplicationContext context: Context): ViennaDatabase {
        return Room.databaseBuilder(
            context,
            ViennaDatabase::class.java,
            ViennaDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providePortfolioDao(database: ViennaDatabase): PortfolioDao {
        return database.portfolioDao()
    }

    @Provides
    @Singleton
    fun provideCachedStockDao(database: ViennaDatabase): CachedStockDao {
        return database.cachedStockDao()
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: ViennaDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    @Singleton
    fun provideAnalysisCacheDao(database: ViennaDatabase): AnalysisCacheDao {
        return database.analysisCacheDao()
    }
}
