package com.bignerdranch.android.jeremybui_springbreak

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private val speechRequestCode = 0
    private lateinit var languageSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val languages = listOf("Spanish", "French", "Chinese") // Extend this list as needed
        languageSpinner = findViewById(R.id.languageSpinner)
        val editTextPhrase: EditText = findViewById(R.id.editTextPhrase)
        val buttonSpeak: Button = findViewById(R.id.buttonSpeak)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        languageSpinner.adapter = adapter

        buttonSpeak.setOnClickListener {
            val selectedLanguage = languageSpinner.selectedItem.toString()
            promptSpeechInput(selectedLanguage)
        }
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
            val editTextPhrase: EditText = findViewById(R.id.editTextPhrase)
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                editTextPhrase.setText(matches[0])
            } else {
                editTextPhrase.setText("")
            }
        }
    }
}
