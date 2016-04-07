package com.newclass.woyaoxue.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FragmentChoose extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private String TAG = "FragmentChoose";
    private static final int REFRESH_DATA = 1;
    private static Gson gson = new Gson();
    private boolean visible = false;
    private List<User> list;
    private MyAdapter adapter;
    private SwipeRefreshLayout srl;
    private TextView tv_time;
    private int time = 0;
    private int offset = 1;//递归时间，单位秒
    private MediaPlayer mediaPlayer;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA:
                    time += offset;
                    if (visible && time >= 60) {
                        refresh();
                    }
                    tv_time.setText(time + "");
                    sendEmptyMessageDelayed(REFRESH_DATA, offset * 1000);
                    break;
            }
        }
    };

    private void refresh() {
        time = 0;
        srl.setRefreshing(true);
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("skip", "0");
        params.add("take", "5");//每次只取最前面5个
        HttpUtil.post(NetworkUtil.teacherInQueue, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<User>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<User>>>() {
                }.getType());

                list.clear();

                List<User> info = resp.info;
                for (User u : info) {
                    list.add(u);
                }
                adapter.notifyDataSetChanged();
                srl.setVisibility(View.VISIBLE);
                srl.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                CommonUtil.toast("网络异常");
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(this);
        srl.setVisibility(View.INVISIBLE);

        ListView listview = (ListView) view.findViewById(R.id.listview);
        list = new ArrayList<User>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);

        tv_time = (TextView) view.findViewById(R.id.tv_time);
    }

    @Override
    public void onResume() {
        super.onResume();
        time = 60;
        visible = true;
        handler.removeCallbacksAndMessages(null);

        Message message = handler.obtainMessage();
        message.what = REFRESH_DATA;
        handler.sendMessage(message);
    }

    @Override
    public void onPause() {
        super.onPause();
        visible = false;
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override //srl刷新接口实现
    public void onRefresh() {
        refresh();
    }

    private class MyAdapter extends BaseAdapter<User> {
        public MyAdapter(List<User> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final User user = getItem(position);
            View inflate = View.inflate(getActivity(), R.layout.listitem_choose, null);
            TextView tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
            ImageView iv_icon = (ImageView) inflate.findViewById(R.id.iv_icon);
            tv_nickname.setText("昵称:" + user.Name);
            TextView tv_about = (TextView) inflate.findViewById(R.id.tv_about);
            tv_about.setText(user.About);

            //下载处理,如果有设置头像,则显示头像,
            //如果头像已经下载过,则加载本地图片
            if (!TextUtils.isEmpty(user.Icon)) {
                final File file = new File(getActivity().getFilesDir(), user.Icon);
                String path = file.exists() ? file.getAbsolutePath() : NetworkUtil.getFullPath(user.Icon);
                new BitmapUtils(getActivity()).display(iv_icon, path, new BitmapLoadCallBack<ImageView>() {
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

            //设置音频
            ImageView iv_voice = (ImageView) inflate.findViewById(R.id.iv_voice);
            if (!TextUtils.isEmpty(user.Voice)) {
                iv_voice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(NetworkUtil.getFullPath(user.Voice));
                            mediaPlayer.prepare(); // might take long! (for buffering, etc)
                            mediaPlayer.start();
                        } catch (Exception ex) {
                            CommonUtil.toast("音频播放失败");
                        }
                    }
                });

            }

            //设置点击
            ImageView bt_call = (ImageView) inflate.findViewById(R.id.bt_call);
            bt_call.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (NIMClient.getStatus() != StatusCode.LOGINED) {
                        getActivity().startActivity(new Intent(getActivity(), ActivitySignIn.class));
                        return;
                    }

                    HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                    parameters.add("id", getActivity().getSharedPreferences("user", Context.MODE_PRIVATE).getInt("id", 0));
                    parameters.add("target", user.Id + "");
                    HttpUtil.post(NetworkUtil.chooseTeacher, parameters, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                            }.getType());
                            if (resp.code == 200) {
                                ActivityCall.start(getActivity(), user.Id, user.Accid, user.Icon, user.Name, ActivityCall.CALL_TYPE_AUDIO);
                            } else {
                                CommonUtil.toast(resp.desc);
                            }
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            Log.i(TAG, "onFailure: " + msg);
                        }
                    });
                }
            });
            return inflate;
        }
    }
}
