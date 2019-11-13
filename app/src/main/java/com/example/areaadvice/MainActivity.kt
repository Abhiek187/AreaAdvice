package com.example.areaadvice

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    // UI elements
    private lateinit var textViewPlacesInfo: TextView

    // Permission codes
    private val internetCode = 0

    /* Steps to hide your API key:
     * 1. Create google_apis.xml in values folder (Git will ignore this file)
     * 2. Add API key as string resource named google_places_key
     * 3. Protect yourself from Chrysnosis (sorry Krishna)
     */
    private lateinit var apiKey: String
    private val input = "Busch"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewPlacesInfo = findViewById(R.id.textViewPlacesInfo)
        apiKey = getString(R.string.google_places_key)

        // Check if all permissions are enabled
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // User denied permission, so we give an explanation
                Toast.makeText(this, "The internet is needed to look up places.",
                    Toast.LENGTH_LONG).show()
            }

            // Try requesting permission to use the internet
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET),
                internetCode)
        } else {
            // Permission has already been granted
            lookupPlaces()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            internetCode -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    lookupPlaces()
                } else {
                    println("The user still refuses to access the internet.")
                }
                return
            }
        }
    }

    private fun lookupPlaces() {
        // Use Google Places API to lookup locations (must be done on a separate thread)
        thread {
            val jsonStr = URL("https://maps.googleapis.com/maps/api/place/" +
                    "findplacefromtext/json?key=$apiKey&input=$input&inputtype=textquery" +
                    "&fields=name,place_id,rating,formatted_address")
                .readText()
            val json = JSONObject(jsonStr)

            runOnUiThread {
                // Remember that you can only change UI elements in the main thread
                textViewPlacesInfo.text = json.toString(2)
            }
        }
    }
}
