package com.tencent.qcloud.tim.uikit.component;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.tim.uikit.R;
import com.ymy.core.ui.IMUIPresenter;
import com.ymy.helper.type.EventType;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;

public class NoticeEventLayout extends RelativeLayout {

    private TextView mTvEventStatus;
    private TextView mTvEventStatusTitle;
    private TextView mTvEventLevel;
    private TextView mTimeText;
    private ImageView mPointEventLevel;
    private TextView mTvEventLiving;
    private View mllTime;
    private Chronometer mChronometer;
    private View mllLevel;
    private View mContent;

    public NoticeEventLayout(Context context) {
        super(context);
        init();
    }

    public NoticeEventLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoticeEventLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public static int getLevelColorWithType(String eventType, int level) {
        switch (eventType) {
            case EventType.TYPE_ALARM:
                return IMUIPresenter.getAlarmLevelColor(level);
            case EventType.TYPE_WORKORDER:
                return IMUIPresenter.getWorkOrderLevelColor(level);
            case EventType.TYPE_XJ:
                return IMUIPresenter.getXJLevelColor(level);
            case EventType.TYPE_YHPC:
                return IMUIPresenter.getYHPCLevelColor(level);
            case EventType.TYPE_YAYL:
                return IMUIPresenter.getActingLevelColor(level);
            case EventType.TYPE_SJPX:
                return IMUIPresenter.getTrainingLevelColor(level);
            case EventType.TYPE_ZKJJ:
                return IMUIPresenter.getZKJJLevelColor(level);
            default:
                return 0;
        }
    }

    public String getStatusTextWithStatus(String eventType, int status) {
        switch (eventType) {
            case EventType.TYPE_ALARM:
                return IMUIPresenter.getAlarmStateStr(status);
            case EventType.TYPE_WORKORDER:
                return IMUIPresenter.getWorkOrderStateStr(status);
            case EventType.TYPE_XJ:
                return IMUIPresenter.getXJStateStr(status);
            case EventType.TYPE_YHPC:
                return IMUIPresenter.getYHPCStateStr(status);
            case EventType.TYPE_YAYL:
                return IMUIPresenter.getYAYLStateStr(status);
            default:
                return "";
        }
    }

    private void init() {
        setBackgroundColor(getContext().getResources().getColor(R.color.grayffb4b8c5));
        inflate(getContext(), R.layout.chat_alarm_notice_layout, this);
        mContent = findViewById(R.id.content);
        mContent.setVisibility(View.INVISIBLE);
        mTvEventStatusTitle = findViewById(R.id.title);
        mTvEventStatus = findViewById(R.id.tv_event_status);
        mPointEventLevel = findViewById(R.id.view_spot_level);
        mTvEventLevel = findViewById(R.id.tv_event_level);
        mTvEventLiving = findViewById(R.id.tv_event_living);
        mllTime = findViewById(R.id.ll_time);
        mChronometer = findViewById(R.id.chronometer);
        mTimeText = findViewById(R.id.tv_ll_time);
        mllLevel = findViewById(R.id.ll_level);
    }

    public void setViewInfo(String eventType, int status, int level, String yaylStartTime, long yaylTime) {
        mContent.setVisibility(View.VISIBLE);
        if (eventType.equals(EventType.TYPE_YAYL)) {
            mllTime.setVisibility(View.VISIBLE);
            mllLevel.setVisibility(View.GONE);
            mTvEventStatusTitle.setText("开始演练时间:");
            mTvEventStatus.setText(yaylStartTime);
            if (TextUtils.equals("未开启", yaylStartTime)) {
                mTimeText.setText("演练未开启");
                mTimeText.setVisibility(View.VISIBLE);
                mChronometer.setVisibility(View.GONE);
                return;
            }
            if (yaylTime == 0L) {
                mTimeText.setText("演练已结束");
                mTimeText.setVisibility(View.VISIBLE);
                mChronometer.setVisibility(View.GONE);
                return;
            }
            mTimeText.setVisibility(View.GONE);
            mChronometer.setVisibility(View.VISIBLE);
            long length = System.currentTimeMillis() - yaylTime;
            mChronometer.setBase(SystemClock.elapsedRealtime() - length);
            mChronometer.start();
        } else {
            mllTime.setVisibility(View.GONE);
            mllLevel.setVisibility(View.VISIBLE);
            String statusTextWithStatus = getStatusTextWithStatus(eventType, status);
            if (statusTextWithStatus.isEmpty()) {
                mTvEventStatusTitle.setVisibility(View.INVISIBLE);
            } else {
                mTvEventStatusTitle.setVisibility(View.VISIBLE);
            }
            mTvEventStatus.setText(statusTextWithStatus);
            int levelColor = getLevelColorWithType(eventType, level);
            mTvEventLevel.setTextColor(levelColor);
            mTvEventLevel.setText(level + "级");
            changeColorTint(mPointEventLevel, levelColor);
        }
    }

    private void changeColorTint(@NotNull ImageView mPointEventLevel, int color) {
        mPointEventLevel.setColorFilter(color);
    }

    public void setLiveText(boolean show, String str) {
        mTvEventLiving.setVisibility(show ? View.VISIBLE : View.GONE);
        mTvEventLiving.setText(str);
    }
}
