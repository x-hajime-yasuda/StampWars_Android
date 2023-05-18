package jp.co.xpower.app.stw.util

import java.text.SimpleDateFormat
import java.util.*

class StwUtils {
    companion object {
        fun formatUnixTime(unixTime: Long): String {
            //val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault())
            val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            val date = Date(unixTime * 1000)
            return sdf.format(date)
        }
    }
}
