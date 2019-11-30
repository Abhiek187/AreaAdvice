package com.example.areaadvice


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment


/**
 * A simple [Fragment] subclass.
 */
class SettingsMenu : Fragment() {
    private var radius="0"
    //private var recSwitch=true

    private lateinit var recSwitch: Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val intent = Intent(
            activity!!.baseContext,
            Home::class.java
        )


        val view=inflater.inflate(R.layout.fragment_settings_menu, container, false)

        recSwitch= view.findViewById(R.id.enableSensorRecToggle)
        intent.putExtra("radius", radius )
        intent.putExtra("recSwitch",recSwitch.isChecked)
        return view
    }


}
