package com.violas.wallet.walletconnect.messageHandler

import android.util.Log
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.quincysx.crypto.CoinTypes
import com.violas.wallet.repository.DataRepository
import com.violas.wallet.walletconnect.libraTransferDataHandler.LibraTransferDecodeEngine
import com.violas.wallet.walletconnect.TransactionSwapVo
import com.violas.walletconnect.exceptions.InvalidJsonRpcParamsException
import com.violas.walletconnect.jsonrpc.JsonRpcError
import com.violas.walletconnect.models.WCMethod
import com.violas.walletconnect.models.violasprivate.WCLibraSendTransaction
import kotlinx.coroutines.runBlocking
import org.palliums.libracore.serialization.hexToBytes
import org.palliums.libracore.serialization.toHex
import org.palliums.libracore.transaction.AccountAddress
import org.palliums.libracore.transaction.TransactionArgument
import org.palliums.libracore.transaction.TransactionPayload
import org.palliums.libracore.transaction.lbrStructTagType
import org.palliums.libracore.transaction.storage.StructTag
import org.palliums.libracore.transaction.storage.TypeTag

class LibraSendTransactionMessageHandler : IMessageHandler<JsonArray> {
    private val mAccountStorage by lazy { DataRepository.getAccountStorage() }
    private val mLibraService by lazy { DataRepository.getLibraService() }

    private val mBuilder = GsonBuilder()
    private val mGson = mBuilder
        .serializeNulls()
        .create()

    override fun canHandle(method: WCMethod): Boolean {
        return method == WCMethod.LIBRA_SEND_TRANSACTION
    }

    override fun decodeMessage(requestID: Long, param: JsonArray): TransactionSwapVo {
        val tx = mGson.fromJson<List<WCLibraSendTransaction>>(param).firstOrNull()
            ?: throw InvalidJsonRpcParamsException(requestID)
        val account = mAccountStorage.findByCoinTypeAndCoinAddress(
            CoinTypes.Libra.coinType(),
            tx.from
        )

        if (account == null) {
            throw InvalidParameterErrorMessage(requestID, "Account does not exist.")
        }

        val gasUnitPrice = tx.gasUnitPrice ?: 0
        val maxGasAmount = tx.maxGasAmount ?: 1_000_000
        val expirationTime = tx.expirationTime ?: System.currentTimeMillis() + 1000
        val gasCurrencyCode = tx.gasCurrencyCode ?: lbrStructTagType()
        val sequenceNumber = tx.sequenceNumber ?: -1
        val chainId = tx.chainId

        val payload = TransactionPayload.Script(
            try {
                tx.payload.code.hexToBytes()
            } catch (e: Exception) {
                throw InvalidParameterErrorMessage(
                    requestID,
                    "Payload code parameter error in transaction."
                )
            },
            try {
                tx.payload.tyArgs.map {
                    TypeTag.newStructTag(
                        StructTag(
                            AccountAddress(it.address.hexToBytes()),
                            it.module,
                            it.name,
                            arrayListOf()
                        )
                    )
                }
            } catch (e: Exception) {
                throw InvalidParameterErrorMessage(
                    requestID,
                    "Payload tyArgs parameter error in transaction."
                )
            },
            tx.payload.args.map {
                when (it.type.toLowerCase()) {
                    "address" -> {
                        try {
                            TransactionArgument.newAddress(it.value)
                        } catch (e: Exception) {
                            throw InvalidParameterErrorMessage(
                                requestID,
                                "Payload args Address parameter type error in transaction."
                            )
                        }
                    }
                    "bool" -> {
                        try {
                            TransactionArgument.newBool(it.value.toBoolean())
                        } catch (e: Exception) {
                            throw InvalidParameterErrorMessage(
                                requestID, "Payload args Bool parameter type error in transaction."
                            )
                        }
                    }
                    "u8" -> {
                        try {
                            TransactionArgument.newU8(it.value.toInt())
                        } catch (e: Exception) {
                            throw InvalidParameterErrorMessage(
                                requestID,
                                "Payload args U8 parameter type error in transaction. is positive integer."
                            )
                        }
                    }
                    "u64" -> {
                        try {
                            TransactionArgument.newU64(it.value.toLong())
                        } catch (e: Exception) {
                            throw InvalidParameterErrorMessage(
                                requestID,
                                "Payload args U64 parameter type error in transaction. is positive integer."
                            )
                        }
                    }
                    "u128" -> {
                        try {
                            TransactionArgument.newU128(it.value.toBigInteger())
                        } catch (e: Exception) {
                            throw InvalidParameterErrorMessage(
                                requestID,
                                "Payload args U128 parameter type error in transaction. is positive integer."
                            )
                        }
                    }
                    "vector" -> {
                        try {
                            TransactionArgument.newByteArray(it.value.hexToBytes())
                        } catch (e: Exception) {
                            throw InvalidParameterErrorMessage(
                                requestID, "Payload args Bytes parameter type error in transaction."
                            )
                        }
                    }
                    else -> {
                        throw InvalidParameterErrorMessage(
                            requestID, "Payload args Unknown type in transaction."
                        )
                    }
                }
            }
        )

        Log.e("WalletConnect", Gson().toJson(payload))

        val generateRawTransaction = runBlocking {
            mLibraService.generateRawTransaction(
                TransactionPayload(payload),
                tx.from,
                sequenceNumber,
                gasCurrencyCode,
                maxGasAmount,
                gasUnitPrice,
                expirationTime - System.currentTimeMillis(),
                chainId
            )
        }

        val decode = try {
            LibraTransferDecodeEngine(generateRawTransaction).decode()
        } catch (e: ProcessedRuntimeException) {
            throw WalletConnectErrorMessage(
                requestID,
                JsonRpcError.invalidParams("Invalid Parameter:${e.message}")
            )
        }

        return TransactionSwapVo(
            requestID,
            generateRawTransaction.toByteArray().toHex(),
            true,
            false,
            account.id,
            CoinTypes.Libra,
            decode.first.value,
            decode.second
        )
    }
}