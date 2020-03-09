package com.violas.wallet.ui.main.message

import android.content.Context
import androidx.lifecycle.*
import com.palliums.net.postTipsMessage
import com.palliums.paging.PagingViewModel
import com.violas.wallet.biz.AccountManager
import com.violas.wallet.biz.GovernorManager
import com.violas.wallet.event.UpdateGovernorInfoEvent
import com.violas.wallet.repository.database.entity.AccountDO
import com.violas.wallet.repository.database.entity.SSOApplicationMsgDO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.palliums.violascore.wallet.Account

class ApplyMessageViewModel : PagingViewModel<SSOApplicationMsgVO>() {

    /**
     * 申请州长牌照状态
     */
    val mAccountLD = MutableLiveData<AccountDO>()
    val mChangedSSOApplicationMsgLD = MediatorLiveData<SSOApplicationMsgDO>()
    val mTipsMessageLD by lazy { EnhancedMutableLiveData<String>() }
    var mGovernorApplicationStatus = -2

    private var mLastObservedMsgApplicationId: String? = null
    private var mLastChangedSSOApplicationMsgLD: LiveData<SSOApplicationMsgDO?>? = null
    private val mGovernorManager by lazy { GovernorManager() }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAccount = AccountManager().currentAccount()
            mAccountLD.postValue(currentAccount)
        }
    }

    fun observeChangedSSOApplicationMsg(observedMsg: SSOApplicationMsgVO) {
        synchronized(this) {
            if (mLastObservedMsgApplicationId == observedMsg.applicationId) {
                return
            }

            mLastChangedSSOApplicationMsgLD?.let {
                mChangedSSOApplicationMsgLD.removeSource(it)
            }
            mLastObservedMsgApplicationId = null
            mLastChangedSSOApplicationMsgLD = null

            mAccountLD.value?.let { account ->
                val msgLD =
                    mGovernorManager.getSSOApplicationMsgLiveData(
                        account.id,
                        observedMsg.applicationId
                    )
                mChangedSSOApplicationMsgLD.addSource(msgLD) { msg ->
                    msg?.let { mChangedSSOApplicationMsgLD.value = it }
                }
                mLastObservedMsgApplicationId = observedMsg.applicationId
                mLastChangedSSOApplicationMsgLD = msgLD
            }
        }
    }

    fun loadGovernorApplicationProgress(
        failureCallback: ((error: Throwable) -> Unit)? = null,
        successCallback: ((applicationStatus: Int) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val governorInfo =
                    mGovernorManager.getGovernorInfo(mAccountLD.value!!)

                // 发送更新州长信息事件
                EventBus.getDefault().post(UpdateGovernorInfoEvent(governorInfo))

                mGovernorApplicationStatus = governorInfo.applicationStatus
                successCallback?.invoke(mGovernorApplicationStatus)
            } catch (e: Exception) {
                failureCallback?.invoke(e)
                postTipsMessage(mTipsMessageLD, e)
            }
        }
    }

    fun publishVStake(
        context: Context,
        account: Account,
        failureCallback: ((error: Throwable) -> Unit)? = null,
        successCallback: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                mGovernorManager.publishVStake(context, account)

                successCallback?.invoke()
            } catch (e: Exception) {
                failureCallback?.invoke(e)
                postTipsMessage(mTipsMessageLD, e)
            }
        }
    }

    override suspend fun loadData(
        pageSize: Int,
        pageNumber: Int,
        pageKey: Any?,
        onSuccess: (List<SSOApplicationMsgVO>, Any?) -> Unit
    ) {
        val msgs =
            mGovernorManager.getSSOApplicationMsgs(
                mAccountLD.value!!,
                pageSize,
                (pageNumber - 1) * pageSize
            )
        onSuccess.invoke(msgs, null)
    }
}
