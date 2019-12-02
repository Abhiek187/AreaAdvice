package com.example.areaadvice.activities

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.areaadvice.R
import com.example.areaadvice.storage.DatabasePlaces
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationInfoMenu : AppCompatActivity()  {

    //private lateinit var mContext: Context
    private lateinit var locName: TextView
    private lateinit var locAddress: TextView
    private lateinit var locProximity: TextView
    private lateinit var locHours: TextView
    private lateinit var locRating: RatingBar

    //private val db = DatabasePlaces(mContext)
    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_location_info_menu)
        val db = DatabasePlaces(this)
        val cursor = db.getAllRows()

        locName = findViewById(R.id.locationName)
        locAddress = findViewById(R.id.locationAddress)
        locProximity = findViewById(R.id.locationProximity)
        locHours = findViewById(R.id.locationHours)
        locRating = findViewById(R.id.ratingBar)

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val mLocLat = cursor.getString(cursor.getColumnIndex(DatabasePlaces.Col_Lat))
            val mLocLong = cursor.getString(cursor.getColumnIndex(DatabasePlaces.Col_Lng))
        }
    }
}


fun distanceBetweenPoints(Lat1: String, Long1: String, Lat2: String, Long2: String): Double {
    val avgRadius = 6371.0
    val lat1 = Lat1.toDouble()
    val long1 = Long1.toDouble()
    val lat2 = Lat2.toDouble()
    val long2 = Long2.toDouble()
    val latDistance = Math.toRadians(lat1 - lat2)
    val longDistance = Math.toRadians(long1 - long2)

    val a =
        (sin(latDistance / 2) * sin(latDistance / 2)) +
                (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(longDistance / 2) * sin(longDistance / 2))
    val c = 2* atan2(sqrt(a), sqrt(1 - a))

    return (avgRadius*c*1000) //in meters
}
