package com.ymy.appnest.wx

import com.jeremyliao.liveeventbus.core.LiveEvent
import com.ymy.appnest.BuildConfig

/**
 * Created on 3/8/21 16:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object WXCons {
    const val WX_APP_ID = BuildConfig.WX_APP_ID
}

object WXLoginManager {
    /**
     * wx请求码，调起时写入，回调时检查
     */
    var wx_login_status = ""
}

class WXLiveData(
    val status: Int = status_fail,
    val code: String = ""
) : LiveEvent{
    companion object{
        const val status_success = 1
        const val status_cancel = 2
        const val status_fail = 0
    }
}