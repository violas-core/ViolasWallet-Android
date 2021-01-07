package com.violas.wallet.ui.incentive.earningsDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.palliums.base.BaseViewHolder
import com.palliums.paging.PagingViewAdapter
import com.palliums.paging.PagingViewModel
import com.palliums.utils.formatDate
import com.palliums.utils.getColorByAttrId
import com.palliums.widget.refresh.IRefreshLayout
import com.palliums.widget.status.IStatusLayout
import com.violas.wallet.R
import com.violas.wallet.base.BasePagingFragment
import com.violas.wallet.common.KEY_ONE
import com.violas.wallet.repository.http.incentive.PoolMiningEarningDTO
import com.violas.wallet.utils.convertAmountToDisplayAmountStr
import kotlinx.android.synthetic.main.fragment_pool_mining_earnings.*
import kotlinx.android.synthetic.main.item_pool_mining_earning.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by elephant on 11/26/20 2:28 PM.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc: 资金池挖矿收益视图
 */
class PoolMiningEarningsFragment : BasePagingFragment<PoolMiningEarningDTO>() {

    companion object {
        fun newInstance(walletAddress: String): PoolMiningEarningsFragment {
            return PoolMiningEarningsFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_ONE, walletAddress)
                }
            }
        }
    }

    private lateinit var mWalletAddress: String

    override fun getLayoutResId(): Int {
        return R.layout.fragment_pool_mining_earnings
    }

    override fun getRecyclerView(): RecyclerView {
        return vRecyclerView
    }

    override fun getRefreshLayout(): IRefreshLayout? {
        return vRefreshLayout
    }

    override fun getStatusLayout(): IStatusLayout? {
        return vStatusLayout
    }

    override fun lazyInitPagingViewModel(): PagingViewModel<PoolMiningEarningDTO> {
        return ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return modelClass
                        .getConstructor(String::class.java)
                        .newInstance(mWalletAddress)
                }
            }
        ).get(PoolMiningEarningsViewModel::class.java)
    }

    override fun lazyInitPagingViewAdapter(): PagingViewAdapter<PoolMiningEarningDTO> {
        return ViewAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!initData(savedInstanceState)) {
            close()
        } else {
            getStatusLayout()?.showStatus(IStatusLayout.Status.STATUS_LOADING)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_ONE, mWalletAddress)
    }

    private fun initData(savedInstanceState: Bundle?): Boolean {
        try {
            val bundle = savedInstanceState ?: arguments ?: return false
            mWalletAddress = bundle.getString(KEY_ONE, null) ?: return false
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun onLazyInitViewByResume(savedInstanceState: Bundle?) {
        super.onLazyInitViewByResume(savedInstanceState)

        getPagingHandler().init()
        if (mWalletAddress.isBlank()) {
            getStatusLayout()?.showStatus(IStatusLayout.Status.STATUS_EMPTY)
        } else {
            getPagingHandler().start()
        }
    }

    class ViewAdapter : PagingViewAdapter<PoolMiningEarningDTO>() {

        private val simpleDateFormat by lazy {
            SimpleDateFormat("HH:mm MM/dd", Locale.ENGLISH)
        }

        override fun onCreateViewHolderSupport(
            parent: ViewGroup,
            viewType: Int
        ): BaseViewHolder<out Any> {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_pool_mining_earning,
                    parent,
                    false
                ),
                simpleDateFormat
            )
        }
    }

    class ViewHolder(
        view: View,
        private val simpleDateFormat: SimpleDateFormat
    ) : BaseViewHolder<PoolMiningEarningDTO>(view) {

        override fun onViewBind(itemPosition: Int, itemData: PoolMiningEarningDTO?) {
            itemData?.let {
                itemView.tvTime.text = formatDate(it.extractionTime, simpleDateFormat)

                itemView.tvAmount.text = "${
                    convertAmountToDisplayAmountStr(it.extractionAmount)
                }VLS"

                itemView.tvState.setText(
                    if (it.status == 0)
                        R.string.mining_earnings_state_issuing
                    else
                        R.string.mining_earnings_state_arrived
                )
                itemView.tvState.setTextColor(
                    getColorByAttrId(
                        if (it.status == 0)
                            R.attr.textColorProcessing
                        else
                            android.R.attr.textColor,
                        itemView.context
                    )
                )

                itemView.tvType.setText(
                    when (it.type) {
                        8 -> R.string.mining_earnings_type_add_liquidity
                        9 -> R.string.mining_earnings_type_remove_liquidity
                        else -> R.string.mining_earnings_type_active_extraction
                    }
                )
            }
        }
    }
}