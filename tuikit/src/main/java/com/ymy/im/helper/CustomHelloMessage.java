package com.ymy.im.helper;

import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;

import java.util.List;

/**
 * 自定义消息的bean实体，用来与json的相互转化
 */
public class CustomHelloMessage {
    String businessID = TUIKitConstants.BUSINESS_ID_CUSTOM_HELLO;
    String text = "欢迎加入云通信IM大家庭！";
    String link = "https://cloud.tencent.com/document/product/269/3794";

    int version = TUIKitConstants.JSON_VERSION_UNKNOWN;
    public static void getGroupCustom( List<String> list){
        // TODO toString打印
        try {
            //Android
//            TIMGroupManager.getInstance().getGroupInfo(list, new TIMValueCallBack() {
//                @Override
//                public void onError(int i, String s) {
//                }
//
//                @Override
//                public void onSuccess(Object o) {
//                    List<TIMGroupDetailInfo> timGroupDetailInfo = (List) o;
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
