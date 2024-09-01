package com.example.areaadvice.activities

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.example.areaadvice.fragments.Home
import com.example.areaadvice.R
import com.example.areaadvice.fragments.SavedLocations
import com.example.areaadvice.fragments.SettingsMenu

class MainActivity : AppCompatActivity() {

    private val fragKey = "fragment"
    private val strHome = "Home"
    private val strSaved = "Saved"
    private val strSettings = "Settings"

    private lateinit var navBar: BottomNavigationView
    private var currentFragment = strHome

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Change the status bar text in light mode to black
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            == Configuration.UI_MODE_NIGHT_NO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    APPEARANCE_LIGHT_STATUS_BARS,
                    APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        navBar = findViewById(R.id.nav_bar)

        if (savedInstanceState != null) {
            // An orientation change occurred (most likely)
            currentFragment = savedInstanceState.getString(fragKey)!!

            when (currentFragment) {
                strHome -> changeFragment(Home())
                strSaved -> changeFragment(SavedLocations())
                strSettings -> changeFragment(SettingsMenu())
            }
        } else {
            changeFragment(Home()) // start at home fragment
        }

        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Home -> {
                    val fragment = Home()
                    currentFragment = strHome
                    changeFragment(fragment)
                    return@setOnItemSelectedListener true
                }
                R.id.savedLocations -> {
                    val fragment = SavedLocations()
                    currentFragment = strSaved
                    changeFragment(fragment)
                    return@setOnItemSelectedListener true
                }
                R.id.Settings -> {
                    val fragment = SettingsMenu()
                    currentFragment = strSettings
                    changeFragment(fragment)
                    return@setOnItemSelectedListener true
                }
            }

            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(fragKey, currentFragment) // save fragment for orientation change
    }

    private fun changeFragment(fragment: Fragment){
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.fragmentcontainer, fragment)
            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit()
    }
}
