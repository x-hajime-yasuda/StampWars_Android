package jp.co.xpower.app.stw

import android.content.Context
import android.content.Intent
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
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import aws.smithy.kotlin.runtime.util.length
import com.amplifyframework.api.aws.AppSyncGraphQLRequest
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.GraphQLResponse
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Consumer
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.core.model.query.predicate.QueryPredicate
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.*
import com.amplifyframework.datastore.generated.model.CheckPoint
import com.amplifyframework.datastore.generated.model.Complete
import com.amplifyframework.datastore.generated.model.Rally
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import jp.co.xpower.app.stw.databinding.ActivityMainBinding
import jp.co.xpower.app.stw.databinding.CameraResultBinding
import jp.co.xpower.app.stw.databinding.TermsOfServiceBinding
import jp.co.xpower.app.stw.model.CommonData
import jp.co.xpower.app.stw.model.CommonDataViewModel
import jp.co.xpower.app.stw.model.DataStoreViewModel
import jp.co.xpower.app.stw.util.StwUtils
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors



class MainActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener {
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
    private val markerList: MutableList<Marker> = mutableListOf()



    companion object {
        const val  EXTRA_MESSAGE ="jp.co.xpower.app.stw.camera_activity.MESSAGE"
        const val RALLY_STATE_PUBLIC = 1
        const val RALLY_STATE_JOIN = 2
        const val RALLY_STATE_END = 3

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
                common.state = 0 // todo 開催期間で判断
                common.cp = rally.cp as ArrayList<CheckPoint>
                common.joinFlg = false
                common.completeFlg = false

                if(serverTime in startAt..endAt){
                    common.state = RALLY_STATE_PUBLIC    // 開催中
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

    //private fun getTodoRequest(id: String): GraphQLRequest<Todo> {
    private fun getEventRequest(): GraphQLRequest<Temporal.DateTime> {
        val document = "query GetServerTime { getServerTime { serverTime } }"

        val headers = mapOf("Authorization" to "Bearer your_token_here")


        return SimpleGraphQLRequest(
            document,
            headers,
            GetServerTimeResponse::class.java,
            GsonVariablesSerializer()
        )
    }

    data class GetServerTimeResponse(val serverTime: Temporal.DateTime)

    private fun startInitProcess(isAgree:Boolean) {
        val fetchSessionFuture = CompletableFuture<AuthSession>()
        val companyQueryFuture = CompletableFuture<List<StwCompany>>()
        val userQueryFuture = CompletableFuture<List<StwUser>>()

        val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        identityId = pref.getString(PREF_KEY_USER_ID, "")

        // データ同期開始
        Amplify.DataStore.start(
            {
                // 会社情報・ラリー詳細
                Amplify.DataStore.observeQuery(
                    StwCompany::class.java,
                    ObserveQueryOptions(),
                    { Log.i("STW", "Subscription established.") },
                    Consumer<DataStoreQuerySnapshot<StwCompany>>{
                        if( it.items.length != 0){
                            companyQueryFuture.complete(it.items.toList())
                        }
                    },
                    { Log.e("STW", "DataStore.observeQuery failed.", it) },
                    { Log.i("STW", "DataStore.observeQuery cancelled.") }
                )
                // 2回目以降はユーザーデータを取得する
                if(isAgree){
                    val predicate: QueryPredicate = StwUser.ID.eq(identityId)
                    Amplify.DataStore.observeQuery(
                        StwUser::class.java,
                        ObserveQueryOptions(predicate, null),
                        { Log.i("STW", "Subscription established.") },
                        Consumer<DataStoreQuerySnapshot<StwUser>>{
                            if( it.items.length != 0){
                                userQueryFuture.complete(it.items.toList())
                            }
                        },
                        { Log.e("STW", "Subscription failed.", it) },
                        { Log.i("STW", "Subscription cancelled.") }
                    )
                }
            },
            { companyQueryFuture.completeExceptionally(it) }
        )

        // タイムサーバーから現在日時を取得
        val timeFuture = CompletableFuture.supplyAsync {
            StwUtils.getNtpTime()
        }
        //CompletableFuture.allOf(timeFuture).thenRun {
        //    serverTime = timeFuture.get() / 1000
        //}

        if(isAgree){
            CompletableFuture.allOf(companyQueryFuture, userQueryFuture, timeFuture).thenRun {

                serverTime = timeFuture.get() / 1000

                // 会社・ラリー詳細
                val companyes = companyQueryFuture.get()
                if(companyes != null){
                    companyList = companyes.toList() as ArrayList<StwCompany>
                }

                val users = userQueryFuture.get()
                updateDataViewModel(users[0])

                // メインビュー処理
                val mainHandler = Handler(Looper.getMainLooper())
                mainHandler.post {
                    mainInitialize()
                }

                // ローディング完了
                initLiveData.postValue(true)
                Log.e("STW", "ok.")
            }
        }
        else {
            // 初回起動時はラリー詳細のみ取得する
            CompletableFuture.allOf(companyQueryFuture).thenRun {
                serverTime = timeFuture.get() / 1000

                // 会社・ラリー詳細
                val companyes = companyQueryFuture.get()
                if(companyes != null){
                    companyList = companyes.toList() as ArrayList<StwCompany>
                }

                // ローディング完了
                initLiveData.postValue(true)
                Log.e("STW", "ok.")
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
                //Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                // todo QRを解析して該当のラリーのチェックポイントを達成とする
                // 獲得したらDB更新
                /*
                val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
                val selectCnId = pref.getString(MainActivity.PREF_KEY_SELECT_CN_ID, "")
                val selectSrId = pref.getString(MainActivity.PREF_KEY_SELECT_SR_ID, "")

                // test
                val cpId = "p0002"

                val completableFuture = dataStoreViewModel.updateAsyncTask(identityId!!, selectCnId!!, selectSrId!!, cpId)
                CompletableFuture.allOf(completableFuture).thenRun {
                    // 達成したらcommonDataListに選択CommonDataのチェックポイントを追加する
                    var cb:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == selectCnId && it.srId == selectSrId }
                    val point:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                    if(cb != null && cb!!.complete != null){
                        cb!!.complete!!.cp.add(point)
                    }

                    // 選択中ラリーの表示更新
                    updateSelected()
                }
                */

                val cameraResultBinding = CameraResultBinding.inflate(layoutInflater)
                cameraResultBinding.textView.text = "\n\nスタンプを獲得しました！\n\n"

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
                Log.i("MainActivity Map ======>", "ready")

                // 新規ユーザー登録
                val user = StwUser.builder()
                    .id(identityNewId)
                    .name("名前未設定")
                    .build()
                Amplify.DataStore.save(user,
                    {
                        identityId = identityNewId

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
                    },
                    { Log.e("STW", "Save failed", it) }
                )
            }
        }
    }

    private fun getSelectId() :Pair<String, String>{
        val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        val selectCnId = pref.getString(PREF_KEY_SELECT_CN_ID, "")
        val selectSrId = pref.getString(PREF_KEY_SELECT_SR_ID, "")
        return Pair(selectCnId!!, selectSrId!!)
    }

    fun isRewardReceived() :Boolean {
        val selected = getSelectId()
        var cd:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == selected.first && it.srId == selected.second }
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
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                binding.tvStampCount.text = "${completeCount}/${cd.cp.count()}"
            }
        }
        else {
            binding.btStampList.visibility = View.INVISIBLE
            binding.tvStamp.visibility = View.INVISIBLE
            binding.layoutSelected.layout.visibility = View.INVISIBLE
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
                for(checkpoint in cd!!.cp){
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
        }
        else{
            // マップが初期化されるまで待機する
            Handler(Looper.getMainLooper()).postDelayed({
                updateMap()
            }, 1000)
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

        // 獲得数だけ強調表示
        //binding.layoutStamp.textGet.changeSizeOfText("53", 38)

        // ボトムメニュー ボタンイベント
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    // ボトムシート表示
                    val aa = ArrayList<StwCompany>()
                    BottomSheetFragment.newInstance(companyList).show(supportFragmentManager, "dialog")

                    true
                }
                R.id.menu_profile -> {
                    val intent = Intent(this, CameraActivity::class.java)
                    //startActivity(intent)
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
                            binding.layoutStamp.buttonReward.setBackgroundResource(R.drawable.button_ripple)
                            binding.layoutStamp.buttonReward.isEnabled = true
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
