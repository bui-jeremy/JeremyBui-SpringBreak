package com.bignerdranch.android.jeremybui_springbreak

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val speechRequestCode = 0
    private lateinit var languageSpinner: Spinner
    private var selectedLanguage = ""
    private lateinit var sensorManager: SensorManager
    private var shakeSensor: Sensor? = null
    private val shakeThreshold = 12f
    private var lastUpdate: Long = 0
    private var last_x: Float = 0.0f
    private var last_y: Float = 0.0f
    private var last_z: Float = 0.0f
    private val vacationSpots = mapOf(
        "Spanish" to listOf(
            "Madrid, Spain" to "40.416775,-3.703790",
            "Barcelona, Spain" to "41.3851,2.1734",
            "Mexico City, Mexico" to "19.4326,-99.1332"
        ),
        "French" to listOf(
            "Paris, France" to "48.8566,2.3522",
            "Nice, France" to "43.7102,7.2620",
            "Montreal, Canada" to "45.5017,-73.5673"
        ),
        "Chinese" to listOf(
            "Beijing, China" to "39.9042,116.4074",
            "Shanghai, China" to "31.2304,121.4737",
            "Hong Kong" to "22.3193,114.1694"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val languages = listOf("Spanish", "French", "Chinese")
        languageSpinner = findViewById(R.id.languageSpinner)
        val editTextPhrase: EditText = findViewById(R.id.editTextPhrase)
        val buttonSpeak: Button = findViewById(R.id.buttonSpeak)

        languageSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, languages
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        buttonSpeak.setOnClickListener {
            selectedLanguage = languageSpinner.selectedItem.toString()
            promptSpeechInput(selectedLanguage)
        }

       initializeSensors()
    }

    private fun initializeSensors(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    private fun promptSpeechInput(language: String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something in $language...")
        }
        try {
            startActivityForResult(intent, speechRequestCode)
        } catch (a: Exception) {
            Toast.makeText(this, "Speech input is not available.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == speechRequestCode && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val editTextPhrase: EditText = findViewById(R.id.editTextPhrase)
            editTextPhrase.setText(matches?.get(0) ?: "")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        val mySensor = event.sensor

        if (mySensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val curTime = System.currentTimeMillis()

            if ((curTime - lastUpdate) > 100) {
                val diffTime = curTime - lastUpdate
                lastUpdate = curTime

                val speed = sqrt((x - last_x).toDouble().pow(2.0) + (y - last_y).toDouble().pow(2.0) + (z - last_z).toDouble().pow(2.0)) / diffTime * 10000

                if (speed > shakeThreshold) {
                    openMapForLanguage(selectedLanguage)
                }

                last_x = x
                last_y = y
                last_z = z
            }
        }
    }

    private fun openMapForLanguage(language: String) {
        val locations = vacationSpots[language] ?: return
        val (locationName, geoLocation) = locations[Random.nextInt(locations.size)]

        val intent = Intent(this, MapsActivity::class.java).apply {
            putExtra("GEO_LOCATION", geoLocation)
            putExtra("LOCATION_NAME", locationName)
            putExtra("LANGUAGE", language)
        }
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
