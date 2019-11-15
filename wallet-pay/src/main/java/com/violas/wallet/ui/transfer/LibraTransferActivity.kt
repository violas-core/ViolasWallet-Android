package com.violas.wallet.ui.transfer

import android.accounts.AccountsException
import android.os.Bundle
import com.quincysx.crypto.CoinTypes
import com.violas.wallet.R
import com.violas.wallet.base.dialog.PasswordInputDialog
import com.violas.wallet.biz.TokenManager
import com.violas.wallet.repository.database.entity.TokenDo
import com.violas.wallet.ui.addressBook.AddressBookActivity
import com.violas.wallet.ui.scan.ScanActivity
import com.violas.wallet.utils.convertAmountToDisplayUnit
import kotlinx.android.synthetic.main.activity_transfer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class LibraTransferActivity : TransferActivity() {
    override fun getLayoutResId() = R.layout.activity_transfer

    private val mTokenManager by lazy {
        TokenManager()
    }

    private var mTokenDo: TokenDo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_transfer)
        accountId = intent.getLongExtra(EXT_ACCOUNT_ID, 0)
        tokenId = intent.getLongExtra(EXT_TOKEN_ID, 0)
        isToken = intent.getBooleanExtra(EXT_IS_TOKEN, false)
        launch(Dispatchers.IO) {
            try {
                account = mAccountManager.getAccountById(accountId)
                mTokenDo = mTokenManager.findTokenById(tokenId)
                if(mTokenDo==null){
                    finish()
                    return@launch
                }
                refreshCurrentAmount()
                val amount = intent.getLongExtra(
                    EXT_AMOUNT,
                    0
                )

                val parseCoinType = CoinTypes.parseCoinType(account!!.coinNumber)
                withContext(Dispatchers.Main) {
                    if (amount > 0) {
                        val convertAmountToDisplayUnit =
                            convertAmountToDisplayUnit(amount, parseCoinType)
                        editAmountInput.setText(convertAmountToDisplayUnit.first)
                    }
                    title = "${parseCoinType.coinName()}${getString(R.string.transfer)}"
                    if (isToken) {
                        tvHintCoinName.text = mTokenDo?.name ?: ""
                    } else {
                        tvHintCoinName.text = parseCoinType.coinName()
                    }
                }
            } catch (e: AccountsException) {
                finish()
            }
        }
        initViewData()

        ivScan.setOnClickListener {
            ScanActivity.start(this, REQUEST_SCAN_QR_CODE)
        }

        btnConfirm.setOnClickListener {
            send()
        }
        tvAddressBook.setOnClickListener {
            account?.coinNumber?.let { it1 ->
                AddressBookActivity.start(
                    this@LibraTransferActivity,
                    it1,
                    true,
                    REQUEST_SELECTOR_ADDRESS
                )
            }
        }
    }

    private fun refreshCurrentAmount() {
        account?.let {
            if (isToken) {
                mTokenDo?.apply {
                    mTokenManager.getTokenBalance(it.address, tokenAddress) { balance ->
                        launch {
                            tvCoinAmount.text = String.format(
                                getString(R.string.hint_transfer_amount),
                                BigDecimal(balance.toString()).divide(
                                    BigDecimal("1000000"),
                                    6,
                                    RoundingMode.HALF_UP
                                ).stripTrailingZeros().toPlainString(),
                                name
                            )
                        }
                    }
                }
            } else {
                mAccountManager.getBalanceWithUnit(it) { balance, unit ->
                    launch {
                        tvCoinAmount.text = String.format(
                            getString(R.string.hint_transfer_amount),
                            balance,
                            unit
                        )
                    }
                }
            }
        }
    }

    private fun send() {
        val amount = editAmountInput.text.toString().trim().toDouble()
        val address = editAddressInput.text.toString().trim()
        if (amount <= 0) {
            showToast(getString(R.string.hint_please_input_amount))
            return
        }
        if (address.isEmpty()) {
            showToast(getString(R.string.hint_please_input_address))
            return
        }
        PasswordInputDialog()
            .setConfirmListener { bytes, dialogFragment ->
                dialogFragment.dismiss()
                account?.let {
                    showProgress()
                    launch(Dispatchers.IO) {
                        mTransferManager.transfer(
                            this@LibraTransferActivity,
                            address,
                            amount,
                            bytes,
                            account!!,
                            sbQuota.progress,
                            isToken,
                            tokenId,
                            {
                                dismissProgress()
                                print(it)
                                finish()
                            },
                            {
                                it.message?.let { it1 -> showToast(it1) }
                                dismissProgress()
                                it.printStackTrace()
                            })
                    }
                }
            }.show(supportFragmentManager)
    }

    private fun initViewData() {
        editAddressInput.setText(intent.getStringExtra(EXT_ADDRESS))
    }

    override fun onSelectAddress(address: String) {
        editAddressInput.setText(address)
    }

    override fun onScanAddressQr(address: String) {
        editAddressInput.setText(address)
    }
}
