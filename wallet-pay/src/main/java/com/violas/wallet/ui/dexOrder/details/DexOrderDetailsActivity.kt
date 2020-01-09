package com.violas.wallet.ui.dexOrder.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.palliums.net.LoadState
import com.palliums.paging.PagingViewAdapter
import com.palliums.paging.PagingViewModel
import com.palliums.widget.status.IStatusLayout
import com.violas.wallet.R
import com.violas.wallet.base.BasePagingActivity
import com.violas.wallet.biz.AccountManager
import com.violas.wallet.event.RevokeDexOrderEvent
import com.violas.wallet.repository.database.entity.AccountDO
import com.violas.wallet.repository.http.dex.DexOrderDTO
import com.violas.wallet.repository.http.dex.DexOrderTradeDTO
import com.violas.wallet.widget.dialog.PasswordInputDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
 * Created by elephant on 2019-12-09 11:34.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc: 交易中心订单详情页面
 */
class DexOrderDetailsActivity : BasePagingActivity<DexOrderTradeDTO>() {

    companion object {
        private const val EXTRA_KEY_DEX_ORDER = "EXTRA_KEY_DEX_ORDER"

        fun start(context: Context, dexOrder: DexOrderDTO) {
            val intent = Intent(context, DexOrderDetailsActivity::class.java)
                .apply {
                    putExtra(EXTRA_KEY_DEX_ORDER, dexOrder)
                }
            context.startActivity(intent)
        }
    }

    private var dexOrder: DexOrderDTO? = null
    private lateinit var currentAccount: AccountDO

    private val mViewModel by viewModels<DexOrderDetailsViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DexOrderDetailsViewModel(
                    dexOrder!!.version
                ) as T
            }
        }
    }

    private val mViewAdapter by lazy {
        DexOrderDetailsViewAdapter(
            retryCallback = { mViewModel.retry() },
            addHeader = true,
            dexOrder = dexOrder!!,
            onOpenBrowserView = {
                // TODO violas浏览器暂未实现
                //showToast(R.string.transaction_record_not_supported_query)
            },
            onClickRevokeOrder = { dexOrder, position ->

                PasswordInputDialog().setConfirmListener { password, dialog ->

                    if (!mViewModel.revokeOrder(
                            currentAccount,
                            password,
                            dexOrder,
                            onCheckPassword = {
                                if (it) {
                                    dialog.dismiss()
                                }
                            }
                        ) {
                            dexOrder.updateStateToRevoking()
                            getViewAdapter().notifyItemChanged(position)

                            EventBus.getDefault().post(
                                RevokeDexOrderEvent(
                                    dexOrder.id,
                                    dexOrder.updateDate
                                )
                            )
                        }
                    ) {
                        dialog.dismiss()
                    }

                }.show(this@DexOrderDetailsActivity.supportFragmentManager)
            }
        )
    }

    override fun getViewModel(): PagingViewModel<DexOrderTradeDTO> {
        return mViewModel
    }

    override fun getViewAdapter(): PagingViewAdapter<DexOrderTradeDTO> {
        return mViewAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch(Dispatchers.IO) {
            val result = initData(savedInstanceState)
            withContext(Dispatchers.Main) {
                if (result) {
                    initView()
                } else {
                    finish()
                }
            }
        }
    }

    private fun initData(savedInstanceState: Bundle?): Boolean {

        if (savedInstanceState != null) {
            dexOrder = savedInstanceState.getParcelable(EXTRA_KEY_DEX_ORDER)
        } else if (intent != null) {
            dexOrder = intent.getParcelableExtra(EXTRA_KEY_DEX_ORDER)
        }

        if (dexOrder == null) {
            return false
        }

        return try {
            currentAccount = AccountManager().currentAccount()

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun initView() {
        setTitle(R.string.title_order_details)

        if (dexOrder!!.isOpen()) {
            mViewModel.loadState.observe(this, Observer {
                when (it.peekData().status) {
                    LoadState.Status.RUNNING -> {
                        showProgress()
                    }

                    else -> {
                        dismissProgress()
                    }
                }
            })

            mViewModel.tipsMessage.observe(this, Observer {
                it.getDataIfNotHandled()?.let { msg ->
                    if (msg.isNotEmpty()) {
                        showToast(msg)
                    }
                }
            })
        }

        getStatusLayout()?.setTipsWithStatus(
            IStatusLayout.Status.STATUS_EMPTY,
            getString(R.string.tips_no_order_trades)
        )
        getDrawable(R.mipmap.ic_no_transaction_record)?.let {
            getStatusLayout()?.setImageWithStatus(IStatusLayout.Status.STATUS_EMPTY, it)
        }

        mPagingHandler.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dexOrder?.let {
            outState.putParcelable(EXTRA_KEY_DEX_ORDER, it)
        }
    }
}