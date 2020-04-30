package com.violas.wallet.biz.governorApproval.task

class SendMintTokenSuccessTask(
    private val walletAddress: String,
    private val ssoApplicationId: String,
    private val ssoWalletAddress: String
) : ApprovalTask() {

    override suspend fun handle() {
        getServiceProvider()!!.getGovernorService()
            .changeSSOApplicationToMinted(
                ssoApplicationId,
                ssoWalletAddress
            )

        getServiceProvider()!!.getApplySsoRecordDao()
            .remove(
                walletAddress,
                ssoApplicationId
            )
    }
}