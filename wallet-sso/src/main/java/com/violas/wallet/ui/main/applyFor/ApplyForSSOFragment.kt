package com.violas.wallet.ui.main.applyFor

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.palliums.base.BaseFragment
import com.palliums.extensions.isNoNetwork
import com.palliums.widget.status.IStatusLayout
import com.violas.wallet.R
import com.violas.wallet.repository.http.issuer.ApplyForSSOSummaryDTO
import com.violas.wallet.ui.main.applyFor.ApplyForSSOViewModel.Companion.CODE_AUTHENTICATION_ACCOUNT
import com.violas.wallet.ui.main.applyFor.ApplyForSSOViewModel.Companion.CODE_AUTHENTICATION_COMPLETE
import kotlinx.android.synthetic.main.fragment_apply_for_sso.*
import me.yokeyword.fragmentation.SupportFragment

/**
 * 发行商申请发行SSO首页
 */
class ApplyForSSOFragment : BaseFragment() {

    private val mViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(ApplyForSSOViewModel::class.java)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_apply_for_sso
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        setStatusBarMode(false)
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initView()
        initEvent()
    }

    private fun initView() {
        vTitleMiddleText.text = getString(R.string.title_apply_issue_sso)
        dslStatusLayout.showStatus(IStatusLayout.Status.STATUS_LOADING)
        srlRefreshLayout.isEnabled = false
    }

    private fun initEvent() {
        srlRefreshLayout.setOnRefreshListener {
            loadIssueSSOStatus()
        }

        mViewModel.tipsMessage.observe(this, Observer {
            it.getDataIfNotHandled()?.let { msg ->
                if (msg.isNotEmpty()) {
                    showToast(msg)
                }
            }
        })

        mViewModel.mApplyForSSOSummaryLiveData.observe(this, Observer {
            it.getDataIfNotHandled()?.let { status ->
                loadFragment(status)
            }
        })

        mViewModel.mAccountDOLiveData.observe(this, Observer {
            loadIssueSSOStatus()
        })
    }

    private fun loadIssueSSOStatus() {
        mViewModel.execute(
            failureCallback = {
                if (srlRefreshLayout == null) return@execute

                if (srlRefreshLayout.isRefreshing) {
                    srlRefreshLayout.isRefreshing = false
                } else {
                    srlRefreshLayout.isEnabled = true
                }

                if (isLoadFragment()) return@execute

                dslStatusLayout.showStatus(
                    if (it.isNoNetwork())
                        IStatusLayout.Status.STATUS_NO_NETWORK
                    else
                        IStatusLayout.Status.STATUS_FAILURE
                )
            }
        ) {
            if (srlRefreshLayout == null) return@execute

            if (srlRefreshLayout.isRefreshing) {
                srlRefreshLayout.isRefreshing = false
            } else {
                srlRefreshLayout.isEnabled = true
            }

            dslStatusLayout.showStatus(IStatusLayout.Status.STATUS_NONE)
        }
    }

    private fun loadFragment(summary: ApplyForSSOSummaryDTO) {
        when (summary.applicationStatus) {
            CODE_AUTHENTICATION_ACCOUNT -> {
                if (topChildFragment !is VerifyIssuerAccountFragment) {
                    loadRootFragment(
                        R.id.fragmentContainerView,
                        VerifyIssuerAccountFragment()
                    )
                }
            }

            CODE_AUTHENTICATION_COMPLETE -> {
                if (topChildFragment !is VerifyIssuerAccountSuccessFragment) {
                    loadRootFragment(
                        R.id.fragmentContainerView,
                        VerifyIssuerAccountSuccessFragment()
                    )
                }
            }

            else -> {
                if (topChildFragment is IssuerApplyForSSOStatusFragment) {
                    topChildFragment.putNewBundle(IssuerApplyForSSOStatusFragment.newBundle(summary))
                    (topChildFragment as SupportFragment).start(
                        topChildFragment,
                        SupportFragment.SINGLETASK
                    )
                } else {
                    loadRootFragment(
                        R.id.fragmentContainerView,
                        IssuerApplyForSSOStatusFragment.getInstance(summary)
                    )
                }
            }
        }
    }

    private fun isLoadFragment(): Boolean {
        return topChildFragment != null
    }
}
