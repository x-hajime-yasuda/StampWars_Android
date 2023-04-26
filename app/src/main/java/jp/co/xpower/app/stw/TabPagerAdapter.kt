package jp.co.xpower.app.stw

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabPagerAdapter(private val param: String, fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments: ArrayList<Fragment> = arrayListOf(
        RallyPublicFragment(),
        RallyPublicFragment.newInstance(param),
        RallyPublicFragment.newInstance(param)
    )

    override fun getItemCount() : Int = 3

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}
