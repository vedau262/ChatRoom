package com.festfive.app.base.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.festfive.app.base.view.BaseFragment

/**
 * Created by Nhat Vo on 03/03/2020.
 */
open class BaseFragmentPagerAdapter<T: BaseFragment<*, *>>(
    var mFragmentList: MutableList<T>,
    fm: FragmentManager
) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    open fun updateFragment(fragments: MutableList<T>) {
        mFragmentList = fragments
        notifyDataSetChanged()
    }
}