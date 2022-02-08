package com.himel.apps.wunderfleet.network

import com.himel.apps.wunderfleet.models.Car
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("cars.json")
    suspend fun getCarList(): Response<List<Car>>

    @GET("cars/{cardId}")
    suspend fun getCarDetails(@Path("carId")id:Int): Response<Car>
}