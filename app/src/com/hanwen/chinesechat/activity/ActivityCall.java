package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.CallLog;
import com.hanwen.chinesechat.bean.NimSysNotice;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.fragment.FragmentThemes;
import com.hanwen.chinesechat.rts.ActionTypeEnum;
import com.hanwen.chinesechat.rts.doodle.DoodleView;
import com.hanwen.chinesechat.rts.doodle.TransactionCenter;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.util.SoundPlayer;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
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
import com.netease.nimlib.sdk.avchat.model.AVChatOptionalParam;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.rts.RTSCallback;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.model.RTSData;
import com.netease.nimlib.sdk.rts.model.RTSOptions;
import com.netease.nimlib.sdk.rts.model.RTSTunData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private static final String KEY_TARGET_USERNAME = "KEY_TARGET_USERNAME";
    public static final String KEY_TARGET_ACCID = "KEY_TARGET_ACCID";
    public static final String KEY_TARGET_ID = "KEY_TARGET_ID";
    public static final String KEY_TARGET_ICON = "KEY_TARGET_ICON";
    public static final String TAG = "ActivityCall";

    private static final int WHAT_REFRESH = 1;
    private static final int WHAT_HANG_UP = 2;
    private static final int WHAT_PLAY_SOUND = 3;
    private static final int WHAT_PEER_BUSY = 4;

    private View bt_hangup, bt_mute, bt_free;
    private View ll_user, ll_ctrl, rl_main;
    private BaseAdapter<Theme> cardAdapter;
    private AlertDialog cardDialog;
    private List<Theme> cardList;
    private Chronometer cm_time;
    private Gson gson = new Gson();
    private ImageView iv_icon;
    private TextView tv_nickname, tv_card;
    private Theme currentTheme = null;
    private String callId;
    private long chatId = -1;
    private User target;
    private boolean IS_CALL_ESTABLISHED = false;
    public static final int REQUEST_CODE_THEME = 1;
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
                        hangup();
                    }
                    break;
                case WHAT_PLAY_SOUND:
                    Log.i(TAG, "WHAT_PLAY_SOUND: ");
                    SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.RING);
                    break;
                case WHAT_PEER_BUSY:
                    Log.i(TAG, "WHAT_PEER_BUSY: ");
                    finish();
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
                        AVChatManager.getInstance().muteLocalAudio(true);

                        //发送自定义通知,通知对方目前本机有外呼电话进来
                        NimSysNotice<String> notice = new NimSysNotice<String>();
                        notice.type = NimSysNotice.NoticeType_Call;

                        CustomNotification notification = new CustomNotification();
                        notification.setSessionId(target.Accid);
                        notification.setSessionType(SessionTypeEnum.P2P);
                        notification.setContent(gson.toJson(notice));
                        NIMClient.getService(MsgService.class).sendCustomNotification(notification);
                    } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                        AVChatManager.getInstance().muteLocalAudio(false);
                    }
                }
            }
        }
    };
    private boolean CALL_ID_RECEIVE = false;
    private Observer<CustomNotification> observerCustomNotification;
    private DoodleView dv_board;
    private View ll_profile;
    private ImageView iv_avatar;
    private TextView tv_name;
    private TextView tv_case;
    private View rl_board;
    private View fl_content;
    private ListView lv_msg;
    private EditText et_msg;
    private ArrayList<String> listMessage;
    private AdapterMessage adapterMessage;

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
                    hangup();
                    CommonUtil.toast(R.string.ActivityCall_coins_error);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                CommonUtil.toast(getString(R.string.network_error));
            }
        });
    }

    private void hangup() {
        AVChatManager.getInstance().hangUp(callback_hangup);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        initView();
        initData();

        observerCustomNotification = new Observer<CustomNotification>() {
            @Override
            public void onEvent(CustomNotification message) {
                Log.i(TAG, "自定义通知: " + message.getContent());
                int type = -1;
                String info = null;
                try {
                    JSONObject jsonObject = new JSONObject(message.getContent());
                    type = jsonObject.getInt("type");
                    info = jsonObject.getString("info");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                switch (type) {
                    case NimSysNotice.NoticeType_Call: {
                        CommonUtil.toast("Very sorry!I have a phone call,Please wait a moment.");
                    }
                    break;
                    case NimSysNotice.NoticeType_Chat:
                        CALL_ID_RECEIVE = true;
                        callId = info;
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(broadcastReceiver, filter);
    }

    public static void start(Context context, int id, String accId, String icon, String nickname, String username, int callTypeAudio) {
        Log.i(TAG, "start: id=" + id + " accid=" + accId + " icon=" + icon + " nickname=" + nickname + " callTypeAudio=" + callTypeAudio);
        Intent intent = new Intent(context, ActivityCall.class);
        intent.putExtra(KEY_TARGET_ID, id);
        intent.putExtra(KEY_TARGET_ACCID, accId);
        intent.putExtra(KEY_TARGET_NICKNAME, nickname);
        intent.putExtra(KEY_TARGET_USERNAME, username);
        intent.putExtra(KEY_TARGET_ICON, icon);
        context.startActivity(intent);

        SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.CONNECTING);
    }

    private void initData() {
        Intent intent = getIntent();
        String nickname = intent.getStringExtra(KEY_TARGET_NICKNAME);
        String username = intent.getStringExtra(KEY_TARGET_USERNAME);
        String icon = intent.getStringExtra(KEY_TARGET_ICON);
        tv_name.setText(TextUtils.isEmpty(nickname) ? username : nickname);
        tv_nickname.setText(TextUtils.isEmpty(nickname) ? username : nickname);

        //下载处理,如果有设置头像,则显示头像,
        //如果头像已经下载过,则加载本地图片
        CommonUtil.showBitmap(iv_icon, NetworkUtil.getFullPath(icon));
        CommonUtil.showBitmap(iv_avatar, NetworkUtil.getFullPath(icon));

        target = new User();
        target.Id = intent.getIntExtra(KEY_TARGET_ID, 0);
        target.Accid = intent.getStringExtra(KEY_TARGET_ACCID);
        target.Nickname = intent.getStringExtra(KEY_TARGET_NICKNAME);
        target.Icon = intent.getStringExtra(KEY_TARGET_ICON);

        //回铃声
        /*
        soundPool = new SoundPool(2, AudioManager.STREAM_RING, 0);
        soundId = soundPool.load(this, R.raw.avchat_ring, 1);
        */

        AVChatNotifyOption option = new AVChatNotifyOption();
        option.apnsBadge = false;
        option.apnsInuse = true;
        option.pushSound = "pushRing.aac";//Push
        option.extendMessage = gson.toJson(ChineseChat.CurrentUser);//把呼叫者的用户名,头像发送过去

        AVChatOptionalParam params = new AVChatOptionalParam();
        params.enableCallProximity(false);
        AVChatManager.getInstance().call(target.Accid, AVChatType.AUDIO, params, option, callback_call);

        registerObserver(true);

        listMessage = new ArrayList<>();
        adapterMessage = new AdapterMessage(listMessage);
        lv_msg.setAdapter(adapterMessage);
    }

    private void initView() {
        //头像
        ll_user = findViewById(R.id.ll_user);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);

        //
        //小头像,名称,学习情况
        ll_profile = findViewById(R.id.ll_profile);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_case = (TextView) findViewById(R.id.tv_case);

        //主题,白板,文字,图片 控制按钮
        View tv_board = findViewById(R.id.tv_board);
        View tv_text = findViewById(R.id.tv_text);
        View tv_image = findViewById(R.id.tv_image);
        tv_board.setOnClickListener(this);
        tv_text.setOnClickListener(this);
        tv_image.setOnClickListener(this);

        //文字
        View rl_texts = findViewById(R.id.rl_texts);
        lv_msg = (ListView) findViewById(R.id.lv_msg);
        et_msg = (EditText) findViewById(R.id.et_msg);
        View bt_msg = findViewById(R.id.bt_msg);
        rl_texts.setOnClickListener(this);
        bt_msg.setOnClickListener(this);

        rl_board = findViewById(R.id.rl_board);

        fl_content = findViewById(R.id.fl_content);
        dv_board = (DoodleView) findViewById(R.id.dv_board);

        //控制
        bt_hangup = findViewById(R.id.bt_hangup);
        bt_mute = findViewById(R.id.bt_mute);
        bt_free = findViewById(R.id.bt_free);
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
                AVChatManager.getInstance().muteLocalAudio(true);

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
                //region挂断事件
                SoundPlayer.instance(this).stop();
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
                            hangup();
                        }
                    });
                    builder.show();
                } else {
                    //直接挂断
                    hangup();
                }
                //endregion
                break;

            case R.id.bt_msg:
                //region 发送文本
                String m = et_msg.getText().toString().trim();
                if (!TextUtils.isEmpty(m)) {

                    listMessage.add(m);
                    adapterMessage.notifyDataSetChanged();
                    et_msg.setText("");
                    lv_msg.smoothScrollByOffset(1);

                    // 创建文本消息
                    IMMessage message = MessageBuilder.createTextMessage(
                            target.Accid, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                            SessionTypeEnum.P2P, // 聊天类型，单聊或群组
                            "聊天对象的 ID，如果是单聊" // 文本内容
                    );
                    // 发送消息。如果需要关心发送结果，可设置回调函数。发送完成时，会收到回调。如果失败，会有具体的错误码。
                    NIMClient.getService(MsgService.class).sendMessage(message, false);
                }
                //endregion
                break;

            case R.id.bt_mute: {
                // 静音设置
                AVChatManager.getInstance().muteLocalAudio(!AVChatManager.getInstance().isLocalAudioMuted());
                //CommonUtil.toast(AVChatManager.getInstance().isLocalAudioMuted() ? "目前静音" : "目前通话");
                bt_mute.setSelected(AVChatManager.getInstance().isLocalAudioMuted());
            }
            break;
            case R.id.bt_free: {
                // 设置扬声器是否开启
                AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
                //CommonUtil.toast(AVChatManager.getInstance().speakerEnabled() ? "目前外放" : "目前耳机");
                bt_free.setSelected(!AVChatManager.getInstance().speakerEnabled());
            }
            break;

            case R.id.tv_card:
                ActivityTheme.start(ActivityCall.this, gson.toJson(currentTheme));
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
                currentTheme = gson.fromJson(data.getStringExtra("theme"), new TypeToken<Theme>() {
                }.getType());

                // 构造自定义通知，指定接收者,并发送自定义通知
                NimSysNotice<Theme> notice = new NimSysNotice<>();
                notice.type = NimSysNotice.NoticeType_Card;
                notice.info = currentTheme;

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
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        SoundPlayer.instance(ChineseChat.getContext()).stop();
        registerObserver(false);
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(broadcastReceiver);
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

        // 如果有自定义通知是作用于全局的，不依赖某个特定的 Activity，那么这段代码应该在 Application 的 onCreate 中就调用
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(observerCustomNotification, register);


        RTSManager.getInstance().observeIncomingSession(observerBord, register);

        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(observerBaseMessage, register);
    }

    Observer<List<IMMessage>> observerBaseMessage = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            for (IMMessage m : messages) {
                Log.i(TAG, "基础消息: " + m.getContent());
                if (!TextUtils.isEmpty(m.getContent())) {
                    listMessage.add(m.getContent());
                    adapterMessage.notifyDataSetChanged();
                    lv_msg.smoothScrollToPosition(listMessage.size() - 1);
                }
            }
        }
    };

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

    //region挂断回调
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
    //endregion

    //region拨打回调
    private AVChatCallback<AVChatData> callback_call = new AVChatCallback<AVChatData>() {

        @Override
        public void onException(Throwable arg0) {
            Log.i(TAG, "拨打异常回调: " + arg0.getMessage());
            SoundPlayer.instance(ChineseChat.getContext()).stop();
            CommonUtil.toast(R.string.ActivityCall_call_error);
            //拨打异常就让老师下线

            Parameters params = new Parameters();
            params.add("id", target.Id);
            HttpUtil.post(NetworkUtil.teacherDequeue, params, null);
            finish();
        }

        @Override
        public void onFailed(int arg0) {
            Log.i(TAG, "拨打失败回调: " + arg0);
            SoundPlayer.instance(ChineseChat.getContext()).stop();

            if (11001 == arg0) {
                //9102	通道失效
                //9103	已经在他端对这个呼叫响应过了
                //11001	通话不可达，对方离线状态
                CommonUtil.toast(R.string.ActivityCall_call_failed);
            } else if (408 == arg0) {
                //408	客户端请求超时
                CommonUtil.toast(R.string.ActivityCall_call_failed);
            }
            Parameters params = new Parameters();
            params.add("id", target.Id);
            HttpUtil.post(NetworkUtil.teacherDequeue, params, null);
            finish();
        }

        @Override
        public void onSuccess(AVChatData avChatData) {
            Log.i(TAG, "拨打成功回调: ChatId=" + avChatData.getChatId());
            //记录下ChatId,如果对方还没有接听就直接挂断,帮对方上线并入队,如果拨打失败则暂时不上线
            chatId = avChatData.getChatId();
            handler.sendEmptyMessageDelayed(WHAT_PLAY_SOUND, 5000);
        }
    };
    //endregion

    //region回应监听
    private Observer<AVChatCalleeAckEvent> observerCalleeAck = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent event) {
            Log.i(TAG, "对方回应监听: ChatId=" + event.getChatId() + " Event=" + event.getEvent());

            SoundPlayer.instance(ChineseChat.getContext()).stop();
            handler.removeMessages(WHAT_PLAY_SOUND);

            if (event.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.PEER_BUSY);
                handler.sendEmptyMessageDelayed(WHAT_PEER_BUSY, 5000);
                return;
            }
            if (event.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.PEER_BUSY);
                // 对方拒绝接听,你好,你拨打的电话正在通话中,请稍后再拔,the number you are calling is busy,please recall later!
                handler.sendEmptyMessageDelayed(WHAT_PEER_BUSY, 5000);
                return;
            }

            if (event.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                // 对方同意接听
                if (event.isDeviceReady()) {
                    // 设备初始化成功，开始通话,关闭回铃声
                    CommonUtil.toast(R.string.ActivityCall_device_ready);

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
    //endregion

    //region挂断监听
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
    //endregion

    //region超时监听
    private Observer<AVChatTimeOutEvent> observerTimeout = new Observer<AVChatTimeOutEvent>() {
        @Override
        public void onEvent(AVChatTimeOutEvent avChatTimeOutEvent) {
            Log.i(TAG, "超时监听=" + avChatTimeOutEvent.name());
            SoundPlayer.instance(ChineseChat.getContext()).stop();
            if (avChatTimeOutEvent == AVChatTimeOutEvent.OUTGOING_TIMEOUT) {
                SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.NO_RESPONSE);
            }
            finish();
        }
    };
    //endregion

    //region通话状态监听
    private AVChatStateObserver observerChatState = new AVChatStateObserver() {
        @Override
        public void onTakeSnapshotResult(String s, boolean b, String s1) {
            Log.i(TAG, "onTakeSnapshotResult: ");
        }

        @Override
        public void onConnectionTypeChanged(int i, int i1) {
            Log.i(TAG, "onConnectionTypeChanged: ");
        }

        @Override
        public void onLocalRecordEnd(String[] strings, int i) {
            Log.i(TAG, "onLocalRecordEnd: ");
        }

        @Override
        public void onFirstVideoFrameAvailable(String s) {
            Log.i(TAG, "onFirstVideoFrameAvailable: ");
        }

        @Override
        public void onVideoFpsReported(String s, int i) {
            Log.i(TAG, "onVideoFpsReported: ");
        }

        @Override
        public void onJoinedChannel(int i, String s, String s1) {
            Log.i(TAG, "onJoinedChannel: ");
        }

        @Override
        public void onLeaveChannel() {
            Log.i(TAG, "onLeaveChannel: ");
        }

        @Override
        public void onUserJoined(String s) {
            Log.i(TAG, "onUserJoined: ");
        }

        @Override
        public void onUserLeave(String s, int i) {
            Log.i(TAG, "onUserLeave: account=" + s + " event=" + i);
            AVChatManager.getInstance().hangUp(null);
            chatHistoryFinish();
            finish();
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
        public void onNetworkQuality(String s, int i) {
            Log.i(TAG, "onNetworkQuality: ");
        }

        @Override
        public void onCallEstablished() {
            Log.i(TAG, "onCallEstablished: ");

            cm_time.setBase(SystemClock.elapsedRealtime());
            cm_time.start();
            IS_CALL_ESTABLISHED = true;
        }

        @Override
        public void onDeviceEvent(String s, int i, String s1) {
            Log.i(TAG, "onDeviceEvent: account=" + s + " code=" + i + " desc=" + s1);
            AVChatManager.getInstance().setSpeaker(true);
            bt_free.setSelected(!AVChatManager.getInstance().speakerEnabled());
            Log.i(TAG, "onDeviceEvent: speakerEnabled=" + AVChatManager.getInstance().speakerEnabled());

            SoundPlayer.instance(ChineseChat.getContext()).stop();
        }
    };
    //endregion

    private RTSData currentRtsData;
    //region 白板监听
    private Observer<RTSData> observerBord = new Observer<RTSData>() {
        @Override
        public void onEvent(RTSData rtsData) {
            Log.i(TAG, "白板监听 onEvent: ");
            currentRtsData = rtsData;

            RTSManager.getInstance().observeReceiveData(currentRtsData.getSessionId(), receiveDataObserver, true);

            RTSOptions options = new RTSOptions().setRecordTCPTun(true);
            RTSManager.getInstance().accept(rtsData.getSessionId(), options, new RTSCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean aBoolean) {
                    Log.i(TAG, "白板回应 onSuccess: ");
                    dv_board.init(currentRtsData.getSessionId(), currentRtsData.getAccount(), DoodleView.Mode.BOTH, Color.WHITE, ActivityCall.this);
                    dv_board.setPaintSize(5);
                    dv_board.setPaintType(ActionTypeEnum.Path.getValue());
                    dv_board.setPaintOffset(0, fl_content.getTop());

                    //白板回应成功后,显示小头像,显示白板控件
                    ll_user.setVisibility(View.INVISIBLE);
                    ll_profile.setVisibility(View.VISIBLE);
                    rl_board.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailed(int i) {
                    Log.i(TAG, "白板回应 onFailed: ");
                }

                @Override
                public void onException(Throwable throwable) {
                    Log.i(TAG, "白板回应 onException: ");
                }
            });
        }
    };
    //endregion

    //region 白板数据监听
    Observer<RTSTunData> receiveDataObserver = new Observer<RTSTunData>() {
        @Override
        public void onEvent(RTSTunData rtsTunData) {
            String data = "[parse bytes error]";
            try {
                data = new String(rtsTunData.getData(), 0, rtsTunData.getLength(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            TransactionCenter.getInstance().onReceive(currentRtsData.getSessionId(), data);
        }
    };
    //endregion


    private class AdapterMessage extends BaseAdapter<String> {
        public AdapterMessage(List<String> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String item = getItem(position);
            View inflate = View.inflate(getApplication(), R.layout.listitem_call, null);
            TextView textview = (TextView) inflate.findViewById(R.id.textview);
            textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            textview.setText(item);
            return inflate;
        }
    }
}
