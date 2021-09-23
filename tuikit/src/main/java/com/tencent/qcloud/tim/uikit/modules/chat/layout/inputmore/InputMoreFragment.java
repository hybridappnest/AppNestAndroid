package com.tencent.qcloud.tim.uikit.modules.chat.layout.inputmore;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.lcw.library.imagepicker.ImagePicker;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.modules.chat.base.BaseInputFragment;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.input.InputLayout;

import java.util.ArrayList;
import java.util.List;

public class InputMoreFragment extends BaseInputFragment {

    public static final int REQUEST_CODE_FILE = 1011;
    public static final int REQUEST_CODE_PHOTO = 1012;

    private View mBaseView;
    private List<InputMoreActionUnit> mInputMoreList = new ArrayList<>();
    private IUIKitCallBack mCallback;
    private InputMoreLayout layout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.chat_inputmore_fragment, container, false);
        layout = mBaseView.findViewById(R.id.input_extra_area);
        layout.init(mInputMoreList);
        return mBaseView;
    }

    public void setActions(List<InputMoreActionUnit> actions) {
        if(actions.size() == mInputMoreList.size() && mInputMoreList.contains(actions)){
            return;
        }
        //判断功能不一致设置功能列表，并重新刷新布局
        this.mInputMoreList = actions;
        if(layout != null){
            layout.init(mInputMoreList);
        }
    }

    public void setCallback(IUIKitCallBack callback) {
        mCallback = callback;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FILE
                || requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode != -1) {
                return;
            }
            final ArrayList<String>  selectedPhotoPath =
                    data.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);
            try {
                if (mCallback != null) {
                    InputLayout.isUpload = false;
                    for(String str : selectedPhotoPath){
                        mCallback.onSuccess(str);
                        SystemClock.sleep(100);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
