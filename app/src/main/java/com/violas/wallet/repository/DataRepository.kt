package com.violas.wallet.repository

import com.violas.wallet.getContext
import com.violas.wallet.repository.database.AppDatabase
import com.violas.wallet.repository.http.btcBrowser.request.BlockChainRequest
import io.grpc.ManagedChannelBuilder
import org.palliums.libracore.admissioncontrol.LibraAdmissionControl

object DataRepository {
    private val appDatabase by lazy {
        AppDatabase.getInstance(getContext())
    }

    private val mChannel by lazy {
        ManagedChannelBuilder.forAddress("ac.testnet.libra.org", 8000)
            .usePlaintext()
            .build()
    }

    fun getAccountStorage() = appDatabase.accountDao()

    fun getAddressBookStorage() = appDatabase.addressBookDao()

    fun getBitcoinService() = BlockChainRequest.get()

    fun getLibraService() = LibraAdmissionControl(mChannel)
}