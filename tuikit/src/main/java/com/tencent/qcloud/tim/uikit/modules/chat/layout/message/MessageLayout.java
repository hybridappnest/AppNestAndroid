package com.tencent.qcloud.tim.uikit.modules.chat.layout.message;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ymy.im.function.Func;
import com.ymy.im.helper.ImHelper;
import com.tencent.qcloud.tim.uikit.component.PopupList;
import com.tencent.qcloud.tim.uikit.component.action.PopActionClickListener;
import com.tencent.qcloud.tim.uikit.component.action.PopMenuAction;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.ymy.core.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


public class MessageLayout extends MessageLayoutUI {

    public static final int DATA_CHANGE_TYPE_REFRESH = 0;
    public static final int DATA_CHANGE_TYPE_LOAD = 1;
    public static final int DATA_CHANGE_TYPE_ADD_FRONT = 2;
    public static final int DATA_CHANGE_TYPE_ADD_BACK = 3;
    public static final int DATA_CHANGE_TYPE_UPDATE = 4;
    public static final int DATA_CHANGE_TYPE_DELETE = 5;
    public static final int DATA_CHANGE_TYPE_CLEAR = 6;


    public MessageLayout(Context context) {
        super(context);
    }

    public MessageLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageLayout(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            View child = findChildViewUnder(e.getX(), e.getY());
            if (child == null) {
                if (mEmptySpaceClickListener != null)
                    mEmptySpaceClickListener.onClick();
            } else if (child instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) child;
                final int count = group.getChildCount();
                float x = e.getRawX();
                float y = e.getRawY();
                View touchChild = null;
                for (int i = count - 1; i >= 0; i--) {
                    final View innerChild = group.getChildAt(i);
                    int position[] = new int[2];
                    innerChild.getLocationOnScreen(position);
                    if (x >= position[0]
                            && x <= position[0] + innerChild.getMeasuredWidth()
                            && y >= position[1]
                            && y <= position[1] + innerChild.getMeasuredHeight()) {
                        touchChild = innerChild;
                        break;
                    }
                }
                if (touchChild == null) {
                    if (mEmptySpaceClickListener != null) {
                        mEmptySpaceClickListener.onClick();
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    public void showItemPopMenu(final int index, final MessageInfo messageInfo, View view) {
        initPopActions(messageInfo);
        if (mPopActions.size() == 0) {
            return;
        }

        final PopupList popupList = new PopupList(getContext());
        List<String> mItemList = new ArrayList<>();
        for (PopMenuAction action : mPopActions) {
            mItemList.add(action.getActionName());
        }
        popupList.show(view, mItemList, new PopupList.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                PopMenuAction action = mPopActions.get(position);
                if (action.getActionClickListener() != null) {
                    action.getActionClickListener().onActionClick(index, messageInfo);
                }
            }
        });
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (popupList != null) {
                    popupList.hidePopupListWindow();
                }
            }
        }, 10000); // 10s后无操作自动消失
    }

    private void initPopActions(final MessageInfo msg) {
        if (msg == null) {
            return;
        }
        List<PopMenuAction> actions = new ArrayList<>();
        PopMenuAction action = new PopMenuAction();
        if (msg.getMsgType() == MessageInfo.MSG_TYPE_TEXT) {
            action.setActionName("复制");
            action.setActionClickListener(new PopActionClickListener() {
                @Override
                public void onActionClick(int position, Object data) {
                    mOnPopActionClickListener.onCopyClick(position, (MessageInfo) data);
                }
            });
            actions.add(action);
        }
        if (msg.getMsgType() != MessageInfo.MSG_TYPE_CUSTOM_BAOJING) {
            action = new PopMenuAction();
            action.setActionName("转发");
            action.setActionClickListener(new PopActionClickListener() {
                @Override
                public void onActionClick(int position, Object data) {
                    mOnPopActionClickListener.onRepeatClick(position, (MessageInfo) data);
                }
            });
            actions.add(action);
        }
        action = new PopMenuAction();
        action.setActionName("删除");
        action.setActionClickListener(new PopActionClickListener() {
            @Override
            public void onActionClick(int position, Object data) {
                mOnPopActionClickListener.onDeleteMessageClick(position, (MessageInfo) data);
            }
        });
        actions.add(action);
        action = new PopMenuAction();
        action.setActionName("多选");
        action.setActionClickListener(new PopActionClickListener() {
            @Override
            public void onActionClick(int position, Object data) {
                mOnPopActionClickListener.onCopyMoreClick(position, (MessageInfo) data);
            }
        });
        actions.add(action);
        action = new PopMenuAction();
        action.setActionName("举报");
        action.setActionClickListener(new PopActionClickListener() {
            @Override
            public void onActionClick(int position, Object data) {
                ToastUtils.showToast(getContext(),"已举报");
            }
        });
        actions.add(action);
        if (msg.getFromUser().startsWith(Func.FUNC_PREFIX)) {
            if (msg.getMsgType() == MessageInfo.MSG_TYPE_FUNCTION_CUSTOM) {
                actions.clear();
                action = new PopMenuAction();
                action.setActionName("转发");
                action.setActionClickListener(new PopActionClickListener() {
                    @Override
                    public void onActionClick(int position, Object data) {
                        mOnPopActionClickListener.onRepeatClick(position, (MessageInfo) data);
                    }
                });
                actions.add(action);
            }
        }
        if (msg.isSelf()) {
//            action = new PopMenuAction();
//            action.setActionName("撤回");
//            action.setActionClickListener(new PopActionClickListener() {
//                @Override
//                public void onActionClick(int position, Object data) {
//                    mOnPopActionClickListener.onRevokeMessageClick(position, (MessageInfo) data);
//                }
//            });
//            actions.add(action);
            if (msg.getStatus() == MessageInfo.MSG_STATUS_SEND_FAIL) {
                action = new PopMenuAction();
                action.setActionName("重发");
                action.setActionClickListener(new PopActionClickListener() {
                    @Override
                    public void onActionClick(int position, Object data) {
                        mOnPopActionClickListener.onSendMessageClick(msg, true);
                    }
                });
                actions.add(action);
            }
        }
        mPopActions.clear();
        mPopActions.addAll(actions);
        mPopActions.addAll(mMorePopActions);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if (mHandler != null) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
                int firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (firstPosition == 0 && ((lastPosition - firstPosition + 1) < getAdapter().getItemCount())) {
                    if (getAdapter() instanceof MessageListAdapter) {
                        ((MessageListAdapter) getAdapter()).showLoading();
                    }
                    mHandler.loadMore();
                }
            }
        }
    }


    public void scrollToEnd() {
        if (getAdapter() != null) {
            scrollToPosition(getAdapter().getItemCount() - 1);
        }
    }

    public OnLoadMoreHandler getLoadMoreHandler() {
        return mHandler;
    }

    public void setLoadMoreMessageHandler(OnLoadMoreHandler mHandler) {
        this.mHandler = mHandler;
    }

    public OnEmptySpaceClickListener getEmptySpaceClickListener() {
        return mEmptySpaceClickListener;
    }

    public void setEmptySpaceClickListener(OnEmptySpaceClickListener mEmptySpaceClickListener) {
        this.mEmptySpaceClickListener = mEmptySpaceClickListener;
    }

    public void setPopActionClickListener(OnPopActionClickListener listener) {
        mOnPopActionClickListener = listener;
    }

    @Override
    public void postSetAdapter(MessageListAdapter adapter) {
        mAdapter.setOnItemClickListener(new MessageLayout.OnItemClickListener() {
            @Override
            public void onMessageLongClick(View view, int position, MessageInfo messageInfo) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onMessageLongClick(view, position, messageInfo);
                }
            }

            @Override
            public void onUserIconClick(View view, int position, MessageInfo info) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onUserIconClick(view, position, info);
                }
            }
        });
    }

    public interface OnLoadMoreHandler {
        void loadMore();
    }

    public interface OnEmptySpaceClickListener {
        void onClick();
    }

    public interface OnItemClickListener {
        void onMessageLongClick(View view, int position, MessageInfo messageInfo);

        void onUserIconClick(View view, int position, MessageInfo messageInfo);
    }

    public interface OnPopActionClickListener {

        void onCopyClick(int position, MessageInfo msg);//复制

        void onRepeatClick(int position, MessageInfo msg);// 转发

        void onCopyMoreClick(int position, MessageInfo msg);//多选

        void onSendMessageClick(MessageInfo msg, boolean retry);// 重发

        void onDeleteMessageClick(int position, MessageInfo msg);// 删除

        void onRevokeMessageClick(int position, MessageInfo msg);// 撤回

        void onReportClick(int position, MessageInfo msg);// 撤回
    }
}
