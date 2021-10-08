package com.tencent.qcloud.tim.uikit.modules.chat.layout.input;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.ymy.im.activity.GroupMemberListActivity;
import com.ymy.im.helper.ImHelper;
import com.ymy.im.helper.type.EventType;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.config.TUIKitConfigs;
import com.tencent.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.base.BaseInputFragment;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.chat.interfaces.IInputLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.inputmore.InputMoreActionUnit;
import com.tencent.qcloud.tim.uikit.utils.PermissionUtils;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.ymy.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

abstract class InputLayoutUI extends LinearLayout implements IInputLayout {

    protected static final int CAPTURE = 1;
    protected static final int AUDIO_RECORD = 2;
    protected static final int VIDEO_RECORD = 3;
    protected static final int SEND_PHOTO = 4;
    protected static final int SEND_FILE = 5;
    private static String TAG = InputLayoutUI.class.getSimpleName();
    /**
     * 语音/文字切换输入控件
     */
    public ImageView mAudioInputSwitchButton;
    protected boolean mAudioInputDisable;

    /**
     * 表情按钮
     */
    protected ImageView mEmojiInputButton;
    protected boolean mEmojiInputDisable;

    /**
     * 更多按钮
     */
    protected ImageView mMoreInputButton;
    protected Object mMoreInputEvent;
    protected boolean mMoreInputDisable;

    /**
     * 消息发送按钮
     */
    protected AppCompatButton mSendTextButton;
    /**
     * 切换按钮
     */
    protected ImageView change_input_btn;
    /**
     * 语音长按按钮
     */
    protected AppCompatButton mSendAudioButton;
    /**
     * 处理按钮
     */
    protected ImageView mHandleBtn;

    /**
     * 文本输入框
     */
    protected EditText mTextInput;
    protected LinearLayout mLayoutInput;
    protected LinearLayoutCompat mLayoutFunctionBar;

    protected AppCompatActivity mActivity;
    protected View mInputMoreLayout;
    //    protected ShortcutArea mShortcutArea;
    protected View mInputMoreView;
    protected List<InputMoreActionUnit> mInputMoreActionList = new ArrayList<>();
    protected List<InputMoreActionUnit> mInputMoreCustomActionList = new ArrayList<>();
    private AlertDialog mPermissionDialog;
    private boolean mSendPhotoDisable;
    private boolean mCaptureDisable;
    private boolean mVideoRecordDisable;
    private boolean mSendFileDisable;
    private boolean mEnableAudioCall;
    private boolean mEnableVideoCall;

    public InputLayoutUI(Context context) {
        super(context);
        initViews();
    }

    private void initViews() {
        mActivity = (AppCompatActivity) getContext();
        inflate(mActivity, R.layout.chat_input_layout, this);
        mLayoutInput = findViewById(R.id.layout_input);
        mLayoutFunctionBar = findViewById(R.id.layout_function);
        //        mShortcutArea = findViewById(R.id.shortcut_area);
        mInputMoreView = findViewById(R.id.more_groups);
        mSendAudioButton = findViewById(R.id.chat_voice_input);
        mAudioInputSwitchButton = findViewById(R.id.voice_input_switch);
        mEmojiInputButton = findViewById(R.id.face_btn);
        mHandleBtn = findViewById(R.id.handle_btn);
        mMoreInputButton = findViewById(R.id.more_btn);
        mSendTextButton = findViewById(R.id.send_btn);
        change_input_btn = findViewById(R.id.change_input_btn);
        mTextInput = findViewById(R.id.chat_message_input);
        mTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable editable = mTextInput.getText();
                ChatInfo chatInfo = GroupChatManagerKit.getInstance().getCurrentChatInfo();
                if (editable.toString().endsWith("@") && chatInfo != null && !TextUtils.isEmpty(chatInfo.getId())) {
                    GroupMemberListActivity.invoke(getContext());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // 子类实现所有的事件处理
        init();
    }

    protected abstract void init();

    public InputLayoutUI(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public InputLayoutUI(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    protected void assembleActions() {
        mInputMoreActionList.clear();
        InputMoreActionUnit action = new InputMoreActionUnit();
        if (!mSendPhotoDisable) {
            action.setIconResId(R.drawable.icon_im_action_photo);
            action.setTitleId(R.string.pics);
            action.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!checkPermission(SEND_PHOTO)) {
                        TUIKitLog.i(TAG, "audio record checkPermission failed");
                        return;
                    }
                    startSendPhoto();
                }
            });
            mInputMoreActionList.add(action);
        }
        if (!mCaptureDisable) {
            action = new InputMoreActionUnit();
            action.setIconResId(R.drawable.icon_im_action_camera);
            action.setTitleId(R.string.photo);
            action.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCapture();
                }
            });
            mInputMoreActionList.add(action);
        }

        if (!mVideoRecordDisable) {
            action = new InputMoreActionUnit();
            action.setIconResId(R.drawable.icon_im_action_living);
            action.setTitleId(R.string.video);
            action.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startVideoRecord();
                }
            });
            mInputMoreActionList.add(action);
        }
        mInputMoreActionList.addAll(mInputMoreCustomActionList);

    }

    protected boolean checkPermission(int type) {
        if (!PermissionUtils.checkPermission(mActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return false;
        }
        if (!PermissionUtils.checkPermission(mActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return false;
        }
        if (type == SEND_FILE || type == SEND_PHOTO) {
            return true;
        } else if (type == CAPTURE) {
            return PermissionUtils.checkPermission(mActivity, Manifest.permission.CAMERA);
        } else if (type == AUDIO_RECORD) {
            return PermissionUtils.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO);
        } else if (type == VIDEO_RECORD) {
            return PermissionUtils.checkPermission(mActivity, Manifest.permission.CAMERA)
                    && PermissionUtils.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO);
        }
        return true;
    }

    protected abstract void startSendPhoto();

    protected abstract void startCapture();

    protected abstract void startVideoRecord();

    protected abstract void startSendFile();

    protected abstract void startAudioCall();

    protected abstract void startVideoCall();

    @Override
    public void disableAudioInput(boolean disable) {
        mAudioInputDisable = disable;
        if (disable) {
            mAudioInputSwitchButton.setVisibility(GONE);
        } else {
            mAudioInputSwitchButton.setVisibility(VISIBLE);
        }
    }

    @Override
    public void disableEmojiInput(boolean disable) {
        mEmojiInputDisable = disable;
        if (disable) {
            mEmojiInputButton.setVisibility(GONE);
        } else {
            mEmojiInputButton.setVisibility(VISIBLE);
        }
    }

    @Override
    public void disableMoreInput(boolean disable) {
        mMoreInputDisable = disable;
        if (disable) {
            mMoreInputButton.setVisibility(GONE);
            mSendTextButton.setVisibility(VISIBLE);
            change_input_btn.setVisibility(VISIBLE);
            mHandleBtn.setVisibility(GONE);
            if (mInputMoreView.getVisibility() == View.VISIBLE) {
                change_input_btn.setImageResource(R.drawable.ic_input_keyboard_pressed);
            } else {
                change_input_btn.setImageResource(R.drawable.icon_im_input_btn_biaoqing);
            }
        } else {
            mMoreInputButton.setVisibility(VISIBLE);
            mSendTextButton.setVisibility(GONE);
            change_input_btn.setVisibility(GONE);
            initHandlerBtn();
        }
    }

    /**
     * 初始化表情按钮
     */
    public void initHandlerBtn() {
        ChatInfo groupChatInfo = GroupChatManagerKit.getInstance().getCurrentChatInfo();
        if (groupChatInfo == null || StringUtils.isEmpty(ImHelper.event_type) || EventType.TYPE_NORMAL.equals(ImHelper.event_type)) {
            mHandleBtn.setVisibility(GONE);
            change_input_btn.setVisibility(VISIBLE);
            if (mInputMoreView.getVisibility() == View.VISIBLE) {
                change_input_btn.setImageResource(R.drawable.ic_input_keyboard_pressed);
            } else {
                change_input_btn.setImageResource(R.drawable.icon_im_input_btn_biaoqing);
            }
        } else {
            mHandleBtn.setVisibility(VISIBLE);
            change_input_btn.setVisibility(GONE);
        }
    }

    @Override
    public void replaceMoreInput(BaseInputFragment fragment) {
        mMoreInputEvent = fragment;
    }

    @Override
    public void replaceMoreInput(OnClickListener listener) {
        mMoreInputEvent = listener;
    }

    @Override
    public void disableSendPhotoAction(boolean disable) {
        mSendPhotoDisable = disable;
    }

    @Override
    public void disableCaptureAction(boolean disable) {
        mCaptureDisable = disable;
    }

    @Override
    public void disableVideoRecordAction(boolean disable) {
        mVideoRecordDisable = disable;
    }

    @Override
    public void disableSendFileAction(boolean disable) {
        mSendFileDisable = disable;
    }

    @Override
    public boolean enableAudioCall() {
        if (TUIKitConfigs.getConfigs().getGeneralConfig().isSupportAVCall()) {
            mEnableAudioCall = true;
            return true;
        } else {
            mEnableAudioCall = false;
            return false;
        }
    }

    @Override
    public boolean enableVideoCall() {
        if (TUIKitConfigs.getConfigs().getGeneralConfig().isSupportAVCall()) {
            mEnableVideoCall = true;
            return true;
        } else {
            mEnableVideoCall = false;
            return false;
        }
    }

    @Override
    public void addAction(InputMoreActionUnit action) {
        mInputMoreCustomActionList.add(action);
    }

    @Override
    public EditText getInputText() {
        return mTextInput;
    }

    protected void showMoreInputButton(int visibility) {
        if (mMoreInputDisable) {
            return;
        }
        mMoreInputButton.setVisibility(visibility);
    }

    protected void showSendTextButton(int visibility) {
        if (mMoreInputDisable) {
            mSendTextButton.setVisibility(VISIBLE);
            change_input_btn.setVisibility(VISIBLE);
            mHandleBtn.setVisibility(GONE);
            if (mInputMoreView.getVisibility() == View.VISIBLE) {
                change_input_btn.setImageResource(R.drawable.ic_input_keyboard_pressed);
            } else {
                change_input_btn.setImageResource(R.drawable.icon_im_input_btn_biaoqing);
            }
        } else {
            if (visibility == GONE) {
                initHandlerBtn();
            } else {
                mHandleBtn.setVisibility(GONE);
                if (mInputMoreView.getVisibility() == View.VISIBLE) {
                    change_input_btn.setImageResource(R.drawable.ic_input_keyboard_pressed);
                } else {
                    change_input_btn.setImageResource(R.drawable.icon_im_input_btn_biaoqing);
                }
            }
            change_input_btn.setVisibility(visibility);
            mSendTextButton.setVisibility(visibility);
        }
    }

    protected void showEmojiInputButton(int visibility) {
        if (mEmojiInputDisable) {
            return;
        }
        mEmojiInputButton.setVisibility(visibility);
    }

    public void clearCustomActionList() {
        mInputMoreCustomActionList.clear();
    }
}
