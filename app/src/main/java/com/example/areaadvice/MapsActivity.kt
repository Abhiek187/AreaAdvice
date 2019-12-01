package com.example.areaadvice

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    var lat = 0.0
    var lon = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        lat = intent.getDoubleExtra("lat", 0.0)
        lon = intent.getDoubleExtra("long", 0.0)
        getLocationUpdates()

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
        mMap.uiSettings.isZoomControlsEnabled = true
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

            // Add a marker in Sydney and move the camera
            //val sydney = LatLng(-34.0, 151.0)
            //handler.post(runnableCode)
            val location = LatLng(lat, lon)
            println(" lat $lat")
            println(" long $lon")

            // mMap.addMarker(MarkerOptions().position(location).title("Current Location"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))

            val db = Database_Places(this)
            val checkInfo=db.readableDatabase


            val cursor2 = checkInfo.query(
                Database_Places.Table_Name,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,//selection,              // The columns for the WHERE clause
                null,//selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null             // The sort order
            )

            var len = (cursor2.count > 0)
            with(cursor2) {
                while (moveToNext()) {

                    val marklocation = LatLng(getString(getColumnIndexOrThrow(Database_Places.Col_Lat)).toDouble(),
                        getString(getColumnIndexOrThrow(Database_Places.Col_Lng)).toDouble())
                    mMap.addMarker(MarkerOptions().position(marklocation).title(getString(getColumnIndexOrThrow(Database_Places.Col_place_Name))))
                }}
        }

    }

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
                     println("Map Lat: $lat")
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
            null // looper
        )
    }

    override  fun onResume(){
        super.onResume()
        startLocationUpdates()
    }
}
