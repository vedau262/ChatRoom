package com.festfive.app.base.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.festfive.app.R
import com.festfive.app.base.others.BaseCustomView
import com.festfive.app.base.viewmodel.IBaseViewModel
import com.festfive.app.data.preference.IConfigurationPrefs
import com.festfive.app.model.error.ErrorMessage
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Created by Nhat.vo on 16/11/2020.
 */
abstract class BaseFragment<V : ViewDataBinding, T : IBaseViewModel> : DaggerFragment(),
        IBaseFragment<T> {

    override val navController by lazy { findNavController() }

    @Inject
    lateinit var mViewModel: T

    override fun getViewModel(): T = mViewModel

    protected lateinit var dataBinding: V

    override val configPrefs: IConfigurationPrefs
        get() = getParentActivity().configPrefs

    override val viewContext: Context
        get() = requireContext()

    fun isViewModelInitialized(): Boolean = ::mViewModel.isInitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(mViewModel as LifecycleObserver)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate(inflater, getLayoutRes(), container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.lifecycleOwner = this
        initViewModel()
        initView()
        initEvent()
    }

    override fun initView() {
        context?.apply {
            view?.findViewWithTag<BaseCustomView>(getString(R.string.tag_tool_bar_view))
                    ?.onSpecialClicked {
                        activity?.onBackPressed()
                    }
        }
    }

    override fun getToolbarTitle(): String? = null

    open fun initEvent() {}

    override fun onSearchCallback(searchKey: String?) {}

    override fun getCurrentFragment(id: Int): Fragment? {
        kotlin.runCatching {
            childFragmentManager.findFragmentById(id)?.childFragmentManager?.fragments?.let {
                if (it.isNotEmpty()) {
                    return it[0]
                }
            }
        }
        return null
    }

    override fun getFragments(id: Int): MutableList<Fragment>? {
        kotlin.runCatching {
            childFragmentManager.findFragmentById(id)?.childFragmentManager?.fragments
        }
        return null
    }

    override fun initViewModel() {
        mViewModel.apply {
            errorObs.observe(viewLifecycleOwner, Observer {
                it?.apply {
                    handleError(this)
                    resetErrorMessage()
                }
            })
            isLoadingObs.observe(viewLifecycleOwner, Observer {
                if (it) {
                    showLoadingDialog()
                } else {
                    dismissLoadingDialog()
                }
            })
        }
    }

    override fun showLoadingDialog() {
        getParentActivity().showLoadingDialog()
    }

    override fun dismissLoadingDialog() {
        getParentActivity().dismissLoadingDialog()
    }

    override fun handleError(errorMessage: ErrorMessage?) {
        getParentActivity().handleError(errorMessage)
    }

    override fun onHandleBackPressed() {
        getParentActivity().onHandleBackPressed()
    }

    override fun getParentActivity(): IBaseActivity<*> {
        return requireActivity() as BaseActivity<*, *>
    }

    override fun getParentViewModel(): IBaseViewModel {
        return getParentActivity().getViewModel()
    }
}