package com.violas.wallet.ui.main.applyFor

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.lxj.xpopup.XPopup
import com.palliums.base.BaseFragment
import com.sl.utakephoto.exception.TakeException
import com.sl.utakephoto.manager.ITakePhotoResult
import com.sl.utakephoto.manager.UTakePhoto
import com.violas.wallet.R
import com.violas.wallet.ui.selectCurrency.SelectCurrencyActivity
import com.violas.wallet.ui.selectCurrency.bean.CurrencyBean
import com.violas.wallet.widget.dialog.TakePhotoPopup
import kotlinx.android.synthetic.main.fragment_apply_submit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApplySubmitFragment : BaseFragment() {
    companion object {
        private const val REQUEST_CURRENCY_CODE = 0
        private const val REQUEST_PHOTO_RESERVES = 1
        private const val REQUEST_PHOTO_ACCOUNT_POSITIVE = 2
        private const val REQUEST_PHOTO_ACCOUNT_REVERSE = 3
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_apply_submit
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvContent.setOnClickListener {
            SelectCurrencyActivity.start(this@ApplySubmitFragment, REQUEST_CURRENCY_CODE)
        }

        upLoadViewReserves.setOnClickListener {
            showTakePhotoPopup(REQUEST_PHOTO_RESERVES)
        }
        upLoadViewAccountPositive.setOnClickListener {
            showTakePhotoPopup(REQUEST_PHOTO_ACCOUNT_POSITIVE)
        }
        upLoadViewAccountReverse.setOnClickListener {
            showTakePhotoPopup(REQUEST_PHOTO_ACCOUNT_REVERSE)
        }
    }

    private fun showTakePhotoPopup(type: Int) {
        context?.let {
            XPopup.Builder(context)
                .hasStatusBarShadow(true)
                .asCustom(TakePhotoPopup(context!!, {
                    takePhoto(true, type)
                }, {
                    takePhoto(false, type)
                }))
                .show()
        }
    }

    private fun takePhoto(isTakePhoto: Boolean = false, type: Int) {
        if (isTakePhoto) {
            UTakePhoto.with(this)
                .openCamera()
        } else {
            UTakePhoto.with(this)
                .openAlbum()
        }.build(object : ITakePhotoResult {
            override fun takeSuccess(uriList: List<Uri?>?) {
                launch(Dispatchers.IO) {
                    if (null == uriList || uriList.isEmpty()) {
                        return@launch
                    }
                    uriList[0]?.let {
                        val openInputStream = context?.contentResolver?.openInputStream(it)
                        val createFromStream =
                            Drawable.createFromStream(openInputStream, it.toString())
                        withContext(Dispatchers.Main) {
                            when (type) {
                                REQUEST_PHOTO_RESERVES -> {
                                    upLoadViewReserves
                                }
                                REQUEST_PHOTO_ACCOUNT_POSITIVE -> {
                                    upLoadViewAccountPositive
                                }
                                REQUEST_PHOTO_ACCOUNT_REVERSE -> {
                                    upLoadViewAccountReverse
                                }
                                else -> {
                                    upLoadViewAccountReverse
                                }
                            }.setContentImage(createFromStream)
                        }
                    }
                }
            }

            override fun takeFailure(ex: TakeException) {
                showToast(getString(R.string.hint_image_load_error))
            }

            override fun takeCancel() {
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CURRENCY_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val parcelable =
                        data?.getParcelableExtra<CurrencyBean>(SelectCurrencyActivity.EXT_CURRENCY_ITEM)
                    tvContent.text = parcelable?.currency
                    tvStableCurrencyValue.setContent("${parcelable?.exchange}")
                }
            }
        }
    }
}
