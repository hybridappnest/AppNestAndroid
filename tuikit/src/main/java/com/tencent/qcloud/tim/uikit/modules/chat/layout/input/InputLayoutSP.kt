package com.tencent.qcloud.tim.uikit.modules.chat.layout.input

import com.ymy.core.utils.Preference

/**
 * Created on 3/13/21 09:28.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:缓存输入框状态
 */
object InputLayoutSP {
    var InputLayoutShowType by Preference(Preference.IM_CHAT_INPUT_LAYOUT_TYPE,InputLayout.STATE_SOFT_INPUT)
}