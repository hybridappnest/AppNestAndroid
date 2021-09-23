package com.ymy.core.notchtools.helper;

import android.os.Build;

/**
 * @author zhangzhun
 * @date 2018/11/4
 */
public class DeviceBrandTools {

    private static DeviceBrandTools sDeviceBrandTools;

    public static DeviceBrandTools getInstance() {
        if (sDeviceBrandTools == null) {
            synchronized (DeviceBrandTools.class) {
                if (sDeviceBrandTools == null) {
                    sDeviceBrandTools = new DeviceBrandTools();
                }
            }
        }
        return sDeviceBrandTools;
    }

    public static boolean isXiaoMi() {
        return "xiaomi".equalsIgnoreCase(Build.BRAND) || "xiaomi".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isHuaWei() {
        return "huawei".equalsIgnoreCase(Build.BRAND) || "huawei".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isOppo() {
        return "oppo".equalsIgnoreCase(Build.BRAND) || "oppo".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isVivo() {
        return "vivo".equalsIgnoreCase(Build.BRAND) || "vivo".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isMeizu() {
        return "meizu".equalsIgnoreCase(Build.BRAND) || "meizu".equalsIgnoreCase(Build.MANUFACTURER) || "22c4185e".equalsIgnoreCase(Build.BRAND);
    }

    public final boolean isSamsung() {
        return "samsung".equalsIgnoreCase(Build.BRAND);
    }

    private String getSystemProperty(String propName) {
        return SystemProperties.getInstance().get(propName);
    }
}
