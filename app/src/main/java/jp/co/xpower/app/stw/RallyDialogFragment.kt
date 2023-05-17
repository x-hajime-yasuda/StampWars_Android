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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RallyDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RallyDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentRallyDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    private val commonDataViewModel by lazy {
        ViewModelProvider(requireActivity())[CommonDataViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRallyDialogBinding.inflate(layoutInflater)

        binding.buttonJoin.setOnClickListener {


            //Log.d(TAG, commonDataViewModel.commonData.message)
            //commonDataViewModel.commonData.message = "ok."


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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RallyDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RallyDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}