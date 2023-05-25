package jp.co.xpower.app.stw

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.datastore.DataStoreConfiguration
import com.amplifyframework.datastore.generated.model.StwUser

class AmplifyApp: Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.configure(applicationContext)
            Log.i("STW", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("STW", "Could not initialize Amplify", error)
        }
    }
}