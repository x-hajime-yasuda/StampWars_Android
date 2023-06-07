package jp.co.xpower.app.stw

import java.io.File
import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.storage.s3.AWSS3StoragePlugin

class AmplifyApp: Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            // 各画像を保存するディレクトリの作成
            createDir()

            // Amplify初期設定
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(applicationContext)
            Log.i("STW", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("STW", "Could not initialize Amplify", error)
        } catch (error: java.lang.Exception){
            Log.e("STW", "General Exception", error)
        }
    }

    /*
    * 各画像を保存するディレクトリの作成
    */
    private fun createDir(){
        val rallyDir = File(filesDir, "rally")
        if (!rallyDir.exists()) {
            rallyDir.mkdir()
        }
        val rewardDir = File(filesDir, "reward")
        if (!rewardDir.exists()) {
            rewardDir.mkdir()
        }
    }
}