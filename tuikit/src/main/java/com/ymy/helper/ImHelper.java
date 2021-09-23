package com.ymy.helper;

import android.content.Context;
import android.content.Intent;

import com.ymy.activity.ImChatActivity;
import com.ymy.activity.SelectSessionActivity;
import com.ymy.helper.type.EventType;
import com.ymy.net.IMHttpManager;
import com.ymy.signature.GenerateTestUserSig;
import com.ymy.utils.Constants;
import com.ymy.utils.DemoLog;
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
     * 新建聊天时使用的类型
     */
    public static int CREATE_ACTION_C2C = 1;
    public static int CREATE_ACTION_GROUP = 3;
    /**
     * 当前群聊是否存在，控制是否可以发送消息等动作，被移除出组群时该值会是false
     */
    public static boolean currentGroupIsExist = true;
    //    public static int retryCount = 0;
    static boolean mCreating;
    private static UIListener mUIListener;
    /**
     * 防空指针用，之前都没有判空
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
         * 获取人员
         *
         * @param chatType
         *         获取类型
         * @param oldMemberIds
         *         原有用户ids
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
         * 获取未读消息数
         *
         * @param unReadNum
         */
        @Override
        public void getUnreadNum(@NotNull int unReadNum) {

        }

        /**
         * 透传消息
         *
         * @param type
         * @param json
         *         展示报警dialog的数据json
         */
        @Override
        public void showAlarmDialog(@NotNull String type, @NotNull Object json) {

        }

        /**
         * 中控交接回调
         *
         * @param data
         *         展示报警dialog的数据List<MessageUpload>
         */
        @Override
        public void showHandover(@NotNull List<MessageUpload> data) {

        }

        /**
         * 展示通知消息
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
         * 获取功能号的底部功能bar数据
         *
         * @param chatId
         */
        @Override
        public void getBottomFunctionBarData(@NotNull String chatId) {

        }

        /**
         * 获取用户功能号数据
         */
        @Override
        public void getCompanyFunctionData() {

        }

        /**
         * 刷新事件信息
         *
         * @param emergency
         */
        @Override
        public void refreshEventInfo(String emergency) {

        }

        /**
         * 调起扫一扫
         */
        @Override
        public void startScanning() {

        }

        /**
         * 切换公司
         */
        @Override
        public void switchCompany() {

        }

        /**
         * 跳转公司地图
         */
        @Override
        public void goToCompanyMap() {

        }

        @Override
        public void goToCameraActivity(Context context, int cameraType, IUIKitCallBack callBack) {

        }

        /**
         * 检查是否展示预案演练布局
         *
         * @param id
         * @return
         */
        @Override
        public boolean checkYJYAIMLayout(String id) {
            return false;
        }

        /**
         * 打开预案演练列表
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
     * 登录
     *
     * @param userId
     *         用户userId
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
//                    //                            callBack.error("获取sign签名异常");
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
//                DemoLog.i(TAG, "登录失败 imLogin errorCode = " + code + ", errorInfo = " + desc);
                Logger.i("登录失败 imLogin errorCode = " + code + ", errorInfo = " + desc);
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
     * 退出登录
     */
    public static void logOut() {
        Logger.e("im logout");
        TUIKit.logout(new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                DemoLog.i(TAG, "退出登录onSuccess ");
                Logger.e("退出登录onSuccess");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                DemoLog.i(TAG, "退出登录 onError");
                Logger.e("退出登录onError");
            }
        });
    }

    /**
     * 修改用户昵称头像
     *
     * @param nickName
     *         参数可能为空，需要判空
     * @param avatar
     *         参数可能为空，需要判空
     */
    public static void userInfoChange(final String nickName, final String avatar) {
        //        final V2TIMUserFullInfo v2TIMUserFullInfo = new V2TIMUserFullInfo();
        //        // 头像
        //        if (!TextUtils.isEmpty(avatar)) {
        //            v2TIMUserFullInfo.setFaceUrl(avatar);
        //        }
        //        // 头像
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
     * 当前IM登录用户信息
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
     * 设置监听
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
     * 处理联系人相关
     * chat 为传过去的信息
     * type = chat 中的type
     * type = 1, 发起单聊，
     * type = 2，群聊加好友
     * type = 3，发起群聊
     * mMembers  t返回用户信息  id，chatName ，
     */
    public static void dealContact(List<GroupMemberInfo> mMembers, final ChatInfo chat,
                                   final GetPersonResultListener callback) {
        if (chat == null) {
            if (callback != null) {
                callback.onResult(false);
            }
        }
        int type = chat.getType();
        if (type == CREATE_ACTION_C2C) {//1, 发起单聊
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
        if (type == CREATE_ACTION_GROUP) {//1, 发起群聊
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
                groupName = groupName + "、" + name;
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
                    ToastUtils.showToast(Ktx.app, "创建群聊失败");
                    if (callback != null) {
                        callback.onResult(false);
                    }
                    //                    ToastUtil.toastLongMessage("createGroupChat fail:" +
                    //                    errCode + "=" + errMsg);
                }
            });
            return;
        }
        //  邀请人进群
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
                //                ToastUtil.toastLongMessage("邀请成员成功");
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

    //关闭当前聊天界面
    public static void closeCurrentEventChat() {

    }

    /**
     * 获取未读消息数量
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
                ToastUtil.toastLongMessage("加载消息失败");
            }
        });
    }

    /**
     * 获取是否有报警或预案演练群
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
     * 主工程回调设置titleEventNoticeBar
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
     * 进入聊天会话页
     * <p>
     * type = V2TIMConversation.V2TIM_C2C   单聊
     * type = V2TIMConversation.V2TIM_GROUP   群聊
     * id :群ID，或者 单聊对象id
     * chatName :群名称，或者 单聊对象名称
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
     * 查询群信息
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
                    ToastUtil.toastShortMessage("对不起，您没有权限处理");
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
     * 进入单聊会话页
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
        //                ToastUtil.toastShortMessage("获取好友信息失败");
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
     * 修改群消息状态
     * type : 类型
     * state:修改状态
     * GetPersonResultListener  状态监听，可为空
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
     * 检查当前聊天群的EventType，并刷新数据
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
     * 转发自定义消息到IM
     */
    public static void forwardMessage(Context context, String data) {
        List<MessageInfo> mDataSource = new ArrayList<>();
        MessageInfo messageInfo = MessageInfoUtil.buildCustomMessage(data);
        mDataSource.add(messageInfo);
        SelectSessionActivity.invoke(context, mDataSource);
    }

    /**
     * IM消息强制设置群属性，解决问题，自定义属性的变化比IM消息接收到要滞后
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
     * 聊天页面设置头部数据回调
     */
    public interface UIListener {
        void setEventTitleData(String eventType, int status, int level, String yaylStartTime,
                               long yaylTime);

        void refreshFunctionLayout();

        void refreshBottomFunctionBar(ArrayList<FunctionBtnData> data);
    }

    /**
     * 未读消息数
     */
    public interface MsgListener {
        void unReaderNum(int unReaderNum);
    }

    public interface DBXSendReq {

        UserInfoDB getUserInfo();//返回登录userId

        //        void goToEventWebActivity(@WebViewType String urlAction, String params);//打开web页

        void goToWebActivity(@NotNull String url);

        /**
         * 获取人员
         *
         * @param chatType
         *         获取类型
         * @param oldMemberIds
         *         原有用户ids
         */
        void goGetPerson(@NotNull int chatType, ArrayList<String> oldMemberIds);
        //打开contactActivity,  chatInfo 中 type:1, 发起单聊，2，群聊加好友，3发起群聊

        void logoutStatus(@NotNull int logoutReason);//退出回调,以及被踢逻辑

        void loginStatus(@NotNull int status);//登录回调

        void refreshMsgTabFragment();//

        /**
         * 获取未读消息数
         *
         * @param unReadNum
         */
        void getUnreadNum(@NotNull int unReadNum);//

        /**
         * 执行事件处理流程
         *
         * @param alarmId 报警id
         */
        //        void sendHandleEventAction(@NotNull String alarmId);

        /**
         * 报警转工单
         *
         * @param eventId 报警id
         */
        //        void eventToWorkOrder(@NotNull String eventId);

        /**
         * 获取事件群相关信息
         *
         * @param alarmId 报警id
         */
        //        void getEventInfo(@NotNull String alarmId);

        /**
         * 透传消息
         *
         * @param json
         *         展示报警dialog的数据json
         */
        void showAlarmDialog(@NotNull String type, @NotNull Object json);

        /**
         * 中控交接回调
         *
         * @param data
         *         展示报警dialog的数据List<MessageUpload>
         */
        void showHandover(@NotNull List<MessageUpload> data);

        /**
         * @return
         */
        //        boolean isUserRoleManager();

        /**
         * 结束
         *
         * @param eventId
         */
        //        void finishYAYL(String eventId);

        /**
         * 展示通知消息
         *
         * @param title
         * @param content
         * @param avator
         */
        void showNotification(@NotNull String title, @NotNull String content, String avator,
                              String senderId, int msgId, int type);

        /**
         * 获取功能号的底部功能bar数据
         *
         * @param chatId
         */
        void getBottomFunctionBarData(@NotNull String chatId);

        /**
         * 获取用户功能号数据
         */
        void getCompanyFunctionData();

        /**
         * 刷新事件信息
         *
         * @param emergency
         */
        void refreshEventInfo(String emergency);

        /**
         * 调起扫一扫
         */
        void startScanning();

        /**
         * 切换公司
         */
        void switchCompany();

        /**
         * 跳转公司地图
         */
        void goToCompanyMap();

        void goToCameraActivity(Context context, int cameraType, IUIKitCallBack callBack);

        /**
         * 检查是否展示预案演练布局
         *
         * @param id
         * @return
         */
        boolean checkYJYAIMLayout(String id);

        /**
         * 打开预案演练列表
         */
        void showYJYATasks();
    }

}

