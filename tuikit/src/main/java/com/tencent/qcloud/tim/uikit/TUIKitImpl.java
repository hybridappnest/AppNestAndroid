package com.tencent.qcloud.tim.uikit;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.ymy.helper.ImHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.orhanobut.logger.Logger;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMConversationListener;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMFriendshipListener;
import com.tencent.imsdk.v2.V2TIMGroupChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfoResult;
import com.tencent.imsdk.v2.V2TIMGroupListener;
import com.tencent.imsdk.v2.V2TIMGroupMemberChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.imsdk.v2.V2TIMSDKListener;
import com.tencent.imsdk.v2.V2TIMTextElem;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.liteav.AVCallManager;
import com.tencent.qcloud.tim.uikit.base.IMEventListener;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.face.FaceManager;
import com.tencent.qcloud.tim.uikit.config.GeneralConfig;
import com.tencent.qcloud.tim.uikit.config.TUIKitConfigs;
import com.tencent.qcloud.tim.uikit.modules.chat.C2CChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfoProvider;
import com.tencent.qcloud.tim.uikit.modules.message.Entry;
import com.tencent.qcloud.tim.uikit.modules.message.Expand;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageRevokedManager;
import com.tencent.qcloud.tim.uikit.modules.message.ViewData;
import com.tencent.qcloud.tim.uikit.modules.message.bean.AlarmIMInfo;
import com.tencent.qcloud.tim.uikit.utils.BackgroundTasks;
import com.tencent.qcloud.tim.uikit.utils.FileUtil;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.ymy.core.BuildConfig;
import com.ymy.core.bean.AlarmComeInfo;
import com.ymy.core.bean.CollectorType;
import com.ymy.core.bean.YAYLAlarmInfoKt;
import com.ymy.core.lifecycle.KtxManager;
import com.ymy.core.manager.BeepHelper;
import com.ymy.core.ok3.GsonUtils;
import com.ymy.core.user.YmyUserManager;
import com.ymy.core.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.tencent.qcloud.tim.uikit.utils.NetWorkUtils.sIMSDKConnected;

/**
 * @author hanxueqiang
 */
public class TUIKitImpl {

    private static final String TAG = "TUIKit";
    public static GroupInfoProvider mGroupInfoProvider = new GroupInfoProvider();
    private static Context sAppContext;
    private static TUIKitConfigs sConfigs;
    private static List<IMEventListener> sIMEventListeners = new ArrayList<>();

    /**
     * TUIKit的初始化函数
     *
     * @param context
     *         应用的上下文，一般为对应应用的ApplicationContext
     * @param sdkAppID
     *         您在腾讯云注册应用时分配的sdkAppID
     * @param configs
     *         TUIKit的相关配置项，一般使用默认即可，需特殊配置参考API文档
     */
    public static void init(Context context, int sdkAppID, TUIKitConfigs configs) {
        TUIKitLog.e(TAG, "init tuikit version: " + BuildConfig.VERSION_NAME);
        sAppContext = context;
        sConfigs = configs;
        if (sConfigs.getGeneralConfig() == null) {
            GeneralConfig generalConfig = new GeneralConfig();
            sConfigs.setGeneralConfig(generalConfig);
        }
        sConfigs.getGeneralConfig().setSDKAppId(sdkAppID);
        String dir = sConfigs.getGeneralConfig().getAppCacheDir();
        if (TextUtils.isEmpty(dir)) {
            TUIKitLog.e(TAG, "appCacheDir is empty, use default dir");
            sConfigs.getGeneralConfig().setAppCacheDir(context.getFilesDir().getPath());
        } else {
            File file = new File(dir);
            if (file.exists()) {
                if (file.isFile()) {
                    TUIKitLog.e(TAG, "appCacheDir is a file, use default dir");
                    sConfigs.getGeneralConfig().setAppCacheDir(context.getFilesDir().getPath());
                } else if (!file.canWrite()) {
                    TUIKitLog.e(TAG, "appCacheDir can not write, use default dir");
                    sConfigs.getGeneralConfig().setAppCacheDir(context.getFilesDir().getPath());
                }
            } else {
                boolean ret = file.mkdirs();
                if (!ret) {
                    TUIKitLog.e(TAG, "appCacheDir is invalid, use default dir");
                    sConfigs.getGeneralConfig().setAppCacheDir(context.getFilesDir().getPath());
                }
            }
        }
        initIM(context, sdkAppID);
        //        TIMOfflinePushSettings settings = new TIMOfflinePushSettings();
        //        //开启离线推送
        //        settings.setEnabled(true);
        //        //设置收到 C2C 离线消息时的提示声音，以把声音文件放在 res/raw 文件夹下为例
        //        settings.setC2cMsgRemindSound(Uri.parse("android.resource://" + sAppContext
        //        .getPackageName() + "/" + R.raw.beep));
        //        //设置收到群离线消息时的提示声音，以把声音文件放在 res/raw 文件夹下为例
        //        settings.setGroupMsgRemindSound(Uri.parse("android.resource://" + sAppContext
        //        .getPackageName() + "/" + R.raw.beep));
        //
        //        TIMManager.getInstance().setOfflinePushSettings(settings);
        BackgroundTasks.initInstance();
        FileUtil.initPath(); // 取决于app什么时候获取到权限，即使在application中初始化，首次安装时，存在获取不到权限，建议app端在activity
        // 中再初始化一次，确保文件目录完整创建
        FaceManager.loadFaceFiles();
    }

    private static void initIM(Context context, int sdkAppID) {
        V2TIMSDKConfig sdkConfig = sConfigs.getSdkConfig();
        if (sdkConfig == null) {
            sdkConfig = new V2TIMSDKConfig();
            sConfigs.setSdkConfig(sdkConfig);
        }
        GeneralConfig generalConfig = sConfigs.getGeneralConfig();
        sdkConfig.setLogLevel(generalConfig.getLogLevel());
        V2TIMManager.getInstance().initSDK(context, sdkAppID, sdkConfig, new V2TIMSDKListener() {
            @Override
            public void onConnecting() {
            }

            @Override
            public void onConnectSuccess() {
                sIMSDKConnected = true;
                for (IMEventListener l : sIMEventListeners) {
                    l.onConnected();
                }
            }

            @Override
            public void onConnectFailed(int code, String error) {
                sIMSDKConnected = false;
                for (IMEventListener l : sIMEventListeners) {
                    l.onDisconnected(code, error);
                }
            }

            @Override
            public void onKickedOffline() {
                for (IMEventListener l : sIMEventListeners) {
                    l.onForceOffline();
                }
                ImHelper.getDBXSendReq().logoutStatus(ImHelper.LOGOUT_REASON_KICK);
                unInit();
            }

            @Override
            public void onUserSigExpired() {
                for (IMEventListener l : sIMEventListeners) {
                    l.onUserSigExpired();
                }
                ImHelper.getDBXSendReq().logoutStatus(ImHelper.LOGOUT_REASON_KICK);
                unInit();
            }
        });

        V2TIMManager.getConversationManager().setConversationListener(new V2TIMConversationListener() {
            @Override
            public void onSyncServerStart() {
                super.onSyncServerStart();
            }

            @Override
            public void onSyncServerFinish() {
                super.onSyncServerFinish();
            }

            @Override
            public void onSyncServerFailed() {
                super.onSyncServerFailed();
            }

            @Override
            public void onNewConversation(List<V2TIMConversation> conversationList) {
                ConversationManagerKit.getInstance().onRefreshConversation(conversationList);
                for (IMEventListener listener : sIMEventListeners) {
                    listener.onRefreshConversation(conversationList);
                }
            }

            @Override
            public void onConversationChanged(List<V2TIMConversation> conversationList) {
                ConversationManagerKit.getInstance().onRefreshConversation(conversationList);
                for (IMEventListener listener : sIMEventListeners) {
                    listener.onRefreshConversation(conversationList);
                }
            }
        });

        V2TIMManager.getInstance().setGroupListener(new V2TIMGroupListener() {
            @Override
            public void onMemberEnter(String groupID, List<V2TIMGroupMemberInfo> memberList) {
                cleanGroupFaceCache(groupID);
                TUIKitLog.i(TAG,
                        "onMemberEnter groupID:" + groupID + ", size:" + memberList.size());
                for (V2TIMGroupMemberInfo v2TIMGroupMemberInfo : memberList) {
                    if (TextUtils.equals(v2TIMGroupMemberInfo.getUserID(),
                            V2TIMManager.getInstance().getLoginUser())) {
                        GroupChatManagerKit.getInstance().notifyJoinGroup(groupID, false);
                        return;
                    }
                }
            }

            @Override
            public void onMemberLeave(String groupID, V2TIMGroupMemberInfo member) {
                cleanGroupFaceCache(groupID);
                TUIKitLog.i(TAG,
                        "onMemberLeave groupID:" + groupID + ", memberID:" + member.getUserID());
            }

            @Override
            public void onMemberInvited(String groupID, V2TIMGroupMemberInfo opUser,
                                        List<V2TIMGroupMemberInfo> memberList) {
                for (V2TIMGroupMemberInfo v2TIMGroupMemberInfo : memberList) {
                    if (v2TIMGroupMemberInfo.getUserID().equals(V2TIMManager.getInstance().getLoginUser())) {
                        GroupChatManagerKit.getInstance().notifyJoinGroup(groupID, true);
                        return;
                    }
                }
            }

            @Override
            public void onMemberKicked(String groupID, V2TIMGroupMemberInfo opUser,
                                       List<V2TIMGroupMemberInfo> memberList) {
                for (V2TIMGroupMemberInfo v2TIMGroupMemberInfo : memberList) {
                    if (v2TIMGroupMemberInfo.getUserID().equals(V2TIMManager.getInstance().getLoginUser())) {
                        GroupChatManagerKit.getInstance().notifyKickedFromGroup(groupID);
                        return;
                    }
                }
            }

            @Override
            public void onMemberInfoChanged(String groupID,
                                            List<V2TIMGroupMemberChangeInfo> v2TIMGroupMemberChangeInfoList) {
            }

            @Override
            public void onGroupCreated(String groupID) {

            }

            @Override
            public void onGroupDismissed(String groupID, V2TIMGroupMemberInfo opUser) {
                GroupChatManagerKit.getInstance().notifyGroupDismissed(groupID);
            }

            @Override
            public void onGroupRecycled(String groupID, V2TIMGroupMemberInfo opUser) {
                GroupChatManagerKit.getInstance().notifyGroupDismissed(groupID);
            }

            @Override
            public void onGroupInfoChanged(String groupID, List<V2TIMGroupChangeInfo> changeInfos) {

            }

            @Override
            public void onReceiveJoinApplication(String groupID, V2TIMGroupMemberInfo member,
                                                 String opReason) {

            }

            @Override
            public void onApplicationProcessed(String groupID, V2TIMGroupMemberInfo opUser,
                                               boolean isAgreeJoin, String opReason) {
                if (!isAgreeJoin) {
                    GroupChatManagerKit.getInstance().notifyJoinGroupRefused(groupID);
                }
            }

            @Override
            public void onGrantAdministrator(String groupID, V2TIMGroupMemberInfo opUser,
                                             List<V2TIMGroupMemberInfo> memberList) {

            }

            @Override
            public void onRevokeAdministrator(String groupID, V2TIMGroupMemberInfo opUser,
                                              List<V2TIMGroupMemberInfo> memberList) {

            }

            @Override
            public void onQuitFromGroup(String groupID) {
                TUIKitLog.i(TAG, "onQuitFromGroup groupID:" + groupID);
            }

            @Override
            public void onReceiveRESTCustomData(String groupID, byte[] customData) {
                GroupChatManagerKit.getInstance().notifyGroupRESTCustomSystemData(groupID,
                        customData);
            }
        });

        V2TIMManager.getFriendshipManager().setFriendListener(new V2TIMFriendshipListener() {
            @Override
            public void onFriendListAdded(List<V2TIMFriendInfo> users) {
                C2CChatManagerKit.getInstance().notifyNewFriend(users);
            }
        });

        V2TIMManager.getMessageManager().addAdvancedMsgListener(new V2TIMAdvancedMsgListener() {
            @Override
            public void onRecvNewMessage(V2TIMMessage msg) {
                for (IMEventListener l : sIMEventListeners) {
                    l.onNewMessage(msg);
                }
                checkMsgInfoAndShowNotification(msg);
            }

            private void checkMsgInfoAndShowNotification(V2TIMMessage timMessage) {
                if (timMessage == null
                        || timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_HAS_DELETED
                        || timMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_NONE) {
                    TUIKitLog.e(TAG, "ele2MessageInfo parameters error");
                    return;
                }
                int type = timMessage.getElemType();
                String senderNickName = timMessage.getNickName();
                String senderId = timMessage.getSender();
                String msgId = timMessage.getMsgID();
                String content = "";
                boolean functionMsg = false;
                switch (type) {
                    case V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM:
                        V2TIMCustomElem customElem = timMessage.getCustomElem();
                        String data = new String(customElem.getData());
                        //                        Logger.e("checkMsgInfoAndShowNotification:" +
                        //                        data);
                        if (data.equals(MessageCustom.BUSINESS_ID_GROUP_CREATE)) {
                        } else {
                            try {
                                Gson mGson = GsonUtils.INSTANCE.getMGson();
                                MessageCustom messageCustom = mGson.fromJson(data,
                                        MessageCustom.class);
                                String msgViewType = messageCustom.getType();
                                if (!TextUtils.isEmpty(msgViewType) && msgViewType.equals(MessageCustom.MessageViewType.FUNCTIONAL)) {
                                    ViewData viewData = mGson.fromJson(data, ViewData.class);
                                    if (timMessage.getSender().startsWith(FunctionConst.ALARM)) {
                                        ImHelper.getDBXSendReq().refreshEventInfo(FunctionConst.ALARM);
                                    }
                                    if (timMessage.getSender().startsWith(FunctionConst.EMERGENCY)) {
                                        ImHelper.getDBXSendReq().refreshEventInfo(FunctionConst.EMERGENCY);
                                    }
                                    if (timMessage.getSender().startsWith(FunctionConst.ENGINE_LOG)) {
                                        long timeStamp = timMessage.getTimestamp();
                                        if (Math.abs(timeStamp - System.currentTimeMillis() / 1000) > 10) {
                                            //收到的消息时间戳与当前时间差1分钟以上的不处理弹出报警逻辑
                                            functionMsg = true;
                                            return;
                                        }
                                        Expand expand = viewData.getExpand();
                                        if (Expand.DATA_TYPE_ALARM_IM_INFO.equals(expand.getDataType())) {
                                            JsonObject json = expand.getData();
                                            AlarmIMInfo alarmIMInfo = mGson.fromJson(json,
                                                    AlarmIMInfo.class);
                                            if (CollectorType.WATER_DEPTH == alarmIMInfo.getCollectorType()
                                                    || CollectorType.WIND_PRESSURE == alarmIMInfo.getCollectorType()
                                                    || CollectorType.WATER_PRESSURE == alarmIMInfo.getCollectorType()) {
                                                //处理水压液位警报时的声音播放
                                                BeepHelper beepHelper =
                                                        new BeepHelper(KtxManager.getCurrentActivity(), com.ymy.core.R.raw.ping2);
                                                beepHelper.initVoice();
                                                beepHelper.playBeepSoundAndVibrate();
                                            }
                                        }
                                    }
                                    if (timMessage.getSender().startsWith(FunctionConst.ALARM_NOTICE)) {
                                        long timeStamp = timMessage.getTimestamp();
                                        if (Math.abs(timeStamp - System.currentTimeMillis() / 1000) > 120) {
                                            //收到的消息时间戳与当前时间差1分钟以上的不处理弹出报警逻辑
                                            functionMsg = true;
                                            return;
                                        }
                                        Expand expand = viewData.getExpand();
                                        if (Expand.DATA_TYPE_ALARM_IM_INFO.equals(expand.getDataType())) {
                                            JsonObject json = expand.getData();
                                            AlarmIMInfo alarmIMInfo = mGson.fromJson(json,
                                                    AlarmIMInfo.class);
                                            if (alarmIMInfo.getUsers().contains(YmyUserManager.INSTANCE.getUser().getUserId())) {
                                                AlarmComeInfo alarmComeInfo =
                                                        new AlarmComeInfo(alarmIMInfo.getTitle(),
                                                                alarmIMInfo.getAddress(),
                                                                alarmIMInfo.getTime(),
                                                                "",
                                                                alarmIMInfo.getLevel(),
                                                                alarmIMInfo.getCollectorType(),
                                                                timMessage.getUserID(),
                                                                alarmIMInfo.getUrl(),
                                                                alarmIMInfo.getSource(),
                                                                alarmIMInfo.getChannel()
                                                        );
                                                ImHelper.getDBXSendReq().showAlarmDialog(YAYLAlarmInfoKt.EVENT_COME_FROM_IM_TYPE_ALARM_V2, alarmComeInfo);
                                            }
                                        }
                                    }
                                    ArrayList<Entry> entrys = viewData.getEntrys();
                                    String desc = "";
                                    if (!entrys.isEmpty()) {
                                        Entry entry = entrys.get(0);
                                        desc = entry.getValue();
                                    }
                                    content = desc;
                                    if (!timMessage.getSender().startsWith(FunctionConst.ALARM_NOTICE)) {
                                        sendToNotification(timMessage, viewData.getTitle(),
                                                senderId, desc);
                                    }
                                    functionMsg = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (data.contains("remoteUrl")) {
                            content = "[语音消息]";
                            break;
                        }
                        if (StringUtils.isEmpty(content)) {
                            content = "[系统消息]";
                        }
                        break;
                    case V2TIMMessage.V2TIM_ELEM_TYPE_TEXT:
                        V2TIMTextElem txtEle = timMessage.getTextElem();
                        content = txtEle.getText();
                        break;
                    case V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE:
                        content = "[图片]";
                        break;
                    case V2TIMMessage.V2TIM_ELEM_TYPE_SOUND:
                        content = "[语音消息]";
                        break;
                    case V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO:
                        content = "[视频]";
                        break;
                    case V2TIMMessage.V2TIM_ELEM_TYPE_FILE:
                        content = "[文件]";
                        break;
                    default:
                        break;
                }
                Logger.e("sendToNotification1:" + ImHelper.needShowNotification);
                Logger.e("sendToNotification2:" + content);
                if (!functionMsg) {
                    sendToNotification(timMessage, senderNickName, senderId, content);
                }
            }

            @Override
            public void onRecvC2CReadReceipt(List<V2TIMMessageReceipt> receiptList) {
                C2CChatManagerKit.getInstance().onReadReport(receiptList);
            }

            @Override
            public void onRecvMessageRevoked(String msgID) {
                super.onRecvMessageRevoked(msgID);
            }
        });
        V2TIMManager.getMessageManager().addAdvancedMsgListener(MessageRevokedManager.getInstance());
    }

    public static void unInit() {
        ConversationManagerKit.getInstance().destroyConversation();
        //        if (!TUIKitConfigs.getConfigs().getGeneralConfig().isSupportAVCall()) {
        //            return;
        //        }
        //        AVCallManager.getInstance().unInit();
    }

    public static void cleanGroupFaceCache(String groupId) {
        String targetId = "group_" + groupId;
        String savedIcon =
                ConversationManagerKit.getInstance().getGroupConversationAvatar(targetId);
        if (!savedIcon.isEmpty()) {
            File cacheIconFile = new File(savedIcon);
            if (cacheIconFile.exists()) {
                cacheIconFile.delete();
            }
            ConversationManagerKit.getInstance().setGroupConversationAvatar(targetId, "");
        }
    }

    private static void sendToNotification(V2TIMMessage timMessage, String senderNickName,
                                           String senderId, String content) {
        long timeStamp = timMessage.getTimestamp();
        if (Math.abs(timeStamp - System.currentTimeMillis() / 1000) > 10) {
            //            收到的消息时间戳与当前时间差1分钟以上的不处理通知栏弹窗
            return;
        }
        String groupID = timMessage.getGroupID();
        if (groupID != null) {
            ArrayList<String> groupIDs = new ArrayList<>();
            groupIDs.add(groupID);
            final String finalContent = senderNickName + ":" + content;
            V2TIMManager.getGroupManager().getGroupsInfo(groupIDs,
                    new V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
                        @Override
                        public void onSuccess(List<V2TIMGroupInfoResult> v2TIMGroupInfoResults) {
                            if (v2TIMGroupInfoResults.size() > 0) {
                                V2TIMGroupInfoResult v2TIMGroupInfoResult =
                                        v2TIMGroupInfoResults.get(0);
                                V2TIMGroupInfo groupInfo = v2TIMGroupInfoResult.getGroupInfo();
                                String groupName = groupInfo.getGroupName();
                                String groupId = groupInfo.getGroupID();
                                ImHelper.DBXSendReq dbxSendReq = ImHelper.getDBXSendReq();
                                if (dbxSendReq != null) {
                                    dbxSendReq.showNotification(groupName, finalContent, null,
                                            groupId, 0, V2TIMConversation.V2TIM_GROUP);
                                }
                            }
                        }

                        @Override
                        public void onError(int code, String desc) {

                        }
                    });
        } else {
            ImHelper.DBXSendReq dbxSendReq = ImHelper.getDBXSendReq();
            if (dbxSendReq != null) {
                dbxSendReq.showNotification(senderNickName, content, null, senderId, 0,
                        V2TIMConversation.V2TIM_C2C);
            }
        }
    }

    public static void login(final String userid, final String usersig,
                             final IUIKitCallBack callback) {
        TUIKitConfigs.getConfigs().getGeneralConfig().setUserId(userid);
        TUIKitConfigs.getConfigs().getGeneralConfig().setUserSig(usersig);
        V2TIMManager.getInstance().login(userid, usersig, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                //                if (TUIKitConfigs.getConfigs().getGeneralConfig()
                //                .isSupportAVCall()) {
                //                    UserModel self = new UserModel();
                //                    self.userId = userid;
                //                    self.userSig = usersig;
                //                    ProfileManager.getInstance().setUserModel(self);
                //                    AVCallManager.getInstance().init(sAppContext);
                //                }
                callback.onSuccess(null);
            }

            @Override
            public void onError(int code, String desc) {
                callback.onError(TAG, code, desc);
            }
        });
    }

    public static void logout(final IUIKitCallBack callback) {
        V2TIMManager.getInstance().logout(new V2TIMCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess(null);
                if (!TUIKitConfigs.getConfigs().getGeneralConfig().isSupportAVCall()) {
                    return;
                }
                Intent intent = new Intent(sAppContext, AVCallManager.class);
                sAppContext.stopService(intent);
            }

            @Override
            public void onError(int code, String desc) {
                callback.onError(TAG, code, desc);
            }
        });
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public static TUIKitConfigs getConfigs() {
        if (sConfigs == null) {
            sConfigs = TUIKitConfigs.getConfigs();
        }
        return sConfigs;
    }

    public static void addIMEventListener(IMEventListener listener) {
        TUIKitLog.i(TAG, "addIMEventListener:" + sIMEventListeners.size() + "|l:" + listener);
        if (listener != null && !sIMEventListeners.contains(listener)) {
            sIMEventListeners.add(listener);
        }
    }

    public static void removeIMEventListener(IMEventListener listener) {
        TUIKitLog.i(TAG, "removeIMEventListener:" + sIMEventListeners.size() + "|l:" + listener);
        if (listener == null) {
            sIMEventListeners.clear();
        } else {
            sIMEventListeners.remove(listener);
        }
    }
}
