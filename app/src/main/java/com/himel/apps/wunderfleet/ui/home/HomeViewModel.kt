package com.himel.apps.wunderfleet.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}