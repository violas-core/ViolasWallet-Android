package com.violas.wallet.ui.bank

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.palliums.extensions.expandTouchArea
import com.palliums.extensions.show
import com.palliums.utils.getResourceId
import com.palliums.widget.status.IStatusLayout
import com.violas.wallet.R
import com.violas.wallet.base.BaseAppActivity
import com.violas.wallet.common.getViolasCoinType
import com.violas.wallet.repository.subscribeHub.BalanceSubscribeHub
import com.violas.wallet.repository.subscribeHub.BalanceSubscriber
import com.violas.wallet.utils.loadRoundedImage
import com.violas.wallet.viewModel.WalletAppViewModel
import com.violas.wallet.viewModel.bean.DiemCurrencyAssetVo
import com.violas.wallet.viewModel.bean.AssetVo
import com.violas.wallet.widget.dialog.AssetsVoTokenSelectTokenDialog
import kotlinx.android.synthetic.main.activity_bank_business.*
import kotlinx.android.synthetic.main.view_bank_business_parameter.view.*
import kotlinx.android.synthetic.main.view_item_expand_problem_info.view.*
import kotlinx.android.synthetic.main.view_item_expand_product_info.view.*
import kotlinx.coroutines.launch

/**
 * Created by QuincySx on 2020/8/21 15:28.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc: 数字银行-存款/借款业务
 */
abstract class BankBusinessActivity : BaseAppActivity(),
    AssetsVoTokenSelectTokenDialog.AssetsDataResourcesBridge {
    companion object {
        const val EXT_BUSINESS_ID = "key1"
        const val EXT_BUSINESS_DTO = "key2"
    }

    protected val mBankBusinessViewModel by lazy {
        ViewModelProvider(this).get(BankBusinessViewModel::class.java)
    }

    protected val mWalletAppViewModel by lazy {
        WalletAppViewModel.getInstance()
    }

    protected val mCurrentAssertsAmountSubscriber = object : BalanceSubscriber(null) {
        override fun onNotice(asset: AssetVo?) {
            onCoinAmountNotice(asset)
        }
    }

    override fun getTitleStyle(): Int {
        return PAGE_STYLE_LIGHT_MODE_PRIMARY_NAV_BAR
    }

    override fun getLayoutResId() = R.layout.activity_bank_business

    open fun onCoinAmountNotice(assetsVo: AssetVo?) {}

    private fun handleBusinessUserView(
        viewGroup: ViewGroup,
        iconView: View,
        titleView: TextView,
        amountView: TextView,
        userAmountInfo: BusinessUserAmountInfo?
    ) {
        if (userAmountInfo == null) {
            viewGroup.visibility = View.GONE
            return
        }

        viewGroup.visibility = View.VISIBLE
        iconView.setBackgroundResource(userAmountInfo.icon)
        titleView.text = userAmountInfo.title
        val content = if (userAmountInfo.value2 == null) {
            userAmountInfo.value1 + userAmountInfo.unit
        } else {
            userAmountInfo.value1 + "/" + userAmountInfo.value2 + userAmountInfo.unit
        }
        amountView.text = content
    }

    abstract fun loadBusiness(businessId: String)

    protected fun loadedSuccess() {
        launch {
            statusLayout.showStatus(IStatusLayout.Status.STATUS_NONE)
        }
    }

    protected fun loadedFailure() {
        launch {
            statusLayout.showStatus(IStatusLayout.Status.STATUS_FAILURE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusLayout.setReloadText(R.string.bank_biz_desc_click_retry)
        val businessId = intent.getStringExtra(EXT_BUSINESS_ID) ?: "0"
        if (businessId != null) {
            loadBusiness(businessId)
        } else {
            throw Exception("BankBusinessActivity Param EXT_BUSINESS_ID is null.")
        }
        statusLayout.setReloadCallback {
            loadBusiness(businessId)
        }

        mBankBusinessViewModel.mPageTitleLiveData.observe(this, Observer {
            title = it
        })
        mBankBusinessViewModel.mBusinessUsableAmount.observe(this, Observer {
            handleBusinessUserView(
                viewGroupBusinessUsableAmount,
                ivBusinessUsableAmount,
                tvBusinessUsableAmountTitle,
                tvBusinessUsableAmount,
                it
            )
        })
        mBankBusinessViewModel.mBusinessUserInfoLiveData.observe(this, Observer { businessInfo ->
            tvBusinessName.text = businessInfo.businessName
            editBusinessValue.hint = businessInfo.businessInputHint

            handleBusinessUserView(
                viewGroupBusinessLimitAmount,
                ivBusinessLimitAmount,
                tvBusinessLimitAmountTitle,
                tvBusinessLimitAmount,
                businessInfo.businessLimitAmount
            )
        })
        mBankBusinessViewModel.mCurrentAssetsLiveData.observe(this, Observer {
            ivCurrentAssetsName.text = it.getAssetsName()
            ivCurrentAssetsIcon.loadRoundedImage(
                it.getLogoUrl(),
                getResourceId(R.attr.iconCoinDefLogo, baseContext),
                14
            )
        })
        mBankBusinessViewModel.mBusinessParameterListLiveData.observe(this, Observer {
            viewGroupBusinessParameter.removeAllViews()
            it.forEach { businessParameter ->
                val inflate = layoutInflater.inflate(
                    R.layout.view_bank_business_parameter,
                    null
                )
                inflate.tvBusinessParameterTitle.text = businessParameter.title
                inflate.tvBusinessParameterContent.text = businessParameter.content
                businessParameter.declare?.let {
                    inflate.tvBusinessParameterDeclare.visibility = View.VISIBLE
                    inflate.tvBusinessParameterDeclare.text = businessParameter.declare
                }
                businessParameter.contentColor?.let {
                    inflate.tvBusinessParameterContent.setTextColor(it)
                }
                viewGroupBusinessParameter.addView(inflate)
            }
        })

        viewGroupProductInfo.setOnClickListener {
            expandLayoutProductInfo.toggleExpand()
            val resId = if (expandLayoutProductInfo.isExpand) {
                R.drawable.icon_bank_info_expand
            } else {
                R.drawable.icon_bank_info_fold
            }
            ivProductInfo.setBackgroundResource(resId)
        }
        mBankBusinessViewModel.mProductExplanationListLiveData.observe(this, Observer {
            if (it == null) {
                viewGroupProductInfo.visibility = View.GONE
                expandLayoutProductInfo.visibility = View.GONE
            } else {
                viewGroupProductInfo.visibility = View.VISIBLE
                expandLayoutProductInfo.visibility = View.VISIBLE

                expandLayoutProductInfo.removeAllViews()
                it.forEach { productInfo ->
                    val inflate = layoutInflater.inflate(
                        R.layout.view_item_expand_product_info,
                        null
                    )
                    inflate.tvProductExplanationTitle.text = productInfo.title
                    inflate.tvProductExplanationContent.text = productInfo.content
                    expandLayoutProductInfo.addView(inflate)
                }
                expandLayoutProductInfo.initExpand(true)
                expandLayoutProductInfo.reSetViewDimensions()
            }
        })

        viewGroupProductIssue.setOnClickListener {
            expandLayoutProductIssue.toggleExpand()
            val resId = if (expandLayoutProductIssue.isExpand) {
                R.drawable.icon_bank_info_expand
            } else {
                R.drawable.icon_bank_info_fold
            }
            ivProductIssue.setBackgroundResource(resId)
        }
        mBankBusinessViewModel.mFAQListLiveData.observe(this, Observer {
            if (it == null) {
                viewGroupProductIssue.visibility = View.GONE
                expandLayoutProductIssue.visibility = View.GONE
            } else {
                viewGroupProductIssue.visibility = View.VISIBLE
                expandLayoutProductIssue.visibility = View.VISIBLE

                expandLayoutProductIssue.removeAllViews()
                it.forEach { productIssue ->
                    val inflate = layoutInflater.inflate(
                        R.layout.view_item_expand_problem_info,
                        null
                    )
                    inflate.tvFAQTitle.text = productIssue.q
                    inflate.tvFAQContent.text = productIssue.a
                    expandLayoutProductIssue.addView(inflate)
                }
                expandLayoutProductIssue.reSetViewDimensions()
                expandLayoutProductIssue.collapse()
            }
        })

        mBankBusinessViewModel.mBusinessPolicyLiveData.observe(this, Observer {
            if (it == null) {
                viewGroupAgreePolicy.visibility = View.GONE
                return@Observer
            }
            btnHasAgreePolicy.expandTouchArea(20)
            viewGroupAgreePolicy.visibility = View.VISIBLE
            tvPolicy.setMovementMethod(LinkMovementMethod.getInstance())
            tvPolicy.text = it
        })
        mBankBusinessViewModel.mBusinessActionHintLiveData.observe(this, Observer {
            if (it == null) {
                tvError.visibility = View.GONE
                return@Observer
            }
            tvError.visibility = View.VISIBLE
            tvError.text = it
        })
        mBankBusinessViewModel.mBusinessActionLiveData.observe(this, Observer {
            btnOperationAction.text = it
        })

        tvAllValue.setOnClickListener { clickSendAll() }
        btnOperationAction.setOnClickListener { clickExecBusiness() }

        BalanceSubscribeHub.observe(this, mCurrentAssertsAmountSubscriber)
    }

    protected fun setCurrentCoin(
        module: String,
        address: String,
        name: String,
        coinType: Int = getViolasCoinType().coinNumber()
    ) {
        var isFind = false
        mWalletAppViewModel.mAssetsLiveData.value?.forEach {
            if (it.isBitcoin()
                && it.getCoinNumber() == coinType
            ) {
                isFind = true
                changeCurrAssets(it)
            } else if (it is DiemCurrencyAssetVo
                && it.getCoinNumber() == coinType
                && it.currency.module == module
                && it.currency.address == address
                && it.currency.name == name
            ) {
                isFind = true
                changeCurrAssets(it)
            }
            if (isFind) {
                return@forEach
            }
        }
        if (!isFind) {
            showToast(getString(R.string.bank_biz_tips_unsupported_currency))
            finish()
        }
    }

    // <editor-fold defaultstate="collapsed" desc="当前币种的选择与切换逻辑">

    private fun changeCurrAssets(assetsVo: AssetVo) {
        launch {
            if (mBankBusinessViewModel.mCurrentAssetsLiveData.value != assetsVo) {
                mBankBusinessViewModel.mCurrentAssetsLiveData.value = assetsVo
            }
        }
    }

    private fun showSelectTokenDialog() {
        AssetsVoTokenSelectTokenDialog()
            .setCallback { assetsVo ->
                changeCurrAssets(assetsVo)
            }.show(supportFragmentManager)
    }

    override fun getCurrCoin(): AssetVo? {
        return mBankBusinessViewModel.mCurrentAssetsLiveData.value
    }

    override suspend fun getSupportAssetsTokens(): LiveData<List<AssetVo>?> {
        return mBankBusinessViewModel.mSupportAssetsTokensLiveData
    }

    //</editor-fold>

    abstract fun clickSendAll()
    abstract fun clickExecBusiness()
}