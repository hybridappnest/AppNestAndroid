package com.tencent.qcloud.tim.uikit.modules.conversation;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.ymy.im.fragment.TabMsgFragment;
import com.orhanobut.logger.Logger;
import com.tencent.imsdk.BaseConstants;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMConversationResult;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfoResult;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.config.TUIKitConfigs;
import com.tencent.qcloud.tim.uikit.modules.contact.ContactItemBean;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.tencent.qcloud.tim.uikit.modules.message.MessageRevokedManager;
import com.tencent.qcloud.tim.uikit.utils.SharedPreferenceUtils;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.ymy.core.user.YmyUserManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConversationManagerKit implements MessageRevokedManager.MessageRevokeHandler {

    private final static String TAG = ConversationManagerKit.class.getSimpleName();
    private final static String SP_NAME = "_top_conversion_list";
    private final static String SP_IMAGE = "_conversation_group_face";
    private final static String TOP_LIST = "top_list";

    private static ConversationManagerKit instance = new ConversationManagerKit();
    TabMsgFragment.InfoChange callBack;
    private ConversationProvider mProvider;
    private List<MessageUnreadWatcher> mUnreadWatchers = new ArrayList<>();
    private SharedPreferences mConversationPreferences;
    private LinkedList<ConversationInfo> mTopLinkedList = new LinkedList<>();
    private int mUnreadTotal;

    private ConversationManagerKit() {
        init();
    }

    private void init() {
        TUIKitLog.i(TAG, "init");
        MessageRevokedManager.getInstance().addHandler(this);
    }

    public static ConversationManagerKit getInstance() {
        return instance;
    }

    public ConversationProvider getProvider() {
        return mProvider;
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param v2TIMConversationList
     *         ???????????????????????????
     */
    public void onRefreshConversation(List<V2TIMConversation> v2TIMConversationList) {
        TUIKitLog.v(TAG, "onRefreshConversation conversations:" + v2TIMConversationList);
        if (mProvider == null) {
            return;
        }
        ArrayList<ConversationInfo> infos = new ArrayList<>();
        for (int i = 0; i < v2TIMConversationList.size(); i++) {
            V2TIMConversation v2TIMConversation = v2TIMConversationList.get(i);
            TUIKitLog.v(TAG,
                    "refreshConversation v2TIMConversation " + v2TIMConversation.toString());
            ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
            if (conversationInfo != null) {
                infos.add(conversationInfo);
            }
        }
        if (infos.size() == 0) {
            return;
        }
        List<ConversationInfo> dataSource = mProvider.getDataSource();
        ArrayList exists = new ArrayList();
        for (int j = 0; j < infos.size(); j++) {
            ConversationInfo update = infos.get(j);
            boolean exist = false;
            for (int i = 0; i < dataSource.size(); i++) {
                ConversationInfo cacheInfo = dataSource.get(i);
                //???????????????????????????????????????????????????????????????????????????????????????????????????????????????id?????????id??????
                if (cacheInfo.getId().equals(update.getId()) && cacheInfo.isGroup() == update.isGroup()) {
                    dataSource.remove(i);
                    dataSource.add(i, update);
                    exists.add(update);
                    //infos.remove(j);
                    //???????????????????????????
                    mUnreadTotal = mUnreadTotal - cacheInfo.getUnRead() + update.getUnRead();
                    TUIKitLog.v(TAG, "onRefreshConversation after mUnreadTotal = " + mUnreadTotal);
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                mUnreadTotal += update.getUnRead();
                TUIKitLog.i(TAG,
                        "onRefreshConversation exist = " + exist + ", mUnreadTotal = " + mUnreadTotal);
            }
        }
        infos.removeAll(exists);
        if (infos.size() > 0) {////
            dataSource.addAll(infos);
        }
        sortConversations(mProvider, dataSource);
        SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
        updateUnreadTotal(mUnreadTotal);
    }

    /**
     * TIMConversation?????????ConversationInfo
     *
     * @param conversation
     * @return
     */
    private ConversationInfo TIMConversation2ConversationInfo(final V2TIMConversation conversation) {
        if (conversation == null) {
            return null;
        }
        //        TUIKitLog.i(TAG, "TIMConversation2ConversationInfo id:" + conversation
        //        .getConversationID()
        //                + "|name:" + conversation.getShowName()
        //                + "|unreadNum:" + conversation.getUnreadCount());
        V2TIMMessage message = conversation.getLastMessage();
        if (message == null) {
            return null;
        }
        final ConversationInfo info = new ConversationInfo();
        int type = conversation.getType();
        if (type != V2TIMConversation.V2TIM_C2C && type != V2TIMConversation.V2TIM_GROUP) {
            return null;
        }

        boolean isGroup = type == V2TIMConversation.V2TIM_GROUP;
        info.setLastMessageTime(message.getTimestamp());
        List<MessageInfo> list = MessageInfoUtil.TIMMessage2MessageInfo(message);
        if (list != null && list.size() > 0) {
            info.setLastMessage(list.get(list.size() - 1));
        }

        info.setTitle(conversation.getShowName());
        if (isGroup) {
            fillConversationUrlForGroup(conversation, info);
        } else {
            List<Object> faceList = new ArrayList<>();
            if (TextUtils.isEmpty(conversation.getFaceUrl())) {
                faceList.add(R.drawable.default_head);
            } else {
                faceList.add(conversation.getFaceUrl());
            }
            info.setIconUrlList(faceList);
        }
        if (isGroup) {
            info.setId(conversation.getGroupID());
        } else {
            info.setId(conversation.getUserID());
        }
        info.setConversationId(conversation.getConversationID());
        info.setGroup(isGroup);
        info.setUnRead(conversation.getUnreadCount());
        return info;
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param sources
     * @return
     */
    private void sortConversations(ConversationProvider mProvider, List<ConversationInfo> sources) {
        ArrayList<ConversationInfo> conversationInfos = new ArrayList<>();
        List<ConversationInfo> normalConversations = new ArrayList<>();
        List<ConversationInfo> topConversations = new ArrayList<>();

        for (int i = 0; i <= sources.size() - 1; i++) {
            ConversationInfo conversation = sources.get(i);
            if (isTop(conversation.getId())) {
                conversation.setTop(true);
                topConversations.add(conversation);
            } else {
                normalConversations.add(conversation);
            }
        }

        mTopLinkedList.clear();
        mTopLinkedList.addAll(topConversations);
        Collections.sort(topConversations); // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        conversationInfos.addAll(topConversations);
        if (normalConversations.size() > 0) {
            Collections.sort(normalConversations); // ?????????????????????????????????????????????????????????????????????
        }
        conversationInfos.addAll(normalConversations);

        mProvider.setDataSource(conversationInfos);
//        getGroupCustom(conversationInfos, mProvider);
    }

    /**
     * ????????????????????????
     *
     * @param unreadTotal
     */
    public void updateUnreadTotal(int unreadTotal) {
        TUIKitLog.i(TAG, "updateUnreadTotal:" + unreadTotal);
        mUnreadTotal = unreadTotal;
        for (int i = 0; i < mUnreadWatchers.size(); i++) {
            mUnreadWatchers.get(i).updateUnread(mUnreadTotal);
        }
        if (callBack != null) {
            callBack.infoChange("", 1, mUnreadTotal);
        }
    }

    private void fillConversationUrlForGroup(final V2TIMConversation conversation,
                                             final ConversationInfo info) {
        if (TextUtils.isEmpty(conversation.getFaceUrl())) {
            final String savedIcon = getGroupConversationAvatar(conversation.getConversationID());
            if (TextUtils.isEmpty(savedIcon)) {
                fillFaceUrlList(conversation.getGroupID(), info);
            } else {
                List<Object> list = new ArrayList<>();
                list.add(savedIcon);
                info.setIconUrlList(list);
            }
        } else {
            List<Object> list = new ArrayList<>();
            list.add(conversation.getFaceUrl());
            info.setIconUrlList(list);
        }
    }

    private boolean isTop(String id) {
        if (mTopLinkedList == null || mTopLinkedList.size() == 0) {
            return false;
        }
        for (ConversationInfo info : mTopLinkedList) {
            if (TextUtils.equals(info.getId(), id)) {
                return true;
            }
        }
        return false;
    }

    public void getGroupCustom(final ArrayList<ConversationInfo> conversationInfos,
                               final ConversationProvider mProvider) {
        try {
            List<String> list = new ArrayList<>();
            for (ConversationInfo info : conversationInfos) {
                ContactItemBean bean = new ContactItemBean();
                if (info.isGroup()) {
                    list.add(info.getId());
                    checkGroupIsExistence(info.getId(), info.getConversationId());
                }
            }
            if (list.isEmpty()) {
                mProvider.setDataSource(conversationInfos);
                return;
            }
            final HashMap<String, String> hp = new HashMap<>();
            final HashMap<String, String> companyHp = new HashMap<>();
            final HashMap<String, String> companyIdHp = new HashMap<>();
            final HashMap<String, String> companyKeyPartHp = new HashMap<>();
            final HashMap<String, String> lableHp = new HashMap<>();

            V2TIMManager.getGroupManager().getGroupsInfo(list,
                    new V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
                        @Override
                        public void onSuccess(List<V2TIMGroupInfoResult> v2TIMGroupInfoResults) {
                            ArrayList<String> deletedGroupID = new ArrayList<String>();
                            for (int i = 0; i < v2TIMGroupInfoResults.size(); ++i) {
                                V2TIMGroupInfo groupInfo =
                                        v2TIMGroupInfoResults.get(i).getGroupInfo();
                                String owner = groupInfo.getOwner();
                                if (owner == null || owner.isEmpty()) {
                                    deletedGroupID.add(groupInfo.getGroupID());
                                    continue;
                                }
                                Map<String, byte[]> customInfo = groupInfo.getCustomInfo();
                                String groupID = groupInfo.getGroupID();
                                if (customInfo.containsKey("event_group_type")) {
                                    String value = new String(customInfo.get("event_group_type"));
                                    hp.put(groupID, value);
                                }
                                if (customInfo.containsKey("company_name")) {
                                    String comValue = new String(customInfo.get("company_name"));
                                    companyHp.put(groupID, comValue);
                                }
                                if (customInfo.containsKey("company_id")) {
                                    String comIdValue = new String(customInfo.get("company_id"));
                                    companyIdHp.put(groupID, comIdValue);
                                }
                                if (customInfo.containsKey("lable")) {
                                    String comLable = new String(customInfo.get("lable"));
                                    lableHp.put(groupID, comLable);
                                }
                                if (customInfo.containsKey("is_key_part")) {
                                    String comkeyValue = new String(customInfo.get("is_key_part"));
                                    companyKeyPartHp.put(groupID, comkeyValue);
                                }
                            }
                            ArrayList<ConversationInfo> deletedConversations =
                                    new ArrayList<ConversationInfo>();
                            //????????????????????????
                            List<ConversationInfo> keyPartConversations = new ArrayList<>();
                            for (int i = 0; i < conversationInfos.size(); ++i) {
                                ConversationInfo conversationItemInfo = conversationInfos.get(i);
                                if (!conversationItemInfo.isGroup()) {
                                    continue;
                                }
                                if (deletedGroupID.contains(conversationItemInfo.getId())) {
                                    deletedConversations.add(conversationItemInfo);
                                    continue;
                                }
                                String groupId = conversationItemInfo.getId();
                                if (hp.containsKey(groupId)) {
                                    conversationItemInfo.setEvent_group_type(hp.get(groupId));
                                }
                                if (companyHp.containsKey(groupId)) {
                                    conversationItemInfo.setCompany_name(companyHp.get(groupId));
                                }
                                if (companyIdHp.containsKey(groupId)) {
                                    conversationItemInfo.setCompany_id(companyIdHp.get(groupId));
                                }
                                if (lableHp.containsKey(groupId)) {
                                    conversationItemInfo.setLable(lableHp.get(groupId));
                                }
                                if (companyKeyPartHp.containsKey(groupId)) {
                                    ConversationInfo conversationInfo = conversationItemInfo;
                                    String is_key_part = companyKeyPartHp.get(groupId);
                                    if (TextUtils.equals(is_key_part, "1")) {
                                        keyPartConversations.add(conversationInfo);
                                    }
                                    conversationInfo.setIs_key_part(is_key_part);
                                }
                            }
                            if (!deletedConversations.isEmpty()) {
                                //                        conversationInfos.removeAll
                                //                        (deletedConversations);
                            }
                            if (!keyPartConversations.isEmpty()) {
                                conversationInfos.removeAll(keyPartConversations);
                                // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                                Collections.sort(keyPartConversations);
                                conversationInfos.addAll(0, keyPartConversations);
                            }
                            mProvider.setDataSource(conversationInfos);
                        }

                        @Override
                        public void onError(int code, String desc) {
                            Logger.e("code:" + code + "desc:" + desc);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mProvider.setDataSource(conversationInfos);
        }
    }

    public String getGroupConversationAvatar(String groupId) {
        SharedPreferences sp = TUIKit.getAppContext().getSharedPreferences(
                TUIKitConfigs.getConfigs().getGeneralConfig().getSDKAppId() + SP_IMAGE,
                Context.MODE_PRIVATE);
        final String savedIcon = sp.getString(groupId, "");
        if (!TextUtils.isEmpty(savedIcon) && new File(savedIcon).isFile() && new File(savedIcon).exists()) {
            return savedIcon;
        }
        return "";
    }

    private void fillFaceUrlList(final String groupID, final ConversationInfo info) {
        V2TIMManager.getGroupManager().getGroupMemberList(groupID,
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ALL, 0,
                new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
                    @Override
                    public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                        List<V2TIMGroupMemberFullInfo> v2TIMGroupMemberFullInfoList =
                                v2TIMGroupMemberInfoResult.getMemberInfoList();
                        int faceSize = Math.min(v2TIMGroupMemberFullInfoList.size(), 9);
                        List<Object> urlList = new ArrayList<>();
                        for (int i = 0; i < faceSize; i++) {
                            V2TIMGroupMemberFullInfo v2TIMGroupMemberFullInfo =
                                    v2TIMGroupMemberFullInfoList.get(i);
                            if (TextUtils.isEmpty(v2TIMGroupMemberFullInfo.getFaceUrl())) {
                                urlList.add(R.drawable.default_head);
                            } else {
                                urlList.add(v2TIMGroupMemberFullInfo.getFaceUrl());
                            }
                        }
                        info.setIconUrlList(urlList);
                        mProvider.updateAdapter(info.getConversationId());
                    }

                    @Override
                    public void onError(int code, String desc) {
                        TUIKitLog.e(TAG,
                                "getGroupMemberList failed! groupID:" + groupID + "|code:" + code +
                                        "|desc: " + desc);
                        if (code == BaseConstants.ERR_SVR_GROUP_PERMISSION_DENY) {
                            List<String> groupIds = new ArrayList();
                            groupIds.add(info.getId());
                            V2TIMManager.getGroupManager().getGroupsInfo(groupIds,
                                    new V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
                                        @Override
                                        public void onSuccess(List<V2TIMGroupInfoResult> v2TIMGroupInfoResults) {
                                            if (v2TIMGroupInfoResults.size() > 0) {
                                                V2TIMGroupInfoResult v2TIMGroupInfoResult =
                                                        v2TIMGroupInfoResults.get(0);
                                                V2TIMGroupInfo groupInfo =
                                                        v2TIMGroupInfoResult.getGroupInfo();
                                                String owner = groupInfo.getOwner();
                                                List<String> temp = new ArrayList();
                                                temp.add(owner);
                                                V2TIMManager.getInstance().getUsersInfo(temp,
                                                        new V2TIMValueCallback<List<V2TIMUserFullInfo>>() {
                                                            @Override
                                                            public void onSuccess(List<V2TIMUserFullInfo> v2TIMUserFullInfos) {
                                                                if (v2TIMUserFullInfos.size() > 0) {
                                                                    V2TIMUserFullInfo v2TIMUserFullInfo = v2TIMUserFullInfos.get(0);
                                                                    List<Object> urlList =
                                                                            new ArrayList<>();
                                                                    if (v2TIMUserFullInfo.getFaceUrl().isEmpty()) {
                                                                        urlList.add(R.drawable.default_head);
                                                                    } else {
                                                                        urlList.add(v2TIMUserFullInfo.getFaceUrl());
                                                                    }
                                                                    String avatarUrl =
                                                                            YmyUserManager.INSTANCE.getUser().getAvatarUrl();
                                                                    if (avatarUrl.isEmpty()) {
                                                                        urlList.add(R.drawable.default_head);
                                                                    } else {
                                                                        urlList.add(avatarUrl);
                                                                    }
                                                                    info.setIconUrlList(urlList);
                                                                    mProvider.updateAdapter(info.getConversationId());
                                                                }
                                                            }

                                                            @Override
                                                            public void onError(int code,
                                                                                String desc) {
                                                                List<Object> urlList =
                                                                        new ArrayList<>();
                                                                urlList.add(R.drawable.default_head);
                                                                info.setIconUrlList(urlList);
                                                                mProvider.updateAdapter(info.getConversationId());
                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onError(int code, String desc) {
                                            List<Object> urlList =
                                                    new ArrayList<>();
                                            urlList.add(R.drawable.default_head);
                                            info.setIconUrlList(urlList);
                                            mProvider.updateAdapter(info.getConversationId());

                                        }
                                    });
                        }
                    }
                });
    }

    private void checkGroupIsExistence(String groupID, String conversationId) {
        V2TIMManager.getGroupManager().getGroupMemberList(groupID,
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ALL, 0,
                new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
                    @Override
                    public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                    }

                    @Override
                    public void onError(int code, String desc) {
                        TUIKitLog.e(TAG,
                                "getGroupMemberList failed! groupID:" + groupID + "|code:" + code +
                                        "|desc: " + desc);
                        if (code == 10010) {
                            V2TIMManager.getConversationManager().deleteConversation(conversationId
                                    , new V2TIMCallback() {
                                        @Override
                                        public void onSuccess() {
                                            TUIKitLog.i(TAG, "deleteConversation success");
                                        }

                                        @Override
                                        public void onError(int code, String desc) {
                                            TUIKitLog.e(TAG,
                                                    "deleteConversation error:" + code + ", " +
                                                            "desc:" + desc);
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * ?????????????????????
     *
     * @param index
     * @param conversation
     */
    public void setConversationTop(int index, ConversationInfo conversation) {
        TUIKitLog.i(TAG, "setConversationTop index:" + index + "|conversation:" + conversation);
        if (!conversation.isTop()) {
            conversation.setTop(true);
            setConversationTop(conversation.getId(), true);
        } else {
            conversation.setTop(false);
            setConversationTop(conversation.getId(), false);
        }
        sortConversations(mProvider, mProvider.getDataSource());
        if (callBack != null) {
            callBack.infoChange("", 0, mUnreadTotal);
        }
    }

    /**
     * ??????????????????
     *
     * @param id
     *         ??????ID
     * @param flag
     *         ????????????
     */
    public void setConversationTop(String id, boolean flag) {
        TUIKitLog.i(TAG, "setConversationTop id:" + id + "|flag:" + flag);
        handleTopData(id, flag);
        sortConversations(mProvider, mProvider.getDataSource());
        SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
    }

    /**
     * ??????????????????????????????????????????imsdk?????????
     *
     * @param index
     *         ????????????????????????
     * @param conversation
     *         ????????????
     */
    public void deleteConversation(int index, ConversationInfo conversation) {
        TUIKitLog.i(TAG, "deleteConversation index:" + index + "|conversation:" + conversation);
        V2TIMManager.getConversationManager().deleteConversation(conversation.getConversationId()
                , new V2TIMCallback() {
                    @Override
                    public void onSuccess() {
                        TUIKitLog.i(TAG, "deleteConversation success");
                    }

                    @Override
                    public void onError(int code, String desc) {
                        TUIKitLog.e(TAG, "deleteConversation error:" + code + ", desc:" + desc);
                    }
                });
        handleTopData(conversation.getId(), false);
        mProvider.deleteConversation(index);
        updateUnreadTotal(mUnreadTotal - conversation.getUnRead());
        if (callBack != null) {
            callBack.infoChange("", 0, mUnreadTotal);
        }
    }

    /**
     * ?????????????????????????????????????????????SharePreferences????????????????????????
     *
     * @param id
     * @param flag
     */
    private void handleTopData(String id, boolean flag) {
        if (mProvider == null) {
            return;
        }
        ConversationInfo conversation = null;
        List<ConversationInfo> conversationInfos = mProvider.getDataSource();
        for (int i = 0; i < conversationInfos.size(); i++) {
            ConversationInfo info = conversationInfos.get(i);
            if (info.getId().equals(id)) {
                conversation = info;
                break;
            }
        }
        if (conversation == null) {
            return;
        }
        if (flag) {
            if (!isTop(conversation.getId())) {
                mTopLinkedList.remove(conversation);
                mTopLinkedList.addFirst(conversation);
                conversation.setTop(true);
            } else {
                return;
            }
        } else {
            if (isTop(conversation.getId())) {
                conversation.setTop(false);
                mTopLinkedList.remove(conversation);
            } else {
                return;
            }
        }
        SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param id
     *         C2C???????????? userID???Group?????? ID
     */
    public void deleteConversation(String id, boolean isGroup) {
        TUIKitLog.i(TAG, "deleteConversation id:" + id + "|isGroup:" + isGroup);
        if (mProvider != null) {
            handleTopData(id, false);
            List<ConversationInfo> conversationInfos = mProvider.getDataSource();
            for (int i = 0; i < conversationInfos.size(); i++) {
                ConversationInfo info = conversationInfos.get(i);
                if (info.getId().equals(id)) {
                    updateUnreadTotal(mUnreadTotal - info.getUnRead());
                    break;
                }
            }
            String conversationID = "";
            List<ConversationInfo> conversationInfoList = mProvider.getDataSource();
            for (ConversationInfo conversationInfo : conversationInfoList) {
                if (isGroup == conversationInfo.isGroup() && conversationInfo.getId().equals(id)) {
                    conversationID = conversationInfo.getConversationId();
                    break;
                }
            }
            if (!TextUtils.isEmpty(conversationID)) {
                mProvider.deleteConversation(conversationID);
            }
            if (!TextUtils.isEmpty(conversationID)) {
                V2TIMManager.getConversationManager().deleteConversation(conversationID,
                        new V2TIMCallback() {
                            @Override
                            public void onSuccess() {
                                TUIKitLog.i(TAG, "deleteConversation success");
                            }

                            @Override
                            public void onError(int code, String desc) {
                                TUIKitLog.i(TAG,
                                        "deleteConversation error:" + code + ", desc:" + desc);
                            }
                        });
            }
        }
    }

    /**
     * ????????????
     *
     * @param conversationInfo
     * @return
     */
    public boolean addConversation(ConversationInfo conversationInfo) {
        List<ConversationInfo> conversationInfos = new ArrayList<>();
        conversationInfos.add(conversationInfo);
        return mProvider.addConversations(conversationInfos);
    }

    /**
     * ???????????????????????????
     *
     * @return ????????????
     */
    public int getUnreadTotal() {
        return mUnreadTotal;
    }

    public boolean isTopConversation(String groupId) {
        TUIKitLog.i(TAG, "isTopConversation:" + groupId);
        return isTop(groupId);
    }

    /**
     * ??????????????????
     *
     * @param msgID
     */
    @Override
    public void handleInvoke(String msgID) {
        TUIKitLog.i(TAG, "handleInvoke msgID:" + msgID);
        if (mProvider != null) {
            loadConversation(null);
        }
    }

    /**
     * ??????????????????
     *
     * @param callBack
     */
    public void loadConversation(final IUIKitCallBack callBack) {
        TUIKitLog.i(TAG, "loadConversation callBack:" + callBack);
        mConversationPreferences = TUIKit.getAppContext().getSharedPreferences(
                TUIKitConfigs.getConfigs().getGeneralConfig().getSDKAppId() + "-"
                        + V2TIMManager.getInstance().getLoginUser() + SP_NAME,
                Context.MODE_PRIVATE);
        mTopLinkedList = SharedPreferenceUtils.getListData(mConversationPreferences, TOP_LIST,
                ConversationInfo.class);
        //mProvider???????????????null,???????????????????????????????????????????????????????????????
        if (mProvider == null) {
            mProvider = new ConversationProvider();
        }

        V2TIMManager.getConversationManager().getConversationList(0, 500,
                new V2TIMValueCallback<V2TIMConversationResult>() {
                    @Override
                    public void onSuccess(V2TIMConversationResult v2TIMConversationResult) {
                        dealConversation(callBack, v2TIMConversationResult);
                    }

                    @Override
                    public void onError(int code, String desc) {
                        TUIKitLog.v(TAG,
                                "loadConversation getConversationList error, code = " + code +
                                        ", desc = " + desc);
                    }
                });
    }

    /**
     * ?????????????????????
     */
    public void dealConversation(final IUIKitCallBack callBack,
                                 final V2TIMConversationResult v2TIMConversationResult) {
        ArrayList<ConversationInfo> infos = new ArrayList<>();
        List<V2TIMConversation> v2TIMConversationList =
                v2TIMConversationResult.getConversationList();
        for (V2TIMConversation v2TIMConversation : v2TIMConversationList) {
            //??? imsdk v2TIMConversation ????????? UIKit ConversationInfo
            ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
            if (conversationInfo != null) {
                conversationInfo.setType(ConversationInfo.TYPE_COMMON);
                infos.add(conversationInfo);
            }
        }
        //?????????imsdk???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????imsdk????????????????????????
        sortConversations(mProvider, infos);
        SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
        if (callBack != null) {
            callBack.onSuccess(mProvider);
        }
    }

    /**
     * ???????????????????????????
     *
     * @param messageUnreadWatcher
     */
    public void addUnreadWatcher(MessageUnreadWatcher messageUnreadWatcher) {
        TUIKitLog.i(TAG, "addUnreadWatcher:" + messageUnreadWatcher);
        if (!mUnreadWatchers.contains(messageUnreadWatcher)) {
            mUnreadWatchers.add(messageUnreadWatcher);
            messageUnreadWatcher.updateUnread(mUnreadTotal);
        }
    }

    public void setCallBac(TabMsgFragment.InfoChange callBac) {
        callBack = callBac;
    }

    /**
     * ???????????????????????????
     *
     * @param messageUnreadWatcher
     */
    public void removeUnreadWatcher(MessageUnreadWatcher messageUnreadWatcher) {
        TUIKitLog.i(TAG, "removeUnreadWatcher:" + messageUnreadWatcher);
        if (messageUnreadWatcher == null) {
            mUnreadWatchers.clear();
        } else {
            mUnreadWatchers.remove(messageUnreadWatcher);
        }
    }

    /**
     * ???UI????????????????????????????????????
     */
    public void destroyConversation() {
        TUIKitLog.i(TAG, "destroyConversation");
        if (mProvider != null) {
            mProvider.attachAdapter(null);
        }
        if (mUnreadWatchers != null) {
            mUnreadWatchers.clear();
        }
    }

    public void setGroupConversationAvatar(String groupId, String url) {
        SharedPreferences sp = TUIKit.getAppContext().getSharedPreferences(
                TUIKitConfigs.getConfigs().getGeneralConfig().getSDKAppId() + SP_IMAGE,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(groupId, url);
        editor.apply();
    }

    /**
     * ?????????????????????????????????
     */
    public interface MessageUnreadWatcher {
        void updateUnread(int count);
    }

}