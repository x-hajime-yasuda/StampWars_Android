package jp.co.xpower.app.stw

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import jp.co.xpower.app.stw.databinding.ActivityMainBinding
import jp.co.xpower.app.stw.databinding.TermsOfServiceBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var termsBinding: TermsOfServiceBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    // テストデータ作成
    private fun populateStamp() {
        for (i in 1..8) {
            var stamp = Stamp(
                R.drawable.sumi1,
                "校舎の屋上の奥の奥に (%d)".format(i)
            )
            stampList.add(stamp)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初回起動時は規約ページを表示する
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
                val intent = Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // タイトルバー非表示
        supportActionBar?.hide()

        // Map表示 Mapテスト時以外非活性
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

        // スタンプ表示切り替え
        binding.buttonStamp.setOnClickListener {
            val layout = binding.layoutStamp
            if(layout.layout.visibility == View.VISIBLE){
                layout.layout.visibility = View.GONE
            }
            else {
                layout.layout.visibility = View.VISIBLE
            }
        }

        // テストデータ設定
        populateStamp()
        // スタンプデータ設定
        binding.layoutStamp.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = StampAdapter(stampList)
        }

        // ボトムメニュー ボタンイベント
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    // ボトムシート表示
                    BottomSheetFragment.newInstance(0).show(supportFragmentManager, "dialog")

                    true
                }
                R.id.menu_profile -> {
                    val intent = Intent(this, CameraActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_gift -> {
                    // 報酬表示
                    BottomSheetFragment.newInstance(3).show(supportFragmentManager, "dialog")
                    true
                }
                else -> false
            }
        }
    }
}
