package com.example.areaadvice.activities

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.areaadvice.R
import com.example.areaadvice.storage.DatabasePlaces
import com.example.areaadvice.storage.Prefs
import com.example.areaadvice.utils.kmToMi
import com.squareup.picasso.Picasso
import java.io.InputStream
import java.net.URL
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
    private lateinit var photo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_location_info_menu)

        val sharedPrefs = Prefs(this)
        locName = findViewById(R.id.locationName)
        locAddress = findViewById(R.id.locationAddress)
        locProximity = findViewById(R.id.locationProximity)
        locSchedule = findViewById(R.id.locationHours)
        locRating = findViewById(R.id.ratingBar)
        photo = findViewById(R.id.photo)
        val saveBtn = findViewById<ImageButton>(R.id.saveBtn)
        val delBtn = findViewById<ImageButton>(R.id.delBtn)

        locName.text = intent.getStringExtra("name")
        locAddress.text = intent.getStringExtra("address")
        locRating.rating = intent.getStringExtra("rating")!!.toFloat()
        val lat = intent.getDoubleExtra("latitude",0.0)
        val lng = intent.getDoubleExtra("longitude",0.0)
        val currentLat = intent.getFloatExtra("lat",0F)
        val currentLng = intent.getFloatExtra("long",0F)
        val open = intent.getStringExtra("isOpen")
        val url = intent.getStringExtra("url")
        val urlImage = intent.getStringExtra("photo")
        println("urlImage is $urlImage")
        println("Url is $url")
        /*var sub1 = urlImage.substringAfter("http")
        sub1="http"+sub1
        sub1=sub1.replace("\\", "")
        sub1=sub1.substringBefore(">")
        sub1=sub1.substring(0,sub1.length-1)*/
        var photoRef = urlImage.substringAfter("photo_reference")
        //photoRef = photoRef.substringBefore("width")
        //val photoWidth = urlImage.substringAfter("width")
        val apikey = getString(R.string.google_places_key)
        var photoImageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=1000&photoreference=$photoRef&key=$apikey"
        //photoImageUrl=photoImageUrl.replace("PhotoWidth","1000")
        //photoImageUrl=photoImageUrl.replace("CnRtAAAATLZNl354RwP_9UKbQ_5Psy40texXePv4oAlgP4qNEkdIrkyse7rPXYGd9D_Uj1rVsQdWT4oRz4QrYAJNpFX7rzqqMlZw2h2E2y5IKMUZ7ouD_SlcHxYq1yL4KbKUv3qtWgTK0A6QbGh87GB3sscrHRIQiG2RrmU_jF4tENr9wGS_YxoUSSDrYjWmrNfeEHSGSc3FyhNLlBU",photoRef)
        //photoImageUrl = photoImageUrl.replace("YOUR_API_KEY",getString(R.string.google_places_key))
        //println("sub1 is $sub1")
        println("PhotoImageUrl is $photoImageUrl")
        loadImage(photoImageUrl)
        //DownloadImageTask(photo).execute(photoImageUrl)
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
            val str = day.replace("[","").replace("]","")
                .replace("\"", "")

            if (str == "null") {
                locSchedule.text = getString(R.string.no_schedule) // no schedule is available
            } else if (str.isNotEmpty()) {
                locSchedule.text = String.format("%s%s\n", locSchedule.text.toString(), str)
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
                    put(DatabasePlaces.Col_Url,url)
                    put(DatabasePlaces.Col_Photo,photoRef)
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
            var id = 0
            val cursor = db.getAllRows()
            with(cursor) {
                while (moveToNext()) {
                    if (this.getString(getColumnIndex(DatabasePlaces.Col_Address))
                        == locAddress.text.toString()) {
                        repeat = true
                        id = this.getInt(getColumnIndex(DatabasePlaces.Col_Id))
                        break
                    }
                }
            }
            if (repeat) {
                db.deleteRow(id)
                Toast.makeText(this,
                    "This location has been deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    "This location is not saved in the database", Toast.LENGTH_SHORT).show()
            }
            cursor.close()
        }
    }

    private fun loadImage(url: String){
        Picasso.with(this)
            .load(url)
            .into(photo)
        /*return try {
            val `is`: InputStream = URL(url).content as InputStream
            val d = Drawable.createFromStream(`is`, "src name")
            //photo=d.toBitmap().toIcon()
            //photo.setImageBitmap(d.toBitmap())
            println("in loadImage")
            photo.setImageIcon(d.toBitmap().toIcon())
        } catch (e: Exception) {
            println(e)
            println("Exception")
        }*/
        /*val urls =
            URL(url)
        val bmp = BitmapFactory.decodeStream(urls.openConnection().getInputStream())
        photo.setImageBitmap(bmp)*/

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

/*private class DownloadImageTask(var bmImage: ImageView) :
    AsyncTask<String?, Void?, Bitmap?>() {
     override fun doInBackground(vararg urls: String?): Bitmap? {
        val urldisplay = urls[0]
        var mIcon11: Bitmap? = null
        try {
            val `in`: InputStream = URL(urldisplay).openStream()
            mIcon11 = BitmapFactory.decodeStream(`in`)
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
            e.printStackTrace()
        }
        return mIcon11
    }

    override fun onPostExecute(result: Bitmap?) {
        bmImage.setImageBitmap(result)
    }

}*/
