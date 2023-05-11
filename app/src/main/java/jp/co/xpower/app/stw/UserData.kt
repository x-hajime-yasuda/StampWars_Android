package jp.co.xpower.app.stw

import com.amplifyframework.datastore.generated.model.Complete

data class UserData(var id: String, var name: String, var complete: List<Complete>)

/*
class UserData (
    var id:String,
    var name: String,
    var complete: List<complete>
)
*/