package com.violas.wallet.ui.identity.createIdentity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.palliums.content.App
import com.palliums.utils.*
import com.violas.wallet.R
import com.violas.wallet.base.BaseAppActivity
import com.violas.wallet.biz.AccountManager
import com.violas.wallet.biz.WalletType
import com.violas.wallet.ui.backup.BackupMnemonicFrom
import com.violas.wallet.ui.backup.BackupPromptActivity
import kotlinx.android.synthetic.main.activity_import_identity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateIdentityActivity : BaseAppActivity() {
    companion object {
        private const val EXT_WALLET_TYPE = "ext_wallet_type"

        fun start(context: Context, walletType: WalletType = WalletType.Governor) {
            Intent(context, CreateIdentityActivity::class.java).apply {
                putExtra(EXT_WALLET_TYPE, walletType.type)
            }.start(context)
        }
    }

    private var mCurrentTypeWallet = WalletType.Governor

    override fun getLayoutResId() = R.layout.activity_create_identity

    override fun getPageStyle(): Int {
        return PAGE_STYLE_DARK_BACKGROUND
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_create_the_wallet)

        mCurrentTypeWallet =
            WalletType.parse(intent.getIntExtra(EXT_WALLET_TYPE, WalletType.Governor.type))

        btnConfirm.setOnClickListener {
            val walletName = editName.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val passwordConfirm = editConfirmPassword.text.toString().trim()

            if (walletName.isEmpty()) {
                showToast(getString(R.string.hint_nickname_empty))
                return@setOnClickListener
            }

            try {
                PasswordCheckUtil.check(password)

                if (!password.contentEquals(passwordConfirm)) {
                    showToast(getString(R.string.hint_confirm_password_fault))
                    return@setOnClickListener
                }

                showProgress()
                launch(Dispatchers.IO) {
                    val mnemonicWords = AccountManager().createIdentity(
                        this@CreateIdentityActivity,
                        walletName,
                        mCurrentTypeWallet,
                        password.toByteArray()
                    )
                    withContext(Dispatchers.Main) {
                        dismissProgress()

                        BackupPromptActivity.start(
                            this@CreateIdentityActivity,
                            mnemonicWords as ArrayList<String>,
                            if (mCurrentTypeWallet == WalletType.Governor)
                                BackupMnemonicFrom.CREATE_GOVERNOR_WALLET
                            else
                                BackupMnemonicFrom.CREATE_SSO_WALLET
                        )

                        App.finishAllActivity()
                    }
                }
            } catch (e: Exception) {
                e.message?.let { it1 -> showToast(it1) }
            }
        }
    }

}