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
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    // UI elements
    private lateinit var textViewPlacesInfo: TextView
    private lateinit var editTextSearch: EditText
    private lateinit var navbar: BottomNavigationView

    /* Steps to hide your API key:
     * 1. Create google_apis.xml in values folder (Git will ignore this file)
     * 2. Add API key as string resource named google_places_key
     * 3. Protect yourself from Chrysnosis by deleting his GitHub branch to avoid any running errors (sorry Krishna)
     */
    private lateinit var apiKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewPlacesInfo = findViewById(R.id.textViewPlacesInfo)
        editTextSearch = findViewById(R.id.editTextSearch)
        navbar = findViewById(R.id.nav_bar)
        val imageButtonSearch = findViewById<ImageButton>(R.id.imageButtonSearch)
        apiKey = getString(R.string.google_places_key)

        navbar.setOnNavigationItemSelectedListener {item ->
            when(item.itemId){
                R.id.Home ->{
                    println("Home Clicked")
                    changeFragment(Home())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.Settings ->{
                    println("Settings Clicked")
                    changeFragment(SettingsMenu())
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false

        }

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

    private fun changeFragment(fragment: Fragment){
        val fragmentToChange = supportFragmentManager.beginTransaction()
        fragmentToChange.replace(R.id.fragmentContainer, fragment)
        fragmentToChange.commit()
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
