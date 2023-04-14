package jp.co.xpower.app.stw

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.xpower.app.stw.databinding.ActivityMainBinding
import jp.co.xpower.app.stw.databinding.RallyListBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*
import kotlin.collections.ArrayList


class BottomSheetFragment : BottomSheetDialogFragment(), RallyClickListener {

    private var _binding: RallyListBinding? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val binding get() = _binding!!

    private fun populateRally(){
        var rally = Rally(
            R.drawable.randoseru1,
            "学園祭-01",
            "開催日未定"
        )
        rallyList.add(rally)

        var rally2 = Rally(
            R.drawable.randoseru1,
            "学園祭-02",
            "開催日未定"
        )
        rallyList.add(rally2)

        var rally3 = Rally(
            R.drawable.randoseru1,
            "学園祭-03",
            "開催日未定"
        )
        rallyList.add(rally3)

        var rally4 = Rally(
            R.drawable.randoseru1,
            "学園祭-04",
            "開催日未定"
        )
        rallyList.add(rally4)

        var rally5 = Rally(
            R.drawable.randoseru1,
            "学園祭-05",
            "開催日未定"
        )
        rallyList.add(rally5)
    }

    public fun clearList(){
        rallyList.clear()
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RallyListBinding.inflate(inflater, container, false)

        populateRally()

        var fragment = this
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = RallyAdapter(rallyList, fragment)
        }

        /*
        binding.button.setOnClickListener{
            //
            Log.i("STW","aa")
        }
        */

        /*
        val rLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerView.layoutManager = rLayoutManager

        val dataSet: ArrayList<String> = arrayListOf()
        var i = 0
        while (i < 20) {
            val str: String = java.lang.String.format(Locale.US, "Data_0%d", i)
            dataSet.add(str)
            i++
        }

        binding.recyclerView.adapter = MyAdapter(dataSet)
        */




        return binding.root
    }

    //override fun getTheme(): Int {
    //    return R.style.MyModalStyle
    //}


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        /*
        val view = View.inflate(requireContext(), R.layout.fragment_bottom_sheet, null)
        dialog.setContentView(view)

        val bottomSheetDialog = BottomSheetDialog(requireContext(), theme)

        //dialog.window?.setLayout(
        //    ViewGroup.LayoutParams.MATCH_PARENT,
        //    ViewGroup.LayoutParams.MATCH_PARENT
        //)
        var bind = FragmentBottomSheetBinding.inflate(layoutInflater)

        //_binding = FragmentBottomSheetBinding.inflate(requireActivity().layoutInflater)
        //val d = Dialog(requireContext())
        //d.setContentView(binding.root)


        bind.recyclerView.setHasFixedSize(true)

        */




        /*
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        var behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = screenHeight
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        */

        //BottomSheetBehavior.from(binding.root).peekHeight = screenHeight
        return dialog
    }



    companion object{
        const val TAG = "BottomSheet"
    }

    override fun onClick(rally: Rally) {

        val behavior = BottomSheetBehavior.from(binding.root.parent as View)

        binding.recyclerView.visibility = View.GONE
        binding.layout.visibility = View.VISIBLE

        val vto = binding.layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // レイアウトが完了したらGlobalLayoutListenerを削除
                binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // レイアウトが完了した時点でのview2の高さを設定
                behavior.peekHeight = binding.layout.measuredHeight
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        })




            //binding.detail.layout.visibility = View.VISIBLE

        //var intent = Intent(requireContext(), DetailActivity::class.java)
        //intent.putExtra(RALLY_ID_EXTRA, rally.id)
        //startActivity(intent)
    }
}
