package jp.co.xpower.app.stw.model

import androidx.lifecycle.ViewModel
import com.amplifyframework.datastore.generated.model.CheckPoint
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import java.util.ArrayList

class CommonDataViewModel : ViewModel() {

    lateinit var companyList:ArrayList<StwCompany>
    lateinit var userList:ArrayList<StwUser>
    //lateinit var commonDataList:ArrayList<CommonData>

    fun countCommonDataById(cnId: String, srId: String): Int {
        return commonDataList.count { it.cnId == cnId && it.srId == srId }
        //return commonDataList.find { it.cnId == cnId && it.srId == srId }
    }
    fun dataCommonDataById(cnId: String, srId: String): CommonData? {
        return commonDataList.find { it.cnId == cnId && it.srId == srId }
    }

    //val commonData = CommonData()
    var commonDataList = ArrayList<CommonData>()

    var identityId = ""
}


data class CommonData(
    var cnId: String = "",
    var srId: String = "",
    var place: String? = "",
    var title: String? = "",
    var detail: String? = "",
    var rewardTitle: String? = "",
    var rewardDetail: String? = "",
    var startAt:Long? = 0,
    var endAt:Long? = 0,
    var state: Int = 0,
    var cp: ArrayList<CheckPoint> = ArrayList<CheckPoint>(),
    var joinFlg: Boolean = false,
    var completeFlg: Boolean = false
)
