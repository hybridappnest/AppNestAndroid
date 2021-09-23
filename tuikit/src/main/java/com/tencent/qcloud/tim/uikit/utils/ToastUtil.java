package com.tencent.qcloud.tim.uikit.utils;

import android.widget.Toast;

import com.tencent.qcloud.tim.uikit.TUIKit;
import com.ymy.core.utils.ToastUtils;

/**
 * UI通用方法类
 */
public class ToastUtil {

    private static Toast mToast;

    public static final void toastLongMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                ToastUtils.showImageToast(TUIKit.getAppContext(), message, true);
            }
        });
    }


    public static final void toastShortMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                ToastUtils.showImageToast(TUIKit.getAppContext(), message, true);
            }
        });
    }

    public static final void toastShortErrorMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                ToastUtils.showImageToast(TUIKit.getAppContext(), message, false);
            }
        });
    }
}
