package com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ymy.helper.ImHelper;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.face.FaceManager;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.utils.LongClickLinkMovementMethod;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;

import java.util.regex.Pattern;

public class MessageTextHolder extends MessageContentHolder {

    public static final Pattern EMAIL_ADDRESS = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
            + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9a-z\\._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}");
    public static final Pattern WEB_PATTERN =
            Pattern
                    .compile("((http[s]{0,1}|ftp)://[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)|(www.[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)");
    public static final Pattern PHONE = Pattern.compile( // sdd = space, dot, or dash
            "(\\+[0-9]+[\\- \\.]*)?" // +<digits><sdd>*
                    + "(\\([0-9]+\\)[\\- \\.]*)?" // (<digits>)<sdd>*
                    + "([0-9][0-9\\- \\.][0-9\\- \\.]+[0-9])");
    public final static Pattern WEB_URL;
    // all domain names
    private static final String[] ext = {
            "top", "com.cn", "com", "net", "org", "edu", "gov", "int", "mil", "cn", "tel", "biz", "cc", "tv", "info",
            "name", "hk", "mobi", "asia", "cd", "travel", "pro", "museum", "coop", "aero", "ad", "ae", "af",
            "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "as", "at", "au", "aw", "az", "ba", "bb", "bd",
            "be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by", "bz",
            "ca", "cc", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cq", "cr", "cu", "cv", "cx",
            "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "ee", "eg", "eh", "es", "et", "ev", "fi",
            "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gh", "gi", "gl", "gm", "gn", "gp",
            "gr", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie", "il", "in", "io",
            "iq", "ir", "is", "it", "jm", "jo", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw",
            "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md",
            "mg", "mh", "ml", "mm", "mn", "mo", "mp", "mq", "mr", "ms", "mt", "mv", "mw", "mx", "my", "mz",
            "na", "nc", "ne", "nf", "ng", "ni", "nl", "no", "np", "nr", "nt", "nu", "nz", "om", "qa", "pa",
            "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "pt", "pw", "py", "re", "ro", "ru", "rw",
            "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr", "st",
            "su", "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk", "tm", "tn", "to", "tp", "tr", "tt",
            "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "va", "vc", "ve", "vg", "vn", "vu", "wf", "ws",
            "ye", "yu", "za", "zm", "zr", "zw"
    };

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < ext.length; i++) {
            sb.append(ext[i]);
            sb.append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        // final pattern str
        String pattern = "((https?|s?ftp|irc[6s]?|git|afp|telnet|smb)://)?((\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|((www\\.|[a-zA-Z\\.\\-]+\\.)?[a-zA-Z0-9\\-]+\\." + sb.toString() + "(:[0-9]{1,5})?))((/[a-zA-Z0-9\\./,;\\?'\\+&%\\$#=~_\\-]*)|([^\\u4e00-\\u9fa5\\s0-9a-zA-Z\\./,;\\?'\\+&%\\$#=~_\\-]*))";
        // Log.v(TAG, "pattern = " + pattern);
        WEB_URL = Pattern.compile(pattern);
    }


    private TextView msgBodyText;

    public MessageTextHolder(View itemView) {
        super(itemView);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_text;
    }

    @Override
    public void initVariableViews() {
        msgBodyText = rootView.findViewById(R.id.msg_body_tv);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void layoutVariableViews(final MessageInfo msg, final int position) {
        msgBodyText.setVisibility(View.VISIBLE);
        if (msg.getExtra() != null) {
            FaceManager.handlerEmojiText(msgBodyText, msg.getExtra().toString(), false);
        }
        if (properties.getChatContextFontSize() != 0) {
            msgBodyText.setTextSize(properties.getChatContextFontSize());
        }
        if (msg.isSelf()) {
            if (properties.getRightChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getRightChatContentFontColor());
            }
        } else {
            if (properties.getLeftChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getLeftChatContentFontColor());
            }
        }
        msgBodyText.setTextColor(Color.parseColor("#333333"));
        Linkify.addLinks(msgBodyText, WEB_URL, null);
//        Linkify.addLinks(msgBodyText, EMAIL_ADDRESS, null);
//        Linkify.addLinks(msgBodyText, PHONE, null);
        interceptHyperLink(msgBodyText);
        msgBodyText.setMovementMethod(new LongClickLinkMovementMethod(new LongClickLinkMovementMethod.OnLongClickListener() {
            @Override
            public void onLongClick() {
                if (onItemClickListener != null) {
                    onItemClickListener.onMessageLongClick(msgBodyText, position, msg);
                }
            }
        }));
    }

    /**
     * 拦截超链接
     *
     * @param tv
     */
    private void interceptHyperLink(TextView tv) {
        CharSequence text = tv.getText();
        if (text instanceof SpannableString) {
            int end = text.length();
            Spannable spannable = (Spannable) tv.getText();
            //autoLink设置为web时会将TextView中的所有内容可被识别的链接自动识别为URLSpan
            URLSpan[] urlSpans = spannable.getSpans(0, end, URLSpan.class);
            if (urlSpans.length == 0) {
                return;
            }

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            spannableStringBuilder.clearSpans();
            // 循环遍历并拦截 所有http://开头的链接
            for (URLSpan url : urlSpans) {
                final String urlString = url.getURL();
                CustomURLSpan customURLSpan = new CustomURLSpan(urlString);
                if (urlString != null && urlString.length() > 0) {
                    int _start = spannable.getSpanStart(url);
                    int _end = spannable.getSpanEnd(url);
                    try {
                        spannableStringBuilder.setSpan(customURLSpan, _start, _end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            tv.setLinkTextColor(tv.getContext().getResources().getColor(R.color.app_blue));
            tv.setText(spannableStringBuilder);
        }

    }

    public class CustomURLSpan extends ClickableSpan {

        String mString = "";

        public CustomURLSpan(String url) {
            mString = url;
        }

        @Override
        public void onClick(@NonNull View widget) {
            ImHelper.getDBXSendReq().goToWebActivity(mString);
//            if (EMAIL_ADDRESS.matcher(mString).find()) {
//                CallPhoneAndSendEmailUtils.sendEmail(
//                        KtxManager.getCurrentActivity(),mString);
//            } else if (WEB_URL.matcher(mString).find()) {
//                ImHelper.getDBXSendReq().goToWebActivity(mString);
//            } else if (PHONE.matcher(mString).find()) {
//                CallPhoneAndSendEmailUtils.dialNum(
//                        KtxManager.getCurrentActivity(),mString);
//            }
        }

        private void dialNum(String mString) {

        }

        private void sendEmail(CharSequence mString) {

        }
    }

}
