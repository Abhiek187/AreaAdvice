package com.example.areaadvice.activities

import android.content.ContentValues
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.areaadvice.R
import com.example.areaadvice.storage.DatabasePlaces
import com.example.areaadvice.storage.Prefs
import com.example.areaadvice.utils.kmToMi
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationInfoMenu : AppCompatActivity()  {

    private lateinit var locName: TextView
    private lateinit var locAddress: TextView
    private lateinit var locProximity: TextView
    private lateinit var locSchedule: TextView
    private lateinit var locRating: RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_location_info_menu)

        val sharedPrefs = Prefs(this)
        locName = findViewById(R.id.locationName)
        locAddress = findViewById(R.id.locationAddress)
        locProximity = findViewById(R.id.locationProximity)
        locSchedule = findViewById(R.id.locationHours)
        locRating = findViewById(R.id.ratingBar)
        val saveBtn = findViewById<ImageButton>(R.id.saveBtn)
        val delBtn = findViewById<Button>(R.id.delBtn)

        locName.text = intent.getStringExtra("name")
        locAddress.text = intent.getStringExtra("address")
        locRating.rating = intent.getStringExtra("rating")!!.toFloat()
        val lat = intent.getDoubleExtra("latitude",0.0)
        val lng = intent.getDoubleExtra("longitude",0.0)
        val currentLat = intent.getFloatExtra("lat",0F)
        val currentLng = intent.getFloatExtra("long",0F)
        val open = intent.getStringExtra("isOpen")

        // Calculate distance to location
        val distance = distanceBetweenPoints(lat, lng, currentLat.toDouble(), currentLng.toDouble())
        if(sharedPrefs.units == 1) {
            locProximity.text = String.format("%.2f km", distance)
        } else {
            locProximity.text = String.format("%.2f mi", kmToMi(distance.toFloat()))
        }

        // Format schedule
        val schedule2 = intent.getStringExtra("schedule")
        val schedule = schedule2!!.split(",")

        locSchedule.text = ""
        for (day in schedule) {
            val str = day.replace("[","")
                .replace("]","")
            if (str == "null") {
                locSchedule.text = getString(R.string.no_schedule) // no schedule is available
            } else if (str.isNotEmpty()) {
                locSchedule.text = String.format("%s%s\n", locSchedule.text.toString(),
                    str.substring(1, str.length - 1))
            }
        }

        saveBtn.setOnClickListener{
            val db = DatabasePlaces(this)
            var repeat = false

            val cursor = db.getAllRows()

            // Check if location is a repeat
            with(cursor) {
                while (moveToNext()) {
                    if (this.getString(getColumnIndexOrThrow(DatabasePlaces.Col_Address))
                        == locAddress.text.toString()) {
                        repeat = true
                        break
                    }
                }
            }
            if (!repeat) {
                val addVal = ContentValues().apply {
                    put(DatabasePlaces.Col_place_Name, locName.text.toString())
                    put(DatabasePlaces.Col_Address, locAddress.text.toString())
                    put(DatabasePlaces.Col_Rating, locRating.rating.toString())
                    put(DatabasePlaces.Col_Lat, intent.getDoubleExtra("latitude",0.0))
                    put(DatabasePlaces.Col_Lng, intent.getDoubleExtra("longitude",0.0))
                    put(DatabasePlaces.Col_Schedule, schedule2.toString())
                    put(DatabasePlaces.Col_Open, open)
                }

                val newInfo = db.writableDatabase
                newInfo.insert(DatabasePlaces.Table_Name, null, addVal)
                Toast.makeText(this, "Your location has been saved!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "This location is already saved.", Toast.LENGTH_SHORT)
                    .show()
            }

            cursor.close()
        }

        delBtn.setOnClickListener{
            val db = DatabasePlaces(this)
            var repeat = false
            var id=0
            val cursor = db.getAllRows()
            with(cursor) {
                while (moveToNext()) {
                    if (this.getString(getColumnIndexOrThrow(DatabasePlaces.Col_Address))
                        == locAddress.text.toString()) {
                        repeat = true
                        id=this.getString(getColumnIndexOrThrow(DatabasePlaces.Col_Id)).toInt()
                        break
                    }
                }

            }
            if(repeat){
                db.deleteRow(id)
                Toast.makeText(this,"This location has been deleted",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this,"This location is not saved in the database",Toast.LENGTH_LONG).show()
            }
            cursor.close()
        }
    }
}


fun distanceBetweenPoints(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
    val avgRadius = 6371.0 // radius of Earth in km
    val latDistance = Math.toRadians(lat1 - lat2)
    val longDistance = Math.toRadians(long1 - long2)

    val a =
        (sin(latDistance / 2) * sin(latDistance / 2)) + (cos(Math.toRadians(lat1))
                * cos(Math.toRadians(lat2)) * sin(longDistance / 2) * sin(longDistance / 2))
    val c = 2* atan2(sqrt(a), sqrt(1 - a))

    return avgRadius * c // in kilometers
}
