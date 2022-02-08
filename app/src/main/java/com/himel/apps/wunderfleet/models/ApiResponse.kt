package com.himel.apps.wunderfleet.models

data class ApiResponse<out T>(val data:T?,val message:String?)