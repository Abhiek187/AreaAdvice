package com.example.areaadvice.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.areaadvice.R
import com.example.areaadvice.storage.Prefs

class SettingsMenu : Fragment() {
    // The most important variables
    private lateinit var mContext: Context
    private lateinit var sharedPref: Prefs

    // UI elements
    private lateinit var senEnable: Switch

    private lateinit var radiusSeekBarText: TextView
    private lateinit var radiusSeek: SeekBar

    private lateinit var radGroup: RadioGroup // How do you do, fellow kids?
    private lateinit var radBtn: RadioButton
    private lateinit var radBtnSI: RadioButton
    private lateinit var radBtnUSA: RadioButton

    private lateinit var radGroup2: RadioGroup
    private lateinit var radBtn2: RadioButton
    private lateinit var radBtnDistance: RadioButton
    private lateinit var radBtnRatings: RadioButton

    private lateinit var openLocEnable: Switch

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_menu, container, false)
        sharedPref = Prefs(mContext)

        senEnable= view.findViewById(R.id.enableSensorRecToggle)

        radiusSeekBarText = view.findViewById(R.id.radiusSeekBarText)
        radiusSeek = view.findViewById(R.id.radiusSeekBar)

        radGroup = view.findViewById(R.id.radiogroup)
        radBtn = view.findViewById(radGroup.checkedRadioButtonId)
        radBtnSI = view.findViewById(R.id.unitsSI)
        radBtnUSA = view.findViewById(R.id.unitsUSA)

        radGroup2 = view.findViewById(R.id.radiogroup2)
        radBtn2 = view.findViewById(radGroup2.checkedRadioButtonId)
        radBtnDistance = view.findViewById(R.id.distance)
        radBtnRatings = view.findViewById(R.id.ratings)

        openLocEnable = view.findViewById(R.id.showOpenPlacesToggle)

        // Check which units to use
        val radCheck = sharedPref.units

        if (radCheck == 1) {
            radBtnSI.isChecked = true
            radiusSeek.max = 80
        } else {
            radBtnUSA.isChecked = true
            radiusSeek.max = 50 // 50 mi ~= 80 km
        }

        radBtn.setOnCheckedChangeListener { _, _ ->
            if (radBtnUSA.isChecked) {
                sharedPref.units = 2
                radiusSeek.max = 50
            } else {
                sharedPref.units = 1
                radiusSeek.max = 80
            }
            sharedPref.radiusText = radiusSeekBarText.text.toString()
            println("USA? ${radBtnUSA.isChecked}, radiusSeek.max: ${radiusSeek.max}")
        }

        // Check radius seek bar
        radiusSeekBarText.text = sharedPref.radiusText
        radiusSeek.progress = sharedPref.radiusBar

        radiusSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                radiusSeekBarText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                radiusSeekBarText.text = seekBar.progress.toString()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                radiusSeekBarText.text = seekBar.progress.toString()
                sharedPref.radiusText = radiusSeekBarText.text.toString()
                sharedPref.radiusBar = radiusSeek.progress
            }
        })

        // Check which criteria we're satisfying
        val radCheck2 = sharedPref.criteria

        if (radCheck2 == 2) {
            radBtnRatings.isChecked = true
        } else {
            radBtnDistance.isChecked = true
        }

        radBtn2.setOnCheckedChangeListener { _, _ ->
            if (radBtnRatings.isChecked) {
                sharedPref.criteria = 2
            } else {
                sharedPref.criteria = 1
            }
            println("Ratings? ${radBtnRatings.isChecked}")
        }

        // Check sensors
        senEnable.isChecked = sharedPref.senEnable
        senEnable.text = sharedPref.senText

        senEnable.setOnCheckedChangeListener { _, _ ->
            sharedPref.senEnable = senEnable.isChecked

            if (senEnable.isChecked) {
                senEnable.text = getString(R.string.OnChoice)
            } else {
                senEnable.text = getString(R.string.OffChoice)
            }

            sharedPref.senText = senEnable.text.toString()
        }

        // Check open locations
        openLocEnable.isChecked = sharedPref.openEnable
        openLocEnable.text = sharedPref.openText

        openLocEnable.setOnCheckedChangeListener { _, _ ->
            sharedPref.openEnable = openLocEnable.isChecked

            if (openLocEnable.isChecked) {
                openLocEnable.text = getString(R.string.OnChoice)
            } else {
                openLocEnable.text = getString(R.string.OffChoice)
            }

            sharedPref.openText = openLocEnable.text.toString()
        }

        return view
    }
}
