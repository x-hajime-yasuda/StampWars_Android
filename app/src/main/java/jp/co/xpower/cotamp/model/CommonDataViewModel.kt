package jp.co.xpower.cotamp.model

import androidx.lifecycle.ViewModel
import com.amplifyframework.datastore.generated.model.CheckPoint
import com.amplifyframework.datastore.generated.model.Complete
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import java.util.ArrayList

class CommonDataViewModel : ViewModel() {

    lateinit var companyList:ArrayList<StwCompany>
    lateinit var userList:ArrayList<StwUser>

    fun countCommonDataById(cnId: String, srId: String): Int {
        return commonDataList.count { it.cnId == cnId && it.srId == srId }
    }
    fun dataCommonDataById(cnId: String, srId: String): CommonData? {
        return commonDataList.find { it.cnId == cnId && it.srId == srId }
    }
    fun dataCommonData(): CommonData? {
        return commonDataList.find { it.cnId == selectCnId && it.srId == selectSrId }
    }

    // 選択中の会社IDとラリーID
    var selectId:Pair<String, String> = Pair("", "")

    var commonDataList = ArrayList<CommonData>()

    var selectCnId:String = ""
    var selectSrId:String = ""

    fun select(cnId: String, srId: String){
        selectCnId = cnId
        selectSrId = srId
        //selected = commonDataList.find { it.cnId == cnId && it.srId == srId }
    }

    // ユーザーID
    var identityId = ""
    // 選択中データ
    var selected:CommonData? = null
    // サーバータイム
    var serverTime: Long = 0L
}


data class CommonData(
    var cnId: String = "",
    var srId: String = "",
    var place: String? = "",
    var title: String? = "",
    var detail: String? = "",
    var rewardTitle: String? = "",
    var rewardDetail: String? = "",
    var rewardUrl: String? = null,
    var startAt:Long? = 0,
    var endAt:Long? = 0,
    var displayStartAt:Long? = 0,
    var displayEndAt:Long? = 0,
    var state: Int = 0,
    var cp: ArrayList<CheckPoint> = ArrayList<CheckPoint>(),
    var complete: Complete? = null,
    var joinFlg: Boolean = false,
    var completeFlg: Boolean = false,
    var got: Boolean = false,
    var isLocationAvailable: Boolean = false,
    var isKeywordAvailable: Boolean = false,
    var maxRadius: Int = 10
)
