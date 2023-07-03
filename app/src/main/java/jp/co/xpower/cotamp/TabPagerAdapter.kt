package jp.co.xpower.cotamp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.amplifyframework.datastore.generated.model.StwCompany

class TabPagerAdapter(private val param: ArrayList<StwCompany>, fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments: ArrayList<Fragment> = arrayListOf(
        RallyPublicFragment.newInstance(MainActivity.RALLY_STATE_ALL),
        RallyPublicFragment.newInstance(MainActivity.RALLY_STATE_JOIN),
        RallyPublicFragment.newInstance(MainActivity.RALLY_STATE_END)
    )

    val fragment get() = fragments[0] as RallyPublicFragment

    override fun getItemCount() : Int = 3

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}
