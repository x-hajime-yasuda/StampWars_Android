package jp.co.xpower.cotamp

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.StwCompany
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.co.xpower.cotamp.databinding.*
import jp.co.xpower.cotamp.model.CommonData
import jp.co.xpower.cotamp.model.CommonDataViewModel
import jp.co.xpower.cotamp.model.StorageViewModel
import jp.co.xpower.cotamp.R
import java.io.File
import javax.sql.CommonDataSource

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM = "param"

interface DialogDismissListener {
    fun onSelect(cnId:String, srId:String)
    fun onDialogDismissed(cnId:String, srId:String)
}

/**
 * A simple [Fragment] subclass.
 * Use the [RallyPublicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RallyPublicFragment : Fragment(), RallyClickListener, DialogDismissListener {
    // TODO: Rename and change types of parameters
    private var param: Int? = null
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
    private var searchWord : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param = it.getInt(ARG_PARAM)
        }
    }

    override fun onSelect(cnId:String, srId:String){

        val rally = rallyList.find { it.cnId == cnId && it.srId == srId }

        // 参加中表示に設定
        val data = commonDataViewModel.dataCommonDataById(cnId!!, srId!!)
        data!!.joinFlg = true

        rally!!.joined = true

        // まずは非選択に設定
        rallyList.forEach { it.selected = false }

        // 対象を選択済みにする
        rally!!.selected = true

        // 選択中のラリーを設定
        val pref = requireContext().getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        pref.edit().putString(MainActivity.PREF_KEY_SELECT_CN_ID, cnId).apply()
        pref.edit().putString(MainActivity.PREF_KEY_SELECT_SR_ID, srId).apply()

        // CommonDataに選択中ラリーを設定
        commonDataViewModel.select(cnId, srId)

        activity?.runOnUiThread {
            binding.recyclerView.adapter?.let { adapter ->
                if (adapter is ItemAdapter) {
                    //adapter.setData(rallyList)
                    //adapter.notifyDataSetChanged()

                    // MAP画面の選択状態を更新
                    val mainActivity = requireActivity() as MainActivity
                    //mainActivity.updateSelected()
                    mainActivity.updateUser()

                    // ボトムシートも閉じる
                    val fragmentManager = requireActivity().supportFragmentManager
                    val fragment = fragmentManager.findFragmentByTag("dialog") as? BottomSheetFragment
                    fragment?.dismiss()
                }
            }
        }
    }

    // ダイアログが閉じられたときに実行したい処理を記述
    override fun onDialogDismissed(cnId:String, srId:String) {
        Log.i("STW", "Auth session =")

        // 未参加の場合参加フラグを設定する
        val rally = rallyList.find { it.cnId == cnId && it.srId == srId }
        rally!!.joined = true

        // まずは非選択に設定
        rallyList.forEach { it.selected = false }
        // 対象を選択済みにする
        rally!!.selected = true

        // ViewModelも更新
        var cb:CommonData? = commonDataViewModel.commonDataList.find { it.cnId == cnId && it.srId == srId }
        cb!!.joinFlg = true

        // 選択中のラリーを設定
        val pref = requireContext().getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        pref.edit().putString(MainActivity.PREF_KEY_SELECT_CN_ID, cnId).apply()
        pref.edit().putString(MainActivity.PREF_KEY_SELECT_SR_ID, srId).apply()

        // CommonDataに選択中ラリーを設定
        commonDataViewModel.select(cnId, srId)


        activity?.runOnUiThread {
            binding.recyclerView.adapter?.let { adapter ->
                if (adapter is ItemAdapter) {
                    adapter.setData(rallyList)
                    adapter.notifyDataSetChanged()
                    Log.i("STW", "end2.`")

                    val mainActivity = requireActivity() as MainActivity
                    mainActivity.updateSelected()

                    // ボトムシートも閉じる
                    val fragmentManager = requireActivity().supportFragmentManager
                    val fragment = fragmentManager.findFragmentByTag("dialog") as? BottomSheetFragment
                    fragment?.dismiss()
                }
            }
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

    private val commonDataViewModel by lazy {
        ViewModelProvider(requireActivity())[CommonDataViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //populateStamp()

        val pref = requireContext().getSharedPreferences("STwPreferences", Context.MODE_PRIVATE)
        val selectCnId = pref.getString(MainActivity.PREF_KEY_SELECT_CN_ID, "")
        val selectSrId = pref.getString(MainActivity.PREF_KEY_SELECT_SR_ID, "")

        var l = ArrayList<CommonData>()
        if(param == MainActivity.RALLY_STATE_ALL){
            l = commonDataViewModel.commonDataList
        }
        else if(param == MainActivity.RALLY_STATE_END){
            l = commonDataViewModel.commonDataList.filter { it.state == MainActivity.RALLY_STATE_END } as ArrayList<CommonData>
        }
        else if(param == MainActivity.RALLY_STATE_JOIN){
            l = commonDataViewModel.commonDataList.filter { it.joinFlg } as ArrayList<CommonData>
        }

        // 部分一致検索
        if(!searchWord.isNullOrBlank()){
            l = l.filter {
                it.title?.matches(Regex(".*${searchWord}.*")) == true
            } as ArrayList<CommonData>
        }

        //for(list in commonDataViewModel.commonDataList){
        //for (list in commonDataViewModel.commonDataList.filter { it.state == MainActivity.RALLY_STATE_PUBLIC }) {
        for(list in l){
            var selected:Boolean = false
            if(selectCnId == list.cnId && selectSrId == list.srId){
                selected = true
            }
            var isVisible = commonDataViewModel.serverTime in list.displayStartAt!! .. list.displayEndAt!!

            var rally = Rally(
                list.cnId,
                list.srId,
                list.place,
                list.title,
                list.detail,
                list.rewardTitle,
                list.rewardDetail,
                R.drawable.no_image,
                joined = list.joinFlg,
                selected = selected,
                isVisible
            )
            rallyList.add(rally)
        }
        var rallyListVisible = rallyList.filter { it.isVisible || it.selected }

        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = ItemAdapter(rallyListVisible, this)
        binding.recyclerView.adapter?.notifyDataSetChanged()

        listener = parentFragment as? RecyclerViewListener ?: throw IllegalStateException("Parent must implement MyRecyclerViewListener")
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                listener.onRecyclerViewScrolled()
            }
        })

        val escapeList : List<Char> = arrayListOf('\\', '*', '+', '.', '?', '{', '}', '(', ')', '[', ']', '^', '$', '|')
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                 //text changed
                if(newText.isNullOrBlank()){
                    onQueryTextSubmit(newText)
                }
                return true
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                // submit button pressed
                rallyList.clear()
                searchWord = query
                for(e in escapeList){
                    searchWord = searchWord?.replace("$e", "\\$e")
                }
                onViewCreated(view, savedInstanceState)
                val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.list.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                binding.list.requestFocus()
                return true
            }
        })

        binding.search.setOnFocusChangeListener { v, hasFocus ->
            rallyList.clear()
            searchWord = (v as SearchView).query.toString()
            for(e in escapeList){
                searchWord = searchWord?.replace("$e", "\\$e")
            }
            onViewCreated(view, savedInstanceState)
        }
    }

    private inner class ViewHolder internal constructor(private val binding: FragmentRallyListDialogItemBinding, private val clickListener: RallyClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindRally(rally: Rally){
            if(rally.joined){
                binding.joined.visibility = View.VISIBLE
            }
            if(rally.selected){
                binding.selected.visibility = View.VISIBLE
            }
            else {
                binding.selected.visibility = View.INVISIBLE
            }

            // ローカルストレージからラリー画像のロード
            val imageName = "${rally.cnId}_${rally.srId}.png"
            //val dir = requireContext().filesDir
            val dir = File("${requireContext().filesDir.absolutePath}/${StorageViewModel.IMAGE_DIR_RALLY}")
            val matchingFiles = dir.listFiles { file ->
                file.isFile && file.path.contains(imageName, ignoreCase = true)
            }
            // 保存済み画像があればロード
            val hit:Int = matchingFiles.size
            if(hit != 0){
                val bitmap = BitmapFactory.decodeFile(matchingFiles[0].absolutePath)
                binding.cover.setImageBitmap(bitmap)
            }
            // 画像登録が無い場合はノーイメージを表示
            else {
                binding.cover.setImageResource(rally.cover)
            }

            binding.title.text = rally.title
            //binding.description.text = rally.description      // 一覧では不要
            binding.cardView.setOnClickListener{
                clickListener.onClick(rally)
            }
        }
    }
    private inner class ItemAdapter internal constructor(
        private var rallys: List<Rally>,
        private val clickListener: RallyClickListener
    ) :
        RecyclerView.Adapter<RallyPublicFragment.ViewHolder>() {

        fun setData(r:List<Rally>){
            rallys = r
        }

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
        val dialog = RallyDialogFragment.newInstance(this, rally.cnId, rally.srId)
        //val dialog = RallyDialogFragment()
        //dialog.dismissListener = this
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
        //fun newInstance(param1: ArrayList<StwCompany>) =
        fun newInstance(param: Int) =
            RallyPublicFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM, param)
                }
            }
    }
}