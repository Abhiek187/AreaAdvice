package com.example.areaadvice.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areaadvice.R
import com.example.areaadvice.adapters.PlacesAdapter
import com.example.areaadvice.models.Place
import com.example.areaadvice.storage.DatabasePlaces

class SavedLocations : Fragment() {

    private lateinit var mContext: Context
    private lateinit var savedLocationsView: RecyclerView
    private var placesList = arrayListOf<Place>()
    private lateinit var placesAdapter: PlacesAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_locations, container, false)

        savedLocationsView = view.findViewById(R.id.savedLocationsRecycler)
        savedLocationsView.layoutManager = LinearLayoutManager(mContext)
        placesAdapter = PlacesAdapter(mContext, placesList)
        savedLocationsView.adapter = placesAdapter
        val divider = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        savedLocationsView.addItemDecoration(divider) // add border between places

        refreshList()
        return view
    }

    private fun refreshList() {
        // Create the recycler view based on what's in the database
        val dB = DatabasePlaces(mContext)
        val cursor = dB.getAllRows()

        if (cursor.count == 0) {
            Toast.makeText(mContext, "No Locations Have Been Saved", Toast.LENGTH_SHORT).show()
        } else {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val mLocationName =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_place_Name))
                val mLocationAddress =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Address))
                val mLocationRating =
                    cursor.getFloat(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Rating))
                val reviews = cursor.getString(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Reviews))
                val mLocationLat =
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Lat))
                val mLocationLng =
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Lng))
                val mSchedule =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Schedule))
                val mOpen =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Open))
                val openBool = mOpen == "Open"
                val photoImage = cursor.getString(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Photo))
                val mUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabasePlaces.Col_Url))

                val place = Place(address = mLocationAddress, name = mLocationName,
                    rating = mLocationRating, reviews = reviews, url = mUrl, latitude = mLocationLat,
                    photo = photoImage, longitude = mLocationLng, schedule = mSchedule,
                    isOpen = openBool)

                placesList.add(place)
                cursor.moveToNext()
            }
        }

        placesAdapter.refreshData()
        cursor.close()
    }

    override fun onResume() {
        super.onResume()
        placesList.clear()
        refreshList()
    }
}
