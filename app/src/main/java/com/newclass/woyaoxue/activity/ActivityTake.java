package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.newclass.woyaoxue.Observer.ObserverHangup;
import com.newclass.woyaoxue.Observer.ObserverTimeOut;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.NimSysNotice;
import com.newclass.woyaoxue.bean.Question;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.Theme;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 电话接听界面
 *
 * @author liaorubei
 */
public class ActivityTake extends Activity implements OnClickListener {

    protected static final String TAG = "ActivityTake";
    public static String KEY_CHAT_DATA = "KEY_CHAT_DATA";

    private View bt_hangup, bt_reject, bt_accept, bt_mute, bt_free;
    private Gson gson = new Gson();
    private ImageView iv_icon;
    private boolean IS_CALL_ESTABLISHED = false;//通话是否已经建立
    private User target;
    private TextView tv_nickname, tv_time, tv_theme;
    private AVChatData avChatData;
    private LinearLayout ll_theme;
    private Chronometer cm_time;
    private ListView listview;
    private View ll_hang, ll_call;
    private List<String> list;
    private MyAdapter adapter;

    private Observer_ChatState observerChatState;
    private Observer<AVChatCommonEvent> observerHangup;
    private Observer<AVChatTimeOutEvent> observerTimeout;
    private AVChatCallback<Void> callbackAccept;
    private AVChatCallback<Void> callbackHangup;

    // 自定义系统通知的广播接收者
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 从 intent 中取出自定义通知， intent 中只包含了一个 CustomNotification 对象
            CustomNotification notification = (CustomNotification) intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
            NimSysNotice<Theme> notice = gson.fromJson(notification.getContent(), new TypeToken<NimSysNotice<Theme>>() {
            }.getType());

            if (notice.NoticeType == NimSysNotice.NoticeType_Card) {
                CommonUtil.toast("对方点击了:" + notice.info.Name);
                Parameters params = new Parameters();
                params.add("chatId", avChatData.getChatId());
                params.add("themeId", notice.info.Id);
                HttpUtil.post(NetworkUtil.chatAddTheme, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "onSuccess: " + responseInfo.result);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.i(TAG, "onFailure: " + msg);
                    }
                });
            }

            showThemeQuestion(notice.info);
        }
    };


    public static void start(Context context, AVChatData avChatData) {
        Intent intent = new Intent(context, ActivityTake.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CHAT_DATA, avChatData);
        context.startActivity(intent);
    }

    private void initData() {
        Intent intent = getIntent();
        avChatData = (AVChatData) intent.getSerializableExtra(KEY_CHAT_DATA);
        target = new User();
        target.Accid = avChatData.getAccount();

        //取得来电用户头像
        Parameters parameters = new Parameters();
        parameters.add("accid", target.Accid);
        HttpUtil.post(NetworkUtil.userGetByAccId, parameters, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());
                if (resp.code == 200) {
                    target.Id = resp.info.Id;
                    target.Nickname = resp.info.Nickname;
                    tv_nickname.setText(target.Nickname);

                    //下载处理,如果有设置头像,则显示头像,
                    //如果头像已经下载过,则加载本地图片
                    CommonUtil.showIcon(getApplicationContext(), iv_icon, resp.info.Icon);
                }
            }
        });
    }

    private void initView() {
        //头像
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        tv_time = (TextView) findViewById(R.id.tv_time);
        cm_time = (Chronometer) findViewById(R.id.cm_time);

        //控制按钮,布局
        bt_mute = findViewById(R.id.bt_mute);
        bt_hangup = findViewById(R.id.bt_hangup);
        bt_free = findViewById(R.id.bt_free);

        bt_reject = findViewById(R.id.bt_reject);
        bt_accept = findViewById(R.id.bt_accept);

        ll_call = findViewById(R.id.ll_call);
        ll_hang = findViewById(R.id.ll_hang);

        //主题,布局
        ll_theme = (LinearLayout) findViewById(R.id.ll_theme);
        tv_theme = (TextView) findViewById(R.id.tv_theme);
        listview = (ListView) findViewById(R.id.listview);

        bt_mute.setOnClickListener(this);
        bt_hangup.setOnClickListener(this);
        bt_free.setOnClickListener(this);
        bt_reject.setOnClickListener(this);
        bt_accept.setOnClickListener(this);

        ll_call.setVisibility(View.INVISIBLE);
        ll_hang.setVisibility(View.VISIBLE);

        list = new ArrayList<>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_reject: {
                Animation animation = new ScaleAnimation(1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F);
                animation.setDuration(500);
                ll_hang.startAnimation(animation);
                AVChatManager.getInstance().hangUp(callbackHangup);
            }
            break;

            case R.id.bt_accept: {
                Animation animation = new ScaleAnimation(1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F);
                animation.setDuration(500);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ScaleAnimation scale = new ScaleAnimation(0F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F);
                        scale.setDuration(500);
                        ll_call.startAnimation(scale);
                        ll_call.setVisibility(View.VISIBLE);
                        ll_hang.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                ll_hang.startAnimation(animation);
                AVChatManager.getInstance().accept(null, callbackAccept);
            }
            break;
            case R.id.bt_mute: {
                //是否静音
                AVChatManager.getInstance().setMute(!AVChatManager.getInstance().isMute());
                bt_mute.setSelected(AVChatManager.getInstance().isMute());
            }
            break;
            case R.id.bt_hangup:
                if (IS_CALL_ESTABLISHED) {
                    Builder builder = new Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    builder.setNegativeButton(R.string.ActivityTake_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton(R.string.ActivityTake_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AVChatManager.getInstance().hangUp(callbackHangup);
                        }
                    });
                    builder.setMessage(R.string.ActivityTake_hangup);
                    builder.show();
                } else {
                    AVChatManager.getInstance().hangUp(callbackHangup);
                }
                break;
            case R.id.bt_free: {
                // 是否外放
                AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
                bt_free.setSelected(!AVChatManager.getInstance().speakerEnabled());
            }
            break;
            case R.id.iv_icon:
                Theme k = new Theme();
                k.Id = 11;
                showThemeQuestion(k);
                break;

            case R.id.bt_face: {
                ActivityHistory.start(ActivityTake.this, avChatData.getAccount());
            }
            break;

            default:
                break;
        }
    }

    private void showThemeQuestion(Theme theme) {
        //主题名称
        tv_theme.setText(theme.Name);
        tv_theme.setVisibility(View.VISIBLE);
        //主题问题
        Parameters params = new Parameters();
        params.add("id", "" + theme.Id);
        HttpUtil.post(NetworkUtil.themeGetById, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<Theme> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<Theme>>() {
                }.getType());

                list.clear();
                if (resp.code == 200) {
                    List<Question> questions = resp.info.Questions;
                    for (Question q : questions) {
                        list.add(q.Name);
                    }
                }
                adapter.notifyDataSetChanged();
                ll_theme.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take);

        initView();
        initData();

        callbackHangup = new MyAVChatCallback(true);
        callbackAccept = new MyAVChatCallback(false);

        registerObserver(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(this.getPackageName() + NimIntent.ACTION_RECEIVE_CUSTOM_NOTIFICATION);
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CommonUtil.toast(R.string.ActivityTake_can_not_back);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        recordChatFinish();
        registerObserver(false);
        unregisterReceiver(receiver);
    }

    private void registerObserver(boolean register) {
        if (observerChatState == null) {
            observerChatState = new Observer_ChatState();
        }
        if (observerHangup == null) {
            observerHangup = new ObserverHangup(this);
        }
        if (observerTimeout == null) {
            observerTimeout = new ObserverTimeOut(this);
        }


        //监听通话过程中状态变化
        AVChatManager.getInstance().observeAVChatState(observerChatState, register);

        // 监听网络通话对方挂断的通知,即在正常通话时,结束通话
        AVChatManager.getInstance().observeHangUpNotification(observerHangup, register);

        // 监听呼叫或接听超时通知
        // 主叫方在拨打网络通话时，超过 45 秒被叫方还未接听来电，则自动挂断。
        // 被叫方超过 45 秒未接听来听，也会自动挂断
        // 在通话过程中网络超时 30 秒自动挂断。
        AVChatManager.getInstance().observeTimeoutNotification(observerTimeout, register);
    }

    private class Observer_ChatState implements AVChatStateObserver {
        @Override
        public void onConnectedServer(int i) {
            Log.i(TAG, "onConnectedServer: " + i);
        }

        @Override
        public void onUserJoin(String s) {
            Log.i(TAG, "onUserJoin: " + s);
        }

        @Override
        public void onUserLeave(String s, int i) {
            Log.i(TAG, "onUserLeave: " + s + " i=" + i);
        }

        @Override
        public void onProtocolIncompatible(int i) {
            Log.i(TAG, "onProtocolIncompatible: ");
        }

        @Override
        public void onDisconnectServer() {
            Log.i(TAG, "onDisconnectServer: ");
        }

        @Override
        public void onNetworkStatusChange(int i) {
            Log.i(TAG, "onNetworkStatusChange: ");
        }

        @Override
        public void onCallEstablished() {
            Log.i(TAG, "onCallEstablished: ");
            cm_time.setBase(SystemClock.elapsedRealtime());
            IS_CALL_ESTABLISHED = true;
            AVChatManager.getInstance().setSpeaker(true);
            bt_mute.setSelected(AVChatManager.getInstance().isMute());
            bt_free.setSelected(!AVChatManager.getInstance().speakerEnabled());
        }

        @Override
        public void onOpenDeviceError(int i) {
            Log.i(TAG, "onOpenDeviceError: ");
        }
    }

    private class MyAVChatCallback implements AVChatCallback<Void> {
        private boolean mIsHangup;

        public MyAVChatCallback(boolean isHangup) {
            this.mIsHangup = isHangup;
        }

        @Override
        public void onSuccess(Void aVoid) {
            Log.i(TAG, "mIsHangup=" + mIsHangup);
            if (mIsHangup) {
                finish();
            }
        }

        @Override
        public void onFailed(int i) {

        }

        @Override
        public void onException(Throwable throwable) {

        }
    }

    //记录通话结束情况,如果通话成功后己方挂断,对方挂断,通话后网络超时挂断,都要记录
    private void recordChatFinish() {
        if (IS_CALL_ESTABLISHED) {
            Parameters params = new Parameters();
            params.add("chatId", avChatData.getChatId());
            HttpUtil.post(NetworkUtil.callFinish, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "recordChatFinish_Success: " + responseInfo.result);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "recordChatFinish_Failure: " + msg);
                }
            });
        }
    }

    private class MyAdapter extends BaseAdapter<String> {

        public MyAdapter(List<String> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String item = getItem(position);
            View inflate = View.inflate(getApplication(), R.layout.listitem_call, null);
            TextView textview = (TextView) inflate.findViewById(R.id.textview);
            textview.setText(item);
            textview.setText(item);
            return inflate;
        }
    }
}
