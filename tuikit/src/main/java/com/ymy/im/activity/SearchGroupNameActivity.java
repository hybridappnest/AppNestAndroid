package com.ymy.im.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationListAdapter;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationProvider;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.base.RootActivity;
import com.ymy.core.utils.StringUtils;
import com.ymy.im.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class SearchGroupNameActivity extends RootActivity implements View.OnClickListener, TextWatcher, TextView.OnEditorActionListener, View.OnFocusChangeListener {

    private static final String TAG = SearchGroupNameActivity.class.getSimpleName();
    private CharSequence keyword ="";
    private EditText etSearch;
    private TextView mBtnCancel;
    private ImageView mIvEditDelete;
    private ConversationListLayout mListView;
    private TextView noneData;
     ConversationListAdapter adapter = null;
    public static void invoke(Context context){
        context.startActivity(new Intent(context, SearchGroupNameActivity.class));
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_group_list_activity);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void init() {
        etSearch = (EditText) findViewById(R.id.search_edit);
        mBtnCancel = (TextView) findViewById(R.id.search_cancel);
        mIvEditDelete = (ImageView) findViewById(R.id.search_edit_delete);
        noneData =findViewById(R.id.noneData);
        mBtnCancel.setOnClickListener(this);
        mIvEditDelete.setOnClickListener(this);
        etSearch.addTextChangedListener(this);
        etSearch.setOnEditorActionListener(this);
        etSearch.setOnFocusChangeListener(this);
        mListView = findViewById(R.id.conversation_list);
        adapter = new ConversationListAdapter();
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new ConversationListLayout.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, ConversationInfo conversationInfo) {
                //此处为demo的实现逻辑，更根据会话类型跳转到相关界面，开发者可根据自己的应用场景灵活实现
                startChatActivity(conversationInfo);
            }
        });
    }

    public void loadDataSource() {
        ConversationManagerKit.getInstance().loadConversation(new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if(data != null && data instanceof ConversationProvider){
                    List<ConversationInfo> mDataSource = ((ConversationProvider) data).getDataSource();
                    List<ConversationInfo> source = new ArrayList<>();
                    for(ConversationInfo info:mDataSource){
                        if(keyword.toString()!=null&&keyword.toString().length()>0&&info.getTitle().contains(keyword.toString())){
                            source.add(info);
                        }
                    }
                    if(source.size() == 0){
                        noneData.setVisibility(View.VISIBLE);
                    }else{
                        noneData.setVisibility(View.GONE);
                    }
                    adapter.setKeyWord(source,(ConversationProvider) data);
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ToastUtil.toastLongMessage("加载消息失败");
            }
        });
    }
    private void startChatActivity(ConversationInfo conversationInfo) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(conversationInfo.isGroup() ? V2TIMConversation.V2TIM_GROUP : V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(conversationInfo.getId());
        chatInfo.setChatName(conversationInfo.getTitle());
        chatInfo.setGroupType(conversationInfo.isGroup()?conversationInfo.getEvent_group_type():"");
        Intent intent = new Intent(TUIKit.getAppContext(), ImChatActivity.class);
        intent.putExtra(Constants.CHAT_INFO, chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TUIKit.getAppContext().startActivity(intent);
    }
    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.search_cancel) {
            etSearch.setText("");
            onClickCancel();
            clearEditFocus();
        } else if (id == R.id.search_edit_delete) {
            etSearch.setText("");
            adapter.setKeyWord(null,null);
        }
    }
    public void clearEditFocus() {
        etSearch.clearFocus();
    }
    public void onClickCancel() {
        finish();
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        keyword = s;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (StringUtils.isEmpty(keyword)) {
            mIvEditDelete.setVisibility(View.INVISIBLE);
        } else {
            mIvEditDelete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_SEARCH:
            case EditorInfo.IME_ACTION_SEND:
            case EditorInfo.IME_ACTION_GO:
            case EditorInfo.IME_ACTION_DONE:
            case EditorInfo.IME_ACTION_UNSPECIFIED:
                startSearch(v);
                return true;
            default:
                return false;
        }
    }
    private void startSearch(TextView v) {
        InputMethodManager imm = (InputMethodManager) v.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        if (TextUtils.isEmpty(keyword)) {
            try {
                keyword = v.getHint().toString().split(":")[1];
            } catch (Exception e) {
                return;
            }
        }
        etSearch.setSelection(keyword.length());
        loadDataSource();
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();

        if (id == R.id.search_edit) { //搜索框
            if (!hasFocus) {
                hideKeyboard();
            }
        }
    }
    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        InputMethodManager im = (InputMethodManager) getSystemService(Context
                .INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(etSearch.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
