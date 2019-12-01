package com.example.areaadvice.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areaadvice.models.Place

class PlacesAdapter(private var context: Context, private var placesList: ArrayList<Place>)
    : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Get all properties from activity_placesview
    /*val contactName: TextView = view.viewContact
    val checkBox: CheckBox = view.checkContact*/
}
