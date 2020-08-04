package com.violas.wallet.biz.exchange.processor

import com.palliums.content.ContextProvider
import com.quincysx.crypto.CoinTypes
import com.violas.wallet.biz.AccountManager
import com.violas.wallet.biz.exchange.*
import com.violas.wallet.common.SimpleSecurity
import com.violas.wallet.repository.DataRepository
import com.violas.wallet.ui.main.market.bean.IAssetsMark
import com.violas.wallet.ui.main.market.bean.ITokenVo
import com.violas.wallet.ui.main.market.bean.StableTokenVo
import com.violas.walletconnect.extensions.hexStringToByteArray
import org.json.JSONObject
import org.palliums.violascore.crypto.KeyPair
import org.palliums.violascore.transaction.AccountAddress
import org.palliums.violascore.transaction.TransactionPayload
import org.palliums.violascore.transaction.optionTransactionPayload
import org.palliums.violascore.transaction.storage.StructTag
import org.palliums.violascore.transaction.storage.TypeTagStructTag
import org.palliums.violascore.wallet.Account
import java.lang.RuntimeException

class ViolasToAssetsMappingProcessor(
    private val supportMappingPair: HashMap<String, MappingInfo>
) : IProcessor {

    private val mAccountManager by lazy {
        AccountManager()
    }

    private val mViolasRpcService by lazy {
        DataRepository.getViolasChainRpcService()
    }

    private val mLibraRpcService by lazy {
        DataRepository.getLibraService()
    }

    override fun hasHandle(tokenFrom: ITokenVo, tokenTo: ITokenVo): Boolean {
        return tokenFrom is StableTokenVo
                && tokenFrom.coinNumber == CoinTypes.Violas.coinType()
                && supportMappingPair.containsKey(IAssetsMark.convert(tokenTo).mark())
    }

    override suspend fun handle(
        pwd: ByteArray,
        tokenFrom: ITokenVo,
        tokenTo: ITokenVo,
        payee: String?,
        amountIn: Long,
        amountOutMin: Long,
        path: ByteArray,
        data: ByteArray
    ): String {
        tokenFrom as StableTokenVo

        val payeeAddress =
            payee ?: mAccountManager.getIdentityByCoinType(tokenTo.coinNumber)?.address
            ?: throw AccountNotFindAddressException()

        // 检查 Libra 的稳定币有没有 Publish
        if (tokenTo.coinNumber == CoinTypes.Libra.coinType()) {
            tokenTo as StableTokenVo

            // 检查收款地址激活状态
            val accountState =
                mLibraRpcService.getAccountState(payeeAddress)
                    ?: throw AccountPayeeNotFindException()

            // 检查收款地址 Token 注册状态
            var isPublishToken = false
            accountState.balances?.forEach {
                if (it.currency.equals(tokenTo.module, true)) {
                    isPublishToken = true
                }
            }
            if (!isPublishToken) {
                throw AccountPayeeTokenNotActiveException(
                    CoinTypes.Libra,
                    payeeAddress,
                    tokenTo
                )
            }
        }

        val simpleSecurity =
            SimpleSecurity.instance(ContextProvider.getContext())

        val fromAccount = mAccountManager.getIdentityByCoinType(tokenFrom.coinNumber)
            ?: throw AccountNotFindAddressException()
        val privateKey = simpleSecurity.decrypt(pwd, fromAccount.privateKey)
            ?: throw RuntimeException("password error")
        // 开始发起 Violas 交易
        val account = Account(KeyPair.fromSecretKey(privateKey))

        val typeTagFrom = TypeTagStructTag(
            StructTag(
                AccountAddress(tokenFrom.address.hexStringToByteArray()),
                tokenFrom.module,
                tokenFrom.name,
                arrayListOf()
            )
        )

        val subExchangeDate = JSONObject()
        subExchangeDate.put("flag", "violas")
        subExchangeDate.put(
            "type",
            supportMappingPair[IAssetsMark.convert(tokenTo).mark()]?.label
        )
        if (tokenTo.coinNumber == CoinTypes.BitcoinTest.coinType() || tokenTo.coinNumber == CoinTypes.Bitcoin.coinType()) {
            subExchangeDate.put("to_address", payeeAddress)
        } else {
            val authKeyPrefix = "00000000000000000000000000000000"
            subExchangeDate.put("to_address", authKeyPrefix + payeeAddress)
        }
        subExchangeDate.put("state", "start")
        subExchangeDate.put("out_amount", amountOutMin)
        subExchangeDate.put("times", 0)

        val optionTokenSwapTransactionPayload =
            TransactionPayload.optionTransactionPayload(
                ContextProvider.getContext(),
                supportMappingPair[IAssetsMark.convert(tokenTo).mark()]?.receiverAddress ?: "",
                amountIn,
                metaData = subExchangeDate.toString().toByteArray(),
                typeTag = typeTagFrom
            )

        return mViolasRpcService.sendTransaction(
            optionTokenSwapTransactionPayload,
            account,
            gasCurrencyCode = typeTagFrom.value.module
        ).sequenceNumber.toString()
    }
}