package jp.co.xpower.app.stw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.StwCompany
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.co.xpower.app.stw.databinding.*


// TODO: Rename parameter arguments, choose names that match
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
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var rallyBinding: FragmentRallyListDialogBinding
    private lateinit var detailBinding: FragmentRallyListDialogItemBinding
    private lateinit var bottomBinding: BottomSheetParentBinding
    private lateinit var listener: RecyclerViewListener
    private lateinit var companyList: ArrayList<StwCompany>
    private var rallyList = mutableListOf<Rally>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //populateStamp()
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = ItemAdapter(rallyList, this)

        listener = parentFragment as? RecyclerViewListener ?: throw IllegalStateException("Parent must implement MyRecyclerViewListener")
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                listener.onRecyclerViewScrolled()
            }
        })

    }

    private fun populateStamp() {
        for (i in 1..9) {
            var rally = Rally(
                R.drawable.rally,
                "学園祭 - %d".format(i),
                "報酬未定\n報酬未定\n報酬未定\n"
            )
            rallyList.add(rally)
        }
    }

    private inner class ViewHolder internal constructor(private val binding: FragmentRallyListDialogItemBinding, private val clickListener: RallyClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindRally(rally: Rally){
            binding.cover.setImageResource(rally.cover)
            binding.title.text = rally.title
            //binding.description.text = rally.description      // 一覧では不要
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

        // ラリー詳細表示
        val dialog = RallyDialogFragment()
        dialog.show(parentFragmentManager, "custom_dialog")
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
        fun newInstance(param1: ArrayList<StwCompany>) =
            RallyPublicFragment().apply {


                rallyList = mutableListOf<Rally>()

                for(s in param1){
                    println(s.id)
                    var rally = Rally(
                        R.drawable.rally,
                        s.name,
                        "報酬未定\n報酬未定\n報酬未定\n"
                    )
                    rallyList.add(rally)
                }
                /*
                for (i in 1..9) {
                    var rally = Rally(
                        R.drawable.rally,
                        "学園祭 - %d".format(i),
                        "報酬未定\n報酬未定\n報酬未定\n"
                    )
                    rallyList.add(rally)
                }
                */

                arguments = Bundle().apply {
                    //putString(ARG_PARAM1, param1)
                }
            }
    }
}