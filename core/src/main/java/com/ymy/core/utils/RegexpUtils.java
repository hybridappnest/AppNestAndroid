package com.ymy.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2020/7/15 11:18.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
public class RegexpUtils {
    /**
     * 车牌号码Pattern
     */
    public static final Pattern PLATE_NUMBER_PATTERN = Pattern
            .compile("^[\u0391-\uFFE5]{1}[a-zA-Z0-9]{6}$");
    /**
     * 证件号码Pattern
     */
    public static final Pattern ID_CODE_PATTERN = Pattern
            .compile("^[a-zA-Z0-9]+$");
    /**
     * 编码Pattern
     */
    public static final Pattern CODE_PATTERN = Pattern
            .compile("^[a-zA-Z0-9]+$");
    /**
     * 固定电话编码Pattern
     */
    public static final Pattern PHONE_NUMBER_PATTERN = Pattern
            .compile("0\\d{2,3}-[0-9]+");
    /**
     * 邮政编码Pattern
     */
    public static final Pattern POST_CODE_PATTERN = Pattern.compile("\\d{6}");
    /**
     * 面积Pattern
     */
    public static final Pattern AREA_PATTERN = Pattern.compile("\\d*.?\\d*");
    /**
     * 手机号码Pattern
     */
    public static final Pattern MOBILE_NUMBER_PATTERN = Pattern
            .compile("\\d{11}");
    public static final Pattern ID_CARD = Pattern.compile("^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}(" +
            "(0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    /**
     * 银行帐号Pattern
     */
    public static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern
            .compile("\\d{16,21}");
    String regularExpression = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" +
            "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";

    /**
     * 车牌号码是否正确
     *
     * @param s
     * @return
     */
    public static boolean isPlateNumber(String s) {
        Matcher m = PLATE_NUMBER_PATTERN.matcher(s);
        return m.matches();
    }

    /**
     * 证件号码是否正确
     *
     * @param s
     * @return
     */
    public static boolean isIDCode(String s) {
        Matcher m = ID_CODE_PATTERN.matcher(s);
        return m.matches();
    }

    /**
     * 编码是否正确
     *
     * @param s
     * @return
     */
    public static boolean isCode(String s) {
        Matcher m = CODE_PATTERN.matcher(s);
        return m.matches();
    }

    /**
     * 固话编码是否正确
     *
     * @param s
     * @return
     */
    public static boolean isPhoneNumber(String s) {
        Matcher m = PHONE_NUMBER_PATTERN.matcher(s);
        return m.matches();
    }

    /**
     * 邮政编码是否正确
     *
     * @param s
     * @return
     */
    public static boolean isPostCode(String s) {
        Matcher m = POST_CODE_PATTERN.matcher(s);
        return m.matches();
    }

    /**
     * 面积是否正确
     *
     * @param s
     * @return
     */
    public static boolean isArea(String s) {
        Matcher m = AREA_PATTERN.matcher(s);
        return m.matches();
    }

    /**
     * 手机号码否正确
     *
     * @param s
     * @return
     */
    public static boolean isMobileNumber(String s) {
        Matcher m = MOBILE_NUMBER_PATTERN.matcher(s);
        return m.matches();
    }

    public static boolean isIdCard(String s) {
        Matcher m = ID_CARD.matcher(s);
        return m.matches();
    }

    /**
     * 银行账号否正确
     *
     * @param s
     * @return
     */
    public static boolean isAccountNumber(String s) {
        Matcher m = ACCOUNT_NUMBER_PATTERN.matcher(s);
        return m.matches();
    }

    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}