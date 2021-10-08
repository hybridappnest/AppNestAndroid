package com.ymy.appnest.qrcode

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Vibrator
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.ymy.appnest.R
import com.ymy.appnest.databinding.ActivityQrcodeScannerBinding
import com.ymy.core.base.BaseActivity
import com.ymy.core.permission.DBXPermissionUtils
import com.ymy.core.permission.requestPermission
import com.ymy.core.utils.ToastUtils
import com.ymy.web.custom.JSCallBack
import com.ymy.web.custom.JSNotificationAction
import java.io.IOException


/**
 * Created on 1/18/21 14:01.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class HWQRCodeScannerActivity : BaseActivity(true) {
    companion object {

        /**
         * 全部
         */
        const val SCANNER_TYPE_ALL = 0
        const val PARAMS_KEY_RESULT = "qr_result"
        const val REQUEST_CODE_PHOTO = 0X1113
        private var jsCallBack: JSCallBack? = null
        const val ACTION_ANNOUNCE = "ANNOUNCE"

        fun invoke(context: Context, jsCallBack: JSCallBack? = null, announce: Int = 0) {
            this.jsCallBack = jsCallBack
            requestPermission(
                context,
                "你好:\n" +
                        "     该功能需要访问您的相册及拍照、录制视频，鉴于您禁用相关权限，请手动设置开启权限:\n" +
                        "1、【相机】\n" +
                        "2、【麦克风】\n",
                arrayOf(
                    DBXPermissionUtils.CAMERA,
                    DBXPermissionUtils.RECORD_AUDIO,
                ),
                actionGranted = {
                    val intent = Intent(context, HWQRCodeScannerActivity::class.java)
                    intent.putExtra(ACTION_ANNOUNCE, announce)
                    context.startActivity(intent)
                },
                actionDenied = {
                    ToastUtils.showImageToast(context, "无权限无法使用该功能", false)
                })
        }

        fun invokeForResult(act: Activity, requestCode: Int) {
            requestPermission(
                act,
                "你好:\n" +
                        "     该功能需要访问您的相册及拍照、录制视频，鉴于您禁用相关权限，请手动设置开启权限:\n" +
                        "1、【相机】\n" +
                        "2、【麦克风】\n",
                arrayOf(
                    DBXPermissionUtils.CAMERA,
                    DBXPermissionUtils.RECORD_AUDIO,
                ),
                actionGranted = {
                    val intent = Intent(act, HWQRCodeScannerActivity::class.java)
                    act.startActivityForResult(intent, requestCode)
                },
                actionDenied = {
                    ToastUtils.showImageToast(act, "无权限无法使用该功能", false)
                })
        }

    }

    val mBinding: ActivityQrcodeScannerBinding by lazy {
        ActivityQrcodeScannerBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View = mBinding.root

    var mScreenWidth = 0
    var mScreenHeight = 0

    val SCAN_FRAME_SIZE = 240
    val img = intArrayOf(R.mipmap.flashlight_on, R.mipmap.flashlight_off)
    var remoteView: RemoteView? = null

    var announce = 0

    override fun getIntentExtra() {
        intent?.run {
            announce = getIntExtra(ACTION_ANNOUNCE, 0)
        }
    }

    override fun initView() {
        mBinding.titleBar.tvTitlebarTitle.text = "扫一扫"
        mBinding.titleBar.btnBack.setOnClickListener {
            finish()
        }
        //1. Obtain the screen density to calculate the viewfinder's rectangle.
        val dm = resources.displayMetrics
        val density = dm.density
        //2. Obtain the screen size.
        mScreenWidth = resources.displayMetrics.widthPixels
        mScreenHeight = resources.displayMetrics.heightPixels

        val scanFrameSize = (SCAN_FRAME_SIZE * density).toInt()

        //3. Calculate the viewfinder's rectangle, which in the middle of the layout.
        //Set the scanning area. (Optional. Rect can be null. If no settings are specified, it will be located in the middle of the layout.)
        val rect = Rect()
        rect.left = mScreenWidth / 2 - scanFrameSize / 2
        rect.right = mScreenWidth / 2 + scanFrameSize / 2
        rect.top = mScreenHeight / 2 - scanFrameSize / 2
        rect.bottom = mScreenHeight / 2 + scanFrameSize / 2

        //Initialize the RemoteView instance, and set callback for the scanning result.
        remoteView = RemoteView
            .Builder()
            .setContext(this)
            .setBoundingBox(rect)
            .setFormat(HmsScan.ALL_SCAN_TYPE)
            .build()
        // When the light is dim, this API is called back to display the flashlight switch.
        remoteView?.setOnLightVisibleCallback { visible ->
            if (visible) {
                mBinding.flushBtn.visibility = View.VISIBLE
            }
        }
        // Subscribe to the scanning result callback event.
        remoteView?.setOnResultCallback { result -> //Check the result.
            if (result != null && result.isNotEmpty() && result[0] != null && !TextUtils.isEmpty(
                    result[0].getOriginalValue()
                )
            ) {
                //展示解码结果
                val code = result[0].getOriginalValue().toString()
                parseScannerResult(code)
            }
        }

        // Load the customized view to the activity.
        remoteView?.onCreate(null)
        val params = FrameLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        mBinding.rim.addView(remoteView, params)

        setFlashOperation()
        setPictureScanOperation()
    }

    private fun setPictureScanOperation() {
        mBinding.imgBtn.setOnClickListener {
            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            this.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO)
        }
    }


    private fun setFlashOperation() {
        mBinding.flushBtn.setOnClickListener {
            if (remoteView?.lightStatus == true) {
                remoteView?.switchLight()
                mBinding.flushBtn.setImageResource(img[1])
            } else {
                remoteView?.switchLight()
                mBinding.flushBtn.setImageResource(img[0])
            }
        }
    }

    override fun initData() {

    }

    override fun onStart() {
        super.onStart()
        //侦听activity的onStart
        remoteView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        //侦听activity的onResume
        remoteView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        //侦听activity的onPause
        remoteView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        //侦听activity的onStop
        remoteView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        //侦听activity的onDestroy
        remoteView?.onDestroy()
    }

    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(200)
    }

    private fun parseScannerResult(result: String) {
        vibrate()
        if (jsCallBack != null) {
            jsCallBack?.run {
                sendResult(
                    JSNotificationAction.jsScan + JSNotificationAction.CALLBACK_SUFFIX,
                    result,
                    announce
                )
                finish()
            }
        } else {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(PARAMS_KEY_RESULT, result)
            })
            finish()
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            if (data != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
                    val hmsScans = ScanUtil.decodeWithBitmap(
                        this,
                        bitmap,
                        HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create()
                    )
                    if (hmsScans != null
                        && hmsScans.isNotEmpty()
                        && hmsScans[0] != null
                        && !TextUtils.isEmpty(hmsScans[0]!!.getOriginalValue())
                    ) {
                        val code = hmsScans[0].getOriginalValue().toString()
                        parseScannerResult(code)
                    } else {
                        toast("图片读取失败，请重试！", false)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    toast("图片读取失败，请重试！", false)
                }
            }
        }
    }

}