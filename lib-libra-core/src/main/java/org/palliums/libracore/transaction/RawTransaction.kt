package org.palliums.libracore.transaction

import org.palliums.libracore.serialization.LCSInputStream
import org.palliums.libracore.serialization.LCSOutputStream
import org.palliums.libracore.transaction.storage.TypeTag
import org.palliums.libracore.wallet.TransactionAuthenticator
import types.AccessPathOuterClass.AccessPath


data class RawTransaction(
    val sender: AccountAddress,
    val sequence_number: Long,
    val payload: TransactionPayload?,
    val max_gas_amount: Long,
    val gas_unit_price: Long,
    val gas_specifier: TypeTag,
    val expiration_time: Long
) {
    fun toByteArray(): ByteArray {
        val stream = LCSOutputStream()
        stream.write(sender.toByteArray())
        stream.writeLong(sequence_number)
        payload?.let {
            stream.write(payload.toByteArray())
        }
        stream.writeLong(max_gas_amount)
        stream.writeLong(gas_unit_price)
        stream.write(gas_specifier.toByteArray())
        stream.writeLong(expiration_time)
        return stream.toByteArray()
    }

    companion object {
        fun decode(input: LCSInputStream): RawTransaction {
            return RawTransaction(
                AccountAddress.decode(input),
                input.readLong(),
                TransactionPayload.decode(input),
                input.readLong(),
                input.readLong(),
                TypeTag.decode(input),
                input.readLong()
            )
        }
    }
}

data class SignedTransaction(
    val rawTxn: RawTransaction,
    val authenticator: TransactionAuthenticator
) {
    val transactionLength: Long

    init {
        transactionLength = rawTxn.toByteArray().size.toLong()
    }

    fun toByteArray(): ByteArray {
        val stream = LCSOutputStream()
        stream.write(rawTxn.toByteArray())
        stream.write(authenticator.toByteArray())
        return stream.toByteArray()
    }

    companion object {
        fun decode(array: ByteArray): SignedTransaction {
            LCSInputStream(array).use {
                return SignedTransaction(
                    RawTransaction.decode(it),
                    TransactionAuthenticator.decode(it)
                )
            }
        }
    }
}

data class AccountAddress(val byte: ByteArray) {
    fun toByteArray(): ByteArray {
        return byte
    }

    companion object {
        val DEFAULT = AccountAddress(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))

        fun decode(input: LCSInputStream): AccountAddress {
            val value = ByteArray(32)
            input.read(value)
            return AccountAddress(
                value
            )
        }
    }
}

fun AccessPath.toLcsBytes(): ByteArray {
    val stream = LCSOutputStream()
    stream.writeBytes(this.address.toByteArray())
    stream.writeBytes(this.path.toByteArray())
    return stream.toByteArray()
}
