package com.ymy.player.video.videoview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorCode;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.bean.InfoCode;
import com.aliyun.player.nativeclass.MediaInfo;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.source.UrlSource;
import com.orhanobut.logger.Logger;
import com.ymy.core.utils.ToastUtils;
import com.ymy.player.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 视频播放view
 *
 * @author xlx
 */
public class AlivcVideoView extends FrameLayout {
    private static String TAG = AlivcVideoView.class.getSimpleName();
    /**
     * 是否点击暂停
     */
    public boolean isPause = false;
    private Context mContext;
    private View mPlayerViewContainer;
    private TextureView mTextureView;
    /**
     * 播放器的封装，可以提前准备视频
     */
    private AliPlayer mAliPlayer;
    private GestureDetector gestureDetector;
    /**
     * 当前页面是否处于可见状态
     */
    private boolean isOnBackground = true;
    /**
     * 鉴权过期时发生
     */
    private OnTimeExpiredErrorListener mTimeExpiredErrorListener;
    private IPlayer.OnLoadingStatusListener mLoadingListener;
    private VideoUIListener mVideoUIListener;

    private int totalPosition = 0;
    private int videoCurrentPosition = 0;
    private IRenderView mIRenderView;

    public AlivcVideoView(@NonNull Context context) {
        super(context);
        this.mContext = context;
        initPlayer();
    }

    private AlivcVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        throw new IllegalArgumentException("this view isn't allow create by xml");
    }

    /**
     * 初始化播放器相关
     */
    private void initPlayer() {
        mPlayerViewContainer = View.inflate(getContext(), R.layout.layout_player_single_view, null);
        mTextureView = mPlayerViewContainer.findViewById(R.id.video_textureview);
        gestureDetector = new GestureDetector(mContext,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        //判断当前view是否可见，防止退后台、切换页面和单击同时操作导致，退后台时视频又重新开始播放
                        if (AlivcVideoView.this.isShown()) {
                            onPauseClick();
                        }
                        return true;
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                });
        mPlayerViewContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Surface mSurface = new Surface(surface);
                if (mAliPlayer != null) {
                    mAliPlayer.setSurface(mSurface);
                    mAliPlayer.redraw();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                if (mAliPlayer != null) {
                    mAliPlayer.redraw();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mAliPlayer != null) {
                    mAliPlayer.setSurface(null);
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        addView(mPlayerViewContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        mAliPlayer = AliPlayerFactory.createAliPlayer(mContext);
        //打开播放器
        PlayerConfig config = mAliPlayer.getConfig();
        config.mClearFrameWhenStop = true;
        mAliPlayer.setConfig(config);
        mAliPlayer.setLoop(true);
        mAliPlayer.enableHardwareDecoder(false);
        mAliPlayer.setAutoPlay(false);
        mAliPlayer.setScaleMode(IPlayer.ScaleMode.SCALE_ASPECT_FIT);
        mAliPlayer.setOnPreparedListener(new IPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                MediaInfo mediaInfo = mAliPlayer.getMediaInfo();
                if (!isPause && !isOnBackground) {
                    mAliPlayer.start();
                }
                totalPosition = mediaInfo.getDuration();
                if (mVideoUIListener != null) {
                    mVideoUIListener.setProgress(0, totalPosition);
                }
            }
        });
        mAliPlayer.setOnCompletionListener(new IPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
                //播放完成事件
                if (mVideoUIListener != null) {
                    mVideoUIListener.onCompletion();
                }
            }
        });
        mAliPlayer.setOnStateChangedListener(new IPlayer.OnStateChangedListener() {
            @Override
            public void onStateChanged(int newState) {
                //播放器状态改变事件
                if (mVideoUIListener != null) {
                    mVideoUIListener.onStateChanged(newState);
                }
            }
        });
        mAliPlayer.setOnRenderingStartListener(new IPlayer.OnRenderingStartListener() {
            @Override
            public void onRenderingStart() {
                Logger.e("onRenderingStart");
                if (mVideoUIListener != null) {
                    mVideoUIListener.onRenderingStart();
                }
            }
        });
        mAliPlayer.setOnInfoListener(new IPlayer.OnInfoListener() {
            @Override
            public void onInfo(InfoBean infoBean) {
                parsePlayerInfo(infoBean);
            }
        });
        mAliPlayer.setOnLoadingStatusListener(new IPlayer.OnLoadingStatusListener() {
            @Override
            public void onLoadingBegin() {
                if (mLoadingListener != null) {
                    mLoadingListener.onLoadingBegin();
                }
            }

            @Override
            public void onLoadingEnd() {
                if (mLoadingListener != null) {
                    mLoadingListener.onLoadingEnd();
                }
            }

            @Override
            public void onLoadingProgress(int percent,
                                          float netSpeed) {
                if (mLoadingListener != null) {
                    mLoadingListener.onLoadingProgress(percent, netSpeed);
                }
            }
        });
        mAliPlayer.setOnErrorListener(new IPlayer.OnErrorListener() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                if (errorInfo.getCode() == ErrorCode.ERROR_SERVER_POP_TOKEN_EXPIRED) {
                    //鉴权过期
                    if (mTimeExpiredErrorListener != null) {
                        mTimeExpiredErrorListener.onTimeExpiredError();
                        Log.i(TAG, "刷新鉴权");
                    }
                }
                ToastUtils.showToast(getContext(), errorInfo.getMsg());
            }
        });

    }

    private void parsePlayerInfo(InfoBean infoBean) {
        if (infoBean.getCode() == InfoCode.CurrentPosition) {
            int currentPosition = (int) infoBean.getExtraValue();
            int duration = mAliPlayer.getMediaInfo().getDuration();
            this.videoCurrentPosition = currentPosition;
            Logger.e("parsePlayerInfo videoCurrentPosition" + videoCurrentPosition);
//            mVideoProgress.setProgress(currentPosition);
            if (mVideoUIListener != null) {
                mVideoUIListener.setProgress(videoCurrentPosition, duration);
            }
        }
        if (infoBean.getCode() == InfoCode.LoopingStart) {
            pausePlay();
        }
    }

    public void setTimeExpiredErrorListener(
            OnTimeExpiredErrorListener mTimeExpiredErrorListener) {
        this.mTimeExpiredErrorListener = mTimeExpiredErrorListener;
    }

    private void init() {

    }

    public void seekTo(long position) {
        mAliPlayer.seekTo(position);
    }

    /**
     * 停止视频播放
     */
    private void stopPlay() {
        mAliPlayer.stop();
        mAliPlayer.setSurface(null);
    }

    /**
     * 开始播放
     *
     * @param url 播放视频地址
     */
    public void startPlay(String url) {
        //恢复界面状态
        isPause = false;
        UrlSource urlSource = new UrlSource();
        urlSource.setUri(url);
        mAliPlayer.setDataSource(urlSource);
        mAliPlayer.prepare();
    }

    public void setLoadingListener(IPlayer.OnLoadingStatusListener mLoadingListener) {
        this.mLoadingListener = mLoadingListener;
    }

    public void setVideoProgressListener(VideoUIListener mListener) {
        this.mVideoUIListener = mListener;
    }

    /**
     * 暂停播放
     */
    private void pausePlay() {
        isPause = true;
        mAliPlayer.pause();
    }

    /**
     * 恢复播放
     */
    private void resumePlay() {
        isPause = false;
        mAliPlayer.start();
    }

    public void onResume() {
        setOnBackground(false);
    }

    public void onPause() {
        setOnBackground(true);
    }

    public void onStop() {
        stopPlay();
    }

    public void onDestroy() {
        mAliPlayer.release();
        mContext = null;
    }

    /**
     * activity不可见或者播放页面不可见时调用该方法
     */
    private void setOnBackground(boolean isOnBackground) {
        this.isOnBackground = isOnBackground;
        if (isOnBackground) {
            pausePlay();
        } else {
            resumePlay();
        }
    }

    /**
     * 视频暂停/恢复的时候使用，
     */
    public void onPauseClick() {
        if (isPause) {
            resumePlay();
        } else {
            pausePlay();
        }
    }

    public int getDuration() {
        return totalPosition;
    }

    public void redraw() {
        mAliPlayer.redraw();
    }

    public void setSurface(Surface mSurface) {
        mAliPlayer.setSurface(mSurface);
    }

    public interface VideoUIListener {
        /**
         * 播放器进度回调
         *
         * @param position
         * @param total
         * @return
         */
        void setProgress(int position, int total);

        /**
         * 开始渲染首帧，去掉封面图
         */
        void onRenderingStart();

        /**
         * 播放完成
         */
        void onCompletion();

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
        void onStateChanged(int newState);
    }
}
