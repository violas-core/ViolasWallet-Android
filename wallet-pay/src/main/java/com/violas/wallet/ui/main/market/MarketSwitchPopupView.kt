package com.violas.wallet.ui.main.market

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.palliums.base.BaseViewHolder
import com.palliums.listing.ListingViewAdapter
import com.palliums.widget.popup.EnhancedAttachPopupView
import com.violas.wallet.R
import kotlinx.android.synthetic.main.item_market_switch.view.*
import kotlinx.android.synthetic.main.popup_market_switch.view.*

/**
 * Created by elephant on 2020/6/29 19:16.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc: 市场选择弹窗
 */
class MarketSwitchPopupView(
    context: Context,
    private val checkedPosition: Int,
    private val dataList: MutableList<String>,
    private val selectCallback: (Int) -> Unit
) : EnhancedAttachPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.popup_market_switch
    }

    override fun initPopupContent() {
        super.initPopupContent()
        if (!popupInfo.hasShadowBg && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            attachPopupContainer.setBackgroundColor(Color.TRANSPARENT)
            attachPopupContainer.elevation = 0f
        }
    }

    override fun onCreate() {
        super.onCreate()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ViewAdapter(dataList, checkedPosition) {
            dismiss()
            selectCallback.invoke(it)
        }
    }

    class ViewAdapter(
        dataList: MutableList<String>,
        private val checkedPosition: Int,
        private val selectCallback: (Int) -> Unit
    ) : ListingViewAdapter<String>(dataList) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_market_switch,
                    parent,
                    false
                ), checkedPosition, selectCallback
            )
        }
    }

    class ViewHolder(
        view: View,
        private val checkedPosition: Int,
        private val selectCallback: (Int) -> Unit
    ) : BaseViewHolder<String>(view) {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onViewBind(itemPosition: Int, itemData: String?) {
            itemData?.let {
                itemView.tvText.text = it
                itemView.tvText.setBackgroundResource(
                    if (checkedPosition == itemPosition)
                        R.drawable.sel_bg_market_switch_item_checked
                    else
                        R.drawable.sel_bg_market_switch_item_normal
                )
            }
        }

        override fun onViewClick(view: View, itemPosition: Int, itemData: String?) {
            selectCallback.invoke(itemPosition)
        }
    }
}