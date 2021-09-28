package com.ymy.plugin

object BuildConfig {
    const val applicationId = PackageConfig.applicationId
    const val compileSdk = 30
    const val buildTools = "30.0.2"
    const val minSdk = 21
    const val targetSdk = 26
    const val versionCode = 91
    const val versionName = "2.0.28"
}


object PackageConfig {
    const val applicationId = "com.ymy.appnest"

    /**
     * 阿里一键登录
     */
    const val AuthSDKID: String = ""

    /**
     * 百度定位配置
     */
    const val baidu_location_id: String = ""

    /**
     * 微信登录
     */
    const val WX_APP_ID: String = ""

    /**
     * 华为_push
     */
    const val HW_push_appid: String = ""

    /**
     * vivo_push
     */
    const val vivo_push_key: String = ""

    /**
     * vivo_push
     */
    const val vivo_push_appId: String = ""

    /**
     * ali_push
     */
    const val ali_appId: String = ""

    /**
     * ali_push
     */
    const val ali_appsecret: String = ""

    /**
     * xiaomi_push
     */
    const val XIAOMI_APP_ID: String = ""

    /**
     * xiaomi_push
     */
    const val XIAOMI_APP_KEY: String = ""

    /**
     * meizu_push
     */
    const val MEIZU_APP_ID: String = ""

    const val MEIZU_APP_KEY: String = ""

    /**
     * oppo_push
     */
    const val OPPO_APP_KEY: String = ""

    const val OPPO_APP_SECRET: String = ""


    /**
     * oss相关配置
     */
    const val OSS_stsServer: String = ""

    const val OSS_endpoint: String = ""

    const val OSS_bucketName: String = ""
}