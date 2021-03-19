package com.violas.wallet.ui.managerAssert

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.palliums.base.BaseViewHolder
import com.palliums.extensions.getShowErrorMessage
import com.palliums.listing.ListingViewAdapter
import com.palliums.listing.ListingViewModel
import com.palliums.utils.DensityUtility
import com.palliums.utils.getResourceId
import com.palliums.utils.openBrowser
import com.palliums.violas.bean.TokenMark
import com.palliums.widget.dividers.RecyclerViewItemDividers
import com.quincysx.crypto.CoinType
import com.smallraw.support.switchcompat.SwitchButton
import com.violas.wallet.R
import com.violas.wallet.base.BaseListingActivity
import com.violas.wallet.biz.AccountManager
import com.violas.wallet.biz.TokenManager
import com.violas.wallet.biz.bean.AssertOriginateToken
import com.violas.wallet.common.getViolasFaucetUrl
import com.violas.wallet.repository.database.entity.AccountDO
import com.violas.wallet.ui.web.WebCommonActivity
import com.violas.wallet.utils.authenticateAccount
import com.violas.wallet.utils.loadCircleImage
import com.violas.wallet.viewModel.WalletAppViewModel
import com.violas.wallet.viewModel.bean.DiemCoinAssetVo
import com.violas.wallet.widget.dialog.PublishTokenDialog
import kotlinx.android.synthetic.main.item_manager_assert.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 添加币种页面
 */
class ManagerAssertActivity : BaseListingActivity<AssertOriginateToken>() {

    companion object {
        //        private const val EXT_ACCOUNT_ID = "0"
        fun start(context: Fragment, requestId: Int) {
            val intent = Intent(context.activity, ManagerAssertActivity::class.java)
//            intent.putExtra(EXT_ACCOUNT_ID, accountId)
            context.startActivityForResult(intent, requestId)
        }

        fun start(context: Activity, requestId: Int) {
            val intent = Intent(context, ManagerAssertActivity::class.java)
//            intent.putExtra(EXT_ACCOUNT_ID, accountId)
            context.startActivityForResult(intent, requestId)
        }
    }

    private var mChange = false

    private val mTokenManager by lazy {
        TokenManager()
    }

    private val mWalletAppViewModel by lazy {
        WalletAppViewModel.getInstance()
    }

    override fun lazyInitListingViewModel(): ListingViewModel<AssertOriginateToken> {
        return ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return modelClass
                        .getConstructor(TokenManager::class.java)
                        .newInstance(mTokenManager)
                }
            }
        ).get(ManagerAssertViewModel::class.java)
    }

    override fun lazyInitListingViewAdapter(): ListingViewAdapter<AssertOriginateToken> {
        return ViewAdapter { checkbox, checked, assertToken ->
            if (checked) {
                openToken(checkbox, checked, assertToken)
            } else {
                launch(Dispatchers.IO) {
                    assertToken.enable = false
                    mTokenManager.insert(checked, assertToken)
                    mChange = true
                }
            }
        }
    }

    override fun enableRefresh(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.add_currency_title)
        setTitleRightText(R.string.add_currency_menu)

        getRecyclerView().addItemDecoration(
            RecyclerViewItemDividers(
                top = DensityUtility.dp2px(this, 5),
                bottom = DensityUtility.dp2px(this, 5),
                left = DensityUtility.dp2px(this, 16),
                right = DensityUtility.dp2px(this, 16)
            )
        )

        getListingHandler().init()
        getRefreshLayout()?.setOnRefreshListener {
            getListingViewModel().execute()
        }
        getListingViewModel().execute()
    }

    private suspend fun isPublish(
        accountId: Long,
        tokenMark: TokenMark?
    ): Boolean {
        if (tokenMark == null) {
            return false
        }
        return mTokenManager.isPublish(accountId, tokenMark)
    }

    private fun openToken(
        checkbox: SwitchButton,
        checked: Boolean,
        assertOriginateToken: AssertOriginateToken
    ) {
        showProgress()
        launch(Dispatchers.IO) {
            try {
                if (isPublish(assertOriginateToken.account_id, assertOriginateToken.tokenMark)) {
                    assertOriginateToken.enable = true
                    mTokenManager.insert(checked, assertOriginateToken)
                    mChange = true
                    dismissProgress()
                } else {
                    val account = AccountManager.getAccountById(assertOriginateToken.account_id)
                    dismissProgress()
                    withContext(Dispatchers.Main) {
                        PublishTokenDialog()
                            .setCurrencyName(assertOriginateToken.name)
                            .setConfirmListener {
                                showPasswordDialog(account, assertOriginateToken, checkbox, checked)
                                it.dismiss()
                            }
                            .setCancelListener {
                                checkbox.setCheckedNoEvent(false)
                            }
                            .show(supportFragmentManager)
                    }
                }
            } catch (e: Exception) {
                dismissProgress()
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    checkbox.setCheckedNoEvent(false)
                    showToast(e.getShowErrorMessage(false))
                }
            }
        }
    }

    private fun showPasswordDialog(
        account: AccountDO,
        assertOriginateToken: AssertOriginateToken,
        checkbox: SwitchButton,
        checked: Boolean
    ) {
        authenticateAccount(
            account,
            cancelCallback = {
                checkbox.setCheckedNoEvent(false)
            }
        ) {
            launch(Dispatchers.IO) {
                try {
                    val hasSuccess = mTokenManager.publishToken(
                        assertOriginateToken.account_id,
                        it,
                        assertOriginateToken.tokenMark!!
                    )
                    if (hasSuccess) {
                        assertOriginateToken.enable = true
                        mTokenManager.insert(checked, assertOriginateToken)
                        mChange = true
                    } else {
                        withContext(Dispatchers.Main) {
                            checkbox.setCheckedNoEvent(false)
                            showToast(
                                getString(
                                    R.string.add_currency_first_tips_add_failure,
                                    CoinType.parseCoinNumber(account.coinNumber).coinName()
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        checkbox.setCheckedNoEvent(false)
                        showToast(
                            e.getShowErrorMessage(
                                failedDesc = getString(
                                    R.string.add_currency_first_tips_add_failure,
                                    CoinType.parseCoinNumber(account.coinNumber).coinName()
                                )
                            )
                        )
                    }
                }
                dismissProgress()
            }
        }
    }

    override fun onTitleRightViewClick() {
        var address = ""
        mWalletAppViewModel.mAssetsLiveData.value?.forEach {
            if (it is DiemCoinAssetVo) {
                address = it.address
                return@forEach
            }
        }

        val url = getViolasFaucetUrl(address)
        if (!openBrowser(this, url)) {
            WebCommonActivity.start(
                this,
                url,
                getString(R.string.add_currency_menu)
            )
        }
    }

    override fun onBackPressedSupport() {
        if (mChange) {
            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        super.onBackPressedSupport()
    }

    class ViewAdapter(
        private val callbacks: (SwitchButton, Boolean, AssertOriginateToken) -> Unit
    ) : ListingViewAdapter<AssertOriginateToken>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseViewHolder<AssertOriginateToken> {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_manager_assert,
                    parent,
                    false
                ),
                callbacks
            )
        }
    }

    class ViewHolder(
        view: View,
        private val callbacks: (SwitchButton, Boolean, AssertOriginateToken) -> Unit
    ) : BaseViewHolder<AssertOriginateToken>(view) {

        init {
            itemView.setOnClickListener(this)
            itemView.checkBox.setOnClickListener(this)
        }

        override fun onViewBind(itemPosition: Int, itemData: AssertOriginateToken?) {
            itemData?.let {
                itemView.name.text = itemData.name
                itemView.fullName.text = itemData.fullName

                if (itemData.isToken) {
                    itemView.checkBox.visibility = View.VISIBLE
                    itemView.checkBox.setCheckedImmediatelyNoEvent(itemData.enable)
                } else {
                    itemView.checkBox.visibility = View.GONE
                }

                itemView.ivCoinLogo.loadCircleImage(
                    itemData.logo,
                    getResourceId(R.attr.iconCoinDefLogo, itemView.context)
                )
            }
        }

        override fun onViewClick(view: View, itemPosition: Int, itemData: AssertOriginateToken?) {
            itemData?.let {
                if (!it.isToken) return@let

                if (view == itemView) {
                    itemView.checkBox.isChecked = !itemView.checkBox.isChecked
                }
                callbacks.invoke(
                    itemView.checkBox,
                    itemView.checkBox.isChecked,
                    itemData
                )
            }
        }
    }
}

class ManagerAssertViewModel(
    private val tokenManager: TokenManager
) : ListingViewModel<AssertOriginateToken>() {

    override suspend fun loadData(vararg params: Any): List<AssertOriginateToken> {
        return tokenManager.loadSupportToken()
    }

    override fun checkNetworkBeforeExecute(): Boolean {
        return false
    }
}
