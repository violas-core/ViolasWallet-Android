package com.violas.wallet.biz.bank

import com.palliums.content.ContextProvider
import com.palliums.violas.error.ViolasException
import com.palliums.violas.smartcontract.ViolasBankContract
import com.violas.wallet.common.SimpleSecurity
import com.violas.wallet.common.Vm
import com.violas.wallet.repository.DataRepository
import com.violas.wallet.repository.database.entity.AccountDO
import com.violas.wallet.ui.main.market.bean.LibraTokenAssetsMark
import com.violas.walletconnect.extensions.hexStringToByteArray
import org.palliums.violascore.crypto.KeyPair
import org.palliums.violascore.transaction.AccountAddress
import org.palliums.violascore.transaction.storage.StructTag
import org.palliums.violascore.transaction.storage.TypeTagStructTag
import org.palliums.violascore.wallet.Account

class BankManager {
    private val mViolasRPCService by lazy { DataRepository.getViolasChainRpcService() }
    private val mBankService by lazy { DataRepository.getBankService() }
    private val mViolasBankContract by lazy { ViolasBankContract(Vm.TestNet) }

    /**
     * 借款
     */
    @Throws(ViolasException::class)
    suspend fun borrow(
        password: ByteArray,
        payerAccountDO: AccountDO,
        productId: String,
        assetsMark: LibraTokenAssetsMark,
        amount: Long
    ): String {
        val payerPrivateKey = SimpleSecurity.instance(ContextProvider.getContext())
            .decrypt(password, payerAccountDO.privateKey)!!

        val typeTagFrom = TypeTagStructTag(
            StructTag(
                AccountAddress(assetsMark.address.hexStringToByteArray()),
                assetsMark.module,
                assetsMark.name,
                arrayListOf()
            )
        )

        val optionBorrowTransactionPayload = mViolasBankContract.optionBorrowTransactionPayload(
            typeTagFrom,
            amount
        )

        val generateTransaction = mViolasRPCService.generateTransaction(
            optionBorrowTransactionPayload,
            Account(KeyPair.fromSecretKey(payerPrivateKey)),
            gasCurrencyCode = typeTagFrom.value.module,
            chainId = Vm.ViolasChainId
        )
        mBankService.submitBorrowTransaction(
            generateTransaction.sender,
            productId,
            amount,
            generateTransaction.signTxn
        )
        return generateTransaction.sequenceNumber.toString()
    }

    /**
     * 还款
     */
    @Throws(ViolasException::class)
    suspend fun repayBorrow(
        password: ByteArray,
        payerAccountDO: AccountDO,
        productId: String,
        assetsMark: LibraTokenAssetsMark,
        amount: Long
    ): String {
        val payerPrivateKey = SimpleSecurity.instance(ContextProvider.getContext())
            .decrypt(password, payerAccountDO.privateKey)!!

        val typeTagFrom = TypeTagStructTag(
            StructTag(
                AccountAddress(assetsMark.address.hexStringToByteArray()),
                assetsMark.module,
                assetsMark.name,
                arrayListOf()
            )
        )

        val optionRepayBorrowTransactionPayload =
            mViolasBankContract.optionRepayBorrowTransactionPayload(
                typeTagFrom,
                amount
            )

        val generateTransaction = mViolasRPCService.generateTransaction(
            optionRepayBorrowTransactionPayload,
            Account(KeyPair.fromSecretKey(payerPrivateKey)),
            gasCurrencyCode = typeTagFrom.value.module,
            chainId = Vm.ViolasChainId
        )
        mBankService.submitRepayBorrowTransaction(
            generateTransaction.sender,
            productId,
            amount,
            generateTransaction.signTxn
        )
        return generateTransaction.sequenceNumber.toString()
    }

    /**
     * 存款
     */
    @Throws(ViolasException::class)
    suspend fun lock(
        password: ByteArray,
        payerAccountDO: AccountDO,
        productId: String,
        assetsMark: LibraTokenAssetsMark,
        amount: Long
    ): String {
        val payerPrivateKey = SimpleSecurity.instance(ContextProvider.getContext())
            .decrypt(password, payerAccountDO.privateKey)!!

        val typeTagFrom = TypeTagStructTag(
            StructTag(
                AccountAddress(assetsMark.address.hexStringToByteArray()),
                assetsMark.module,
                assetsMark.name,
                arrayListOf()
            )
        )

        val optionLockBorrowTransactionPayload =
            mViolasBankContract.optionLockTransactionPayload(
                typeTagFrom,
                amount
            )

        val generateTransaction = mViolasRPCService.generateTransaction(
            optionLockBorrowTransactionPayload,
            Account(KeyPair.fromSecretKey(payerPrivateKey)),
            gasCurrencyCode = typeTagFrom.value.module,
            chainId = Vm.ViolasChainId
        )
        mBankService.submitDepositTransaction(
            generateTransaction.sender,
            productId,
            amount,
            generateTransaction.signTxn
        )
        return generateTransaction.sequenceNumber.toString()
    }

    /**
     * 提款
     */
    @Throws(ViolasException::class)
    suspend fun redeem(
        password: ByteArray,
        payerAccountDO: AccountDO,
        productId: String,
        assetsMark: LibraTokenAssetsMark,
        amount: Long
    ): String {
        val payerPrivateKey = SimpleSecurity.instance(ContextProvider.getContext())
            .decrypt(password, payerAccountDO.privateKey)!!

        val typeTagFrom = TypeTagStructTag(
            StructTag(
                AccountAddress(assetsMark.address.hexStringToByteArray()),
                assetsMark.module,
                assetsMark.name,
                arrayListOf()
            )
        )

        val optionRedeemBorrowTransactionPayload =
            mViolasBankContract.optionRedeemTransactionPayload(
                typeTagFrom,
                amount
            )

        val generateTransaction = mViolasRPCService.generateTransaction(
            optionRedeemBorrowTransactionPayload,
            Account(KeyPair.fromSecretKey(payerPrivateKey)),
            gasCurrencyCode = typeTagFrom.value.module,
            chainId = Vm.ViolasChainId
        )
        mBankService.submitRedeemTransaction(
            generateTransaction.sender,
            productId,
            amount,
            generateTransaction.signTxn
        )
        return generateTransaction.sequenceNumber.toString()
    }

    /**
     * 提取挖矿奖励
     */
    @Throws(ViolasException::class)
    suspend fun withdrawReward(
        privateKey: ByteArray,
    ) {
        val withdrawRewardTransactionPayload =
            mViolasBankContract.optionWithdrawRewardTransactionPayload()

        mViolasRPCService.sendTransaction(
            payload = withdrawRewardTransactionPayload,
            account = Account(KeyPair.fromSecretKey(privateKey)),
            gasCurrencyCode = "vls",
            chainId = Vm.ViolasChainId
        )
    }
}