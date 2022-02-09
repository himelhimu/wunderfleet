package com.himel.apps.wunderfleet.di

import com.himel.apps.wunderfleet.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

const val BASE_URL = "https://s3.eu-central-1.amazonaws.com/wunderfleet-recruiting-dev/"

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Singleton
    @Provides
    fun provideOkHttp(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(provideLoggingInterceptor())
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor():Interceptor{
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Provides
    @Singleton
    fun provideApiService() : ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}