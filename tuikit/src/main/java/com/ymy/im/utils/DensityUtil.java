package com.ymy.im.utils;

import android.content.Context;

import com.tencent.qcloud.tim.uikit.TUIKit;

public class DensityUtil {
    /**
     * 获得x方向的dp转像素
     * @param dpvalue
     * @return
     */
    public static int dip2pxX(float dpvalue){
        return (int) (dpvalue* TUIKit.getAppContext().getResources().getDisplayMetrics().xdpi / 160+0.5f);
    }
    /**
     * 获得y方向的dp转像素
     * @param dpvalue
     * @return
     */
    public static int dip2pxY(float dpvalue){
        return (int) (dpvalue*  TUIKit.getAppContext().getResources().getDisplayMetrics().ydpi / 160+0.5f);
    }

    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
    
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (pxValue / fontScale + 0.5f);  
    }
    public static long timeStamp = 0l;

    /**
     * 是否快速点击
     * @return
     */
    public static boolean isFastClcick(){
        if((System.currentTimeMillis() - timeStamp)<800){
            return  true;
        }
        timeStamp = System.currentTimeMillis();
        return false;
    }
}
