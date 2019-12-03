package com.example.areaadvice.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.areaadvice.R
import com.example.areaadvice.activities.LocationInfoMenu
import com.example.areaadvice.models.Place
import com.example.areaadvice.storage.Prefs
import kotlinx.android.synthetic.main.adapter_places.view.*

class PlacesAdapter(private var context: Context, private var placesList: ArrayList<Place>)
    : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Populate RecyclerView with layout from adapter_places
        val inflater = LayoutInflater.from(context).inflate(R.layout.adapter_places, parent,
            false)
        val layoutPlace = inflater.findViewById<ConstraintLayout>(R.id.layoutPlace)

        return ViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewName.text = placesList[position].name
        holder.ratingBar.rating = placesList[position].rating
        holder.textViewAddress.text = placesList[position].address
        val isOpen = placesList[position].isOpen

        if (isOpen) {
            holder.textViewOpen.setTextColor(Color.GREEN)
            holder.textViewOpen.text = context.getString(R.string.open)
        } else {
            holder.textViewOpen.setTextColor(Color.RED)
            holder.textViewOpen.text = context.getString(R.string.closed)
        }
        
        holder.itemView.setOnClickListener{
            val sharedPrefs = Prefs(context)
            val currentLat= sharedPrefs.lats
            val currentLng=sharedPrefs.lngs
            val intent = Intent(context, LocationInfoMenu::class.java)

            intent.putExtra("name",holder.textViewName.text.toString())
            intent.putExtra("address",holder.textViewAddress.text.toString())
            intent.putExtra("rating",holder.ratingBar.rating.toString())
            intent.putExtra("isOpen",holder.textViewOpen.text.toString())
            intent.putExtra("latitude",placesList[position].latitude)
            intent.putExtra("longitude",placesList[position].longitude)
            intent.putExtra("lat",currentLat)
            intent.putExtra("long",currentLng)
            intent.putExtra("schedule",placesList[position].schedule)

            this.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun refreshData() {
        notifyDataSetChanged()
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Get all properties from adapter_places
    val textViewName: TextView = view.textViewName
    val ratingBar: RatingBar = view.ratingBar
    val textViewAddress: TextView = view.textViewAddress
    val textViewOpen: TextView = view.textViewOpen
}
