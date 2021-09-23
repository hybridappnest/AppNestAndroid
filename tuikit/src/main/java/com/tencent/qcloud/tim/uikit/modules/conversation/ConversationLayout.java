package com.tencent.qcloud.tim.uikit.modules.conversation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.qcloud.tim.uikit.modules.conversation.interfaces.IConversationLayout;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ConversationLayout extends RelativeLayout implements IConversationLayout {

    int currentPosition = 0;
    private ConversationListLayout mConversationList;

    public ConversationLayout(Context context) {
        super(context);
        init();
    }

    public ConversationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConversationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化相关UI元素
     */
    private void init() {
        inflate(getContext(), R.layout.conversation_layout, this);
        mConversationList = findViewById(R.id.conversation_list);
        mConversationList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (recyclerView != null && recyclerView.getChildCount() > 0) {
                    try {
                        currentPosition = ((RecyclerView.LayoutParams) recyclerView.getChildAt(0).getLayoutParams()).getViewAdapterPosition();
                    } catch (Exception e) {
                    }
                }

            }

        });
    }

    private ConversationProvider getConversationProvider() {
        return ConversationManagerKit.getInstance().getProvider();
    }

    public void onResume() {
        if (mConversationList.getAdapter() != null) {
            ConversationListAdapter adapter = mConversationList.getAdapter();
            ConversationProvider conversationProvider = getConversationProvider();
            conversationProvider.attachAdapter(adapter);
        }
    }

    public void initDefault() {
        final ConversationListAdapter adapter = new ConversationListAdapter();
        mConversationList.setAdapter(adapter);
        try {
            ConversationProvider conversationProvider = getConversationProvider();
            List<ConversationInfo> list = conversationProvider.getDataSource();
            adapter.setDataProvider(conversationProvider);
            if (list != null && list.size() > 0) {
                if (list.size() == 0) {
//                    findViewById(R.id.noneData).setVisibility(VISIBLE);
//                    mConversationList.setVisibility(GONE);
                } else {
//                    findViewById(R.id.noneData).setVisibility(GONE);
                    mConversationList.setVisibility(VISIBLE);
                    try {
                        mConversationList.scrollToPosition(currentPosition);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
//                findViewById(R.id.noneData).setVisibility(VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initSession() {
        final ConversationListAdapter adapter = new ConversationListAdapter();
        mConversationList.setAdapter(adapter);
        try {
            ConversationProvider conversationProvider = getConversationProvider();
            List<ConversationInfo> list = conversationProvider.getDataSource();
            adapter.refreshDataSource(list);
            if (list != null && list.size() > 0) {
                if (list.size() == 0) {
//                    findViewById(R.id.noneData).setVisibility(VISIBLE);
//                    mConversationList.setVisibility(GONE);
                } else {
                    mConversationList.setVisibility(VISIBLE);
                    try {
                        mConversationList.scrollToPosition(currentPosition);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public TitleBarLayout getTitleBar() {
        return null;
    }

    @Override
    public void setParentLayout(Object parent) {

    }

    @Override
    public ConversationListLayout getConversationList() {
        return mConversationList;
    }

    public void addConversationInfo(int position, ConversationInfo info) {
        mConversationList.getAdapter().addItem(position, info);
    }

    public void removeConversationInfo(int position) {
        mConversationList.getAdapter().removeItem(position);
    }

    @Override
    public void setConversationTop(int position, ConversationInfo conversation) {
        ConversationManagerKit.getInstance().setConversationTop(position, conversation);
    }

    @Override
    public void deleteConversation(int position, ConversationInfo conversation) {
        ConversationManagerKit.getInstance().deleteConversation(position, conversation);
    }
}
