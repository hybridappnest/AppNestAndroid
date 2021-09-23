package com.ymy.signature;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.core.graphics.drawable.DrawableCompat;

import com.ymy.helper.ImHelper;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.action.PopActionClickListener;
import com.tencent.qcloud.tim.uikit.component.action.PopMenuAction;
import com.tencent.qcloud.tim.uikit.component.action.PopMenuAdapter;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.utils.ScreenUtil;
import com.ymy.core.Ktx;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    public static final int MENU_TYPE_CONTACT = 1;
    public static final int MENU_TYPE_CONVERSATION = 2;

    // 更多menu
    private ListView mMenuList;
    private PopMenuAdapter mMenuAdapter;
    private PopupWindow mMenuWindow;
    private List<PopMenuAction> mActions = new ArrayList<>();
    private Activity mActivity;
    private View mAttachView;

    public Menu(Activity activity, View attach, int menuType) {
        mActivity = activity;
        mAttachView = attach;

        initActions(menuType);
    }


    private void initActions(int menuType) {
        PopActionClickListener popActionClickListener = new PopActionClickListener() {
            @Override
            public void onActionClick(int position, Object data) {
                PopMenuAction action = (PopMenuAction) data;
                if (TextUtils.equals(action.getActionName(), mActivity.getResources().getString(R.string.start_conversation))) {
                    ChatInfo chat = new ChatInfo();
                    chat.setType(ImHelper.CREATE_ACTION_C2C);
                    ArrayList<String> oldMemberIds = new ArrayList<>();
                    oldMemberIds.add(V2TIMManager.getInstance().getLoginUser());
                    ImHelper.goGetPerson(chat, oldMemberIds);
                }
                if (TextUtils.equals(action.getActionName(), mActivity.getResources().getString(R.string.create_group_chat))) {
                    ChatInfo chat = new ChatInfo();
                    chat.setType(ImHelper.CREATE_ACTION_GROUP);
                    ArrayList<String> oldMemberIds = new ArrayList<>();
                    oldMemberIds.add(V2TIMManager.getInstance().getLoginUser());
                    ImHelper.goGetPerson(chat, oldMemberIds);
                }
                if (TextUtils.equals(action.getActionName(), mActivity.getResources().getString(R.string.start_scan))) {
                    ImHelper.getDBXSendReq().startScanning();
                }
                mMenuWindow.dismiss();
            }
        };

        // 设置右上角+号显示PopAction
        List<PopMenuAction> menuActions = new ArrayList<PopMenuAction>();

        PopMenuAction action = new PopMenuAction();
        action.setActionName(Ktx.app.getResources().getString(R.string.start_conversation));
        action.setActionClickListener(popActionClickListener);
        action.setIconResId(R.drawable.icon_im_pop_menu_member);
        menuActions.add(action);

        action = new PopMenuAction();
        action.setActionName(Ktx.app.getResources().getString(R.string.create_group_chat));
        action.setIconResId(R.drawable.group_icon);
        action.setActionClickListener(popActionClickListener);
        menuActions.add(action);

        action = new PopMenuAction();
        action.setActionName(Ktx.app.getResources().getString(R.string.start_scan));
        action.setActionClickListener(popActionClickListener);
        action.setIconResId(R.drawable.icon_im_pop_menu_scan);
        menuActions.add(action);

        mActions.clear();
        mActions.addAll(menuActions);
    }

    public boolean isShowing() {
        if (mMenuWindow == null) {
            return false;
        }
        return mMenuWindow.isShowing();
    }

    public void hide() {
        mMenuWindow.dismiss();
    }

    public void show() {
        if (mActions == null || mActions.size() == 0) {
            return;
        }
        mMenuWindow = new PopupWindow(mActivity);
        mMenuAdapter = new PopMenuAdapter();
        mMenuAdapter.setDataSource(mActions);
        View menuView = LayoutInflater.from(mActivity).inflate(R.layout.conversation_pop_menu, null);
        // 设置布局文件
        mMenuWindow.setContentView(menuView);

        mMenuList = menuView.findViewById(R.id.conversation_pop_list);
        mMenuList.setAdapter(mMenuAdapter);
        mMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PopMenuAction action = (PopMenuAction) mMenuAdapter.getItem(position);
                if (action != null && action.getActionClickListener() != null) {
                    action.getActionClickListener().onActionClick(position, mActions.get(position));
                }
            }
        });
        mMenuWindow.setWidth(ScreenUtil.getPxByDp(128));
        mMenuWindow.setHeight(ScreenUtil.getPxByDp(156));
//        mMenuWindow.setBackgroundDrawable(getDrawAble(mActivity.getResources().getDrawable(R.drawable.top_pop), Color.parseColor("#010F3A")));

        mMenuWindow.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.top_pop));
        // 设置pop获取焦点，如果为false点击返回按钮会退出当前Activity，如果pop中有Editor的话，fo/cusable必须要为true
        mMenuWindow.setFocusable(true);
        // 设置pop可点击，为false点击事件无效，默认为true
        mMenuWindow.setTouchable(true);
        // 设置点击pop外侧消失，默认为false；在focusable为true时点击外侧始终消失
        mMenuWindow.setOutsideTouchable(true);
        backgroundAlpha(0.5f);
        // 相对于 + 号正下面，同时可以设置偏移量
        mMenuWindow.showAtLocation(mAttachView, Gravity.RIGHT | Gravity.TOP, ScreenUtil.getPxByDp(15), ScreenUtil.getPxByDp(70));
        // 设置pop关闭监听，用于改变背景透明度
        mMenuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
    }

    public Drawable getDrawAble(Drawable drawable1, int color) {
        Drawable drawable = DrawableCompat.wrap(drawable1);
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }

    /**
     * 此方法用于改变背景的透明度，从而达到“变暗”的效果
     */
    private void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        // 0.0-1.0
        lp.alpha = bgAlpha;
        mActivity.getWindow().setAttributes(lp);
        // everything behind this window will be dimmed.
        // 此方法用来设置浮动层，防止部分手机变暗无效
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
}
