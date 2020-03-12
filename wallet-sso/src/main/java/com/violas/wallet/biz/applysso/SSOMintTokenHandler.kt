package com.violas.wallet.biz.applysso

import androidx.annotation.WorkerThread
import com.violas.wallet.biz.applysso.handler.MintTokenHandler
import com.violas.wallet.biz.applysso.handler.PublishTokenHandler
import com.violas.wallet.biz.applysso.handler.SendMintTokenSuccessHandler
import com.violas.wallet.repository.DataRepository
import org.palliums.violascore.wallet.Account
import org.palliums.violascore.wallet.LibraWallet
import org.palliums.violascore.wallet.WalletConfig

/**
 * 基于 SSO 相关信息，恢复派生账户。
 *
 * 铸币账户给 SSO 申请账户铸币
 * 本地记录某一层铸币账户已经铸币成功
 *
 * 通知服务器铸币成功通过
 * 删除本地记录某一层铸币账户的铸币记录
 */
class SSOMintTokenHandler(
    private val account: Account,
    private val mnemonics: List<String>,
    private val SSOApplyWalletAddress: String,
    private val SSOApplyAmount: Long,
    private val mintTokenAddress: String,
    private val walletLayersNumber: Long
) {

    @WorkerThread
    suspend fun exec(): Boolean {
        val applyEngine = ApplyEngine()
        val findUnDoneRecord = applyEngine.getUnMintRecord(
            account.getAddress().toHex(),
            mintTokenAddress,
            SSOApplyWalletAddress
        )
        val layerWallet = findUnDoneRecord?.childNumber ?: walletLayersNumber

        val mintAccount = LibraWallet(WalletConfig(mnemonics)).generateAccount(layerWallet)

        applyEngine.addApplyHandle(
            PublishTokenHandler(
                account.getAddress().toHex(),
                layerWallet,
                mintTokenAddress,
                mintAccount
            )
        )

        applyEngine.addApplyHandle(
            MintTokenHandler(
                account.getAddress().toHex(),
                layerWallet,
                mintTokenAddress,
                mintAccount,
                SSOApplyWalletAddress,
                SSOApplyAmount
            )
        )

        applyEngine.addApplyHandle(
            SendMintTokenSuccessHandler(
                account.getAddress().toHex(),
                layerWallet,
                SSOApplyWalletAddress
            )
        )

        return applyEngine.execMint(findUnDoneRecord?.status)
    }
}