package com.violas.wallet.ui.transfer

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.WorkerThread
import com.quincysx.crypto.CoinTypes
import com.violas.wallet.R
import com.violas.wallet.base.BaseActivity
import com.violas.wallet.biz.AccountManager
import com.violas.wallet.biz.TransferManager
import com.violas.wallet.biz.decodeScanQRCode
import com.violas.wallet.repository.database.entity.AccountDO
import com.violas.wallet.ui.addressBook.AddressBookActivity
import com.violas.wallet.ui.scan.ScanActivity
import com.violas.wallet.utils.start
import kotlinx.coroutines.launch

abstract class TransferActivity : BaseActivity() {
    companion object {
        const val EXT_ACCOUNT_ID = "0"
        const val EXT_ADDRESS = "1"
        const val EXT_AMOUNT = "2"
        const val EXT_IS_TOKEN = "3"
        const val EXT_TOKEN_ID = "4"

        const val REQUEST_SELECTOR_ADDRESS = 1
        const val REQUEST_SCAN_QR_CODE = 2

        @WorkerThread
        fun start(
            context: Context,
            accountId: Long,
            address: String = "",
            amount: Long = 0,
            isToken: Boolean = false,
            tokenId: Long = 0
        ) {
            val account = AccountManager().getAccountById(accountId)
            when (account.coinNumber) {
                CoinTypes.BitcoinTest.coinType(),
                CoinTypes.Bitcoin.coinType() -> {
                    Intent(context, BTCTransferActivity::class.java)
                }
                else -> {
                    Intent(context, LibraTransferActivity::class.java)
                }
            }.apply {
                putExtra(EXT_ACCOUNT_ID, accountId)
                putExtra(EXT_ADDRESS, address)
                putExtra(EXT_AMOUNT, amount)
                putExtra(EXT_IS_TOKEN, isToken)
                putExtra(EXT_TOKEN_ID, tokenId)
            }.start(context)
        }
    }

    var isToken = false
    var tokenId = 0L
    var accountId = 0L
    var account: AccountDO? = null

    val mAccountManager by lazy {
        AccountManager()
    }

    val mTransferManager by lazy {
        TransferManager()
    }

    abstract fun onSelectAddress(address: String)
    abstract fun onScanAddressQr(address: String)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECTOR_ADDRESS -> {
                data?.apply {
                    val address = getStringExtra(AddressBookActivity.RESULT_SELECT_ADDRESS) ?: ""
                    onSelectAddress(address)
                }
            }
            REQUEST_SCAN_QR_CODE -> {
                data?.getStringExtra(ScanActivity.RESULT_QR_CODE_DATA)?.let { msg ->
                    decodeScanQRCode(msg) { coinType, address, amount ->
                        Log.e("===scan===", "${coinType}  ${address}  ${amount}")
                        launch {
                            account?.let {
                                if (coinType == it.coinNumber || coinType == -1) {
                                    onScanAddressQr(address)
                                } else {
                                    showToast(getString(R.string.hint_address_error))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}