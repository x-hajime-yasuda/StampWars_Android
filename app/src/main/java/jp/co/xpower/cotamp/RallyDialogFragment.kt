package jp.co.xpower.cotamp

import android.app.Dialog
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.co.xpower.cotamp.databinding.FragmentRallyDialogBinding
import jp.co.xpower.cotamp.model.CommonDataViewModel
import jp.co.xpower.cotamp.model.DataStoreViewModel
import jp.co.xpower.cotamp.model.StorageViewModel
import jp.co.xpower.cotamp.util.StwUtils
import jp.co.xpower.cotamp.R
import java.io.File
import java.util.concurrent.CompletableFuture


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


        // 開催期間チェック
        if(data!!.startAt != null && data!!.endAt != null){
            if(commonDataViewModel.serverTime in data.startAt!!..data.endAt!!){
            }
            else {
                binding.buttonJoin.setBackgroundResource(R.drawable.button_gray)
                binding.buttonJoin.isEnabled = false
            }
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
            }
        }

        binding.buttonClose.setOnClickListener {
            this.dismiss()
        }

        return binding.root
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