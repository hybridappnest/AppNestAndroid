package com.ymy.appnest.ui.gallery.adapter.items

import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.orhanobut.logger.Logger
import com.ymy.appnest.R
import com.ymy.appnest.ui.gallery.adapter.BaseGalleryItemProvider
import com.ymy.appnest.ui.gallery.adapter.IGallerySourceModel
import com.ymy.appnest.ui.gallery.bean.GalleryVideo
import com.ymy.core.glide.ImageLoader
import com.ymy.core.utils.StringUtils.stringForTime
import com.ymy.player.video.videoview.AlivcVideoView

/**
 * Created on 3/22/21 16:41.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class ItemProviderVideo(
    override val itemViewType: Int = IGallerySourceModel.video,
    override val layoutId: Int = R.layout.item_gallery_video
) : BaseGalleryItemProvider() {
    override fun getBaseVideoHolder(view: View) = GalleryVideoHolder(view)

    override fun convert(helper: BaseViewHolder, item: IGallerySourceModel) {
        if (helper is GalleryVideoHolder) {
            helper.data = item
        }
    }

    class GalleryVideoHolder(
        val mView: View
    ) : BaseGalleryItemProvider.BaseVideoHolder(mView), View.OnClickListener, View.OnTouchListener,
        SeekBar.OnSeekBarChangeListener {

        init {
            mView.tag = this
        }

        val ivStart: ImageView by lazy {
            mView.findViewById<ImageView>(R.id.start)
        }
        val mTextureView: TextureView by lazy {
            mView.findViewById<TextureView>(R.id.video_textureview)
        }
        val ivPlay_status: ImageView by lazy {
            mView.findViewById<ImageView>(R.id.play_status)
        }
        val ivThumb: ImageView by lazy {
            mView.findViewById<ImageView>(R.id.thumb)
        }
        val skProgress: SeekBar by lazy {
            mView.findViewById<SeekBar>(R.id.progress)
        }
        val tvTimeCurrent: TextView by lazy {
            mView.findViewById<TextView>(R.id.current)
        }
        val tvTimeTotal: TextView by lazy {
            mView.findViewById<TextView>(R.id.total)
        }
        val videoRoot: FrameLayout by lazy {
            mView.findViewById<FrameLayout>(R.id.ali_video_root)
        }
        var mAlivcVideoView: AlivcVideoView? = null

        var galleryVideo: GalleryVideo? = null

        private var mEndPlay = false
        private var videoTotal = -1

        var mSurface: Surface? = null

        /**
         * 根据数据初始化视图
         */
        override fun initViewByData() {
            data?.run {
                galleryVideo = this as GalleryVideo
                ImageLoader.loadOriginalImage(firstFrame, ivThumb)
            }
            ivStart.setOnClickListener(this)
            ivThumb.setOnClickListener(this)
            skProgress.setOnTouchListener(this)
            ivPlay_status.setOnClickListener(this)
            skProgress.setOnSeekBarChangeListener(this)
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
            videoRoot.addView(view, params)
        }

        fun onSelected(alivcVideoView: AlivcVideoView) {
            mAlivcVideoView = alivcVideoView
            mAlivcVideoView?.run {
                addSubView(this)
                bindingVideoView()
                this.setOnClickListener {
                    this.onPauseClick()
                    updateStartImage(this.isPause)
                }
            }
            galleryVideo?.run {
                startPlay(url)
            }
        }

        fun unSelected() {
            mAlivcVideoView?.onStop()
            videoRoot.removeView(mAlivcVideoView)
            mAlivcVideoView = null
            resetPlayView()
        }

        override fun onViewAttachedToWindow() {
            resetPlayView()
            mAlivcVideoView?.onResume()
        }

        override fun onViewDetachedFromWindow() {
            mAlivcVideoView?.onDestroy()
        }

        private fun bindingVideoView() {
            mAlivcVideoView?.setVideoProgressListener(object : AlivcVideoView.VideoUIListener {
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
                        val totalStr = stringForTime(total)
                        tvTimeTotal.text = totalStr
                    }
//                Logger.e("setProgress position:${position}")
                    skProgress.progress = position
                    val positionStr = stringForTime(position)
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
            mEndPlay = false
            mAlivcVideoView?.startPlay(url)
        }

        private fun updateStartImage(isPlaying: Boolean) {
            if (isPlaying) {
                ivPlay_status.setImageResource(R.drawable.biz_video_pause)
                ivStart.visibility = View.INVISIBLE
            } else {
                ivStart.visibility = View.VISIBLE
                ivPlay_status.setImageResource(R.drawable.biz_video_play)
            }
        }

        private fun resetPlayView() {
            galleryVideo?.run {
                ivStart.visibility = View.VISIBLE
                ivThumb.visibility = View.VISIBLE
                ivPlay_status.setImageResource(R.drawable.biz_video_play)
                skProgress.progress = 0
                val positionStr = stringForTime(0)
                tvTimeCurrent.text = positionStr
                val totalStr = stringForTime(0)
                tvTimeTotal.text = totalStr
                videoTotal = -1
            }
        }

        override fun onClick(v: View?) {
            val i = v?.id
            if (i == R.id.start || i == R.id.thumb || i == R.id.play_status) {
                mAlivcVideoView?.run {
                    this.onPauseClick()
                    updateStartImage(!this.isPause)
                }
            }
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            return false
        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                mAlivcVideoView?.run {
                    val time = (progress * duration / 100f).toLong()
                    try {
                        seekTo(time)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}