package com.example.areaadvice.activities

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.areaadvice.R
import com.example.areaadvice.storage.DatabasePlaces
import com.example.areaadvice.storage.Prefs
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
        val sharedPrefs = Prefs(this)
        locName = findViewById(R.id.locationName)
        locAddress = findViewById(R.id.locationAddress)
        locProximity = findViewById(R.id.locationProximity)
        locHours = findViewById(R.id.locationHours)
        locRating = findViewById(R.id.ratingBar)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        locName.text=intent.getStringExtra("name")
        locAddress.text=intent.getStringExtra("address")
        locRating.rating=intent.getStringExtra("rating")!!.toFloat()
        locHours.text=intent.getStringExtra("isOpen")
        val lng=intent.getDoubleExtra("longitude",0.0)
        val lat=intent.getDoubleExtra("latitude",0.0)
        val currentLat=intent.getFloatExtra("lat",0F)
        val currentLng=intent.getFloatExtra("long",0F)
        if(sharedPrefs.units==1) {
            locProximity.text = distanceBetweenPoints(
                lat.toString(),
                lng.toString(),
                currentLat.toString(),
                currentLng.toString()
            ).toString()
        }
        else{
            locProximity.text = (distanceBetweenPoints(
                lat.toString(),
                lng.toString(),
                currentLat.toString(),
                currentLng.toString()
            )/1.609).toString()
        }

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val mLocLat = cursor.getString(cursor.getColumnIndex(DatabasePlaces.Col_Lat))
            val mLocLong = cursor.getString(cursor.getColumnIndex(DatabasePlaces.Col_Lng))
            cursor.moveToNext()
        }
        cursor.close()
        saveBtn.setOnClickListener{
            val db = DatabasePlaces(this)
            val newInfo = db.writableDatabase
            val checkInfo=db.readableDatabase
            var repeat=false

            val cursor2 = checkInfo.query(
                DatabasePlaces.Table_Name,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,//selection,              // The columns for the WHERE clause
                null,//selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null             // The sort order
            )

            //var len = (cursor2.count > 0)
            with(cursor2) {
                while (moveToNext()) {
                    //println("database "+getString(getColumnIndexOrThrow(Database_Places.Col_place_Name)))
                    //println("current "+it.getString("name"))
                    if (this!!.getString(getColumnIndexOrThrow(DatabasePlaces.Col_Address))!!.contentEquals(locAddress.text.toString()))
                    {repeat=true

                    }
                }
            }
            if (!repeat) {
                //val tempLoc = it.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                //val tempLat1 = tempLoc.substringAfter(":")
                //val tempLat2 = tempLat1.substringBefore(",")
                //val tempLng = it.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                //val tempLng2 = tempLng.substringBefore("}")
                val addVal = ContentValues().apply {
                    put(DatabasePlaces.Col_place_Name, locName.text.toString())
                    put(DatabasePlaces.Col_Address, locAddress.text.toString())
                    put(DatabasePlaces.Col_Rating, locRating.rating.toString())
                    put(DatabasePlaces.Col_Lat, intent.getDoubleExtra("latitude",0.0))
                    put(DatabasePlaces.Col_Lng, intent.getDoubleExtra("longitude",0.0))
                }
                newInfo?.insert(DatabasePlaces.Table_Name, null, addVal)
            }
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

    return (avgRadius*c) //in kilometers
}
