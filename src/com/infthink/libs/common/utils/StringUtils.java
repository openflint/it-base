package com.infthink.libs.common.utils;

import java.net.URLDecoder;
import java.security.MessageDigest;

public class StringUtils implements IDebuggable {

    /**
     * 将url格式化为符合文件名规则的32位字符串
     * @param url
     * @return
     */
    public static String convertToFilename(String url) {
        String resultString = null;
        try {
            resultString = URLDecoder.decode(url, "UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = toHexString(md.digest(resultString.getBytes()));
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    private static String toHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            int value = bytes[i];
            sb.append(Integer.toHexString((value >> 4) & 0x0f));
            sb.append(Integer.toHexString(value& 0x0f));
        }
        return sb.toString();
    }

    /**
     * <pre>
     *  appendUrl("http://www.abc.com","a=1&b=2");
     *  // http://www.abc.com?a=1&b=2
     *  
     *  appendUrl("http://www.abc.com?c=0","a=1&b=2");
     *  // http://www.abc.com?c=0&a=1&b=2
     * </pre>
     * 
     * @param url
     * @param args
     * @return
     */
    public static String appendUrl(String url, String args) {
        if (url != null) {
            int index = url.indexOf('?');
            if (index != -1) {
                url = url + "&" + args;
            } else {
                url = url + "?" + args;
            }
        }
        return url;
    }

    /**
     * @param str
     * @return 如果str为null或者长度为0，返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * @param str
     * @return 如果str为null返回长度为0的字符串，否则返回trim后的值
     */
    public static String trimToEmpty(String str) {
        return isEmpty(str) ? "" : str.trim();
    }

}
