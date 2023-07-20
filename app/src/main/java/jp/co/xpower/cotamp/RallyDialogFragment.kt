package jp.co.xpower.cotamp

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.co.xpower.cotamp.AlarmReceiver.Companion.getNotificationId
import jp.co.xpower.cotamp.databinding.FragmentRallyDialogBinding
import jp.co.xpower.cotamp.model.CommonDataViewModel
import jp.co.xpower.cotamp.model.DataStoreViewModel
import jp.co.xpower.cotamp.model.StorageViewModel
import jp.co.xpower.cotamp.util.StwUtils
import jp.co.xpower.cotamp.R
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.CompletableFuture
import kotlin.math.pow


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1_CN_ID = "cnId"
private const val ARG_PARAM1_SR_ID= "srId"

/**
 * A simple [Fragment] subclass.
 * Use the [RallyDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RallyDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var cnId: String? = null
    private var srId: String? = null
    private lateinit var binding: FragmentRallyDialogBinding
    var dismissListener: DialogDismissListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cnId = it.getString(ARG_PARAM1_CN_ID)
            srId = it.getString(ARG_PARAM1_SR_ID)
        }
    }
    private val commonDataViewModel by lazy {
        ViewModelProvider(requireActivity())[CommonDataViewModel::class.java]
    }

    private val dataStoreViewModel by lazy {
        ViewModelProvider(requireActivity())[DataStoreViewModel::class.java]
    }


    private fun getOnlineImage(type: String) :Bitmap? {
        var bitmap: Bitmap? = null

        // ローカルストレージからラリー画像のロード
        val imageName = "${cnId}_${srId}.png"
        val dir = File("${requireContext().filesDir.absolutePath}/${type}")
        val matchingFiles = dir.listFiles { file ->
            file.isFile && file.path.contains(imageName, ignoreCase = true)
        }
        // 保存済み画像があればロード
        val hit:Int = matchingFiles.size
        if(hit != 0){
            bitmap = BitmapFactory.decodeFile(matchingFiles[0].absolutePath)
        }

        return bitmap
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRallyDialogBinding.inflate(layoutInflater)

        val data = commonDataViewModel.dataCommonDataById(cnId!!, srId!!)

        // ラリー画像
        var bitmapRally: Bitmap? = getOnlineImage(StorageViewModel.IMAGE_DIR_RALLY)
        if(bitmapRally != null){
            binding.imgMain.setImageBitmap(bitmapRally)
        }
        else {
            binding.imgMain.setImageResource(R.drawable.no_image)
        }

        // 景品画像
        var bitmapReward: Bitmap? = getOnlineImage(StorageViewModel.IMAGE_DIR_REWARD)
        if(bitmapReward != null){
            binding.imgReward.setImageBitmap(bitmapReward)
        }
        else {
            binding.imgReward.setImageResource(R.drawable.no_image)
        }

        // ラリータイトル
        binding.textTitle.text = data!!.title

        // ラリー詳細
        binding.textDescription.text = data!!.detail

        // 開催期間
        if(data!!.startAt != 0L && data!!.endAt != 0L){
            val startAt = StwUtils.formatUnixTime(data!!.startAt!!)
            val endAt = StwUtils.formatUnixTime(data!!.endAt!!)
            val textDate:String = binding.textDate.text.toString()
            binding.textDate.text = textDate + startAt + " - " + endAt
        }

        // 参加判定
        if(data!!.joinFlg){
            binding.imgJoin.visibility = View.VISIBLE
            binding.buttonJoin.text = resources.getString(R.string.button_select)
        }
        else {
            binding.buttonJoin.text = resources.getString(R.string.button_join)
        }

        // 開催場所
        if(data.place != null){
            val textPlace:String = binding.textPlace.text.toString()
            binding.textPlace.text = textPlace + data!!.place
        }

        // 報酬タイトル
        binding.textReward.text = data!!.rewardTitle

        // 選択・参加ボタン押下
        binding.buttonJoin.setOnClickListener {
            // 参加中のラリーの選択
            if(data!!.joinFlg){
                dismissListener?.onSelect(data.cnId, data.srId)
                dismiss()
            }
            // 未参加のラリーには参加する
            else {
                commonDataViewModel.select(cnId!!, srId!!)
                val completableFuture = dataStoreViewModel.rallyJoining(commonDataViewModel)
                CompletableFuture.allOf(completableFuture).thenRun {
                    dismissListener?.onSelect(cnId!!, srId!!)
                    dismiss()
                }

                // 通知を送る
                val calendar : Calendar = Calendar.getInstance()
                val date = Date(data!!.startAt!! * 1000)
                val content = getString(R.string.notification_content_rally_start, data!!.title)
                val title = getString(R.string.notification_title_rally_start)
                calendar.clear()
                calendar.time = date
                println("------------------- ${calendar.get(Calendar.YEAR)}年${calendar.get(Calendar.MONTH)+1}月${calendar.get(Calendar.DAY_OF_MONTH)}日　${calendar.get(Calendar.HOUR_OF_DAY)}時${calendar.get(Calendar.MINUTE)}分${calendar.get(Calendar.SECOND)}秒　${calendar.get(Calendar.MILLISECOND)}millisecond ------------------------")
                if(System.currentTimeMillis() < date.time){
                    rallyStartNotification(title, content, calendar)
                }
            }



            // 確認用
//            val content = getString(R.string.notification_content_rally_start, data!!.title)
//            val title = getString(R.string.notification_title_rally_start)
//            calendar.timeInMillis = System.currentTimeMillis()
//            calendar.add(Calendar.SECOND, 10)
//            rallyStartNotification(title, content, calendar)
        }

        binding.buttonClose.setOnClickListener {
            this.dismiss()
        }

        return binding.root
    }

    // 通知を送る
    private fun rallyStartNotification(title : String, content : String, calendar: Calendar) {
        val notificationIntent = Intent(this.requireContext(), AlarmReceiver::class.java)
        val notificationId = getNotificationId()
        println("-------------- $notificationId ------------------")
        notificationIntent.putExtra("title", title)
        notificationIntent.putExtra("content", content)
        notificationIntent.putExtra("notificationId", notificationId)
        notificationIntent.putExtra("channelId", AlarmReceiver.ChannelId.RALLY_START)
        notificationIntent.putExtra("cnId", cnId!!)
        notificationIntent.putExtra("srId", srId!!)
        val pendingIntent = PendingIntent.getBroadcast(
            this.requireContext(),
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager : AlarmManager = this.requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    MainActivity.PERMISSION_REQUEST_CODE
                )
            }
            return
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        isCancelable = true

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog
    }

    override fun onStart() {
        super.onStart()

        // ダイアログの幅を調整
        val width = resources.displayMetrics.widthPixels - 50
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.setLayout(width, height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnClickListener {
            Log.d(TAG, "background clicked")
            if (isCancelable) dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(listener:RallyPublicFragment, cnId: String, srId: String) =
            RallyDialogFragment().apply {
                dismissListener = listener
                arguments = Bundle().apply {
                    putString(ARG_PARAM1_CN_ID, cnId)
                    putString(ARG_PARAM1_SR_ID, srId)
                }
            }
    }
}