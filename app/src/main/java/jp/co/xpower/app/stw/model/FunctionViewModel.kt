package jp.co.xpower.app.stw.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageItem
import com.amplifyframework.storage.options.StoragePagedListOptions
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.ArrayList
import java.util.concurrent.CompletableFuture

class FunctionViewModel : ViewModel() {

    companion object {
        const val FUNCTION_QR = "qr"
        const val FUNCTION_TIME = "time"
    }

    /*
    * Lambdaを実行
    * */
    fun callFunction(type: String, param: String) :CompletableFuture<String> {
        val completableFuture = CompletableFuture<String>()

        viewModelScope.launch {
            try {
                Amplify.API.query(getRequest(type, param),
                    { it ->
                        val data = it.data as String
                        completableFuture.complete(data)
                    },
                    {
                        completableFuture.completeExceptionally(it)
                    }
                )
            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }

    private fun getRequest(type: String, param: String): GraphQLRequest<String> {
        var request: GraphQLRequest<String>? = null

        if(type == FUNCTION_QR ){
            request = qrRequest(param)
        }
        else if(type == FUNCTION_TIME ){
            request = serverTimeRequest()
        }
        return request!!
    }

    /*
    * デコードQR取得ファンクション
    *
    */
    private fun qrRequest(text:String): GraphQLRequest<String> {
        val document = ("query getQRDecode(\$qr: String) { "
                + "getQRDecode(qr: \$qr) "
                + "}")
        return SimpleGraphQLRequest(
            document,
            mapOf("qr" to text),
            String::class.java,
            GsonVariablesSerializer()
        )
    }

    /*
    * サーバータイム取得ファンクション
    *
    */
    private fun serverTimeRequest(): GraphQLRequest<String> {
        val document = ("query getServerTime { "
                + "getServerTime "
                + "}")
        return SimpleGraphQLRequest(
            document,
            mapOf("id" to "abc"),
            String::class.java,
            GsonVariablesSerializer())
    }
}
