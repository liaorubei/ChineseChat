package com.hanwen.chinesechat.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ChineseChat on 2017/2/17.
 */
public class TextUtil {
    public static boolean isEmail(String username) {
        //验证邮箱格式
        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(username);
        return matcher.matches();
    }
}
