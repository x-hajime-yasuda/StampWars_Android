package jp.co.xpower.app.stw

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import jp.co.xpower.app.stw.databinding.ActivityMainBinding
import jp.co.xpower.app.stw.databinding.RallyCellBinding
import jp.co.xpower.app.stw.databinding.TermsOfServiceBinding

class MainActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var termsBinding: TermsOfServiceBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    companion object {
        const val  EXTRA_MESSAGE ="jp.co.xpower.app.stw.camera_activity.MESSAGE"
    }

    // テストデータ作成
    private fun populateStamp() {
        for (i in 1..21) {
            var stamp = Stamp(
                R.drawable.sumi1,
                "校舎の屋上の奥の奥に (%d)".format(i)
            )
            stampList.add(stamp)
        }
    }

    private fun TextView.changeSizeOfText(target: String, size: Int){

        // 対象となる文字列を引数に渡し、SpannableStringBuilderのインスタンスを生成
        val spannable = SpannableStringBuilder(target + "/99個")

        // Spanを組み込む
        spannable.setSpan(
            AbsoluteSizeSpan(size, true),
            0, // start
            //target.length, // end
            1, // end
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // TextViewにSpannableStringBuilderをセット
        text = spannable
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        return false
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
            googleMap.setOnMarkerClickListener(this)

            val tokyo = LatLng(35.681167, 139.767052)
            googleMap.addMarker(MarkerOptions().position(tokyo).title("東京駅"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 15f))
        }
        */

        // Camera結果の取得
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if (result.resultCode == RESULT_OK) {
                val text = result.data?.getStringExtra(MainActivity.EXTRA_MESSAGE) ?: ""
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

                binding.layoutStamp.layout.visibility = View.VISIBLE

                val builder = AlertDialog.Builder(this)
                builder.setView(layoutInflater.inflate(R.layout.camera_result, null))
                val dialog = builder.create()
                dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
            }
        }

        // スタンプ表示切り替え
        binding.btStampList.setOnClickListener(onButtonClick())
        /*
        binding.btStampList.setOnClickListener {
            val layout = binding.layoutStamp
            if(layout.layout.visibility == View.VISIBLE){
                layout.layout.visibility = View.GONE
            }
            else {
                layout.layout.visibility = View.VISIBLE
            }
        }
         */

        // テストデータ設定
        populateStamp()
        // スタンプデータ設定
        binding.layoutStamp.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = StampAdapter(stampList)
        }

        // 報酬獲得画面の表示
        binding.layoutStamp.buttonReward.setOnClickListener(onButtonClick())

        // 報酬獲得画面・受取完了画面を閉じてスタンプカード画面を表示
        binding.layoutReward.btBackStampList.setOnClickListener(onButtonClick())
        binding.layoutReceived.btBackStampList.setOnClickListener(onButtonClick())

        // bgLayoutの設定
        //binding.bgLayout.bringToFront()
        binding.bgLayout.setOnClickListener(null)

        // スタンプカード表示ボタンを最前面に配置
        binding.rlStampCard.bringToFront()

        // 報酬受け取り完了画面の表示
        binding.layoutReward.swReceiveReward.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                binding.layoutReward.layout.visibility = View.GONE
                binding.layoutReceived.layout.visibility = View.VISIBLE
            }
        }


        // 獲得数だけ強調表示
        binding.layoutStamp.textGet.changeSizeOfText("3", 38)


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
                    //startActivity(intent)
                    cameraLauncher.launch(intent)

                    true
                }
                else -> false
            }
        }
    }

    private inner class onButtonClick : View.OnClickListener{
        val rewardLayout = binding.layoutReward.layout
        val stampLayout = binding.layoutStamp.layout
        val receivedLayout = binding.layoutReceived.layout
        val bgLayout = binding.bgLayout
        val btStampList = binding.btStampList
        override fun onClick(v: View) {
            when(v.id){
                // スタンプカード表示切り替え
                R.id.btStampList -> {
                    if(stampLayout.visibility == View.VISIBLE){
                        stampLayout.visibility = View.GONE
                        bgLayout.visibility = View.GONE
                    }
                    else {
                        stampLayout.visibility = View.VISIBLE
                        bgLayout.visibility = View.VISIBLE
                    }
                }

                // スタンプカードに戻る
                R.id.btBackStampList -> {
                    rewardLayout.visibility = View.GONE
                    receivedLayout.visibility = View.GONE
                    stampLayout.visibility = View.VISIBLE
                    btStampList.isClickable = true
                }

                // 報酬獲得画面を表示
                R.id.button_reward -> {
                    rewardLayout.visibility = View.VISIBLE
                    stampLayout.visibility = View.GONE
                    btStampList.isClickable = false
                }
            }
        }

    }
}
