package com.example.stw_b1

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.stw_b1.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync {
            Log.i("MainActivity Map ======>", "map.ready")
            googleMap = it
            val tokyo = LatLng(35.681167, 139.767052)
            googleMap.addMarker(MarkerOptions().position(tokyo).title("東京駅"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 15f))
        }

    }


}