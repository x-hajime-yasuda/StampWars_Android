package jp.co.xpower.cotamp

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import aws.smithy.kotlin.runtime.util.length
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.CheckPoint
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import jp.co.xpower.cotamp.databinding.ActivityMainBinding
import jp.co.xpower.cotamp.databinding.CameraResultBinding
import jp.co.xpower.cotamp.databinding.TermsOfServiceBinding
import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

import jp.co.xpower.cotamp.model.*
import jp.co.xpower.cotamp.R
import jp.co.xpower.cotamp.databinding.FragmentCustomSplashScreenBinding

import kotlinx.serialization.json.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var termsBinding: TermsOfServiceBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    private lateinit var splashBinding: FragmentCustomSplashScreenBinding


    private var identityId:String? = null
    private var identityNewId:String = ""
    private lateinit var stwUser:StwUser
    private lateinit var stwCompanys:ArrayList<StwCompany>
    private lateinit var companyList:ArrayList<StwCompany>
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var mGestureDetector : GestureDetector

    private var serverTime:Long = 0L

    private val initLiveData = MutableLiveData<Boolean>()
    private val markerList: MutableList<Marker> = mutableListOf()

    private var isDataStoreInitialized = false


    companion object {
        const val  EXTRA_MESSAGE ="jp.co.xpower.app.stw.camera_activity.MESSAGE"
        const val RALLY_STATE_ALL = 0       // すべて
        const val RALLY_STATE_PUBLIC = 1    // 開催中
        const val RALLY_STATE_JOIN = 2      // 参加中
        const val RALLY_STATE_END = 3       // 終了済み
        const val RALLY_STATE_PRIVATE = 4    // 開催期間外

        // 初期MAP座標(東大)
        const val MAP_DEFAULT_LATITUDE = 35.712914101248444
        const val MAP_DEFAULT_LONGITUDE = 139.76234881348526
        const val MAP_ZOOM_LEVEL = 17.0f

        const val PREF_KEY_USER_ID = "identity_id"
        const val PREF_KEY_AGREE = "is_agree"
        const val PREF_KEY_SELECT_CN_ID = "select_cn_id"
        const val PREF_KEY_SELECT_SR_ID = "select_sr_id"
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
            if(company.rallyList == null){
                continue
            }
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
                else if(serverTime < startAt ){
                    common.state = RALLY_STATE_PRIVATE    // 開催期間外
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
        val futureFunction = functionViewModel.callFunctionRetry(FunctionViewModel.FUNCTION_TIME, "")

        val future = dataStoreViewModel.initDataStore()
        CompletableFuture.allOf(future, futureFunction)
            .handle { _, exception ->
                if (exception != null) {
                    Log.i("STW", "ex.")
                }
            }
            .thenRun {
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

            // 2度目以降の起動
            if(isAgree) {
                CompletableFuture.allOf(futureCompany, futureUser).thenRun {
                    // 会社・ラリー情報
                    val company = futureCompany.get()
                    companyList = company as ArrayList<StwCompany>

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
            // 初回起動
            else {
                CompletableFuture.allOf(futureCompany).thenRun {
                    // 会社・ラリー情報
                    val company = futureCompany.get()
                    companyList = company as ArrayList<StwCompany>
                    // ローディング完了
                    /////initLiveData.postValue(true)

                    // 規約一時非表示対応

                    // 認証開始 ここから →
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
                                //termsBinding.root.visibility = View.GONE

                                // メインビュー処理
                                mainInitialize()

                                // ローディング完了
                                initLiveData.postValue(true)
                            }
                        }
                    }
                    // 認証開始 ← ここまで

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

        // スプラッシュの裏でローディング画面を用意する
        splashBinding = FragmentCustomSplashScreenBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        // 3秒後にスプラッシュを強制非表示にする。データ取得が完了すると自動的にローディングが上書きされてメイン表示される
        var runnable = Runnable {
            initLiveData.postValue(true)
        }
        var handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(runnable!!, 3000)


        super.onCreate(savedInstanceState)

        // Camera結果の取得
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if (result.resultCode == RESULT_OK) {
                val text = result.data?.getStringExtra(MainActivity.EXTRA_MESSAGE) ?: ""

                val loadingDialog = showLoadingDialog()
                loadingDialog.show()
                //val futureFunction = functionViewModel.callFunction(FunctionViewModel.FUNCTION_QR, text)
                val futureFunction = functionViewModel.callFunctionRetry(FunctionViewModel.FUNCTION_QR, text)
                CompletableFuture.allOf(futureFunction).thenRun {
                    loadingDialog.dismiss()
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

                        var cd:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == cnId && it.srId == srId }

                        /*
                        if(cd != null && cd!!.startAt != null && cd!!.endAt != null){
                            if(commonDataViewModel.serverTime in cd!!.startAt!!..cd!!.endAt!!){
                                message = resources.getText(R.string.stamp_camera_qr_rally_private).toString()
                                showAlertDialog(message)
                            }
                        }
                        */

                        if("${cnId}_${srId}" == "${qrCnId}_${qrSrId}"){
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

                                        // 全ポイント達成チェック
                                        if(cb!!.cp.length == cb!!.complete!!.cp.length){
                                            cb!!.completeFlg = true
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

        /*
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
        */
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
            for(marker in markerList){
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
                    markerList.add(m!!)
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
                markerList.add(m!!)
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
                // ロードダイアログ
                val loadingDialog = showLoadingDialog()
                loadingDialog.show()

                val selected = getSelectId()
                val completableFuture = dataStoreViewModel.updateRewardAsyncTask(identityId!!, selected.first, selected.second, true)
                CompletableFuture.allOf(completableFuture).thenRun {

                    loadingDialog.dismiss()

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

        // ユーザ設定画面ボタン（仮）の表示
//        binding.openUserSetting.setOnClickListener {
//            val intent2UserSetting = Intent(this@MainActivity, UserSettingActivity::class.java)
//            startActivity(intent2UserSetting)
//        }
    }

    fun showLoadingDialog() :AlertDialog{
        //val builder = AlertDialog.Builder(this, R.style.TransparentDialog)
        val builder = AlertDialog.Builder(this)

        val dialogBinding = FragmentCustomSplashScreenBinding.inflate(layoutInflater)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
            setPadding(50, 50, 50, 50)
            //indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
        }
        val textView = TextView(this).apply {
            text = "Loading..."
            //setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        val button = Button(this, null, R.style.Stw_button).apply {
            text = "リトライ"
            setPadding(50, 50, 50, 50)
            setOnClickListener {
                // ボタンがクリックされた時の処理を記述
            }
        }

        layout.addView(progressBar)
        layout.addView(textView)
        layout.addView(button)

        //builder.setView(layout)
        builder.setView(dialogBinding.root)
        builder.setCancelable(false) // 画面外タッチでキャンセルできないようにする

        return builder.create()
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