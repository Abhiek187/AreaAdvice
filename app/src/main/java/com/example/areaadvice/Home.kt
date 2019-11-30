package com.example.areaadvice


import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorEventListener
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread
import kotlin.math.abs

class Home : Fragment(), SensorEventListener {

    private lateinit var mContext: Context
    private lateinit var textViewPlacesInfo: TextView
    private lateinit var editTextSearch: EditText
    private lateinit var mapBtn: Button
    private lateinit var clearBtn: Button
    private lateinit var imageButtonSearch: ImageButton

    private lateinit var sensorManager: SensorManager
    private var currentTemp: Sensor? =null
    private var light: Sensor?=null
    private var prevTemp: Float? = null
    private var prevLight:Float?=null
    private var recommendations: String=""
    private var recPrev: String=""
    private var lightSen=true

    private lateinit var apiKey: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    var lat = 0.0
    var lon = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        this.sensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        currentTemp=sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        light=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
        }


        textViewPlacesInfo = view.findViewById(R.id.textViewPlacesInfo)
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


        imageButtonSearch.setOnClickListener {
            // Initiate search
            val query = editTextSearch.text.toString()

            if (!isOnline()) {
                Toast.makeText(mContext, "Can't access the internet.", Toast.LENGTH_SHORT)
                    .show()
                /*val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)*/
            } else if (query.isEmpty()) {
                Toast.makeText(mContext, "Search query is empty.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                textViewPlacesInfo.text = getString(R.string.loading)
                lookupPlaces(query)
            }
        }

        getLocationUpdates()



        mapBtn.setOnClickListener {

            val intent = Intent(mContext, MapsActivity::class.java)
            intent.putExtra("lat",lat)
            intent.putExtra("long",lon)
            startActivity(intent)
        }

        clearBtn.setOnClickListener {
            textViewPlacesInfo.text = ""
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        mContext = context
    }

    private fun getLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
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
                    println("Main Lat: $lat")


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

    override  fun onResume(){
        super.onResume()
        startLocationUpdates()
        sensorManager.registerListener(this,currentTemp,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,light,SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause(){
        super.onPause()
        sensorManager.unregisterListener(this)
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
                        "json?key=$apiKey&place_id=$placeID&fields=photo,name,formatted_address," +
                        "rating,review,geometry,type,opening_hours,url").readText()
                val detailsJSON = JSONObject(detailsStr)
                println(detailsJSON.toString(2))
                val result = detailsJSON.getJSONObject("result")

                val address = result.getString("formatted_address")
                val location = result.getJSONObject("geometry")
                    .getJSONObject("location")
                val name = result.getString("name")
                val hours = result.optJSONObject("opening_hours")
                val isOpen = hours?.getBoolean("open_now")
                val schedule = hours?.getJSONArray("weekday_text")
                val photos = result.optJSONArray("photos")
                val rating = result.optDouble("rating", 0.0)
                val reviews = result.optJSONArray("reviews")
                val placeType=result.optJSONArray("types")
                val url = result.getString("url")

                activity!!.runOnUiThread {
                    // Remember that you can only change UI elements in the main thread
                    textViewPlacesInfo.text = getString(R.string.place_details, photos?.length(),
                        name, address, "%.1f".format(rating), reviews?.length(),placeType?.toString(),
                        location.toString(2), isOpen, schedule?.toString(2),
                        url)
                }
            } else {
                // Try to output an error message, else show a generic "no results" message
                activity!!.runOnUiThread {
                    textViewPlacesInfo.text = placesJSON.optString("error_message",
                        getString(R.string.no_results))
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
        if(event==currentTemp||event==light){
            if(accuracy==0){
                println("unreliable")
            }
            if(accuracy==1){
                println("low accuracy")
            }
            if(accuracy==2){
                println("Medium accuracy")
            }
            if(accuracy==3){
                println("Very accurate")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when(event?.sensor?.type){
            Sensor.TYPE_AMBIENT_TEMPERATURE->{
                val temp= event.values?.get(0)
                if(prevTemp!=null){
                    val diff= temp?.minus(prevTemp!!)
                    if(diff?.let { abs(it) }!! >=2){
                        prevTemp=temp
                        println("temp is $temp")
                        recommendations = if(temp<0){
                            "Restaurant"
                        } else if(temp> 0 && temp <5){
                            "university"
                        } else if(temp>5 && temp<15){
                            "library"
                        } else if(temp>15 && temp<20){
                            "gym"
                        } else{
                            "park"
                        }
                        if (!isOnline()) {
                            Toast.makeText(mContext, "Can't access the internet.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        else {
                            textViewPlacesInfo.text = getString(R.string.loading)
                            lookupPlaces(recommendations)
                        }
                    }
                }
                else {
                    prevTemp = temp
                }
            }
            Sensor.TYPE_LIGHT->{
                val bright= event.values[0]
                if(prevLight!=null){
                    val diff2= bright.minus(prevLight!!)
                    if(abs(diff2)>=2){
                        prevLight=bright
                        println("Light levels are $bright")
                        recPrev = recommendations

                        recommendations = if(bright<500){
                            "restaurant"
                        } else if(bright>500 && bright<2000){
                            "university"
                        } else if(bright>2000 && bright < 10000){
                            "gym"
                        } else if(bright>10000 && bright<20000){
                            "tourist"
                        } else{
                            "park"
                        }
                        if (!isOnline()) {
                            Toast.makeText(mContext, "Can't access the internet.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        else {
                            if (lightSen && recPrev != recommendations){
                                textViewPlacesInfo.text = getString(R.string.loading)
                                lookupPlaces(recommendations)
                            }
                        }
                    }
                }
                prevLight=bright
            }
        }
    }


}
