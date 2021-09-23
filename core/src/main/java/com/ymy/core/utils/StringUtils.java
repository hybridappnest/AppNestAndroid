package com.ymy.core.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final int LENGTH_46 = 46;
    public static final int LENGTH_60 = 60;
    public static final int LENGTH_12 = 12;
    public static final int LENGTH_200 = 200;
    private static String DATETIME_FORMAT_T_SECOND = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * URL解码
     *
     * @param str
     * @return
     */
    public static String decodeURL(String str) {
        if (!StringUtils.isEmpty(str)) {
            try {
                return URLDecoder.decode(str, "UTF-8");
            } catch (Exception e) {

            }
        }
        return str;
    }

    public static String enCodeRUL(String str) {
        if (!StringUtils.isEmpty(str)) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (Exception ex) {
            }
        }
        return str;
    }

    public static boolean isEmpty(Object str) {
        return str == null || str.toString().length() == 0;
    }

    public static boolean isNotEmpty(Object str) {
        return !isEmpty(str);
    }

    /* 去掉时间为00:00:00 */
    public static String replaceTimeZero(String date) {
        if (date != null) {
            if (date.indexOf("00:00:00") > 0) {
                date = date.replaceAll("00:00:00", "");
            } else if (date.indexOf(":00") == 16) {
                date = date.substring(0, 16);
            }
        }
        return date;
    }

    public static boolean startWithHttp(Object str) {
        return str != null
                && str.toString().toLowerCase((Locale.CHINA)).startsWith("http://");
    }

    /* 字符串截取 防止出现半个汉字 */
    @Deprecated
    public static String truncate(String str, int byteLength) {
        switch (byteLength) {
            case LENGTH_200:
            case LENGTH_12:
                return truncate(str, byteLength, false);
            case LENGTH_46:
            case LENGTH_60:
            default:
                return truncate(str, byteLength, true);
        }

    }

    private static String truncate(String str, int byteLength, boolean isRandom) {
        if (byteLength < 0)
            return "";
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return str;
        }
//		if (byteLength < 0) {
//			throw new IllegalArgumentException(
//					"Parameter byteLength must be great than 0");
//		}
        if (isRandom)
            byteLength += new Random().nextInt(15);
        int i = 0;
        int len = 0;
        int leng = 0;
        char[] chs = str.toCharArray();
        try {
            leng = str.getBytes("gbk").length;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (leng <= byteLength)
            return str;
        try {
            while ((len < byteLength) && (i < leng)) {
                len = (chs[i++] > 0xff) ? (len + 3) : (len + 1);
            }
        } catch (Exception e) {

        }

        if (len > byteLength) {
            i--;
        }
        return new String(chs, 0, i) + "...";
    }

    /**
     * 分割keyword 按最后一个出现的@分割
     *
     * @param data
     * @return keyword
     */
    public static String splitKeyWord(String data) {
        if (data == null || data.length() == 0)
            return null;
        if (data.lastIndexOf("@") == -1)
            return data;
        return data.substring(0, data.lastIndexOf("@"));
    }

    /**
     * @param date (时间戳)
     * @return 年－月－日 (2013-03-01)
     */
    public static String convertDate(String date) {
        try {
            if (date == null || "".equals(date) || "0".equals(date))
                return "";
            if (isNumeric(date))
                return computingTime(Long.parseLong(date));
            else
                return "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 如果 缓存类型为AbstractAQuery.CACHE_POLICY_CACHE
     * 需要减去文件缓存时间
     *
     * @param date
     * @return
     */
    public static String convertDate(String date, boolean reduce) {
        try {
            if (date == null || "".equals(date))
                return "";
            if (isNumeric(date))
                return computingTime(Long.parseLong(date), reduce);
            else
                return "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String convertDateToYMD(String date) {
        try {
            if (date == null || "".equals(date))
                return "";
            if (isNumeric(date))
                return toNYR(Long.parseLong(date));
            else
                return date;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 确定是否是时间戳
     *
     * @param str
     * @return
     */
    private static boolean isNumeric(String str) {
        if (str == null || "".equals(str))
            return false;
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;

    }

    /**
     * 计算时间1-59分钟前，
     *
     * @param date
     * @return
     */
    private static String computingTime(Long date) {
        if (date < 10000)
            return "";
        float currentTime = System.currentTimeMillis();
        float x = (currentTime - date) / 1000L;
        int hour = (int) Math.floor((x / (60 * 60)));
        int min = (int) Math.floor((x / 60)) % 60;
        if (x / 60 <= 60) {
            if (min <= 1) {
                return "刚刚";
            } else if (min == 60) {
                return "59分钟前";
            } else
                return min + "分钟前";
        } else if (hour < 24) {
            if (hour <= 0)
                return "2小时前";
            return hour % 24 + "小时前";
        } else if (hour < 48)
            return "昨天";
        return toNYR(date);
    }


    private static String computingTime(Long date, boolean reduce) {
        if (date < 10000)
            return "";
        float currentTime = System.currentTimeMillis();
        float x = currentTime / 1000L - date / 1000L;
        if (reduce) x -= 10;
        int hour = (int) Math.ceil((x / (60 * 60)));
        int min = (int) Math.ceil((x / 60)) % 60;
        if (x / 60 <= 60) {
            if (min <= 1) {
                return "刚刚";
            } else if (min == 60) {
                return "59分钟前";
            } else
                return min + "分钟前";
        } else if (hour < 24) {
            if (hour <= 0)
                return "2小时前";
            return hour % 24 + "小时前";
        } else if (hour < 48)
            return "昨天";
        return toNYR(date);
    }

    /**
     * 截取年月日 如（2013-01-08）
     *
     * @param data
     * @return yyyy-MM-dd
     */
    public static String toNYR(long data) {
        SimpleDateFormat dateFormat;
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(data);
        int sourceYear = calendar.get(Calendar.YEAR);
        if (curYear == sourceYear) {
            dateFormat = setDataFormat("MM-dd");
        } else {
            dateFormat = setDataFormat("yyyy-MM-dd");
        }

        try {
            return dateFormat.format(data);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 截取年月日 如（2013-01-08 10:10:20）
     *
     * @param data
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String toNYR_HMS(long data) {
        SimpleDateFormat dateFormat;
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(data);
        int sourceYear = calendar.get(Calendar.YEAR);
        if (curYear == sourceYear) {
            dateFormat = setDataFormat("MM/dd  HH:mm:ss");
        } else {
            dateFormat = setDataFormat("yyyy/MM/dd  HH:mm:ss");
        }

        try {
            return dateFormat.format(data);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 截取年月日 如（2013-01-08）
     *
     * @param data
     * @return yyyy-MM-dd
     */
    public static String toYMHS(long data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d HH:mm");
        try {
            Date d = new Date(data);
            String str = dateFormat.format(d);
            return str;
        } catch (Exception e) {
            return "";
        }
    }

    public static SimpleDateFormat setDataFormat(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat;
    }

    /**
     * 解析去掉url路径，保留参数部分
     *
     * @param strURL
     * @return
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase((Locale.CHINA));

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    /**
     * 将所有的半角转成全角。
     *
     * @param input
     * @return
     */
    public static String ToSBC(String input) {
        if (!TextUtils.isEmpty(input)) {
            char c[] = input.toCharArray();
            for (int i = 0; i < c.length; i++) {
                if (c[i] == ' ') {
                    c[i] = '\u3000';
                } else if (c[i] < '\177') {
                    c[i] = (char) (c[i] + 65248);
                }
            }
            return new String(c);
        }
        return "";
    }


    public static Date parseStringToDateTHMS(String d) throws ParseException {
        return parse(DATETIME_FORMAT_T_SECOND, d);
    }

    public static Date parse(String f, String d) throws ParseException {
        return new SimpleDateFormat(f).parse(d);
    }

    /**
     * 毫秒转化为 00:00格式
     */
    public static String formatMillSecondClock(long ms) {
        if (ms <= 0) {
            return "00:00";
        }
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long minute = ms / mi;
        Long second = (ms - minute * mi) / ss;

        StringBuffer sb = new StringBuffer();
        if (minute > 0) {
            if (minute > 9) {
                sb.append(minute + ":");
            } else {
                sb.append("0" + minute + ":");
            }
        } else {
            sb.append("00:");
        }
        if (second > 0) {
            if (second > 9) {
                sb.append(second);
            } else {
                sb.append("0" + second);
            }
        } else {
            sb.append("00");
        }
        return sb.toString();
    }


    /**
     * @param str     接收输入的中英文字符串
     * @param byteLen 接收要截取的字节数
     */
    public static String getSubStrByByte(String str, int byteLen) {
        byte[] bytes = str.getBytes();
        boolean needSuffix = false;
        if (bytes.length > byteLen) {
            byteLen = byteLen - 3;
            needSuffix = true;
        }
        int count = 0;   //已经遍历出的字节数
        String tempStr = "";  //最终输出字符串
        List<Byte> list = new ArrayList<Byte>();  //临时存放一个中文每一个字节的列表
        for (byte b : bytes) {
            if (b >= 0) {//大于等于0表示英文
                tempStr += new String(new byte[]{b}); //直接追加到输出字符串
                count++;
            } else {
                list.add(b);   //小于0表示中文，并将字节添加到临时列表中
                if (list.size() == 3) {  //当列表长度为3时，先转换为字符数组，再转为字符并追加到输出字符串
                    byte[] temp = new byte[3];
                    int i = 0;
                    for (Byte l : list) {
                        temp[i] = (byte) l;
                        i++;
                    }
                    tempStr += new String(temp);
                    count += 3;  //一个中文字节数加3
                    list.clear(); //清空临时列表
                }
            }
            if (count == byteLen) {   //当遍历的字节数等于需要截取的字节数时则输出，并跳出循环
                break;
            }
            //当遍历的字节数减需要截取的字节数=2时则说明最后一个字符为中文，输出并跳出循环
            else if ((count - byteLen == 2)) {
                tempStr = tempStr.substring(0, tempStr.length() - 1);
                break;
            }
        }
        if (needSuffix) {
            tempStr += "...";
        }
        return tempStr;
    }

    public static String cutTitle(String title) {
        if (title.getBytes().length > 30) {
            String[] chars = title.split("|");
            int len = 0;
            StringBuilder titleBuilder = new StringBuilder();
            for (String t : chars) {
                if (len + t.getBytes().length > 27) {
                    break;
                }

                titleBuilder.append(t);
                len += t.getBytes().length;
            }
            titleBuilder.append("...");
            title = titleBuilder.toString();
        }

        return title;
    }


    /**
     * 截取年月日 如（2013-01-08 10:10:20）
     *
     * @param data
     * @return MM-dd HH:mm
     */
    public static String toMD_HM(long data) {
        SimpleDateFormat dateFormat;
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(data);
        int sourceYear = calendar.get(Calendar.YEAR);
        if (curYear == sourceYear) {
            dateFormat = setDataFormat("MM/dd  HH:mm");
        } else {
            dateFormat = setDataFormat("yyyy/MM/dd  HH:mm");
        }

        try {
            return dateFormat.format(data);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 秒钟转时分秒
     * @param dateSecond
     * @return
     */
    public static String computingTimeToHMS(int dateSecond) {
        int hour = (int) Math.ceil((dateSecond / (60 * 60)));
        int min = (int) Math.ceil((dateSecond / 60)) % 60;
        int second = (int) Math.ceil((dateSecond % 60));
        StringBuffer sb = new StringBuffer();
        if (hour > 0) {
            sb.append(hour);
            sb.append("时");
        }
        if (min > 0) {
            sb.append(min);
            sb.append("分");
        }
        if (second > 0) {
            sb.append(second);
            sb.append("秒");
        }
        return sb.toString();
    }

    public static String stringForTime(int timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
