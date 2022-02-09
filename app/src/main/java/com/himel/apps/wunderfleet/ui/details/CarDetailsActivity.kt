package com.himel.apps.wunderfleet.ui.details

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.himel.apps.wunderfleet.databinding.ActivityCarDetailsBinding
import com.himel.apps.wunderfleet.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CarDetailsActivity : AppCompatActivity() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding : ActivityCarDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val carId = intent.getIntExtra("car_id",-1)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeViewModel.detailCarId = carId


        homeViewModel.detailCar.observe(this) {
            if (it?.data != null) {
                    binding.car = it.data
                    binding.executePendingBindings()
            }else {
                Toast.makeText(
                    this,
                    it?.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        homeViewModel.getCarDetails()
        homeViewModel.postResponse.observe(this){
            Toast.makeText(
                this,
                it,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun quickRent(view: View) {
        homeViewModel.postData()
    }
}