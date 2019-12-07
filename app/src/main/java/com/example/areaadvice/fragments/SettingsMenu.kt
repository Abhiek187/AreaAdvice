package com.example.areaadvice.fragments

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.areaadvice.R
import com.example.areaadvice.storage.Prefs
import com.example.areaadvice.utils.cToF
import com.example.areaadvice.utils.lxToFc
import kotlin.math.abs
import kotlin.math.roundToInt

class SettingsMenu : Fragment(), SensorEventListener {
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

    private lateinit var textViewSensorTemp: TextView
    private lateinit var textViewSensorLight: TextView

    // Sensors
    private lateinit var sensorManager: SensorManager
    private var temp: Sensor? = null
    private var light: Sensor? = null
    private var prevTemp: Float? = null
    private var prevLight: Float? = null
    private var tempUnits = ""
    private var lightUnits = ""

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
        val radiusSeekMin = 1 // the alternative to radiusSeek.min

        radGroup = view.findViewById(R.id.radiogroup)
        radBtn = view.findViewById(radGroup.checkedRadioButtonId)
        radBtnSI = view.findViewById(R.id.unitsSI)
        radBtnUSA = view.findViewById(R.id.unitsUSA)

        radGroup2 = view.findViewById(R.id.radiogroup2)
        radBtn2 = view.findViewById(radGroup2.checkedRadioButtonId)
        radBtnDistance = view.findViewById(R.id.distance)
        radBtnRatings = view.findViewById(R.id.ratings)

        openLocEnable = view.findViewById(R.id.showOpenPlacesToggle)

        this.sensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        textViewSensorTemp = view.findViewById(R.id.textViewSensorTemp)
        textViewSensorTemp.visibility = if (sharedPref.senEnable) View.VISIBLE else View.GONE
        tempUnits = if (sharedPref.units == 1) getString(R.string.temp_celsius)
            else getString(R.string.temp_fahrenheit)
        textViewSensorLight = view.findViewById(R.id.textViewSensorLight)
        textViewSensorLight.visibility = if (sharedPref.senEnable) View.VISIBLE else View.GONE
        lightUnits = if (sharedPref.units == 1) getString(R.string.light_lux)
            else getString(R.string.light_foot_candle)

        // Check which units to use
        val radCheck = sharedPref.units

        if (radCheck == 1) {
            radBtnSI.isChecked = true
            radiusSeek.max = 80 - radiusSeekMin
        } else {
            radBtnUSA.isChecked = true
            radiusSeek.max = 50 - radiusSeekMin // 50 mi ~= 80 km
        }

        radBtn.setOnCheckedChangeListener { _, _ ->
            if (radBtnUSA.isChecked) {
                sharedPref.units = 2
                radiusSeek.max = 50 - radiusSeekMin
                // Change measurements automatically
                tempUnits = getString(R.string.temp_fahrenheit)
                lightUnits = getString(R.string.light_foot_candle)
                prevTemp?.let {
                    textViewSensorTemp.text = getString(R.string.sensor_temp,
                        cToF(it).roundToInt(), tempUnits)
                }
                prevLight?.let {
                    textViewSensorLight.text = getString(R.string.sensor_light,
                        lxToFc(it).roundToInt(), lightUnits)
                }
            } else {
                sharedPref.units = 1
                radiusSeek.max = 80 - radiusSeekMin
                // Change measurements automatically
                tempUnits = getString(R.string.temp_celsius)
                lightUnits = getString(R.string.light_lux)
                prevTemp?.let {
                    textViewSensorTemp.text = getString(R.string.sensor_temp,
                        it.roundToInt(), tempUnits)
                }
                prevLight?.let {
                    textViewSensorLight.text = getString(R.string.sensor_light,
                        it.roundToInt(), lightUnits)
                }
            }

            sharedPref.radiusText = radiusSeekBarText.text.toString()
        }

        // Check radius seek bar
        radiusSeekBarText.text = sharedPref.radiusText
        radiusSeek.progress = sharedPref.radiusBar - radiusSeekMin

        radiusSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                radiusSeekBarText.text = (progress + radiusSeekMin).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                radiusSeekBarText.text = (seekBar.progress + radiusSeekMin).toString()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                radiusSeekBarText.text = (seekBar.progress + radiusSeekMin).toString()
                sharedPref.radiusText = radiusSeekBarText.text.toString()
                sharedPref.radiusBar = radiusSeek.progress + radiusSeekMin
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
        }

        // Check sensors
        senEnable.isChecked = sharedPref.senEnable
        senEnable.text = sharedPref.senText

        senEnable.setOnCheckedChangeListener { _, _ ->
            sharedPref.senEnable = senEnable.isChecked

            if (senEnable.isChecked) {
                senEnable.text = getString(R.string.OnChoice)
                textViewSensorTemp.visibility = View.VISIBLE
                textViewSensorLight.visibility = View.VISIBLE
            } else {
                senEnable.text = getString(R.string.OffChoice)
                textViewSensorTemp.visibility = View.GONE
                textViewSensorLight.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
        // Enable sensors on resume, if available
        if (temp == null) {
            textViewSensorTemp.text = getString(R.string.no_sensor_temp)
        } else {
            sensorManager.registerListener(this, temp, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (light == null) {
            textViewSensorLight.text = getString(R.string.no_sensor_light)
        } else {
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop sensors on pause
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(event: Sensor?, accuracy: Int) {
        // Should be called once when sensors are enabled
        if (event == temp || event == light) {
            when (accuracy) {
                0 -> {
                    println("Unreliable")
                }
                1 -> {
                    println("Low accuracy")
                }
                2 -> {
                    println("Medium accuracy")
                }
                else -> {
                    println("Very accurate")
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                val temp = event.values?.get(0)

                if (prevTemp != null) {
                    val diff = temp?.minus(prevTemp!!)
                    // Check for temperature change when there's at least 2 degrees of change
                    if (diff?.let { abs(it) }!! >= 2) {
                        prevTemp = temp

                        if (radBtnUSA.isChecked) {
                            textViewSensorTemp.text = getString(R.string.sensor_temp,
                                cToF(temp).roundToInt(), tempUnits)
                        } else {
                            textViewSensorTemp.text = getString(R.string.sensor_temp,
                                temp.roundToInt(), tempUnits)
                        }
                    }
                } else {
                    prevTemp = temp
                }
            }
            Sensor.TYPE_LIGHT -> {
                val bright= event.values[0]

                if (prevLight!=null) {
                    val diff2 = bright.minus(prevLight!!)
                    // Check for light if there's at least a 2 lx change
                    if (abs(diff2) >= 2) {
                        prevLight = bright

                        if (radBtnUSA.isChecked) {
                            textViewSensorLight.text = getString(R.string.sensor_light,
                                lxToFc(bright).roundToInt(), lightUnits)
                        } else {
                            textViewSensorLight.text = getString(R.string.sensor_light,
                                bright.roundToInt(), lightUnits)
                        }
                    }
                } else {
                    prevLight = bright
                }
            }
        }
    }
}
