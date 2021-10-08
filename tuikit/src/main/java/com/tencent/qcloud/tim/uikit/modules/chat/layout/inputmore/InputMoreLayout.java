package com.tencent.qcloud.tim.uikit.modules.chat.layout.inputmore;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.ymy.im.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class InputMoreLayout extends LinearLayout {

    public InputMoreLayout(Context context) {
        super(context);
        init();
    }

    public InputMoreLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InputMoreLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private List<InputMoreActionUnit> mInputMoreList = new ArrayList<>();
    private ImageView[] indicators;
    private void initIndicator() {
        if (mInputMoreList == null || mInputMoreList.size() <= 0) {
            return;
        }
        int ivSize =  (mInputMoreList.size() + 8 - 1) / 8;
        //设置指示器
        indicators = new ImageView[ivSize];
        mBannerIndicator.removeAllViews();
        //添加dot
        LinearLayout.LayoutParams params = null;
        ImageView dot = null;
        if (indicators.length > 1) {
            for (int i = 0; i < indicators.length; i++) {
                dot = new ImageView(TUIKit.getAppContext());
                params = new LinearLayout.LayoutParams(DensityUtil.dip2pxX(5),
                        DensityUtil.dip2pxX(4));
                if(i>0){
                    params.leftMargin = 8;
                }
                indicators[i] = dot;
                dot.setLayoutParams(params);
                mBannerIndicator.addView(dot);
            }
        }
    }
    private void setIndicator(int position) {
        for (int i = 0; indicators.length > 0 && i < indicators.length; i++) {
            ImageView indicator = indicators[i];
            if (indicator != null) {
                if (i == position) {
                    indicator.setBackgroundResource(R.drawable.update_bg_select);
                } else {
                    indicator.setBackgroundResource(R.drawable.update_bg);
                }
            }
        }
    }
    private void init() {
        inflate(getContext(), R.layout.chat_inputmore_layout, this);
        mBannerIndicator = findViewById(R.id
                .business_header_banner_ll_indicator);
    }
    private LinearLayout mBannerIndicator;  //指示器容器
    // 初始化更多布局adapter
    public void init(List<InputMoreActionUnit> actions) {
        mInputMoreList =actions;

        final ViewPager viewPager = findViewById(R.id.viewPager);
        ActionsPagerAdapter adapter = new ActionsPagerAdapter(viewPager, actions);
        viewPager.setAdapter(adapter);
        if (mInputMoreList != null && mInputMoreList.size() > 0) {
            initIndicator();
            setIndicator(0);
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int
                    positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mInputMoreList == null || mInputMoreList.size() == 0) return;
                setIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}
