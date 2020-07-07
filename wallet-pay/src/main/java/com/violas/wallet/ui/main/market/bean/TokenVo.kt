package com.violas.wallet.ui.main.market.bean

import com.violas.wallet.repository.database.entity.AccountType


/**
 * Created by elephant on 2020/7/6 19:03.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc:
 */

/**
 * 市场支持的币
 */
abstract class ITokenVo(
    val accountDoId: Long,          // 本地账户id
    val coinNumber: Int,            // 币种类型值
    val displayName: String,        // 币的显示名称
    val logoUrl: String,            // 币的logo url
    var amount: Long,               // 币的金额
    var anchorValue: Double,        // 币的锚定价值
    var selected: Boolean           // 币是否选中
) {
    abstract fun areContentsTheSame(another: ITokenVo): Boolean
}

/**
 * 市场支持的平台币
 */
class PlatformTokenVo(
    accountDoId: Long,
    val accountType: AccountType,   // 账户类型
    val accountAddress: String,     // 账户地址，即钱包地址
    coinNumber: Int,
    displayName: String,
    logoUrl: String,
    amount: Long = 0,
    anchorValue: Double = 0.00,
    selected: Boolean = false
) : ITokenVo(accountDoId, coinNumber, displayName, logoUrl, amount, anchorValue, selected) {

    override fun areContentsTheSame(another: ITokenVo): Boolean {
        return if (another is PlatformTokenVo)
            accountDoId == another.accountDoId
                    && accountType == another.accountType
                    && accountAddress == another.accountAddress
                    && coinNumber == another.coinNumber
                    && displayName == another.displayName
                    && logoUrl == another.logoUrl
                    && amount == another.amount
                    && anchorValue == another.anchorValue
                    && selected == another.selected
        else
            false
    }

    override fun hashCode(): Int {
        var result = accountDoId.hashCode()
        result = result * 31 + accountType.hashCode()
        result = result * 31 + accountAddress.hashCode()
        result = result * 31 + coinNumber.hashCode()
        return result
    }
}

/**
 * 市场支持的稳定币
 */
class StableTokenVo(
    accountDoId: Long,
    coinNumber: Int,
    val marketIndex: Int,           // 市场支持的币种的索引
    val tokenDoId: Long,            // 本地token的id
    val address: String,            // 稳定币的地址
    val module: String,             // 稳定币的module名
    val name: String,               // 稳定币的名称
    displayName: String,
    logoUrl: String,
    val localEnable: Boolean,       // 本地启用（本地是否显示）
    val chainEnable: Boolean,       // 链上启用（是否已添加到账户）
    amount: Long = 0,
    anchorValue: Double = 0.00,
    selected: Boolean = false
) : ITokenVo(accountDoId, coinNumber, displayName, logoUrl, amount, anchorValue, selected) {

    override fun hashCode(): Int {
        var result = accountDoId.hashCode()
        result = result * 31 + coinNumber.hashCode()
        result = result * 31 + tokenDoId.hashCode()
        result = result * 31 + address.hashCode()
        result = result * 31 + module.hashCode()
        result = result * 31 + name.hashCode()
        return result
    }

    override fun areContentsTheSame(another: ITokenVo): Boolean {
        return if (another is StableTokenVo)
            accountDoId == another.accountDoId
                    && coinNumber == another.coinNumber
                    && marketIndex == another.marketIndex
                    && tokenDoId == another.tokenDoId
                    && address == another.address
                    && module == another.module
                    && name == another.name
                    && displayName == another.displayName
                    && logoUrl == another.logoUrl
                    && localEnable == another.localEnable
                    && chainEnable == another.chainEnable
                    && amount == another.amount
                    && anchorValue == another.anchorValue
                    && selected == another.selected
        else
            false
    }
}