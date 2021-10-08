package com.ymy.im.activity;

import android.content.Intent;
import android.os.Bundle;

import com.ymy.im.fragment.ChatFragment;
import com.ymy.im.helper.ImHelper;
import com.ymy.im.helper.type.EventType;
import com.ymy.im.utils.Constants;
import com.ymy.im.utils.DemoLog;
import com.orhanobut.logger.Logger;
import com.tencent.imsdk.BaseConstants;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.AudioPlayer;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.ymy.core.base.RootActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class ImChatActivity extends RootActivity {

    private static final String TAG = ImChatActivity.class.getSimpleName();
    public static boolean isStop = true;
    private ChatFragment mChatFragment;
    private ChatInfo mChatInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        //初始化群类型
        ImHelper.event_type = "";
        ImHelper.currentGroupIsExist = true;
        chat(getIntent());
    }

    private void chat(Intent intent) {
        Bundle bundle = intent.getExtras();
        DemoLog.i(TAG, "bundle: " + bundle + " intent: " + intent);
        if (bundle == null) {
            startSplashActivity(null);
            return;
        }

        //        OfflineMessageBean bean = OfflineMessageDispatcher.parseOfflineMessage(intent);
        //        if (bean != null) {
        //            mChatInfo = new ChatInfo();
        //            mChatInfo.setType(bean.chatType);
        //            mChatInfo.setId(bean.sender);
        //            bundle.putSerializable(Constants.CHAT_INFO, mChatInfo);
        //            DemoLog.i(TAG, "offline mChatInfo: " + mChatInfo);
        //        } else {
        //            mChatInfo = (ChatInfo) bundle.getSerializable(Constants.CHAT_INFO);
        //            if (mChatInfo == null) {
        //                startSplashActivity(null);
        //                return;
        //            }
        //        }
        mChatInfo = (ChatInfo) bundle.getSerializable(Constants.CHAT_INFO);
        ImHelper.currentChatId = mChatInfo.getId();
        ArrayList<String> userIDList = new ArrayList<>();
        userIDList.add(mChatInfo.getId());
        V2TIMManager.getInstance().getUsersInfo(userIDList,
                new V2TIMValueCallback<List<V2TIMUserFullInfo>>() {
                    @Override
                    public void onSuccess(List<V2TIMUserFullInfo> v2TIMUserFullInfos) {
                        if (v2TIMUserFullInfos.size() > 0) {
                            Logger.e("onSuccess" + v2TIMUserFullInfos.get(0));
                        }
                    }

                    @Override
                    public void onError(int code, String desc) {

                    }
                });

        mChatFragment = new ChatFragment();
        mChatFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.empty_view, mChatFragment).commitAllowingStateLoss();

        checkGroupIsExist();
    }

    /**
     * 如果会话为群聊时检查群聊是否存在或是否被移除群聊
     */
    private void checkGroupIsExist() {
        if (mChatInfo.getType() != V2TIMConversation.V2TIM_C2C) {
            V2TIMManager.getGroupManager().getGroupMemberList(mChatInfo.getId(),
                    V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ALL, 0,
                    new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
                        /**
                         * 成功时回调
                         *
                         * @param v2TIMGroupMemberInfoResult
                         */
                        @Override
                        public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                            ImHelper.currentGroupIsExist = true;
                        }

                        /**
                         * 出错时回调
                         *
                         * @param code
                         *         错误码
                         * @param desc
                         *         错误描述
                         */
                        @Override
                        public void onError(int code, String desc) {
                            if (code == BaseConstants.ERR_SVR_GROUP_PERMISSION_DENY) {
                                ImHelper.currentGroupIsExist = false;
                            }
                        }
                    });
        } else {
            ImHelper.currentGroupIsExist = true;
        }
    }

    private void startSplashActivity(Bundle bundle) {
        try {
            ImHelper.getDBXSendReq().logoutStatus(ImHelper.LOGOUT_REASON_KICK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        DemoLog.i(TAG, "onNewIntent");
        super.onNewIntent(intent);
        chat(intent);
    }

    @Override
    protected void onResume() {
        DemoLog.i(TAG, "onResume");
        isStop = false;
        ImHelper.needShowNotification = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isStop = true;
        AudioPlayer.getInstance().stopPlay();
        ImHelper.needShowNotification = true;
    }

    @Override
    protected void onDestroy() {
        ImHelper.currentChatId = "";
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (EventType.TYPE_ZKJJ.equals(mChatFragment.mEventType)) {
            if (mChatFragment.onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }
}
