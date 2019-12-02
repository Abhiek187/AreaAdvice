package com.example.areaadvice.fragments


import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val dB = DatabasePlaces(mContext)
        savedLocationsView = view.findViewById(R.id.savedLocationsRecycler)
        savedLocationsView.layoutManager = LinearLayoutManager(mContext)
        placesAdapter = PlacesAdapter(mContext, placesList)
        savedLocationsView.adapter = placesAdapter
        val divider = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        savedLocationsView.addItemDecoration(divider) // add border between places

        val cursor = dB.getAllRows()
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            val mLocationName = cursor.getString(cursor.getColumnIndex(DatabasePlaces.Col_place_Name))
            val mLocationAddress = cursor.getString(cursor.getColumnIndex(DatabasePlaces.Col_Address))
            val mLocationRating = cursor.getFloat(cursor.getColumnIndex(DatabasePlaces.Col_Rating))

            val place = Place(address = mLocationAddress, name = mLocationName, rating = mLocationRating, url = "")
            placesList.add(place)
            cursor.moveToNext()
        }
        placesAdapter.refreshData()
        cursor.close()
        return view
    }


}