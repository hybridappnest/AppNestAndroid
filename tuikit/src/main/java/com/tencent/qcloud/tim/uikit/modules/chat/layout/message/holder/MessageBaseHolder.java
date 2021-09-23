package com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageLayoutUI;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageListAdapter;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;

import androidx.recyclerview.widget.RecyclerView;

public abstract class MessageBaseHolder extends RecyclerView.ViewHolder {

    public MessageListAdapter mAdapter;
    public RecyclerView recyclerView;
    public MessageLayoutUI.Properties properties = MessageLayout.Properties.getInstance();
    protected View rootView;
    protected MessageLayout.OnItemClickListener onItemClickListener;

    public MessageBaseHolder(View itemView) {
        super(itemView);

        rootView = itemView;
    }

    public void setRecyclerView(RecyclerView view) {
        recyclerView = view;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = (MessageListAdapter) adapter;
    }

    public void setOnItemClickListener(MessageLayout.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public abstract void layoutViews(final MessageInfo msg, final int position);

    public static class Factory {

        public static RecyclerView.ViewHolder getInstance(ViewGroup parent, RecyclerView.Adapter adapter, RecyclerView recyclerView, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(TUIKit.getAppContext());
            RecyclerView.ViewHolder holder = null;
            View view = null;

            // 头部的holder
            if (viewType == MessageListAdapter.MSG_TYPE_HEADER_VIEW) {
                view = inflater.inflate(R.layout.message_adapter_content_header, parent, false);
                holder = new MessageHeaderHolder(view);
                return holder;
            }

            // 加群消息等holder
            if (viewType >= MessageInfo.MSG_TYPE_TIPS) {
                view = inflater.inflate(R.layout.message_adapter_item_empty, parent, false);
                holder = new MessageTipsHolder(view);
            }
            // 具体消息holder
            if (viewType == MessageInfo.MSG_TYPE_CUSTOM_BAOJING) {
                view = inflater.inflate(R.layout.message_adapter_item_content_dbx, parent, false);
            } else if (viewType == MessageInfo.MSG_TYPE_FUNCTION_CUSTOM) {
                view = inflater.inflate(R.layout.message_adapter_item_empty, parent, false);
            } else {
                view = inflater.inflate(R.layout.message_adapter_item_content, parent, false);
            }
            switch (viewType) {
                case MessageInfo.MSG_TYPE_TEXT:
                    holder = new MessageTextHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_CUSTOM_TEXT:
                    holder = new MessageSystemTextHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_IMAGE:
                case MessageInfo.MSG_TYPE_VIDEO:
                case MessageInfo.MSG_TYPE_CUSTOM_FACE:
                    holder = new MessageImageHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_AUDIO:
                case MessageInfo.MSG_TYPE_AUDIO_LEFT:
                    holder = new MessageAudioHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_AUDIO_CUSTOM:
                    holder = new MessageAudioDBXHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_FILE:
                    holder = new MessageFileHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_CUSTOM:
                    holder = new MessageCustomHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_CUSTOM_BAOJING:
                    holder = new MessageCustomDBXHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_FUNCTION_CUSTOM:
                    holder = new MessageFunctionHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_NORMAl_FUNCTION_CUSTOM:
                    holder = new MessageFunctionInChatHolder(view);
                    break;
                default:
                    break;
            }
            if (holder != null) {
                ((MessageEmptyHolder) holder).setAdapter(adapter);
                ((MessageEmptyHolder) holder).setRecyclerView(recyclerView);
            }

            return holder;
        }
    }
}
