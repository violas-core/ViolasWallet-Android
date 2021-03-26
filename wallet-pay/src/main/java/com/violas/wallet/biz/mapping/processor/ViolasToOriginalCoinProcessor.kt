package com.violas.wallet.biz.mapping.processor

import com.palliums.content.ContextProvider
import com.violas.wallet.biz.WrongPasswordException
import com.violas.wallet.biz.bean.DiemAppToken
import com.violas.wallet.biz.transaction.DiemTxnManager
import com.violas.wallet.biz.transaction.ViolasTxnManager
import com.violas.wallet.common.*
import com.violas.wallet.repository.DataRepository
import com.violas.wallet.repository.database.entity.AccountDO
import com.violas.wallet.repository.http.mapping.MappingCoinPairDTO
import com.violas.wallet.utils.str2CoinType
import com.violas.walletconnect.extensions.hexStringToByteArray
import org.json.JSONObject
import org.palliums.libracore.http.LibraRpcService
import org.palliums.violascore.crypto.KeyPair
import org.palliums.violascore.transaction.AccountAddress
import org.palliums.violascore.transaction.TransactionPayload
import org.palliums.violascore.transaction.optionTransactionPayload
import org.palliums.violascore.transaction.storage.StructTag
import org.palliums.violascore.transaction.storage.TypeTag
import org.palliums.violascore.wallet.Account

/**
 * Created by elephant on 2020/8/13 17:13.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc:
 */
class ViolasToOriginalCoinProcessor(
    private val mDiemRpcService: LibraRpcService
) : MappingProcessor {

    private val mViolasRpcService by lazy { DataRepository.getViolasRpcService() }

    override fun hasMappable(coinPair: MappingCoinPairDTO): Boolean {
        return str2CoinType(coinPair.fromCoin.chainName) == getViolasCoinType() &&
                when (str2CoinType(coinPair.toCoin.chainName)) {
                    getBitcoinCoinType(), getDiemCoinType(), getEthereumCoinType() -> true
                    else -> false
                }
    }

    override suspend fun mapping(
        checkPayeeAccount: Boolean,
        payeeAddress: String?,
        payeeAccountDO: AccountDO?,
        payerAccountDO: AccountDO,
        password: ByteArray,
        mappingAmount: Long,
        coinPair: MappingCoinPairDTO
    ): String {
        if (checkPayeeAccount && str2CoinType(coinPair.toCoin.chainName) == getDiemCoinType()) {
            // 检查Diem收款人账户
            DiemTxnManager().getReceiverAccountState(
                payeeAddress ?: payeeAccountDO!!.address,
                DiemAppToken.convert(coinPair.toCoin.assets)
            ) {
                mDiemRpcService.getAccountState(it)
            }
        }

        val privateKey = SimpleSecurity.instance(ContextProvider.getContext())
            .decrypt(password, payerAccountDO.privateKey) ?: throw WrongPasswordException()
        val payerAccount = Account(KeyPair.fromSecretKey(privateKey))

        // 检查Violas付款人账户
        val violasTxnManager = ViolasTxnManager()
        val payerAccountState = violasTxnManager.getSenderAccountState(payerAccount) {
            mViolasRpcService.getAccountState(it)
        }

        // 计算gas info
        val gasInfo = violasTxnManager.calculateGasInfo(
            payerAccountState,
            listOf(Pair(coinPair.fromCoin.assets.module, mappingAmount))
        )

        // 构建映射协议数据
        val subMappingDate = JSONObject()
        subMappingDate.put("flag", "violas")
        subMappingDate.put("type", coinPair.mappingType)
        subMappingDate.put(
            "to_address",
            when (str2CoinType(coinPair.toCoin.chainName)) {
                getBitcoinCoinType() ->
                    payeeAccountDO!!.address
                getEthereumCoinType() ->
                    payeeAddress!!
                else ->
                    "00000000000000000000000000000000${payeeAccountDO!!.address}"
            }
        )
        subMappingDate.put("state", "start")
        subMappingDate.put("times", 0)

        val transferTransactionPayload = TransactionPayload.optionTransactionPayload(
            ContextProvider.getContext(),
            coinPair.receiverAddress,
            mappingAmount,
            metaData = subMappingDate.toString().toByteArray(),
            typeTag = TypeTag.newStructTag(
                StructTag(
                    AccountAddress(coinPair.fromCoin.assets.address.hexStringToByteArray()),
                    coinPair.fromCoin.assets.module,
                    coinPair.fromCoin.assets.name,
                    arrayListOf()
                )
            )
        )

        return mViolasRpcService.sendTransaction(
            transferTransactionPayload,
            payerAccount,
            sequenceNumber = payerAccountState.sequenceNumber,
            gasCurrencyCode = gasInfo.gasCurrencyCode,
            maxGasAmount = gasInfo.maxGasAmount,
            gasUnitPrice = gasInfo.gasUnitPrice,
            chainId = getViolasChainId()
        ).sequenceNumber.toString()
    }
}