package com.ymy.ijk;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tencent.imsdk.v2.V2TIMVideoElem;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.picture.imageEngine.impl.GlideEngine;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.ymy.core.base.RootActivity;

/**
 * @description: 视频详情页
 * @auther:
 * @data: 2016/3/18.
 */
public class VideoDetailActivity extends RootActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {
    static String videoUrl;
    static String coverUrl;
    static V2TIMVideoElem mVideoElem;
    static  int mWidth;
    static int mHight;
    public ImageView ivStart;
    SeekBar skProgress;
    TextView tvTimeCurrent, tvTimeTotal;
    public ImageView ivPlay_status;
    public ImageView ivThumb;
    public static void invoke(Context context,V2TIMVideoElem videoElem, String url,String cover,int height,int width) {
        mWidth= width;
        mHight = height;
        videoUrl = url;
        coverUrl = cover;
        mVideoElem =videoElem;
        Intent intent = new Intent(context, VideoDetailActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.vedio_detail_activity);
        dealBiliVideo();
    }

    private void dealBiliVideo() {
        ivStart = (ImageView) findViewById(R.id.start);
        ivThumb = (ImageView) findViewById(R.id.thumb);
        skProgress = (SeekBar) findViewById(R.id.progress);
        tvTimeCurrent = (TextView) findViewById(R.id.current);
        tvTimeTotal = (TextView) findViewById(R.id.total);
        ivPlay_status = (ImageView) findViewById(R.id.play_status);
        ivStart.setOnClickListener(this);
        ivThumb.setOnClickListener(this);
        skProgress.setOnTouchListener(this);
        ivPlay_status.setOnClickListener(this);
        skProgress.setOnSeekBarChangeListener(this);
        if (!TextUtils.isEmpty(coverUrl)) {
            GlideEngine.loadImage(ivThumb, coverUrl, null);
        }
        findViewById(R.id.photo_view_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        videoView = findViewById(R.id.video_view);
        videoView.setVisibility(View.VISIBLE);
        startVideoPlay();
    }
    private TXVodPlayer mPlayer;
    private TXVodPlayConfig mTXVodPlayConfig;
    private boolean mEndPlay;
    private boolean mPaused;//生命周期暂停
    private boolean mClickPaused;//点击暂停
    private final int START_PROGRESS_ANIMATION = 1000;
    private TXCloudVideoView videoView;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void startVideoPlay() {
        if(mPlayer !=null){
            release();
        }
        mPlayer = new TXVodPlayer(this);
        if(mTXVodPlayConfig == null){
            mTXVodPlayConfig = new TXVodPlayConfig();
            mTXVodPlayConfig.setMaxCacheItems(15);
            mTXVodPlayConfig.setProgressInterval(200);
        }
        mPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        mPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mPlayer.setConfig(mTXVodPlayConfig);
        mPlayer.setAutoPlay(true);
        mPlayer.setVodListener(new ITXVodPlayListener() {
            @Override
            public void onPlayEvent(TXVodPlayer txVodPlayer, int e, Bundle bundle) {
                switch (e) {
                    case TXLiveConstants.PLAY_EVT_PLAY_BEGIN://加载完成，开始播放的回调
//                        progress.setProgress(0, mPlayer);
                        updateStartImage(mPlayer.isPlaying());
                        String  str = Utils.stringForTime((int)mPlayer.getDuration()*1000);
                        tvTimeTotal.setText(str);
                        break;
                    case TXLiveConstants.PLAY_EVT_PLAY_LOADING: //开始加载的回调
                        break;
                    case TXLiveConstants.PLAY_EVT_PLAY_PROGRESS: //开始加载的回调
//                        progress.setProgress(mPlayer.getCurrentPlaybackTime(), mPlayer);
                        int time = (int) (mPlayer.getCurrentPlaybackTime()*100 / mPlayer.getDuration());
                        String  str1 = Utils.stringForTime((int)mPlayer.getCurrentPlaybackTime()*1000);
                        tvTimeCurrent.setText(str1);
                        skProgress.setProgress(time);
                        break;
                    case TXLiveConstants.PLAY_EVT_PLAY_END://获取到视频播放完毕的回调
                        if (!mEndPlay) {
                            mEndPlay = true;
                        }
                        updateStartImage(mPlayer.isPlaying());
                        break;
                    case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME://获取到视频首帧回调
                        if (mPaused && mPlayer != null) {
                            mPlayer.pause();
                        }
                        break;
                    case TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION://获取到视频宽高回调
                        onVideoSizeChanged(bundle.getInt("EVT_PARAM1", 0), bundle.getInt("EVT_PARAM2", 0));
                        break;
                }
            }

            @Override
            public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

            }
        });
        mPlayer.setPlayerView(videoView);
        startPlay(videoUrl);
        //延迟一秒开启加载动画
        Message message = Message.obtain();
        message.what = START_PROGRESS_ANIMATION;
    }
    /**
     * 开始播放
     */
    public void startPlay(String url) {
        mClickPaused = false;
        mPaused = false;
        mEndPlay = false;
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mPlayer != null) {
            mPlayer.startPlay(url);
        }
    }
    private void updateStartImage(boolean isPlaying) {
        if (isPlaying) {
            ivPlay_status.setImageResource(R.drawable.biz_video_pause);
            ivStart.setVisibility(View.GONE);
            ivThumb.setVisibility(View.GONE);
        } else {
            ivStart.setVisibility(View.VISIBLE);
            ivThumb.setVisibility(View.VISIBLE);
            ivPlay_status.setImageResource(R.drawable.biz_video_play);
        }

    }
    /**
     * 生命周期暂停
     */
    public void pausePlay() {
        mPaused = true;
        if (!mClickPaused && mPlayer != null) {
            mPlayer.pause();
            updateStartImage(false);
        }
    }
    /**
     * 停止播放
     */
    public void stopPlay() {
        if (mPlayer != null) {
            mPlayer.stopPlay(true);
        }
    }
    /**
     * 获取到视频宽高回调
     */
    public void onVideoSizeChanged(float   videoWidth, float videoHeight ) {
//        if(mWidth != -1&& mHight  != -1){
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
//            params.width = (int) videoWidth;
//            params.height = (int) videoHeight;
//            videoView.setLayoutParams(params);
//            return;
//        }
//        if (videoView != null && videoWidth > 0 && videoHeight > 0) {
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
//            int targetH = 0;
//            if (videoWidth / videoHeight > 0.5625f) {//横屏 9:16=0.5625
//                targetH = (int) (videoView.getWidth() / videoWidth * videoHeight);
//            } else {
//                targetH = ViewGroup.LayoutParams.MATCH_PARENT;
//            }
//            if (targetH != params.height) {
//                params.height = targetH;
//                videoView.requestLayout();
//            }
//        }
    }
    public void release() {
        if (mPlayer != null) {
            mPlayer.stopPlay(false);
            mPlayer.setPlayListener(null);
        }
        mPlayer = null;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start || i == R.id.thumb || i == R.id.play_status) {
            if(mPlayer.isPlaying()){
                pausePlay();
            }else{
                if(mPaused){
                    resumePlay();
                }else{
                    startVideoPlay();
                }
            }
            updateStartImage(mPlayer.isPlaying());
        }
    }
    /**
     * 生命周期恢复
     */
    public void resumePlay() {
        if (mPaused) {
            if (mPlayer != null) {
                mPlayer.resume();
                updateStartImage(true);
            }
        }
        mPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlay();
    }

    @Override
    protected void onDestroy() {
        stopPlay();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        pausePlay();
        super.onStop();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            float time = progress * mPlayer.getDuration() / 100f;
            try {
                mPlayer.seek(time);
                if(!mPlayer.isPlaying()){
                    mPlayer.resume();
                }
                updateStartImage(mPlayer.isPlaying());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}

