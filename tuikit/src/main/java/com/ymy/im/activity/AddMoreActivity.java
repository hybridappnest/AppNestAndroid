package com.ymy.im.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.ymy.im.utils.DemoLog;
import com.tencent.imsdk.BaseConstants;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMFriendAddApplication;
import com.tencent.imsdk.v2.V2TIMFriendOperationResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.utils.SoftKeyBoardUtil;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.base.RootActivity;

public class AddMoreActivity extends RootActivity {

    private static final String TAG = AddMoreActivity.class.getSimpleName();

    private TitleBarLayout mTitleBar;
    private EditText mUserID;
    private EditText mAddWording;
    private boolean mIsGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mIsGroup = getIntent().getExtras().getBoolean(TUIKitConstants.GroupType.GROUP);
        }
        setContentView(R.layout.contact_add_activity);
        mTitleBar = findViewById(R.id.add_friend_titlebar);
        mTitleBar.setTitle(mIsGroup ? getResources().getString(R.string.add_group) : getResources().getString(R.string.add_friend), TitleBarLayout.POSITION.LEFT);
        mTitleBar.setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBar.getRightGroup().setVisibility(View.GONE);

        mUserID = findViewById(R.id.user_id);
        mAddWording = findViewById(R.id.add_wording);
    }

    public void add(View view) {
        if (mIsGroup) {
            addGroup(view);
        } else {
            addFriend(view);
        }
    }

    public void addFriend(View view) {
        String id = mUserID.getText().toString();
        if (TextUtils.isEmpty(id)) {
            return;
        }

        V2TIMFriendAddApplication v2TIMFriendAddApplication = new V2TIMFriendAddApplication(id);
        v2TIMFriendAddApplication.setAddWording(mAddWording.getText().toString());
        v2TIMFriendAddApplication.setAddSource("android");
        V2TIMManager.getFriendshipManager().addFriend(v2TIMFriendAddApplication, new V2TIMValueCallback<V2TIMFriendOperationResult>() {
            @Override
            public void onError(int code, String desc) {
                DemoLog.e(TAG, "addFriend err code = " + code + ", desc = " + desc);
//                ToastUtil.toastShortMessage("Error code = " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess(V2TIMFriendOperationResult v2TIMFriendOperationResult) {
                DemoLog.i(TAG, "addFriend success");
                switch (v2TIMFriendOperationResult.getResultCode()) {
                    case BaseConstants.ERR_SUCC:
                        ToastUtil.toastShortMessage("成功");
                        break;
                    case BaseConstants.ERR_SVR_FRIENDSHIP_INVALID_PARAMETERS:
                        if (TextUtils.equals(v2TIMFriendOperationResult.getResultInfo(), "Err_SNS_FriendAdd_Friend_Exist")) {
                            ToastUtil.toastShortMessage("对方已是您的好友");
                            break;
                        }
                    case BaseConstants.ERR_SVR_FRIENDSHIP_COUNT_LIMIT:
                        ToastUtil.toastShortMessage("您的好友数已达系统上限");
                        break;
                    case BaseConstants.ERR_SVR_FRIENDSHIP_PEER_FRIEND_LIMIT:
                        ToastUtil.toastShortMessage("对方的好友数已达系统上限");
                        break;
                    case BaseConstants.ERR_SVR_FRIENDSHIP_IN_SELF_BLACKLIST:
                        ToastUtil.toastShortMessage("被加好友在自己的黑名单中");
                        break;
                    case BaseConstants.ERR_SVR_FRIENDSHIP_ALLOW_TYPE_DENY_ANY:
                        ToastUtil.toastShortMessage("对方已禁止加好友");
                        break;
                    case BaseConstants.ERR_SVR_FRIENDSHIP_IN_PEER_BLACKLIST:
                        ToastUtil.toastShortMessage("您已被被对方设置为黑名单");
                        break;
                    case BaseConstants.ERR_SVR_FRIENDSHIP_ALLOW_TYPE_NEED_CONFIRM:
                        ToastUtil.toastShortMessage("等待好友审核同意");
                        break;
                    default:
//                        ToastUtil.toastLongMessage(v2TIMFriendOperationResult.getResultCode() + " " + v2TIMFriendOperationResult.getResultInfo());
                        break;
                }
                finish();
            }
        });
    }

    public void addGroup(View view) {
        String id = mUserID.getText().toString();
        if (TextUtils.isEmpty(id)) {
            return;
        }

        V2TIMManager.getInstance().joinGroup(id, mAddWording.getText().toString(), new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                DemoLog.e(TAG, "addGroup err code = " + code + ", desc = " + desc);
//                ToastUtil.toastShortMessage("Error code = " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess() {
                DemoLog.i(TAG, "addGroup success");
                ToastUtil.toastShortMessage("加群请求已发送");
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        SoftKeyBoardUtil.hideKeyBoard(mUserID.getWindowToken());
    }
}
