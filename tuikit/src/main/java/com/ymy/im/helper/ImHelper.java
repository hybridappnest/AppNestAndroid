package com.ymy.im.helper;

import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.SelectionActivity;
import com.tencent.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.input.FunctionBtnData;
import com.tencent.qcloud.tim.uikit.modules.contact.ContactItemBean;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationProvider;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfoProvider;
import com.tencent.qcloud.tim.uikit.modules.group.member.GroupMemberInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.tencent.qcloud.tim.uikit.modules.message.MessageUpload;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.Ktx;
import com.ymy.core.user.UserInfoDB;
import com.ymy.core.user.YmyUserManager;
import com.ymy.core.utils.StringUtils;
import com.ymy.core.utils.ToastUtils;
import com.ymy.im.activity.ImChatActivity;
import com.ymy.im.activity.SelectSessionActivity;
import com.ymy.im.helper.type.EventType;
import com.ymy.im.net.IMHttpManager;
import com.ymy.im.signature.GenerateTestUserSig;
import com.ymy.im.utils.Constants;
import com.ymy.im.utils.DemoLog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImHelper {


    public static final int FAIL = 0;
    public static final int SUCCESS = 1;
    public static final ArrayList<String> EventTypeList;
    public static final int LOGOUT_REASON_USER = 1001;
    public static final int LOGOUT_REASON_KICK = 1002;
    public static String TAG = "ImHelper_LOG";
    public static DBXSendReq mSendReq;
    public static String eventId = "";
    public static String groupId = "";
    public static String currentChatId = "";
    public static String groupName = "";
    public static String event_type = "";
    public static String mCompanyId = "";
    public static ChatInfo mChatInfo;
    public static boolean needShowNotification = true;
    public static WeakReference<SelectSessionActivity.SelectSessionCallBack> mSelectSessionCallBack;
    /**
     * ??????????????????????????????
     */
    public static int CREATE_ACTION_C2C = 1;
    public static int CREATE_ACTION_GROUP = 3;
    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????false
     */
    public static boolean currentGroupIsExist = true;
    //    public static int retryCount = 0;
    static boolean mCreating;
    private static UIListener mUIListener;
    /**
     * ???????????????????????????????????????
     */
    private static DBXSendReq emptyDBXSendReq = new DBXSendReq() {

        @Override
        public UserInfoDB getUserInfo() {
            return null;
        }

        @Override
        public void goToWebActivity(@NotNull String url) {

        }

        /**
         * ????????????
         *
         * @param chatType
         *         ????????????
         * @param oldMemberIds
         *         ????????????ids
         */
        @Override
        public void goGetPerson(@NotNull int chatType, ArrayList<String> oldMemberIds) {

        }

        @Override
        public void logoutStatus(@NotNull int logoutReason) {

        }

        @Override
        public void loginStatus(@NotNull int status) {

        }

        @Override
        public void refreshMsgTabFragment() {

        }

        /**
         * ?????????????????????
         *
         * @param unReadNum
         */
        @Override
        public void getUnreadNum(@NotNull int unReadNum) {

        }

        /**
         * ????????????
         *
         * @param type
         * @param json
         *         ????????????dialog?????????json
         */
        @Override
        public void showAlarmDialog(@NotNull String type, @NotNull Object json) {

        }

        /**
         * ??????????????????
         *
         * @param data
         *         ????????????dialog?????????List<MessageUpload>
         */
        @Override
        public void showHandover(@NotNull List<MessageUpload> data) {

        }

        /**
         * ??????????????????
         *
         * @param title
         * @param content
         * @param avator
         * @param senderId
         * @param msgId
         * @param type
         */
        @Override
        public void showNotification(@NotNull String title, @NotNull String content,
                                     String avator, String senderId, int msgId, int type) {

        }

        /**
         * ??????????????????????????????bar??????
         *
         * @param chatId
         */
        @Override
        public void getBottomFunctionBarData(@NotNull String chatId) {

        }

        /**
         * ???????????????????????????
         */
        @Override
        public void getCompanyFunctionData() {

        }

        /**
         * ??????????????????
         *
         * @param emergency
         */
        @Override
        public void refreshEventInfo(String emergency) {

        }

        /**
         * ???????????????
         */
        @Override
        public void startScanning() {

        }

        /**
         * ????????????
         */
        @Override
        public void switchCompany() {

        }

        /**
         * ??????????????????
         */
        @Override
        public void goToCompanyMap() {

        }

        @Override
        public void goToCameraActivity(Context context, int cameraType, IUIKitCallBack callBack) {

        }

        /**
         * ????????????????????????????????????
         *
         * @param id
         * @return
         */
        @Override
        public boolean checkYJYAIMLayout(String id) {
            return false;
        }

        /**
         * ????????????????????????
         */
        @Override
        public void showYJYATasks() {

        }
    };

    static {
        EventTypeList = new ArrayList();
        EventTypeList.add(EventType.TYPE_ALARM);
        EventTypeList.add(EventType.TYPE_WORKORDER);
        EventTypeList.add(EventType.TYPE_XJ);
        EventTypeList.add(EventType.TYPE_YHPC);
        EventTypeList.add(EventType.TYPE_YAYL);
        EventTypeList.add(EventType.TYPE_SJPX);
        EventTypeList.add(EventType.TYPE_ZKJJ);
    }

    /**
     * ??????
     *
     * @param userId
     *         ??????userId
     */
    public static void login(final String userId, final LoginCallBack callBack) {
        //        TabMsgFragment.allList = null;

        ConversationProvider provider = ConversationManagerKit.getInstance().getProvider();
        if (provider != null) {
            provider.clear();
        }

        String loginUser = V2TIMManager.getInstance().getLoginUser();
        if (loginUser != null && loginUser.equals(userId)) {
            if (callBack != null) {
                callBack.success();
            }
            if (mSendReq != null) {
                mSendReq.loginStatus(SUCCESS);
            }
            return;
        }
        //        retryCount = 0;
        doGetSignRequest(userId, callBack);
        //        loginByCacheSign(userId, "eJw1jtEKwiAYRt/F22L9MxEVumhBQRRRLuh2TI0/K8xkjKJ3b2x1
        //        +Z2PA+dNyo3ObBswWqI4MAEw7lljI1GEZkCG/TS+CgENUTkDYCC5FMODxt4TOuyFHIDzv4LnjjyuJ
        //        +qW+qgxluvp/jVv/cRtdVUscBebgKuiGl0S1gfjZz8x4a2ryTkFyqkQ8vMFvjwwyA==", callBack);
    }

    private static void doGetSignRequest(final String userId, final LoginCallBack callBack) {
        String userSig = GenerateTestUserSig.genTestUserSig(userId);
        Logger.i("imLogin onSuccess loginName = " + userId + " userSig:" + userSig);
        doLoginIM(userId, userSig, callBack);
        //        retryCount++;
//        try {
//            IMHttpManager.getIMSign(new IMHttpManager.CallBack() {
//                @Override
//                public void callSuccess(@Nullable Object any) {
//                    if (any instanceof String) {
//                        String userSig = any.toString();
//                        DemoLog.i(TAG, "imLogin   loginName= " + userId);
//                        Logger.i("imLogin onSuccess loginName = " + userId + " userSig:" + userSig);
//                        doLoginIM(userId, userSig, callBack);
//                    }
//                }
//
//                @Override
//                public void callError() {
//                    //                    if (retryCount < 2) {
//                    //                        doGetSignRequest(userId, callBack);
//                    //                    } else {
//                    //                        if (mSendReq != null) {
//                    //                            mSendReq.loginStatus(FAIL);
//                    //                        }
//                    //                        if (callBack != null) {
//                    //                            callBack.error("??????sign????????????");
//                    //                        }
//                    //                        doLoginIM(userId,
//                    //                        "eJw1jtEKwiAYRt/F22L9MxEVumhBQRRRLuh2TI0
//                    //                        /K8xkjKJ3b2x1+Z2PA+dNyo3ObBswWqI4MAEw7lljI1GEZkCG
//                    //                        /TS+CgENUTkDYCC5FMODxt4TOuyFHIDzv4LnjjyuJ+qW
//                    //                        +qgxluvp/jVv/cRtdVUscBebgKuiGl0S1gfjZz8x4a2ryTkFyqkQ8vMFvjwwyA==", callBack);
//                    //                    }
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private static void doLoginIM(final String userId, String userSig,
                                  final LoginCallBack callBack) {
        TUIKit.login(userId, userSig, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if (mSendReq != null) {
                    mSendReq.loginStatus(SUCCESS);
                }
                if (callBack != null) {
                    callBack.success();
                }
//                DemoLog.i(TAG, "imLogin onSuccess loginName = " + userId + " backInfo" + data);
                Logger.i("imLogin onSuccess loginName = " + userId + " backInfo" + data);
            }

            @Override
            public void onError(String module, final int code, final String desc) {
//                DemoLog.i(TAG, "???????????? imLogin errorCode = " + code + ", errorInfo = " + desc);
                Logger.i("???????????? imLogin errorCode = " + code + ", errorInfo = " + desc);
                //                                if (mSendReq != null) {
                //                                    mSendReq.loginStatus(FAIL);
                //                                }
                if (callBack != null) {
                    callBack.error(desc);
                }
            }
        });
    }

    private static void loginByCacheSign(final String userId, String userSig,
                                         final LoginCallBack callBack) {
        doLoginIM(userSig, userId, callBack);
    }

    /**
     * ????????????
     */
    public static void logOut() {
        Logger.e("im logout");
        TUIKit.logout(new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                DemoLog.i(TAG, "????????????onSuccess ");
                Logger.e("????????????onSuccess");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                DemoLog.i(TAG, "???????????? onError");
                Logger.e("????????????onError");
            }
        });
    }

    /**
     * ????????????????????????
     *
     * @param nickName
     *         ?????????????????????????????????
     * @param avatar
     *         ?????????????????????????????????
     */
    public static void userInfoChange(final String nickName, final String avatar) {
        //        final V2TIMUserFullInfo v2TIMUserFullInfo = new V2TIMUserFullInfo();
        //        // ??????
        //        if (!TextUtils.isEmpty(avatar)) {
        //            v2TIMUserFullInfo.setFaceUrl(avatar);
        //        }
        //        // ??????
        //        if (!TextUtils.isEmpty(nickName)) {
        //            v2TIMUserFullInfo.setNickname(nickName);
        //        }
        //        V2TIMManager.getInstance().setSelfInfo(v2TIMUserFullInfo, new V2TIMCallback() {
        //            @Override
        //            public void onError(int code, String desc) {
        //                DemoLog.e(TAG, "modifySelfProfile err code = " + code + ", desc = " +
        //                desc);
        //            }
        //
        //            @Override
        //            public void onSuccess() {
        //                DemoLog.i(TAG, "modifySelfProfile success");
        //                if (!TextUtils.isEmpty(avatar)) {
        //                    TUIKitConfigs.getConfigs().getGeneralConfig().setUserFaceUrl(avatar);
        //                }
        //                if (!TextUtils.isEmpty(nickName)) {
        //                    TUIKitConfigs.getConfigs().getGeneralConfig().setUserNickname
        //                    (nickName);
        //                }
        //
        //            }
        //        });
    }

    /**
     * ??????IM??????????????????
     *
     * @return
     */
    public static String getLoginUserId() {
        return V2TIMManager.getInstance().getLoginUser();
    }

    public static void setUIListener(UIListener uiListener) {
        mUIListener = uiListener;
    }

    public static void setGroupName(String groupname) {
        groupName = groupname;
    }

    public static void goGetPerson(ChatInfo chatInfo, ArrayList<String> oldMemberIds) {
        mChatInfo = chatInfo;
        if (getDBXSendReq() != null) {
            getDBXSendReq().goGetPerson(chatInfo.getType(), oldMemberIds);
        }
    }

    public static DBXSendReq getDBXSendReq() {
        return mSendReq != null ? mSendReq : emptyDBXSendReq;
    }

    /**
     * ????????????
     *
     * @param sendReq
     */
    public static void setDBXSendReq(DBXSendReq sendReq) {
        mSendReq = sendReq;
    }

    public static void getPersonCallBack(ArrayList<GroupMemberInfo> memberInfos,
                                         GetPersonResultListener getPersonResultListener) {
        dealContact(memberInfos, mChatInfo, getPersonResultListener);
    }

    /**
     * ?????????????????????
     * chat ?????????????????????
     * type = chat ??????type
     * type = 1, ???????????????
     * type = 2??????????????????
     * type = 3???????????????
     * mMembers  t??????????????????  id???chatName ???
     */
    public static void dealContact(List<GroupMemberInfo> mMembers, final ChatInfo chat,
                                   final GetPersonResultListener callback) {
        if (chat == null) {
            if (callback != null) {
                callback.onResult(false);
            }
        }
        int type = chat.getType();
        if (type == CREATE_ACTION_C2C) {//1, ????????????
            ChatInfo chatInfo = new ChatInfo();
            if (mMembers != null && mMembers.size() > 0) {
                chatInfo.setId(mMembers.get(0).getAccount());
                chatInfo.setChatName(mMembers.get(0).getUserName());
            }
            chatInfo.setType(V2TIMConversation.V2TIM_C2C);
            if (mSelectSessionCallBack != null && mSelectSessionCallBack.get() != null) {
                mSelectSessionCallBack.get().selectSession(chatInfo);
            } else {
                Intent intent = new Intent(TUIKit.getAppContext(), ImChatActivity.class);
                intent.putExtra(Constants.CHAT_INFO, chatInfo);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                TUIKit.getAppContext().startActivity(intent);
            }
            if (callback != null) {
                callback.onResult(true);
            }
            return;
        }
        if (type == CREATE_ACTION_GROUP) {//1, ????????????
            if (mCreating) {
                if (callback != null) {
                    callback.onResult(false);
                }
                return;
            }
            final GroupInfo groupInfo = new GroupInfo();
            String groupName = YmyUserManager.INSTANCE.getUser().getNickname();
            if (groupName.isEmpty()) {
                groupName = YmyUserManager.INSTANCE.getUser().getRealName();
            }
            for (int i = 0; i < mMembers.size(); i++) {
                String name = StringUtils.isEmpty(mMembers.get(i).getUserName()) ?
                        mMembers.get(i).getNameCard() : mMembers.get(i).getUserName();
                if (StringUtils.isEmpty(name)) {
                    name = mMembers.get(i).getAccount();
                }
                groupName = groupName + "???" + name;
            }
            if (SelectionActivity.getStrLength(groupName) > 30) {
                groupName = SelectionActivity.truncate(groupName, 30);
            }
            groupInfo.setChatName(groupName);
            groupInfo.setGroupName(groupName);
            groupInfo.setMemberDetails(mMembers);
            groupInfo.setGroupType("Public");
            groupInfo.setJoinType(2);

            mCreating = true;
            GroupChatManagerKit.createGroupChat(groupInfo, new IUIKitCallBack() {
                @Override
                public void onSuccess(Object data) {
                    mCreating = false;
                    if (callback != null) {
                        callback.onResult(true);
                    }
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setType(V2TIMConversation.V2TIM_GROUP);
                    chatInfo.setId(data.toString());
                    chatInfo.setChatName(groupInfo.getGroupName());
                    if (mSelectSessionCallBack != null && mSelectSessionCallBack.get() != null) {
                        mSelectSessionCallBack.get().selectSession(chatInfo);
                    } else {
                        Intent intent = new Intent(TUIKit.getAppContext(), ImChatActivity.class);
                        intent.putExtra(Constants.CHAT_INFO, chatInfo);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        TUIKit.getAppContext().startActivity(intent);
                    }
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    mCreating = false;
                    ToastUtils.showToast(Ktx.app, "??????????????????");
                    if (callback != null) {
                        callback.onResult(false);
                    }
                    //                    ToastUtil.toastLongMessage("createGroupChat fail:" +
                    //                    errCode + "=" + errMsg);
                }
            });
            return;
        }
        //  ???????????????
        String ids = "";
        for (GroupMemberInfo memberInfo : mMembers) {
            if (StringUtils.isEmpty(ids)) {
                ids = memberInfo.getAccount();
            } else {
                ids += "," + memberInfo.getAccount();
            }
        }

        IMHttpManager.addGroupMember(chat.getId(), ids, new IMHttpManager.CallBack() {
            @Override
            public void callSuccess(@Nullable Object any) {
                //                ToastUtil.toastLongMessage("??????????????????");
                if (callback != null) {
                    callback.onResult(true);
                }
            }

            @Override
            public void callError() {
                if (callback != null) {
                    callback.onResult(false);
                }
            }
        });
    }

    //????????????????????????
    public static void closeCurrentEventChat() {

    }

    /**
     * ????????????????????????
     *
     * @param listener
     * @return
     */
    public static void getUnreadNum(final MsgListener listener) {
        ConversationManagerKit.getInstance().loadConversation(new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                try {
                    listener.unReaderNum(ConversationManagerKit.getInstance().getUnreadTotal());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ToastUtil.toastLongMessage("??????????????????");
            }
        });
    }

    /**
     * ???????????????????????????????????????
     *
     * @param listener
     */
    public static void getHasAlarmOrYAYlGroup(final GroupListener listener) {
        V2TIMManager.getGroupManager().getJoinedGroupList(new V2TIMValueCallback<List<V2TIMGroupInfo>>() {
            @Override
            public void onSuccess(List<V2TIMGroupInfo> v2TIMGroupInfos) {
                TUIKitLog.i(TAG, "getGroupList success: " + v2TIMGroupInfos.size());
                if (v2TIMGroupInfos.size() == 0) {
                    TUIKitLog.i(TAG, "getGroupList success but no data");
                }
                List<String> list = new ArrayList<>();
                for (V2TIMGroupInfo info : v2TIMGroupInfos) {
                    ContactItemBean bean = new ContactItemBean();
                    list.add(info.getGroupID());
                }

                V2TIMManager.getGroupManager().getGroupsInfo(list,
                        new V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
                            @Override
                            public void onSuccess(List<V2TIMGroupInfoResult> v2TIMGroupInfoResults) {
                                for (int i = 0; i < v2TIMGroupInfoResults.size(); ++i) {
                                    V2TIMGroupInfo groupInfo =
                                            v2TIMGroupInfoResults.get(i).getGroupInfo();
                                    Map<String, byte[]> customInfo = groupInfo.getCustomInfo();
                                    if (customInfo.containsKey("event_group_type")) {
                                        String value = new String(customInfo.get(
                                                "event_group_type"));
                                        if (EventType.TYPE_ALARM.equals(value) || EventType.TYPE_YAYL.equals(value)) {
                                            listener.checkHasAlarmOrYAYlGroup(true);
                                            return;
                                        }
                                    }
                                }
                                listener.checkHasAlarmOrYAYlGroup(false);
                            }

                            @Override
                            public void onError(int code, String desc) {

                            }
                        });
            }

            @Override
            public void onError(int code, String desc) {
                TUIKitLog.e(TAG, "getGroupList err code = " + code + ", desc = " + desc);
            }
        });
    }

    /**
     * ?????????????????????titleEventNoticeBar
     *
     * @param status
     * @param level
     */
    public static void setTitleData(@EventType String eventType, String eventId, int status,
                                    int level, String yaylStartTime, long yaylTime) {
        if (mUIListener != null) {
            mUIListener.setEventTitleData(eventType, status, level, yaylStartTime, yaylTime);
        }
    }

    /**
     * ?????????????????????
     * <p>
     * type = V2TIMConversation.V2TIM_C2C   ??????
     * type = V2TIMConversation.V2TIM_GROUP   ??????
     * id :???ID????????? ????????????id
     * chatName :?????????????????? ??????????????????
     */
    public static void startGroupConversation(String id, String chatName,
                                              JoinGroupListener callBack) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(V2TIMConversation.V2TIM_GROUP);
        chatInfo.setId(id);
        chatInfo.setChatName(chatName);
        loadGroupInfo(chatInfo, callBack);
    }

    /**
     * ???????????????
     *
     * @param chatInfo
     */
    private static void loadGroupInfo(final ChatInfo chatInfo, final JoinGroupListener callBack) {
        new GroupInfoProvider().loadGroupInfo(chatInfo.getId(), new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if (callBack != null) {
                    callBack.onResult(true);
                }
                GroupInfo groupInfo = (GroupInfo) data;
                chatInfo.setChatName(groupInfo.getChatName());
                setConversation(chatInfo);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if (callBack != null) {
                    callBack.onResult(false);
                } else {
                    ToastUtil.toastShortMessage("?????????????????????????????????");
                }
                //                setConversation(chatInfo);
            }
        });
    }

    private static void setConversation(ChatInfo chatInfo) {
        Intent intent = new Intent(TUIKit.getAppContext(), ImChatActivity.class);
        intent.putExtra(Constants.CHAT_INFO, chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TUIKit.getAppContext().startActivity(intent);
    }

    /**
     * ?????????????????????
     *
     * @param id
     */
    public static void startC2CConversation(final String id) {
        ArrayList<String> userIDList = new ArrayList<>();
        userIDList.add(id);
        V2TIMManager.getInstance().getUsersInfo(userIDList,
                new V2TIMValueCallback<List<V2TIMUserFullInfo>>() {
                    @Override
                    public void onSuccess(List<V2TIMUserFullInfo> v2TIMUserFullInfos) {
                        if (v2TIMUserFullInfos.size() > 0) {
                            V2TIMUserFullInfo v2TIMFriendInfoResult = v2TIMUserFullInfos.get(0);
                            Logger.e("v2TIMFriendInfoResult:" + v2TIMFriendInfoResult.toString());
                            String userID = v2TIMFriendInfoResult.getUserID();
                            String nickName = v2TIMFriendInfoResult.getNickName();
                            ChatInfo chatInfo = new ChatInfo();
                            chatInfo.setType(V2TIMConversation.V2TIM_C2C);
                            chatInfo.setId(userID);
                            chatInfo.setChatName(nickName);
                            setConversation(chatInfo);
                        }

                    }

                    @Override
                    public void onError(int code, String desc) {

                    }
                });
        //        V2TIMManagerImpl.getFriendshipManager().getFriendsInfo(userIDList, new
        //        V2TIMValueCallback<List<V2TIMFriendInfoResult>>() {
        //            @Override
        //            public void onError(int code, String desc) {
        //                ToastUtil.toastShortMessage("????????????????????????");
        //            }
        //
        //            @Override
        //            public void onSuccess(List<V2TIMFriendInfoResult> v2TIMFriendInfoResults) {
        //                if (v2TIMFriendInfoResults.size() > 0) {
        //                    V2TIMFriendInfoResult v2TIMFriendInfoResult =
        //                    v2TIMFriendInfoResults.get(0);
        //                    V2TIMFriendInfo friendInfo = v2TIMFriendInfoResult.getFriendInfo();
        //                    V2TIMUserFullInfo userProfile = friendInfo.getUserProfile();
        //                    Logger.e("userProfile:" + userProfile.toString());
        //                    String userID = userProfile.getUserID();
        //                    String nickName = userProfile.getNickName();
        //                    ChatInfo chatInfo = new ChatInfo();
        //                    chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        //                    chatInfo.setId(userID);
        //                    chatInfo.setChatName(nickName);
        //                    setConversation(chatInfo);
        //                }
        //            }
        //        });

    }

    /**
     * ?????????????????????
     * type : ??????
     * state:????????????
     * GetPersonResultListener  ????????????????????????
     *
     * @return
     */
    public static void changeGroupChatState(@EventType final String type, final boolean state) {

        //        V2TIMManager.getGroupManager().getJoinedGroupList(new
        //        V2TIMValueCallback<List<V2TIMGroupInfo>>() {
        //            @Override
        //            public void onError(int code, String desc) {
        //                TUIKitLog.e(TAG, "getGroupList err code = " + code + ", desc = " + desc);
        //            }
        //
        //            @Override
        //            public void onSuccess(List<V2TIMGroupInfo> v2TIMGroupInfos) {
        //                TUIKitLog.i(TAG, "getGroupList success: " + v2TIMGroupInfos.size());
        //                if (v2TIMGroupInfos.size() == 0) {
        //                    TUIKitLog.i(TAG, "getGroupList success but no data");
        //                }
        //                List<String> list = new ArrayList<>();
        //                for (V2TIMGroupInfo info : v2TIMGroupInfos) {
        //                    ContactItemBean bean = new ContactItemBean();
        //                    list.add(info.getGroupID());
        //                }
        //                final HashMap<String, String> hp = new HashMap<>();
        //
        //                V2TIMManager.getGroupManager().getGroupsInfo(list, new
        //                V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
        //                    @Override
        //                    public void onSuccess(List<V2TIMGroupInfoResult>
        //                    v2TIMGroupInfoResults) {
        //                        for (int i = 0; i < v2TIMGroupInfoResults.size(); ++i) {
        //                            V2TIMGroupInfo groupInfo = v2TIMGroupInfoResults.get(i)
        //                            .getGroupInfo();
        //                            Map<String, byte[]> customInfo = groupInfo.getCustomInfo();
        //                            if (customInfo.containsKey("event_group_type")) {
        //                                String value = new String(customInfo.get
        //                                ("event_group_type"));
        //                                if (!StringUtils.isEmpty(type) && type.equals(value)) {
        //                                    V2TIMManager.getGroupManager().setReceiveMessageOpt
        //                                    (groupInfo.getGroupID(), state ? V2TIMGroupInfo
        //                                    .V2TIM_GROUP_RECEIVE_MESSAGE : V2TIMGroupInfo
        //                                    .V2TIM_GROUP_RECEIVE_NOT_NOTIFY_MESSAGE, null);
        //                                }
        //                            }
        //                        }
        //                    }
        //
        //                    @Override
        //                    public void onError(int code, String desc) {
        //
        //                    }
        //                });
        //            }
        //        });
    }

    public static void startC2CContact(GroupMemberInfo info) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setId(info.getAccount());
        chatInfo.setChatName(info.getUserName());
        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        Intent intent = new Intent(TUIKit.getAppContext(), ImChatActivity.class);
        intent.putExtra(Constants.CHAT_INFO, chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TUIKit.getAppContext().startActivity(intent);
    }

    /**
     * ????????????????????????EventType??????????????????
     */
    public static void checkGroupEventTypeAndGetInfo() {
        //        if (StringUtils.isEmpty(groupId)) {
        //            return;
        //        }
        //        final GroupInfo groupInfo = new GroupInfo();
        //        groupInfo.setId(groupId);
        //        GroupInfoProvider.getGroupCustom(groupInfo, new IUIKitCallBack() {
        //            @Override
        //            public void onSuccess(Object data) {
        //                String eventType = groupInfo.getEvent_group_type();
        //                if (StringUtils.isNotEmpty(eventType) && !event_type.equals(eventType)) {
        //                    setEventInfo(eventType, groupId, mCompanyId);
        //                } else {
        //                    ImHelper.getDBXSendReq().getEventInfo(eventId);
        //                }
        //            }
        //
        //            @Override
        //            public void onError(String module, int errCode, String errMsg) {
        //                ImHelper.getDBXSendReq().getEventInfo(eventId);
        //            }
        //        });
    }

    /**
     * ????????????????????????IM
     */
    public static void forwardMessage(Context context, String data) {
        List<MessageInfo> mDataSource = new ArrayList<>();
        MessageInfo messageInfo = MessageInfoUtil.buildCustomMessage(data);
        mDataSource.add(messageInfo);
        SelectSessionActivity.invoke(context, mDataSource);
    }

    /**
     * IM????????????????????????????????????????????????????????????????????????IM????????????????????????
     *
     * @param changeEventType
     */
    public static void changeGroupEventTypeAndGetInfo(String changeEventType) {
        setEventInfo(changeEventType, groupId, mCompanyId);
    }

    public static void setEventInfo(String eventType, String groupid, String companyId) {
        event_type = eventType;
        groupId = groupid;
        mCompanyId = companyId;
        int i = groupId.lastIndexOf("_");
        eventId = groupId.subSequence(i + 1, groupId.length()).toString();
    }

    public static void refreshFunctionLayout() {
        if (mUIListener != null) {
            mUIListener.refreshFunctionLayout();
        }
    }

    public static void refreshBottomFunctionBar(ArrayList<FunctionBtnData> data) {
        if (mUIListener != null) {
            mUIListener.refreshBottomFunctionBar(data);
        }
    }

    public static void setSelectSessionCallBack(SelectSessionActivity.SelectSessionCallBack callBack) {
        if (callBack == null) {
            mSelectSessionCallBack.clear();
        } else {
            mSelectSessionCallBack =
                    new WeakReference<SelectSessionActivity.SelectSessionCallBack>(callBack);
        }
    }

    public interface LoginCallBack {
        void success();

        void error(String str);
    }

    public interface GroupListener {
        void checkHasAlarmOrYAYlGroup(boolean has);
    }

    public interface YAYLGroupListener {
        void checkHasYAYlGroup(boolean has, String groupid);
    }

    public interface GetPersonResultListener {
        void onResult(boolean result);
    }

    public interface JoinGroupListener {
        void onResult(boolean result);
    }

    /**
     * ????????????????????????????????????
     */
    public interface UIListener {
        void setEventTitleData(String eventType, int status, int level, String yaylStartTime,
                               long yaylTime);

        void refreshFunctionLayout();

        void refreshBottomFunctionBar(ArrayList<FunctionBtnData> data);
    }

    /**
     * ???????????????
     */
    public interface MsgListener {
        void unReaderNum(int unReaderNum);
    }

    public interface DBXSendReq {

        UserInfoDB getUserInfo();//????????????userId

        //        void goToEventWebActivity(@WebViewType String urlAction, String params);//??????web???

        void goToWebActivity(@NotNull String url);

        /**
         * ????????????
         *
         * @param chatType
         *         ????????????
         * @param oldMemberIds
         *         ????????????ids
         */
        void goGetPerson(@NotNull int chatType, ArrayList<String> oldMemberIds);
        //??????contactActivity,  chatInfo ??? type:1, ???????????????2?????????????????????3????????????

        void logoutStatus(@NotNull int logoutReason);//????????????,??????????????????

        void loginStatus(@NotNull int status);//????????????

        void refreshMsgTabFragment();//

        /**
         * ?????????????????????
         *
         * @param unReadNum
         */
        void getUnreadNum(@NotNull int unReadNum);//

        /**
         * ????????????????????????
         *
         * @param alarmId ??????id
         */
        //        void sendHandleEventAction(@NotNull String alarmId);

        /**
         * ???????????????
         *
         * @param eventId ??????id
         */
        //        void eventToWorkOrder(@NotNull String eventId);

        /**
         * ???????????????????????????
         *
         * @param alarmId ??????id
         */
        //        void getEventInfo(@NotNull String alarmId);

        /**
         * ????????????
         *
         * @param json
         *         ????????????dialog?????????json
         */
        void showAlarmDialog(@NotNull String type, @NotNull Object json);

        /**
         * ??????????????????
         *
         * @param data
         *         ????????????dialog?????????List<MessageUpload>
         */
        void showHandover(@NotNull List<MessageUpload> data);

        /**
         * @return
         */
        //        boolean isUserRoleManager();

        /**
         * ??????
         *
         * @param eventId
         */
        //        void finishYAYL(String eventId);

        /**
         * ??????????????????
         *
         * @param title
         * @param content
         * @param avator
         */
        void showNotification(@NotNull String title, @NotNull String content, String avator,
                              String senderId, int msgId, int type);

        /**
         * ??????????????????????????????bar??????
         *
         * @param chatId
         */
        void getBottomFunctionBarData(@NotNull String chatId);

        /**
         * ???????????????????????????
         */
        void getCompanyFunctionData();

        /**
         * ??????????????????
         *
         * @param emergency
         */
        void refreshEventInfo(String emergency);

        /**
         * ???????????????
         */
        void startScanning();

        /**
         * ????????????
         */
        void switchCompany();

        /**
         * ??????????????????
         */
        void goToCompanyMap();

        void goToCameraActivity(Context context, int cameraType, IUIKitCallBack callBack);

        /**
         * ????????????????????????????????????
         *
         * @param id
         * @return
         */
        boolean checkYJYAIMLayout(String id);

        /**
         * ????????????????????????
         */
        void showYJYATasks();
    }

}

