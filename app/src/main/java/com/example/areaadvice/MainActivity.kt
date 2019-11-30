package com.example.areaadvice

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var navbar: BottomNavigationView

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

