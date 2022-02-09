package com.himel.apps.wunderfleet.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.himel.apps.wunderfleet.models.ApiResponse
import com.himel.apps.wunderfleet.models.Car
import com.himel.apps.wunderfleet.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val apiService: ApiService) : ViewModel() {

    private var _apiResponse = MutableLiveData<ApiResponse<List<Car>>>().apply {
        value = ApiResponse(emptyList(), null)
    }
    val apiResponse: LiveData<ApiResponse<List<Car>>> = _apiResponse
    var detailCarId=-1
     private var detialViewingCar = MutableLiveData<ApiResponse<Car>>()
     val detailCar : LiveData<ApiResponse<Car>> = detialViewingCar
    var postResponse : MutableLiveData<String> = MutableLiveData<String>().apply {
        value = "empty"
    }

    fun getCarList(){
        viewModelScope.launch(Dispatchers.IO) {
            val response = apiService.getCarList()
            if (response.isSuccessful){
                val list = response.body()
                if (list!=null && list.isNotEmpty()){
                    _apiResponse.postValue(ApiResponse(list,null))
                }else {
                    sendErrors(response.errorBody().toString())
                }
            }else {
                sendErrors(response.errorBody().toString())
            }
        }
    }

    private fun sendErrors(msg:String){
        _apiResponse.postValue(ApiResponse(null,msg))
    }

    fun getCarDetails(){
        viewModelScope.launch {
            val response = apiService.getCarDetails(detailCarId)
            if (response.isSuccessful){
                val list = response.body()
                if (list!=null){
                    detialViewingCar.postValue(ApiResponse(list,null))
                }else {
                    sendErrors(response.errorBody().toString())
                }
            }else {
                sendErrors(response.errorBody().toString())
            }
        }

    }

    fun postData(){
        viewModelScope.launch(Dispatchers.IO) {
            val map = HashMap<String,Int>().apply { put("carId",detailCarId) }
            val res = apiService.postCarRental(map)
            if (res.body()?.get("reservationId")?.toInt()!! >0){
                postResponse.postValue("Reservation success Id "+res.body()?.get("reservationId"))
            }else {
                postResponse.postValue("Reservation not successfull")
            }

        }
    }
}