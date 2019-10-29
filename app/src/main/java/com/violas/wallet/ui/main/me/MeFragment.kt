package com.violas.wallet.ui.main.me

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.violas.wallet.R
import com.violas.wallet.base.BaseFragment
import com.violas.wallet.ui.account.management.AccountManagementActivity
import kotlinx.android.synthetic.main.fragment_me.*

/**
 * 我的页面
 */
class MeFragment : BaseFragment() {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_me
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        mivWalletManagement.setOnClickListener(this)
        mivTransferRecord.setOnClickListener(this)
        mivAddressBook.setOnClickListener(this)
        mivSettings.setOnClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view.id) {
            R.id.mivWalletManagement -> {
                startActivity(Intent(activity, AccountManagementActivity::class.java))
            }

            R.id.mivTransferRecord -> {

            }

            R.id.mivAddressBook -> {

            }

            R.id.mivSettings -> {

            }
        }
    }
}