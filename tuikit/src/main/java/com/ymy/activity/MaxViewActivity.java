package com.ymy.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.ymy.adapter.ConversationAdapter;
import com.ymy.module.MaxBean;
import com.ymy.view.CustomViewPager;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.photoview.PhotoViewFragment;
import com.ymy.core.base.RootActivity;

import java.util.ArrayList;
import java.util.List;

public class MaxViewActivity extends RootActivity  {

    List<Fragment> fragments = new ArrayList<>();
    private CustomViewPager mViewPager;
    private ConversationAdapter adapter = null;
    private List<MaxBean> mList = new ArrayList<>();
    public static  MaxBean maxBean;
    public static void invoke(Context context, MaxBean bean){
        maxBean = bean;
        Intent intent = new Intent(context,MaxViewActivity.class);
        context.startActivity(intent);
    }
    ViewPager.OnPageChangeListener mOnPageChangeCallback = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            pageNum.setText((position+1)+"/"+maxBean.getData().size());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.max_view_layout);
        if(maxBean == null){
            finish();
            return;
        }
        findViewById(R.id.photo_view_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initFragMent();
        initView();
    }
    private static long  lastNum = 0;//
    TextView pageNum;
    private void initView() {
        mViewPager = (CustomViewPager) findViewById(R.id.view_pager);
        adapter = new ConversationAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(mOnPageChangeCallback);
        adapter.setList(fragments);
        pageNum  = findViewById(R.id.position);
        pageNum.setText((maxBean.getIndex()+1)+"/"+maxBean.getData().size());
        mViewPager.setCurrentItem(maxBean.getIndex());
    }


    private void initFragMent() {
        for (int i = 0 ;i<maxBean.getData().size();i++) {
            MaxBean.Data data = maxBean.getData().get(i);
            if("2".equals(data.getType())){
                PhotoViewFragment  fragment = new PhotoViewFragment();
                data.setLength(maxBean.getData().size());
                fragment.setData(data);
                fragments.add(fragment);
            }else  if("1".equals(data.getType())){
                PhotoViewFragment  fragment = new PhotoViewFragment();
                data.setLength(maxBean.getData().size());
                fragment.setData(data);
                fragments.add(fragment);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
