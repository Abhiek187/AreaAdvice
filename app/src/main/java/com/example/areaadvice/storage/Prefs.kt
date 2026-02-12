package com.example.areaadvice.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Prefs(context: Context) {
    private val fileName = "MyPref"
    private val senTextKey = "senText"
    private val senEnableKey = "senEnable"
    private val radiusTextKey = "radiusText"
    private val radiusBarKey = "radiusBar"
    private val unitsKey = "units"
    private val criteriaKey = "criteria"
    private val openTextKey = "openText"
    private val openEnableKey = "openEnable"
    private val latKey = "lat"
    private val lngKey = "long"
    private val prefs: SharedPreferences = context.getSharedPreferences(fileName,
        Context.MODE_PRIVATE)

    var senText: String
        get() = prefs.getString(senTextKey, "Off")!! // default: disable sensors
        set(value) = prefs.edit { putString(senTextKey, value) }
    var senEnable: Boolean
        get() = prefs.getBoolean(senEnableKey, false)
        set(value) = prefs.edit { putBoolean(senEnableKey, value) }
    var radiusText: String
        get() = prefs.getString(radiusTextKey, "25")!! // default: 25 km
        set(value) = prefs.edit { putString(radiusTextKey, value) }
    var radiusBar: Int
        get() = prefs.getInt(radiusBarKey, 25)
        set(value) = prefs.edit { putInt(radiusBarKey, value) }
    var units: Int
        get() = prefs.getInt(unitsKey, 2) // default: US units
        set(value) = prefs.edit { putInt(unitsKey, value) }
    var criteria: Int
        get() = prefs.getInt(criteriaKey, 2) // default: search by ratings
        set(value) = prefs.edit { putInt(criteriaKey, value) }
    var openText: String
        get() = prefs.getString(openTextKey, "On")!! // default: must be open
        set(value) = prefs.edit { putString(openTextKey, value) }
    var openEnable: Boolean
        get() = prefs.getBoolean(openEnableKey, true)
        set(value) = prefs.edit { putBoolean(openEnableKey, value) }
    var lat: Float
        get() = prefs.getFloat(latKey, 91f) // default: invalid lat and lon
        set(value) = prefs.edit { putFloat(latKey, value) }
    var lng: Float
        get() = prefs.getFloat(lngKey, 181f)
        set(value) = prefs.edit { putFloat(lngKey, value) }
}
