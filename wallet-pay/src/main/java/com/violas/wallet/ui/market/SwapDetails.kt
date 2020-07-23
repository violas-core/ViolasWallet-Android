package com.violas.wallet.ui.market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.palliums.utils.formatDate
import com.palliums.utils.getColorByAttrId
import com.palliums.utils.getResourceId
import com.palliums.utils.start
import com.palliums.violas.http.MarketSwapRecordDTO
import com.violas.wallet.R
import com.violas.wallet.base.BaseAppActivity
import com.violas.wallet.common.KEY_ONE
import com.violas.wallet.utils.convertAmountToDisplayAmountStr
import com.violas.wallet.utils.convertAmountToExchangeRateStr
import kotlinx.android.synthetic.main.activity_swap_details.*

/**
 * Created by elephant on 2020/7/15 09:42.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc: 交易市场兑换详情页面
 */
class SwapDetailsActivity : BaseAppActivity() {

    companion object {
        fun start(context: Context, record: MarketSwapRecordDTO) {
            Intent(context, SwapDetailsActivity::class.java)
                .apply { putExtra(KEY_ONE, record) }
                .start(context)
        }
    }

    private lateinit var mSwapRecord: MarketSwapRecordDTO

    override fun getLayoutResId(): Int {
        return R.layout.activity_swap_details
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.swap_details)
        if (initData(savedInstanceState)) {
            initView(mSwapRecord)
        } else {
            close()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_ONE, mSwapRecord)
    }

    private fun initData(savedInstanceState: Bundle?): Boolean {
        var record: MarketSwapRecordDTO? = null
        if (savedInstanceState != null) {
            record = savedInstanceState.getParcelable(KEY_ONE)
        } else if (intent != null) {
            record = intent.getParcelableExtra(KEY_ONE)
        }

        return if (record == null) {
            false
        } else {
            mSwapRecord = record
            true
        }
    }

    private fun initView(record: MarketSwapRecordDTO) {
        tvFromToken.text =
            if (record.fromName.isNullOrBlank() || record.fromAmount.isNullOrBlank()) {
                getString(R.string.value_null)
            } else {
                "${convertAmountToDisplayAmountStr(record.fromAmount!!)} ${record.fromName}"
            }
        tvToToken.text =
            if (record.toName.isNullOrBlank() || record.toAmount.isNullOrBlank()) {
                getString(R.string.value_null)
            } else {
                "${convertAmountToDisplayAmountStr(record.toAmount!!)} ${record.toName}"
            }
        tvExchangeRate.text =
            if (record.fromAmount.isNullOrBlank() || record.toAmount.isNullOrBlank()) {
                getString(R.string.value_null)
            } else {
                "1:${convertAmountToExchangeRateStr(record.fromAmount!!, record.toAmount!!)}"
            }
        tvHandlingFee.text = getString(R.string.value_null)
        tvGasFee.text = getString(R.string.value_null)
        tvOrderTime.text = formatDate(record.date, pattern = "yyyy-MM-dd HH:mm:ss")
        tvDealTime.text = getString(R.string.value_null)

        tvProcessingDesc.setTextColor(
            getColorByAttrId(R.attr.marketDetailsCompletedStateTextColor, this)
        )
        vVerticalLine2.setBackgroundColor(
            getColorByAttrId(R.attr.marketDetailsCompletedLineBgColor, this)
        )
        vVerticalLine2.visibility = View.VISIBLE
        ivResultIcon.visibility = View.VISIBLE
        tvResultDesc.visibility = View.VISIBLE

        // 兑换成功
        if (record.status == 4001) {
            tvResultDesc.setText(R.string.market_swap_state_succeeded)
            tvResultDesc.setTextColor(
                getColorByAttrId(R.attr.textColorSuccess, this)
            )
            ivResultIcon.setBackgroundResource(
                getResourceId(R.attr.iconRecordStateSucceeded, this)
            )
            return
        }

        // 兑换失败
        tvResultDesc.setText(R.string.market_swap_state_failed)
        tvResultDesc.setTextColor(
            getColorByAttrId(R.attr.textColorFailure, this)
        )
        ivResultIcon.setBackgroundResource(
            getResourceId(R.attr.iconRecordStateFailed, this)
        )
        tvRetry.visibility = View.VISIBLE
        tvRetry.setOnClickListener {
            // TODO 重试
        }
    }
}