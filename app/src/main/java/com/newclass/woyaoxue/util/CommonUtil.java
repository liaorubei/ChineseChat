package com.newclass.woyaoxue.util;

import com.newclass.woyaoxue.MyApplication;
import com.newclass.woyaoxue.activity.SignInActivity;
import com.newclass.woyaoxue.bean.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class CommonUtil {

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(String text) {
        toast(MyApplication.getContext(), text);
    }

    public static String millisecondsFormat(long milliseconds) {
        long minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (milliseconds % (1000 * 60)) / 1000;
        return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }


    public static void saveUserToSP(Context context, User user) {
        SharedPreferences.Editor editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
        editor.putInt("id", user.Id);
        editor.putString("accid", user.Accid);
        editor.putString("token", user.Token);
        editor.putString("nickname", user.NickName);
        editor.putInt("gender", user.Gender);
        editor.putString("avater", user.Avater);
        editor.putString("email", user.Email);
        editor.putString("birth", user.Birth);
        editor.putString("mobile", user.Mobile);

        editor.putString("username", user.UserName);
        editor.putString("password", user.PassWord);
        editor.commit();
    }


}
