package com.violas.wallet.repository.http.bitcoin.bitmain

import com.quincysx.crypto.CoinTypes
import com.violas.wallet.common.BaseBrowserUrl
import com.violas.wallet.common.Vm
import com.violas.wallet.repository.http.TransactionRecordService
import com.violas.wallet.ui.transactionRecord.TransactionRecordVO
import com.violas.wallet.ui.transactionRecord.TransactionState
import com.violas.wallet.ui.transactionRecord.TransactionType

/**
 * Created by elephant on 2019-11-07 18:57.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc: 比特大陆 service
 */
class BitmainService(
    private val repository: BitmainRepository
) : TransactionRecordService {

    override suspend fun getTransactionRecords(
        walletAddress: String,
        tokenAddress: String?,
        tokenName: String?,
        transactionType: Int,
        pageSize: Int,
        pageNumber: Int,
        pageKey: Any?,
        onSuccess: (List<TransactionRecordVO>, Any?) -> Unit
    ) {
        val response =
            repository.getTransactionRecords(walletAddress, pageSize, pageNumber)

        if (response.data == null || response.data!!.list.isNullOrEmpty()) {
            onSuccess.invoke(emptyList(), null)
            return
        }

        val list = response.data!!.list!!.mapIndexed { index, dto ->

            // 解析交易状态
            val transactionState = if (dto.confirmations >= 6)
                TransactionState.SUCCESS
            else
                TransactionState.PENDING

            // 解析交易类型，暂时只分收款和付款
            var transactionType = TransactionType.COLLECTION

            var totalInputAmount = 0L
            val inputAddressesExcludeSelf = arrayListOf<String>()
            dto.inputs.forEach { inputInfo ->
                totalInputAmount += inputInfo.prev_value

                inputInfo.prev_addresses.forEach { address ->
                    if (address == walletAddress) {
                        transactionType = TransactionType.TRANSFER
                    } else {
                        inputAddressesExcludeSelf.add(address)
                    }
                }
            }

            var outputAmountToSelf = 0L
            val outputAddressesExcludeSelf = arrayListOf<String>()
            dto.outputs.forEach { outputInfo ->
                var self = false
                outputInfo.addresses.forEach { address ->
                    if (address == walletAddress) {
                        self = true
                    } else {
                        outputAddressesExcludeSelf.add(address)
                    }
                }

                if (self) {
                    outputAmountToSelf += outputInfo.value
                }
            }

            // 解析地址
            val fromAddress =
                if (transactionType == TransactionType.COLLECTION
                    && inputAddressesExcludeSelf.isNotEmpty()
                ) {
                    inputAddressesExcludeSelf[0]
                } else {
                    walletAddress
                }
            val toAddress =
                if (transactionType != TransactionType.COLLECTION
                    && outputAddressesExcludeSelf.isNotEmpty()
                ) {
                    outputAddressesExcludeSelf[0]
                } else {
                    walletAddress
                }

            // 解析展示金额，收款:自己接收的金额, 付款:自己交付的总金额 - 自己接收的金额（系统找零）- 矿工费
            var showAmount = if (transactionType == TransactionType.COLLECTION) {
                outputAmountToSelf
            } else {
                totalInputAmount - outputAmountToSelf - dto.fee
            }
            if (showAmount < 0) {
                showAmount = 0
            }

            TransactionRecordVO(
                id = (pageNumber - 1) * pageSize + index,
                coinType = if (Vm.TestNet) CoinTypes.BitcoinTest else CoinTypes.Bitcoin,
                transactionType = transactionType,
                transactionState = transactionState,
                time = dto.block_time,
                fromAddress = fromAddress,
                toAddress = toAddress,
                amount = showAmount.toString(),
                gas = dto.fee.toString(),
                transactionId = dto.hash,
                url = BaseBrowserUrl.getBitcoinBrowserUrl(dto.hash),
                tokenName = null
            )
        }
        onSuccess.invoke(list, null)
    }
}