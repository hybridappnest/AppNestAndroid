package com.tencent.qcloud.tim.uikit.modules.conversation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.qcloud.tim.uikit.modules.conversation.holder.ConversationBaseHolder;
import com.tencent.qcloud.tim.uikit.modules.conversation.holder.ConversationCommonHolder;
import com.tencent.qcloud.tim.uikit.modules.conversation.interfaces.IConversationAdapter;
import com.tencent.qcloud.tim.uikit.modules.conversation.interfaces.IConversationProvider;
import com.tencent.qcloud.tim.uikit.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ConversationListAdapter extends IConversationAdapter {

    private boolean mHasShowUnreadDot = true;
    private int mItemAvatarRadius = ScreenUtil.getPxByDp(5);
    private int mTopTextSize;
    private int mBottomTextSize;
    private int mDateTextSize;
    private List<ConversationInfo> mDataSource = new ArrayList<>();
    private ConversationListLayout.OnItemClickListener mOnItemClickListener;
    private ConversationListLayout.OnItemLongClickListener mOnItemLongClickListener;

    public void setKeyWord(List<ConversationInfo> source, IConversationProvider provider) {
        mDataSource.clear();
//        if (provider instanceof ConversationProvider) {
//            provider.attachAdapter(this);
//        }
        if(source != null){
            mDataSource.addAll(source);
            notifyDataSetChanged();
        }

    }

    public void setOnItemClickListener(ConversationListLayout.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(ConversationListLayout.OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

//    public void setDataSource( List<ConversationInfo> source){
//        HashMap<String,ConversationInfo> currentId = new HashMap<>();
//        for(ConversationInfo conversationInfo :source){//新群的所有ID
//            currentId.put(conversationInfo.getConversationId(),conversationInfo);
//        }
//        HashMap<String,ConversationInfo> delConversation = new HashMap<>();
//        for(ConversationInfo info:mDataSource){//获取移除会话ID
//            if(!currentId.containsKey(info.getConversationId())){
//                delConversation.put(info.getConversationId(),null);
//            }
//        }
//        Iterator<ConversationInfo> infoIterator = mDataSource.iterator();
//        while (infoIterator.hasNext()){// 删除已移除的会话
//            ConversationInfo conversationInfo = infoIterator.next();
//            if(delConversation.containsKey(conversationInfo.getConversationId())){
//                infoIterator.remove();
//            }
//        }
//        int length = mDataSource.size();
//        for(int i= 0; i<length;i++){
//            mDataSource.set(i,source.get(i));
//        }
//        if(source.size()>length){
//            List<ConversationInfo> temp = source.subList(length,source.size());
//            mDataSource.addAll(temp);
//        }
//        mDataSource.clear();
//        mDataSource.addAll(source);
//        notifyDataSetChanged();
//    }

    public void refreshDataSource(List<ConversationInfo> newDataSource){
        mDataSource.clear();
        mDataSource.addAll(newDataSource);
        notifyDataSetChanged();
    }

    @Override
    public void setDataProvider(IConversationProvider provider) {
        List<ConversationInfo> newDataSource = provider.getDataSource();
        if (provider instanceof ConversationProvider) {
            provider.attachAdapter(this);
        }
        mDataSource.clear();
        mDataSource.addAll(newDataSource);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(TUIKit.getAppContext());
        RecyclerView.ViewHolder holder = null;
        // 创建不同的 ViewHolder
        View view;
        // 根据ViewType来创建条目
//        if (viewType == ConversationInfo.TYPE_CUSTOM) {
//            view = inflater.inflate(R.layout.conversation_custom_adapter, parent, false);
//            holder = new ConversationCustomHolder(view);
//        } else {
            view = inflater.inflate(R.layout.conversation_adapter, parent, false);
            holder = new ConversationCommonHolder(view);
//        }
        if (holder != null) {
            ((ConversationBaseHolder) holder).setAdapter(this);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ConversationInfo conversationInfo = getItem(position);
        ConversationBaseHolder baseHolder = (ConversationBaseHolder) holder;

        switch (getItemViewType(position)) {
            case ConversationInfo.TYPE_CUSTOM:
                break;
            default:
                //设置点击和长按事件
                if (mOnItemClickListener != null) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnItemClickListener.onItemClick(view, position, conversationInfo);
                        }
                    });
                }
                if (mOnItemLongClickListener != null) {
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            mOnItemLongClickListener.OnItemLongClick(view, position, conversationInfo);
                            return true;
                        }
                    });
                }
                break;
        }
        baseHolder.layoutViews(conversationInfo, position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof ConversationCommonHolder) {
//            ((ConversationCommonHolder) holder).conversationIconView.setBackground(null);
        }
    }

    @Override
    public ConversationInfo getItem(int position) {
        if (mDataSource.size() == 0) {
            return null;
        }
        return mDataSource.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataSource != null) {
            ConversationInfo conversation = mDataSource.get(position);
            return conversation.getType();
        }
        return 1;
    }

    public void addItem(int position, ConversationInfo info) {
        mDataSource.add(position, info);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mDataSource.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void notifyDataSourceChanged(String info) {
        for (int i = 0; i < mDataSource.size(); i++) {
            if (TextUtils.equals(info, mDataSource.get(i).getConversationId())) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    public int getItemTopTextSize() {
        return mTopTextSize;
    }

    public void setItemTopTextSize(int size) {
        mTopTextSize = size;
    }

    public int getItemBottomTextSize() {
        return mBottomTextSize;
    }

    public void setItemBottomTextSize(int size) {
        mBottomTextSize = size;
    }

    public int getItemDateTextSize() {
        return mDateTextSize;
    }

    public void setItemDateTextSize(int size) {
        mDateTextSize = size;
    }

    public int getItemAvatarRadius() {
        return mItemAvatarRadius;
    }

    public void setItemAvatarRadius(int radius) {
        mItemAvatarRadius = radius;
    }

    public void disableItemUnreadDot(boolean flag) {
        mHasShowUnreadDot = !flag;
    }

    public boolean hasItemUnreadDot() {
        return mHasShowUnreadDot;
    }
}
