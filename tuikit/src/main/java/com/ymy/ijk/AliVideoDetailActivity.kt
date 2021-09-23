package com.ymy.ijk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.orhanobut.logger.Logger
import com.tencent.imsdk.v2.V2TIMVideoElem
import com.tencent.qcloud.tim.uikit.R
import com.tencent.qcloud.tim.uikit.component.picture.imageEngine.impl.GlideEngine
import com.ymy.core.base.RootActivity
import com.ymy.player.video.videoview.AlivcVideoView

/**
 * Created on 3/12/21 13:50.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class AliVideoDetailActivity : RootActivity(), View.OnClickListener,
    OnSeekBarChangeListener, OnTouchListener {
    val ivStart: ImageView by lazy {
        findViewById<ImageView>(R.id.start)
    }
    val ivPlay_status: ImageView by lazy {
        findViewById<ImageView>(R.id.play_status)
    }
    val ivThumb: ImageView by lazy {
        findViewById<ImageView>(R.id.thumb)
    }
    val mAlivcVideoView: AlivcVideoView by lazy {
        AlivcVideoView(this)
    }
    val skProgress: SeekBar by lazy {
        findViewById<SeekBar>(R.id.progress)
    }
    val tvTimeCurrent: TextView by lazy {
        findViewById<TextView>(R.id.current)
    }
    val tvTimeTotal: TextView by lazy {
        findViewById<TextView>(R.id.total)
    }

    private var mEndPlay = false
    private var videoTotal = -1
    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.ali_video_detail_activity)
        dealBiliVideo()
    }

    /**
     * addSubView 添加子view到布局中
     *
     * @param view 子view
     */
    private fun addSubView(view: View) {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        (findViewById<View>(R.id.ali_video_root) as FrameLayout).addView(view, params)
    }

    private fun dealBiliVideo() {
        addSubView(mAlivcVideoView)
        ivStart.setOnClickListener(this)
        ivThumb.setOnClickListener(this)
        skProgress.setOnTouchListener(this)
        ivPlay_status.setOnClickListener(this)
        skProgress.setOnSeekBarChangeListener(this)
        if (!TextUtils.isEmpty(coverUrl)) {
            GlideEngine.loadImage(ivThumb, coverUrl, null)
            ivThumb.visibility = View.VISIBLE
        }
        findViewById<View>(R.id.photo_view_back).setOnClickListener { finish() }
        startVideoPlay()
        mAlivcVideoView.setOnClickListener {
            mAlivcVideoView.onPauseClick()
            updateStartImage(mAlivcVideoView.isPause)
        }
    }

    private fun startVideoPlay() {
        mAlivcVideoView.setVideoProgressListener(object : AlivcVideoView.VideoUIListener {
            /**
             * 播放器进度回调
             *
             * @param position
             * @param total
             * @return
             */
            override fun setProgress(position: Int, total: Int) {
                if (videoTotal == -1 && total != 0) {
                    skProgress.max = total
                    videoTotal = total
                    val totalStr = Utils.stringForTime(total)
                    tvTimeTotal.text = totalStr
                }
//                Logger.e("setProgress position:${position}")
                skProgress.progress = position
                val positionStr = Utils.stringForTime(position)
                tvTimeCurrent.text = positionStr
            }

            /**
             * 开始渲染首帧，去掉封面图
             */
            override fun onRenderingStart() {
                ivThumb.visibility = View.GONE
            }

            /**
             * 播放完成
             */
            override fun onCompletion() {
                showVideoStop()
            }

            /**
             * int unknow = -1;
             * int idle = 0;
             * int initalized = 1;
             * int prepared = 2;
             * int started = 3;
             * int paused = 4;
             * int stopped = 5;
             * int completion = 6;
             * int error = 7;
             *
             * @param newState
             */
            override fun onStateChanged(newState: Int) {
                when (newState) {
                    3 -> {
                        showVideoStart()
                    }
                    4 -> {
                        showVideoPause()
                    }
                    5 -> {
                        showVideoStop()
                    }
                    6 -> {
                        showVideoCompletion()
                    }
                    7 -> {

                    }
                }
            }

        })
        startPlay(videoUrl)
    }

    private fun showVideoCompletion() {
        Logger.e("showVideoCompletion")
        updateStartImage(false)
    }

    private fun showVideoPause() {
        Logger.e("showVideoPause")
        updateStartImage(false)
    }

    private fun showVideoStop() {
        Logger.e("showVideoStop")
        updateStartImage(false)
    }

    private fun showVideoStart() {
        Logger.e("showVideoStart")
        updateStartImage(true)
        skProgress.progress = 0
    }

    /**
     * 开始播放
     */
    fun startPlay(url: String?) {
        if (TextUtils.isEmpty(url)) {
            finish()
            return
        }
        mEndPlay = false
        mAlivcVideoView.startPlay(url)
    }

    private fun updateStartImage(isPlaying: Boolean) {
        if (isPlaying) {
            ivPlay_status.setImageResource(R.drawable.biz_video_pause)
            ivStart.visibility = View.INVISIBLE
            ivThumb.visibility = View.INVISIBLE
        } else {
            ivStart.visibility = View.VISIBLE
            ivThumb.visibility = View.VISIBLE
            ivPlay_status.setImageResource(R.drawable.biz_video_play)
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.start || i == R.id.thumb || i == R.id.play_status) {
            mAlivcVideoView.onPauseClick()
            updateStartImage(!mAlivcVideoView.isPause)
        }
    }

    override fun onResume() {
        super.onResume()
        mAlivcVideoView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mAlivcVideoView.onPause()
    }

    override fun onDestroy() {
        mAlivcVideoView.onDestroy()
        super.onDestroy()
    }

    override fun onStop() {
        mAlivcVideoView.onStop()
        super.onStop()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val time = (progress * mAlivcVideoView.duration / 100f).toLong()
            try {
                mAlivcVideoView.seekTo(time)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }

    companion object {
        var videoUrl: String? = null
        var coverUrl: String? = null
        var mVideoElem: V2TIMVideoElem? = null
        var mWidth = 0
        var mHight = 0

        @JvmStatic
        fun invoke(
            context: Context,
            videoElem: V2TIMVideoElem?,
            url: String?,
            cover: String?,
            height: Int,
            width: Int
        ) {
            mWidth = width
            mHight = height
            videoUrl = url
            coverUrl = cover
            mVideoElem = videoElem
            val intent = Intent(context, AliVideoDetailActivity::class.java)
            context.startActivity(intent)
        }
    }
}

