package com.violas.wallet.walletconnect.libraTransferDataHandler

import com.google.gson.*
import com.violas.wallet.walletconnect.walletConnectMessageHandler.TransactionDataType
import org.palliums.libracore.serialization.toHex
import org.palliums.libracore.transaction.RawTransaction
import java.lang.reflect.Type


class LibraTransferDecodeEngine(
    private val mRawTransaction: RawTransaction
) {
    private val mDecode: ArrayList<TransferDecode> =
        arrayListOf(
            TransferP2PWithDataDecode(mRawTransaction),
            TransferAddCurrencyToAccountDecode(mRawTransaction)
        )

    fun decode(): Pair<TransactionDataType, String> {
        mDecode.forEach {
            if (it.isHandle()) {
                return Pair(it.getTransactionDataType(), Gson().toJson(it.handle()))
            }
        }
        val gson = GsonBuilder().registerTypeHierarchyAdapter(
            ByteArray::class.java,
            ByteArrayToHexStringTypeAdapter()
        ).create()
        return Pair(
            TransactionDataType.None,
            gson.toJson(mRawTransaction.payload?.payload)
        )
    }

    private class ByteArrayToHexStringTypeAdapter : JsonSerializer<ByteArray?>,
        JsonDeserializer<ByteArray?> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): ByteArray {
            return json.asString.toByteArray()
        }

        override fun serialize(
            src: ByteArray?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.toHex())
        }
    }
}