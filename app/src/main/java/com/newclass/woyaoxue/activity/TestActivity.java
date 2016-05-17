package com.newclass.woyaoxue.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Service;
import android.app.LauncherActivity.ListItem;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewDebug.FlagToString;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.newclass.woyaoxue.MainActivity;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Document;
import com.newclass.woyaoxue.service.DownloadService;
import com.newclass.woyaoxue.service.DownloadService.DownloadManager;
import com.newclass.woyaoxue.service.DownloadService.MyBinder;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.FylxListView;
import com.voc.woyaoxue.R;

public class TestActivity extends FragmentActivity {
    private CheckBox cb;
    private PopupWindow window;
    private boolean isShow = false;
    private boolean isDown = false;
    private ImageView iv;
    private View pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        pd = findViewById(R.id.pd);
        iv = (ImageView) findViewById(R.id.iv);

/*        new BitmapUtils(getApplicationContext(), getCacheDir().getAbsolutePath()).display(iv, NetworkUtil.getFullPath("/File/20160511/446d3cfb-c9bc-45df-9c9a-2185a5b7af44.jpg"), new BitmapLoadCallBack<ImageView>() {
            @Override
            public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {

            }

            @Override
            public void onLoadFailed(ImageView container, String uri, Drawable drawable) {

            }
        });*/

        new BitmapUtils(getApplicationContext(), getCacheDir().getAbsolutePath()).display(iv, NetworkUtil.getFullPath("/File/20160511/446d3cfb-c9bc-45df-9c9a-2185a5b7af44.jpg"));
        ActivityAlbum.start(this, new String[]{"/File/20160511/446d3cfb-c9bc-45df-9c9a-2185a5b7af44.jpg", "/File/20160511/446d3cfb-c9bc-45df-9c9a-2185a5b7af44.jpg", "/File/20160511/ab3581c0-bbff-4496-a9fe-01957b2fdf77.jpg"});

    }

}
