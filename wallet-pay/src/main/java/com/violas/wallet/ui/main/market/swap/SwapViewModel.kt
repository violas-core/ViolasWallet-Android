package com.violas.wallet.ui.main.market.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.palliums.base.BaseViewModel
import com.palliums.utils.coroutineExceptionHandler
import com.quincysx.crypto.CoinTypes
import com.violas.wallet.biz.ExchangeManager
import com.violas.wallet.ui.main.market.bean.IAssetsMark
import com.violas.wallet.ui.main.market.bean.ITokenVo
import com.violas.wallet.ui.main.market.bean.MutBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Created by elephant on 2020/6/30 17:42.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc:
 */
class SwapViewModel : BaseViewModel() {

    // 输入输出选择的Token
    private val currFromTokenLiveData = MediatorLiveData<ITokenVo?>()
    private val currToTokenLiveData = MediatorLiveData<ITokenVo?>()

    // 手续费率
    private val handlingFeeRateLiveData = MediatorLiveData<BigDecimal?>()

    // 兑换率
    private val exchangeRateLiveData = MediatorLiveData<BigDecimal?>()

    // Gas费
    private val gasFeeLiveData = MediatorLiveData<Long?>()

    private val mExchangeManager by lazy {
        ExchangeManager()
    }

    /**
     * 稳定币交易所，uniswap 交易所支持的所有币种
     */
    private var mSupportTokens: List<ITokenVo>? = null

    /**
     * 映射兑换支持的币种
     */
    private var mMappingSupportSwapPairMap: HashMap<String, MutBitmap>? = null

    init {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler()) {
            mSupportTokens = mExchangeManager.getMarketSupportTokens()
            mSupportTokens?.let {
                mMappingSupportSwapPairMap =
                    mExchangeManager.getMappingMarketSupportTokens(it)
            }
        }
        val convertToExchangeRate: (ITokenVo, ITokenVo) -> BigDecimal? =
            { fromToken, toToken ->
                val fromAnchorValue = BigDecimal(fromToken.anchorValue.toString())
                val toAnchorValue = BigDecimal(toToken.anchorValue.toString())
                val zero = BigDecimal("0.00")
                if (fromAnchorValue <= zero || toAnchorValue <= zero)
                    null
                else
                    fromAnchorValue.divide(toAnchorValue, 8, RoundingMode.DOWN)
                        .stripTrailingZeros()
            }
        val handleCurrTokenChange: (ITokenVo?, ITokenVo?) -> Unit =
            { fromToken, toToken ->
                if (fromToken == null || toToken == null) {
                    handlingFeeRateLiveData.postValue(null)
                    exchangeRateLiveData.postValue(null)
                    gasFeeLiveData.postValue(null)
                } else {
                    handlingFeeRateLiveData.postValue(
                        if (fromToken.coinNumber != CoinTypes.Violas.coinType()
                            || toToken.coinNumber != CoinTypes.Violas.coinType()
                        )
                            BigDecimal("0.3")
                        else
                            BigDecimal("0.00591")
                    )
                    exchangeRateLiveData.postValue(convertToExchangeRate(fromToken, toToken))
                    gasFeeLiveData.postValue(0)
                }
            }
        exchangeRateLiveData.addSource(currFromTokenLiveData) { fromToken ->
            handleCurrTokenChange(fromToken, currToTokenLiveData.value)
        }
        exchangeRateLiveData.addSource(currToTokenLiveData) { toToken ->
            handleCurrTokenChange(currFromTokenLiveData.value, toToken)
        }
    }

    //*********************************** Token相关方法 ***********************************//
    fun getCurrFromTokenLiveData(): LiveData<ITokenVo?> {
        return currFromTokenLiveData
    }

    fun getCurrToTokenLiveData(): LiveData<ITokenVo?> {
        return currToTokenLiveData
    }

    fun selectToken(selectFrom: Boolean, selected: ITokenVo) {
        if (selectFrom) {
            val currFromToken = currFromTokenLiveData.value
            if (selected != currFromToken) {
                val currToToken = currToTokenLiveData.value
                if (selected == currToToken) {
                    currToTokenLiveData.postValue(currFromToken)
                }
                currFromTokenLiveData.postValue(selected)
            }
        } else {
            val currToToken = currToTokenLiveData.value
            if (selected != currToToken) {
                val currFromToken = currFromTokenLiveData.value
                if (selected == currFromToken) {
                    currFromTokenLiveData.postValue(currToToken)
                }
                currToTokenLiveData.postValue(selected)
            }
        }
    }

    @Deprecated("delete")
    fun filter(currToken: ITokenVo, iTokenVo: ITokenVo): Boolean {
        val targetToken = IAssetsMark.convert(iTokenVo)
        mMappingSupportSwapPairMap?.get(IAssetsMark.convert(currToken).mark())?.forEach {
            try {
                mSupportTokens?.get(it)?.let { token ->
                    if (IAssetsMark.convert(token) == targetToken) {
                        return true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    //*********************************** 其它信息相关方法 ***********************************//
    fun getHandlingFeeRateLiveDataLiveData(): LiveData<BigDecimal?> {
        return handlingFeeRateLiveData
    }

    fun getExchangeRateLiveData(): LiveData<BigDecimal?> {
        return exchangeRateLiveData
    }

    fun getGasFeeLiveData(): LiveData<Long?> {
        return gasFeeLiveData
    }

    //*********************************** 耗时相关任务 ***********************************//
    override suspend fun realExecute(action: Int, vararg params: Any) {
        // TODO 兑换逻辑
    }
}