package com.example.areaadvice.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.areaadvice.R
import com.example.areaadvice.models.Place
import kotlinx.android.synthetic.main.adapter_places.view.*

class PlacesAdapter(private var context: Context, private var placesList: ArrayList<Place>)
    : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Populate RecyclerView with layout from adapter_places
        val inflater = LayoutInflater.from(context).inflate(R.layout.adapter_places, parent,
            false)
        val layoutPlace = inflater.findViewById<ConstraintLayout>(R.id.layoutPlace)

        layoutPlace.setOnClickListener {
            println("Clicked on ${placesList[viewType].name}")
        }

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
