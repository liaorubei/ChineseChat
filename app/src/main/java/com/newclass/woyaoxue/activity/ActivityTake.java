package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.JsonReader;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.Observer.ObserverHangup;
import com.newclass.woyaoxue.Observer.ObserverTimeOut;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.CallLog;
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

import org.json.JSONException;
import org.json.JSONObject;

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
    private TextView tv_nickname, tv_time, tv_theme;
    private AVChatData avChatData;
    private LinearLayout ll_theme;
    private Chronometer cm_time;
    private ListView listview;
    private View ll_hang, ll_call;
    private List<String> list;
    private MyAdapter adapter;


    // 自定义系统通知的广播接收者
    private BroadcastReceiver CustomNotificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 从 intent 中取出自定义通知， intent 中只包含了一个 CustomNotification 对象
            CustomNotification notification = (CustomNotification) intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
            Log.i(TAG, "收到自定义通知: " + notification.getContent());
            int type = -1;
            String info = null;
            try {
                JSONObject jsonObject = new JSONObject(notification.getContent());
                type = jsonObject.getInt("type");
                info = jsonObject.getString("info");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (type == NimSysNotice.NoticeType_Card) {
                Theme theme = gson.fromJson(info, Theme.class);
                CommonUtil.toast("对方点击了:" + theme.Name);
                Parameters params = new Parameters();
                params.add("chatId", avChatData.getChatId());
                params.add("themeId", theme.Id);
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

                showThemeQuestion(theme);
            } else if (type == NimSysNotice.NoticeType_Call) {
                CommonUtil.toast("Very sorry!I have a phone call,Please wait a moment.");
            } else if (type == NimSysNotice.NoticeType_Chat) {
                CALL_ID_RECEIVE = true;
                callId = info;
            }
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent.getAction());
            if ("android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if (IS_CALL_ESTABLISHED) {
                    if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                        AVChatManager.getInstance().setMute(true);

                        //发送自定义通知,通知对方目前本机有外呼电话进来
                        NimSysNotice<String> notice = new NimSysNotice<String>();
                        notice.type = NimSysNotice.NoticeType_Call;

                        CustomNotification notification = new CustomNotification();
                        notification.setSessionId(avChatData.getAccount());
                        notification.setSessionType(SessionTypeEnum.P2P);
                        notification.setContent(gson.toJson(notice));
                        NIMClient.getService(MsgService.class).sendCustomNotification(notification);
                    } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                        AVChatManager.getInstance().setMute(false);
                    }
                }
            }
        }
    };
    private User student;
    private String callId;
    private boolean CALL_ID_RECEIVE = false;

    public static void start(Context context, AVChatData avChatData) {
        Intent intent = new Intent(context, ActivityTake.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CHAT_DATA, avChatData);
        context.startActivity(intent);
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

                //public abstract void accept(VideoChatParam videoParam,boolean serverAudioRecord,boolean serverVideoRecord,AVChatCallback<java.lang.Void> callback)
                //videoParam - 视频通话渲染视频所需的参数，接听音频通话传null
                //serverAudioRecord - 服务器是否录制语音(还需要后台额外的配置)
                //serverVideoRecord - 服务器是否录制视频(还需要后台额外的配置)
                //callback - 回调函数，返回接听后，本地音视频设备启动是否成功。 回调onSuccess表示成功；回调onFailed表示失败，错误码-1表示初始化引擎失败，需要重试。 注意：由于音视频引擎析构需要时间，请尽可能保持上一次通话挂断到本次电话接听时间间隔在2秒以上，否则有可能在接听时出现初始化引擎失败（code = -1）
                AVChatManager.getInstance().accept(null, false, false, callbackAccept);
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
                    Builder builder = new Builder(this);
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_take);

        initView();
        initData();
        registerObserver(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(this.getPackageName() + NimIntent.ACTION_RECEIVE_CUSTOM_NOTIFICATION);
        registerReceiver(CustomNotificationReceiver, filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(broadcastReceiver, filter1);
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

    private void initData() {
        Intent intent = getIntent();
        avChatData = (AVChatData) intent.getSerializableExtra(KEY_CHAT_DATA);

        //如果拨打方云信SDK版本过低,不支持avChatData.getExtra(),那就不显示头像吧
        try {
            student = gson.fromJson(avChatData.getExtra(), new TypeToken<User>() {
            }.getType());

            tv_nickname.setText(student.Nickname);
            CommonUtil.showIcon(getApplicationContext(), iv_icon, student.Avatar);
        } catch (Exception ex) {
            Log.i(TAG, "initData: 对方云信SDK版本过低");
        }

        cm_time.start();
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
        registerObserver(false);
        unregisterReceiver(CustomNotificationReceiver);
        unregisterReceiver(broadcastReceiver);
    }

    private void registerObserver(boolean register) {
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

    //接听回调
    private AVChatCallback<Void> callbackAccept = new AVChatCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Log.i(TAG, "回应成功: ");

            Log.i(TAG, "CALL_ID_RECEIVE: " + CALL_ID_RECEIVE);
            //创建记录
            chatHistoryCreate();

            //定时挂断
        }

        @Override
        public void onFailed(int i) {
            Log.i(TAG, "回应失败: " + i);
        }

        @Override
        public void onException(Throwable throwable) {
            Log.i(TAG, "回应异常: " + throwable.getMessage());
        }
    };

    private AVChatCallback<Void> callbackHangup = new AVChatCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Log.i(TAG, "挂断成功回调: ");
            if (!TextUtils.isEmpty(callId)) {
                chatHistoryFinish();
            }
            finish();
        }

        @Override
        public void onFailed(int i) {
            Log.i(TAG, "挂断失败回调: " + i);
            if (!TextUtils.isEmpty(callId)) {
                chatHistoryFinish();
            }
            finish();
        }

        @Override
        public void onException(Throwable throwable) {
            Log.i(TAG, "挂断异常回调: " + throwable.getMessage());
            if (!TextUtils.isEmpty(callId)) {
                chatHistoryFinish();
            }
            finish();
        }
    };

    private AVChatStateObserver observerChatState = new AVChatStateObserver() {
        @Override
        public void onConnectedServer(int i, String s, String s1) {

        }

        @Override
        public void onUserJoin(String s) {
            Log.i(TAG, "onUserJoin: " + s);
        }

        @Override
        public void onUserLeave(String s, int i) {
            Log.i(TAG, "onUserLeave: " + s);
        }

        @Override
        public void onProtocolIncompatible(int i) {

        }

        @Override
        public void onDisconnectServer() {

        }

        @Override
        public void onNetworkStatusChange(int i) {

        }

        @Override
        public void onCallEstablished() {
            Log.i(TAG, "onCallEstablished: ");
            cm_time.setBase(SystemClock.elapsedRealtime());
            IS_CALL_ESTABLISHED = true;
            AVChatManager.getInstance().setSpeaker(true);
        }

        @Override
        public void onOpenDeviceError(int i) {

        }

        @Override
        public void onRecordEnd(String[] strings, int i) {

        }
    };

    private Observer<AVChatCommonEvent> observerHangup = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent event) {
            Log.i(TAG, "对方挂断监听: ChatId=" + event.getChatId());
            if (!TextUtils.isEmpty(callId)) {
                chatHistoryFinish();
            }
            finish();
        }
    };

    private Observer<AVChatTimeOutEvent> observerTimeout = new Observer<AVChatTimeOutEvent>() {
        @Override
        public void onEvent(AVChatTimeOutEvent avChatTimeOutEvent) {
            Log.i(TAG, "超时监听=" + avChatTimeOutEvent.name());
            finish();
        }
    };

    private void chatHistoryCreate() {

        Parameters parameters = new Parameters();
        parameters.add("chatId", avChatData.getChatId());
        parameters.add("chatType", avChatData.getChatType().getValue());
        parameters.add("target", ChineseChat.CurrentUser.Id);
        parameters.add("source", student.Id);
        HttpUtil.post(NetworkUtil.callCreate, parameters, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "记录创建成功:" + responseInfo.result);
                Response<CallLog> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<CallLog>>() {
                }.getType());

                //记录callId
                if (resp.code == 200) {
                    callId = resp.info.Id;
                }

                //发送callId
                Log.i(TAG, "发送CallId: " + !CALL_ID_RECEIVE);
                if (!CALL_ID_RECEIVE) {
                    NimSysNotice<String> notice = new NimSysNotice<String>();
                    notice.type = NimSysNotice.NoticeType_Chat;
                    notice.info = callId;

                    CustomNotification notification = new CustomNotification();
                    notification.setSessionId(avChatData.getAccount());
                    notification.setSessionType(SessionTypeEnum.P2P);
                    notification.setContent(gson.toJson(notice));
                    NIMClient.getService(MsgService.class).sendCustomNotification(notification);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "记录创建失败: " + msg);
            }
        });
    }

    private void chatHistoryFinish() {
        Parameters parameters = new Parameters();
        parameters.add("callId", callId);
        HttpUtil.post(NetworkUtil.callFinish, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "记录结束成功:" + responseInfo.result);

                //如果是学生端,保存学币信息
                if (ChineseChat.isStudent()) {
                    Response<CallLog> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<CallLog>>() {
                    }.getType());
                    if (resp.code == 200) {
                        ChineseChat.CurrentUser.Coins = resp.info.Student.Coins;
                        CommonUtil.saveUserToSP(ChineseChat.getContext(), ChineseChat.CurrentUser, false);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "记录结束失败:" + msg);
            }
        });
    }
}
