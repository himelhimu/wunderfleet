package com.himel.apps.wunderfleet

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import com.himel.apps.wunderfleet.databinding.ActivityMapsBinding
import com.himel.apps.wunderfleet.models.Car
import com.himel.apps.wunderfleet.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "MapsActivity"
private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 33
private const val DEFAULT_ZOOM = 15

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var markerTapCounter=0

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var homeViewModel: HomeViewModel

    private var locationPermissionGranted = false
    private var markerList: MutableList<Marker> = mutableListOf()
    private var carMarkerMap: MutableMap<Marker, Car> = mutableMapOf()

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        homeViewModel.getCarList()
        observeForCarList()

    }

    fun observeForCarList() {
        homeViewModel.apiResponse.observe(this) {
            if (it != null && it.data!!.isNotEmpty()) {
                processData(it.data)
            }
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            updateLocationUI()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    private fun processData(data: List<Car>) {
        if (mMap == null) {
            return
        }
        data.forEach { car ->
            val latLng = LatLng(car.lat!!, car.lon!!)
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng).title(car.title)
                    .snippet(car.title)
            ).also {
                if (it != null) {
                    markerList.add(it)
                    carMarkerMap[it] = car
                }
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        }

        mMap.setOnMarkerClickListener(object : OnMarkerClickListener {
            override fun onMarkerClick(clickedMarker: Marker): Boolean {
                markerTapCounter++


                val car = carMarkerMap[clickedMarker]
                if (markerTapCounter==2){
                    Toast.makeText(this@MapsActivity,"Will go to ${car?.title}",Toast.LENGTH_SHORT).show()
                }
                val iconGenerator = IconGenerator(this@MapsActivity)
                mMap.clear()

                iconGenerator.setStyle(IconGenerator.STYLE_GREEN)
                iconGenerator.makeIcon(car?.title)
                val bitmap = iconGenerator.makeIcon()
                val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
                val latLng = LatLng(car?.lat!!, car.lon!!)
                mMap.addMarker(MarkerOptions().position(latLng).icon(icon))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                return true
            }

        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationPermission()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
                getDeviceLocation()
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}