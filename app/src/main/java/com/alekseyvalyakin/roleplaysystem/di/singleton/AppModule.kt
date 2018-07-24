package com.alekseyvalyakin.roleplaysystem.di.singleton

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.alekseyvalyakin.roleplaysystem.app.RpsApp
import com.alekseyvalyakin.roleplaysystem.data.prefs.LocalKeyValueStorage
import com.alekseyvalyakin.roleplaysystem.data.prefs.SharedPreferencesHelper
import com.alekseyvalyakin.roleplaysystem.data.repo.ResourcesProvider
import com.alekseyvalyakin.roleplaysystem.data.repo.ResourcesProviderImpl
import com.alekseyvalyakin.roleplaysystem.data.repo.StringRepository
import com.alekseyvalyakin.roleplaysystem.data.repo.StringRepositoryImpl
import com.alekseyvalyakin.roleplaysystem.di.activity.ThreadConfig
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Singleton

/**
 * Base app module
 */
@Module
class AppModule(private val mApp: RpsApp) {

    @Provides
    @Singleton
    fun provideApplicationContext(): Context {
        return mApp
    }

    @Provides
    @Singleton
    @ThreadConfig(ThreadConfig.TYPE.UI)
    fun provideUiScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    @Provides
    @Singleton
    @ThreadConfig(ThreadConfig.TYPE.IO)
    fun provideIoScheduler(): Scheduler {
        return Schedulers.io()
    }

    @Provides
    @Singleton
    @ThreadConfig(ThreadConfig.TYPE.COMPUTATATION)
    fun provideCompScheduler(): Scheduler {
        return Schedulers.computation()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesHelper(sharedPreferences: SharedPreferences): LocalKeyValueStorage {
        return SharedPreferencesHelper(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideResourceProvider(context: Context): ResourcesProvider {
        return ResourcesProviderImpl(context)
    }

    @Provides
    @Singleton
    fun provideStringRepo(resourcesProvider:ResourcesProvider): StringRepository {
        return StringRepositoryImpl(resourcesProvider)
    }

}