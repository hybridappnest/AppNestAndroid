package com.tencent.qcloud.tim.uikit.modules.group.member;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfoAdapter;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfoProvider;
import com.tencent.qcloud.tim.uikit.modules.group.interfaces.IGroupMemberLayout;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;

import java.util.List;

public class GroupMemberDeleteLayout extends LinearLayout implements IGroupMemberLayout {

    private TitleBarLayout mTitleBar;
    private GridView mMembers;
    private GroupMemberDeleteAdapter mAdapter;
    private List<GroupMemberInfo> mDelMembers;
    private GroupInfo mGroupInfo;

    public GroupMemberDeleteLayout(Context context) {
        super(context);
        init();
    }

    public GroupMemberDeleteLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GroupMemberDeleteLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.group_member_del_layout, this);
        mTitleBar = findViewById(R.id.group_member_title_bar);
//        mTitleBar.setTitle("移除", TitleBarLayout.POSITION.RIGHT);
        mTitleBar.setTitle("移除成员", TitleBarLayout.POSITION.MIDDLE);
        mTitleBar.getRightTitle().setTextColor(Color.BLUE);
        mTitleBar.getRightIcon().setVisibility(View.GONE);
        // 成员列表
        mAdapter = new GroupMemberDeleteAdapter(getContext());
        mMembers = findViewById(R.id.group_members);
        mMembers.setAdapter(mAdapter);
    }

    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    @Override
    public void setParentLayout(Object parent) {

    }

    @Override
    public void setDataSource(GroupInfo groupInfo) {
        mGroupInfo = groupInfo;
        mAdapter.setDataSource(groupInfo,groupInfo.getMemberDetails());
    }
}
