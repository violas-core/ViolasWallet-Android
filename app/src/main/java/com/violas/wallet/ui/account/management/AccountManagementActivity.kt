package com.violas.wallet.ui.account.management

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.quincysx.crypto.CoinTypes
import com.violas.wallet.R
import com.violas.wallet.base.BaseActivity
import com.violas.wallet.base.recycler.RecycleViewItemDivider
import com.violas.wallet.ui.account.AccountDisplayType
import com.violas.wallet.ui.account.AccountVo
import com.violas.wallet.ui.account.fakeAccounts
import com.violas.wallet.ui.account.loadAccounts
import com.violas.wallet.utils.DensityUtility
import com.violas.wallet.widget.GroupListLayout
import kotlinx.android.synthetic.main.activity_account_management.accOptGroupListLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by elephant on 2019-10-23 17:35.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc: 钱包账户管理页面
 */
class AccountManagementActivity : BaseActivity() {

    override fun getLayoutResId(): Int {
        return R.layout.activity_account_management
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.account_management_title)

        initView()
        initData()
    }

    private fun initView() {
        accOptGroupListLayout.itemFactory = object : GroupListLayout.ItemFactory() {

            override fun createContentItemLayout(
                context: Context,
                viewType: Int
            ): GroupListLayout.ItemLayout<out GroupListLayout.ItemData> {
                return ContentItem(context)
            }
        }
        accOptGroupListLayout.addItemDecoration(
            RecycleViewItemDivider(
                this,
                DensityUtility.dp2px(this, 4),
                DensityUtility.dp2px(this, 60),
                0,
                0,
                showFirstTop = true,
                onlyShowLastBottom = true
            )
        )
    }

    private fun initData() {
        launch(Dispatchers.IO) {
            //val data = fakeAccounts(AccountDisplayType.ALL)
            val data = loadAccounts(AccountDisplayType.ALL)
            withContext(Dispatchers.Main) {
                accOptGroupListLayout.setData(data)
            }
        }
    }

    class ContentItem(context: Context) : GroupListLayout.ItemLayout<AccountVo>,
        View.OnClickListener {

        private val rootView: View = View.inflate(context, R.layout.item_account_management, null)
        private val tvName: TextView
        private val tvAddress: TextView

        private var accountVo: AccountVo? = null

        init {
            rootView.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    DensityUtility.dp2px(context, 15),
                    0,
                    DensityUtility.dp2px(context, 15),
                    0
                )
            }

            tvName = rootView.findViewById(R.id.tvAccountName)
            tvAddress = rootView.findViewById(R.id.tvAccountAddress)
            rootView.setOnClickListener(this)
        }

        override fun refreshView(itemData: GroupListLayout.ItemData?) {
            accountVo = itemData as? AccountVo

            accountVo?.let {
                tvName.text = it.accountDO.walletNickname
                tvAddress.text = it.accountDO.address
                rootView.setBackgroundResource(
                    when (it.accountDO.coinNumber) {
                        CoinTypes.Libra.coinType() ->
                            R.drawable.selector_account_management_item_libra
                        CoinTypes.Bitcoin.coinType() ->
                            R.drawable.selector_account_management_item_btc
                        else ->
                            R.drawable.selector_account_management_item_violas
                    }
                )
            }
        }

        override fun getItemView(): View {
            return rootView
        }

        override fun onClick(view: View) {
            if (!isFastMultiClick(view)) {
                accountVo?.let {
                    // TODO 跳转到钱包管理页面
                }
            }
        }

    }
}