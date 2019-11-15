package com.violas.wallet.base

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.violas.wallet.R
import com.violas.wallet.base.widget.LoadingDialog
import com.violas.wallet.ui.changeLanguage.MultiLanguageUtility
import com.violas.wallet.utils.DensityUtility
import com.violas.wallet.utils.LightStatusBarUtil
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_base_status_bar.*
import kotlinx.android.synthetic.main.activity_base_title.*
import kotlinx.coroutines.*
import me.yokeyword.fragmentation.SupportActivity
import qiu.niorgai.StatusBarCompat

abstract class BaseActivity : SupportActivity(), View.OnClickListener, ViewController,
    CoroutineScope by MainScope() {
    private var mLoadingDialog: LoadingDialog? = null
    abstract fun getLayoutResId(): Int
    protected open fun getLayoutView(): View? = null
    fun getRootView(): LinearLayout = containerView

    override fun onCreate(savedInstanceState: Bundle?) {
        //透明状态栏，布局延伸到状态栏中
        StatusBarCompat.translucentStatusBar(this, true)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_base)
        val layoutView = getLayoutView()
        if (layoutView == null) {
            val content = layoutInflater.inflate(getLayoutResId(), containerView, false)
            containerView.addView(content)
        } else {
            containerView.addView(layoutView)
        }
        statusBar.layoutParams.height = getStatusBarHeight()

        titleLeftMenuView?.setOnClickListener(this)

        setTitleStyle(getTitleStyle())
    }

    @TitleStyle
    open fun getTitleStyle(): Int {
        return TITLE_STYLE_DEFAULT
    }

    @IntDef(
        TITLE_STYLE_DEFAULT,
        TITLE_STYLE_GREY_BACKGROUND,
        TITLE_STYLE_MAIN_COLOR,
        TITLE_STYLE_NOT_TITLE
    )
    annotation class TitleStyle

    private fun setTitleStyle(@TitleStyle style: Int) {
        when (style) {
            TITLE_STYLE_GREY_BACKGROUND -> {
                rootView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.def_activity_vice_bg,
                        null
                    )
                )
                StatusBarMode(this, true)
                statusBar.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.white,
                        null
                    )
                )
                titleBar.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.white,
                        null
                    )
                )
                titleLeftMenuView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.icon_back_black,
                        null
                    )
                )
                titleView.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.def_text_font_black,
                        null
                    )
                )
                titleRightMenuView.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.def_text_font_black,
                        null
                    )
                )
            }
            TITLE_STYLE_MAIN_COLOR -> {
                rootView.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.shape_deputy_background,
                    null
                )
                statusBar.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.transparent,
                        null
                    )
                )
                titleBar.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.transparent,
                        null
                    )
                )
                titleLeftMenuView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.icon_back_white,
                        null
                    )
                )
                titleView.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.white,
                        null
                    )
                )
                titleRightMenuView.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.white,
                        null
                    )
                )
            }
            TITLE_STYLE_NOT_TITLE -> {
                rootView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.def_activity_bg,
                        null
                    )
                )
                statusBar.visibility = View.GONE
                titleBar.visibility = View.GONE
            }
            else -> {
                rootView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.def_activity_bg,
                        null
                    )
                )
                StatusBarMode(this, true)
                statusBar.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.white,
                        null
                    )
                )
                titleBar.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.white,
                        null
                    )
                )
                titleLeftMenuView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.icon_back_black,
                        null
                    )
                )
                titleView.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.def_text_font_black,
                        null
                    )
                )
                titleRightMenuView.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.def_text_font_black,
                        null
                    )
                )
            }
        }
    }

    override fun onDestroy() {
        try {
            mLoadingDialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cancel()
        super.onDestroy()
    }

    /**
     * 设置标题
     */
    override fun setTitle(@StringRes strId: Int) {
        if (titleView != null && strId != 0) {
            titleView.setText(strId)
        }
    }

    override fun setTitle(title: CharSequence?) {
        if (titleView != null && !title.isNullOrEmpty()) {
            titleView.text = title
        }
    }

    fun setTitleRightText(@StringRes strId: Int) {
        if (titleRightMenuView != null && strId != 0) {
            titleRightMenuView.setText(strId)
            titleRightMenuView.visibility = View.VISIBLE
            titleRightMenuView.setOnClickListener(this)
        } else if (titleRightMenuView != null) {
            titleRightMenuView.visibility = View.GONE
        }
    }

    fun setTitleRightImage(@DrawableRes resId: Int) {
        if (ivTitleRightMenuView != null && resId != 0) {
            ivTitleRightMenuView.setImageResource(resId)
            ivTitleRightMenuView.visibility = View.VISIBLE
            ivTitleRightMenuView.setOnClickListener(this)
        } else if (titleRightMenuView != null) {
            ivTitleRightMenuView.visibility = View.GONE
        }
    }

    /**
     * 浅色状态模式，设置字体为深色
     */
    protected fun setLightStatusBar(isLightStatusBar: Boolean) {
        LightStatusBarUtil.setLightStatusBarMode(this, isLightStatusBar)
    }

    /**
     * 获取状态栏高度
     */
    protected fun getStatusBarHeight(): Int {
        var result = DensityUtility.dp2px(application, 24)
        val resId = application.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            result = application.resources.getDimensionPixelOffset(resId)
        }
        return result
    }

    companion object {
        const val TITLE_STYLE_DEFAULT = 0
        const val TITLE_STYLE_GREY_BACKGROUND = 1
        const val TITLE_STYLE_MAIN_COLOR = 2
        const val TITLE_STYLE_NOT_TITLE = 3

        ///////////////////////////////////////////////////////////////////////////
        // 防止多次点击
        ///////////////////////////////////////////////////////////////////////////
        //点击同一 view 最小的时间间隔（毫秒），如果小于这个数则忽略此次单击。
        private val INTERVAL_TIME = 800

        /**
         * 判断当前的点击事件是否是快速多次点击(连续多点击），该方法用来防止多次连击。
         *
         * @param view 被点击view，如果前后是同一个view，则进行双击校验
         * @return 认为是重复点击时返回 true，当连续点击时，如果距上一次有效点击时间超过了 INTERVAL_TIME 则返回 false
         */
        @JvmStatic
        fun isFastMultiClick(view: View?): Boolean {
            return isFastMultiClick(view, INTERVAL_TIME.toLong())
        }

        /**
         * 判断当前的点击事件是否是快速多次点击(连续多点击），该方法用来防止多次连击。
         *
         * @param view     被点击view，如果前后是同一个view，则进行双击校验
         * @param duration 两次点击的最小间隔（单位：毫秒），必须大于 0 否则将返回 false。
         * @return 认为是重复点击时返回 true，当连续点击时，如果距上一次有效点击时间超过了 duration 则返回 false
         */
        @JvmStatic
        protected fun isFastMultiClick(view: View?, duration: Long): Boolean {
            if (view == null || duration < 1) {
                return false
            }

            val pervTime = view.getTag(R.id.view_click_time)?.let { it as Long }

            if (pervTime != null && System.currentTimeMillis() - pervTime < duration) {
                return true
            }

            view.setTag(R.id.view_click_time, System.currentTimeMillis())

            return false
        }
    }

    /**
     * 防重点击处理，子类复写[onViewClick]来响应事件
     */
    final override fun onClick(v: View?) {
        v?.let {
            if (isFastMultiClick(v)) {
                return
            }

            // TitleBar的View点击事件与页面其它的View点击事件分开处理
            when (v.id) {
                R.id.titleLeftMenuView ->
                    onTitleLeftViewClick()

                R.id.titleRightMenuView, R.id.ivTitleRightMenuView ->
                    onTitleRightViewClick()

                else ->
                    onViewClick(v)
            }
        }
    }

    /**
     * TitleBar的左侧View点击回调，已防重点击处理
     * 默认关闭当前页面
     */
    protected open fun onTitleLeftViewClick() {
        finish()
    }

    /**
     * TitleBar的右侧View点击回调，已防重点击处理
     * 没有响应逻辑
     */
    protected open fun onTitleRightViewClick() {

    }

    /**
     * View点击回调，已防重点击处理
     * 该回调只会分发页面非TitleBar的View点击事件
     * 如需处理TitleBar的View点击事件，请按需覆写[onTitleLeftViewClick]和[onTitleRightViewClick]
     * @param view
     */
    protected open fun onViewClick(view: View) {

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(MultiLanguageUtility.attachBaseContext(newBase))
    }

    override fun showProgress(@StringRes resId: Int) {
        showProgress(getString(resId))
    }

    override fun showProgress(msg: String?) {
        try {
            launch {
                mLoadingDialog?.dismiss()
                mLoadingDialog = LoadingDialog()
                    .setMessage(msg)
                mLoadingDialog?.show(supportFragmentManager, "load")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun dismissProgress() {
        try {
            launch {
                mLoadingDialog?.dismiss()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

    override fun showToast(@StringRes msgId: Int) {
        showToast(getString(msgId))
    }

    override fun showToast(msg: String) {
        launch {
            Toast.makeText(this@BaseActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun StatusBarMode(activity: Activity, dark: Boolean): Int {
        var result = 0
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (MIUISetStatusBarLightMode(activity, dark)) {
                    result = 1
                } else if (FlymeSetStatusBarLightMode(activity.window, dark)) {
                    result = 2
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    result = 3
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun FlymeSetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val lp = window.getAttributes()
                val darkFlag = WindowManager.LayoutParams::class.java
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                    .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.setAttributes(lp)
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }

    private fun MIUISetStatusBarLightMode(activity: Activity, dark: Boolean): Boolean {
        var result = false
        val window = activity.window
        if (window != null) {
            val clazz = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod(
                    "setExtraFlags",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                }
                result = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    if (dark) {
                        activity.window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }
            } catch (e: Exception) {

            }

        }
        return result
    }

    val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("==error==","$exception")
    }
}
