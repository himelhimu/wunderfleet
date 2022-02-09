package com.himel.apps.wunderfleet.network

import com.himel.apps.wunderfleet.models.Car
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("cars.json")
    suspend fun getCarList(): Response<List<Car>>

    @GET("cars/{carId}")
    suspend fun getCarDetails(@Path("carId")id:Int): Response<Car>

    @Headers("Authorization: Bearer df7c313b47b7ef87c64c0f5f5cebd6086bbb0fa")
    @POST("https://4i96gtjfia.execute-api.eu-central-1.amazonaws.com/default/wunderfleet-recruiting-mobile-dev-quick-rental")
    suspend fun postCarRental(@Body data:Map<String,Int>): Response<Map<String,String>>
}