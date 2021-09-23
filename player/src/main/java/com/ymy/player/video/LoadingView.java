package com.ymy.player.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ymy.player.R;

import java.lang.ref.WeakReference;

/**
 * loadingView
 *
 * @author xlx
 */
public class LoadingView extends View {

    private Paint paint;

    private static final int DEFAULT_HEIGHT = 2;
    /**
     * view width
     */
    private int width;

    /**
     * 避免重复start
     */
    private boolean needCanvas = true;

    /**
     * x起始位置
     */
    private int startX;

    /**
     * x结束位置
     */
    private int endX;

    /**
     * 默认颜色值, 后期考虑改为外部配置
     */
    private int[] gradientColors = {Color.parseColor("#1AD4FF"), Color.parseColor("#0836FB")};
    private int[] gradientColorsCenter = {Color.RED, Color.BLUE, Color.BLUE, Color.RED};
    private int[] gradientColorsReverse = {Color.BLUE, Color.RED};

    /**
     * 默认从左往右
     */
    private StartMode startMode = StartMode.LeftStart;
    /**
     * 线高
     */
    private static final int LINE_STROKE_WIDTH = 2;

    /**
     * 增量
     */
    private static final int DELTA = 10;

    /**
     * 绘制间隔时间, 单位: 毫秒(ms)
     */
    private static final int DELAY_TIME = 30;
    private ProgressHandler progressHandler;
    private static final int CODE_PROGRESS_START = 3000;
    private Shader gradient;

    private float[] gradientPercent = {0.1f, 0.2f, 0.8f, 0.9f};

    /**
     * 默认绘制模式---left
     */
    private static final String DEFAULT_START_MODE_LEFT = "left";
    /**
     * 默认绘制模式---center
     */
    private static final String DEFAULT_START_MODE_CENTER = "center";
    /**
     * 默认绘制模式---right
     */
    private static final String DEFAULT_START_MODE_RIGHT = "right";

    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("CustomViewStyleable")
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingViewAttrs);
        String mode = typedArray.getString(R.styleable.LoadingViewAttrs_startmode);
        if (DEFAULT_START_MODE_LEFT.equals(mode)) {
            startMode = StartMode.LeftStart;
        } else if (DEFAULT_START_MODE_CENTER.equals(mode)) {
            startMode = StartMode.Center;
        } else if (DEFAULT_START_MODE_RIGHT.equals(mode)) {
            startMode = StartMode.RightStart;
        } else {
            startMode = StartMode.LeftStart;
        }

        typedArray.recycle();
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(LINE_STROKE_WIDTH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        switch (startMode) {
            case LeftStart:
                startX = 0;
                endX = 0;
                break;
            case Center:
                startX = width / 2;
                endX = width / 2;
                break;
            case RightStart:
                startX = width;
                endX = width;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = 0;
        int height = 0;
        /**
         * 设置宽度
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.AT_MOST:
                width = getPaddingLeft() + getPaddingRight();
                break;

            case MeasureSpec.UNSPECIFIED:
                break;
            default:
                break;
        }

        /**
         * 设置高度
         */
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = width / 10;
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
            default:
                break;
        }

        setMeasuredDimension(width, height);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (startMode) {
            case LeftStart:
                gradient = new LinearGradient(startX, DEFAULT_HEIGHT, endX, DEFAULT_HEIGHT, gradientColors, null,
                    Shader.TileMode.CLAMP);
                break;
            case Center:
                gradient = new LinearGradient(startX, DEFAULT_HEIGHT, endX, DEFAULT_HEIGHT, gradientColorsCenter, gradientPercent,
                    Shader.TileMode.MIRROR);
                break;
            case RightStart:
                gradient = new LinearGradient(startX, DEFAULT_HEIGHT, endX, DEFAULT_HEIGHT, gradientColorsReverse, null,
                    Shader.TileMode.CLAMP);
                break;
            default:
                break;
        }
        paint.setShader(gradient);
        canvas.drawLine(startX, DEFAULT_HEIGHT, endX, DEFAULT_HEIGHT, paint);
    }

    /**
     * 绘制位置的计算
     */
    private void calculateValue() {
        switch (startMode) {
            case LeftStart:
                caculateFromLeft();
                break;
            case Center:
                caculateFromCenter();
                break;
            case RightStart:
                caculateFromRight();
                break;
            default:
                break;
        }

    }

    /**
     * 计算从左侧开始绘制
     */
    private void caculateFromLeft() {
        endX += DELTA;
        if (endX > width) {
            endX = 0;
        }
    }

    /**
     * 计算从中间开始绘制
     */
    private void caculateFromCenter() {
        startX -= DELTA;
        endX += DELTA;

        if (startX <= 0) {
            startX = width / 2;
        }

        if (endX >= width) {
            endX = width / 2;
        }
    }

    /**
     * 计算从右侧开始绘制
     */
    private void caculateFromRight() {
        startX -= DELTA;
        if (startX <= 0) {
            startX = width;
        }
    }

    /**
     * 开始progress
     */
    public void start() {
        if (needCanvas) {
            this.setVisibility(VISIBLE);
            needCanvas = false;
            progressHandler = new ProgressHandler(this);
            Message message = Message.obtain();
            message.what = CODE_PROGRESS_START;
            progressHandler.sendMessageDelayed(message, DELAY_TIME);
        }
    }

    /**
     * 取消progress
     */
    public void cancle() {
        if (!needCanvas) {
            this.setVisibility(INVISIBLE);
            progressHandler.removeMessages(CODE_PROGRESS_START);
            // 取消后, 重置起始位置
            switch (startMode) {
                case LeftStart:
                    startX = 0;
                    endX = 0;
                    break;
                case Center:
                    startX = width / 2;
                    endX = width / 2;
                    break;
                case RightStart:
                    startX = width;
                    endX = width;
                    break;
                default:
                    break;
            }

            needCanvas = true;
        }
    }

    private static class ProgressHandler extends Handler {
        WeakReference<LoadingView> weakReference;

        ProgressHandler(LoadingView view) {
            weakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CODE_PROGRESS_START) {
                LoadingView loadingView = weakReference.get();
                if (loadingView != null) {
                    loadingView.calculateValue();
                    loadingView.postInvalidate();
                    Message message = Message.obtain();
                    message.what = CODE_PROGRESS_START;
                    sendMessageDelayed(message, DELAY_TIME);
                }
            }
        }
    }

    enum StartMode {
        /**
         * 从左开始绘制
         */
        LeftStart,
        /**
         * 从右开始绘制
         */
        RightStart,
        /**
         * 从中间往两边
         */
        Center
    }

}
