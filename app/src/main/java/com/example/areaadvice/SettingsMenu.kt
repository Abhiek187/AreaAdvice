package com.example.areaadvice

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import android.widget.SeekBar
import android.widget.TextView

class SettingsMenu : Fragment() {

    //private var recSwitch=true
    private lateinit var senEnable: Switch
    private lateinit var tempDegree:Switch
    private lateinit var disUnit:Switch

    private lateinit var mContext: Context
    private lateinit var recommendSensor: Switch
    private lateinit var maxRadiusSeek: SeekBar
    private lateinit var maxRadiusSeekText: TextView
    private lateinit var radiusSeekBarText: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*val intent = Intent(
            activity!!.baseContext,
            Home::class.java
        )*/
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_settings_menu, container, false)

        senEnable= view.findViewById(R.id.enableSensorRecToggle)
        radiusSeekBarText=view.findViewById(R.id.radiusSeekBarText)
        tempDegree=view.findViewById(R.id.tempChoiceToggle)
        disUnit=view.findViewById(R.id.distChoiceToggle)

        //intent.putExtra("radius", radiusSeekBarText.text )
        //intent.putExtra("senEnable",senEnable.isChecked)

        val sharedPref: SharedPreferences = activity!!.getSharedPreferences("MyPref", 0)


        val editor = sharedPref.edit()
        senEnable.isChecked=sharedPref.getBoolean("senEnable",true)
        tempDegree.isChecked=sharedPref.getBoolean("tempUnit",true)
        disUnit.isChecked=sharedPref.getBoolean("disUnit",true)
        radiusSeekBarText.text= sharedPref.getString("radius","1").toString()


        senEnable.setOnCheckedChangeListener { _, _ ->
            editor.putBoolean("senEnable", senEnable.isChecked)
            editor.apply()
            editor.commit()
        }

        tempDegree.setOnCheckedChangeListener { _, _ ->
            editor.putBoolean("tempUnit", tempDegree.isChecked)
            editor.apply()
            editor.commit()
        }

        disUnit.setOnCheckedChangeListener { _, _ ->
            editor.putBoolean("disUnit",disUnit.isChecked)
            editor.apply()
            editor.commit()
        }

        recommendSensor = view.findViewById(R.id.enableSensorRecToggle)
        maxRadiusSeek = view.findViewById(R.id.radiusSeekBar)
        maxRadiusSeekText = view.findViewById(R.id.radiusSeekBarText)

        maxRadiusSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                maxRadiusSeekText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                maxRadiusSeekText.text = seekBar.progress.toString()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                maxRadiusSeekText.text = seekBar.progress.toString()
                editor.putString("radius",radiusSeekBarText.text.toString())
                editor.apply()
                editor.commit()
            }
        })

        return view
    }
}
