package com.violas.wallet.biz.btc.outputScript

import com.quincysx.crypto.bitcoin.BitcoinOutputStream
import com.quincysx.crypto.bitcoin.script.Script
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ViolasOutputScript {
    companion object {
        const val OP_VER: Int = 0x0001
        val OP_TYPE_START: ByteArray = byteArrayOf(0x30, 0x00)
        val OP_TYPE_END: ByteArray = byteArrayOf(0x30, 0x01)
        val TYPE_CANCEL: ByteArray = byteArrayOf(0x30, 0x02)
    }

    /**
     * 创建跨链兑换交易
     * @param address 接收地址
     * @param vtokenAddress Token 地址
     */
    fun requestExchange(
        address: ByteArray,
        vtokenAddress: ByteArray,
        sequence: Long = System.currentTimeMillis()
    ): Script {
        val authKeyPrefix = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        val dataStream = BitcoinOutputStream()
        dataStream.write("violas".toByteArray())
        //dataStream.writeInt16(OP_VER)
        // 此处非 BTC 小端字节规则，需要注意
        writeInt16(OP_VER, dataStream)

        dataStream.write(OP_TYPE_START)
        dataStream.write(authKeyPrefix + address)
        dataStream.write(
            ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(sequence).array()
        )
        dataStream.write(authKeyPrefix + vtokenAddress)

        val scriptStream = BitcoinOutputStream()
        scriptStream.write(Script.OP_RETURN.toInt())
        Script.writeBytes(dataStream.toByteArray(), scriptStream)
        return Script(scriptStream.toByteArray())
    }

    fun cancelExchange(address: ByteArray, sequence: Long = System.currentTimeMillis()): Script {
        val dataStream = BitcoinOutputStream()
        dataStream.write("violas".toByteArray())
        //dataStream.writeInt16(OP_VER)
        // 此处非 BTC 小端字节规则，需要注意
        writeInt16(OP_VER, dataStream)

        dataStream.write(TYPE_CANCEL)
        dataStream.write(address)
        dataStream.write(
            ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(sequence).array()
        )

        val scriptStream = BitcoinOutputStream()
        scriptStream.write(Script.OP_RETURN.toInt())
        Script.writeBytes(dataStream.toByteArray(), scriptStream)
        return Script(scriptStream.toByteArray())
    }

    // 大端顺序 write Int16
    private fun writeInt16(value: Int, output: BitcoinOutputStream) {
        output.write(OP_VER shr 8 and 0xff)
        output.write(OP_VER and 0xff)
    }
}