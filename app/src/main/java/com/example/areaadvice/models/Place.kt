package com.example.areaadvice.models

data class Place(
    val address: String,
    /*val lat: Double,
    val lon: Double,*/
    val name: String,
    val isOpen: Boolean = false,
    val rating: Float = 0f,
    //val reviews: ArrayList<String> = arrayListOf(),
    val url: String,
    val latitude: Double,
    val longitude: Double,
    val schedule: String
)
