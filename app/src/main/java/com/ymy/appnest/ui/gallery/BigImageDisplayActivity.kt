package com.ymy.appnest.ui.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import com.ymy.appnest.R
import net.mikaelzero.mojito.view.sketch.core.Sketch
import net.mikaelzero.mojito.view.sketch.core.SketchImageView
import net.mikaelzero.mojito.view.sketch.core.SketchView
import net.mikaelzero.mojito.view.sketch.core.request.DisplayOptions


/**
 * Created on 12/29/20 14:45.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

class BigImageDisplayActivity : Activity() {
    companion object {
        /**
         * 跳转页面
         *
         * @param context
         * @param fileUrl  文件url
         * @param fileName 文件名
         */
        fun actionStart(context: Context, fileUrl: String?, fileName: String?) {
            val intent = Intent(context, BigImageDisplayActivity::class.java)
            intent.putExtra("fileUrl", fileUrl)
            intent.putExtra("fileName", fileName)
            context.startActivity(intent)
        }
    }

    //文件url 由文件url截取的文件名 上个页面传过来用于显示的文件名
    private var mFileUrl: String? = ""
    private val mFileName: String? = null
    private var fileName: String? = null
    private var flImageRoot: FrameLayout? = null
    private var sketchImageView: SketchImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_bigimage_display)
        getFileUrlByIntent()
        findViewById()
        initBigImageLoader()
    }

    private fun initBigImageLoader() {
        sketchImageView = SketchImageView(this)
        sketchImageView?.run {
            isZoomEnabled = true
            options.isDecodeGifImage = true
        }
        flImageRoot?.addView(sketchImageView)
        Sketch.with(this).display(mFileUrl, sketchImageView!!)
            .loadingImage { _: Context?, _: SketchView?, _: DisplayOptions? ->
                (sketchImageView as SketchImageView).drawable // 解决缩略图切换到原图显示的时候会闪烁的问题
            }.commit()
    }

    private fun findViewById() {
        val backIcon = findViewById<View>(R.id.page_title_left_icon) as ImageView
        backIcon.setOnClickListener {
            this.finish()
        }
        flImageRoot = findViewById<View>(R.id.fl_image_root) as FrameLayout
    }

    /**
     * 获取传过来的文件url和文件名
     */
    private fun getFileUrlByIntent() {
        val intent = intent
        intent?.run {
            mFileUrl = getStringExtra("fileUrl")
            fileName = getStringExtra("fileName")
        }
    }

}
