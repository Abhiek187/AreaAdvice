package com.example.areaadvice.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.areaadvice.R
import com.example.areaadvice.storage.DatabasePlaces
import com.example.areaadvice.storage.Prefs
import com.google.android.gms.maps.*
import kotlin.math.abs

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var sharedPref: Prefs
    private var lat = 91.0
    private var lon = 181.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the new map renderer, if available
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        setContentView(R.layout.activity_maps)

        sharedPref = Prefs(this)
        lat = sharedPref.lat.toDouble()
        lon = sharedPref.lng.toDouble()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> println("The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> println("The legacy version of the renderer is used.")
        }
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
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        if (abs(lat) > 90 || abs(lon) > 180) {
            // Permission is not granted
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

            // Zoom to current location
            val location = LatLng(lat, lon)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
        }

        // Place markers on all saved locations
        val db = DatabasePlaces(this)
        val cursor2 = db.getAllRows()

        with (cursor2) {
            while (moveToNext()) {
                val markLocation = LatLng(
                    getString(getColumnIndexOrThrow(DatabasePlaces.Col_Lat)).toDouble(),
                    getString(getColumnIndexOrThrow(DatabasePlaces.Col_Lng)).toDouble()
                )
                mMap.addMarker(MarkerOptions().position(markLocation)
                    .title(getString(getColumnIndexOrThrow(DatabasePlaces.Col_place_Name))))
            }
        }
    }
}
