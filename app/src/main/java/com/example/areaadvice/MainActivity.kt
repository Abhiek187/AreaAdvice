package com.example.areaadvice

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest

class MainActivity : AppCompatActivity() {

    private lateinit var navbar: BottomNavigationView
    private lateinit var map: Button
    private lateinit var clear:Button

    private lateinit var sensorManager: SensorManager
    private var currentTemp: Sensor? =null
    private var light: Sensor?=null
    private var prevTemp: Float? = null
    private var prevLight:Float?=null
    private var recommendations: String=""
    private var recPrev: String=""
    private var lightSen=true;

   // private var manualRec=false;
    /* Steps to hide your API key:
     * 1. Create google_apis.xml in values folder (Git will ignore this file)
     * 2. Add API key as string resource named google_places_key
     * 3. Protect yourself from Chrysnosis by deleting his GitHub branch to avoid any running errors (sorry Krishna)
     */
    private lateinit var apiKey: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    var lat = 0.0
    var lon = 0.0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        changeFragment(Home())
        navbar = findViewById(R.id.nav_bar)
        navbar.setOnNavigationItemSelectedListener {item ->
            val fm = supportFragmentManager.beginTransaction()
            when(item.itemId){
                R.id.Home ->{
                    println("Home Clicked")
                    /*val active = SettingsMenu()
                    fm.hide(active).show(Home()).commit()*/
                    val fragment = Home()
                    fm.hide(SettingsMenu())
                    changeFragment(fragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.Settings ->{
                    println("Settings Clicked")
                    /*val active = Home()
                    fm.hide(active).show(SettingsMenu()).commit()*/
                    val fragment = SettingsMenu()
                    fm.hide(Home())
                    changeFragment(fragment)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false

        }
    }
    private fun changeFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentcontainer, fragment).commit()
        return true
    }
}
