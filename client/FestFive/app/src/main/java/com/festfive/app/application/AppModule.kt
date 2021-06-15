package com.festfive.app.application

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.festfive.app.data.network.NetworkModule
import com.festfive.app.data.preference.ConfigurationPrefs
import com.festfive.app.data.preference.IConfigurationPrefs
import com.festfive.app.di.ActivityModule
import com.festfive.app.di.ConfigureModule
import com.festfive.app.di.DBModule
import com.festfive.app.di.DialogModule
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import dagger.Module
import dagger.Provides
import dagger.android.support.AndroidSupportInjectionModule
import io.realm.Realm
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/*
*Created by Nhat.vo on 8/27/2019.
*/

@Module(
    includes = [
        AndroidSupportInjectionModule::class,
        NetworkModule::class,
        ActivityModule::class,
        DialogModule::class,
        DBModule::class,
        ConfigureModule::class
    ]
)
class AppModule {

    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun provideConfigPrefs(context: Context): IConfigurationPrefs {
        return ConfigurationPrefs(context)
    }

    @Provides
    internal fun provideRealm(): Realm = Realm.getDefaultInstance()

    @Provides
    @Singleton
    fun provideRestAdapter(client: OkHttpClient, gson: Gson): Retrofit.Builder {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
    }
}