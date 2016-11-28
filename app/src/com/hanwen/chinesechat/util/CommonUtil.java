package com.hanwen.chinesechat.util;

import com.hanwen.chinesechat.bean.Hskk;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.*;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class CommonUtil {

    private static final String TAG = "CommonUtil";
    private static BitmapUtils bitmapUtils;

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

    public static void saveUserToSP(Context context, User user, boolean savePassword) {
        SharedPreferences.Editor editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
        editor.putInt("id", user.Id);
        editor.putString("accid", user.Accid);
        editor.putString("token", user.Token);
        editor.putString("nickname", user.Nickname);
        editor.putInt("gender", user.Gender);
        editor.putString("avatar", user.Avatar);
        editor.putString("email", user.Email);
        editor.putString("birth", user.Birth);
        editor.putString("mobile", user.Mobile);
        editor.putInt("coins", user.Coins);

        editor.putString("country", user.Country);
        editor.putString("language", user.Language);
        editor.putString("job", user.Job);
        editor.putString("about", user.About);
        editor.putString("school", user.School);
        editor.putString("hobbies", user.Hobbies);
        editor.putString("spoken", user.Spoken);
        editor.putStringSet("photos", user.Photos);

        editor.putString("username", user.Username);
        if (savePassword) {
            editor.putString("password", user.PassWord);
        }
        editor.commit();
    }

    /**
     * 显示图片，会优先使用缓存图片，如果本地没有，则请求网络图片
     *
     * @param context      上下文
     * @param imageView    要显示的图片控件
     * @param relativePath 相对路径，如果在本地查不到图片，则自动添加网络图片的域名，组成全路径
     */
    public static void showIcon(Context context, ImageView imageView, String relativePath) {
        if (!TextUtils.isEmpty(relativePath)) {
            final File file = new File(context.getFilesDir(), relativePath);
            String path = file.exists() ? file.getAbsolutePath() : NetworkUtil.getFullPath(relativePath);
            new BitmapUtils(context).display(imageView, path, new BitmapLoadCallBack<ImageView>() {
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
                    container.setImageResource(ChineseChat.isStudent() ? R.drawable.ic_launcher_student : R.drawable.ic_launcher_teacher);
                }
            });
        } else {
            imageView.setImageResource(ChineseChat.isStudent() ? R.drawable.ic_launcher_student : R.drawable.ic_launcher_teacher);
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

    /**
     * 显示图片,并缓存,如果缓存中已经存在图片,则从缓存读取
     *
     * @param imageView    要显示图片的控件
     * @param absolutePath 图片的全路径,如域名
     */
    public static void showBitmap(ImageView imageView, String absolutePath) {
        if (bitmapUtils == null) {
            bitmapUtils = new BitmapUtils(ChineseChat.getContext(), ChineseChat.getContext().getCacheDir().getAbsolutePath());
        }
        if (TextUtils.isEmpty(absolutePath)) {
            return;
        }
        bitmapUtils.display(imageView, absolutePath);
    }

    public static String hskkRank(int rank) {
        switch (rank) {
            case 1:
                return "初级";
            case 2:
                return "中级";
            case 3:
                return "高级";
            default:
                return "异常";
        }
    }

    public static String hskkPart(int rank, int part) {
        String result = "未知";
        switch (rank) {
            case 1:

                switch (part) {
                    case 1:
                        result = "听后重复";
                        break;
                    case 2: result = "听后回答";
                        break;
                    case 3: result = "回答问题";
                        break;
                }
                break;
            case 2:
                switch (part) {
                    case 1:
                        result = "听后重复";
                        break;
                    case 2: result = "看图说话";
                        break;
                    case 3: result = "回答问题";
                        break;
                }
                break;
            case 3:
                switch (part) {
                    case 1:
                        result = "听后复述";
                        break;
                    case 2: result = "朗读";
                        break;
                    case 3: result = "回答问题";
                        break;
                }
                break;
        }

        return result;
    }

    /**
     * 给HSKK实体附加说明数据，因为这样说明是一样的，所以服务器不用保存这些数据
     *
     * @param hskk
     */
    public static void hskkDesc(Hskk hskk) {
        switch (hskk.Rank) {
            case 1:
                hskk.RankName = "初级";
                //第一部分是听后重复，共15题。每题播放一个句子，考生听后重复一次。考试时间为6分钟。
                //第二部分是听后回答，共10题。每题播放一个问题，考生听后做简短回答。考试时间为4分钟。
                //第三部分是回答问题，共2题。试卷上提供两个问题（加拼音），考生回答问题，每题至少说5句话。考试时
                switch (hskk.Part) {
                    case 1:
                        hskk.PartName = "听后重复";
                        hskk.Desc = "现在开始第1到15题。每题你会听到一个句子，请在老师读完后重复这个句子。现在开始第1题。";
                        break;
                    case 2:
                        hskk.PartName = "听后回答";
                        hskk.Desc = "现在开始第16题到25题。每题你会听到一个问题，请在老师问完后回答这个问题。现在开始第16题。";
                        break;
                    case 3:
                        hskk.PartName = "回答问题";
                        hskk.Desc = "现在开始第26到27题。可以写提纲，准备时间为7分钟。";
                        break;
                    default:
                        hskk.PartName = "未知";
                        hskk.Desc = "未知";
                        break;
                }
                break;
            case 2:
                hskk.RankName = "中级";
                //第一部分是听后重复，共10题。每题播放一个句子，考生听后重复一次。考试时间为5分钟。
                //第二部分是看图说话，共2题。每题提供一张图片，考生结合图片说一段话。考试时间为4分钟。
                //第三部分是回答问题，共2题。试卷上提供两个问题（加拼音），考生回答问题。考试时间为4分钟。
                switch (hskk.Part) {
                    case 1:
                        hskk.PartName = "听后重复";
                        hskk.Desc = "现在开始第1到10题。每题你会听到一个句子，请在老师读完后重复这个句子。现在开始第1题。";
                        break;
                    case 2:
                        hskk.PartName = "看图说话";
                        hskk.Desc = "现在开始准备第11到12题，可以写提纲，准备时间为7分钟。";
                        break;
                    case 3:
                        hskk.PartName = "回答问题";
                        hskk.Desc = "现在开始准备第13到14题，可以写提纲，准备时间为7分钟。";
                        break;
                    default:
                        hskk.PartName = "未知";
                        hskk.Desc = "未知";
                        break;
                }
                break;
            case 3:
                hskk.RankName = "高级";
                //第一部分是听后复述，共3题。每题播放一段话，考生听后复述。考试时间为8分钟。
                //第二部分是朗读，共1题。试卷上提供一段文字，考生朗读。考试时间是2分钟。
                //第三部分是回答问题，共2题。试卷上提供两个问题，考生读后回答问题。考试时间是5分钟。
                switch (hskk.Part) {
                    case 1:
                        hskk.PartName = "听后复述";
                        hskk.Desc = "现在开始第1到3题。每题你会听到一段话，请在老师读完后复述这段话。现在开始第1题。";
                        break;
                    case 2:
                        hskk.PartName = "朗读";
                        hskk.Desc = "现在开始准备第4题，准备时间为3分钟。";
                        break;
                    case 3:
                        hskk.PartName = "回答问题";
                        hskk.Desc = "现在开始准备第5到6题，准备时间为7分钟。";
                        break;
                    default:
                        hskk.PartName = "未知";
                        hskk.Desc = "未知";
                        break;
                }
                break;
            default:
                hskk.RankName = "未知";
                switch (hskk.Part) {
                    case 1:
                        hskk.PartName = "";
                        hskk.Desc = "";
                        break;
                    case 2:
                        hskk.PartName = "";
                        hskk.Desc = "";
                        break;
                    case 3:
                        hskk.PartName = "";
                        hskk.Desc = "";
                        break;
                    default:
                        hskk.PartName = "未知";
                        hskk.Desc = "未知";
                        break;
                }
                break;
        }
    }
}
