package com.tencent.qcloud.tim.uikit.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tencent.qcloud.tim.uikit.R;

import androidx.annotation.Nullable;

public class NoticeV2Layout extends RelativeLayout {

    public NoticeV2Layout(Context context) {
        super(context);
        init();
    }

    public NoticeV2Layout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoticeV2Layout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.chat_yjya_notice_v2_layout, this);
    }
}
