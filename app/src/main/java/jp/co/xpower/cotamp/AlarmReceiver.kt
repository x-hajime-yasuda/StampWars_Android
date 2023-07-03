package jp.co.xpower.cotamp

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import aws.sdk.kotlin.services.s3.model.ReplicationStatus
import kotlin.math.pow

class AlarmReceiver : BroadcastReceiver() {
    object ChannelId{
        val RALLY_START = "CHANNEL_ID_RALLY_START"
    }

    object ChannelName{
        val RALLY_START = "ラリー開始時間が近くなったら通知"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val notificationId = intent.getIntExtra("notificationId", 0)
        val channelId = intent.getStringExtra("channelId")!!
        val cnId = intent.getStringExtra("cnId")!!
        val srId = intent.getStringExtra("srId")!!

        val intent2 = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        intent2.putExtra("cnId", cnId)
        intent2.putExtra("srId", srId)
        val pendingIntent = PendingIntent.getActivity(context, col2int("$cnId$srId"), intent2, PendingIntent.FLAG_IMMUTABLE)

        var builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.stamp_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId, builder.build())
        }
    }

    private fun col2int(str : String) : Int{
        val chars = "0123456789abcdefghijklmnopqrstuvwxyz"
        val cl = chars.length
        val sl = str.length
        var ret = 0
        var i = 0
        while(i < sl){
            ret += (cl.toDouble().pow(i) * chars.indexOf(str[i++])).toInt()
        }

        return ret
    }
}