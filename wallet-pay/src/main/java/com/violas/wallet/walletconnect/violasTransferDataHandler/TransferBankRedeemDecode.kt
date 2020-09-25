package com.violas.wallet.walletconnect.violasTransferDataHandler

import com.palliums.content.ContextProvider
import com.palliums.violas.smartcontract.ViolasBankContract
import com.palliums.violas.smartcontract.ViolasExchangeContract
import com.quincysx.crypto.utils.Base64
import com.violas.wallet.common.Vm
import com.violas.wallet.walletconnect.BankDepositDatatype
import com.violas.wallet.walletconnect.BankRedeemDatatype
import com.violas.wallet.walletconnect.messageHandler.ProcessedRuntimeException
import com.violas.wallet.walletconnect.TransactionDataType
import com.violas.wallet.walletconnect.TransferDataType
import org.palliums.libracore.move.Move
import org.palliums.violascore.transaction.RawTransaction
import org.palliums.violascore.transaction.TransactionPayload

class TransferBankRedeemDecode(private val transaction: RawTransaction) : TransferDecode {
    private val mViolasBankContract by lazy {
        ViolasBankContract(Vm.TestNet)
    }

    override fun isHandle(): Boolean {
        val payload = transaction.payload?.payload

        return payload is TransactionPayload.Script && payload.code.contentEquals(
            mViolasBankContract.getRedeem2Contract()
        )
    }

    override fun getTransactionDataType(): TransactionDataType {
        return TransactionDataType.VIOLAS_BANK_REDEEM
    }

    override fun handle(): BankRedeemDatatype {
        val payload = transaction.payload?.payload as TransactionPayload.Script
        return try {
            val coinName = decodeCoinName(
                0,
                payload
            )

            BankRedeemDatatype(
                transaction.sender.toHex(),
                payload.args[0].decodeToValue() as Long,
                coinName
            )
        } catch (e: ProcessedRuntimeException) {
            throw ProcessedRuntimeException(
                "bank_redeem contract parameter list(amount: Number,\n" +
                        "    metadata: Bytes)"
            )
        }
    }
}