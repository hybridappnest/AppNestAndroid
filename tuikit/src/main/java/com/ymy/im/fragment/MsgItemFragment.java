package com.ymy.im.fragment;//package com.ymy.im.fragment;
//
//
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ListView;
//import android.widget.PopupWindow;
//
//import androidx.annotation.Nullable;
//import androidx.viewpager.widget.ViewPager;
//
//import com.ymy.im.activity.GroupListActivity;
//import com.ymy.im.activity.ImChatActivity;
//import com.ymy.im.adapter.ExamplePagerAdapter;
//import com.ymy.im.signature.Menu;
//import com.ymy.im.utils.Constants;
//import com.tencent.imsdk.v2.V2TIMConversation;
//import com.tencent.qcloud.tim.uikit.R;
//import com.tencent.qcloud.tim.uikit.TUIKit;
//import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
//import com.tencent.qcloud.tim.uikit.component.action.PopActionClickListener;
//import com.tencent.qcloud.tim.uikit.component.action.PopDialogAdapter;
//import com.tencent.qcloud.tim.uikit.component.action.PopMenuAction;
//import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
//import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationLayout;
//import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
//import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
//import com.tencent.qcloud.tim.uikit.utils.PopWindowUtil;
//import com.ymy.core.base.RootFragment;
//
//import net.lucode.hackware.magicindicator.MagicIndicator;
//import net.lucode.hackware.magicindicator.ViewPagerHelper;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MsgItemFragment extends RootFragment {
//    private View mBaseView;
//    private ConversationLayout mConversationLayout;
//    private ListView mConversationPopList;
//    private PopDialogAdapter mConversationPopAdapter;
//    private PopupWindow mConversationPopWindow;
//    private List<PopMenuAction> mConversationPopActions = new ArrayList<>();
//    private Menu mMenu;
//    private String [] key= new String[]{"EVENT_BJ","EVENT_GD","EVENT_XJ","EVENT_YHPC","EVENT_YAYL","EVENT_SJPX","EVENT_ZKJJ"};
//    private String [] value = new String[]{"??????","??????","??????","????????????","????????????","????????????","????????????"};
//    private ViewPager mViewPager;
//    private ExamplePagerAdapter adapter = new ExamplePagerAdapter(value);
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        mBaseView = inflater.inflate(R.layout.conversation_fragment, container, false);
//        initView();
//        initMagicIndicator();
//        return mBaseView;
//    }
//
//    private void initView() {
//        mViewPager = (ViewPager)mBaseView.findViewById(R.id.view_pager);
//        mViewPager.setAdapter(adapter);
//
//        // ??????????????????????????????????????????
//        mConversationLayout = mBaseView.findViewById(R.id.conversation_layout);
//        mMenu = new Menu(getActivity(), (TitleBarLayout) mConversationLayout.getTitleBar(), Menu.MENU_TYPE_CONVERSATION);
//        // ???????????????????????????UI??????????????????
//        mConversationLayout.initDefault("");
//        // ??????API??????ConversataonLayout??????????????????????????????????????????????????????????????????
////        ConversationLayoutHelper.customizeConversation(mConversationLayout);
//        mConversationLayout.getConversationList().setOnItemClickListener(new ConversationListLayout.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position, ConversationInfo conversationInfo) {
//                //?????????demo??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//                startChatActivity(conversationInfo);
//            }
//        });
//        mConversationLayout.getConversationList().setOnItemLongClickListener(new ConversationListLayout.OnItemLongClickListener() {
//            @Override
//            public void OnItemLongClick(View view, int position, ConversationInfo conversationInfo) {
//                startPopShow(view, position, conversationInfo);
//            }
//        });
//        initTitleAction();
//        initPopMenuAction();
//    }
//
//    private void initTitleAction() {
//        mConversationLayout.getTitleBar().setOnRightClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mMenu.isShowing()) {
//                    mMenu.hide();
//                } else {
//                    mMenu.show();
//                }
//            }
//        });
//        mConversationLayout.getTitleBar().setOnRightClickListener2(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GroupListActivity.invoke(getContext());
//            }
//        });
//    }
//
//    private void initPopMenuAction() {
//
//        // ????????????conversation??????PopAction
//        List<PopMenuAction> conversationPopActions = new ArrayList<PopMenuAction>();
//        PopMenuAction action = new PopMenuAction();
//        action.setActionName(getResources().getString(R.string.chat_top));
//        action.setActionClickListener(new PopActionClickListener() {
//            @Override
//            public void onActionClick(int position, Object data) {
//                mConversationLayout.setConversationTop(position, (ConversationInfo) data);
//            }
//        });
//        conversationPopActions.add(action);
//        action = new PopMenuAction();
//        action.setActionClickListener(new PopActionClickListener() {
//            @Override
//            public void onActionClick(int position, Object data) {
//                mConversationLayout.deleteConversation(position, (ConversationInfo) data);
//            }
//        });
//        action.setActionName(getResources().getString(R.string.chat_delete));
//        conversationPopActions.add(action);
//        mConversationPopActions.clear();
//        mConversationPopActions.addAll(conversationPopActions);
//    }
//
//    /**
//     * ????????????item??????
//     *
//     * @param index            ???????????????
//     * @param conversationInfo ??????????????????
//     * @param locationX        ?????????X??????
//     * @param locationY        ?????????Y??????
//     */
//    private void showItemPopMenu(final int index, final ConversationInfo conversationInfo, float locationX, float locationY) {
//        if (mConversationPopActions == null || mConversationPopActions.size() == 0)
//            return;
//        View itemPop = LayoutInflater.from(getActivity()).inflate(R.layout.pop_menu_layout, null);
//        mConversationPopList = itemPop.findViewById(R.id.pop_menu_list);
//        mConversationPopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                PopMenuAction action = mConversationPopActions.get(position);
//                if (action.getActionClickListener() != null) {
//                    action.getActionClickListener().onActionClick(index, conversationInfo);
//                }
//                mConversationPopWindow.dismiss();
//            }
//        });
//
//        for (int i = 0; i < mConversationPopActions.size(); i++) {
//            PopMenuAction action = mConversationPopActions.get(i);
//            if (conversationInfo.isTop()) {
//                if (action.getActionName().equals(getResources().getString(R.string.chat_top))) {
//                    action.setActionName(getResources().getString(R.string.quit_chat_top));
//                }
//            } else {
//                if (action.getActionName().equals(getResources().getString(R.string.quit_chat_top))) {
//                    action.setActionName(getResources().getString(R.string.chat_top));
//                }
//
//            }
//        }
//        mConversationPopAdapter = new PopDialogAdapter();
//        mConversationPopList.setAdapter(mConversationPopAdapter);
//        mConversationPopAdapter.setDataSource(mConversationPopActions);
//        mConversationPopWindow = PopWindowUtil.popupWindow(itemPop, mBaseView, (int) locationX, (int) locationY);
//        mBaseView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mConversationPopWindow.dismiss();
//            }
//        }, 10000); // 10s????????????????????????
//    }
//
//    private void startPopShow(View view, int position, ConversationInfo info) {
//        showItemPopMenu(position, info, view.getX(), view.getY() + view.getHeight() / 2);
//    }
//
//    private void startChatActivity(ConversationInfo conversationInfo) {
//        ChatInfo chatInfo = new ChatInfo();
//        chatInfo.setType(conversationInfo.isGroup() ? V2TIMConversation.V2TIM_GROUP : V2TIMConversation.V2TIM_C2C);
//        chatInfo.setId(conversationInfo.getId());
//        chatInfo.setChatName(conversationInfo.getTitle());
//        Intent intent = new Intent(TUIKit.getAppContext(), ImChatActivity.class);
//        intent.putExtra(Constants.CHAT_INFO, chatInfo);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        TUIKit.getAppContext().startActivity(intent);
//    }
//
//    private void initMagicIndicator() {
//        MagicIndicator magicIndicator = (MagicIndicator) mBaseView.findViewById(R.id.magic_indicator);
//        magicIndicator.setBackgroundColor(Color.BLACK);
//        CommonNavigator commonNavigator = new CommonNavigator(getContext());
//        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
//
//            @Override
//            public int getCount() {
//                return value.length;
//            }
//
//            @Override
//            public IPagerTitleView getTitleView(Context context, final int index) {
//                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
//                simplePagerTitleView.setNormalColor(Color.GRAY);
//                simplePagerTitleView.setSelectedColor(Color.WHITE);
//                simplePagerTitleView.setText(value[index]);
//                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mViewPager.setCurrentItem(index);
//                    }
//                });
//                return simplePagerTitleView;
//            }
//
//            @Override
//            public IPagerIndicator getIndicator(Context context) {
//                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
//                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
//                linePagerIndicator.setColors(Color.WHITE);
//                return linePagerIndicator;
//            }
//        });
//        magicIndicator.setNavigator(commonNavigator);
//        ViewPagerHelper.bind(magicIndicator, mViewPager);
//    }
//}
