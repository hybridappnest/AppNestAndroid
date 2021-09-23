package com.tencent.qcloud.tim.uikit.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class SelectionActivity extends Activity {

    private static OnResultReturnListener sOnResultReturnListener;

    private RadioGroup radioGroup;
    private EditText input;
    private int mSelectionType;

    public static void startTextSelection(Context context, Bundle bundle, OnResultReturnListener listener) {
        bundle.putInt(TUIKitConstants.Selection.TYPE, TUIKitConstants.Selection.TYPE_TEXT);
        startSelection(context, bundle, listener);
    }

    public static void startListSelection(Context context, Bundle bundle, OnResultReturnListener listener) {
        bundle.putInt(TUIKitConstants.Selection.TYPE, TUIKitConstants.Selection.TYPE_LIST);
        startSelection(context, bundle, listener);
    }

    private static void startSelection(Context context, Bundle bundle, OnResultReturnListener listener) {
        Intent intent = new Intent(context, SelectionActivity.class);
        intent.putExtra(TUIKitConstants.Selection.CONTENT, bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        sOnResultReturnListener = listener;
    }

    /**
     * 获取字符串的长度
     *
     * @param str
     */
    public static int getStrLength(String str) {
        if (str == null) {
            return 0;
        }
        if (str.length() == 0) {
            return 0;
        }
        int i = 0;
        int len = 0;
        int leng = 0;
        char[] chs = str.toCharArray();
        try {
            leng = str.getBytes("gbk").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            while (i < leng) {
                len = (chs[i++] > 0xff) ? (len + 3) : (len + 1);
            }
        } catch (Exception e) {

        }
        return len;
    }

    /* 字符串截取 防止出现半个汉字 */
    public static String truncate(String str, int byteLength) {
        if (byteLength < 0)
            return "";
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return str;
        }
        int i = 0;
        int len = 0;
        int leng = 0;
        char[] chs = str.toCharArray();
        try {
            leng = str.getBytes("gbk").length;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            while ((len < byteLength) && (i < leng)) {
                len = (chs[i++] > 0xff) ? (len + 3) : (len + 1);
            }
        } catch (Exception e) {

        }

        if (len > byteLength) {
            i--;
        }
        return new String(chs, 0, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_activity);
        final TitleBarLayout titleBar = findViewById(R.id.edit_title_bar);
        radioGroup = findViewById(R.id.content_list_rg);
        input = findViewById(R.id.edit_content_et);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable editable = input.getText();
                int len = getStrLength(editable.toString().trim());
                ((TextView) findViewById(R.id.tips)).setText("长度" + len + "" + "/30个字符");
                if (len > 30) {
                    String str = editable.toString().trim();
                    String newStr = truncate(str, 30);
                    int len1 = getStrLength(newStr);
                    if (len1 > 30) {
                        return;
                    }
                    input.setText(newStr);
                    input.setSelection(newStr.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Bundle bundle = getIntent().getBundleExtra(TUIKitConstants.Selection.CONTENT);
        if (bundle == null) {
            finish();
            return;
        }
        switch (bundle.getInt(TUIKitConstants.Selection.TYPE)) {
            case TUIKitConstants.Selection.TYPE_TEXT:
                radioGroup.setVisibility(View.GONE);
                String defaultString = bundle.getString(TUIKitConstants.Selection.INIT_CONTENT);
                int limit = bundle.getInt(TUIKitConstants.Selection.LIMIT);
                if (!TextUtils.isEmpty(defaultString)) {
                    input.setText(defaultString);
                    input.setSelection(defaultString.length());
                }
                break;
            case TUIKitConstants.Selection.TYPE_LIST:
                input.setVisibility(View.GONE);
                ArrayList<String> list = bundle.getStringArrayList(TUIKitConstants.Selection.LIST);
                if (list == null || list.size() == 0) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(list.get(i));
                    radioButton.setId(i);
                    radioGroup.addView(radioButton, i, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                int checked = bundle.getInt(TUIKitConstants.Selection.DEFAULT_SELECT_ITEM_INDEX);
                radioGroup.check(checked);
                radioGroup.invalidate();
                break;
            default:
                finish();
                return;
        }
        mSelectionType = bundle.getInt(TUIKitConstants.Selection.TYPE);

        final String title = bundle.getString(TUIKitConstants.Selection.TITLE);
        titleBar.setTitle(title, TitleBarLayout.POSITION.MIDDLE);
        titleBar.setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleBar.getRightIcon().setVisibility(View.GONE);
        titleBar.getRightTitle().setText("完成");
        titleBar.setOnRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                echoClick(title);
            }
        });
    }

    private void echoClick(String title) {
        switch (mSelectionType) {
            case TUIKitConstants.Selection.TYPE_TEXT:
                if (TextUtils.isEmpty(input.getText().toString()) && title.equals(getResources().getString(R.string.modify_group_name))) {
                    ToastUtil.toastLongMessage("请输入群昵称");
                    return;
                }

                if (sOnResultReturnListener != null) {
                    sOnResultReturnListener.onReturn(input.getText().toString());
                }
                break;
            case TUIKitConstants.Selection.TYPE_LIST:
                if (sOnResultReturnListener != null) {
                    sOnResultReturnListener.onReturn(radioGroup.getCheckedRadioButtonId());
                }
                break;
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sOnResultReturnListener = null;
    }

    public interface OnResultReturnListener {
        void onReturn(Object res);
    }
}
