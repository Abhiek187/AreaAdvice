package com.example.areaadvice

data class Place(
    val address: String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val isOpen: Boolean = false,
    val rating: Double = 0.0,
    val reviews: ArrayList<String> = arrayListOf(),
    val url: String
)
