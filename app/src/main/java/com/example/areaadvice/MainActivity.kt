package com.example.areaadvice

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread
import kotlin.math.abs


class MainActivity : AppCompatActivity(),SensorEventListener {
    // UI elements
    private lateinit var textViewPlacesInfo: TextView
    private lateinit var editTextSearch: EditText
    private lateinit var sensorManager: SensorManager
    private var currentTemp: Sensor? =null
    private var light: Sensor?=null
    private var prevTemp: Float? = null
    private var prevLight:Float?=null
    /* Steps to hide your API key:
     * 1. Create google_apis.xml in values folder (Git will ignore this file)
     * 2. Add API key as string resource named google_places_key
     * 3. Protect yourself from Chrysnosis (sorry Krishna)
     */
    private lateinit var apiKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.sensorManager= getSystemService(Context.SENSOR_SERVICE) as SensorManager
        currentTemp=sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        light=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
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

    override  fun onResume(){
        super.onResume()
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

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        if(p0==currentTemp){
            //println("accuracy is $p1")
            if(p1==0){
                println("unreliable")
            }
            if(p1==1){
                println("low accuracy")
            }
            if(p1==2){
                println("Medium accuracy")
            }
            if(p1==3){
                println("Very accurate")
            }
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        when(p0?.sensor?.type){
            Sensor.TYPE_AMBIENT_TEMPERATURE->{
                val temp= p0.values?.get(0)
                if(prevTemp!=null){
                    val diff= temp?.minus(prevTemp!!)
                    if(diff?.let { abs(it) }!! >=2){
                        prevTemp=temp
                        println("temp is $temp")
                    }
                }
                prevTemp=temp
            }
            Sensor.TYPE_LIGHT->{
                val bright= p0.values[0]
                if(prevLight!=null){
                    val diff2= bright.minus(prevLight!!)
                    if(abs(diff2)>=2){
                        prevLight=bright
                        println("Light levels are $bright")
                    }
                }
                prevLight=bright
            }
        }
    }


}
