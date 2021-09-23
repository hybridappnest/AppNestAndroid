package com.ymy;

import android.view.View;

import com.ymy.core.base.RootActivity;

public class TuikitBaseActivity extends RootActivity {
    View statusBarView;
    public void initStatusBar(int sourceId) {

        if (statusBarView == null) {

            int identifier = getResources().getIdentifier("statusBarBackground", "id", "android");

            statusBarView = getWindow().findViewById(identifier);

        }

        if (statusBarView != null) {

            statusBarView.setBackgroundDrawable(null);//在设置前将背景设置为null;

            statusBarView.setBackgroundResource(sourceId);

        }
    }
}
