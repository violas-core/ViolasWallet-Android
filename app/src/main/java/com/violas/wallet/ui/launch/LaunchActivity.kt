package com.violas.wallet.ui.launch

import android.os.Bundle
import com.violas.wallet.R
import com.violas.wallet.base.BaseActivity
import com.violas.wallet.biz.AccountManager
import com.violas.wallet.ui.identity.IdentityActivity
import com.violas.wallet.ui.main.MainActivity
import kotlinx.coroutines.*

class LaunchActivity : BaseActivity(), CoroutineScope by MainScope() {
    override fun getLayoutResId(): Int {
        return R.layout.activity_launch
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleStyle(TITLE_STYLE_NOT_TITLE)
        launch(Dispatchers.IO) {
            delay(1000)
            if (AccountManager().existsWalletAccount()) {
                withContext(Dispatchers.Main) {
                    MainActivity.start(this@LaunchActivity)
                    finish()
                }
            } else {
                withContext(Dispatchers.Main) {
                    IdentityActivity.start(this@LaunchActivity)
                    finish()
                }
            }
        }
    }
}
