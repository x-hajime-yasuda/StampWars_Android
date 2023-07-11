package jp.co.xpower.cotamp.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageItem
import com.amplifyframework.storage.options.StoragePagedListOptions
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayList
import java.util.concurrent.CompletableFuture

class StorageViewModel : ViewModel() {

    companion object {
        const val IMAGE_DIR_RALLY = "rally"
        const val IMAGE_DIR_QR = "qr"
        const val IMAGE_DIR_REWARD = "reward"
    }

    /*
    * 保存先と対象ファイルのキーを指定してS3よりダウンロードする
    * file: 保存先
    * key: S3の対象ファイルのキー
    * */
    private fun downloadItem(file: File, item: String) {
        viewModelScope.launch {
            Amplify.Storage.downloadFile(item, file,
                {
                    Log.i("STW", "Successfully downloaded: ${it.file.name}")
                },
                {
                    Log.e("STW",  "Download Failure", it)
                }
            )
        }
    }

    /*
    * S3に無くてローカルにある画像を削除する
    * path: 対象のローカルディレクトリパス
    * list: チェック対象元のS3ファイルリスト
    */
    private fun clearImage(path: String, list:ArrayList<StorageItem>){
        val directory = File(path)
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                // ファイル名を取得
                val fileName = file.name.split("-")[0]
                val p = file.path

                // Listにファイル名が存在しない場合、ファイルを削除
                if (!list.any { it.key.split("/")[1] == fileName }) {
                    file.delete()
                }
            }
        }
    }

    /*
    * S3ストレージからのファイルダウンロード
    * path: ローカルストレージパス
    * type: S3ディレクトリ
    */
    fun imageDownload(path:String, type: String) :CompletableFuture<Boolean> {

        val completableFuture = CompletableFuture<Boolean>()

        viewModelScope.launch {
            try {
                val listFuture = getStorageItem("${type}/")

                CompletableFuture.allOf(listFuture).thenRun {
                    val list = listFuture.get()

                    // S3に無くてローカルにある画像を削除する
                    clearImage("${path}/${type}", list)

                    for( item in list){
                        // ラリー画像検索
                        val directory = File("${path}/${type}")

                        val matchingFiles = directory.listFiles { file ->
                            file.isFile && file.path.contains(item.key, ignoreCase = true)
                        }

                        // 画像ファイルが無ければダウンロード
                        if(matchingFiles.isEmpty()){
                            // ダウンロード
                            val imageFile = File("${path}/${item.key}-${item.lastModified.time}")
                            downloadItem(imageFile, item.key)
                        }
                        // 画像ファイルあり
                        else {
                            // タイムスタンプの比較
                            val orgName = item.key.split("/")[1] + "-" + item.lastModified.time.toString()
                            val localName = matchingFiles[0].name
                            // 一致していれば更新なし
                            if(orgName == localName){
                                // nothing
                                Log.i("STW", "nothing.")
                            }
                            // 差分があり
                            else {
                                // ローカルファイルを削除
                                matchingFiles[0].delete()
                                // ダウンロード
                                val imageFile = File("${path}/${item.key}-${item.lastModified.time}")
                                downloadItem(imageFile, item.key)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }

    /*
    * ダウンロード対象のリストを取得する
    * path: S3ストレージパス
    */
    private fun getStorageItem(path:String) :CompletableFuture<ArrayList<StorageItem>> {
        val options = StoragePagedListOptions.builder()
            .setPageSize(500)
            .build()

        var list:ArrayList<StorageItem> = ArrayList<StorageItem>()

        val completableFuture = CompletableFuture<ArrayList<StorageItem>>()

        viewModelScope.launch {
            try {
                Amplify.Storage.list(path, options,
                    { result ->
                        result.items.forEach { item ->
                            // ファイルではない場合スルー
                            if(item.size == 0L){
                                return@forEach
                            }
                            list.add(item)
                        }
                        completableFuture.complete(list)
                    },
                    { completableFuture.completeExceptionally(it) }
                )
            } catch (e: Exception) {
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }
}
