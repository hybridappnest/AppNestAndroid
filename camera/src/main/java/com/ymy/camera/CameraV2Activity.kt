package com.ymy.camera

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.ymy.camera.listener.JCameraErrorListener
import com.ymy.camera.listener.JCameraListener
import com.ymy.core.base.BaseActivity
import com.ymy.core.base.IUIKitCallBack
import com.ymy.core.notchtools.NotchTools
import com.ymy.core.permission.DBXPermissionUtils
import com.ymy.core.permission.requestPermission
import com.ymy.core.utils.FileUtil
import com.ymy.core.utils.StatusBarTool
import com.ymy.core.utils.TUIKitConstants
import com.ymy.core.utils.ToastUtils
import com.ymy.core.view.DBXLoadingView

/**
 * Created on 2/5/21 10:31.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:隐患上报，拍照页面
 */
class CameraV2Activity : BaseActivity() {

    companion object {
        var callBack: IUIKitCallBack? = null

        fun invoke(context: Context, cameraType:Int,cb: IUIKitCallBack) {
            requestPermission(
                context,
                "你好:\n" +
                        "     该功能需要访问您的相册及拍照、录制视频，鉴于您禁用相关权限，请手动设置开启权限:\n" +
                        "1、【相机】\n" +
                        "2、【麦克风】\n" +
                        "3、【存储】\n",
                arrayOf(
                    DBXPermissionUtils.CAMERA,
                    DBXPermissionUtils.RECORD_AUDIO,
                    DBXPermissionUtils.WRITE_EXTERNAL_STORAGE,
                ),
                actionGranted = {
                    callBack = cb
                    val intent = Intent(context, CameraV2Activity::class.java)
                    intent.putExtra(TUIKitConstants.CAMERA_TYPE,cameraType)
                    context.startActivity(intent)

                },
                actionDenied =
                {
                    ToastUtils.showImageToast(context, "无权限无法使用该功能", false)
                })
        }
    }

    override fun getLayoutResId() = R.layout.activity_v2_camera
    private val jCameraView: JCameraView by lazy {
        findViewById(R.id.jcameraview)
    }

    override fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setCutoutMode()
        }

        val statusHeight = NotchTools.getFullScreenTools().getStatusHeight(window)
        val titleBar = findViewById<FrameLayout>(R.id.title_bar)
        val btnBack = findViewById<View>(R.id.btn_back)
        btnBack.visibility = View.GONE
        val tvTitle = findViewById<TextView>(R.id.tv_titlebar_title)
        tvTitle.text = ""
        titleBar.setPadding(0, statusHeight, 0, 0)
        titleBar.setBackgroundResource(R.color.transparent)

        StatusBarTool.setTranslucentStatus(this)
        findViewById<FrameLayout>(R.id.loadingroot)?.run {
            initLoadingView(this)
        }
        loadingView.hide()
    }

    lateinit var loadingView: DBXLoadingView
    private fun initLoadingView(rootView: FrameLayout) {
        loadingView = DBXLoadingView(rootView).apply {
            setLoadingText("上传中。。。")
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun setCutoutMode() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        val lp = window.attributes
        lp.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = lp
    }

    override fun initData() {
        //设置视频保存路径
        //jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");
        val state = intent.getIntExtra(TUIKitConstants.CAMERA_TYPE, JCameraView.BUTTON_STATE_BOTH)
        jCameraView.setFeatures(state)
        if (state == JCameraView.BUTTON_STATE_ONLY_CAPTURE) {
            jCameraView.setTip("点击拍照")
        } else if (state == JCameraView.BUTTON_STATE_ONLY_RECORDER) {
            jCameraView.setTip("长按摄像")
        }

        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE)
        jCameraView.setErrorLisenter(object :
            JCameraErrorListener {
            override fun onError() {
                val intent = Intent()
                setResult(103, intent)
                finish()
            }

            override fun AudioPermissionError() {
//                ToastUtil.toastShortMessage("给点录音权限可以?");
            }
        })
        //JCameraView监听
        jCameraView.setJCameraLisenter(object : JCameraListener {
            override fun captureSuccess(bitmap: Bitmap) {
                //获取图片bitmap
                val path = FileUtil.saveBitmap("JCamera", bitmap)
                callBack?.onSuccess(
                    path
                )
                finish()
            }

            override fun recordSuccess(url: String, firstFrame: Bitmap, duration: Long) {
                //获取视频路径
                val path = FileUtil.saveBitmap("JCamera", firstFrame)
                val intent = Intent()
                intent.putExtra(TUIKitConstants.IMAGE_WIDTH, firstFrame.width)
                intent.putExtra(TUIKitConstants.IMAGE_HEIGHT, firstFrame.height)
                intent.putExtra(TUIKitConstants.VIDEO_TIME, duration)
                intent.putExtra(TUIKitConstants.CAMERA_IMAGE_PATH, path)
                intent.putExtra(TUIKitConstants.CAMERA_VIDEO_PATH, url)
                //setResult(-1, intent);
                callBack?.onSuccess(
                    intent
                )
                finish()
            }
        })

        jCameraView.setLeftClickListener {
            this@CameraV2Activity.finish()
        }
        jCameraView.setRightClickListener {
            //                ToastUtil.toastShortMessage("Right");
        }
    }

    override fun onStart() {
        super.onStart()
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = option
        }
    }

    override fun onResume() {
        super.onResume()
        jCameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        jCameraView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}