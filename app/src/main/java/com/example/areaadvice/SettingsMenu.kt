package com.example.areaadvice

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class SettingsMenu : Fragment() {

    private lateinit var mContext: Context
    private lateinit var recommendSensor: Switch
    private lateinit var maxRadiusSeek: SeekBar
    private lateinit var maxRadiusSeekText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_settings_menu, container, false)

        recommendSensor = view.findViewById(R.id.enableSensorRecToggle)
        maxRadiusSeek = view.findViewById(R.id.radiusSeekBar)
        maxRadiusSeekText = view.findViewById(R.id.radiusSeekBarText)

        maxRadiusSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                maxRadiusSeekText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                maxRadiusSeekText.text = seekBar.progress.toString()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                maxRadiusSeekText.text = seekBar.progress.toString()
            }

        })


        return view
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        mContext = context
    }
}
