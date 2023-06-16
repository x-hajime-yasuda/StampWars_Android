package jp.co.xpower.app.stw

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import aws.smithy.kotlin.runtime.util.length
import com.amplifyframework.datastore.generated.model.CheckPoint
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import jp.co.xpower.app.stw.databinding.ActivityMainBinding
import jp.co.xpower.app.stw.databinding.CameraResultBinding
import jp.co.xpower.app.stw.databinding.TermsOfServiceBinding
import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

import jp.co.xpower.app.stw.model.*

import kotlinx.serialization.json.*
import java.lang.NullPointerException
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener{
    private lateinit var binding: ActivityMainBinding
    private lateinit var termsBinding: TermsOfServiceBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    private var identityId:String? = null
    private var identityNewId:String = ""
    private lateinit var stwUser:StwUser
    private lateinit var stwCompanys:ArrayList<StwCompany>
    private lateinit var companyList:ArrayList<StwCompany>
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var mGestureDetector : GestureDetector

    private var serverTime:Long = 0L

    private val initLiveData = MutableLiveData<Boolean>()
    //private val markerList: MutableList<Marker> = mutableListOf()
    private val markerList: HashMap<Marker, CheckPoint?> = hashMapOf()
    private var isDataStoreInitialized = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private var currentMarker : Marker? = null
    private var locationFlag : Boolean = false
    private var openedMarker : Marker? = null

    companion object {
        const val  EXTRA_MESSAGE ="jp.co.xpower.app.stw.camera_activity.MESSAGE"
        const val RALLY_STATE_ALL = 0       // すべて
        const val RALLY_STATE_PUBLIC = 1    // 開催中
        const val RALLY_STATE_JOIN = 2      // 参加中
        const val RALLY_STATE_END = 3       // 終了済み

        // 初期MAP座標(東大)
        const val MAP_DEFAULT_LATITUDE = 35.712914101248444
        const val MAP_DEFAULT_LONGITUDE = 139.76234881348526
        const val MAP_ZOOM_LEVEL = 17.0f

        const val PREF_KEY_USER_ID = "identity_id"
        const val PREF_KEY_AGREE = "is_agree"
        const val PREF_KEY_SELECT_CN_ID = "select_cn_id"
        const val PREF_KEY_SELECT_SR_ID = "select_sr_id"

        const val PERMISSION_REQUEST_CODE = 1234
    }

    private fun TextView.changeSizeOfText(target: String, other:String, size: Int){

        // 対象となる文字列を引数に渡し、SpannableStringBuilderのインスタンスを生成
        val spannable = SpannableStringBuilder(target + "/${other}個")

        // Spanを組み込む
        spannable.setSpan(
            AbsoluteSizeSpan(size, true),
            0, // start
            target.length, // end
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // TextViewにSpannableStringBuilderをセット
        text = spannable
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        val m = marker.title

        if(marker.equals(currentMarker)){
            return true
        }

        return false
    }

    // アプリ全体共通ビューモデル
    private val commonDataViewModel by lazy {
        ViewModelProvider(this)[CommonDataViewModel::class.java]
    }

    // データ連携(DB)用ビューモデル
    private val dataStoreViewModel by lazy {
        ViewModelProvider(this)[DataStoreViewModel::class.java]
    }

    // 画像連携用ビューモデル
    private val storageViewModel by lazy {
        ViewModelProvider(this)[StorageViewModel::class.java]
    }

    // Lambda用ビューモデル
    private val functionViewModel by lazy {
        ViewModelProvider(this)[FunctionViewModel::class.java]
    }


    // アプリ全体共通ビューモデルの設定処理
    private fun updateDataViewModel(user:StwUser){
        val commonDataList = ArrayList<CommonData>()
        for (company in companyList) {
            for (rally in company.rallyList) {

                var startAt = 0L
                if(rally.startAt != null){
                    startAt = rally.startAt.secondsSinceEpoch
                }

                var endAt = 0L
                if(rally.endAt != null){
                    endAt = rally.endAt.secondsSinceEpoch
                }

                val common = CommonData()
                common.cnId = company.id
                common.srId = rally.srId
                common.place = rally.place
                common.title = rally.title
                common.detail = rally.detail
                common.rewardTitle = rally.rewardTitle
                common.rewardDetail = rally.rewardDetail
                common.startAt = startAt
                common.endAt = endAt

                val serverTime = commonDataViewModel.serverTime

                common.state = 0 // todo 開催期間で判断
                common.cp = rally.cp as ArrayList<CheckPoint>
                common.joinFlg = false
                common.completeFlg = false

                if(serverTime in startAt..endAt){
                    common.state = RALLY_STATE_PUBLIC    // 開催中
                }
                else {
                    common.state = RALLY_STATE_END    // 終了済み
                }

                // チェックポイント数とユーザーデータのチェックポイント数の一致でラリー達成
                if(user.complete != null){
                    val cpTotal: Int = rally.cp.count()
                    val complete = user.complete.find { it.cnId == company.id && it.srId == rally.srId }
                    if (complete != null) {
                        common.complete = complete
                        common.joinFlg = true
                        if(complete.got != null){
                            common.got = complete.got
                        }

                        if(complete.cp != null){
                            if(cpTotal == complete.cp.length){
                                common.completeFlg = true
                            }
                        }
                    }
                }
                commonDataList.add(common)
            }
        }

        val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        val selectCnId = pref.getString(PREF_KEY_SELECT_CN_ID, "")
        val selectSrId = pref.getString(PREF_KEY_SELECT_SR_ID, "")
        commonDataViewModel.select(selectCnId!!, selectSrId!!)

        commonDataViewModel.identityId = identityId!!

        commonDataViewModel.commonDataList = commonDataList
    }

    fun updateUser(){
        val futureUser = dataStoreViewModel.getUser(identityId!!)
        CompletableFuture.allOf(futureUser).thenRun {
            val user = futureUser.get()
            updateDataViewModel(user[0])
            // メインビュー処理
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                // 選択中ラリーの表示更新
                updateSelected()
            }
        }
    }

    private fun startInitProcess(isAgree:Boolean) {

        val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)

        identityId = pref.getString(PREF_KEY_USER_ID, "")

        // 画像ダウンロード(ラリー・景品)
        val futureRallyImage = storageViewModel.imageDownload(filesDir.absolutePath, StorageViewModel.IMAGE_DIR_RALLY)
        val futureRewardImage = storageViewModel.imageDownload(filesDir.absolutePath, StorageViewModel.IMAGE_DIR_REWARD)
        CompletableFuture.allOf(futureRallyImage, futureRewardImage).thenRun {
        }

        // サーバータイム取得
        val futureFunction = functionViewModel.callFunction(FunctionViewModel.FUNCTION_TIME, "")

        val future = dataStoreViewModel.initDataStore()
        CompletableFuture.allOf(future, futureFunction).thenRun {
            Log.i("STW", "DataStore.initDataStore.")

            val futureCompany = dataStoreViewModel.getCompany()

            val futureUser = dataStoreViewModel.getUser(identityId!!)

            // サーバータイム設定
            val data: String = futureFunction.get()
            val jsonElement = Json.parseToJsonElement(data)
            val getQRDecodeJsonString: String? = jsonElement.jsonObject["getServerTime"]?.jsonPrimitive?.content
            val getQRDecodeElement: JsonElement? = getQRDecodeJsonString?.let { Json.parseToJsonElement(it) }
            val serverTime = getQRDecodeElement!!.jsonObject["data"]!!.jsonPrimitive.long
            commonDataViewModel.serverTime = serverTime

            if(isAgree) {
                CompletableFuture.allOf(futureCompany, futureUser).thenRun {
                    // 会社・ラリー情報
                    val company = futureCompany.get()
                    companyList = company.toList() as ArrayList<StwCompany>

                    // ユーザー情報
                    val user = futureUser.get()
                    updateDataViewModel(user[0])

                    // メインビュー処理
                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
                        mainInitialize()
                    }

                    // ローディング完了
                    initLiveData.postValue(true)
                }
            }
            else {
                CompletableFuture.allOf(futureCompany).thenRun {
                    // 会社・ラリー情報
                    val company = futureCompany.get()
                    companyList = company.toList() as ArrayList<StwCompany>

                    // ローディング完了
                    initLiveData.postValue(true)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        // 初期処理状態
        initLiveData.postValue(false)

        val splashScreen = installSplashScreen()
        // スプラッシュ表示状態監視
        splashScreen.setKeepOnScreenCondition {
            !initLiveData.value!!
        }
        // スプラッシュクローズ待機
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // スプラッシュクローズ
            splashScreenView.remove()
        }

        super.onCreate(savedInstanceState)

        // Camera結果の取得
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if (result.resultCode == RESULT_OK) {
                val text = result.data?.getStringExtra(MainActivity.EXTRA_MESSAGE) ?: ""

                val futureFunction = functionViewModel.callFunction(FunctionViewModel.FUNCTION_QR, text)
                CompletableFuture.allOf(futureFunction).thenRun {
                    val data: String = futureFunction.get()
                    val jsonElement = Json.parseToJsonElement(data)
                    val getQRDecodeJsonString: String? = jsonElement.jsonObject["getQRDecode"]?.jsonPrimitive?.content
                    val getQRDecodeElement: JsonElement? = getQRDecodeJsonString?.let { Json.parseToJsonElement(it) }
                    val dec = getQRDecodeElement!!.jsonObject["dec"]!!.jsonPrimitive.content
                    val statusCode = getQRDecodeElement!!.jsonObject["statusCode"]

                    var message = ""
                    if(statusCode!!.jsonPrimitive.int == 200){
                        val cnId = commonDataViewModel.selectCnId
                        val srId = commonDataViewModel.selectSrId
                        val qrCnId = dec.split("_")[0]
                        val qrSrId = dec.split("_")[1]
                        val qrCpId = dec.split("_")[2]

                        if("${cnId}_${srId}" == "${qrCnId}_${qrSrId}"){
                            var cd:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == cnId && it.srId == srId }

                            var checkPoint:CheckPoint? = cd!!.complete!!.cp.find{it.cpId == qrCpId}
                            // 達成済み
                            if(checkPoint != null){
                                message = resources.getText(R.string.stamp_camera_qr_already_point).toString()
                                showAlertDialog(message)
                            }
                            else {
                                // 達成処理開始
                                val futureStamp = dataStoreViewModel.rallyStamping(commonDataViewModel, qrCpId)
                                CompletableFuture.allOf(futureStamp).thenRun {
                                    // 選択中ラリーの表示更新
                                    val mainHandler = Handler(Looper.getMainLooper())
                                    mainHandler.post {
                                        // 選択中ラリーの表示更新
                                        // 達成したらcommonDataListに選択CommonDataのチェックポイントを追加する
                                        var cb:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == cnId && it.srId == srId }
                                        val point:CheckPoint = CheckPoint.builder().cpId(qrCpId).build()
                                        if(cb != null && cb!!.complete != null){
                                            cb!!.complete!!.cp.add(point)
                                        }
                                        // 画面更新
                                        updateSelected()

                                        message = resources.getText(R.string.stamp_camera_qr_get).toString()
                                        showAlertDialog(message)
                                    }
                                }
                            }
                        }
                        else {
                            message = resources.getText(R.string.stamp_camera_qr_wrong).toString()
                            showAlertDialog(message)
                        }
                    } else {
                        message = resources.getText(R.string.stamp_camera_qr_invalid).toString()
                        showAlertDialog(message)
                    }
                }
            }
        }

        // 初回起動時は規約ページを表示する
        val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        val isAgree = pref.getBoolean(PREF_KEY_AGREE, false)

        // 初期データ取得
        startInitProcess(isAgree)


        // 初回起動規約同意前
        if(!isAgree){
            // 規約同意レイアウト表示
            termsBinding = TermsOfServiceBinding.inflate(layoutInflater)
            setContentView(termsBinding.root)
            supportActionBar?.hide()
            termsBinding.button.setOnClickListener {
                // 認証開始
                val futureAuth = dataStoreViewModel.fetchAuth()
                CompletableFuture.allOf(futureAuth).thenRun {
                    identityId = futureAuth.get()
                    val futureCreate = dataStoreViewModel.createUser(identityId!!, "名前未設定")
                    CompletableFuture.allOf(futureCreate).thenRun {
                        val user = futureCreate.get()
                        // View更新
                        updateDataViewModel(user)

                        val mainHandler = Handler(Looper.getMainLooper())
                        mainHandler.post {
                            pref.edit().putString(PREF_KEY_USER_ID, identityId).apply()
                            pref.edit().putBoolean(PREF_KEY_AGREE, true).apply()
                            termsBinding.root.visibility = View.GONE

                            // メインビュー処理
                            mainInitialize()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(this::fusedLocationClient.isInitialized){
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    /*
    * ポイント達成時のアラートダイアログ表示
    * message: アラートメッセージ
    */
    private fun showAlertDialog(message: String){
        val cameraResultBinding = CameraResultBinding.inflate(layoutInflater)
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            cameraResultBinding.textView.text = message
            // QA読込アラートダイアログの表示
            val builder = AlertDialog.Builder(this)
            builder.setView(cameraResultBinding.root)
            val dialog = builder.create()
            dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            cameraResultBinding.positiveButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun getSelectId() :Pair<String, String>{
        val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        val selectCnId = pref.getString(PREF_KEY_SELECT_CN_ID, "")
        val selectSrId = pref.getString(PREF_KEY_SELECT_SR_ID, "")
        return Pair(selectCnId!!, selectSrId!!)
    }

    fun isRewardEnabled() {
        val selected = getSelectId()
        var cd: CommonData? = commonDataViewModel.commonDataList.find { it.cnId == selected.first && it.srId == selected.second }
        if(cd == null){
            binding.layoutStamp.buttonReward.setBackgroundResource(R.drawable.button_gray)
            binding.layoutStamp.buttonReward.isEnabled = false
            return
        }

        if(cd!!.completeFlg){
            binding.layoutStamp.buttonReward.setBackgroundResource(R.drawable.button_ripple)
            binding.layoutStamp.buttonReward.isEnabled = true
        }
        else {
            binding.layoutStamp.buttonReward.setBackgroundResource(R.drawable.button_gray)
            binding.layoutStamp.buttonReward.isEnabled = false
        }
    }


    fun isRewardReceived() :Boolean {
        val selected = getSelectId()
        var cd:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == selected.first && it.srId == selected.second }
        // 未参加or未選択
        if(cd == null){
            return false
        }
        return cd!!.got
    }

    fun updateSelected(){
        val selected = getSelectId()
        var cd:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == selected.first && it.srId == selected.second }
        if(cd != null){
            binding.layoutSelected.textTitle.text = cd.title

            var completeCount:Int = 0

            // 獲得スタンプ一覧作成
            var list = mutableListOf<Stamp>()
            for( cp in cd.cp){

                var icon:Int = -1

                if(cd.complete != null && cd.complete!!.cp != null) {
                    completeCount = cd.complete!!.cp.count()

                    var checkPoint:CheckPoint? = cd.complete!!.cp.find{it.cpId == cp.cpId}
                    if(checkPoint != null){
                        icon = R.drawable.stamp_icon
                    }
                }

                var stamp = Stamp(
                    icon,
                    cp?.cpName
                )
                list.add(stamp)
            }
            binding.layoutStamp.recyclerView.apply {
                layoutManager = GridLayoutManager(context, 4)
                adapter = StampAdapter(list)
            }

            // MAP更新
            updateMap()

            // 獲得数だけ強調表示
            binding.layoutStamp.textGet.changeSizeOfText(completeCount.toString(), cd.cp.count().toString(),38)

            binding.tvStampCount.text = "${completeCount}/${cd.cp.count()}"
        }
    }

    private fun updateMap() {
        if (::googleMap.isInitialized) {

            // マーカー削除
            for(marker in markerList.keys){
                marker.remove()
            }
            markerList.clear()

            val selected = getSelectId()
            var cd:CommonData? = commonDataViewModel.commonDataList.find { res -> res.cnId == selected.first && res.srId == selected.second }
            if(cd != null){
                var latitudeList:ArrayList<Double> = ArrayList<Double>()
                var longitudeList:ArrayList<Double> = ArrayList<Double>()

                for(checkpoint in cd!!.cp){
                    val latLng = LatLng(checkpoint.latitude.toDouble(), checkpoint.longitude.toDouble())
                    val m = googleMap.addMarker(MarkerOptions().position(latLng).title(checkpoint.cpName))
                    latitudeList.add(checkpoint.latitude.toDouble())
                    longitudeList.add(checkpoint.longitude.toDouble())
                    markerList.set(m!!, checkpoint!!)
                    //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
                }
                // チェックポイント群のMAP中央表示
                val latLng = LatLng(( latitudeList.min() + latitudeList.max() ) / 2,  (longitudeList.min() + longitudeList.max() ) / 2)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))

            }
            // 未選択(初期表示位置)
            else {
                val latLng = LatLng(MAP_DEFAULT_LATITUDE, MAP_DEFAULT_LONGITUDE)
                val m = googleMap.addMarker(MarkerOptions().position(latLng).title("東京大学"))
                markerList.set(m!!, null)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
            }
        }
        else{
            // マップが初期化されるまで待機する
            Handler(Looper.getMainLooper()).postDelayed({
                updateMap()
            }, 500)
        }
    }

    private fun mainInitialize(){
        // メインレイアウト設定
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // タイトルバー非表示
        supportActionBar?.hide()

        // Map表示 Mapテスト時以外非活性
        //*
        mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync {
            Log.i("STW", "GoogleMap ready.")
            //mapViewModel.initializeMap(it)

            googleMap = it
            googleMap.setOnMarkerClickListener(this)

            // できるだけ早くMAP位置を設定しないと世界地図が表示されてしまう。
            val latLng = LatLng(MAP_DEFAULT_LATITUDE, MAP_DEFAULT_LONGITUDE)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))

            // info windowの設定
            googleMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
                override fun getInfoContents(marker: Marker): View? {
                    val view = layoutInflater.inflate(R.layout.marker_info_window, null)
                    val latLng = marker.position
                    val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                    val tvMarkerTitle = view.findViewById<TextView>(R.id.tvMarkerTitle)

                    val normalLayout = view.findViewById<ConstraintLayout>(R.id.normalLayout)
                    val getableLayout = view.findViewById<ConstraintLayout>(R.id.getableLayout)

                    tvTitle.setText(marker.title)
                    //tvMarkerTitle.setText(marker.title)

                    // すべてのレイアウトを非表示に設定
                    for(view in view.findViewById<ConstraintLayout>(R.id.infoCL).children){
                        view.visibility = View.GONE
                    }

                    // 表示するレイアウトのみをVISIBLEに設定
                    if(isGetable(latLng.latitude, latLng.longitude)){
                        getableLayout.visibility = View.VISIBLE
                    } else {
                        normalLayout.visibility = View.VISIBLE

                    }

                    return view
                }

                override fun getInfoWindow(marker: Marker): View? {
                    return null
                }
            })

            googleMap.setOnInfoWindowClickListener {
                if(isGetable(it.position.latitude, it.position.longitude)){
                    val cnId = commonDataViewModel.selectCnId
                    val srId = commonDataViewModel.selectSrId
                    val cpId = markerList.get(it)!!.cpId

                    val cd:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == cnId && it.srId == srId }
                    val checkPoint:CheckPoint? = cd!!.complete!!.cp.find{it.cpId == cpId}

                    // 達成済み
                    if(checkPoint != null){
                        val message = resources.getText(R.string.stamp_camera_qr_already_point).toString()
                        showAlertDialog(message)
                    }
                    else{
                        val futureStamp = dataStoreViewModel.rallyStamping(commonDataViewModel, cpId)
                        CompletableFuture.allOf(futureStamp).thenRun {
                            val mainHandler = Handler(Looper.getMainLooper())
                            mainHandler.post {
                                // 選択中ラリーの表示更新
                                // 達成したらcommonDataListに選択CommonDataのチェックポイントを追加する
                                val cb: CommonData? = commonDataViewModel.commonDataList.find { it.cnId == cnId && it.srId == srId }
                                val point: CheckPoint = CheckPoint.builder().cpId(cpId).build()
                                if (cb != null && cb!!.complete != null) {
                                    cb!!.complete!!.cp.add(point)
                                }
                                // 画面更新
                                updateSelected()

                                val message = resources.getText(R.string.stamp_camera_qr_get).toString()
                                showAlertDialog(message)
                            }
                        }
                    }
                }
            }

            /*
            // 初回の位置設定
            val selected = getSelectId()
            var cd:CommonData? = commonDataViewModel.commonDataList.find { res -> res.cnId == selected.first && res.srId == selected.second }

            markerList.clear()

            if(cd != null){
                //mapViewModel.setCheckPoints(cd.cp, cd.complete)
                for(checkpoint in cd.cp){
                    val latLng = LatLng(checkpoint.latitude.toDouble(), checkpoint.longitude.toDouble())
                    val m = googleMap.addMarker(MarkerOptions().position(latLng).title(checkpoint.cpName))
                    markerList.add(m!!)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
                }
            }
            // 未選択(初期表示位置)
            else {
                val latLng = LatLng(MAP_DEFAULT_LATITUDE, MAP_DEFAULT_LONGITUDE)
                val m = googleMap.addMarker(MarkerOptions().position(latLng).title("東京大学"))
                markerList.add(m!!)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
            }
            */
        }

        // 選択中ラリーの表示更新
        updateSelected()

        // スタンプ表示切り替え
        binding.btStampList.setOnClickListener(OnButtonClick())

        // 報酬獲得画面の表示
        binding.layoutStamp.buttonReward.setOnClickListener(OnButtonClick())

        // 報酬獲得画面・受取完了画面を閉じてスタンプカード画面を表示
        binding.layoutReward.btBackStampList.setOnClickListener(OnButtonClick())
        binding.layoutReceived.btBackStampList.setOnClickListener(OnButtonClick())

        // bgLayoutの設定
        binding.bgLayout.setOnClickListener(null)

        // スタンプカード表示ボタンを最前面に配置
        binding.rlStampCard.bringToFront()

        // 報酬受け取り完了画面の表示
        binding.layoutReward.swReceiveReward.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                val selected = getSelectId()
                val completableFuture = dataStoreViewModel.updateRewardAsyncTask(identityId!!, selected.first, selected.second, true)
                CompletableFuture.allOf(completableFuture).thenRun {

                    // ViewModel更新
                    var cb:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == selected.first && it.srId == selected.second }
                    cb!!.got = true

                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
                        binding.layoutReward.layout.visibility = View.GONE
                        binding.layoutReceived.layout.visibility = View.VISIBLE
                        binding.layoutReward.swReceiveReward.isClickable = false
                    }
                }
            }
        }
        mGestureDetector = GestureDetector(this@MainActivity, MGestureListener())
        binding.layoutReward.swReceiveReward.setOnTouchListener(SwTouchListener())

        // スタンプカード・報酬受取画面・受取完了画面の閉じるボタンの設定
        binding.layoutStamp.buttonClose.setOnClickListener(OnButtonClick())
        binding.layoutReward.buttonClose.setOnClickListener(OnButtonClick())
        binding.layoutReceived.buttonClose.setOnClickListener(OnButtonClick())

        // ボトムメニュー ボタンイベント
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    // ボトムシート表示
                    BottomSheetFragment.newInstance(companyList).show(supportFragmentManager, "dialog")

                    true
                }
                R.id.menu_profile -> {
                    val intent = Intent(this, CameraActivity::class.java)
                    cameraLauncher.launch(intent)

                    true
                }

                /*
                R.id.menu_profile2 -> {
                    // クリアテスト
                    Amplify.DataStore.clear(
                        { Log.i("MyAmplifyApp", "DataStore is cleared") },
                        { Log.e("MyAmplifyApp", "Failed to clear DataStore") }
                    )
                    true
                }
                */

                else -> false
            }
        }

        // ユーザ設定画面ボタン（仮）の設定
        binding.openUserSetting.setOnClickListener {
            val intent2UserSetting = Intent(this@MainActivity, UserSettingActivity::class.java)
            startActivity(intent2UserSetting)
        }

        // 現在地情報取得のための設定
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){
                    val current = LatLng(location.latitude, location.longitude)
                    currentMarker?.remove()
                    currentMarker = googleMap?.addMarker(
                        MarkerOptions()
                            .position(current)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location))
                            .zIndex(-1.0f)
                    )
                    currentLocation = location
                    showGetablePoint()
                }
            }
        }
        startLocationUpdates()

        // 現在地に移動するボタンの設定
        binding.moveCurrentLocation.setOnClickListener {
            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
        }
    }
    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showGetablePoint(){
        var flag = true
        for(marker in markerList.keys){
            if(isGetable(marker)){
                flag = false

                if(!locationFlag){
                    marker.showInfoWindow()
                    openedMarker = marker
                    locationFlag = true
                }
                break
            }
        }
        if(flag){
            openedMarker?.hideInfoWindow()
            locationFlag = false
        }
    }

    private fun isGetable(latitude: Double, longitude: Double) : Boolean {
        var results = FloatArray(3)

        // currentLocationが初期化されていなければfalse
        if(!this::currentLocation.isInitialized){
            return false
        }

        Location.distanceBetween(latitude, longitude, currentLocation.latitude, currentLocation.longitude, results)

        return results.get(0) < 5
    }

    private fun isGetable(marker: Marker): Boolean{
        return isGetable(marker.position.latitude, marker.position.longitude)
    }

    private fun startLocationUpdates() {
        val locationRequest = createLocationRequest() ?: return

        checkPermission()
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private inner class SwTouchListener : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            mGestureDetector.onTouchEvent(event)
            return false
        }
    }

    private inner class MGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val backgroundReceiver = Runnable {
                Thread.sleep(1000)
                binding.layoutReward.swReceiveReward.isClickable = true
            }
            val executeService = Executors.newSingleThreadExecutor()

            binding.layoutReward.swReceiveReward.isClickable = false
            executeService.submit(backgroundReceiver)
            return true
        }
    }

    private inner class OnButtonClick : View.OnClickListener{
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
                        val received = isRewardReceived()
                        if(received){
                            binding.layoutStamp.buttonReward.text = resources.getText(R.string.stamp_got_reward)
                            binding.layoutStamp.buttonReward.setBackgroundResource(R.drawable.button_gray)
                            binding.layoutStamp.buttonReward.isEnabled = false
                        }
                        else {
                            binding.layoutReward.swReceiveReward.isChecked = false
                            binding.layoutReward.swReceiveReward.isClickable = true
                            binding.layoutStamp.buttonReward.text = resources.getText(R.string.stamp_get_reward)
                            // スタンプラリー達成していれば獲得ボタン有効
                            isRewardEnabled()
                        }

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
                    // 獲得済み判定
                    val received = isRewardReceived()
                    if(received){
                        binding.layoutStamp.buttonReward.text = resources.getText(R.string.stamp_got_reward)
                        binding.layoutStamp.buttonReward.setBackgroundResource(R.drawable.button_gray)
                        binding.layoutStamp.buttonReward.isEnabled = false
                    }
                    else {
                        binding.layoutStamp.buttonReward.text = resources.getText(R.string.stamp_get_reward)
                        binding.layoutStamp.buttonReward.setBackgroundResource(R.drawable.button_ripple)
                        binding.layoutStamp.buttonReward.isEnabled = true
                    }
                }

                // 報酬獲得画面を表示
                R.id.button_reward -> {
                    btStampList.isClickable = false
                    stampLayout.visibility = View.GONE

                    val received = isRewardReceived()
                    if(received){
                    //if(!binding.layoutReward.swReceiveReward.isChecked){
                        // 報酬受取済の場合は受取完了画面を表示
                        receivedLayout.visibility = View.VISIBLE
                    }
                    else {
                        // 報酬未受取の場合は受取画面を表示
                        rewardLayout.visibility = View.VISIBLE
                    }
                }

                //閉じるボタン
                R.id.button_close -> {
                    rewardLayout.visibility = View.GONE
                    receivedLayout.visibility = View.GONE
                    stampLayout.visibility = View.GONE
                    bgLayout.visibility = View.GONE
                    btStampList.isClickable = true
                }
            }
        }
    }
}
