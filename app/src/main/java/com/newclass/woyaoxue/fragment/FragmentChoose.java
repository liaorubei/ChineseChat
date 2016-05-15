package com.newclass.woyaoxue.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.activity.ActivityCall;
import com.newclass.woyaoxue.activity.ActivitySignIn;
import com.newclass.woyaoxue.activity.ActivityProfile;
import com.newclass.woyaoxue.activity.ActivityTake;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.XListView;
import com.newclass.woyaoxue.view.XListViewFooter;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

//#DEAE12
public class FragmentChoose extends Fragment {
    private String TAG = "FragmentChoose";
    private static final int REFRESH_DATA = 1;
    private static Gson gson = new Gson();
    private boolean visible = false;
    private List<User> list;
    private MyAdapter adapter;
    private SwipeRefreshLayout srl;
    private int time = 0;

    private MediaPlayer mediaPlayer;
    private AlertDialog dialogLogin;
    private XListView xListView;
    private int take = 50;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA:
                    int offset = 1;//递归时间，单位秒
                    time += offset;
                    if (visible && time >= 60) {
                        refresh(true);
                    }
                    //Log.i(TAG, "handleMessage: " + time);
                    sendEmptyMessageDelayed(REFRESH_DATA, offset * 1000);
                    break;
            }
        }
    };
    private boolean first = false;

    /*
    * params refresh
     */
    private void refresh(final boolean refresh) {
        time = 0;
        if (first) {//只第一次自动刷新的时候出现这个加载图标
            srl.setRefreshing(true);
            first = false;
        }

        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("skip", refresh ? 0 : list.size());
        params.add("take", take);
        HttpUtil.post(NetworkUtil.getTeacher, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<User>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<User>>>() {
                }.getType());
                if (refresh) {
                    list.clear();
                }
                List<User> info = resp.info;
                for (User u : info) {
                    list.add(u);
                }
                adapter.notifyDataSetChanged();
                srl.setRefreshing(false);
                xListView.setPullupEnable(true);
                xListView.stopLoadMore(info.size() < take ? XListViewFooter.STATE_NOMORE : XListViewFooter.STATE_NORMAL);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                CommonUtil.toast(R.string.network_error);

                srl.setRefreshing(false);
                xListView.setPullupEnable(true);
                xListView.stopLoadMore(XListViewFooter.STATE_ERRORS);
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
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });

        list = new ArrayList<User>();
        adapter = new MyAdapter(list);
        xListView = (XListView) view.findViewById(R.id.listview);
        xListView.setAdapter(adapter);
        xListView.setPullupEnable(false);
        xListView.setPullDownEnable(false);
        xListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }

            @Override
            public void onLoadMore() {
                refresh(false);
            }
        });
        xListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: " + list.get(position).Nickname);
                ActivityProfile.start(getActivity(), list.get(position-1));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        time = 60;
        visible = true;
        first = true;
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

    public class MyAdapter extends BaseAdapter<User> {
        public MyAdapter(List<User> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final User user = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.listitem_choose, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            //三种状态，在线，忙线，掉线 {绿，红，灰}1,2.3
            int status = 3;
            if (user.IsOnline) {
                status = (user.IsEnable && position < 5) ? 1 : 2;
            }

            holder.tv_nickname.setText(user.Nickname);
            holder.tv_nickname.setTextColor(user.IsEnable ? getResources().getColor(R.color.color_app) : Color.RED);
            holder.tv_nickname.setTextColor(user.IsOnline ? holder.tv_nickname.getCurrentTextColor() : getResources().getColor(R.color.color_app_normal));

            holder.tv_spoken.setText(user.Spoken);
            holder.tv_location.setText(user.Country);

            CommonUtil.showBitmap(holder.iv_avatar, NetworkUtil.getFullPath(user.Avatar));
            holder.iv_status.setImageResource(user.IsEnable ? R.drawable.teacher_online : R.drawable.teacher_busy);
            if (!user.IsOnline) {
                holder.iv_status.setImageResource(R.drawable.teacher_offline);
            }

            //设置音频
            if (!TextUtils.isEmpty(user.Voice)) {
                holder.iv_voice.setOnClickListener(new View.OnClickListener() {
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
            holder.bt_call.setBackgroundResource(user.IsOnline ? R.drawable.selector_choose_calla : R.drawable.selector_choose_callb);
            holder.bt_call.setEnabled(user.IsEnable && user.IsOnline && position < 5);
            holder.bt_call.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //如果没有登录,那么要求登录
                    if (NIMClient.getStatus() != StatusCode.LOGINED) {
                        showLoginDialog();
                        return;
                    }

                    if (!user.IsOnline) {
                        CommonUtil.toast(R.string.FragmentChoose_offline);
                        return;
                    }

                    if (!user.IsEnable) {
                        CommonUtil.toast(R.string.FragmentChoose_busy);
                        return;
                    }

                    HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                    parameters.add("id", ChineseChat.CurrentUser.Id);
                    parameters.add("target", user.Id);
                    HttpUtil.post(NetworkUtil.chooseTeacher, parameters, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                            }.getType());
                            if (resp.code == 200) {
                                ActivityCall.start(getActivity(), user.Id, user.Accid, user.Avatar, user.Nickname, ActivityCall.CALL_TYPE_AUDIO);
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
            return convertView;
        }
    }

    private class ViewHolder {
        public TextView tv_nickname, tv_location;
        public ImageView iv_avatar, iv_status;
        public TextView tv_spoken;
        public ImageView bt_call;
        public ImageView iv_voice;

        public ViewHolder(View convertView) {
            this.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            this.iv_status = (ImageView) convertView.findViewById(R.id.iv_status);
            this.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
            this.tv_spoken = (TextView) convertView.findViewById(R.id.tv_spoken);
            this.bt_call = (ImageView) convertView.findViewById(R.id.bt_call);
            this.tv_location = (TextView) convertView.findViewById(R.id.tv_location);
        }
    }

    private void showLoginDialog() {
        if (dialogLogin == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.fragment_person_dialog_title);
            builder.setMessage(R.string.fragment_person_dialog_message);
            builder.setPositiveButton(R.string.fragment_person_dialog_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.fragment_person_dialog_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialogLogin = builder.create();
        }
        dialogLogin.show();
    }
}
