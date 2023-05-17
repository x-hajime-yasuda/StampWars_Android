package jp.co.xpower.app.stw

//import kotlinx.coroutines.async
//import kotlinx.coroutines.launch

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Layout
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import aws.smithy.kotlin.runtime.util.length
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Action
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Consumer
import com.amplifyframework.core.async.Cancelable
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.core.model.query.predicate.QueryPredicate
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
import kotlinx.coroutines.*
import java.util.ArrayList
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.CompletableFuture
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import jp.co.xpower.app.stw.model.CommonData
import jp.co.xpower.app.stw.model.CommonDataViewModel
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var termsBinding: TermsOfServiceBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    //private lateinit var user:UserData
    private var identityId:String = ""
    //private var aaaaaa:Int = 1
    private lateinit var stwUser:StwUser
    private lateinit var stwCompanys:ArrayList<StwCompany>
    private lateinit var companyList:ArrayList<StwCompany>
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var mGestureDetector : GestureDetector


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




        identityId = readData()

        val fetchSessionFuture = CompletableFuture<AuthSession>()
        val companyQueryFuture = CompletableFuture<List<StwCompany>>()
        val userQueryFuture = CompletableFuture<List<StwUser>>()

        // ID認証開始
        Amplify.Auth.fetchAuthSession(
            { fetchSessionFuture.complete(it) },
            { fetchSessionFuture.completeExceptionally(it) }
        )
        fetchSessionFuture.thenAccept { authSession ->
            // 未認証ID取得
            val cognitoAuthSession = authSession as AWSCognitoAuthSession
            val id: String? = cognitoAuthSession.identityIdResult.value
            // 初アクセス時のIDをユーザーIDとして使用する
            if((identityId == null || identityId == "") && id != null){
                identityId = id.toString()
                saveData(identityId)
                createData(identityId)
            }
            Log.i("STW", "Auth session = $authSession")

            // データ同期開始
            Amplify.DataStore.start(
                {
                    Amplify.DataStore.query(StwCompany::class.java,
                        { companyQueryFuture.complete(it.asSequence().toList()) },
                        { companyQueryFuture.completeExceptionally(it) }
                    )
                    Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq(identityId),
                        { userQueryFuture.complete(it.asSequence().toList()) },
                        { userQueryFuture.completeExceptionally(it) }
                    )
                },
                { companyQueryFuture.completeExceptionally(it) }
            )
        }

        CompletableFuture.allOf(companyQueryFuture, userQueryFuture).thenRun {
            val query1Result = companyQueryFuture.get()
            val users = userQueryFuture.get()

            companyList = companyQueryFuture.get() as ArrayList<StwCompany>

            val viewModel = ViewModelProvider(this)[CommonDataViewModel::class.java]
            //viewModel.data.message = "test1"

            // ラリー詳細とユーザーデータのマージ
            var user:StwUser = users[0]

            val companyes:ArrayList<StwCompany> = companyQueryFuture.get() as ArrayList<StwCompany>
            for( company in companyes){
                for( rally in company.rallyList){
                    val common = CommonData()
                    common.cnId = company.id
                    common.srId = rally.srId
                    common.srTitle = rally.title
                    common.srState = 0 // todo 開催期間で判断
                    common.srCp = rally.cp as ArrayList<CheckPoint>
                    common.joinFlg = false
                    common.completeFlg = false

                    //val a = user.complete.find{ it.cnId == company.id && it.srId == rally.srId }
                    //val aa = user.complete.find{ it.cnId == company.id && it.srId == rally.srId }

                }
            }


            //commonDataViewModel.companyList = companyQueryFuture.get() as ArrayList<StwCompany>


            //val d1 = query1Result[0].createdAt.toDate()

            Log.i("STW", "All Ok.")
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
        binding.btStampList.setOnClickListener(OnButtonClick())
        /*
        binding.buttonStamp.setOnClickListener {
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

                    // ユーザーデータ
                    /*
                    Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq(identityId),
                        { matches ->
                            if (matches.hasNext()) {
                                val post = matches.next()
                                Log.i("STW", "id: ${post.id}")
                                Log.i("STW", "name: ${post.name}")
                                for(c in post.complete){
                                    Log.i("STW", "name: ${c.cnId}")
                                    Log.i("STW", "name: ${c.srId}")
                                    for(cp in c.cp){
                                        Log.i("STW", "name: ${cp.cpId}")
                                    }
                                }
                            }
                        },
                        { Log.e("MyAmplifyApp", "Query failed.", it) }
                    )
                    */






                    true
                }
                R.id.menu_profile -> {
                    val intent = Intent(this, CameraActivity::class.java)
                    //startActivity(intent)
                    cameraLauncher.launch(intent)

                    // 同期
                    /*
                    Amplify.addPlugin(
                        AWSDataStorePlugin.builder().dataStoreConfiguration(
                        DataStoreConfiguration.builder()
                            .syncExpression(StwUser::class.java) { StwUser.ID.eq("u0001") }
                            .build())
                        .build())
                    */



                    /*
                    // 更新(取得してあれば)
                    Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq("u0001"),
                        { matches ->
                            if (matches.hasNext()) {
                                val original = matches.next()
                                val edited = original.copyOfBuilder()
                                    .name("New Title $aaaaaa")
                                    .build()
                                Amplify.DataStore.save(edited,
                                    { Log.i("MyAmplifyApp", "Updated a post") },
                                    { Log.e("MyAmplifyApp", "Update failed", it) }
                                )
                            }
                        },
                        { Log.e("MyAmplifyApp", "Query failed", it) }
                    )
                    aaaaaa += 1
                    */

                    //createData("aaaaaaaaaa")


                    /*
                    // 削除
                    Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq("u0001"),
                        { matches ->
                            if (matches.hasNext()) {
                                val post = matches.next()
                                Amplify.DataStore.delete(post,
                                    { Log.i("STW", "Deleted a user.") },
                                    { Log.e("STW", "Delete failed.", it) }
                                )
                            }
                        },
                        { Log.e("STW", "Query failed.", it) }
                    )
                    */





                    /*
                    Amplify.DataStore.observe(
                        StwUser::class.java,
                        { Log.i("MyAmplifyApp", "Observation began") },
                        {
                            // only listen for incoming messages sent to the logged in user
                            Log.i("MyAmplifyApp", "Message: ${it.item().name}")
                        },
                        { Log.e("ChatLog-Listen", "Observation failed", it) },
                        { Log.i("ChatLog-Listen", "Observation complete") }
                    )
                    */

                    /* // 取得できるサンプル
                    val predicate: QueryPredicate = StwUser.ID.eq("u0001")

                    val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<StwUser>> =
                        Consumer<DataStoreQuerySnapshot<StwUser>> { value: DataStoreQuerySnapshot<StwUser> ->
                            val post = value.items[0]
                            Log.i("MyAmplifyApp", "Message: $post")
                        }
                    val observationStarted =
                        Consumer { _: Cancelable ->
                            Log.i("MyAmplifyApp", "Message: cancelable")
                        }
                    val onObservationError =
                        Consumer { value: DataStoreException ->
                            Log.i("MyAmplifyApp", "Message: $value")
                        }
                    val onObservationComplete = Action {
                    }

                    val options = ObserveQueryOptions(predicate, null)
                    Amplify.DataStore.observeQuery(
                        StwUser::class.java,
                        options,
                        observationStarted,
                        onQuerySnapshot,
                        onObservationError,
                        onObservationComplete
                    )
                    */







                    /*
                    Amplify.DataStore.query(StwUser::class.java,
                        { allPosts ->
                            while (allPosts.hasNext()) {
                                val post = allPosts.next()
                                Log.i("MyAmplifyApp", "Name: ${post.name}")
                            }
                        },
                        { Log.e("MyAmplifyApp", "Query failed", it) }
                    )
                    */
                    /*
                    Amplify.DataStore.query(StwUser::class.java,
                        { todos ->
                            while (todos.hasNext()) {
                                val todo: StwUser = todos.next()
                                Log.i("Tutorial", "==== Todo ====")
                                Log.i("Tutorial", "Name: ${todo.name}")
                            }
                        },
                        { Log.e("Tutorial", "Could not query DataStore", it)  }
                    )
                    */



                    true
                }
                /*
                R.id.menu_profile2 -> {
                    updateRally(identityId, "c0001", "s0001", "p0002")

                    //val clen = stwCompanys.length
                    //Log.e("Tutorial", clen.toString())
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


    /*
    // ユーザーデータ取得
    private fun observeUser() {
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<StwUser>> =
            Consumer<DataStoreQuerySnapshot<StwUser>> { value: DataStoreQuerySnapshot<StwUser> ->
                val u = value.items
                if(u.length == 1){
                    stwUser = u[0]
                    Log.e("STW", "${u[0].name}")
                }
            }
        val observationStarted =
            Consumer { _: Cancelable ->
            }
        val onObservationError =
            Consumer { value: DataStoreException ->
            }
        val onObservationComplete = Action {
        }

        val predicate: QueryPredicate = StwUser.ID.eq("ap-northeast-1:275a1926-c177-46a6-9f51-0e40d01f03f5")
        val options = ObserveQueryOptions(predicate, null)
        Amplify.DataStore.observeQuery(
            StwUser::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }

    private fun observeCompany() {
        Amplify.DataStore.query(StwCompany::class.java,
            { matches ->
                if (matches.hasNext()) {
                    val post = matches.next()
                    Log.e("STW", "Query Company.")
                }
            },
            { Log.e("STW", "Query Company.", it) }
        )

        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<StwCompany>> =
            Consumer<DataStoreQuerySnapshot<StwCompany>> { value: DataStoreQuerySnapshot<StwCompany> ->
                if(value.items.length == 0){
                    return@Consumer
                }
                stwCompanys = ArrayList<StwCompany>()
                for(list in value.items){
                    Log.e("STW", "${list.name}")
                    stwCompanys.add(list)
                }
                Log.e("STW", "${stwCompanys.length}")
            }
        val observationStarted =
            Consumer { _: Cancelable ->
            }
        val onObservationError =
            Consumer { value: DataStoreException ->
            }
        val onObservationComplete = Action {
        }

        //val predicate: QueryPredicate = StwCompany.ID.eq("ap-northeast-1:275a1926-c177-46a6-9f51-0e40d01f03f5")
        //val options = ObserveQueryOptions(predicate, null)
        val options = ObserveQueryOptions()
        Amplify.DataStore.observeQuery(
            StwCompany::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }
    */

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
                        val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                        checkPointList.add(cp)
                        val c: Complete = Complete.builder().cnId(cnId).srId(srId).history(0).cp(checkPointList).build()
                        completeData.add(c)
                    }
                    else {
                        var isExist = false
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
                    }

                    val edited = original.copyOfBuilder()
                        .complete(completeData)
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

    // スタンプラリー状況更新
    private fun updateData(id:String, complete:ArrayList<Complete>){

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
    private fun createData(id:String){
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

    private fun readData(): String {
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

    private fun saveData(id:String){
        val fileName = "app.dat"
        openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(id.toByteArray())
        }
    }
}
