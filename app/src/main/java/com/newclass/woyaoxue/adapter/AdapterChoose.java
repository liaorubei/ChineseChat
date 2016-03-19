package com.newclass.woyaoxue.adapter;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.activity.ActivityCall;
import com.newclass.woyaoxue.activity.ActivitySignIn;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by liaorubei on 2016/1/13.
 */
public class AdapterChoose extends BaseAdapter<User> {
    private static final String TAG = "AdapterChoose";
    private Context mContext;


    public AdapterChoose(List<User> list, Context context) {
        super(list);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final User user = getItem(position);
        View inflate = View.inflate(mContext, R.layout.listitem_choose, null);
        TextView tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
        ImageView iv_icon = (ImageView) inflate.findViewById(R.id.iv_icon);
        tv_nickname.setText("昵称:" + user.Name);

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

        ImageView bt_call = (ImageView) inflate.findViewById(R.id.bt_call);
        bt_call.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    mContext.startActivity(new Intent(mContext, ActivitySignIn.class));
                    return;
                }

                HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                parameters.add("id", 1 + "");
                parameters.add("target", user.Id + "");
                HttpUtil.post(NetworkUtil.chooseTeacher, parameters, new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        ActivityCall.start(mContext, user.Id, user.Accid, user.NickName, ActivityCall.CALL_TYPE_AUDIO);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                    }
                });
            }
        });

        return inflate;
    }
}
