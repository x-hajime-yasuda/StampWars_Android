package jp.co.xpower.app.stw

import android.app.Dialog
import android.content.ContentValues.TAG
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
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import jp.co.xpower.app.stw.databinding.FragmentRallyDialogBinding
import jp.co.xpower.app.stw.model.CommonData
import jp.co.xpower.app.stw.model.CommonDataViewModel
import jp.co.xpower.app.stw.model.DataStoreViewModel
import jp.co.xpower.app.stw.util.StwUtils
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRallyDialogBinding.inflate(layoutInflater)

        val data = commonDataViewModel.dataCommonDataById(cnId!!, srId!!)

        val identityId = commonDataViewModel.identityId

        // ラリータイトル
        binding.textTitle.text = data!!.title

        // ラリー詳細
        binding.textDescription.text = data!!.detail

        // 開催期間
        if(data!!.startAt != 0L && data!!.endAt != 0L){
            val startAt = StwUtils.formatUnixTime(data!!.startAt!!)
            val endAt = StwUtils.formatUnixTime(data!!.endAt!!)
            val textDate:String = binding.textDate.text.toString()
            binding.textDate.text = textDate + startAt + "-" + endAt
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

        if(data!!.completeFlg){
            binding.buttonJoin.isEnabled = false
            // 非活性
            binding.buttonJoin.setBackgroundResource(R.drawable.button_gray)
        }


        binding.buttonJoin.setOnClickListener {


            //val completableFuture = dataStoreViewModel.updateAsyncTask(identityId, "c0004", "s0001", "p0005")
            val completableFuture = dataStoreViewModel.updateAsyncTask(identityId, data.cnId, data.srId, "")
            CompletableFuture.allOf(completableFuture).thenRun {
                Log.i("STW", "Updated a ....")
                dismiss()
            }


            /*
            val textColor = resources.getColor(R.color.white, requireContext().theme)
            val backgroundColor = resources.getColor(R.color.list_background_color, requireContext().theme)

            val snackBar = Snackbar.make(requireView(), "参加しました。", Snackbar.LENGTH_SHORT)
            snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                    super.onShown(transientBottomBar)
                }
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    dismiss()
                }
            })
            snackBar.view.setBackgroundColor(backgroundColor);
            snackBar.setTextColor(textColor)
            snackBar.show()
            */
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
        fun newInstance(cnId: String, srId: String) =
            RallyDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1_CN_ID, cnId)
                    putString(ARG_PARAM1_SR_ID, srId)
                }
            }
    }
}