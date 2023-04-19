package jp.co.xpower.app.stw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import jp.co.xpower.app.stw.databinding.BottomSheetParentBinding

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
class BottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetParentBinding? = null

    private val binding get() = _binding!!

    private var behavior: BottomSheetBehavior<View>? = null

    override fun onStart() {
        super.onStart()
        /*
        val dialog = dialog as BottomSheetDialog
        val behavior = dialog.behavior

        val displayMetrics = requireContext().resources.displayMetrics
        val windowHeight = displayMetrics.heightPixels
        behavior.peekHeight = (windowHeight * 0.6).toInt()
        */
        /*
        view?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                val parentHeight = (view?.parent as View).height // BottomSheet の高さは親 View の高さに基づいて計算されるため、親 View の高さを取得する
                val peekHeight = (parentHeight * 0.6).toInt() // 画面の 60% を peekHeight に設定する
                dialog?.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
                    val behavior = BottomSheetBehavior.from(bottomSheet)
                    behavior.peekHeight = peekHeight
                }
            }
        })
        */

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

                val peekHeight = (resources.displayMetrics.heightPixels * 0.66 + resources.getDimensionPixelSize(R.dimen.list_item_spacing_half) * 2).toInt()

                //behavior!!.peekHeight = ((view!!.measuredHeight).toDouble() * 0.65).toInt()
                behavior!!.peekHeight = view!!.measuredHeight
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val adapter = TabPagerAdapter(childFragmentManager, lifecycle)

        binding.layout.viewPager.adapter = adapter

        val tabTitles = listOf(
            resources.getString(R.string.tab_in_session),
            resources.getString(R.string.tab_joining),
            resources.getString(R.string.tab_history),
            resources.getString(R.string.tab_reward)
        )

        /*
        for (i in tabTitles.indices) {
            binding.layout.tabs.addTab(binding.layout.tabs.newTab().setText(tabTitles[i]))
        }

        val initialTab = arguments?.getInt(ARG_ITEM_COUNT) ?: 0
        binding.layout.viewPager.currentItem = initialTab

        binding.layout.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.layout.viewPager.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.layout.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.layout.tabs.selectTab(binding.layout.tabs.getTabAt(position))
            }
        })
        */

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        // コールバックを設定する
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // BottomSheetが折りたたまれた時にはコンテンツに合わせて高さを変更
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    val params = bottomSheet.layoutParams.height
                    //layoutParams.height = params
                    //bottomSheet.layoutParams = layoutParams
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val params = bottomSheet.layoutParams.height
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
                // BottomSheetを閉じる
                //behavior!!.state = BottomSheetBehavior.STATE_HIDDEN
            }
        })
    }

    companion object {
        // TODO: Customize parameters
        fun newInstance(itemCount: Int): BottomSheetFragment =
            BottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}