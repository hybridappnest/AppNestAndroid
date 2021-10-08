package com.ymy.im.adapter;

import android.view.ViewGroup;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息内容子页面适配器
 */
public class ConversationAdapter extends FragmentPagerAdapter {
    public ConversationAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment curFragment = getFragment(position);
        super.setPrimaryItem(container, position, curFragment);
    }
    List<Fragment> allFragment = new ArrayList<>();
    /**
     * 数据列表
     *
     */
    public void setList(List<Fragment> fragments) {
        allFragment.clear();
        allFragment.addAll(fragments);
        notifyDataSetChanged();
    }
    @Override
    public Fragment getItem(int position) {
        return  allFragment.get(position);
    }

    @Override
    public int getCount() {
        return allFragment.size();
    }
    private Fragment getFragment(int position){

        return  allFragment.get(position);
    }
    private String [] key= new String[]{"EVENT_BJ","EVENT_GD","EVENT_XJ","EVENT_YHPC","EVENT_YAYL","EVENT_SJPX","EVENT_ZKJJ"};
    @Override
    public int getItemPosition(Object object) {
        try {
            Fragment fragment = (Fragment) object;
            String key = fragment.getArguments().getString("KEY");
            return key.indexOf(key);
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

}

