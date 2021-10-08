package com.ymy.im.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.qcloud.tim.uikit.R;


public class CommenDialog extends Dialog {

    private Context mContext;
    private Button cancleBtn;
    private Button enterBtn;
    private TextView tv_title;
    private int mLayoutId;
    private DialogCallBack mCallBack;
    private String mtitle;

    public CommenDialog(Context context,String title ,int layoutId, DialogCallBack callBack) {
        super(context, R.style.common_dialog_style);
        this.mContext = context;
        this.mCallBack = callBack;
        this.mtitle = title;
        if (layoutId == -1) {
            layoutId = R.layout.dialog_layout;
        }
        this.mLayoutId = layoutId;
    }
    public  <T extends View> T findView(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(mLayoutId);
        enterBtn = (Button) findViewById(R.id.confirm);
        cancleBtn = (Button) findViewById(R.id.cancel);
        tv_title  = findView(R.id.tv_title);
        if(enterBtn!=null){
            enterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCallBack!=null){
                        mCallBack.callBack("0");
                    }
                    dismiss();
                }
            });
        }
        if(cancleBtn!=null){
            cancleBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        if(tv_title!=null){
            tv_title.setText(mtitle);
        }
    }
    public interface DialogCallBack {
        void callBack(Object obj);
    }
}
