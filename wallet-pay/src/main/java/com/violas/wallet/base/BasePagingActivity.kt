package com.violas.wallet.base

import androidx.recyclerview.widget.RecyclerView
import com.palliums.paging.PagingController
import com.palliums.paging.PagingHandler
import com.palliums.paging.PagingViewAdapter
import com.palliums.paging.PagingViewModel
import com.palliums.widget.refresh.IRefreshLayout
import com.palliums.widget.status.IStatusLayout
import com.violas.wallet.R
import kotlinx.android.synthetic.main.activity_base_list.*

/**
 * Created by elephant on 2019-11-06 14:56.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 * desc: 分页列表页面基类
 */
abstract class BasePagingActivity<VO> : BaseAppActivity(), PagingController<VO> {

    private val mPagingHandler by lazy {
        PagingHandler(this, this, this)
    }

    private val mPagingViewModel by lazy {
        lazyInitPagingViewModel()
    }

    private val mPagingViewAdapter by lazy {
        lazyInitPagingViewAdapter()
    }

    /**
     * 子类通过覆写[getLayoutResId]返回自定义布局，必须包含RecyclerView
     */
    override fun getLayoutResId(): Int {
        return R.layout.activity_base_list
    }

    /**
     * 子类覆写[getLayoutResId]后，需覆写[getRecyclerView]返回RecyclerView
     */
    override fun getRecyclerView(): RecyclerView {
        return vRecyclerView
    }

    /**
     * 子类覆写[getLayoutResId]后，需覆写[getRefreshLayout]返回SmartRefreshLayout
     */
    override fun getRefreshLayout(): IRefreshLayout? {
        return vRefreshLayout
    }

    /**
     * 子类覆写[getLayoutResId]后，需覆写[getStatusLayout]返回DataLoadStatusLayout
     */
    override fun getStatusLayout(): IStatusLayout? {
        return vStatusLayout
    }

    override fun getPagingViewModel(): PagingViewModel<VO> {
        return mPagingViewModel
    }

    override fun getPagingViewAdapter(): PagingViewAdapter<VO> {
        return mPagingViewAdapter
    }

    fun getPagingHandler(): PagingHandler<VO> {
        return mPagingHandler
    }

    abstract fun lazyInitPagingViewModel(): PagingViewModel<VO>

    abstract fun lazyInitPagingViewAdapter(): PagingViewAdapter<VO>
}