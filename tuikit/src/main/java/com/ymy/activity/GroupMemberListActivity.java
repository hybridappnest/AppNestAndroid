package com.ymy.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.ymy.adapter.GroupMembersAdapter;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.ITitleBarLayout;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfoProvider;
import com.tencent.qcloud.tim.uikit.modules.group.member.GroupMemberInfo;
import com.ymy.core.base.RootActivity;

public class GroupMemberListActivity extends RootActivity {

    public static String name = "";
    private TitleBarLayout mTitleBar;
    private ListView mListView;
    private GroupMembersAdapter adapter;

    public static void invoke(Context context) {
        context.startActivity(new Intent(context, GroupMemberListActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_member_list_activity);

        init();
    }

    private void init() {
        mTitleBar = findViewById(R.id.black_list_titlebar);
        mTitleBar.setTitle("选择提醒的人", TitleBarLayout.POSITION.MIDDLE);
        mTitleBar.setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBar.getRightGroup().setVisibility(View.GONE);
        adapter = new GroupMembersAdapter();
        mListView = findViewById(R.id.listView);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupMemberInfo memberInfo = adapter.getItem(position);
                if (!TextUtils.isEmpty(memberInfo.getUserName())) {
                    name = memberInfo.getUserName();
                } else {
                    name = memberInfo.getAccount();
                }
                finish();
            }
        });
        loadDataSource();
    }

    @Override
    public void finish() {
        super.finish();
    }

    public void loadDataSource() {
        String id = "";
        try {
            id = GroupChatManagerKit.getInstance().getCurrentChatInfo().getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (id == null || id.isEmpty()) {
            return;
        }
        new GroupInfoProvider().loadGroupInfo(id, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                GroupInfo groupInfo = (GroupInfo) data;
                adapter.setDataSource(groupInfo);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
