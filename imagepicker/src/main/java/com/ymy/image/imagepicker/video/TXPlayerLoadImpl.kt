package com.ymy.image.imagepicker.video

import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.orhanobut.logger.Logger
import com.tencent.rtmp.ITXVodPlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXVodPlayConfig
import com.tencent.rtmp.TXVodPlayer
import com.tencent.rtmp.ui.TXCloudVideoView
import com.ymy.core.Ktx
import com.ymy.core.lifecycle.KtxAppLifeObserver
import com.ymy.image.R
import net.mikaelzero.mojito.interfaces.OnMojitoViewCallback
import net.mikaelzero.mojito.loader.ContentLoader
import net.mikaelzero.mojito.loader.OnLongTapCallback
import net.mikaelzero.mojito.loader.OnTapCallback


/**
 * @Author:         MikaelZero
 * @CreateDate:     2020/6/21 3:48 PM
 * @Description:
 */
class TXPlayerLoadImpl : ContentLoader {
    lateinit var mPlayerView: TXCloudVideoView
    lateinit var mPlayConfig: TXVodPlayConfig
    lateinit var coverIv: AppCompatImageView
    lateinit var ivStateChange: ImageView
    lateinit var controlLayout: View
    lateinit var tvPlayTime: TextView
    lateinit var mTextDuration: TextView
    lateinit var mSeekBar: SeekBar
    lateinit var frameLayout: View
    lateinit var progress: ProgressBar
    lateinit var context: Context
    lateinit var mVodPlayer: TXVodPlayer
    var targetUrl: String? = null
    override val displayRect: RectF
        get() = RectF()

    override fun init(
        context: Context,
        originUrl: String,
        targetUrl: String?,
        onMojitoViewCallback: OnMojitoViewCallback?
    ) {
        this.context = context
        this.targetUrl = originUrl
        frameLayout = LayoutInflater.from(context).inflate(R.layout.video_layout, null)
        mPlayerView = frameLayout.findViewById(R.id.video_view)
        progress = frameLayout.findViewById(R.id.progress)
        coverIv = frameLayout.findViewById(R.id.coverIv)
        initControlView()

        mPlayerView.visibility = View.GONE
        coverIv.scaleType = ImageView.ScaleType.CENTER_CROP
        initPlayer()
    }

    private var mHWDecode = false
    private var mStartSeek = false
    private var mVideoPause = false
    private var mVideoPlay = false
    private var mPlayRate = 1.0f
    private var mStartPlayTS: Long = 0

    private var mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION
    private var mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT

    private fun initPlayer() {
        mVodPlayer = TXVodPlayer(context)
        mPlayConfig = TXVodPlayConfig()

        mVodPlayer.setPlayerView(mPlayerView)
        mVodPlayer.setVodListener(object : ITXVodPlayListener {
            override fun onPlayEvent(player: TXVodPlayer?, event: Int, param: Bundle?) {
                if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
                    val playEventLog =
                        "receive event: $event, " + param?.getString(
                            TXLiveConstants.EVT_DESCRIPTION
                        )
                    Logger.d(playEventLog)
                }
                when (event) {
                    TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED, TXLiveConstants.PLAY_EVT_VOD_LOADING_END ->
                        stopLoadingAnimation()
                    TXLiveConstants.PLAY_EVT_PLAY_BEGIN -> {
                        ivStateChange.setImageResource(R.drawable.icon_video_pause)
                        showControlLayout(true)
                        coverIv.visibility = View.GONE
                        stopLoadingAnimation()
                        Log.d(
                            "AutoMonitor",
                            "PlayFirstRender,cost=" + (System.currentTimeMillis() - mStartPlayTS)
                        )
                        if (!KtxAppLifeObserver.appForeGround) {
                            mVodPlayer.pause()
                        }
                    }
                    TXLiveConstants.PLAY_EVT_PLAY_PROGRESS -> {
                        if (mStartSeek) {
                            return
                        }
                        param?.run {
                            val progress: Int = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS)
                            val duration: Int = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS)
                            mSeekBar.progress = progress
                            tvPlayTime.text = String.format(
                                "%02d:%02d",
                                progress / 1000 / 60,
                                progress / 1000 % 60
                            )
                            mTextDuration.text = String.format(
                                "/%02d:%02d",
                                duration / 1000 / 60,
                                duration / 1000 % 60
                            )
                            mSeekBar.max = duration
                            val playEventLog =
                                "receive event: $event, progress：$progress + duration:$duration"
                            Logger.d(playEventLog);
                        }
                        return
                    }
                    TXLiveConstants.PLAY_ERR_NET_DISCONNECT, TXLiveConstants.PLAY_EVT_PLAY_END, TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND -> {
                        stopPlayVod()
                        mVideoPlay = false
                        mVideoPause = false
                        tvPlayTime.text = "00:00"
                        mSeekBar.progress = 0
                        coverIv.visibility = View.VISIBLE
                        ivStateChange.setImageResource(R.drawable.icon_video_play)
                        showControlLayout(false)
                    }
                    TXLiveConstants.PLAY_EVT_PLAY_LOADING -> {
                        startLoadingAnimation()
                    }
                    TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME -> {
                        stopLoadingAnimation()
                        if (!KtxAppLifeObserver.appForeGround) {
                            mVodPlayer.pause()
                        }
                    }
                    TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION -> {
                    }
                    TXLiveConstants.PLAY_ERR_HLS_KEY -> {
                        //HLS 解密 key 获取失败
                        stopPlayVod()
                    }
                    TXLiveConstants.PLAY_WARNING_RECONNECT -> {
                        startLoadingAnimation()
                    }
                    TXLiveConstants.PLAY_EVT_CHANGE_ROTATION -> {
                        return
                    }
                }
                if (event < 0) {
                    Toast.makeText(
                        Ktx.app,
                        param?.getString(TXLiveConstants.EVT_DESCRIPTION),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNetStatus(p0: TXVodPlayer?, p1: Bundle?) {
            }

        })
        mVodPlayer.setRate(mPlayRate)
        // 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
        // (1) 只有 4.3 以上android系统才支持
        // (2) 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
        mVodPlayer.enableHardwareDecode(mHWDecode)
        mVodPlayer.setRenderRotation(mCurrentRenderRotation)
        mVodPlayer.setRenderMode(mCurrentRenderMode)

        val header: Map<String, String> = HashMap()
        mPlayConfig.setProgressInterval(200)
        mPlayConfig.setHeaders(header)
        mVodPlayer.setConfig(mPlayConfig)
        mVodPlayer.setAutoPlay(true)
        startLoadingAnimation()
    }

    private fun startPlay(): Boolean {
        val result = mVodPlayer.startPlay(targetUrl)
        if (result != 0) {
            return false
        }
        showControlLayout(true)
        return true
    }

    private fun startLoadingAnimation() {
        progress.visibility = View.VISIBLE
    }

    private fun stopLoadingAnimation() {
        progress.visibility = View.GONE
    }

    private fun initControlView() {
        controlLayout = frameLayout.findViewById(R.id.ll_control_layout)
        frameLayout.setOnClickListener {
            showControlLayout(true)
        }
        ivStateChange = frameLayout.findViewById(R.id.iv_state_change)
        tvPlayTime = frameLayout.findViewById(R.id.tv_play_time)
        mTextDuration = frameLayout.findViewById(R.id.tv_duration)
        mSeekBar = frameLayout.findViewById(R.id.seekbar)
        ivStateChange.setOnClickListener {
            togglePlayState()
        }
        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvPlayTime.text = String.format(
                    "%02d:%02d",
                    progress / 1000 / 60,
                    progress / 1000 % 60
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mStartSeek = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.run {
                    mVodPlayer.seek(seekBar.progress / 1000f)
                    val handler = Handler()
                    handler.postDelayed({ mStartSeek = false }, 500)
                }
            }

        })
    }

    private fun stopPlayVod() {
        stopLoadingAnimation()
        mVodPlayer.pause()
        mVodPlayer.stopPlay(false)
        mVideoPause = false
        mVideoPlay = false
    }

    private val showControlLayout = 1001

    private val mHandler = Handler {
        when (it.what) {
            showControlLayout -> {
                hideControlLayout()
            }
        }
        true
    }

    private fun showControlLayout(needHide: Boolean) {
        controlLayout.visibility = View.VISIBLE
        mHandler.removeMessages(showControlLayout)
        if (needHide) {
            mHandler.sendEmptyMessageDelayed(showControlLayout, 2000)
        }
    }

    private fun hideControlLayout() {
        controlLayout.visibility = View.INVISIBLE
    }

    private fun togglePlayState() {
        if (mVideoPlay) {
            if (!mVodPlayer.isPlaying) {
                showControlLayout(true)
                mVodPlayer.resume()
                ivStateChange.setImageResource(R.drawable.icon_video_pause)
            } else {
                showControlLayout(false)
                mVodPlayer.pause()
                ivStateChange.setImageResource(R.drawable.icon_video_play)
            }
            mVideoPause = !mVideoPause
        } else {
            mVideoPlay = startPlay()
            ivStateChange.setImageResource(R.drawable.icon_video_pause)
        }
    }

    override fun pageChange(isHidden: Boolean) {
        if (isHidden) {
            stopPlayVod()
        } else {
            if (mVideoPlay) {
                if (!mVodPlayer.isPlaying) {
                    mVodPlayer.resume()
                    ivStateChange.setImageResource(R.drawable.icon_video_play)
                }
                mVideoPause = !mVideoPause
            } else {
                mVideoPlay = startPlay()
            }
            coverIv.visibility = View.VISIBLE
        }
    }


    override fun providerView(): View {
        return frameLayout
    }

    override fun providerRealView(): View {
        return coverIv
    }

    override fun dispatchTouchEvent(
        isDrag: Boolean,
        isActionUp: Boolean,
        isDown: Boolean,
        isRight: Boolean,
    ): Boolean {
        return false
    }


    override fun dragging(width: Int, height: Int, ratio: Float) {

    }

    override fun beginBackToMin(isResetSize: Boolean) {
    }

    override fun backToNormal() {
    }

    override fun loadAnimFinish() {
    }

    override fun needReBuildSize(): Boolean {
        return false
    }

    override fun useTransitionApi(): Boolean {
        return false
    }

    override fun isLongImage(width: Int, height: Int): Boolean {
        return false
    }

    override fun onTapCallback(onTapCallback: OnTapCallback) {
    }

    override fun onLongTapCallback(onLongTapCallback: OnLongTapCallback) {

    }
}