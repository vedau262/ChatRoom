package com.festfive.app.base.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import com.festfive.app.R
import com.festfive.app.base.viewmodel.IBaseViewModel
import com.festfive.app.customize.dialog.ErrorDialog
import com.festfive.app.customize.dialog.LoadingProgress
import com.festfive.app.data.preference.IConfigurationPrefs
import com.festfive.app.extension.getDefault
import com.festfive.app.extension.hideKeyboard
import com.festfive.app.model.error.CommonError
import com.festfive.app.model.error.ErrorMessage
import com.festfive.app.repository.LoginRepository
import com.festfive.app.utils.Utils
import dagger.android.support.DaggerAppCompatActivity
import retrofit2.Retrofit
import javax.inject.Inject


/**
 * Created by Nhat.vo on 16/11/2020.
 */
abstract class BaseActivity<V : ViewDataBinding, T : IBaseViewModel> : DaggerAppCompatActivity(),
    IBaseActivity<T> {

    @Inject
    lateinit var mViewModel: T

    @Inject
    lateinit var prefs: IConfigurationPrefs

    lateinit var restBuilder : Retrofit.Builder

    override val configPrefs: IConfigurationPrefs
        get() = prefs

    override val viewContext: Context
        get() = this

    override fun getViewModel(): T = mViewModel

    private var loadingProgress: LoadingProgress? = null
    private var errorDialog: ErrorDialog? = null

    protected lateinit var dataBinding: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onStartAnimateTransaction()
        onChangeStatusBarColor()
        dataBinding = DataBindingUtil.setContentView(this, getLayoutRes())
        dataBinding.lifecycleOwner = this

    }

    open fun onStartAnimateTransaction() {
        if (!intent.hasExtra("SkipAnimation")) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onChangeStatusBarColor() {
//        window.statusBarColor = prefs.brandingConfig.colors.primaryColor.parseStringColor()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onCheckNewIntent(intent)
    }

    /**
     * Init [View] components here. Such as set adapter for [RecyclerView], set listener
     * or anything else
     */
    @SuppressLint("SetTextI18n")
    override fun initView() {
        loadingProgress = LoadingProgress(this)
    }

    override fun initViewModel() {
        mViewModel.apply {
            lifecycle.addObserver(this as LifecycleObserver)
            errorObs.observe(this@BaseActivity, Observer {
                it?.apply {
                    handleError(this)
                    resetErrorMessage()
                }
            })
            isLoadingObs.observe(this@BaseActivity, Observer {
                if (it) {
                    showLoadingDialog()
                } else {
                    dismissLoadingDialog()
                }
            })
        }
    }

    override fun getToolbarTitle(): String? = null

    override fun onCheckNewIntent(intent: Intent?) {}

    override fun onAuthenticationHandler() {
        /*startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })*/
    }

    override fun onSearchCallback(searchKey: String?) {}

    override fun showLoadingDialog() {
        if (loadingProgress == null) {
            loadingProgress = LoadingProgress(this)
        }
        loadingProgress?.let {
            if (!it.isShowing) {
                it.show()
            }
        }
    }

    override fun dismissLoadingDialog() {
        try {
            loadingProgress?.let {
                it.dismiss()
                loadingProgress = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun handleError(errorMessage: ErrorMessage?) {
        errorMessage?.let {
            when (it.error) {
                CommonError.UNKNOWN -> errorMessage.message = it.message
                CommonError.NETWORK_ERROR -> errorMessage.message =
                    getString(R.string.msg_no_network)
                CommonError.UNAUTHENTICATED -> {
                    onAuthenticationHandler()
                    return
                }
                else -> {
                }
            }

            // Hardcode
            errorMessage.message.getDefault().split("operation: ").let{ list ->
                if (list.isNotEmpty()) {
                    errorMessage.message = list.last()
                }
            }
            // End hardcode

            onShowErrorDialog(errorMessage.message.getDefault())
        }
    }

    /**
     * Show error dialog message
     */

//    lateinit var messageConfirmDialog :MessageConfirmDialog

    override fun onShowErrorDialog(message: String) {

        /* messageConfirmDialog = MessageConfirmDialog(
                viewContext, title = "",
                message = message
        )
        messageConfirmDialog.setRightAction {
            SharePreferencesUtils.setBoolean("request_authorization_login", true)
            SharePreferencesUtils.setBoolean(UsKey.allowBiometric, true)
        }

        messageConfirmDialog.show()*/
    }

    override fun getCurrentFragment(id: Int): Fragment? {
        supportFragmentManager.findFragmentById(id)?.childFragmentManager?.fragments?.let {
            if (it.isNotEmpty()) {
                return it[0]
            }
        }
        return null
    }

    override fun getFragments(id: Int): MutableList<Fragment>? {
        return supportFragmentManager.findFragmentById(id)?.childFragmentManager?.fragments
    }

    override fun getNavId(): Int? = null

    override fun onHandleBackPressed() {
        onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        Utils.hideSoftKeyboard(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            loadingProgress?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Utils.hideSoftKeyboard(this)
    }

    private var isClick: Boolean = false
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                isClick = true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (isClick) {
                    val v = currentFocus
                    if (v != null && v is EditText && !v.javaClass.name.startsWith("android.webkit.")) {
                        val scrcoords = IntArray(2)
                        v.getLocationOnScreen(scrcoords)
                        val x = ev.rawX + v.getLeft() - scrcoords[0]
                        val y = ev.rawY + v.getTop() - scrcoords[1]
                        if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()
                        ) {
                            v.clearFocus()
                            Handler().postDelayed({
                                if (currentFocus !is EditText) {
                                    v.hideKeyboard(this)
                                }
                            }, 250)
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                isClick = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}