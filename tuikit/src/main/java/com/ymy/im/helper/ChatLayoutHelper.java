package com.ymy.im.helper;

import android.content.Context;
import android.view.View;

import androidx.annotation.StringDef;

import com.ymy.im.helper.type.EventType;
import com.ymy.im.utils.DemoLog;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.modules.chat.ChatLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.input.InputLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.inputmore.InputMoreActionUnit;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder.ICustomMessageViewGroup;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder.IOnCustomMessageDrawListener;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.ymy.core.utils.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatLayoutHelper {

    private static final String TAG = ChatLayoutHelper.class.getSimpleName();
    private static HashMap<String, List<String>> eventTypeInputAction;
    private static HashMap<String, InputMoreActionUnit> mInputMoreActionUnitMap;

    static {
        initEventActionMap();
        initInputMoreActionUnitMap();
    }

    private Context mContext;

    public ChatLayoutHelper(Context context) {
        mContext = context;
    }

    private static void initEventActionMap() {
        eventTypeInputAction = new HashMap();
        ArrayList<String> list = new ArrayList();
        list.add(InputAction.history);
        list.add(InputAction.a_to_w);
        list.add(InputAction.yanlian);
        list.add(InputAction.timeline);
        list.add(InputAction.moveingline);
        list.add(InputAction.desc);
        eventTypeInputAction.put(EventType.TYPE_ALARM, list);

        ArrayList<String> list1 = new ArrayList();
        list1.add(InputAction.desc);
        eventTypeInputAction.put(EventType.TYPE_WORKORDER, list1);

        ArrayList<String> list2 = new ArrayList();
        list2.add(InputAction.desc);
        list2.add(InputAction.a_to_w);
        eventTypeInputAction.put(EventType.TYPE_XJ, list2);

        ArrayList<String> list3 = new ArrayList();
        list3.add(InputAction.a_to_w);
        eventTypeInputAction.put(EventType.TYPE_YHPC, list3);

        ArrayList<String> list4 = new ArrayList();
        list4.add(InputAction.desc);
        list4.add(InputAction.a_to_w);
        list4.add(InputAction.moveingline);
        eventTypeInputAction.put(EventType.TYPE_YAYL, list4);
        ArrayList<String> list5 = new ArrayList();
        eventTypeInputAction.put(EventType.TYPE_SJPX, list5);
        ArrayList<String> list6 = new ArrayList();
        eventTypeInputAction.put(EventType.TYPE_ZKJJ, list6);
    }

    private static void initInputMoreActionUnitMap() {

        InputMoreActionUnit unit2 = new InputMoreActionUnit();
        unit2.setIconResId(R.drawable.icon_im_action_desc);
        unit2.setTitleId(R.string.test_custom_action_alarm_desc);
        unit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImHelper.getDBXSendReq().goToEventWebActivity(WebViewType.EVENT_DESC,"");
            }
        });

//        InputMoreActionUnit unit7 = new InputMoreActionUnit();
//        unit7.setIconResId(R.drawable.icon_im_action_a_to_w);
//        unit7.setTitleId(R.string.test_custom_action_alarm_to_workorder);
//        unit7.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ImHelper.getDBXSendReq().eventToWorkOrder(ImHelper.eventId);
//            }
//        });


        InputMoreActionUnit unit6 = new InputMoreActionUnit();
        unit6.setIconResId(R.drawable.icon_im_action_timeline);
        unit6.setTitleId(R.string.test_custom_action_alarm_timeline);
        unit6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImHelper.getDBXSendReq().goToEventWebActivity(WebViewType.ALARM_TIMELINE,"");
            }
        });

        InputMoreActionUnit unit3 = new InputMoreActionUnit();
        unit3.setIconResId(R.drawable.icon_im_action_history);
        unit3.setTitleId(R.string.test_custom_action_alarm_history);
        unit3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImHelper.getDBXSendReq().goToEventWebActivity(WebViewType.ALARM_HISTORY,"");
            }
        });

        InputMoreActionUnit unit4 = new InputMoreActionUnit();
        unit4.setIconResId(R.drawable.icon_im_action_moveline);
        unit4.setTitleId(R.string.test_custom_action_alarm_moveingline);
        unit4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImHelper.getDBXSendReq().goToEventWebActivity(WebViewType.EVENT_MOVELINE,"");
            }
        });
        InputMoreActionUnit unit9 = new InputMoreActionUnit();
        unit9.setIconResId(R.drawable.icon_im_action_zhuan);
        unit9.setTitleId(R.string.test_custom_action_alarm_yanlian);
        unit9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImHelper.getDBXSendReq().goToEventWebActivity(WebViewType.ALARM_YANLIAN,"");
            }
        });
        mInputMoreActionUnitMap = new HashMap();
        mInputMoreActionUnitMap.put(InputAction.desc, unit2);
//        mInputMoreActionUnitMap.put(InputAction.a_to_w, unit7);
        mInputMoreActionUnitMap.put(InputAction.timeline, unit6);
        mInputMoreActionUnitMap.put(InputAction.history, unit3);
        mInputMoreActionUnitMap.put(InputAction.moveingline, unit4);
        mInputMoreActionUnitMap.put(InputAction.yanlian, unit9);
    }

    public void customizeChatLayout(final ChatLayout layout, String eventType) {
        MessageLayout messageLayout = layout.getMessageLayout();
        // 设置自定义的消息渲染时的回调
        messageLayout.setOnCustomMessageDrawListener(new CustomMessageDraw());

        //====== InputLayout使用范例 ======//
        final InputLayout inputLayout = layout.getInputLayout();

        inputLayout.enableAudioCall();
        inputLayout.enableVideoCall();
        inputLayout.initHandlerBtn();
//        inputLayout.change();
        // TODO 可以自己增加一些功能，可以打开下面代码测试
        inputLayout.clearCustomActionList();
        InputMoreActionUnit unit8 = new InputMoreActionUnit();
        unit8.setIconResId(R.drawable.icon_im_action_emoji);
        unit8.setTitleId(R.string.test_custom_emoji);
        unit8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputLayout.toggleEmojiLayout();
            }
        });
        if (StringUtils.isEmpty(eventType) || EventType.TYPE_NORMAL.equals(eventType)) {
//            InputMoreActionUnit unit7 = new InputMoreActionUnit();
//            unit7.setIconResId(R.drawable.icon_im_action_a_to_w);
//            unit7.setTitleId(R.string.test_custom_action_alarm_to_workorder);
//            unit7.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ImHelper.getDBXSendReq().eventToWorkOrder(ImHelper.eventId);
//                }
//            });
//            inputLayout.addAction(unit7);
        }else  if (EventType.TYPE_ZKJJ.equals(eventType)) {
            InputMoreActionUnit unit10 = new InputMoreActionUnit();
            unit10.setIconResId(R.drawable.icon_im_action_jiaojie);
            unit10.setTitleId(R.string.test_custom_action_alarm_jiaojie);
            unit10.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layout.showJiaoJieState();
                }
            });
            inputLayout.addAction(unit10);
        }else {
            InputMoreActionUnit unit5 = new InputMoreActionUnit();
            unit5.setIconResId(R.drawable.icon_im_action_handle);
            unit5.setTitleId(R.string.test_custom_action_alarm_send_action);
            unit5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    ImHelper.getDBXSendReq().sendHandleEventAction(ImHelper.eventId);
                }
            });
            inputLayout.addAction(unit5);
        }
//        if (EventType.TYPE_YAYL.equals(eventType) && ImHelper.getDBXSendReq().isUserRoleManager()) {
//            InputMoreActionUnit unit11 = new InputMoreActionUnit();
//            unit11.setIconResId(R.drawable.icon_im_action_handle);
//            unit11.setTitleId(R.string.test_custom_action_yayl_finish);
//            unit11.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    ImHelper.getDBXSendReq().finishYAYL(ImHelper.eventId);
//                }
//            });
//            inputLayout.addAction(unit11);
//        }
        inputLayout.addAction(unit8);
        List<String> inputActions = eventTypeInputAction.get(eventType);
        Logger.d(inputActions);
        if (inputActions != null) {
            for (String action : inputActions) {
                InputMoreActionUnit inputMoreActionUnit = mInputMoreActionUnitMap.get(action);
                inputLayout.addAction(inputMoreActionUnit);
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER})
    @StringDef(value = {InputAction.desc, InputAction.a_to_w, InputAction.timeline, InputAction.history, InputAction.moveingline, InputAction.yanlian})
    public @interface InputAction {
        String desc = "desc";
        String a_to_w = "a_to_w";
        String timeline = "timeline";
        String history = "history";
        String moveingline = "moveingline";
        String yanlian = "yanlian";
    }


    public class CustomMessageDraw implements IOnCustomMessageDrawListener {

        /**
         * 自定义消息渲染时，会调用该方法，本方法实现了自定义消息的创建，以及交互逻辑
         *
         * @param parent 自定义消息显示的父View，需要把创建的自定义消息view添加到parent里
         * @param info   消息的具体信息
         */
        @Override
        public void onDraw(ICustomMessageViewGroup parent, MessageInfo info) {
            // 获取到自定义消息的json数据
            if (info.getTimMessage().getElemType() != V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
                return;
            }
            V2TIMCustomElem elem = info.getTimMessage().getCustomElem();
            // 自定义的json数据，需要解析成bean实例
            CustomHelloMessage data = null;
            try {
                data = new Gson().fromJson(new String(elem.getData()), CustomHelloMessage.class);
            } catch (Exception e) {
                DemoLog.w(TAG, "invalid json: " + new String(elem.getData()) + " " + e.getMessage());
            }
            if (data == null) {
                DemoLog.e(TAG, "No Custom Data: " + new String(elem.getData()));
            } else if (data.version == TUIKitConstants.JSON_VERSION_1
                    || (data.version == TUIKitConstants.JSON_VERSION_4 && data.businessID.equals("text_link"))) {
                CustomHelloTIMUIController.onDraw(parent, data);
            } else {
                DemoLog.w(TAG, "unsupported version: " + data);
            }
        }
    }

}
