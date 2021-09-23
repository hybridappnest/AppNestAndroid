package com.ymy.core.notchtools.core;

import android.app.Activity;
import android.view.Window;

import com.ymy.core.notchtools.helper.NotchStatusBarUtils;


/**
 * @author zhangzhun
 * @date 2018/11/4
 */

public abstract class AbsNotchScreenSupport implements INotchSupport {

    @Override
    public int getStatusHeight(Window window) {
        return NotchStatusBarUtils.getStatusBarHeight(window.getContext());
    }

    @Override
    public void fullScreenDontUseStatus(Activity activity, OnNotchCallBack notchCallBack) {
        NotchStatusBarUtils.setFullScreenWithSystemUi(activity.getWindow(), false);
        onBindCallBackWithNotchProperty(activity, notchCallBack);
    }

    @Override
    public void fullScreenDontUseStatusForPortrait(Activity activity, OnNotchCallBack notchCallBack) {
        fullScreenDontUseStatus(activity, notchCallBack);
    }

    @Override
    public void fullScreenDontUseStatusForLandscape(Activity activity, OnNotchCallBack notchCallBack) {
        NotchStatusBarUtils.setFullScreenWithSystemUi(activity.getWindow(), false);
        onBindCallBackWithNotchProperty(activity, notchCallBack);
        if (isNotchScreen(activity.getWindow())) {
            NotchStatusBarUtils.hideFakeNotchView(activity.getWindow());
        }
    }

    @Override
    public void fullScreenUseStatus(Activity activity, OnNotchCallBack notchCallBack) {
        NotchStatusBarUtils.setFullScreenWithSystemUi(activity.getWindow(), false);
        onBindCallBackWithNotchProperty(activity, getNotchHeight(activity.getWindow()), notchCallBack);
    }

    @Override
    public void translucentStatusBar(Activity activity) {
        translucentStatusBar(activity, null);
    }

    @Override
    public void translucentStatusBar(Activity activity, OnNotchCallBack onNotchCallBack) {
        NotchStatusBarUtils.hideFakeNotchView(activity.getWindow());
        NotchStatusBarUtils.setStatusBarTransparent(activity.getWindow(), onNotchCallBack);
    }

    protected void onBindCallBackWithNotchProperty(Activity activity, OnNotchCallBack notchCallBack) {
        if (notchCallBack != null) {
            NotchProperty notchProperty = new NotchProperty();
            notchProperty.setNotchHeight(getNotchHeight(activity.getWindow()));
            notchProperty.setNotch(isNotchScreen(activity.getWindow()));
            if (notchCallBack != null) {
                notchCallBack.onNotchPropertyCallback(notchProperty);
            }
        }
    }

    protected void onBindCallBackWithNotchProperty(Activity activity, int marginTop, OnNotchCallBack notchCallBack) {
        if (notchCallBack != null) {
            NotchProperty notchProperty = new NotchProperty();
            notchProperty.setNotchHeight(getNotchHeight(activity.getWindow()));
            notchProperty.setNotch(isNotchScreen(activity.getWindow()));
            notchProperty.setMarginTop(marginTop);
            if (notchCallBack != null) {
                notchCallBack.onNotchPropertyCallback(notchProperty);
            }
        }
    }
}
