package com.violas.wallet.repository.http.bank

import com.palliums.net.await

class BankRepository(private val api: BankApi) {

    /**
     * 获取账户信息
     */
    suspend fun getAccountInfo(
        address: String
    ) =
        api.getAccountInfo(address).await().data
            ?: AccountInfoDTO("0", "0", "0", "0")

    /**
     * 获取存款产品列表
     */
    suspend fun getDepositProducts() =
        api.getDepositProducts().await().data ?: emptyList()

    /**
     * 获取存款产品详情
     * @param id 业务 ID
     * @param address
     */
    suspend fun getDepositProductDetails(
        id: String,
        address: String
    ) =
        api.getDepositProductDetails(id, address).await().data


    /**
     * 分页获取存款信息
     */
    suspend fun getDepositInfos(
        address: String,
        limit: Int,
        offset: Int
    ) =
        api.getDepositInfos(address, limit, offset).await().data ?: emptyList()

    /**
     * 分页获取存款记录
     */
    suspend fun getDepositRecords(
        address: String,
        currency: String?,
        state: Int?,
        limit: Int,
        offset: Int
    ) =
        api.getDepositRecords(address, currency, state, limit, offset).await().data
            ?: emptyList()

    /**
     * 获取借贷产品列表
     */
    suspend fun getBorrowingProducts() =
        api.getBorrowingProducts().await().data ?: emptyList()

    /**
     * 获取借贷产品详情
     */
    suspend fun getBorrowProductDetails(
        id: String,
        address: String
    ) =
        api.getBorrowProductDetails(id, address).await().data

    /**
     * 分页获取借贷信息
     */
    suspend fun getBorrowingInfos(
        address: String,
        limit: Int,
        offset: Int
    ) =
        api.getBorrowingInfos(address, limit, offset).await().data ?: emptyList()

    /**
     * 分页获取借贷记录
     */
    suspend fun getBorrowingRecords(
        address: String,
        currency: String?,
        state: Int?,
        limit: Int,
        offset: Int
    ) =
        api.getBorrowingRecords(address, currency, state, limit, offset).await().data
            ?: emptyList()

    /**
     * 分页获取借贷明细
     */
    suspend fun getBorrowingDetails(
        address: String,
        id: String,
        type: Int,
        offset: Int,
        limit: Int
    ) =
        api.getBorrowingDetails(address, id, type, limit, offset).await().data
}