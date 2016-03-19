package com.newclass.woyaoxue.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by liaorubei on 2016/1/14.
 */
public class AdapterLineup extends BaseAdapter<User> {
    private static final String TAG = "AdapterLineup";
    private Context mContext;

    public AdapterLineup(List<User> list, Context context) {
        super(list);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        String accid = mContext.getSharedPreferences("user", Context.MODE_PRIVATE).getString("accid", "");
        View inflate = View.inflate(mContext, R.layout.listitem_teacherqueue, null);
        TextView tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
        ImageView iv_icon = (ImageView) inflate.findViewById(R.id.iv_icon);

        //昵称
        tv_nickname.setText(user.Name + (accid.equals(user.Accid) ? "(本人)" : ""));

        //下载处理,如果有设置头像,则显示头像,
        //如果头像已经下载过,则加载本地图片
        if (!TextUtils.isEmpty(user.Icon)) {
            final File file = new File(mContext.getFilesDir(), user.Icon);
            String path = file.exists() ? file.getAbsolutePath() : NetworkUtil.getFullPath(user.Icon);
            new BitmapUtils(mContext).display(iv_icon, path, new BitmapLoadCallBack<ImageView>() {
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
                    Log.i(TAG, "onLoadCompleted: uri=" + uri);
                }

                @Override
                public void onLoadFailed(ImageView container, String uri, Drawable drawable) {
                    container.setImageResource(R.drawable.ic_launcher_student);
                    Log.i(TAG, "onLoadFailed: ");
                }
            });
        }


        return inflate;
    }
}
