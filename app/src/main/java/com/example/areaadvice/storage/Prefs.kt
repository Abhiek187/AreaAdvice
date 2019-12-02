package com.example.areaadvice.storage

import android.content.Context
import android.content.SharedPreferences

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
    private val prefs: SharedPreferences = context.getSharedPreferences(fileName,
        Context.MODE_PRIVATE)

    var senText: String
        get() = prefs.getString(senTextKey, "Off")!! // default: disable sensors
        set(value) = prefs.edit().putString(senTextKey, value).apply()
    var senEnable: Boolean
        get() = prefs.getBoolean(senEnableKey, false)
        set(value) = prefs.edit().putBoolean(senEnableKey, value).apply()
    var radiusText: String
        get() = prefs.getString(radiusTextKey, "25")!! // default: 25 km
        set(value) = prefs.edit().putString(radiusTextKey, value).apply()
    var radiusBar: Int
        get() = prefs.getInt(radiusBarKey, 25)
        set(value) = prefs.edit().putInt(radiusBarKey, value).apply()
    var units: Int
        get() = prefs.getInt(unitsKey, 1) // default: metric units
        set(value) = prefs.edit().putInt(unitsKey, value).apply()
    var criteria: Int
        get() = prefs.getInt(criteriaKey, 2) // default: search by ratings
        set(value) = prefs.edit().putInt(criteriaKey, value).apply()
    var openText: String
        get() = prefs.getString(openTextKey, "On")!! // default: must be open
        set(value) = prefs.edit().putString(openTextKey, value).apply()
    var openEnable: Boolean
        get() = prefs.getBoolean(openEnableKey, true)
        set(value) = prefs.edit().putBoolean(openEnableKey, value).apply()
}
