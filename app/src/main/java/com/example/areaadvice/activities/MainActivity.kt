package com.example.areaadvice.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.areaadvice.fragments.Home
import com.example.areaadvice.R
import com.example.areaadvice.fragments.SavedLocations
import com.example.areaadvice.fragments.SettingsMenu

class MainActivity : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        changeFragment(Home()) // start at home fragment
        navBar = findViewById(R.id.nav_bar)

        navBar.setOnNavigationItemSelectedListener { item ->
            val fm = supportFragmentManager.beginTransaction()

            when(item.itemId) {
                R.id.Home -> {
                    val fragment = Home()
                    changeFragment(fragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.savedLocations -> {
                    val fragment = SavedLocations()
                    changeFragment(fragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.Settings -> {
                    val fragment = SettingsMenu()
                    /*fm.hide(Home())
                    fm.hide(SavedLocations())*/
                    changeFragment(fragment)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false

        }
    }

    private fun changeFragment(fragment: Fragment){
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.fragmentcontainer,
            fragment).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit()
    }
}
