package com.tencent.qcloud.tim.uikit.modules.chat.layout.input;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lcw.library.imagepicker.ImagePicker;
import com.tencent.cloud.qcloudasrsdk.common.QCloudAudioFormat;
import com.tencent.cloud.qcloudasrsdk.common.QCloudAudioFrequence;
import com.tencent.cloud.qcloudasrsdk.common.QCloudSourceType;
import com.tencent.cloud.qcloudasrsdk.models.QCloudOneSentenceRecognitionParams;
import com.tencent.cloud.qcloudasrsdk.recognizer.QCloudOneSentenceRecognizer;
import com.tencent.cloud.qcloudasrsdk.recognizer.QCloudOneSentenceRecognizerListener;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.liteav.SelectContactActivity;
import com.tencent.liteav.login.UserModel;
import com.tencent.liteav.model.DiscernResult;
import com.tencent.liteav.model.ITRTCAVCall;
import com.tencent.liteav.trtcvideocalldemo.ui.TRTCVideoCallActivity;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.AudioPlayer;
import com.tencent.qcloud.tim.uikit.component.face.Emoji;
import com.tencent.qcloud.tim.uikit.component.face.FaceFragment;
import com.tencent.qcloud.tim.uikit.component.face.FaceManager;
import com.tencent.qcloud.tim.uikit.config.TUIKitConfigs;
import com.tencent.qcloud.tim.uikit.modules.chat.base.AbsChatLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.base.BaseInputFragment;
import com.tencent.qcloud.tim.uikit.modules.chat.interfaces.IChatLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.inputmore.InputMoreFragment;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.tencent.qcloud.tim.uikit.utils.NetWorkUtils;
import com.tencent.qcloud.tim.uikit.utils.PermissionUtils;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.ymy.camera.JCameraView;
import com.ymy.core.lifecycle.KtxManager;
import com.ymy.core.upload.OSSManager;
import com.ymy.core.upload.UploadUtils;
import com.ymy.core.upload.UploadViewModel;
import com.ymy.core.utils.MediaFileUtil;
import com.ymy.core.utils.MediaUtils;
import com.ymy.core.utils.StringUtils;
import com.ymy.im.helper.ImHelper;
import com.ymy.image.imagepicker.loader.GlideLoader;
import com.ymy.im.signature.GenerateTestUserSig;
import com.ymy.im.utils.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.FragmentManager;

/**
 * 聊天界面，底部发送图片、拍照、摄像、文件面板
 */

public class InputLayout extends InputLayoutUI implements View.OnClickListener, TextWatcher {

    public static final int STATE_NONE_INPUT = -1;
    public static final int STATE_SOFT_INPUT = 0;
    public static final int STATE_VOICE_INPUT = 1;
    public static final int STATE_FACE_INPUT = 2;
    public static final int STATE_ACTION_INPUT = 3;
    private static final String TAG = InputLayout.class.getSimpleName();
    public static int mCurrentState;
    public static boolean isUpload = false;
    public static List<String> videos = new ArrayList<>();
    AbsChatLayout mAbsChatLayout;
    private FaceFragment mFaceFragment;
    private ChatInputHandler mChatInputHandler;
    private MessageHandler mMessageHandler;
    private FragmentManager mFragmentManager;
    private InputMoreFragment mInputMoreFragment;
    private IChatLayout mChatLayout;
    private boolean mSendEnable;
    private boolean mAudioCancel;
    private int mLastMsgLineCount;
    private float mStartRecordY;
    private String mInputContent;
    private QCloudOneSentenceRecognizer recognizer;

    public InputLayout(Context context) {
        super(context);
    }

    public InputLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InputLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void init() {

        mAudioInputSwitchButton.setOnClickListener(this);
        mEmojiInputButton.setOnClickListener(this);
        mHandleBtn.setOnClickListener(this);
        mMoreInputButton.setOnClickListener(this);
        mSendTextButton.setOnClickListener(this);
        change_input_btn.setOnClickListener(this);
        mTextInput.addTextChangedListener(this);
        mTextInput.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showSoftInput();
                return false;
            }
        });
        mTextInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return false;
            }
        });
        mTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });

        mSendAudioButton.setOnTouchListener(new OnTouchListener() {
            public boolean hasPermission = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                TUIKitLog.i(TAG, "mSendAudioButton onTouch action:" + motionEvent.getAction());
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    hasPermission = checkPermission(AUDIO_RECORD);
                    if (!hasPermission) {
                        TUIKitLog.i(TAG, "audio record checkPermission failed");
                        return false;
                    }
                }
                if (!hasPermission) {
                    return false;
                }
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mAudioCancel = true;
                        mStartRecordY = motionEvent.getY();
                        if (mChatInputHandler != null) {
                            mChatInputHandler.onRecordStatusChanged(ChatInputHandler.RECORD_START);
                        }
                        mSendAudioButton.setText("松开结束");
                        AudioPlayer.getInstance().startRecord(new AudioPlayer.Callback() {
                            @Override
                            public void onCompletion(Boolean success) {
                                recordComplete(success);
                            }
                        });
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (motionEvent.getY() - mStartRecordY < -100) {
                            mAudioCancel = true;
                            if (mChatInputHandler != null) {
                                mChatInputHandler.onRecordStatusChanged(ChatInputHandler.RECORD_CANCEL);
                            }
                        } else {
                            if (mAudioCancel) {
                                if (mChatInputHandler != null) {
                                    mChatInputHandler.onRecordStatusChanged(ChatInputHandler.RECORD_START);
                                }
                            }
                            mAudioCancel = false;
                        }
                        mSendAudioButton.setText("松开结束");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mAudioCancel = motionEvent.getY() - mStartRecordY < -100;
                        if (mChatInputHandler != null) {
                            mChatInputHandler.onRecordStatusChanged(ChatInputHandler.RECORD_STOP);
                        }
                        AudioPlayer.getInstance().stopRecord();
                        mSendAudioButton.setText("按住说话");
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        if (InputLayoutSP.INSTANCE.getInputLayoutShowType() == STATE_SOFT_INPUT) {
            mCurrentState = STATE_SOFT_INPUT;
            mSendAudioButton.setVisibility(GONE);
            mTextInput.setVisibility(VISIBLE);
        } else if (InputLayoutSP.INSTANCE.getInputLayoutShowType() == STATE_VOICE_INPUT) {
            mCurrentState = STATE_VOICE_INPUT;
            mSendAudioButton.setVisibility(VISIBLE);
            mTextInput.setVisibility(GONE);
        }
    }

    @Override
    public EditText getInputText() {
        return mTextInput;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTextInput.removeTextChangedListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void startSendPhoto() {
//        TUIKitLog.i(TAG, "startSendPhoto");
//        if (!checkPermission(SEND_PHOTO)) {
//            TUIKitLog.i(TAG, "startSendPhoto checkPermission failed");
//            return;
//        }
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
        mInputMoreFragment.setCallback(new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                try {
                    boolean isVideo = MediaFileUtil.isVideoFileType(data + "");
                    if (isVideo) {
                        dealUploadVideo(data + "");
//                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//                        mediaMetadataRetriever.setDataSource(data+"");
//                        final String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//                        final String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//                        final String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
//                        MediaUtils.getImageForVideo(data+"", new MediaUtils.OnLoadVideoImageListener() {
//                            @Override
//                            public void onLoadImage(File file,String  videoPath){
//                                if(file.exists()){
//                                    MessageInfo msg = MessageInfoUtil.buildVideoMessage(file.getPath(),videoPath,Integer.parseInt(width),Integer.parseInt(height),Integer.parseInt(time));
//                                    if (mMessageHandler != null) {
//                                        mMessageHandler.sendMessage(msg);
//                                        hideSoftInput();
//                                    }
//                                }
//                            }
//                        },width,height);
                    } else {
                        MessageInfo msg = MessageInfoUtil.buildImageMessage1(data + "", false);
                        if (mMessageHandler != null) {
                            mMessageHandler.sendMessage(msg);
                            hideSoftInput();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                TUIKitLog.i(TAG, "errCode: " + errCode);
//                ToastUtil.toastLongMessage(errMsg);
            }
        });
        ImagePicker.getInstance()
                .showImage(true) //设置是否展示图片
                .showVideo(true) //设置是否展示视频
                .filterGif(false) //设置是否过滤gif图片
                .setMaxCount(9) //设置最大选择图片数目(默认为1，单选)
                .setSingleType(false) //设置图片视频不能同时选择
                .setImagePaths(null) //设置历史选择记录
                .setImageLoader(new GlideLoader()) //设置自定义图片加载器
                .start(mInputMoreFragment, InputMoreFragment.REQUEST_CODE_PHOTO);
    }

    public synchronized void dealUploadVideo(String path) {
        if (!StringUtils.isEmpty(path)) {
            videos.add(path);
        }
        Log.e("MediaUtils", " path = " + path);
        if (isUpload) {

        } else {
            isUpload = true;
            if (videos.size() == 0) {
                return;
            }
            String data = videos.get(0);
            videos.remove(0);
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(data);
            final String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            final String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//            final String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            MediaUtils.getImageForVideo(data, new MediaUtils.OnLoadVideoImageListener() {
                @Override
                public void onLoadImage(File file, String videoPath, String width, String height) {
                    if (file.exists()) {
                        MessageInfo msg = MessageInfoUtil.buildVideoMessage(file.getPath(), videoPath, Integer.parseInt(width), Integer.parseInt(height), Integer.parseInt(time));
                        if (mMessageHandler != null) {
                            mMessageHandler.sendMessage(msg);
                            hideSoftInput();
                        }
                    }
                    Log.e("MediaUtils", "imagePath = " + file.getPath() + "  videoPath = " + videoPath);
                    isUpload = false;
                    dealUploadVideo("");
                }
            });
        }
    }

    @Override
    protected void startCapture() {
        TUIKitLog.i(TAG, "startCapture");
        if (!checkPermission(CAPTURE)) {
            TUIKitLog.i(TAG, "startCapture checkPermission failed");
            return;
        }
        ImHelper.getDBXSendReq().goToCameraActivity(getContext(), JCameraView.BUTTON_STATE_ONLY_CAPTURE,new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                Uri contentUri = Uri.fromFile(new File(data.toString()));
                MessageInfo msg = MessageInfoUtil.buildImageMessage(contentUri, true);
                if (mMessageHandler != null) {
                    mMessageHandler.sendMessage(msg);
                    hideSoftInput();
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
    }

    @Override
    protected void startVideoRecord() {
        TUIKitLog.i(TAG, "startVideoRecord");
        if (!checkPermission(VIDEO_RECORD)) {
            TUIKitLog.i(TAG, "startVideoRecord checkPermission failed");
            return;
        }
        ImHelper.getDBXSendReq().goToCameraActivity(getContext(),JCameraView.BUTTON_STATE_ONLY_RECORDER,new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                Intent videoData = (Intent) data;
                String imgPath = videoData.getStringExtra(TUIKitConstants.CAMERA_IMAGE_PATH);
                String videoPath = videoData.getStringExtra(TUIKitConstants.CAMERA_VIDEO_PATH);
                int imgWidth = videoData.getIntExtra(TUIKitConstants.IMAGE_WIDTH, 0);
                int imgHeight = videoData.getIntExtra(TUIKitConstants.IMAGE_HEIGHT, 0);
                long duration = videoData.getLongExtra(TUIKitConstants.VIDEO_TIME, 0);
                MessageInfo msg = MessageInfoUtil.buildVideoMessage(imgPath, videoPath, imgWidth, imgHeight, duration);
                if (mMessageHandler != null) {
                    mMessageHandler.sendMessage(msg);
                    hideSoftInput();
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
    }

    @Override
    protected void startSendFile() {
        TUIKitLog.i(TAG, "startSendFile");
        if (!checkPermission(SEND_FILE)) {
            TUIKitLog.i(TAG, "startSendFile checkPermission failed");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        mInputMoreFragment.setChatLayout(mChatLayout);
        mInputMoreFragment.setCallback(new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                MessageInfo info = MessageInfoUtil.buildFileMessage((Uri) data);
                if (mMessageHandler != null) {
                    mMessageHandler.sendMessage(info);
                    hideSoftInput();
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
//                ToastUtil.toastLongMessage(errMsg);
            }
        });
        mInputMoreFragment.startActivityForResult(intent, InputMoreFragment.REQUEST_CODE_FILE);
    }

    @Override
    public void startAudioCall() {
        if (!PermissionUtils.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO)) {
            TUIKitLog.i(TAG, "startAudioCall checkPermission failed");
            return;
        }
    }

    @Override
    protected void startVideoCall() {
        if (!(PermissionUtils.checkPermission(mActivity, Manifest.permission.CAMERA)
                && PermissionUtils.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO))) {
            TUIKitLog.i(TAG, "startVideoCall checkPermission failed");
            return;
        }
        if (mChatLayout.getChatInfo().getType() == V2TIMConversation.V2TIM_C2C) {
            List<UserModel> contactList = new ArrayList<>();
            UserModel model = new UserModel();
            model.userId = mChatLayout.getChatInfo().getId();
            model.userName = mChatLayout.getChatInfo().getChatName();
            model.userSig = TUIKitConfigs.getConfigs().getGeneralConfig().getUserSig();
            contactList.add(model);
            TRTCVideoCallActivity.startCallSomeone(mActivity.getApplicationContext(), contactList);
        } else {
            SelectContactActivity.start(mActivity.getApplicationContext(), mChatLayout.getChatInfo().getId(), ITRTCAVCall.TYPE_VIDEO_CALL);
        }
    }

    public void setChatInputHandler(ChatInputHandler handler) {
        this.mChatInputHandler = handler;
    }

    public void setMessageHandler(MessageHandler handler) {
        this.mMessageHandler = handler;
    }

    public void change() {
        mCurrentState = STATE_VOICE_INPUT;
        mAudioInputSwitchButton.setImageResource(R.drawable.action_textinput_selector);
        mSendAudioButton.setVisibility(VISIBLE);
        mTextInput.setVisibility(GONE);
        hideSoftInput();
    }

    @Override
    public void onClick(View view) {
        TUIKitLog.i(TAG, "onClick id:" + view.getId()
                + "|voice_input_switch:" + R.id.voice_input_switch
                + "|face_btn:" + R.id.face_btn
                + "|more_btn:" + R.id.more_btn
                + "|send_btn:" + R.id.send_btn
                + "|mCurrentState:" + mCurrentState
                + "|mSendEnable:" + mSendEnable
                + "|mMoreInputEvent:" + mMoreInputEvent);
        if (view.getId() == R.id.voice_input_switch) {
            if (mCurrentState == STATE_FACE_INPUT || mCurrentState == STATE_ACTION_INPUT) {
                mCurrentState = STATE_VOICE_INPUT;
                mInputMoreView.setVisibility(View.GONE);
                mEmojiInputButton.setImageResource(R.drawable.action_face_selector);
            } else if (mCurrentState == STATE_SOFT_INPUT) {
                mCurrentState = STATE_VOICE_INPUT;
                InputLayoutSP.INSTANCE.setInputLayoutShowType(mCurrentState);
            } else {
                mCurrentState = STATE_SOFT_INPUT;
                InputLayoutSP.INSTANCE.setInputLayoutShowType(mCurrentState);
            }
            if (mCurrentState == STATE_VOICE_INPUT) {
//                mAudioInputSwitchButton.setImageResource(R.drawable.action_textinput_selector);
                mSendAudioButton.setVisibility(VISIBLE);
                mTextInput.setVisibility(GONE);
                hideSoftInput();
            } else {
//                mAudioInputSwitchButton.setImageResource(R.drawable.action_audio_selector);
                mSendAudioButton.setVisibility(GONE);
                mTextInput.setVisibility(VISIBLE);
                showSoftInput();
            }
        } else if (view.getId() == R.id.face_btn) {
            toggleEmojiLayout();
        } else if (view.getId() == R.id.change_input_btn) {
            if (mInputMoreView.getVisibility() == View.VISIBLE) {
                mCurrentState = STATE_NONE_INPUT;
                showSoftInput();
                mInputMoreView.setVisibility(GONE);
                change_input_btn.setImageResource(R.drawable.icon_im_input_btn_biaoqing);
            } else {
                mCurrentState = STATE_VOICE_INPUT;
                toggleEmojiLayout();
                change_input_btn.setImageResource(R.drawable.ic_input_keyboard_pressed);
            }
        } else if (view.getId() == R.id.more_btn) {//若点击右边的“+”号按钮
            hideSoftInput();
            if (mMoreInputEvent instanceof View.OnClickListener) {
                ((View.OnClickListener) mMoreInputEvent).onClick(view);
            } else if (mMoreInputEvent instanceof BaseInputFragment) {
                showCustomInputMoreFragment();
            } else {
                if (mCurrentState == STATE_ACTION_INPUT) {
                    mCurrentState = STATE_NONE_INPUT;
                    //以下是zanhanding添加的代码，用于fix有时需要两次点击加号按钮才能呼出富文本选择布局的问题
                    //判断富文本选择布局是否已经被呼出，并反转相应的状态
                    if (mInputMoreView.getVisibility() == View.VISIBLE) {
                        mInputMoreView.setVisibility(View.GONE);
                    } else {
                        mInputMoreView.setVisibility(View.VISIBLE);
                    }
                    hideInputMoreLayout();
                    //以上是zanhanding添加的代码，用于fix有时需要两次点击加号按钮才能呼出富文本选择布局的问题
                } else {
                    showInputMoreLayout();//显示“更多”消息发送布局
                    mCurrentState = STATE_ACTION_INPUT;
                    mAudioInputSwitchButton.setImageResource(R.drawable.action_audio_selector);
                    mEmojiInputButton.setImageResource(R.drawable.action_face_selector);
                    mSendAudioButton.setVisibility(GONE);
                    mTextInput.setVisibility(VISIBLE);
                }
            }
        } else if (view.getId() == R.id.send_btn) {
            if (mSendEnable) {
                if (mMessageHandler != null) {
                    mMessageHandler.sendMessage(MessageInfoUtil.buildTextMessage(mTextInput.getText().toString().trim()));
                }
                mTextInput.setText("");
            }
        } else if (view.getId() == R.id.handle_btn) {
            if (StringUtils.isEmpty(ImHelper.eventId)) {
                toggleEmojiLayout();
            } else {
//                if (EventType.TYPE_ZKJJ.equals(ImHelper.event_type)) {
//                    if (mAbsChatLayout != null) {
//                        mAbsChatLayout.showJiaoJieState();
//                    }
//
//                } else {
//                    ImHelper.getDBXSendReq().sendHandleEventAction(ImHelper.eventId);
//                }
            }
        }
    }

    public void toggleEmojiLayout() {
        if (mCurrentState == STATE_VOICE_INPUT) {
            mCurrentState = STATE_NONE_INPUT;
            mAudioInputSwitchButton.setImageResource(R.drawable.action_audio_selector);
            mSendAudioButton.setVisibility(GONE);
            mTextInput.setVisibility(VISIBLE);
        }
        if (mCurrentState == STATE_FACE_INPUT) {
            mCurrentState = STATE_NONE_INPUT;
            mInputMoreView.setVisibility(View.GONE);
            mEmojiInputButton.setImageResource(R.drawable.action_face_selector);
            mTextInput.setVisibility(VISIBLE);
        } else {
            mCurrentState = STATE_FACE_INPUT;
            mEmojiInputButton.setImageResource(R.drawable.action_textinput_selector);
            showFaceViewGroup();
        }
    }

    private void showSoftInput() {
        TUIKitLog.v(TAG, "showSoftInput");
        hideInputMoreLayout();
        mAudioInputSwitchButton.setImageResource(R.drawable.action_audio_selector);
        mEmojiInputButton.setImageResource(R.drawable.ic_input_face_normal);
        mTextInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTextInput, 0);
        if (mChatInputHandler != null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChatInputHandler.onInputAreaClick();
                }
            }, 200);
        }
    }

    public void hideSoftInput() {
        TUIKitLog.i(TAG, "hideSoftInput");
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTextInput.getWindowToken(), 0);
        mTextInput.clearFocus();
        mInputMoreView.setVisibility(View.GONE);
    }

    private void showFaceViewGroup() {
        TUIKitLog.i(TAG, "showFaceViewGroup");
        if (mFragmentManager == null) {
            mFragmentManager = mActivity.getSupportFragmentManager();
        }
        if (mFaceFragment == null) {
            mFaceFragment = new FaceFragment();
        }
        hideSoftInput();
        mInputMoreView.setVisibility(View.VISIBLE);
        mTextInput.requestFocus();
        mFaceFragment.setListener(new FaceFragment.OnEmojiClickListener() {
            @Override
            public void onEmojiDelete() {
                int index = mTextInput.getSelectionStart();
                Editable editable = mTextInput.getText();
                boolean isFace = false;
                if (index <= 0) {
                    return;
                }
                if (editable.charAt(index - 1) == ']') {
                    for (int i = index - 2; i >= 0; i--) {
                        if (editable.charAt(i) == '[') {
                            String faceChar = editable.subSequence(i, index).toString();
                            if (FaceManager.isFaceChar(faceChar)) {
                                editable.delete(i, index);
                                isFace = true;
                            }
                            break;
                        }
                    }
                }
                if (!isFace) {
                    editable.delete(index - 1, index);
                }
            }

            @Override
            public void onEmojiClick(Emoji emoji) {
                int index = mTextInput.getSelectionStart();
                Editable editable = mTextInput.getText();
                editable.insert(index, emoji.getFilter());
                FaceManager.handlerEmojiText(mTextInput, editable.toString(), true);
            }

            @Override
            public void onCustomFaceClick(int groupIndex, Emoji emoji) {
                mMessageHandler.sendMessage(MessageInfoUtil.buildCustomFaceMessage(groupIndex, emoji.getFilter()));
            }
        });
        mFragmentManager.beginTransaction().replace(R.id.more_groups, mFaceFragment).commitAllowingStateLoss();
        if (mChatInputHandler != null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChatInputHandler.onInputAreaClick();
                }
            }, 100);
        }
    }

    private void showCustomInputMoreFragment() {
        TUIKitLog.i(TAG, "showCustomInputMoreFragment");
        if (mFragmentManager == null) {
            mFragmentManager = mActivity.getSupportFragmentManager();
        }
        BaseInputFragment fragment = (BaseInputFragment) mMoreInputEvent;
        hideSoftInput();
        mInputMoreView.setVisibility(View.VISIBLE);
        mFragmentManager.beginTransaction().replace(R.id.more_groups, fragment).commitAllowingStateLoss();
        if (mChatInputHandler != null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChatInputHandler.onInputAreaClick();
                }
            }, 100);
        }
    }

    private void showInputMoreLayout() {
        TUIKitLog.i(TAG, "showInputMoreLayout");
        if (mFragmentManager == null) {
            mFragmentManager = mActivity.getSupportFragmentManager();
        }
        if (mInputMoreFragment == null) {
            mInputMoreFragment = new InputMoreFragment();
        }

        assembleActions();
        mInputMoreFragment.setActions(mInputMoreActionList);
        hideSoftInput();
        mInputMoreView.setVisibility(View.VISIBLE);
        mFragmentManager.beginTransaction().replace(R.id.more_groups, mInputMoreFragment).commitAllowingStateLoss();
        if (mChatInputHandler != null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChatInputHandler.onInputAreaClick();
                }
            }, 100);
        }
    }

    private void hideInputMoreLayout() {
        mInputMoreView.setVisibility(View.GONE);
    }

    private void recordComplete(boolean success) {
        final int duration = AudioPlayer.getInstance().getDuration();
        TUIKitLog.i(TAG, "recordComplete duration:" + duration);
        if (mChatInputHandler != null) {
            if (!success || duration == 0) {
                mChatInputHandler.onRecordStatusChanged(ChatInputHandler.RECORD_FAILED);
                return;
            }
            if (mAudioCancel) {
                mChatInputHandler.onRecordStatusChanged(ChatInputHandler.RECORD_CANCEL);
                return;
            }
            if (duration < 1000) {
                mChatInputHandler.onRecordStatusChanged(ChatInputHandler.RECORD_TOO_SHORT);
                return;
            }
            mChatInputHandler.onRecordStatusChanged(ChatInputHandler.RECORD_STOP);
        }

        if (mMessageHandler != null && success) {
            discernVoice(mMessageHandler, duration, AudioPlayer.getInstance().getPath());
        }
    }

    /**
     * 上传并发送
     *
     * @param mMessageHandler
     * @param discdernResult
     */
    private void upload(final MessageHandler mMessageHandler, final String discdernResult) {
        if (recognizer != null) {
            recognizer.clear();
            recognizer = null;
        }
        ArrayList<String> list = new ArrayList<>();
        list.add(AudioPlayer.getInstance().getPath());
        Log.e("INPUT_INFO", "LOCAL_VOICE = " + AudioPlayer.getInstance().getPath());
        UploadViewModel mUploadViewModel = new UploadViewModel();
        mUploadViewModel.uploadFile(
                list,
                UploadUtils.getUploadFilePath(OSSManager.imFolder, "alarm"), new UploadViewModel.CallBack() {
                    @Override
                    public void onSuccess(@NotNull String ossTag, @NotNull ArrayList<String> resultList) {
                        //上传成功的urls
                        ArrayList<String> showSuccessList = resultList;
                        if (showSuccessList != null && showSuccessList.size() > 0) {
                            Log.e("INPUT_INFO", "duration = " + AudioPlayer.getInstance().getDuration());
                            Gson gson = new Gson();
                            MessageCustom customAudioMessage = new MessageCustom();
                            customAudioMessage.setDiscernResult(discdernResult);
                            customAudioMessage.setDuration(AudioPlayer.getInstance().getDuration() / 1000);
                            customAudioMessage.setLocalPath(AudioPlayer.getInstance().getPath());
                            customAudioMessage.setRemoteUrl(showSuccessList.get(0));
                            String data = gson.toJson(customAudioMessage);
                            Log.e("INPUT_INFO", "MessageInfo = " + data);
                            mMessageHandler.sendMessage(MessageInfoUtil.buildCustomAudioMessage(data));
                        }
                    }

                    @Override
                    public void showLoading(boolean show) {
                        // TODO: 2020/8/27 true展示loading 或 false去掉loading
                    }

                    @Override
                    public void onError(@NotNull String errorMsg) {
                        MessageCustom customAudioMessage = new MessageCustom();
                        Gson gson = new Gson();
                        customAudioMessage.setDiscernResult(discdernResult);
                        customAudioMessage.setDuration(AudioPlayer.getInstance().getDuration() / 1000);
                        customAudioMessage.setLocalPath(AudioPlayer.getInstance().getPath());
                        customAudioMessage.setRemoteUrl("");
                        String data = gson.toJson(customAudioMessage);
                        Log.e("INPUT_INFO", "MessageInfo = " + data);
                        mMessageHandler.sendMessage(MessageInfoUtil.buildCustomAudioMessage(data));
                    }
                });
    }

    /**
     * 开始识别
     *
     * @param mMessageHandler
     * @param path
     */
    private void discernVoice(final MessageHandler mMessageHandler, final int duration, final String path) {
        InputStream is = null;
        try {
            if (!NetWorkUtils.IsNetWorkEnable(KtxManager.getCurrentActivity())) {
                upload(mMessageHandler, "");
                return;
            }
            if (recognizer == null) {
                recognizer = new QCloudOneSentenceRecognizer((AppCompatActivity) mActivity, GenerateTestUserSig.apppId, GenerateTestUserSig.secretId, GenerateTestUserSig.secretKey);
                recognizer.setCallback(new QCloudOneSentenceRecognizerListener() {
                    @Override
                    public void didStartRecord() {

                    }

                    @Override
                    public void didStopRecord() {

                    }

                    @Override
                    public void recognizeResult(QCloudOneSentenceRecognizer qCloudOneSentenceRecognizer, String s, Exception e) {
                        Log.e("recognizeResult", "thread id:" + Thread.currentThread().getId() + " name:" + Thread.currentThread().getName());
                        try {
                            DiscernResult messageCustom = new Gson().fromJson(s, DiscernResult.class);
                            upload(mMessageHandler, messageCustom.getResponse().getResult());
                            return;
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        // 语音发送异常
                    }
                });
            }
            File f = new File(path);
            is = new FileInputStream(f);
            int length = is.available();
            byte[] audioData = new byte[length];
            is.read(audioData);
            QCloudOneSentenceRecognitionParams params = (QCloudOneSentenceRecognitionParams) QCloudOneSentenceRecognitionParams.defaultRequestParams();
            params.setFilterDirty(0);// 0 ：默认状态 不过滤脏话 1：过滤脏话
            params.setFilterModal(0);// 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
            params.setFilterPunc(0); // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
            params.setConvertNumMode(1);//1：默认状态 根据场景智能转换为阿拉伯数字；0：全部转为中文数字。
//                    params.setHotwordId(""); // 热词id。用于调用对应的热词表，如果在调用语音识别服务时，不进行单独的热词id设置，自动生效默认热词；如果进行了单独的热词id设置，那么将生效单独设置的热词id。
            params.setData(audioData);
            params.setVoiceFormat(QCloudAudioFormat.QCloudAudioFormatMp3);
            params.setSourceType(QCloudSourceType.QCloudSourceTypeData);
            params.setEngSerViceType(QCloudAudioFrequence.QCloudAudioFrequence16k.getFrequence());
            recognizer.recognize(params);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception msg" + e.getMessage());
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mInputContent = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s.toString().trim())) {
            mSendEnable = false;
            showSendTextButton(View.GONE);
            showMoreInputButton(View.VISIBLE);
        } else {
            mSendEnable = true;
            showSendTextButton(View.VISIBLE);
            showMoreInputButton(View.GONE);
            if (mTextInput.getLineCount() != mLastMsgLineCount) {
                mLastMsgLineCount = mTextInput.getLineCount();
                if (mChatInputHandler != null) {
                    mChatInputHandler.onInputAreaClick();
                }
            }
            if (!TextUtils.equals(mInputContent, mTextInput.getText().toString())) {
                FaceManager.handlerEmojiText(mTextInput, mTextInput.getText().toString(), true);
            }
        }
    }

    public void setAbsChatLayout(AbsChatLayout absChatLayout) {
        mAbsChatLayout = absChatLayout;
    }

    public void setChatLayout(IChatLayout chatLayout) {
        mChatLayout = chatLayout;
    }

    public void showFunctionBar(ArrayList<FunctionBtnData> data) {
        mLayoutInput.setVisibility(View.GONE);
        mLayoutFunctionBar.setVisibility(View.VISIBLE);
        mLayoutFunctionBar.removeAllViews();
        for (FunctionBtnData info : data) {
            Context context = mLayoutFunctionBar.getContext();
            TextView textView = new TextView(context);
            textView.setText(info.text);
            textView.setTextSize(16f);
            textView.setTextColor(getContext().getResources().getColor(R.color.black00102e));
            textView.setGravity(Gravity.CENTER);
            textView.setTag(info);
//            textView.setBackground(getContext().getDrawable(R.drawable.msg_editor_border));
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();
                    if (tag instanceof FunctionBtnData) {
                        FunctionBtnData invokeData = (FunctionBtnData) tag;
                        switch (invokeData.invokeType) {
                            case FunctionBtnData.INVOKE_TYPE_WEB: {
                                ImHelper.getDBXSendReq().goToWebActivity(invokeData.url);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
            });
            LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            int left = DensityUtil.dip2pxX(6f);
            layoutParams.setMargins(left, 0, left, 0);
            textView.setLayoutParams(layoutParams);
            mLayoutFunctionBar.addView(textView);
        }
    }

    public interface MessageHandler {
        void sendMessage(MessageInfo msg);
    }

    public interface ChatInputHandler {

        int RECORD_START = 1;
        int RECORD_STOP = 2;
        int RECORD_CANCEL = 3;
        int RECORD_TOO_SHORT = 4;
        int RECORD_FAILED = 5;

        void onInputAreaClick();

        void onRecordStatusChanged(int status);
    }
}
