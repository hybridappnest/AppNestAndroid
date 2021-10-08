package com.ymy.plugin

object Versions {
    const val appcompat = "1.2.0"
    const val activity = "1.2.3"
    const val fragment = "1.3.3"
    const val lifecycle = "2.3.1"
    const val constraintLayout = "2.0.4"
    const val swipeRefreshLayout = "1.1.0"
    const val roomVersion = "2.3.0"
    const val workVersion = "2.5.0"

    const val baseRecyclerViewAdapterHelper = "3.0.4"
    const val banner = "1.4.10"
    const val preference = "1.1.1"
    const val cardView = "1.0.0"
    const val material = "1.2.1"
    const val recyclerView = "1.2.1"
    const val viewmodel_ktx = "2.2.0"
    const val livedata_ktx = "2.2.0"
    const val viewPager2 = "1.0.0"
    const val core_ktx = "1.3.2"
    const val navigation = "2.2.2"
    const val lifecycle_extension = "2.2.0"
    const val camerax_version = "1.0.0"
    const val camerax_view_version = "1.0.0"
    const val androidxannotation_version = "1.1.0"
    const val androidxlegacy_version = "1.0.0"


    const val kotlin = "1.5.20"
    const val koin3 = "3.0.2"

    const val circleImageview = "2.2.0"
    const val easypermissions = "3.0.0"

    const val verticalTabLayout = "1.2.5"
    const val flowLayout = "1.1.2"

    const val autosize = "1.2.1"

    const val gson = "2.8.6"
    const val fastjson = "1.2.39"

    const val glide = "4.11.0"
    const val glide_compiler = "4.11.0"
    const val glide_okhttp3 = "4.11.0"

    const val leakcanary = "2.0-alpha-3"


    const val retrofit = "2.9.0"
    const val retrofit_converter_gson = "2.9.0"
    const val okhttp_logging_interceptor = "4.8.1"
    const val okhttp = "4.8.1"
    const val persistentCookieJar = "v1.0.1"
}

object AndroidX {
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val activity_ktx = "androidx.activity:activity-ktx:${Versions.activity}"
    const val androidx_fragment = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    const val androidx_tracing_ktx = "androidx.tracing:tracing-ktx:1.0.0"
    const val lifecycle_runtime_ktx =
        "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val swiperefreshlayout =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefreshLayout}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val livedata_ktx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.livedata_ktx}"
    const val viewPager2 = "androidx.viewpager2:viewpager2:${Versions.viewPager2}"
    const val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    const val navigation_fragment_ktx =
        "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val navigation_ui_ktx = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    const val viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.viewmodel_ktx}"

    const val lifecycle_extension =
        "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle_extension}"

    const val cardView = "androidx.cardview:cardview:${Versions.cardView}"
    const val work_runtime_ktx = "androidx.work:work-runtime-ktx:${Versions.workVersion}"
    const val preference = "androidx.preference:preference:${Versions.preference}"
    const val preference_ktx = "androidx.preference:preference-ktx:${Versions.preference}"
    const val camerax_core = "androidx.camera:camera-core:${Versions.camerax_version}"
    const val camerax_camera2 = "androidx.camera:camera-camera2:${Versions.camerax_version}"
    const val camera_lifecycle = "androidx.camera:camera-lifecycle:${Versions.camerax_version}"
    const val camera_view = "androidx.camera:camera-view:${Versions.camerax_view_version}"
    const val androidxannotation =
        "androidx.annotation:annotation:${Versions.androidxannotation_version}"
    const val androidxlegacy =
        "androidx.legacy:legacy-support-v4:${Versions.androidxlegacy_version}"
    const val datastore_preferences =
        "androidx.datastore:datastore-preferences:1.0.0-beta01"
}

object Room {
    const val room_compiler = "androidx.room:room-compiler:${Versions.roomVersion}"
    const val room_runtime = "androidx.room:room-runtime:${Versions.roomVersion}"
    const val room_ktx = "androidx.room:room-ktx:${Versions.roomVersion}"
}

object Kt {
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val kotlin_coroutines_android =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0"
}

object Okhttp {
    // network
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofit_converter_gson =
        "com.squareup.retrofit2:converter-gson:${Versions.retrofit_converter_gson}"
    const val okhttp_logging_interceptor =
        "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp_logging_interceptor}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val persistentCookieJar =
        "com.github.franmontiel:PersistentCookieJar:${Versions.persistentCookieJar}"
}

object Koin {

    // Koin for Kotlin Multiplatform
    const val koin_core = "io.insert-koin:koin-core:${Versions.koin3}"

    //    Koin Extended & experimental features (JVM)
    const val koin_core_ext = "io.insert-koin:koin-core-ext:${Versions.koin3}"

    //    Koin main features for Android (Scope,ViewModel ...)
    const val koin_android = "io.insert-koin:koin-android:${Versions.koin3}"

    //    Koin Android - experimental builder extensions
    const val koin_android_ext = "io.insert-koin:koin-android-ext:${Versions.koin3}"

    //    Koin for Jetpack Compose (unstable version)
//    const val koin_androidx_compose = "io.insert-koin:koin-androidx-compose:${Versions.koin3}"

    //    Koin for Jetpack WorkManager
//    const val koin_workmanager = "io.insert-koin:koin-androidx-workmanager:${Versions.koin3}"


}

object Third {
    const val circleimageview = "de.hdodenhof:circleimageview:${Versions.circleImageview}"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}"
    const val baseRecyclerViewAdapterHelper =
        "com.github.CymChad:BaseRecyclerViewAdapterHelper:${Versions.baseRecyclerViewAdapterHelper}"
    const val banner = "com.youth.banner:banner:${Versions.banner}"
    const val verticalTabLayout = "q.rorbin:VerticalTabLayout:${Versions.verticalTabLayout}"
    const val flowLayout = "com.hyman:flowlayout-lib:${Versions.flowLayout}"

    //    动态权限请求
    const val easypermissions = "pub.devrel:easypermissions:${Versions.easypermissions}"

    //    屏幕适配
    const val autosize = "me.jessyan:autosize:${Versions.autosize}"


    const val fastjson = "com.alibaba:fastjson:${Versions.fastjson}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val loadingView = "com.wang.avi:library:2.1.3"
    const val systembartint = "com.readystatesoftware.systembartint:systembartint:1.0.3"
    const val logger = "com.orhanobut:logger:2.2.0"

    //定制化指示器控件https://github.com/hackware1993/MagicIndicator
    const val MagicIndicator = "com.github.hackware1993:MagicIndicator:1.5.0"

    const val PickerView = "com.contrarywind:Android-PickerView:4.1.9"

    //图片浏览框架
    const val mojito_core = "net.mikaelzero.mojito:core:1.1.0"
    const val mojito_SketchImageFactory = "net.mikaelzero.mojito:SketchImageFactory:1.1.0"
    const val mojito_GlideImageLoader = "net.mikaelzero.mojito:GlideImageLoader:1.1.0"

    //红点
    const val badgeview = "q.rorbin:badgeview:1.1.3"

    //switchbutton
    const val switchbutton = "com.kyleduo.switchbutton:library:2.0.2"

    //汉字转拼音
    const val tinypinyin = "com.github.promeg:tinypinyin:2.0.3"

    //基于Android WebView
    const val agentweb = "com.github.Justson.AgentWeb:agentweb-core:v4.1.9-androidx"
    const val agentfile = "com.github.Justson.AgentWeb:agentweb-filechooser:v4.1.9-androidx"
    const val agentdownload = "com.github.Justson:Downloader:v4.1.9-androidx"

    //基于Android live-event-bus
    const val live_event_bus = "com.jeremyliao:live-event-bus-x:1.7.2"

    //悬浮窗控件
    const val EasyFloat = "com.github.princekin-f:EasyFloat:1.3.4"

    //图片添加水印
    const val waterMark = "com.huangyz0918:androidwm-light:0.1.2"
}

object Glide {
    //    图片加载框架
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glide_compiler = "com.github.bumptech.glide:compiler:${Versions.glide_compiler}"
    const val glide_okhttp3 =
        "com.github.bumptech.glide:okhttp3-integration:${Versions.glide_okhttp3}"
}

object Aliyun {
    //    阿里播放器
    const val AliyunPlayer = "com.aliyun.sdk.android:AliyunPlayer:4.7.3-full"

    const val arouterApi = "com.alibaba:arouter-api:1.5.2"
    const val arouterComplier = "com.alibaba:arouter-compiler:1.5.2"

}