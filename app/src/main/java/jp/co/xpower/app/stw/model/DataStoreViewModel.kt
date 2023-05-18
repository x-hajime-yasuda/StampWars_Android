package jp.co.xpower.app.stw.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.CheckPoint
import com.amplifyframework.datastore.generated.model.Complete
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.CompletableFuture

class DataStoreViewModel : ViewModel() {

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
                                    // ラリーデータがあってチェックポイントしていない場合は既に参加済み
                                    if(complete.cp == null){
                                        Log.i("STW", "cp exist.")
                                        completableFuture.complete(true)
                                        return@query
                                    }

                                    val checkCp = complete.cp.find { it.cpId == cpId }
                                    if(checkCp != null){
                                        Log.i("STW", "cp exist.")
                                        completableFuture.complete(true)
                                        return@query
                                    }
                                    val cp:CheckPoint = CheckPoint.builder().cpId(cpId).build()
                                    // 対象のラリーにチェックポイントを追加
                                    complete.cp.add(cp)
                                }
                                // 新規ラリー達成
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

    /*
    fun updateRally(id:String, cnId:String, srId:String, cpId:String){
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
                            .build()

                        Amplify.DataStore.save(edited,
                            { Log.i("STW", "Updated a user") },
                            { Log.e("STW", "Update failed", it) }
                        )
                    }
                }
            },
            { Log.e("STW", "Query failed", it) }
        )
    }
    */

}
