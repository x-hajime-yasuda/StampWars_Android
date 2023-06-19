package jp.co.xpower.cotamp

import com.amplifyframework.datastore.generated.model.Complete

data class UserData(var id: String, var name: String, var complete: List<Complete>)
