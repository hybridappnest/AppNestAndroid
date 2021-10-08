package com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ymy.im.helper.ImHelper;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.component.NoticeEventLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.CustomActionType;
import com.tencent.qcloud.tim.uikit.modules.message.Event_property;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;

import java.util.ArrayList;

public class MessageCustomDBXHolder extends MessageContentHolder implements ICustomMessageViewGroup {

    private MessageInfo mMessageInfo;
    private int mPosition;

    private TextView msgBodyText;
    private TextView msg_body_title;
    private Button msg_body_btn;
    private LinearLayout custom_desc_layout;
    private TextView im_level;

    public MessageCustomDBXHolder(View itemView) {
        super(itemView);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_dbx_text;
    }

    @Override
    public void initVariableViews() {
        msgBodyText = rootView.findViewById(R.id.msg_body_tv);
        msg_body_title = rootView.findViewById(R.id.msg_body_title);
        msg_body_btn = rootView.findViewById(R.id.dissolve_button);
        custom_desc_layout = rootView.findViewById(R.id.custom_desc_layout);
        im_level = rootView.findViewById(R.id.im_level);
    }

    @Override
    public void layoutViews(MessageInfo msg, int position) {
        mMessageInfo = msg;
        mPosition = position;
        super.layoutViews(msg, position);
    }

    @Override
    public void layoutVariableViews(final MessageInfo msg, int position) {
        msgBodyText.setVisibility(View.VISIBLE);
        msg_body_title.setVisibility(View.VISIBLE);
        msg_body_btn.setVisibility(View.VISIBLE);
        custom_desc_layout.setVisibility(View.VISIBLE);

        if (msg.getExtra() != null && msg.getExtra() instanceof MessageCustom) {
            final MessageCustom custom = (MessageCustom) msg.getExtra();
            msg_body_title.setText(custom.getTitle());
            msgBodyText.setText(custom.getDesc());
            if (custom.getAction() != null) {
                String type = custom.getAction().getAction_type();
                if (CustomActionType.ACTION_JUMP_URL.equals(type)) {
                    msg_body_btn.setText("查看");
                    msg_body_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String h5_url = custom.getAction().getParams().getH5_url();
                            if (h5_url != null) {
//                                ImHelper.getDBXSendReq().goToEventWebActivity(h5_url,"");
                            }
                        }
                    });
                } else if (CustomActionType.ACTION_HANDLE_EVENT.equals(type)) {
                    msg_body_btn.setText("去处理");
                    msg_body_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String event_type = custom.getEvent_type();
//                            ImHelper.getDBXSendReq().sendHandleEventAction(custom.getEvent_id());
                        }
                    });
                } else if (CustomActionType.ACTION_JUMP_EVENT_DETAIL.equals(type)) {
                    msg_body_btn.setText("查看");
                    msg_body_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            ImHelper.getDBXSendReq().goToEventWebActivity(WebViewType.EVENT_DESC,custom.getAction().getParams().getUserId());
                        }
                    });
                } else if (CustomActionType.ACTION_JUMP_ZKJJ_DETAIL.equals(type)) {
                    final MessageCustom.Params params = custom.getAction().getParams();
                    String status = params.getStatus();
                    if(status.equals("1")){
                        msg_body_btn.setText("去处理");
                    }else if(status.equals("0")){
                        msg_body_btn.setText("查看");
                    }
                    msg_body_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            ImHelper.getDBXSendReq().goToEventWebActivity(WebViewType.EVENT_DESC, params.getHandoverId());
                        }
                    });
                }  else if (CustomActionType.ACTION_JUMP_SJPX_PICTURE.equals(type)) {
                    msg_body_btn.setText("去处理");
                    msg_body_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            ImHelper.getDBXSendReq().goToEventWebActivity(WebViewType.ALARM_SJPX_TEST,"");
                        }
                    });
                } else {
                    msg_body_btn.setText("其他");
                    msg_body_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String event_type = custom.getEvent_type();
                        }
                    });
                }
            }
            im_level.setVisibility(View.GONE);
            if (custom.getEvent_property() == null || custom.getEvent_property().size() == 0) {
                custom_desc_layout.setVisibility(View.GONE);
                return;
            }
            custom_desc_layout.removeAllViews();
            custom_desc_layout.addView(initEvent_propertyView(custom));
        }


    }

    private View initEvent_propertyView(MessageCustom messageCustom) {
        ArrayList<Event_property> custom = messageCustom.getEvent_property();
        View view = LinearLayout.inflate(TUIKit.getAppContext(), R.layout.message_adapter_content_dbx_custom_text, null);
        LinearLayout right_layout = view.findViewById(R.id.right_layout);
        right_layout.setVisibility(View.GONE);
        TextView custom_desc_title1 = view.findViewById(R.id.custom_desc_title1);
        TextView custom_desc_type1 = view.findViewById(R.id.custom_desc_type1);
        TextView custom_desc_level1 = view.findViewById(R.id.custom_desc_level1);
        TextView custom_desc_level_type1 = view.findViewById(R.id.custom_desc_level_type1);
        if (custom.size() > 0) {
            Event_property c = custom.get(0);
            custom_desc_title1.setText(c.getProperty_value());
            custom_desc_type1.setText(c.getProperty_name());
            if ("等级".equals(c.getProperty_name())) {
                try {
                    im_level.setVisibility(View.VISIBLE);
                    im_level.setBackgroundColor(NoticeEventLayout.getLevelColorWithType(messageCustom.getEvent_type(), Integer.parseInt(c.getProperty_value())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (custom.size() > 1) {
            right_layout.setVisibility(View.VISIBLE);
            Event_property c = custom.get(1);
            custom_desc_level1.setText(c.getProperty_value());
            custom_desc_level_type1.setText(c.getProperty_name());
            if ("等级".equals(c.getProperty_name())) {
                try {
                    im_level.setVisibility(View.VISIBLE);
                    im_level.setBackgroundColor(NoticeEventLayout.getLevelColorWithType(messageCustom.getEvent_type(), Integer.parseInt(c.getProperty_value())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        for(Event_property c :custom){
//            if("type".equals(c.getProperty_name())){
//                custom_desc_title1.setText(c.getProperty_value());
//                custom_desc_type1.setText("报警类型");
//                continue;
//            }
//            if("createBy".equals(c.getProperty_name())){
//                custom_desc_title1.setText(c.getProperty_value());
//                custom_desc_type1.setText("报警人");
//                continue;
//            }
//            if("level".equals(c.getProperty_name())){
//                custom_desc_level1.setText(c.getProperty_value());
//                custom_desc_level_type1.setText("等级");
//            }
//        }
        return view;
    }

    private void hideAll() {
        for (int i = 0; i < ((RelativeLayout) rootView).getChildCount(); i++) {
            ((RelativeLayout) rootView).getChildAt(i).setVisibility(View.GONE);
        }
    }

    @Override
    public void addMessageItemView(View view) {
        hideAll();
        if (view != null) {
            ((RelativeLayout) rootView).removeView(view);
            ((RelativeLayout) rootView).addView(view);
        }
    }

    @Override
    public void addMessageContentView(View view) {
        // item有可能被复用，因为不能确定是否存在其他自定义view，这里把所有的view都隐藏之后重新layout
        hideAll();
        super.layoutViews(mMessageInfo, mPosition);

        if (view != null) {
            for (int i = 0; i < msgContentFrame.getChildCount(); i++) {
                msgContentFrame.getChildAt(i).setVisibility(View.GONE);
            }
            msgContentFrame.removeView(view);
            msgContentFrame.addView(view);
        }
    }

}
