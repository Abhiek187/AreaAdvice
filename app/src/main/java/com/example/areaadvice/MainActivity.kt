package com.example.areaadvice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import kotlin.collections.Map
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    // UI elements
    private lateinit var textViewPlacesInfo: TextView
    private lateinit var editTextSearch: EditText

    /* Steps to hide your API key:
     * 1. Create google_apis.xml in values folder (Git will ignore this file)
     * 2. Add API key as string resource named google_places_key
     * 3. Protect yourself from Chrysnosis (sorry Krishna)
     */
    private lateinit var apiKey: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    var lat = 0.0
    var lon = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
        }


        textViewPlacesInfo = findViewById(R.id.textViewPlacesInfo)
        editTextSearch = findViewById(R.id.editTextSearch)
        val imageButtonSearch = findViewById<ImageButton>(R.id.imageButtonSearch)
        apiKey = getString(R.string.google_places_key)

        imageButtonSearch.setOnClickListener {
            // Initiate search
            val query = editTextSearch.text.toString()

            if (!isOnline(this)) {
                Toast.makeText(this, "Can't access the internet.", Toast.LENGTH_SHORT)
                    .show()
            } else if (query.isEmpty()) {
                Toast.makeText(this, "Search query is empty.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                textViewPlacesInfo.text = getString(R.string.loading)
                lookupPlaces(query)
            }
        }

        getLocationUpdates()
        Map.setOnClickListener {
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra("lat",lat)
            intent.putExtra("long",lon)
            startActivity(intent)
        }

    }

    private fun getLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this!!)
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.smallestDisplacement = 10f // 170 m = 0.1 mile
        locationRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        println("get Location")
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

    private fun lookupPlaces(input: String) {
        // Use Google Places API to lookup locations (must be done on a separate thread)
        thread {
            // Need to convert user input to query string
            val encodedInput = URLEncoder.encode(input, "UTF-8")
            val jsonStr = URL("https://maps.googleapis.com/maps/api/place/" +
                    "findplacefromtext/json?key=$apiKey&input=$encodedInput&inputtype=textquery" +
                    "&fields=name,place_id,rating,formatted_address").readText()
            val json = JSONObject(jsonStr)

            runOnUiThread {
                // Remember that you can only change UI elements in the main thread
                textViewPlacesInfo.text = json.toString(2)
            }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val n = cm.activeNetwork
        n?.let {
            val nc = cm.getNetworkCapabilities(n)
            // Check for both wifi and cellular network
            return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
        return false
    }
}
