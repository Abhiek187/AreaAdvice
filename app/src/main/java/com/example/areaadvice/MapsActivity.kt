package com.example.areaadvice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    var lat = 0.0
    var lon = 0.0
    //val handler = Handler()
    //var repeat =0;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        lat = intent.getDoubleExtra("lat", 0.0)
        lon = intent.getDoubleExtra("long", 0.0)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isCompassEnabled = true
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mMap.uiSettings.isZoomControlsEnabled=true


        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //handler.post(runnableCode)
        val location= LatLng(lat,lon)
        println(" lat $lat")
        println(" long $lon")

       // mMap.addMarker(MarkerOptions().position(location).title("Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }
/*    val runnableCode = object : Runnable {
        override fun run() {
            if (repeat <2){
            getLocationUpdates()
                repeat=repeat+1
            handler.postDelayed(this, 5000)}
        }
    }*/

    private fun getLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.smallestDisplacement = 10f // 170 m = 0.1 mile
        locationRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return


                if (locationResult.locations.isNotEmpty()) {

                    // get latest location
                    val location =
                        locationResult.lastLocation
                    // use your location object
                    // get latitude , longitude and other info from this
                    //Lat.text = "Lat: " + location.latitude
                    // println("Lat: "+location.latitude)
                    //Long.text = "Long: " + location.longitude
                    lat = location.latitude
                    lon = location.longitude


                }
            }
        }
        startLocationUpdates()
    }
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }
}
