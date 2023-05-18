package jp.co.xpower.app.stw

//import kotlinx.coroutines.async
//import kotlinx.coroutines.launch

import android.app.usage.UsageEvents
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
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import jp.co.xpower.app.stw.databinding.ActivityMainBinding
import jp.co.xpower.app.stw.databinding.TermsOfServiceBinding
import jp.co.xpower.app.stw.model.CommonData
import jp.co.xpower.app.stw.model.CommonDataViewModel
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

    //private lateinit var user:UserData
    private var identityId:String = ""
    private var identityNewId:String = ""
    //private var aaaaaa:Int = 1
    private lateinit var stwUser:StwUser
    private lateinit var stwCompanys:ArrayList<StwCompany>
    private lateinit var companyList:ArrayList<StwCompany>
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var mGestureDetector : GestureDetector

    private val initLiveData = MutableLiveData<Boolean>()


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


    private val commonDataViewModel by lazy {
        ViewModelProvider(this)[CommonDataViewModel::class.java]
    }

    /*
    private fun startDataSync(){
        val companyQueryFuture = CompletableFuture<List<StwCompany>>()
        val userQueryFuture = CompletableFuture<List<StwUser>>()

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
                    { Log.e("STW", "Subscription failed.", it) },
                    { Log.i("STW", "Subscription cancelled.") }
                )
            },
            {
                val common = CommonData()
                companyQueryFuture.completeExceptionally(it)
            }
        )

        CompletableFuture.allOf(companyQueryFuture, userQueryFuture).thenRun {
            val users = userQueryFuture.get()

            companyList = companyQueryFuture.get() as ArrayList<StwCompany>

            // ラリー詳細とユーザーデータのマージ
            var user: StwUser = users[0]

            val commonDataList = ArrayList<CommonData>()

            val companyes: ArrayList<StwCompany> = companyQueryFuture.get() as ArrayList<StwCompany>
            for (company in companyes) {
                for (rally in company.rallyList) {
                    val common = CommonData()
                    common.cnId = company.id
                    common.srId = rally.srId
                    common.srTitle = rally.title
                    common.srState = 0 // todo 開催期間で判断
                    common.srCp = rally.cp as ArrayList<CheckPoint>
                    common.joinFlg = false
                    common.completeFlg = false

                    // チェックポイント数とユーザーデータのチェックポイント数の一致でラリー達成
                    if(user.complete != null){
                        val cpTotal: Int = rally.cp.count()
                        val completeTotal: Int = user.complete.count { it.cnId == company.id && it.srId == rally.srId }
                        if (cpTotal > 0 && cpTotal == completeTotal) {
                            common.completeFlg = true
                        }
                        val complete = user.complete.find { it.cnId == company.id && it.srId == rally.srId }
                        if (complete != null) {
                            common.joinFlg = true
                        }
                    }
                    commonDataList.add(common)
                }
            }

            commonDataViewModel.commonDataList = commonDataList

            // 初期処理状態
            initLiveData.postValue(true)
        }
    }
    */

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

                // チェックポイント数とユーザーデータのチェックポイント数の一致でラリー達成
                if(user.complete != null){
                    val cpTotal: Int = rally.cp.count()
                    //val completeTotal: Int = user.complete.count { it.cnId == company.id && it.srId == rally.srId }
                    //if (cpTotal > 0 && cpTotal == completeTotal) {
                    //    common.completeFlg = true
                    //}
                    val complete = user.complete.find { it.cnId == company.id && it.srId == rally.srId }
                    if (complete != null) {
                        common.joinFlg = true

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

        commonDataViewModel.identityId = identityId

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

         identityId = readData()

        // ID認証開始
        Amplify.Auth.fetchAuthSession(
            { fetchSessionFuture.complete(it) },
            { fetchSessionFuture.completeExceptionally(it) }
        )
        fetchSessionFuture.thenAccept { authSession ->
            Log.i("STW", "Auth session = $authSession")

            // 未認証ID取得
            val cognitoAuthSession = authSession as AWSCognitoAuthSession
            identityNewId = cognitoAuthSession.identityIdResult.value!!

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
                                companyList = it.items.toList() as ArrayList<StwCompany>
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
        }

        if(isAgree){
            CompletableFuture.allOf(companyQueryFuture, userQueryFuture).thenRun {
                val users = userQueryFuture.get()
                updateDataViewModel(users[0])

                // ローディング完了
                initLiveData.postValue(true)
                Log.e("STW", "ok.")
            }
        }
        else {
            // 初回起動時はラリー詳細のみ取得する
            CompletableFuture.allOf(companyQueryFuture).thenRun {
                // ローディング完了
                initLiveData.postValue(true)
                Log.e("STW", "ok.")
            }
        }



    }

    override fun onCreate(savedInstanceState: Bundle?) {

        //val aa = intent.getBooleanExtra("terms", false)

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
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

                binding.layoutStamp.layout.visibility = View.VISIBLE

                val builder = AlertDialog.Builder(this)
                builder.setView(layoutInflater.inflate(R.layout.camera_result, null))
                val dialog = builder.create()
                dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
            }
        }

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


        // 初回起動時は規約ページを表示する
        val pref = getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        val isAgree = pref.getBoolean("is_agree", false)

        // 初期データ取得
        startInitProcess(isAgree)


        // 初回起動規約同意前
        if(!isAgree){

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
                        Log.i("STW", "Saved a user")
                        // IDをローカル保存
                        saveData(identityNewId)

                        identityId = identityNewId

                        //userSaveFuture.complete(true)

                        // View更新
                        updateDataViewModel(user)

                        val mainHandler = Handler(Looper.getMainLooper())
                        mainHandler.post {
                            pref.edit().putBoolean("is_agree", true).apply()
                            termsBinding.root.visibility = View.GONE

                            // メインビュー処理
                            mainInitialize()
                        }
                    },
                    { Log.e("STW", "Save failed", it) }
                )

            }
            //return
        }
        else {
            // メインビュー処理
            mainInitialize()
        }
    }

    private fun mainInitialize(){
        // メインレイアウト設定
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // タイトルバー非表示
        supportActionBar?.hide()

        // スタンプ表示切り替え
        binding.btStampList.setOnClickListener(OnButtonClick())

        // テストデータ設定
        populateStamp()
        // スタンプデータ設定
        binding.layoutStamp.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = StampAdapter(stampList)
        }

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
                binding.layoutReward.layout.visibility = View.GONE
                binding.layoutReceived.layout.visibility = View.VISIBLE
                binding.layoutReward.swReceiveReward.isClickable = false
            }
        }
        mGestureDetector = GestureDetector(this@MainActivity, MGestureListener())
        binding.layoutReward.swReceiveReward.setOnTouchListener(SwTouchListener())

        // スタンプカード・報酬受取画面・受取完了画面の閉じるボタンの設定
        binding.layoutStamp.buttonClose.setOnClickListener(OnButtonClick())
        binding.layoutReward.buttonClose.setOnClickListener(OnButtonClick())
        binding.layoutReceived.buttonClose.setOnClickListener(OnButtonClick())

        // 獲得数だけ強調表示
        binding.layoutStamp.textGet.changeSizeOfText("3", 38)

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
                    updateRally(identityId, "c0004", "s0001", "p0005")
                    //updateRally(identityId, "c0002", "s0001", "")
                    //Amplify.DataStore.clear(
                    //    { Log.i("MyAmplifyApp", "DataStore is cleared") },
                    //    { Log.e("MyAmplifyApp", "Failed to clear DataStore") }
                    //)
                    true
                }
                */

                else -> false
            }
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
                    btStampList.isClickable = false
                    stampLayout.visibility = View.GONE
                    if(!binding.layoutReward.swReceiveReward.isChecked){
                        // 報酬未受取の場合は受取画面を表示
                        rewardLayout.visibility = View.VISIBLE
                    }
                    else {
                        // 報酬受取済の場合は受取完了画面を表示
                        receivedLayout.visibility = View.VISIBLE
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

    // cnId: 会社ID
    // srId: ラリーID
    // cpId: チェックポイントID
    private fun updateRally(id:String, cnId:String, srId:String, cpId:String){
        Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq(id),
            { matches ->
                if (matches.hasNext()) {
                    val original = matches.next()

                    var completeData:ArrayList<Complete> = ArrayList<Complete>()

                    // 新規
                    if(original.complete == null){
                        var checkPointList:ArrayList<CheckPoint> = ArrayList<CheckPoint>()
                        if(cpId != ""){
                            val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                            checkPointList.add(cp)
                            val c: Complete = Complete.builder().cnId(cnId).srId(srId).history(0).cp(checkPointList).build()
                            completeData.add(c)
                        }
                        else {
                            val c: Complete = Complete.builder().cnId(cnId).srId(srId).history(0).build()
                            completeData.add(c)
                        }

                        val edited = original.copyOfBuilder()
                            .complete(completeData)
                            .build()

                        Amplify.DataStore.save(edited,
                            { Log.i("STW", "Updated a user") },
                            { Log.e("STW", "Update failed", it) }
                        )
                    }
                    else {
                        val complete = original.complete.find { it.cnId == cnId && it.srId == srId }
                        // チェックポイント追加
                        if(complete != null){
                            val checkCp = complete.cp.find { it.cpId == cpId }
                            if(checkCp != null){
                                Log.i("STW", "cp exist.")
                                return@query
                            }
                            val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                            // 対象のラリーにチェックポイントを追加
                            complete.cp.add(cp)
                            //original.complete.add(complete)
                        }
                        // 新規ラリー達成
                        else {
                            var checkPointList:ArrayList<CheckPoint> = ArrayList<CheckPoint>()
                            val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                            checkPointList.add(cp)
                            val c: Complete = Complete.builder().cnId(cnId).srId(srId).history(0).cp(checkPointList).build()
                            original.complete.add(c)

                        }

                        val edited = original.copyOfBuilder()
                            //.complete(completeData)
                            .build()

                        Amplify.DataStore.save(edited,
                            { Log.i("STW", "Updated a user") },
                            { Log.e("STW", "Update failed", it) }
                        )

                        /*
                        completeData = original.complete as ArrayList<Complete>
                        completeData.forEach { element ->
                            // 既存の会社・ラリーの達成
                            if( element.cnId == cnId && element.srId == srId ){
                                val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                                element.cp.add(cp)
                                isExist = true
                            }
                        }
                        // 既存の会社・ラリー以外の達成の場合
                        if(!isExist){
                            var checkPointList:ArrayList<CheckPoint> = ArrayList<CheckPoint>()
                            val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                            checkPointList.add(cp)
                            val c: Complete = Complete.builder().cnId(cnId).srId(srId).history(0).cp(checkPointList).build()
                            completeData.add(c)
                        }
                        */
                    }

                    /*
                    val edited = original.copyOfBuilder()
                        //.complete(completeData)
                        .build()

                    Amplify.DataStore.save(edited,
                        { Log.i("STW", "Updated a user") },
                        { Log.e("STW", "Update failed", it) }
                    )
                    */
                }
            },
            { Log.e("STW", "Query failed", it) }
        )
    }

    // スタンプラリー状況更新
    fun updateData(id:String, complete:ArrayList<Complete>){

        Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq(id),
            { matches ->
                if (matches.hasNext()) {
                    val original = matches.next()
                    val edited = original.copyOfBuilder()
                        .complete(complete)
                        .build()

                    Amplify.DataStore.save(edited,
                        { Log.i("STW", "Updated a user") },
                        { Log.e("STW", "Update failed", it) }
                    )
                }
            },
            { Log.e("STW", "Query failed", it) }
        )
    }

    // 初回ユーザーアカウント作成
    fun createData(id:String){
        Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq(id),
            { matches ->
                if (!matches.hasNext()) {
                    Log.i("STW", "create.")
                    val user = StwUser.builder()
                        .id(id)
                        .name("名前未設定")
                        .build()
                    Amplify.DataStore.save(user,
                        { Log.i("STW", "Saved a user") },
                        { Log.e("STW", "Save failed", it) }
                    )

                }
            },
            { Log.e("STW", "Query failed.", it) }
        )
    }

    fun readData(): String {
        val fileName = "app.dat"
        var text:String = ""

        try {
            val stream: FileInputStream = openFileInput(fileName)
            val data = stream.readBytes().decodeToString()
            stream.close()
            text = data
        }
        catch (e: FileNotFoundException){

        }
        catch (e:IOException){
            e.printStackTrace()
        }

        return text
    }

    fun saveData(id:String){
        val fileName = "app.dat"
        openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(id.toByteArray())
        }
    }
}
