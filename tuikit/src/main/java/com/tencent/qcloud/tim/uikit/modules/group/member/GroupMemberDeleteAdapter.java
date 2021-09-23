package com.tencent.qcloud.tim.uikit.modules.group.member;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ymy.net.IMHttpManager;
import com.ymy.utils.CommenDialog;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.picture.imageEngine.impl.GlideEngine;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfoProvider;
import com.tencent.qcloud.tim.uikit.utils.BackgroundTasks;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.user.YmyUserManager;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class GroupMemberDeleteAdapter extends BaseAdapter {


    Context mContext;
    CommenDialog dialog;
    private List<GroupMemberInfo> mGroupMembers = new ArrayList<>();
    private GroupInfo mGroupInfo;

    public GroupMemberDeleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mGroupMembers.size();
    }

    @Override
    public GroupMemberInfo getItem(int i) {
        return mGroupMembers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        MyViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(TUIKit.getAppContext()).inflate(R.layout.group_member_adpater, viewGroup, false);
            holder = new MyViewHolder();
            holder.memberIcon = view.findViewById(R.id.group_member_icon);
            holder.group_member_icon_del = view.findViewById(R.id.group_member_icon_del);
            holder.memberName = view.findViewById(R.id.group_member_name);
            view.setTag(holder);
        } else {
            holder = (MyViewHolder) view.getTag();
        }
        final GroupMemberInfo info = getItem(i);
        GlideEngine.loadCornerImage(holder.memberIcon, info.getIconUrl(), null, 300);
        if (!TextUtils.isEmpty(info.getUserName())) {
            holder.memberName.setText(info.getUserName());
        } else {
            holder.memberName.setText(info.getAccount());
        }
        if (mGroupInfo.getOwner().equals(info.getAccount()) || V2TIMManager.getInstance().getLoginUser().equals(info.getAccount())) {
            holder.group_member_icon_del.setVisibility(View.INVISIBLE);
        } else {
            holder.group_member_icon_del.setVisibility(View.VISIBLE);
            holder.group_member_icon_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDelDialog(mGroupInfo, info);
                }
            });
        }
        view.setOnClickListener(null);
        holder.memberIcon.setBackground(null);
        return view;
    }

    public void showDelDialog(final GroupInfo mGroupInfo, final GroupMemberInfo info) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new CommenDialog(mContext, "确认删除？", -1, new CommenDialog.DialogCallBack() {
            @Override
            public void callBack(Object obj) {

                IMHttpManager.deleteGroupMember(mGroupInfo.getId(), info.getAccount(), new IMHttpManager.CallBack() {
                    @Override
                    public void callSuccess(@Nullable Object any) {
                        new GroupInfoProvider().loadGroupInfo(mGroupInfo.getId(), new IUIKitCallBack() {
                            @Override
                            public void onSuccess(Object data) {
                                deleteMemeber(info);
                                ToastUtil.toastLongMessage("删除成员成功");
                                GroupInfo groupInfo = (GroupInfo) data;
                                setDataSource(groupInfo, groupInfo.getMemberDetails());
                            }

                            @Override
                            public void onError(String module, int errCode, String errMsg) {
                                ToastUtil.toastLongMessage("删除成员失败");
                            }
                        });
                    }

                    @Override
                    public void callError() {
                        ToastUtil.toastLongMessage("删除成员失败");
                    }
                });
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void setDataSource(GroupInfo groupInfo, List<GroupMemberInfo> members) {
        this.mGroupInfo = groupInfo;
        mGroupMembers.clear();
        if (members != null) {
            mGroupMembers.addAll(members);
            int position = -1;
            for (int i = 0; i < mGroupMembers.size(); i++) {
                if (YmyUserManager.INSTANCE.getUser().getUserId().equals(mGroupMembers.get(i).getAccount())) {
                    position = i;
                    continue;
                }
            }
            if (position >= 0) {
                mGroupMembers.remove(position);
            }
            BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    private void deleteMemeber(final GroupMemberInfo info) {
        List<GroupMemberInfo> dels = new ArrayList<>();
        dels.add(info);
        GroupInfoProvider provider = new GroupInfoProvider();
        provider.loadGroupInfo(mGroupInfo);
        provider.removeGroupMembers(dels, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                mGroupMembers.remove(info);
                notifyDataSetChanged();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
//                                ToastUtil.toastLongMessage("移除成员失败:errCode=" + errCode);
            }
        });
    }

    private class MyViewHolder {
        private ImageView memberIcon;
        private ImageView group_member_icon_del;
        private TextView memberName;
    }

}
