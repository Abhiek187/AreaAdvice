package com.example.areaadvice

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment


class SettingsMenu : Fragment() {

    private lateinit var mContext: Context
    private lateinit var senEnable: Switch
    private lateinit var recommendSensor: Switch
    private lateinit var RadiusSeek: SeekBar
    private lateinit var radiusSeekBarText: TextView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var radGroup: RadioGroup
    private lateinit var radBtn: RadioButton
    private lateinit var radBtnSI: RadioButton
    private lateinit var radBtnUSA: RadioButton
    private lateinit var radBtnDistance: RadioButton
    private lateinit var radBtnRatings: RadioButton
    private lateinit var radGroup2: RadioGroup

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_settings_menu, container, false)
        sharedPref = activity!!.getSharedPreferences("MyPref", 0)
        val editor = sharedPref.edit()

        senEnable= view.findViewById(R.id.enableSensorRecToggle)
        radiusSeekBarText = view.findViewById(R.id.radiusSeekBarText)
        recommendSensor = view.findViewById(R.id.enableSensorRecToggle)
        RadiusSeek = view.findViewById(R.id.radiusSeekBar)
        radGroup = view.findViewById(R.id.radiogroup)
        radBtn = view.findViewById(radGroup.checkedRadioButtonId)
        radBtnSI = view.findViewById(R.id.unitsSI)
        radBtnUSA = view.findViewById(R.id.unitsUSA)

        val radCheck = sharedPref.getInt("units", 1)
        if(radCheck == 1) radBtnUSA.isChecked = true
        else radBtnSI.isChecked = true

        radBtn.setOnCheckedChangeListener{_,_ ->
            if(radBtnUSA.isChecked){
                editor.putInt("units", 1)
            } else {
                editor.putInt("units", 2)
            }
            editor.apply()
        }

        radiusSeekBarText.text= sharedPref.getString("radius","1").toString()
        senEnable.isChecked = sharedPref.getBoolean("senEnable",true)
        sharedPref.getString("check", senEnable.text.toString())

        senEnable.setOnCheckedChangeListener { _, _ ->
            changeSwitchText()
        }

        RadiusSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                radiusSeekBarText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                radiusSeekBarText.text = seekBar.progress.toString()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                radiusSeekBarText.text = seekBar.progress.toString()
                editor.putString("radius",radiusSeekBarText.text.toString())
                editor.apply()
            }
        })

        return view
    }

    private fun changeSwitchText(){
        sharedPref = activity!!.getSharedPreferences("MyPref", 0)
        val editor = sharedPref.edit()
        editor.putBoolean("senEnable", senEnable.isChecked)
        if(senEnable.isChecked) {
            senEnable.text = "On"
            editor.putString("check", senEnable.text.toString())
        }
        else {
            senEnable.text = "Off"
            editor.putString("check", senEnable.text.toString())
        }
        editor.apply()
    }

}
