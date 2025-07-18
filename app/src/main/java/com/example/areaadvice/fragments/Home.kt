package com.example.areaadvice.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areaadvice.R
import com.example.areaadvice.activities.MapsActivity
import com.example.areaadvice.adapters.PlacesAdapter
import com.example.areaadvice.models.Place
import com.example.areaadvice.storage.Prefs
import com.example.areaadvice.utils.getSignature
import com.example.areaadvice.utils.miToM
import com.google.android.gms.location.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.abs

class Home : Fragment(), SensorEventListener {
    // Important variables
    private lateinit var mContext: Context
    private lateinit var apiKey: String
    private var placesList = arrayListOf<Place>()
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var sharedPrefs: Prefs

    // UI elements
    private lateinit var editTextSearch: EditText
    private lateinit var imageButtonSearch: ImageButton
    private lateinit var textViewLoading: TextView
    private lateinit var recyclerViewPlaces: RecyclerView
    private lateinit var mapBtn: ImageButton
    private lateinit var clearBtn: ImageButton
    private lateinit var recBtn: Button

    // Sensor variables
    private lateinit var sensorManager: SensorManager
    private var temp: Sensor? = null
    private var light: Sensor? = null
    private var prevTemp: Float? = null
    private var prevLight: Float? = null
    private var recommendations: String = "restaurant" // must be a type supported by the Places API

    // Settings variables
    private var senEnable = true
    private var openLocEnable = true
    private var unitChoice: Int = 2
    private var critChoice: Int = 2
    private var radius = "25" // placeholder: 25 mi
    private lateinit var unitTemp: String
    private lateinit var unitLight: String

    // Location variables
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    var lat = 91.0
    var lon = 181.0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        this.sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        unitTemp = getString(R.string.temp_celsius)
        unitLight = getString(R.string.light_lux)

        // Get shared preferences
        sharedPrefs = Prefs(mContext)

        senEnable = sharedPrefs.senEnable
        openLocEnable = sharedPrefs.openEnable
        unitChoice = sharedPrefs.units
        critChoice = sharedPrefs.criteria
        radius = sharedPrefs.radiusText

        textViewLoading = view.findViewById(R.id.textViewLoading)
        recyclerViewPlaces = view.findViewById(R.id.recyclerViewPlaces)
        editTextSearch = view.findViewById(R.id.editTextSearch)
        imageButtonSearch = view.findViewById(R.id.imageButtonSearch)
        mapBtn = view.findViewById(R.id.map)
        clearBtn = view.findViewById(R.id.clear)
        recBtn = view.findViewById(R.id.recBtn)
        /* Steps to hide your API key:
         * 1. Create google_apis.xml in values folder (Git will ignore this file)
         * 2. Add API key as string resource named google_places_key
         * 3. Protect yourself from Chrysnosis by deleting his GitHub branch to avoid any running
         * errors (sorry Krishna)
         */
        apiKey = getString(R.string.google_places_key)
        getLocationUpdates() // track location in the background

        // Set up recycler view
        recyclerViewPlaces.layoutManager = LinearLayoutManager(mContext)
        placesAdapter = PlacesAdapter(mContext, placesList)
        recyclerViewPlaces.adapter = placesAdapter
        val divider = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        recyclerViewPlaces.addItemDecoration(divider) // add border between places

        editTextSearch.setOnKeyListener{_, keyCode, keyEvent ->
            // Search if user presses enter on keyboard
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                imageButtonSearch.performClick()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        imageButtonSearch.setOnClickListener {
            // Initiate search if online
            val query = editTextSearch.text.toString()
            val useRatings = critChoice == 2
            val useHours = openLocEnable
            val useMetrics = unitChoice == 1

            if (!isOnline()) {
                Toast.makeText(mContext, "Can't access the internet", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
                /*val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)*/
            } else if (abs(lat) > 90 || abs(lon) > 180) {
                Toast.makeText(mContext, "Can't access your location", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val reqParam = if (useRatings) {
                mapOf(
                    "locationBias" to mapOf(
                        "circle" to mapOf(
                            "center" to mapOf(
                                "latitude" to lat,
                                "longitude" to lon
                            ),
                            // Convert radius to meters
                            "radius" to if (useMetrics) {
                                radius.toFloat() * 1000
                            } else {
                                miToM(radius.toFloat())
                            }
                        )
                    )
                )
            } else {
                mapOf("rankPreference" to "DISTANCE")
            }
            val rankByParam = when {
                query.isNotEmpty() -> {
                    mapOf("textQuery" to query)
                }
                else -> {
                    Toast.makeText(mContext, "A query is required.",
                        Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            val openParam = mapOf("openNow" to useHours)

            placesList.clear()
            placesAdapter.refreshData()
            textViewLoading.visibility = View.VISIBLE
            textViewLoading.text = getString(R.string.loading)
            recommendPlaces(reqParam, rankByParam, openParam)
        }

        mapBtn.setOnClickListener {
            // Go to MapsActivity
            val intent = Intent(mContext, MapsActivity::class.java)
            startActivity(intent)
        }

        clearBtn.setOnClickListener {
            placesList.clear()
            placesAdapter.refreshData()
            textViewLoading.text = ""
            editTextSearch.text.clear()
        }

        recBtn.setOnClickListener{
            // Search without a query
            val useRatings = critChoice == 2
            val useHours = openLocEnable
            val useMetrics = unitChoice == 1

            if (!isOnline()) {
                Toast.makeText(mContext, "Can't access the internet", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
                /*val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)*/
            } else if (abs(lat) > 90 || abs(lon) > 180) {
                Toast.makeText(mContext, "Can't access your location", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val reqParam = if (useRatings) {
                mapOf(
                    "locationBias" to mapOf(
                        "circle" to mapOf(
                            "center" to mapOf(
                                "latitude" to lat,
                                "longitude" to lon
                            ),
                            // Convert radius to meters
                            "radius" to if (useMetrics) {
                                radius.toFloat() * 1000
                            } else {
                                miToM(radius.toFloat())
                            }
                        )
                    )
                )
            } else {
                mapOf("rankPreference" to "DISTANCE")
            }
            val rankByParam = when {
                senEnable -> {
                    // Use a preset type from the sensors
                    mapOf("includedType" to recommendations)
                }
                else -> {
                    Toast.makeText(mContext, "Sensors are disabled, so a query is required.",
                        Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            val openParam = mapOf("openNow" to useHours)

            placesList.clear()
            placesAdapter.refreshData()
            textViewLoading.visibility = View.VISIBLE
            textViewLoading.text = getString(R.string.loading)
            recommendPlaces(reqParam, rankByParam, openParam)
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(5000)
            .setMinUpdateDistanceMeters(10f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    locationResult.lastLocation?.let { location ->
                        lat = location.latitude
                        lon = location.longitude
                        sharedPrefs.lat = lat.toFloat()
                        sharedPrefs.lng = lon.toFloat()
                    }
                }
            }
        }
        startLocationUpdates()
    }
    private fun startLocationUpdates() {
        if (
            ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                0
            )
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() // looper
        )
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        // Enable sensors on resume, if available
        temp?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        light?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop sensors on pause
        sensorManager.unregisterListener(this)
    }

    private fun recommendPlaces(
        reqParam: Map<String, Any>,
        rankByParam: Map<String, Any>,
        openParam: Map<String, Any>
    ) {
        // Use Google Places API to look up locations (must be done on a separate thread)
        thread {
            val placesUrl = URL("https://places.googleapis.com/v1/places:searchText")
            val placesBody = reqParam + rankByParam + openParam + mapOf(
                "pageSize" to 10 // limit to 10 results to speed up results
            )
            val placesBodyStr = JSONObject(placesBody).toString()
            val placesResponseFields = listOf(
                "places.id" // Essentials (IDs Only)
            )

            val placesStr = sendPlacesRequest(
                method = "POST",
                url = placesUrl,
                body = placesBodyStr,
                fields = placesResponseFields
            )
            val placesJSON = JSONObject(placesStr)
            val places = placesJSON.getJSONArray("places")

            if (places.length() > 0) {
                // At least one result is available
                for (i in 0 until places.length()) {
                    val placeID = places.getJSONObject(i).getString("id")

                    val detailsUrl = URL("https://places.googleapis.com/v1/places/$placeID")
                    val detailsResponseFields = listOf(
                        "photos", // Essentials (IDs Only)
                        "displayName", // Pro
                        "formattedAddress", // Essentials
                        "rating", // Enterprise
                        "reviews", // Enterprise + Atmosphere
                        "location", // Essentials
                        "regularOpeningHours", // Enterprise
                        "googleMapsUri" // Pro
                    )

                    val detailsStr = sendPlacesRequest(
                        method = "GET",
                        url = detailsUrl,
                        fields = detailsResponseFields
                    )
                    val detailsJSON = JSONObject(detailsStr)

                    val address = detailsJSON.getString("formattedAddress")
                    val location = detailsJSON.getJSONObject("location")
                    val name = detailsJSON.getJSONObject("displayName")
                        .getString("text")
                    val hours = detailsJSON.optJSONObject("regularOpeningHours")
                    val isOpen = hours?.getBoolean("openNow") ?: false
                    val schedule = hours?.getJSONArray("weekdayDescriptions")
                    val image = detailsJSON.optJSONArray("photos")?.getJSONObject(0)
                        ?.getString("name")
                    val rating = detailsJSON.optDouble("rating", 0.0)
                    val reviews = formReview(detailsJSON.optJSONArray("reviews"))
                    val url = detailsJSON.getString("googleMapsUri")

                    val place = Place(address = address, name = name, isOpen = isOpen,
                        reviews = reviews, rating = rating.toFloat(), url = url, photo = image,
                        latitude = location.getDouble("latitude"),
                        longitude = location.getDouble("longitude"),
                        schedule = schedule.toString())

                    placesList.add(place)
                }

                activity?.runOnUiThread {
                    // Remember that you can only change UI elements in the main thread
                    placesAdapter.refreshData()
                    textViewLoading.visibility = View.INVISIBLE
                }
            } else {
                // Try to output an error message, else show a generic "no results" message
                activity?.runOnUiThread {
                    textViewLoading.text = getString(R.string.no_results)
                }
            }
        }
    }

    private fun sendPlacesRequest(
        method: String, url: URL, body: String? = null, fields: List<String>
    ): String {
        with(url.openConnection() as HttpURLConnection) {
            println("Method: $method")
            println("URL: $url")
            println("Request Body: $body")

            requestMethod = method
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("X-Goog-Api-Key", apiKey)
            setRequestProperty("X-Goog-FieldMask", fields.joinToString(","))
            setRequestProperty("X-Android-Package", context?.packageName)
            setRequestProperty("X-Android-Cert", getSignature(context))
            println("Request Headers: $requestProperties")

            if (body != null) {
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(body)
                    writer.flush()
                }
            }

            println("Response Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val response = reader.readText()
                    println("Response Body: $response")
                    return response
                }
            } else {
                BufferedReader(InputStreamReader(errorStream)).use { reader ->
                    val response = reader.readText()
                    println("Response Body: $response")
                    return response
                }
            }
        }
    }

    private fun formReview(reviews: JSONArray?): String {
        reviews ?: return "No reviews yet" // default if reviews array doesn't exist
        var reviewStr = ""

        for (i in 0 until reviews.length()) {
            val review = reviews.getJSONObject(i)
            val rating = review.getInt("rating")
            val time = review.getString("relativePublishTimeDescription")
            val text = review.getJSONObject("text").getString("text")

            reviewStr += "<b>Rating: $rating</b>&emsp;<i>$time</i><br>$text<br><br>"
            if (i == 2) break // stop at 3 reviews
        }

        return reviewStr
    }

    private fun isOnline(): Boolean {
        val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val n = cm.activeNetwork
        n?.let {
            val nc = cm.getNetworkCapabilities(n)
            // Check for both wifi and cellular network
            return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
        return false
    }

    override fun onAccuracyChanged(event: Sensor?, accuracy: Int) {
        // Should be called once when sensors are enabled
        if (event == temp || event == light) {
            when (accuracy) {
                0 -> {
                    println("Unreliable")
                }
                1 -> {
                    println("Low accuracy")
                }
                2 -> {
                    println("Medium accuracy")
                }
                else -> {
                    println("Very accurate")
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                val temp = event.values[0]

                // Check subsequent temperature when there's at least 2 degrees of change
                if (prevTemp == null || abs(temp - prevTemp!!) >= 2) {
                    prevTemp = temp

                    // Sample recommendations based on temperature
                    recommendations = if (temp <= 0) {
                        "restaurant"
                    } else if (temp > 0 && temp <= 5) {
                        "university"
                    } else if (temp > 5 && temp <= 15) {
                        "library"
                    } else if (temp > 15 && temp <= 20) {
                        "gym"
                    } else {
                        "park"
                    }
                }
            }
            Sensor.TYPE_LIGHT -> {
                val bright = event.values[0]

                // Check subsequent light when there's at least a 2 lx change
                if (prevLight == null || abs(bright - prevLight!!) >= 2) {
                    prevLight = bright

                    /* Sample recommendations based on light level
                     * (total = 15, delta = 2500, y = e^.757x)
                     */
                    recommendations = if (bright <= 1) {
                        "lodging"
                    } else if (bright > 1 && bright <= 2) {
                        "restaurant"
                    } else if (bright > 2 && bright <= 5) {
                        "movie_theater"
                    } else if (bright > 5 && bright <= 10) {
                        "museum"
                    } else if (bright > 10 && bright <= 21) {
                        "library"
                    } else if (bright > 21 && bright <= 44) {
                        "aquarium"
                    } else if (bright > 44 && bright <= 94) {
                        "bowling_alley"
                    } else if (bright > 94 && bright <= 200) {
                        "shopping_mall"
                    } else if (bright > 200 && bright <= 427) {
                        "gym"
                    } else if (bright > 427 && bright <= 910) {
                        "spa"
                    } else if (bright > 910 && bright <= 1939) {
                        "cafe"
                    } else if (bright > 1939 && bright <= 4134) {
                        "tourist_attraction"
                    } else if (bright > 4134 && bright <= 8813) {
                        "park"
                    } else if (bright > 8813 && bright <= 18788) {
                        "zoo"
                    } else {
                        "amusement_park"
                    }
                }
            }
        }
    }
}
