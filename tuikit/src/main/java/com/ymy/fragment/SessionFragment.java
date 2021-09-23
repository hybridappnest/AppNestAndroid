package com.ymy.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.ymy.activity.SelectSessionActivity;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.BaseFragment;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationLayout;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.ymy.core.base.Refresher;


/**
 * @author hanxueqiang
 */
public class SessionFragment extends BaseFragment implements Refresher {

    public SelectSessionActivity.SelectSessionCallBack mSelectSessionCallBack;
    private View mBaseView;
    private ConversationLayout mConversationLayout;

    public void setSelectSessionCallBack(SelectSessionActivity.SelectSessionCallBack mSelectSessionCallBack) {
        this.mSelectSessionCallBack = mSelectSessionCallBack;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.conversation_fragment, container, false);
        initView();
        return mBaseView;
    }

    private void initView() {
        if (mBaseView == null) {
            return;
        }
        // 从布局文件中获取会话列表面板
        mConversationLayout = mBaseView.findViewById(R.id.conversation_layout);
        mConversationLayout.initSession();
        // 通过API设置ConversataonLayout各种属性的样例，开发者可以打开注释，体验效果
        mConversationLayout.getConversationList().setOnItemClickListener(new ConversationListLayout.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, ConversationInfo conversationInfo) {
                //此处为demo的实现逻辑，更根据会话类型跳转到相关界面，开发者可根据自己的应用场景灵活实现
                if (SelectSessionActivity.mDataSource != null && SelectSessionActivity.mDataSource.size() > 0) {
                    if (mSelectSessionCallBack != null) {
                        mSelectSessionCallBack.selectSession(conversationInfo);
                    }
                    return;
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        initView();
    }
}
