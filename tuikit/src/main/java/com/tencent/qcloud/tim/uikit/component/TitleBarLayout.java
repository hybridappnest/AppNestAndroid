package com.tencent.qcloud.tim.uikit.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.ITitleBarLayout;
import com.tencent.qcloud.tim.uikit.utils.ScreenUtil;


public class TitleBarLayout extends LinearLayout implements ITitleBarLayout {

    private LinearLayout mLeftGroup;
    private LinearLayout mRightGroup;
    private LinearLayout mRightGroup2;

    private TextView mLeftTitle;
    private TextView mCenterTitle;
    private TextView mRightTitle;
    private ImageView mLeftIcon;
    private ImageView mRightIcon;
    private ImageView mRightIcon2;
    private LinearLayout mTitleLayout;


    public TitleBarLayout(Context context) {
        super(context);
        init();
    }

    public TitleBarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TitleBarLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.title_bar_layout, this);
        mTitleLayout = findViewById(R.id.page_title_layout);
        mLeftGroup = findViewById(R.id.page_title_left_group);
        mRightGroup = findViewById(R.id.page_title_right_group);
        mRightGroup2 =findViewById(R.id.page_title_right_group2);
        mLeftTitle = findViewById(R.id.page_title_left_text);
        mRightTitle = findViewById(R.id.page_title_right_text);
        mCenterTitle = findViewById(R.id.page_title);
        mLeftIcon = findViewById(R.id.page_title_left_icon);
        mRightIcon = findViewById(R.id.page_title_right_icon);
        mRightIcon2 = findViewById(R.id.page_title_right_icon2);
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTitleLayout.getLayoutParams();
//        params.height = ScreenUtil.getPxByDp(45);
//        mTitleLayout.setLayoutParams(params);
        setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void setOnLeftClickListener(OnClickListener listener) {
        mLeftGroup.setOnClickListener(listener);
    }

    @Override
    public void setOnRightClickListener(OnClickListener listener) {
        mRightGroup.setOnClickListener(listener);
    }
    public void setOnRightClickListener2(OnClickListener listener) {
        mRightGroup2.setOnClickListener(listener);
    }
    public void setRightHide() {
        mRightGroup2.setVisibility(View.GONE);
    }
    @Override
    public void setTitle(String title, POSITION position) {
        switch (position) {
            case LEFT:
                mLeftTitle.setText(title);
                break;
            case RIGHT:
                mRightTitle.setText(title);
                break;
            case MIDDLE:
                mCenterTitle.setText(title);
                break;
        }
    }

    @Override
    public LinearLayout getLeftGroup() {
        return mLeftGroup;
    }

    @Override
    public LinearLayout getRightGroup() {
        return mRightGroup;
    }

    @Override
    public ImageView getLeftIcon() {
        return mLeftIcon;
    }

    @Override
    public void setLeftIcon(int resId) {
        mLeftIcon.setImageResource(resId);
    }

    @Override
    public ImageView getRightIcon() {
        return mRightIcon;
    }

    @Override
    public void setRightIcon(int resId) {
        mRightIcon.setImageResource(resId);
    }

    @Override
    public ImageView getRightIcon2() {
        return mRightIcon2;
    }
    @Override
    public void setRightIcon2(int resId) {
        mRightIcon2.setVisibility(View.VISIBLE);
        mRightIcon2.setImageResource(resId);
    }
    @Override
    public TextView getLeftTitle() {
        return mLeftTitle;
    }

    @Override
    public TextView getMiddleTitle() {
        return mCenterTitle;
    }

    @Override
    public TextView getRightTitle() {
        return mRightTitle;
    }
}
