package com.example.areaadvice

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    private fun lookupPlaces(input: String) {
        // Use Google Places API to lookup locations (must be done on a separate thread)
        thread {
            // Need to convert user input to query string
            val encodedInput = URLEncoder.encode(input, "UTF-8")
            val placesStr = URL("https://maps.googleapis.com/maps/api/place/" +
                    "findplacefromtext/json?key=$apiKey&input=$encodedInput&inputtype=textquery" +
                    "&fields=place_id").readText()
            val placesJSON = JSONObject(placesStr)

            if (placesJSON.getString("status") == "OK") {
                // At least one result is available
                val placeID = placesJSON.getJSONArray("candidates").getJSONObject(0)
                    .getString("place_id")

                val detailsStr = URL("https://maps.googleapis.com/maps/api/place/details/" +
                        "json?key=$apiKey&place_id=$placeID&fields=name,formatted_address," +
                        "opening_hours,rating,review").readText()
                val detailsJSON = JSONObject(detailsStr)

                runOnUiThread {
                    // Remember that you can only change UI elements in the main thread
                    textViewPlacesInfo.text = detailsJSON.toString(2)
                }
            } else {
                // Try to output an error message, else show a generic "no results" message
                runOnUiThread {
                    textViewPlacesInfo.text = placesJSON.optString("error_message",
                        getString(R.string.no_results))
                }
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
