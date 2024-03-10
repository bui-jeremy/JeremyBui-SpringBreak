package com.bignerdranch.android.jeremybui_springbreak

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val geoLocation = intent.getStringExtra("GEO_LOCATION")
        val locationName = intent.getStringExtra("LOCATION_NAME") ?: "Vacation Spot"
        val language = intent.getStringExtra("LANGUAGE")

        geoLocation?.let {
            val latLng = it.split(",").let { parts -> LatLng(parts[0].toDouble(), parts[1].toDouble()) }
            mMap.addMarker(MarkerOptions().position(latLng).title(locationName))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
        }

        playGreetingForLanguage(language)
    }

    private fun playGreetingForLanguage(language: String?) {
        val greetingResId = when (language) {
            "Spanish" -> R.raw.hello_spanish
            "French" -> R.raw.hello_french
            "Chinese" -> R.raw.hello_chinese
            else -> null
        }

        greetingResId?.let {
            mediaPlayer = MediaPlayer.create(this, it).apply {
                start()
                setOnCompletionListener { mp -> mp.release() }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
