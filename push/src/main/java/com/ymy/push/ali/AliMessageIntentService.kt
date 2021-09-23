package com.ymy.push.ali

import android.content.Context
import android.util.Log
import com.alibaba.sdk.android.push.AliyunMessageIntentService
import com.alibaba.sdk.android.push.notification.CPushMessage


/**
 * Created on 2/4/21 10:46.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 *
 * 为避免推送广播被系统拦截的小概率事件,我们推荐用户通过IntentService处理消息互调,接入步骤:
 * 1. 创建IntentService并继承AliyunMessageIntentService
 * 2. 覆写相关方法,并在Manifest的注册该Service
 * 3. 调用接口CloudPushService.setPushIntentService
 * 详细用户可参考:https://help.aliyun.com/document_detail/30066.html#h2-2-messagereceiver-aliyunmessageintentservice
 */
class AliMessageIntentService : AliyunMessageIntentService() {

    private val TAG = "AliMessageIntentService"

    /**
     * 推送通知的回调方法
     * @param context
     * @param title
     * @param summary
     * @param extraMap
     */
    override fun onNotification(
        context: Context?,
        title: String,
        summary: String,
        extraMap: Map<String?, String?>?
    ) {
        Log.i(TAG, "收到一条推送通知 ： $title, summary:$summary")
    }

    /**
     * 推送消息的回调方法
     * @param context
     * @param cPushMessage
     */
    override fun onMessage(context: Context?, cPushMessage: CPushMessage) {
        Log.i(
            TAG,
            "收到一条推送消息 ： " + cPushMessage.getTitle()
                .toString() + ", content:" + cPushMessage.getContent()
        )
    }

    /**
     * 从通知栏打开通知的扩展处理
     * @param context
     * @param title
     * @param summary
     * @param extraMap
     */
    override fun onNotificationOpened(
        context: Context?,
        title: String,
        summary: String,
        extraMap: String
    ) {
        Log.i(TAG, "onNotificationOpened ：  : $title : $summary : $extraMap")
    }

    /**
     * 无动作通知点击回调。当在后台或阿里云控制台指定的通知动作为无逻辑跳转时,通知点击回调为onNotificationClickedWithNoAction而不是onNotificationOpened
     * @param context
     * @param title
     * @param summary
     * @param extraMap
     */
    override fun onNotificationClickedWithNoAction(
        context: Context?,
        title: String,
        summary: String,
        extraMap: String
    ) {
        Log.i(TAG, "onNotificationClickedWithNoAction ：  : $title : $summary : $extraMap")
    }

    /**
     * 通知删除回调
     * @param context
     * @param messageId
     */
    override fun onNotificationRemoved(context: Context?, messageId: String) {
        Log.i(TAG, "onNotificationRemoved ： $messageId")
    }

    /**
     * 应用处于前台时通知到达回调。注意:该方法仅对自定义样式通知有效,相关详情请参考https://help.aliyun.com/document_detail/30066.html#h3-3-4-basiccustompushnotification-api
     * @param context
     * @param title
     * @param summary
     * @param extraMap
     * @param openType
     * @param openActivity
     * @param openUrl
     */
    override fun onNotificationReceivedInApp(
        context: Context?,
        title: String,
        summary: String,
        extraMap: Map<String?, String?>,
        openType: Int,
        openActivity: String,
        openUrl: String
    ) {
        Log.i(
            TAG,
            "onNotificationReceivedInApp ：  : $title : $summary  $extraMap : $openType : $openActivity : $openUrl"
        )
    }
}