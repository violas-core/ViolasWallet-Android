package com.violas.wallet.biz.exchangeMapping.transactionProcessor

import androidx.annotation.WorkerThread
import com.palliums.content.ContextProvider
import com.palliums.utils.getString
import com.violas.wallet.R
import com.violas.wallet.biz.LackOfBalanceException
import com.violas.wallet.biz.TokenManager
import com.violas.wallet.biz.exchangeMapping.LibraMappingToken
import com.violas.wallet.biz.exchangeMapping.MappingAccount
import com.violas.wallet.biz.exchangeMapping.ViolasMappingToken
import com.violas.wallet.repository.DataRepository
import org.json.JSONObject
import org.palliums.libracore.crypto.KeyPair
import org.palliums.libracore.serialization.toHex
import org.palliums.libracore.transaction.*
import org.palliums.libracore.wallet.Account
import java.math.BigDecimal

class TransactionProcessorLibraTovLibra() : TransactionProcessor {
    private val mTokenManager by lazy {
        TokenManager()
    }
    private val mLibraService by lazy {
        DataRepository.getLibraService()
    }

    override fun dispense(sendAccount: MappingAccount, receiveAccount: MappingAccount): Boolean {
        return sendAccount is LibraMappingToken
                && sendAccount.isSendAccount()
                && receiveAccount is ViolasMappingToken
                && receiveAccount.isSendAccount()
    }

    @WorkerThread
    @Throws(Exception::class)
    override suspend fun handle(
        sendAccount: MappingAccount,
        receiveAccount: MappingAccount,
        sendAmount: BigDecimal,
        receiveAddress: String
    ): String {
        val sendAccount = sendAccount as LibraMappingToken
        val receiveAccount = receiveAccount as ViolasMappingToken

        var balance = mLibraService.getBalance(
            sendAccount.getAddress().toHex()
        ).let { BigDecimal(it) }

        if (sendAmount > balance) {
            throw LackOfBalanceException()
        }
        val checkTokenRegister = mTokenManager.isPublish(receiveAccount.getAddress().toHex())

        if (!checkTokenRegister) {
            try {
                publishToken(
                    org.palliums.violascore.wallet.Account(
                        org.palliums.violascore.crypto.KeyPair.fromSecretKey(
                            receiveAccount.getPrivateKey()!!
                        )
                    )
                )
            } catch (e: Exception) {
                throw RuntimeException(
                    getString(R.string.hint_exchange_error)
                )
            }
        }
        val authKeyPrefix = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        val subExchangeDate = JSONObject()
        subExchangeDate.put("flag", "libra")
        subExchangeDate.put("type", "l2v")
        subExchangeDate.put("to_address", (authKeyPrefix + receiveAccount.getAddress()).toHex())
        subExchangeDate.put("state", "start")

        val transactionPayload =
            TransactionPayload.optionTransactionPayload(
                ContextProvider.getContext(),
                receiveAddress,
                sendAmount.multiply(BigDecimal("1000000")).toLong(),
                subExchangeDate.toString().toByteArray()
            )

        mLibraService.sendTransaction(
            transactionPayload,
            Account(KeyPair.fromSecretKey(sendAccount.getPrivateKey()!!))
        )
        return ""
    }

    private suspend fun publishToken(mAccount: org.palliums.violascore.wallet.Account) {
        return mTokenManager.publishToken(mAccount)
    }
}