package com.example.stw_b1

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stw_b1.databinding.ActivityMainBinding
import com.example.stw_b1.databinding.TermsOfServiceBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var termsBinding: TermsOfServiceBinding
    //private lateinit var stampBinding: StampListBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private val LOCATION_PERMISSION_REQUEST_CODE:Int = 1

    private lateinit var bottomSheet :BottomSheetFragment

    private fun populateStamp(){
        var stamp1 = Stamp(
            R.drawable.sumi1,
            "校舎の屋上の奥の奥に",
            "開催日未定"
        )
        stampList.add(stamp1)

        var stamp2 = Stamp(
            R.drawable.sumi1,
            "校舎裏の裏",
            "校舎裏の裏"
        )
        stampList.add(stamp2)

        var stamp3 = Stamp(
            R.drawable.sumi1,
            "体躯倉庫のだいぶ奥の方",
            "体躯倉庫のだいぶ奥の方"
        )
        stampList.add(stamp3)

        var stamp4 = Stamp(
            R.drawable.sumi1,
            "運動場の真ん中",
            "体躯倉庫のだいぶ奥の方"
        )
        stampList.add(stamp4)

        var stamp5 = Stamp(
            R.drawable.sumi1,
            "校門の前",
            "体躯倉庫のだいぶ奥の方"
        )
        stampList.add(stamp5)

        var stamp6 = Stamp(
            R.drawable.sumi1,
            "校長室の金庫の中",
            "体躯倉庫のだいぶ奥の方"
        )
        stampList.add(stamp6)

        var stamp7 = Stamp(
            R.drawable.sumi1,
            "職員室のとてもわかりにくい場所に置きました。",
            "体躯倉庫のだいぶ奥の方"
        )
        stampList.add(stamp7)
    }


    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("位置情報の使用許可が必要です")
            .setMessage("このアプリでは、位置情報を使用します。位置情報の使用許可が必要です。ご使用される場合は設定から許可をお願いします。")
            .setPositiveButton("OK") { _, _ ->
                Log.i("MainActivity old ======>", "許可する")
                //requestLocationPermission()
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            }
            .show()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            }
            else {
                // 再表示しない拒否中
                showRationaleDialog()
            }
        }
        else {
            Log.i("MainActivity old ======>", "許可中")
        }
    }

    //override fun onClick(stamp: Stamp) {
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        val isTos = pref.getBoolean("IS_TOS", false)
        // 初回起動規約同意前
        if(!isTos){
            termsBinding = TermsOfServiceBinding.inflate(layoutInflater)
            setContentView(termsBinding.root)
            supportActionBar?.hide()
            termsBinding.button.setOnClickListener {
                Log.i("MainActivity Map ======>", "ready")
                pref.edit().putBoolean("IS_TOS", true).apply()
                val intent = Intent(this, MapActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            return
        }

        //stampBinding = StampListBinding.inflate(layoutInflater)


        //button_stamp





        /*
        // 規約同意後
        else {
            val intent = Intent(this, MapActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            overridePendingTransition(0, 0);
        }
        */


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // タイトルバー非表示
        supportActionBar?.hide()

        populateStamp()

        binding.layoutStamp.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = StampAdapter(stampList)
        }

        binding.buttonStamp.setOnClickListener {
            val layout = binding.layoutStamp
            if(layout.layout.visibility == View.VISIBLE){
                layout.layout.visibility = View.GONE
            }
            else {
                layout.layout.visibility = View.VISIBLE
            }
        }


        bottomSheet = BottomSheetFragment()





        /*
        mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync {
            Log.i("MainActivity Map ======>", "map.ready")
            googleMap = it

            val tokyo = LatLng(35.681167, 139.767052)
            googleMap.addMarker(MarkerOptions().position(tokyo).title("東京駅"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 15f))
        }
        */



        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    //val bottomSheetFragment = BottomSheetFragment()
                    bottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
                    true
                }
                R.id.menu_profile -> {
                    //checkPermission()
                    val intent = Intent(this, CameraActivity::class.java)
                    startActivity(intent)

                    true
                }
                //R.id.menu_settings -> {
                //    true
                //}
                else -> false
            }
        }



    }
}