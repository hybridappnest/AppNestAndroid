package com.tencent.qcloud.tim.uikit.modules.chat.layout.message;

/**
 * Created on 2020/9/18 15:03.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc：自定义消息处理动作类型
 */
public interface CustomActionType {
    //跳转h5_url字段中的地址
    String ACTION_JUMP_URL = "0";
    //处理事件
    String ACTION_HANDLE_EVENT = "1";
    //跳转事件详情
    String ACTION_JUMP_EVENT_DETAIL = "2";
    //跳转中控交接处理和查看界面 status = "1" 处理 "0"查看
    String ACTION_JUMP_ZKJJ_DETAIL = "3";

    String ACTION_JUMP_SJPX_PICTURE = "4";
}
