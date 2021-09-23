package com.ymy.fragment;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.ymy.activity.SearchGroupNameActivity;
import com.ymy.adapter.ConversationAdapter;
import com.ymy.helper.ImHelper;
import com.ymy.signature.Menu;
import com.ymy.view.BadgePagerTitleView;
import com.ymy.view.CustomViewPager;
import com.ymy.view.ScaleTransitionPagerTitleView;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationProvider;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.base.Refresher;
import com.ymy.core.base.RootFragment;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeAnchor;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.ymy.core.notchtools.NotchTools;

public class TabMsgFragment extends RootFragment implements Refresher {

    List<Fragment> fragments = new ArrayList<>();
    private View mBaseView;
    private TitleBarLayout mTitleBarLayout;
    private Menu mMenu;
    private String[] key = new String[]{"EVENT_ALL", "EVENT_BJ", "EVENT_YHPC", "EVENT_XJ", "EVENT_GD", "EVENT_ZKJJ", "EVENT_SJPX", "EVENT_YAYL"};
    private String[] value = new String[]{"全部", "报警", "隐患排查" ,"巡检","工单", "中控交接","实践能力", "预案演练"};
    private CustomViewPager mViewPager;
    private ConversationAdapter adapter = null;
    private HashMap<String, TextView> dotViews = new HashMap<String, TextView>();
    private MagicIndicator magicIndicator;
    ViewPager.OnPageChangeListener mOnPageChangeCallback = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            if(allList == null){
                onRefresh();
            }else {
                dealUnreader(allList);
            }
            magicIndicator.onPageSelected(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            magicIndicator.onPageScrollStateChanged(state);
        }
    };
    /**
     * 未读消息数
     */
    public interface InfoChange {
        void infoChange(String eventType,int status, int level);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_tab_msg, container, false);
        initFragMent();
        initView();
        initMagicIndicator();
        ConversationManagerKit.getInstance().setCallBac(new InfoChange() {
            @Override
            public void infoChange(String eventType, int status, int count) {
                if(status == 1 && lastNum  == count){
                    return;
                }
                lastNum = count;
                onRefresh();
            }
        });
        return mBaseView;
    }
    private static long  lastNum = 0;//
    private void initView() {
        mViewPager = (CustomViewPager) mBaseView.findViewById(R.id.view_pager);
        adapter = new ConversationAdapter(getChildFragmentManager());
        mViewPager.setAdapter(adapter);
        adapter.setList(fragments);
        mTitleBarLayout = mBaseView.findViewById(R.id.conversation_title);
        // 从布局文件中获取会话列表面板
        mTitleBarLayout.setTitle("日常", TitleBarLayout.POSITION.MIDDLE);
        mTitleBarLayout.getLeftGroup().setVisibility(View.GONE);
        mTitleBarLayout.setRightIcon(R.drawable.conversation_more);
        mTitleBarLayout.setRightIcon2(R.drawable.conversation_search);
        mMenu = new Menu(getActivity(), mTitleBarLayout, Menu.MENU_TYPE_CONVERSATION);
        initTitleAction();
        setNotch(mBaseView);
    }

    private void setNotch(View mBaseView) {
        FragmentActivity activity = getActivity();
        if(activity != null){
            Window window = activity.getWindow();
            int statusHeight = NotchTools.getFullScreenTools().getStatusHeight(window);
            mTitleBarLayout.setPadding(0, statusHeight, 0, 0);
        }
    }

    private void initFragMent() {
        for (String str : key) {
            ConversationFragment conversationFragment = new ConversationFragment();
            conversationFragment.setKey(str);
            fragments.add(conversationFragment);
        }
        ConversationManagerKit.getInstance().loadConversation(new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if(data instanceof  ConversationProvider){
                    dealUnreader(((ConversationProvider) data).getDataSource());
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ToastUtil.toastLongMessage("加载消息失败");
            }
        });

    }

    private void initTitleAction() {
        mTitleBarLayout.setOnRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMenu.isShowing()) {
                    mMenu.hide();
                } else {
                    mMenu.show();
                }
            }
        });
        mTitleBarLayout.setOnRightClickListener2(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchGroupNameActivity.invoke(getContext());
            }
        });
    }

    public void updateDotsNum(String key, int num) {
        TextView dots = dotViews.get(key);
        if (!dotViews.containsKey(key)||num <= 0) {
            dots.setVisibility(View.GONE);
            return;
        }
        dots.setVisibility(View.VISIBLE);
        if(num>99){
            dots.setText("99+");
        }else {
            dots.setText(num + "");
        }
    }

    private void initMagicIndicator() {
        magicIndicator = (MagicIndicator) mBaseView.findViewById(R.id.magic_indicator);
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return value.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                final BadgePagerTitleView badgePagerTitleView = new BadgePagerTitleView(context);
                SimplePagerTitleView simplePagerTitleView = new ScaleTransitionPagerTitleView(context);
                simplePagerTitleView.setText(value[index]);
                simplePagerTitleView.setPadding(35,0,35,0);
                simplePagerTitleView.setTextSize(20f);
                simplePagerTitleView.setNormalColor(Color.parseColor("#CCFFFFFF"));
                simplePagerTitleView.setSelectedColor(Color.WHITE);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index);
                    }
                });
                badgePagerTitleView.setInnerPagerTitleView(simplePagerTitleView);
                TextView badgeTextView = (TextView) LayoutInflater.from(context).inflate(R.layout.count_badge_layout, null);
                badgeTextView.setVisibility(View.GONE);
                dotViews.put(key[index], badgeTextView);
                badgePagerTitleView.setBadgeView(badgeTextView);
                badgePagerTitleView.setXBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_RIGHT, -UIUtil.dip2px(context, 10)));
                badgePagerTitleView.setYBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_TOP, -15));
                badgePagerTitleView.setAutoCancelBadge(false);
                return badgePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setColors(Color.parseColor("#32B8EC"));
                return linePagerIndicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, mViewPager);
        mViewPager.addOnPageChangeListener(mOnPageChangeCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
        ImHelper.needShowNotification = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        ImHelper.needShowNotification = true;
    }

    public static List<ConversationInfo> allList = new ArrayList<>();
    /**
     * 统一处理会话列表
     * @param list
     */
    public  void dealUnreader(List<ConversationInfo> list){
        allList = list;
        for(String str:key){
            List<ConversationInfo> temp = new ArrayList<>();
            int num = 0;
            for(ConversationInfo conversationInfo : list){
                if(str.equals(conversationInfo.getEvent_group_type())){
                    num  += conversationInfo.getUnRead();
                    temp.add(conversationInfo);
                }else if("EVENT_ALL".equals(str)){
                    num  += conversationInfo.getUnRead();
                    temp.add(conversationInfo);
                }
            }
            if("EVENT_ALL".equals(str)){
                ImHelper.getDBXSendReq().getUnreadNum(num);
            }
            updateDotsNum(str,num);
        }
        int currentItem = mViewPager.getCurrentItem();
         Fragment fragment = fragments.get(currentItem);
        if (fragment instanceof ConversationFragment) {
            ((ConversationFragment) fragment).onRefresh();
        }
    }
    @Override
    public void onRefresh() {
        if(mViewPager!= null && fragments.size()>=0){
            ConversationManagerKit.getInstance().loadConversation(new IUIKitCallBack() {
                @Override
                public void onSuccess(Object data) {
                    if(data instanceof  ConversationProvider){
                        dealUnreader(((ConversationProvider) data).getDataSource());
                    }
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    ToastUtil.toastLongMessage("加载消息失败");
                }
            });

        }
    }
}
