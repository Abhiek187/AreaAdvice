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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areaadvice.R
import com.example.areaadvice.activities.MapsActivity
import com.example.areaadvice.adapters.PlacesAdapter
import com.example.areaadvice.models.Place
import com.example.areaadvice.storage.Prefs
import com.example.areaadvice.utils.cToF
import com.example.areaadvice.utils.lxToFc
import com.example.areaadvice.utils.miToM
import com.google.android.gms.location.*
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
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
    private lateinit var mapBtn: Button
    private lateinit var clearBtn: Button
    private var result: JSONObject? = null

    // Sensor variables
    private lateinit var sensorManager: SensorManager
    private var currentTemp: Sensor? = null
    private var light: Sensor? = null
    private var prevTemp: Float? = null
    private var prevLight:Float? = null
    private var recommendations: String = "library" // must be a type supported by the Places API
    private var recPrev: String = ""

    // Settings variables
    private var senEnable = true
    private var openLocEnable = true
    private var unitChoice: Int = 1
    private var critChoice: Int = 1
    private var radius = "25" // placeholder: 25 mi
    private lateinit var unitTemp: String
    private lateinit var unitLight: String

    // Location variables
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    var lat = 0.0
    var lon = 0.0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        this.sensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        currentTemp=sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        light=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        unitTemp = getString(R.string.temp_celsius)
        unitLight = getString(R.string.light_lux)

        // Get shared preferences
        sharedPrefs = Prefs(mContext)

        senEnable = sharedPrefs.senEnable
        openLocEnable = sharedPrefs.openEnable
        unitChoice = sharedPrefs.units
        critChoice = sharedPrefs.criteria
        radius = sharedPrefs.radiusText

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }

        textViewLoading = view.findViewById(R.id.textViewLoading)
        recyclerViewPlaces = view.findViewById(R.id.recyclerViewPlaces)
        editTextSearch = view.findViewById(R.id.editTextSearch)
        imageButtonSearch = view.findViewById(R.id.imageButtonSearch)
        mapBtn = view.findViewById(R.id.map)
        clearBtn = view.findViewById(R.id.clear)
        /* Steps to hide your API key:
         * 1. Create google_apis.xml in values folder (Git will ignore this file)
         * 2. Add API key as string resource named google_places_key
         * 3. Protect yourself from Chrysnosis by deleting his GitHub branch to avoid any running errors (sorry Krishna)
         */
        apiKey = getString(R.string.google_places_key)
        getLocationUpdates() // track location in the background

        // Set up recycler view
        recyclerViewPlaces.layoutManager = LinearLayoutManager(mContext)
        placesAdapter = PlacesAdapter(mContext, placesList)
        recyclerViewPlaces.adapter = placesAdapter
        val divider = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        recyclerViewPlaces.addItemDecoration(divider) // add border between places

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
            } else if (lat == 0.0 && lon == 0.0) {
                Toast.makeText(mContext, "Can't access your location", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val reqParam = if (useRatings) {
                // Convert radius to meters
                if (useMetrics) {
                    "radius=${radius.toFloat() * 1000}"
                } else {
                    "radius=${miToM(radius.toFloat())}"
                }
            } else {
                "rankby=distance"
            }
            val rankByParam = when {
                query.isNotEmpty() -> {
                    // Need to convert user input to encoded query string
                    val encodedQuery = URLEncoder.encode(query, "UTF-8")
                    "&keyword=$encodedQuery"
                }
                senEnable -> {
                    // Use a preset type from the sensors
                    "&type=$recommendations"
                }
                else -> {
                    Toast.makeText(mContext, "Sensors are disabled, so a query is required.",
                        Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            val openParam = if (useHours) "&opennow" else ""

            placesList.clear()
            placesAdapter.refreshData()
            textViewLoading.visibility = View.VISIBLE
            textViewLoading.text = getString(R.string.loading)
            recommendPlaces(reqParam, rankByParam, openParam)
        }

        mapBtn.setOnClickListener {
            // Go to MapsActivity
            val intent = Intent(mContext, MapsActivity::class.java)
            intent.putExtra("lat",lat)
            intent.putExtra("long",lon)
            startActivity(intent)
        }

        clearBtn.setOnClickListener {
            placesList.clear()
            placesAdapter.refreshData()
            textViewLoading.text = ""
        }


        // Inflate the layout for this fragment
        return view
    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.smallestDisplacement = 10f
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        //println("get Location")

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
                    println("Main Lat: $lat")
                    println("Main Lon: $lon")
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

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        // val intent = Intent()
        // senEnable = intent.getBooleanExtra("recSwitch",true)
        // Enable sensors on resume
        sensorManager.registerListener(this,currentTemp,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,light,SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        // Stop sensors on pause
        sensorManager.unregisterListener(this)
    }

    private fun recommendPlaces(reqParam: String, rankByParam: String, openParam: String) {
        // Use Google Places API to look up locations (must be done on a separate thread)
        thread {
            println("https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?key=$apiKey&location=$lat,$lon&$reqParam$rankByParam$openParam")
            val placesStr = URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?key=$apiKey&location=$lat,$lon&$reqParam$rankByParam$openParam").readText()
            val placesJSON = JSONObject(placesStr)

            if (placesJSON.getString("status") == "OK") {
                // At least one result is available
                val places = placesJSON.getJSONArray("results")

                for (i in 0 until places.length()) {
                    val placeID = places.getJSONObject(i).getString("place_id")

                    val detailsStr = URL("https://maps.googleapis.com/maps/api/place/" +
                            "details/json?key=$apiKey&place_id=$placeID&fields=photo,name," +
                            "formatted_address,rating,review,geometry,type,opening_hours,url"
                    ).readText()
                    val detailsJSON = JSONObject(detailsStr)
                    println(detailsJSON.toString(2))
                    result = detailsJSON.getJSONObject("result")

                    val address = result!!.getString("formatted_address")
                    val location = result!!.getJSONObject("geometry")
                        .getJSONObject("location")
                    val name = result!!.getString("name")
                    val hours = result!!.optJSONObject("opening_hours")
                    val isOpen = hours?.getBoolean("open_now")
                    val schedule = hours?.getJSONArray("weekday_text")
                    val photos = result!!.optJSONArray("photos")
                    val rating = result!!.optDouble("rating", 0.0)
                    val reviews = result!!.optJSONArray("reviews")
                    val placeType = result!!.optJSONArray("types")
                    val url = result!!.getString("url")
                    println(
                        getString(
                            R.string.place_details,
                            photos?.length(),
                            name,
                            address,
                            "%.1f".format(rating),
                            reviews?.length(),
                            placeType?.toString(),
                            location.toString(2),
                            isOpen,
                            schedule?.toString(2),
                            url
                        )
                    )

                    val place = if (isOpen != null) {
                        Place(address = address, name = name, isOpen = isOpen,
                            rating = rating.toFloat(), url = url,
                            latitude =location.getDouble("lat"),longitude = location.getDouble("lng"))
                    } else {
                        Place(address = address, name = name, rating = rating.toFloat(), url = url,
                            latitude =location.getDouble("lat"),longitude = location.getDouble("lng"))
                    }
                    placesList.add(place)

                    if (i == 2) {
                        break // no need to show more than 3 results (for now)
                    }
                }

                activity!!.runOnUiThread {
                    // Remember that you can only change UI elements in the main thread
                    placesAdapter.refreshData()
                    textViewLoading.visibility = View.GONE
                }
            } else {
                // Try to output an error message, else show a generic "no results" message
                activity!!.runOnUiThread {
                    textViewLoading.text = getString(R.string.no_results)
                }
            }
        }
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
        if (event == currentTemp || event == light) {
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
                val temp = event.values?.get(0)

                if (prevTemp != null) {
                    val diff = temp?.minus(prevTemp!!)
                    // Check for temperature change when there's at least 2 degrees of change
                    if (diff?.let { abs(it) }!! >= 2) {
                        prevTemp = temp
                        println("temp is $temp $unitTemp " +
                                "(${cToF(temp)} ${getString(
                                    R.string.temp_fahrenheit
                                )})")

                        // Sample recommendations based on temperature
                        recommendations = if (temp <= 0) {
                            "Restaurant"
                        } else if (temp > 0 && temp <= 5) {
                            "university"
                        } else if (temp > 5 && temp <= 15) {
                            "library"
                        } else if(temp > 15 && temp <= 20) {
                            "gym"
                        } else {
                            "park"
                        }
                    }
                } else {
                    prevTemp = temp
                }
            }
            Sensor.TYPE_LIGHT -> {
                val bright= event.values[0]

                if (prevLight!=null) {
                    val diff2 = bright.minus(prevLight!!)
                    // Check for light if there's at least a 2 lx change
                    if (abs(diff2) >= 2) {
                        prevLight = bright
                        println("Light levels are $bright $unitLight " +
                                "(${lxToFc(bright)} ${getString(
                                    R.string.light_foot_candle
                                )})")
                        recPrev = recommendations

                        // Sample recommendations based on light level
                        recommendations = if (bright <= 500) {
                            "restaurant"
                        } else if (bright > 500 && bright <= 2000) {
                            "university"
                        } else if(bright > 2000 && bright <= 10000) {
                            "gym"
                        } else if(bright > 10000 && bright <= 20000) {
                            "tourist"
                        } else {
                            "park"
                        }
                    }
                } else {
                    prevLight = bright
                }
            }
        }
    }
}
