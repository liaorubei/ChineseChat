package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.CallLog;
import com.newclass.woyaoxue.bean.NimSysNotice;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.Theme;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.fragment.FragmentThemes;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 电话拨打界面
 *
 * @author liaorubei
 */
public class ActivityCall extends Activity implements OnClickListener {
    public static final String CALL_TYPE_KEY = "CALL_TYPE_KEY";
    public static final int CALL_TYPE_AUDIO = 1;
    public static final int CALL_TYPE_VIDEO = 2;
    public static final String KEY_TARGET_NICKNAME = "KEY_TARGET_NICKNAME";
    public static final String KEY_TARGET_ACCID = "KEY_TARGET_ACCID";
    public static final String KEY_TARGET_ID = "KEY_TARGET_ID";
    public static final String KEY_TARGET_ICON = "KEY_TARGET_ICON";
    public static final String TAG = "ActivityCall";

    private static final int WHAT_REFRESH = 1;
    private static final int WHAT_HANG_UP = 2;
    private static final int WHAT_PLAY_SOUND = 3;

    private View bt_hangup, bt_mute, bt_free;
    private View ll_user, ll_ctrl, rl_main;
    private BaseAdapter<Theme> cardAdapter;
    private AlertDialog cardDialog;
    private List<Theme> cardList;
    private Chronometer cm_time;
    private Gson gson = new Gson();
    private ImageView iv_icon;
    private TextView tv_nickname, tv_card;
    private int soundId;
    private SoundPool soundPool;
    private Theme theme = null;
    private String callId;
    private long chatId = -1;
    private User target;
    private boolean IS_CALL_ESTABLISHED = false;
    private int REQUEST_CODE_THEME = 1;
    private Dialog faceDialog;
    private long m刷新DelayMillis = 60000;
    private long m挂断DelayMillis = 60000;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_REFRESH:
                    if (!TextUtils.isEmpty(callId)) {
                        refresh();
                        sendEmptyMessageDelayed(WHAT_REFRESH, m刷新DelayMillis);
                    }
                    break;
                case WHAT_HANG_UP:
                    if (TextUtils.isEmpty(callId)) {
                        AVChatManager.getInstance().hangUp(callback_hangup);
                    }
                    break;
                case WHAT_PLAY_SOUND:
                    ((SoundPool) msg.obj).play(soundId, 1, 1, 0, -1, 1);
                    break;
            }
        }
    };


    // 自定义系统通知的广播接收者
    private BroadcastReceiver CustomNotificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 从 intent 中取出自定义通知， intent 中只包含了一个 CustomNotification 对象
            CustomNotification notification = (CustomNotification) intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
            Log.i(TAG, "收到自定义通知: " + notification.getContent());
            NimSysNotice<String> notice = gson.fromJson(notification.getContent(), new TypeToken<NimSysNotice<String>>() {
            }.getType());

            switch (notice.type) {
                case NimSysNotice.NoticeType_Call:
                    CommonUtil.toast("Very sorry!I have a phone call,Please wait a moment.");
                    break;
                case NimSysNotice.NoticeType_Chat:
                    CALL_ID_RECEIVE = true;
                    callId = notice.info;
                    //显示主题选择按钮
                    tv_card.setVisibility(View.VISIBLE);
                    break;
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
                        notification.setSessionId(target.Accid);
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
    private boolean CALL_ID_RECEIVE = false;

    private void refresh() {
        Parameters params = new Parameters();
        params.add("callId", callId);
        HttpUtil.post(NetworkUtil.callRefresh, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());
                if (resp.code != 200) {
                    AVChatManager.getInstance().hangUp(callback_hangup);
                    CommonUtil.toast(R.string.ActivityCall_coins_error);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                AVChatManager.getInstance().hangUp(callback_hangup);
                CommonUtil.toast(getString(R.string.network_error));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        initView();
        initData();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(broadcastReceiver, filter);
    }

    public static void start(Context context, int id, String accId, String icon, String nickName, int callTypeAudio) {
        Intent intent = new Intent(context, ActivityCall.class);
        intent.putExtra(KEY_TARGET_ID, id);
        intent.putExtra(KEY_TARGET_ACCID, accId);
        intent.putExtra(KEY_TARGET_NICKNAME, nickName);
        intent.putExtra(KEY_TARGET_ICON, icon);
        context.startActivity(intent);
    }

    private void initData() {
        Intent intent = getIntent();
        String nickname = intent.getStringExtra(KEY_TARGET_NICKNAME);
        String icon = intent.getStringExtra(KEY_TARGET_ICON);
        tv_nickname.setText(nickname);

        //下载处理,如果有设置头像,则显示头像,
        //如果头像已经下载过,则加载本地图片
        CommonUtil.showBitmap(iv_icon, NetworkUtil.getFullPath(icon));

        target = new User();
        target.Id = intent.getIntExtra(KEY_TARGET_ID, 0);
        target.Accid = intent.getStringExtra(KEY_TARGET_ACCID);
        target.Nickname = intent.getStringExtra(KEY_TARGET_NICKNAME);
        target.Icon = intent.getStringExtra(KEY_TARGET_ICON);

        //回铃声
        soundPool = new SoundPool(2, AudioManager.STREAM_RING, 0);
        soundId = soundPool.load(this, R.raw.avchat_ring, 1);

        //public abstract void call(java.lang.String account,AVChatType callType,VideoChatParam videoChatParam,boolean serverAudioRecord,boolean serverVideoRecord,AVChatNotifyOption notifyOption,AVChatCallback<AVChatData> callback)
        //account - 对方帐号
        //callType - 通话类型：语音、视频
        //videoChatParam - 发起视频通话时传入，发起音频通话传null
        //serverAudioRecord - 服务器是否录制语音(还需要后台额外的配置)
        //serverVideoRecord - 服务器是否录制视频(还需要后台额外的配置)
        //notifyOption - 可选通知参数
        //callback - 回调函数，返回NetCallInfo
        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        notifyOption.apnsBadge = false;
        notifyOption.apnsInuse = true;
        notifyOption.apnsSound = "pushRing.aac";//Push
        notifyOption.extendMessage = gson.toJson(ChineseChat.CurrentUser);//把呼叫者的用户名,头像发送过去
        AVChatManager.getInstance().call(target.Accid, AVChatType.AUDIO, null, false, false, notifyOption, callback_call);
        registerObserver(true);
    }

    private void initView() {
        //头像
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        iv_icon.setOnClickListener(this);

        //控制
        bt_hangup = findViewById(R.id.bt_hangup);
        bt_mute = findViewById(R.id.bt_mute);
        bt_free = findViewById(R.id.bt_free);
        ll_user = findViewById(R.id.ll_user);
        ll_ctrl = findViewById(R.id.ll_ctrl);
        rl_main = findViewById(R.id.rl_main);

        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        cm_time = (Chronometer) findViewById(R.id.cm_time);
        tv_card = (TextView) findViewById(R.id.tv_card);

        bt_hangup.setOnClickListener(this);
        bt_mute.setOnClickListener(this);
        bt_free.setOnClickListener(this);
        tv_card.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_icon:
                AVChatManager.getInstance().setMute(true);

                //发送自定义通知,通知对方目前本机有外呼电话进来
                NimSysNotice<String> notice = new NimSysNotice<String>();
                notice.type = NimSysNotice.NoticeType_Call;

                CustomNotification notification = new CustomNotification();
                notification.setSessionId(target.Accid);
                notification.setSessionType(SessionTypeEnum.P2P);
                notification.setContent(gson.toJson(notice));
                NIMClient.getService(MsgService.class).sendCustomNotification(notification);
                break;
            case R.id.bt_hangup:
                if (IS_CALL_ESTABLISHED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.ActivityCall_hangup);
                    builder.setNegativeButton(R.string.ActivityCall_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton(R.string.ActivityCall_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AVChatManager.getInstance().hangUp(callback_hangup);
                        }
                    });
                    builder.show();
                } else {
                    //直接挂断
                    AVChatManager.getInstance().hangUp(callback_hangup);
                }
                break;

            case R.id.bt_mute: {
                // 静音设置
                AVChatManager.getInstance().setMute(!AVChatManager.getInstance().isMute());
                //CommonUtil.toast(AVChatManager.getInstance().isMute() ? "目前静音" : "目前通话");
                bt_mute.setSelected(AVChatManager.getInstance().isMute());
            }
            break;
            case R.id.bt_free: {
                // 设置扬声器是否开启
                Log.i(TAG, "speakerEnabled: " + AVChatManager.getInstance().speakerEnabled());
                AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
                Log.i(TAG, "speakerEnabled: " + AVChatManager.getInstance().speakerEnabled());

                //CommonUtil.toast(AVChatManager.getInstance().speakerEnabled() ? "目前外放" : "目前耳机");
                bt_free.setSelected(!AVChatManager.getInstance().speakerEnabled());
            }
            break;

            case R.id.tv_card:
                Intent intent = new Intent(this, ActivityTheme.class);
                intent.putExtra("levelId", theme == null ? 0 : theme.Id);
                startActivityForResult(intent, REQUEST_CODE_THEME);
                break;
            case R.id.bt_text:
                // 创建文本消息
                IMMessage message = MessageBuilder.createTextMessage(
                        target.Accid, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                        SessionTypeEnum.P2P, // 聊天类型，单聊或群组
                        "文本内容" // 文本内容
                );
                // 发送消息。如果需要关心发送结果，可设置回调函数。发送完成时，会收到回调。如果失败，会有具体的错误码。
                NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: 文本消息发送成功");
                    }

                    @Override
                    public void onFailed(int i) {
                        Log.i(TAG, "onFailed: " + i);
                    }

                    @Override
                    public void onException(Throwable throwable) {

                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_THEME) {
            if (resultCode == FragmentThemes.RESULTCODE_CHOOSE) {
                tv_card.setText(R.string.ActivityCall_switch_theme);
                theme = gson.fromJson(data.getStringExtra("theme"), new TypeToken<Theme>() {
                }.getType());

                // 构造自定义通知，指定接收者,并发送自定义通知
                NimSysNotice<Theme> notice = new NimSysNotice<>();
                notice.type = NimSysNotice.NoticeType_Card;
                notice.info = theme;

                CustomNotification notification = new CustomNotification();
                notification.setSessionId(target.Accid);
                notification.setSessionType(SessionTypeEnum.P2P);
                notification.setContent(gson.toJson(notice));
                NIMClient.getService(MsgService.class).sendCustomNotification(notification);
            } else {
                CommonUtil.toast(R.string.ActivityCall_topic_choose_failed);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
        }
        registerObserver(false);
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(broadcastReceiver);
    }

    //记录通话结束情况,如果通话成功后己方挂断,对方挂断,通话后网络超时挂断,都要记录
    private void recordChatFinish() {
        if (IS_CALL_ESTABLISHED) {
            Parameters params = new Parameters();
            params.add("chatId", chatId);
            HttpUtil.post(NetworkUtil.callFinish, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "recordChatFinish_Success: " + responseInfo.result);
                    Response<CallLog> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<CallLog>>() {
                    }.getType());
                    if (resp.code == 200) {
                        ChineseChat.CurrentUser.Coins = resp.info.Student.Coins;
                        CommonUtil.saveUserToSP(getApplication(), ChineseChat.CurrentUser, false);
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "recordChatFinish_Failure: " + msg);
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CommonUtil.toast(R.string.ActivityCall_can_not_back);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void registerObserver(boolean register) {
        //接听observer
        // 监听网络通话被叫方的响应（接听、拒绝、忙）
        AVChatManager.getInstance().observeCalleeAckNotification(observerCalleeAck, register);

        //挂断observer
        // 监听网络通话对方挂断的通知,即在正常通话时,结束通话
        AVChatManager.getInstance().observeHangUpNotification(observerHangup, register);

        //监听通话过程中状态变化
        AVChatManager.getInstance().observeAVChatState(observerChatState, register);

        // 监听呼叫或接听超时通知
        // 主叫方在拨打网络通话时，超过 45 秒被叫方还未接听来电，则自动挂断。
        // 被叫方超过 45 秒未接听来听，也会自动挂断
        // 在通话过程中网络超时 30 秒自动挂断。
        AVChatManager.getInstance().observeTimeoutNotification(observerTimeout, register);
    }

    private void chatHistoryCreate(AVChatCalleeAckEvent event) {
        Parameters parameters = new Parameters();
        parameters.add("chatId", event.getChatId());
        parameters.add("chatType", event.getChatType().getValue());
        parameters.add("target", target.Id);
        parameters.add("source", ChineseChat.CurrentUser.Id);
        HttpUtil.post(NetworkUtil.callCreate, parameters, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "记录创建成功:" + responseInfo.result);
                Response<CallLog> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<CallLog>>() {
                }.getType());

                if (resp.code == 200) {
                    callId = resp.info.Id;
                }

                //记录callId

                //发送callId
                Log.i(TAG, "发送CallId通知: " + !CALL_ID_RECEIVE);
                if (!CALL_ID_RECEIVE) {
                    NimSysNotice<String> notice = new NimSysNotice<String>();
                    notice.type = NimSysNotice.NoticeType_Chat;
                    notice.info = callId;

                    CustomNotification notification = new CustomNotification();
                    notification.setSessionId(target.Accid);
                    notification.setSessionType(SessionTypeEnum.P2P);
                    notification.setContent(gson.toJson(notice));
                    NIMClient.getService(MsgService.class).sendCustomNotification(notification);
                }

                //显示主题选择按钮
                tv_card.setVisibility(View.VISIBLE);
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

    //挂断回调
    private AVChatCallback<Void> callback_hangup = new AVChatCallback<Void>() {

        @Override
        public void onException(Throwable arg0) {
            Log.i(TAG, "挂断异常回调: 异常=" + arg0.getMessage());
            if (!TextUtils.isEmpty(callId)) {
                chatHistoryFinish();
            }
            finish();
        }

        @Override
        public void onFailed(int arg0) {
            Log.i(TAG, "挂断失败回调: 失败=" + arg0);
            if (!TextUtils.isEmpty(callId)) {
                chatHistoryFinish();
            }
            finish();
        }

        @Override
        public void onSuccess(Void arg0) {
            Log.i(TAG, "挂断成功回调: ChatId=" + chatId);
            if (!TextUtils.isEmpty(callId)) {
                chatHistoryFinish();
            }
            finish();
        }
    };

    //拨打回调
    private AVChatCallback<AVChatData> callback_call = new AVChatCallback<AVChatData>() {

        @Override
        public void onException(Throwable arg0) {
            Log.i(TAG, "拨打异常回调: " + arg0.getMessage());
            CommonUtil.toast(R.string.ActivityCall_call_error);
            finish();
        }

        @Override
        public void onFailed(int arg0) {
            Log.i(TAG, "拨打失败回调: " + arg0);

            if (11001 == arg0) {
                CommonUtil.toast(R.string.ActivityCall_call_failed);
            } else if (408 == arg0) {
                //408	客户端请求超时
                CommonUtil.toast(R.string.ActivityCall_call_failed);
            }
            finish();
        }

        @Override
        public void onSuccess(AVChatData avChatData) {
            Log.i(TAG, "拨打成功回调: ChatId=" + avChatData.getChatId());

            //记录下ChatId,如果对方还没有接听就直接挂断,帮对方上线并入队,如果拨打失败则暂时不上线
            chatId = avChatData.getChatId();

            cm_time.start();

            //等待对方接听铃声
            Message msg = handler.obtainMessage();
            msg.obj = soundPool;
            msg.what = WHAT_PLAY_SOUND;
            handler.sendMessageDelayed(msg, 2500);
        }
    };

    //回应监听
    private Observer<AVChatCalleeAckEvent> observerCalleeAck = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent event) {
            Log.i(TAG, "对方回应监听: ChatId=" + event.getChatId() + " Event=" + event.getEvent());

            if (event.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                // 对方正在忙
                CommonUtil.toast(R.string.ActivityCall_device_busy);
                finish();
                return;
            }
            if (event.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                // 对方拒绝接听
                CommonUtil.toast(R.string.ActivityCall_device_reject);
                finish();
                return;
            }

            if (event.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                // 对方同意接听
                if (event.isDeviceReady()) {
                    // 设备初始化成功，开始通话,关闭回铃声
                    CommonUtil.toast(R.string.ActivityCall_device_ready);
                    soundPool.release();

                    //创建记录
                    chatHistoryCreate(event);

                    //挂断定时
                    handler.sendEmptyMessageDelayed(WHAT_HANG_UP, m挂断DelayMillis);

                    //刷新定时
                    handler.sendEmptyMessageDelayed(WHAT_REFRESH, m刷新DelayMillis);
                } else {
                    // 设备初始化失败，无法进行通话
                    CommonUtil.toast(R.string.ActivityCall_device_error);
                    finish();
                }
            }
        }

    };

    //挂断监听
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

    //超时监听
    private Observer<AVChatTimeOutEvent> observerTimeout = new Observer<AVChatTimeOutEvent>() {
        @Override
        public void onEvent(AVChatTimeOutEvent avChatTimeOutEvent) {
            Log.i(TAG, "超时监听=" + avChatTimeOutEvent.name());
            finish();
        }
    };

    //通话状态监听
    private AVChatStateObserver observerChatState = new AVChatStateObserver() {
        @Override
        public void onConnectedServer(int i, String s, String s1) {
            Log.i(TAG, "onConnectedServer: ");
        }

        @Override
        public void onUserJoin(String s) {
            Log.i(TAG, "onUserJoin: ");
        }

        @Override
        public void onUserLeave(String s, int i) {
            Log.i(TAG, "onUserLeave: " + s);
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
        }

        @Override
        public void onOpenDeviceError(int i) {
            Log.i(TAG, "onOpenDeviceError: ");
        }

        @Override
        public void onRecordEnd(String[] strings, int i) {
            Log.i(TAG, "onRecordEnd: ");
        }
    };
}
