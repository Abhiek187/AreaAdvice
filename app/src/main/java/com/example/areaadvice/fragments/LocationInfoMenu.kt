package com.example.areaadvice.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import com.example.areaadvice.R
import com.example.areaadvice.storage.DatabasePlaces
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationInfoMenu : Fragment() {

    private lateinit var mContext: Context
    private lateinit var locName: TextView
    private lateinit var locAddress: TextView
    private lateinit var locProximity: TextView
    private lateinit var locHours: TextView
    private lateinit var locRating: RatingBar

    private val db = DatabasePlaces(mContext)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location_info_menu, container, false)
        val cursor = db.getAllRows()

        locName = view.findViewById(R.id.locationName)
        locAddress = view.findViewById(R.id.locationAddress)
        locProximity = view.findViewById(R.id.locationProximity)
        locHours = view.findViewById(R.id.locationHours)
        locRating = view.findViewById(R.id.ratingBar)

        cursor.moveToFirst()
        while (!cursor.isAfterLast){
            val mLocLat = cursor.getString(cursor.getColumnIndex(DatabasePlaces.Col_Lat))
            val mLocLong = cursor.getString(cursor.getColumnIndex(DatabasePlaces.Col_Lng))

        }

        return view
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
