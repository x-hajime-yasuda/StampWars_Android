package jp.co.xpower.app.stw

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior


class TabPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments: ArrayList<Fragment> = arrayListOf(
        RallyPublicFragment(),
        RallyPublicFragment.newInstance("a"),
        BlankFragment.newInstance("11", "a22"),
        RewardFragment.newInstance("11", "a22")
    )

    override fun getItemCount() : Int = 4

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}
