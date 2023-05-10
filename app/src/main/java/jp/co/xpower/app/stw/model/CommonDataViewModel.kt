package jp.co.xpower.app.stw.model

import androidx.lifecycle.ViewModel
import com.amplifyframework.datastore.generated.model.CheckPoint
import com.amplifyframework.datastore.generated.model.StwCompany
import com.amplifyframework.datastore.generated.model.StwUser
import java.util.ArrayList

class CommonDataViewModel : ViewModel() {

    lateinit var companyList:ArrayList<StwCompany>
    lateinit var userList:ArrayList<StwUser>

    fun countCommonDataById(cnId: String, srId: String): Int {
        return commonDataList.count { it.cnId == cnId && it.srId == srId }
        //return commonDataList.find { it.cnId == cnId && it.srId == srId }
    }

    val commonData = CommonData()
    val commonDataList = ArrayList<CommonData>()
}


data class CommonData(
    var cnId: String = "",
    var srId: String = "",
    var srTitle: String = "",
    var srState: Int = 0,
    var srCp: ArrayList<CheckPoint> = ArrayList<CheckPoint>(),
    var joinFlg: Boolean = false,
    var completeFlg: Boolean = false
)
