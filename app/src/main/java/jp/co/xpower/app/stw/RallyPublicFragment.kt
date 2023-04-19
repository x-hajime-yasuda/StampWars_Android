package jp.co.xpower.app.stw

//import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.co.xpower.app.stw.databinding.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RallyPublicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RallyPublicFragment : Fragment(), RallyClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var _binding: FragmentRallyPublicBinding? = null
    private val binding get() = _binding!!

    private var behavior: BottomSheetBehavior<View>? = null

    //private lateinit var bottomSheet :BottomSheetFragment
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var rallyBinding: FragmentRallyListDialogBinding
    private lateinit var detailBinding: FragmentRallyListDialogItemBinding
    private lateinit var bottomBinding: BottomSheetParentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    fun setBottomSheetBehavior(b:BottomSheetBehavior<View>){
        behavior = b
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRallyPublicBinding.inflate(inflater, container, false)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        rallyBinding = FragmentRallyListDialogBinding.inflate(layoutInflater)
        bottomBinding = BottomSheetParentBinding.inflate(layoutInflater)


        detailBinding = FragmentRallyListDialogItemBinding.inflate(layoutInflater)

        //val behavior = BottomSheetBehavior.from<View>(binding.root.parent as View)
        //val behavior = BottomSheetBehavior.from<View>(bottomBinding.layout.bottomSheet)

        binding.buttonBack.setOnClickListener {
            binding.list.visibility = View.VISIBLE
            binding.detail.visibility = View.INVISIBLE
        }




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateStamp()
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = ItemAdapter(rallyList, this)
    }

    private fun populateStamp() {
        for (i in 1..9) {
            var rally = Rally(
                R.drawable.randoseru1,
                "学園祭 - %d".format(i),
                "報酬未定\n報酬未定\n報酬未定\n"
            )
            rallyList.add(rally)
        }
    }
//                "開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定\n開催日未定"

    private inner class ViewHolder internal constructor(private val binding: FragmentRallyListDialogItemBinding, private val clickListener: RallyClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindRally(rally: Rally){
            binding.cover.setImageResource(rally.cover)
            binding.title.text = rally.title
            binding.description.text = rally.description
            binding.cardView.setOnClickListener{
                clickListener.onClick(rally)
            }
        }
    }
    private inner class ItemAdapter internal constructor(
        private val rallys: List<Rally>,
        private val clickListener: RallyClickListener
    ) :
        RecyclerView.Adapter<RallyPublicFragment.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                FragmentRallyListDialogItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                ),
                clickListener
            )
        }

        override fun onBindViewHolder(holder: RallyPublicFragment.ViewHolder, position: Int) {
            holder.bindRally(rallys[position])
        }

        override fun getItemCount(): Int {
            return rallys.size
            //return mItemCount
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(rally: Rally) {
        //val behavior2 = BottomSheetBehavior.from(mainBinding.root as View)
        //val behavior1 = BottomSheetBehavior.from(rallyBinding.root as View)

        //mainBinding.bottomSheetContainer.visibility = View.GONE

        //val mainActivity = activity as MainActivity
        //mainActivity.visibleChange()
        //var behavior = (activity as MainActivity).getBottomSheetBehavior()
        //behavior!!.state = BottomSheetBehavior.STATE_HIDDEN

        //val layoutParams2 = binding.list.layoutParams
        //layoutParams2.height = binding.list.computeVerticalScrollRange()
        //binding.list.layoutParams = layoutParams2


        //(requireActivity() as MainActivity).visibleChange()


        //exampleBinding = FragmentExampleBinding.inflate(layoutInflater)

        //val overlayLayout = LayoutInflater.from(context).inflate(R.layout.fragment_example, null)
        val overlayLayout = LayoutInflater.from(context).inflate(R.layout.fragment_example, null)

        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val parentLayout = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        //parentLayout.addView(overlayLayout, layoutParams)

        val a1 = bottomBinding.root
        val a2 = bottomBinding.root.parent
        val a3 = mainBinding.root
        val a4 = mainBinding.root.parent
        val ba = bottomBinding.layout.bottomSheet
        val bb = bottomBinding.layout.bottomSheet.parent

        //val b1 = binding.list
        //val b12 = binding.list.parent
        //val b123 = binding.list.parent.parent

        val b2 = binding.root.parent
        val b3 = binding.root.parent.parent
        val b4 = binding.root.parent.parent.parent
        val b5 = binding.root.parent.parent.parent.parent
        val b6 = binding.root.parent.parent.parent.parent.parent

        //val bottomSheetBehavior = BottomSheetBehavior.from(requireView().findViewById(R.id.bottom_sheet))
        //val behavior1 = BottomSheetBehavior.from(a1.parent as View)
        //val behavior1 = BottomSheetBehavior.from(b6 as View)
        //val bbb = bottomBinding.layout.bottomSheet.parent

        /*
        a3.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                a3.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // BottomSheetのPeekHeightにviewの高さを設定
                val parent = view!!.parent as View
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(parent)
                behavior.peekHeight = ((view!!.measuredHeight).toDouble() * 0.65).toInt()

            }
        })
        */

        binding.list.visibility = View.INVISIBLE
        binding.detail.visibility = View.VISIBLE

        //(b4 as View).visibility = View.GONE
        //(b5 as View).visibility = View.GONE
        //binding.list.visibility = View.GONE



        //behavior!!.state = BottomSheetBehavior.STATE_HIDDEN


        //parentLayout.addView(detailBinding.container, layoutParams)
        //overlayLayout.bringToFront()
        //parentLayout.requestLayout()

        // bottom_sheet_container
        //mainBinding.bottomSheetContainer.visibility = View.INVISIBLE

        //val decorView = requireActivity().window.decorView as ViewGroup
        //decorView.addView(mainBinding.overlayLayout, layoutParams)








        /*
        var behavior = (requireActivity() as MainActivity).getBottomSheetBehavior()

        binding.list.visibility = View.GONE
        binding.layout.visibility = View.VISIBLE

        val vto = binding.layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // レイアウトが完了したらGlobalLayoutListenerを削除
                binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // レイアウトが完了した時点でのview2の高さを設定
                //behavior!!.peekHeight = binding.layout.measuredHeight
                behavior!!.state = BottomSheetBehavior.STATE_EXPANDED

                binding.cover.setImageResource(rally.cover)
                binding.title.text = rally.title
                binding.description.text = rally.description
            }
        })
        */


        //val c1 = mainBinding.root
        //val c2 = mainBinding.root.parent

        //val view = requireActivity().findViewById<View>(R.id.bo)
        //val fcvLand2 = requireActivity().findViewById<View>(R.id.bottom_sheet)
        //val bottomSheetBehavior = BottomSheetBehavior.from(fcvLand2)
        //val behavior2 = BottomSheetBehavior.from(mainBinding.root)
        //val behavior = BottomSheetBehavior.from(binding.root as View)

        /*
        binding.list.visibility = View.GONE
        binding.layout.visibility = View.VISIBLE

        val vto = binding.layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // レイアウトが完了したらGlobalLayoutListenerを削除
                binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // レイアウトが完了した時点でのview2の高さを設定
                behavior.peekHeight = binding.layout.measuredHeight
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                binding.cover.setImageResource(rally.cover)
                binding.title.text = rally.title
                binding.description.text = rally.description
            }
        })
        */
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment RallyPublicFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            RallyPublicFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}