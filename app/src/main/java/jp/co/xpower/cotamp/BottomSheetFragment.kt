package jp.co.xpower.cotamp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.viewpager2.widget.ViewPager2
import com.amplifyframework.datastore.generated.model.Complete.SrIdStep
import com.amplifyframework.datastore.generated.model.StwCompany
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import jp.co.xpower.cotamp.R
import jp.co.xpower.cotamp.databinding.BottomSheetParentBinding

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ItemListDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class BottomSheetFragment : BottomSheetDialogFragment(), RecyclerViewListener {

    private var _binding: BottomSheetParentBinding? = null

    private val binding get() = _binding!!

    private var behavior: BottomSheetBehavior<View>? = null

    private lateinit var companyList: ArrayList<StwCompany>

    override fun onRecyclerViewScrolled() {
        // RecyclerViewがスクロールされたときに呼ばれる処理
        val behavior = (view?.parent as? View)?.let {
            BottomSheetBehavior.from(it)
        }
    }

    private var cnId : String? = null
    private var srId : String? = null

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val param = arguments?.getInt(ARG_ITEM_COUNT)
        _binding = BottomSheetParentBinding.inflate(inflater, container, false)

        binding.layout.bottomSheet.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.layout.bottomSheet.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // BottomSheetのPeekHeightにviewの高さを設定
                val parent = view!!.parent as View
                behavior = BottomSheetBehavior.from(parent)
                behavior!!.peekHeight = view!!.measuredHeight
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // paramで詳細などデータを連携する
        val adapter = TabPagerAdapter(companyList, childFragmentManager, lifecycle)

        binding.layout.viewPager.adapter = adapter

        val tabTitles = listOf(
            resources.getString(R.string.tab_in_all),
            resources.getString(R.string.tab_joining),
            resources.getString(R.string.tab_ended)
        )

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        // コールバックを設定する
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // BottomSheetが折りたたまれた時にはコンテンツに合わせて高さを変更
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    val params = bottomSheet.layoutParams.height
                    // nothing
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val params = bottomSheet.layoutParams.height
                // nothing
            }
        })

        // BottomSheetを表示する
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val initialTab = arguments?.getInt(ARG_ITEM_COUNT) ?: 0
        binding.layout.viewPager.setCurrentItem(initialTab, false)

        TabLayoutMediator(binding.layout.tabs, binding.layout.viewPager) { tab, position ->
            // タブにタイトルを設定する
            tab.text = tabTitles[position]

        }.attach()

        binding.layout.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // nothing
            }
        })

        // 通知をタップして開いたとき
        if(cnId != null && srId != null){
            val dialog = RallyDialogFragment.newInstance(adapter.fragment, cnId!!, srId!!)
            dialog.show(parentFragmentManager, "custom_dialog")
        }
    }

    companion object {
        // MainActivityから渡されるパラメータ
        fun newInstance(itemCount: ArrayList<StwCompany>, cnId : String?, srId : String?): BottomSheetFragment =
            BottomSheetFragment().apply {
                companyList = itemCount
                this.cnId = cnId
                this.srId = srId
                arguments = Bundle().apply {
                    //putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

        fun newInstance(itemCount: ArrayList<StwCompany>): BottomSheetFragment =
            BottomSheetFragment().apply {
                companyList = itemCount
                this.cnId = null
                this.srId = null
                arguments = Bundle().apply {
                    //putInt(ARG_ITEM_COUNT, itemCount)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
