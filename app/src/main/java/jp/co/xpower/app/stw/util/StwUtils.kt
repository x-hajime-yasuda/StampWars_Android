package jp.co.xpower.app.stw.util

import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo


const val NTP_SERVER = "pool.ntp.org"

class StwUtils {
    companion object {
        fun formatUnixTime(unixTime: Long): String {
            //val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault())
            //val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val date = Date(unixTime * 1000)
            return sdf.format(date)
        }


        fun getNtpTime(): Long {
            val client = NTPUDPClient()
            client.open()

            try {
                val info: TimeInfo = client.getTime(InetAddress.getByName(NTP_SERVER))
                info.computeDetails() // 詳細情報を計算

                val ntpTime = info.message.transmitTimeStamp.time
                return ntpTime
            } finally {
                client.close()
            }
        }


    }
}
