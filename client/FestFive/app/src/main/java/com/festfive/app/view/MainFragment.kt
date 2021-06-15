package com.festfive.app.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.festfive.app.R
import com.festfive.app.base.view.BaseFragment
import com.festfive.app.base.viewmodel.EmptyViewModel
import com.festfive.app.databinding.FragmentMainBinding


class MainFragment : BaseFragment<FragmentMainBinding, EmptyViewModel>() {
    override fun getLayoutRes(): Int = R.layout.fragment_main

}