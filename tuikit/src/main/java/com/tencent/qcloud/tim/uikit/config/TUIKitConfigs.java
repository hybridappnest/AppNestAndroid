package com.tencent.qcloud.tim.uikit.config;

import android.os.Environment;
import android.util.Log;

import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.qcloud.tim.uikit.TUIKit;

import java.io.File;

public class TUIKitConfigs {

    private static TUIKitConfigs sConfigs;
    private GeneralConfig generalConfig;
    private CustomFaceConfig customFaceConfig;
    private V2TIMSDKConfig sdkConfig;

    private TUIKitConfigs() {

    }

    /**
     * 获取TUIKit的全部配置
     *
     * @return
     */
    public static TUIKitConfigs getConfigs() {
        if (sConfigs == null) {
            sConfigs = new TUIKitConfigs();
        }
        return sConfigs;
    }

    /**
     * 获取TUIKit的通用配置
     *
     * @return
     */
    public GeneralConfig getGeneralConfig() {
        if(generalConfig == null){
            GeneralConfig config = new GeneralConfig();
            // 显示对方是否已读的view将会展示
            config.setShowRead(false);
            config.setAppCacheDir(TUIKit.getAppContext().getFilesDir().getPath());
            if (new File(Environment.getExternalStorageDirectory() + "/111222333").exists()) {
                config.setTestEnv(true);
            }
            TUIKit.getConfigs().setGeneralConfig(config);
            return  config;
        }
        return generalConfig;
    }

    /**
     * 设置TUIKit的通用配置
     *
     * @param generalConfig
     * @return
     */
    public TUIKitConfigs setGeneralConfig(GeneralConfig generalConfig) {
        this.generalConfig = generalConfig;
        return this;
    }

    /**
     * 获取自定义表情包配置
     *
     * @return
     */
    public CustomFaceConfig getCustomFaceConfig() {
        return customFaceConfig;
    }

    /**
     * 设置自定义表情包配置
     *
     * @param customFaceConfig
     * @return
     */
    public TUIKitConfigs setCustomFaceConfig(CustomFaceConfig customFaceConfig) {
        this.customFaceConfig = customFaceConfig;
        return this;
    }

    /**
     * 获取IMSDK的配置
     *
     * @return
     */
    public V2TIMSDKConfig getSdkConfig() {
        return sdkConfig;
    }

    /**
     * 设置IMSDK的配置
     *
     * @param timSdkConfig
     * @return
     */
    public TUIKitConfigs setSdkConfig(V2TIMSDKConfig timSdkConfig) {
        this.sdkConfig = timSdkConfig;
        return this;
    }

}
