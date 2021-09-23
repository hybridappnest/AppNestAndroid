package com.ymy.push.ali

import com.jeremyliao.liveeventbus.core.LiveEvent

/**
 * Created on 2/3/21 17:08.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class PushTokenData(
    val keyword: String,
    val token: String,
) : LiveEvent