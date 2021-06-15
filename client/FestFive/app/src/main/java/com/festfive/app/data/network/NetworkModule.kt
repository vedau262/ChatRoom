package com.festfive.app.data.network

import android.content.Context
import com.festfive.app.BuildConfig
import com.festfive.app.R
import com.festfive.app.data.RetrofitAPIs
import com.festfive.app.data.preference.IConfigurationPrefs
import com.festfive.app.utils.Constants
import com.festfive.app.utils.SharePreferencesUtils
import com.festfive.app.utils.UsKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    internal fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(context: Context, prefs: IConfigurationPrefs?): OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .cache(Cache(context.cacheDir, CACHE_SIZE.toLong()))
            .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor {
                if (!NetworkUtils.isConnected(context))
                    throw NoConnectionException(
                        context.getString(R.string.msg_no_network)
                    )
                val request = it.request()
                val builder = request.newBuilder()
                if (prefs != null) {
                    builder.addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("Content-Language", prefs.language)
                        .addHeader("X-Platform", "android")
                    SharePreferencesUtils.getString(UsKey.accessToken).let { accessToken ->
                        builder.addHeader(
                            "Authorization",
                            "${Constants.KEY_BEARER} $accessToken"
                        )
                    }
                }
                it.proceed(builder.build())

            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()


    @Provides
    @Singleton
    fun provideRestAdapter(context: Context, gson: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): RetrofitAPIs {
        return retrofit.create(RetrofitAPIs::class.java)
    }

    companion object {
        const val CACHE_SIZE = 10 * 1024 * 1024
        const val CONNECT_TIMEOUT = 1 * 60 // seconds
        const val READ_TIMEOUT = 1 * 60 // ms
        const val WRITE_TIMEOUT = 1 * 60 // ms
    }
}