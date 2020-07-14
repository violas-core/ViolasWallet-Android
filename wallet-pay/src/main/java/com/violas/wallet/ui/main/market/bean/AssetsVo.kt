package com.violas.wallet.ui.main.market.bean

import com.quincysx.crypto.CoinTypes
import java.lang.RuntimeException

interface IAssetsMark {
    companion object {
        fun convert(iTokenVo: ITokenVo): IAssetsMark {
            return if (iTokenVo is PlatformTokenVo) {
                CoinAssetsMark(CoinTypes.parseCoinType(iTokenVo.coinNumber))
            } else if (iTokenVo is StableTokenVo) {
                LibraTokenAssetsMark(
                    CoinTypes.parseCoinType(iTokenVo.coinNumber),
                    iTokenVo.module,
                    iTokenVo.address,
                    iTokenVo.module
                )
            } else {
                throw RuntimeException("不支持的")
            }
        }
    }

    fun mark(): String
}

class CoinAssetsMark(val coinTypes: CoinTypes) : IAssetsMark {
    override fun mark(): String {
        return "c${coinTypes}"
    }

    override fun equals(other: Any?): Boolean {
        return (other is CoinAssetsMark) && coinTypes == other.coinTypes
    }
}

class LibraTokenAssetsMark(
    val coinTypes: CoinTypes,
    val module: String,
    val address: String,
    val name: String
) : IAssetsMark {
    override fun mark(): String {
        return "lt${coinTypes}${module}${address}${name}"
    }

    override fun equals(other: Any?): Boolean {
        return (other is LibraTokenAssetsMark)
                && coinTypes == other.coinTypes
                && module == other.module
                && address == other.address
                && name == other.name
    }
}