package jp.co.xpower.cotamp.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aws.smithy.kotlin.runtime.util.length
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Consumer
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.core.model.query.predicate.QueryPredicate
import com.amplifyframework.datastore.DataStoreQuerySnapshot
import com.amplifyframework.datastore.generated.model.CheckPoint
import com.amplifyframework.datastore.generated.model.Complete
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.concurrent.CompletableFuture

class DataStoreViewModel : ViewModel() {

    companion object {
        const val SUCCESS = 0   // ラリー未参加
        const val NOT_YET_JOIN = 1   // ラリー未参加
        const val ALREADY_COMPLETED = 2 // 達成済みのポイント
        const val INVALID_CODE = 3  // 不正コード
        const val OTHER_RALLY_CODE = 4  // 選択中以外のコード
    }

    // ユーザー認証
    fun fetchAuth() :CompletableFuture<String> {
        val completableFuture = CompletableFuture<String>()
        val fetchSessionFuture = CompletableFuture<AuthSession>()

        viewModelScope.launch {
            try {
                Amplify.Auth.fetchAuthSession(
                    { fetchSessionFuture.complete(it) },
                    { fetchSessionFuture.completeExceptionally(it) }
                )
                fetchSessionFuture.thenAccept { authSession ->
                    val cognitoAuthSession = authSession as AWSCognitoAuthSession
                    val newId = cognitoAuthSession.identityIdResult.value!!
                    completableFuture.complete(newId)
                }
            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }

    // ユーザー登録
    fun createUser(identityId:String, name:String) :CompletableFuture<StwUser> {
        val completableFuture = CompletableFuture<StwUser>()

        viewModelScope.launch {
            try {
                val user = StwUser.builder()
                    .id(identityId)
                    .name(name)
                    .build()

                Amplify.DataStore.save(user,
                    { completableFuture.complete(user) },
                    { completableFuture.completeExceptionally(it) }
                )
            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }

        return completableFuture
    }

    // DataStore開始
    fun initDataStore() :CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()

        viewModelScope.launch {
            try {
                Amplify.DataStore.start(
                    { completableFuture.complete(true) },
                    { completableFuture.completeExceptionally(it) }
                )
            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }


    // 会社・ラリー詳細取得
    fun getCompany() :CompletableFuture<List<StwCompany>> {
        val completableFuture = CompletableFuture<List<StwCompany>>()

        Amplify.DataStore.observeQuery(
            StwCompany::class.java,
            ObserveQueryOptions(),
            { Log.i("STW", "getCompany established.") },
            Consumer<DataStoreQuerySnapshot<StwCompany>>{
                if(it.items.length == 0){
                    completableFuture.complete(ArrayList<StwCompany>())
                }
                else if(it.items.length == 1){
                    completableFuture.complete(arrayListOf(it.items[0]))
                }
                else {
                    completableFuture.complete(it.items.toList())
                }
            },
            { Log.e("STW", "getCompany failed.", it) },
            { Log.i("STW", "getCompany cancelled.") }
        )

        return completableFuture
    }

    // ユーザー情報取得
    fun getUser(identityId:String) :CompletableFuture<List<StwUser>> {
        val completableFuture = CompletableFuture<List<StwUser>>()

        val predicate: QueryPredicate = StwUser.ID.eq(identityId)
        Amplify.DataStore.observeQuery(
            StwUser::class.java,
            ObserveQueryOptions(predicate, null),
            { Log.i("STW", "getUser established.") },
            Consumer<DataStoreQuerySnapshot<StwUser>>{
                if( it.items.length != 0){
                    completableFuture.complete(it.items.toList())
                }
            },
            { Log.e("STW", "getUser failed.", it) },
            { Log.i("STW", "getUser cancelled.") }
        )

        return completableFuture
    }



    fun updateRewardAsyncTask(id:String, cnId:String, srId:String, isReward:Boolean) : CompletableFuture<Boolean>{
        val completableFuture = CompletableFuture<Boolean>()

        viewModelScope.launch {

            Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq(id),
                { matches ->
                    if (matches.hasNext()) {
                        val original = matches.next()
                        var completeData:ArrayList<Complete> = ArrayList<Complete>()

                        //var edited = original.copyOfBuilder().build()
                        for(complete in original.complete){

                            if(complete.cnId == cnId && complete.srId == srId){
                                val cmp = Complete.builder().cnId(complete.cnId).srId(complete.srId).cp(complete.cp).history(complete.history).got(isReward).build()
                                completeData.add(cmp)
                            }
                            else {
                                val cmp = Complete.builder().cnId(complete.cnId).srId(complete.srId).cp(complete.cp).history(complete.history).got(complete.got).build()
                                completeData.add(cmp)
                            }
                        }

                        val edited = original.copyOfBuilder()
                            .complete(completeData)
                            .build()

                        Amplify.DataStore.save(edited,
                            {
                                Log.i("STW", "updateRewardAsyncTask:save Success.")
                                completableFuture.complete(true)
                            },
                            {
                                Log.e("STW", "updateRewardAsyncTask:save failed.", it)
                                completableFuture.completeExceptionally(it)
                            }
                        )
                    }
                },
                {
                    Log.e("STW", "updateRewardAsyncTask:query failed.", it)
                    completableFuture.completeExceptionally(it)
                }
            )
        }

        return completableFuture
    }

    fun rallyStamping(data:CommonDataViewModel, cpId:String) :CompletableFuture<Int> {
        val completableFuture = CompletableFuture<Int>()
        viewModelScope.launch {
            try {
                Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq(data.identityId),
                    { matches ->
                        if (matches.hasNext()) {
                            val original = matches.next()
                            if(original.complete == null){
                                // まだ参加していない
                                completableFuture.complete(NOT_YET_JOIN)
                                return@query
                            }
                            val complete = original.complete.find { it.cnId == data.selectCnId && it.srId == data.selectSrId }
                            // 新規達成
                            if(complete!!.cp == null){
                                var checkPointList:ArrayList<CheckPoint> = ArrayList<CheckPoint>()
                                val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                                checkPointList.add(cp)
                                val c: Complete = Complete.builder().cnId(data.selectCnId).srId(data.selectSrId).history(0).cp(checkPointList).build()

                                original.complete.find { it.cnId == data.selectCnId && it.srId == data.selectSrId}?.let {
                                    original.complete.remove(it)
                                }
                                original.complete.add(c)

                                val edited = original.copyOfBuilder()
                                    .build()

                                Amplify.DataStore.save(edited,
                                    {
                                        Log.i("STW", "Updated a user")
                                        completableFuture.complete(SUCCESS)
                                    },
                                    {
                                        completableFuture.completeExceptionally(it)
                                    }
                                )
                            }
                            // 既存ラリーのチェックポイント達成
                            else {
                                val checkCp = complete.cp.find { it.cpId == cpId }
                                // 登録済みチェックポイント
                                if(checkCp != null){
                                    // チェックポイント達成済み
                                    completableFuture.complete(ALREADY_COMPLETED)
                                    return@query
                                }
                                val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                                // 対象のラリーにチェックポイントを追加
                                complete.cp.add(cp)

                                val edited = original.copyOfBuilder()
                                    .build()

                                Amplify.DataStore.save(edited,
                                    {
                                        Log.i("STW", "Updated a user")
                                        completableFuture.complete(SUCCESS)
                                    },
                                    {
                                        completableFuture.completeExceptionally(it)
                                    }
                                )
                            }
                        }
                    },
                    { completableFuture.completeExceptionally(it)}
                )
            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }

    fun rallyJoining(data:CommonDataViewModel) :CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        viewModelScope.launch {
            try {
                Amplify.DataStore.query(StwUser::class.java, StwUser.ID.eq(data.identityId),
                    { matches ->
                        if (matches.hasNext()) {
                            val original = matches.next()
                            // 1度も達成していない場合
                            if(original.complete == null){
                                var completeList:ArrayList<Complete> = ArrayList<Complete>()
                                var checkPointList:ArrayList<CheckPoint> = ArrayList<CheckPoint>()
                                val complete:Complete = Complete.builder().cnId(data.selectCnId).srId(data.selectSrId).history(0).cp(checkPointList).build()
                                completeList.add(complete)

                                val edited = original.copyOfBuilder()
                                    .complete(completeList)
                                    .build()

                                Amplify.DataStore.save(edited,
                                    {
                                        Log.i("STW", "Updated a user")
                                        completableFuture.complete(true)
                                    },
                                    {
                                        completableFuture.completeExceptionally(it)
                                    }
                                )
                            }
                            // 新規参加
                            else {
                                val complete = original.complete.find { it.cnId == data.selectCnId && it.srId == data.selectSrId }
                                if(complete == null){
                                    var checkPointList:ArrayList<CheckPoint> = ArrayList<CheckPoint>()
                                    val complete:Complete = Complete.builder().cnId(data.selectCnId).srId(data.selectSrId).history(0).cp(checkPointList).build()
                                    original.complete.add(complete)
                                }
                                // 既に参加済み
                                else {
                                    completableFuture.complete(false)
                                    return@query
                                }

                                val edited = original.copyOfBuilder()
                                    .build()

                                Amplify.DataStore.save(edited,
                                    {
                                        Log.i("STW", "Updated a user")
                                        completableFuture.complete(true)
                                    },
                                    {
                                        completableFuture.completeExceptionally(it)
                                    }
                                )
                            }
                        }
                    },
                    { completableFuture.completeExceptionally(it)}
                )
            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }

    fun updateAsyncTask(id:String, cnId:String, srId:String, cpId:String): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()

        viewModelScope.launch {
            try {
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
                                    {
                                        Log.i("STW", "Updated a user")
                                        completableFuture.complete(true)
                                    },
                                    {
                                        Log.e("STW", "Update failed", it)
                                    }
                                )
                            }
                            else {
                                val complete = original.complete.find { it.cnId == cnId && it.srId == srId }
                                // チェックポイント追加
                                if(complete != null){
                                    if(complete.cp == null){
                                        if(cpId == ""){
                                            Log.i("STW", "rally select.")
                                            completableFuture.complete(true)
                                            return@query
                                        }
                                        Log.i("STW", "cp new.")
                                        var checkPointList:ArrayList<CheckPoint> = ArrayList<CheckPoint>()
                                        val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                                        checkPointList.add(cp)
                                        val c: Complete = Complete.builder().cnId(cnId).srId(srId).history(0).cp(checkPointList).build()

                                        original.complete.find { it.cnId == cnId && it.srId == srId}?.let {
                                            original.complete.remove(it)
                                        }
                                        original.complete.add(c)

                                    }
                                    // 既存ラリーにチェックポイントを追加
                                    else {
                                        if(cpId == ""){
                                            Log.i("STW", "rally select.")
                                            completableFuture.complete(true)
                                            return@query
                                        }

                                        val checkCp = complete.cp.find { it.cpId == cpId }
                                        // 登録済みチェックポイント
                                        if(checkCp != null){
                                            Log.i("STW", "cp exist.")
                                            completableFuture.complete(true)
                                            return@query
                                        }
                                        val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                                        // 対象のラリーにチェックポイントを追加
                                        complete.cp.add(cp)
                                    }
                                }
                                // 新規ラリー達成(選択時に空のリストが作成されるので入らないはず)
                                else {
                                    if(cpId != ""){
                                        var checkPointList:ArrayList<CheckPoint> = ArrayList<CheckPoint>()
                                        val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                                        checkPointList.add(cp)
                                        val c: Complete = Complete.builder().cnId(cnId).srId(srId).history(0).cp(checkPointList).build()
                                        original.complete.add(c)
                                    }
                                    // チェックポイント指定なし。参加のみの処理
                                    else {
                                        val c: Complete = Complete.builder().cnId(cnId).srId(srId).history(0).build()
                                        original.complete.add(c)
                                    }
                                }

                                val edited = original.copyOfBuilder()
                                    .build()

                                Amplify.DataStore.save(edited,
                                    {
                                        Log.i("STW", "Updated a user")
                                        completableFuture.complete(true)
                                    },
                                    {
                                        Log.e("STW", "Update failed", it)
                                    }
                                )
                            }
                        }
                    },
                    { Log.e("STW", "Query failed", it) }
                )

            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }

        return completableFuture
    }

}
