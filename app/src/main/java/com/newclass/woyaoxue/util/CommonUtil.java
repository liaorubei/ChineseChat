package com.newclass.woyaoxue.util;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.bean.User;
import com.voc.woyaoxue.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class CommonUtil {

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(int resId) {
        Toast.makeText(ChineseChat.getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public static void toast(String text) {
        toast(ChineseChat.getContext(), text);
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
        editor.putString("avatar", user.Avatar);
        editor.putString("email", user.Email);
        editor.putString("birth", user.Birth);
        editor.putString("mobile", user.Mobile);
        editor.putInt("coins", user.Coins);

        editor.putString("country", user.Country);
        editor.putString("language", user.Language);
        editor.putString("vocation", user.Job);

        editor.putString("username", user.Username);
        editor.putString("password", user.PassWord);
        editor.commit();
    }

    /*
    * 显示头像专用，因为头像的图片在网络上的位置和手机本地的位置是相对一样的
    *
    * */
    public static void showIcon(Context context, ImageView iv_icon, String icon) {
        if (!TextUtils.isEmpty(icon)) {
            final File file = new File(context.getFilesDir(), icon);
            String path = file.exists() ? file.getAbsolutePath() : NetworkUtil.getFullPath(icon);
            new BitmapUtils(context).display(iv_icon, path, new BitmapLoadCallBack<ImageView>() {
                @Override
                public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
                    container.setImageBitmap(bitmap);

                    //缓存处理,如果本地照片已经保存过,则不做保存处理
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onLoadFailed(ImageView container, String uri, Drawable drawable) {
                    container.setImageResource(R.drawable.ic_launcher_student);
                }
            });
        }
    }

    public static void toastCENTER(int resId) {
        Toast toast = Toast.makeText(ChineseChat.getContext(), resId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void toastTOP(String text) {
        Toast toast = Toast.makeText(ChineseChat.getContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, ChineseChat.getContext().getResources().getDisplayMetrics()));
        toast.show();
    }
}
