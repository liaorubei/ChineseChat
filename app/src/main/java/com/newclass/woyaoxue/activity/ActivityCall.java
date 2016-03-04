package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
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

import java.util.List;

/**
 * 电话拨打界面
 *
 * @author liaorubei
 */
public class ActivityCall extends Activity implements OnClickListener {
    public static final int CALL_TYPE_AUDIO = 1;
    public static final String CALL_TYPE_KEY = "CALL_TYPE_KEY";
    public static final int CALL_TYPE_VIDEO = 2;
    public static final String KEY_TARGET_NICKNAME = "KEY_TARGET_NICKNAME";
    public static final String KEY_TARGET_ACCID = "KEY_TARGET_ACCID";
    public static final String KEY_TARGET_ID = "KEY_TARGET_ID";
    public static final String TAG = "ActivityCall";

    private AVChatCallback<Void> callback_hangup;
    private AVChatCallback<AVChatData> callback_call;
    private Button bt_hangup, bt_mute, bt_free, bt_face, bt_card;

    private View ll_user, ll_ctrl, rl_main;

    private BaseAdapter<Theme> cardAdapter;
    private AlertDialog cardDialog;
    private List<Theme> cardList;
    private Chronometer cm_time;
    private Gson gson = new Gson();
    private ImageView iv_icon;

    private TextView tv_nickname;

    private String callId;
    private User source;
    private User target;

    // 监听网络通话被叫方的响应（接听、拒绝、忙）
    private Observer<AVChatCalleeAckEvent> observerCallAck = new Observer<AVChatCalleeAckEvent>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void onEvent(AVChatCalleeAckEvent event) {
            switch (event.getEvent()) {
                case CALLEE_ACK_AGREE:// 被叫方同意接听
                    if (event.isDeviceReady()) {
                        IS_CHATTING = true;

                        CommonUtil.toast("设备正常,开始通话");
                        cm_time.setBase(SystemClock.elapsedRealtime());

                        Log.i(TAG, "被叫方同意接听: ackEvent.getChatId():" + event.getChatId());

                        Parameters parameters = new Parameters();
                        parameters.add("chatId", event.getChatId() + "");
                        parameters.add("chatType", event.getChatType().getValue() + "");
                        parameters.add("target", target.Id + "");
                        parameters.add("source", source.Id + "");

                        HttpUtil.post(NetworkUtil.callstart, parameters, new RequestCallBack<String>() {

                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                Response<CallLog> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<CallLog>>() {
                                }.getType());

                                if (resp.code == 200) {
                                    callId = resp.info.Id;
                                }
                                Log.i(TAG, "记录Id:" + resp.info.Id);
                            }

                            @Override
                            public void onFailure(HttpException error, String msg) {
                                Log.i(TAG, "添加记录失败:" + msg);
                            }
                        });
                    } else {
                        CommonUtil.toast("设备异常,无法通话");
                        finish();
                    }
                    break;
                case CALLEE_ACK_REJECT:
                    CommonUtil.toast("对方拒绝接听");
                    finish();
                    break;

                case CALLEE_ACK_BUSY:
                    CommonUtil.toast("对方忙");
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    private Observer<AVChatCommonEvent> observerHangUp = new Observer<AVChatCommonEvent>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void onEvent(AVChatCommonEvent event) {
            Log.i("logi", "对方已挂断 event.getChatId:" + event.getChatId());
            Parameters parameters = new Parameters();
            parameters.add("id", callId + "");
            parameters.add("chatId", event.getChatId() + "");

            HttpUtil.post(NetworkUtil.callFinish, parameters, null);
            finish();
        }
    };

    private Observer<AVChatTimeOutEvent> observerTimeOut = new Observer<AVChatTimeOutEvent>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void onEvent(AVChatTimeOutEvent timeOutEvent) {
            CommonUtil.toast("超时");
            finish();
        }
    };

    private boolean IS_CHATTING = false;
    private int requestcode_theme = 1;
    private PopupWindow facePopupWindow;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Dialog faceDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        initView();
        initData();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void nimCall() {
        if (callback_call == null) {
            callback_call = new AVChatCallback<AVChatData>() {

                @Override
                public void onException(Throwable arg0) {
                    CommonUtil.toast("拨打异常");
                }

                @Override
                public void onFailed(int arg0) {
                    CommonUtil.toast("拨打错误");
                }

                @Override
                public void onSuccess(AVChatData avChatData) {
                    cm_time.start();
                    CommonUtil.toast("拨打成功");
                }
            };
        }
        // 呼叫拨出
        AVChatManager.getInstance().call(target.Accid, AVChatType.AUDIO, null, callback_call);
    }

    private void nimHangup() {
        if (callback_hangup == null) {
            callback_hangup = new AVChatCallback<Void>() {

                @Override
                public void onException(Throwable arg0) {
                    Log.i("logi", "callactivity hangUp onException:" + arg0.getMessage());
                    finish();
                }

                @Override
                public void onFailed(int arg0) {
                    Log.i("logi", "callactivity hangUp onFailed:" + arg0);
                    finish();
                }

                @Override
                public void onSuccess(Void arg0) {
                    Log.i("logi", "callactivity hangUp onSuccess");
                    finish();
                }
            };
        }
        AVChatManager.getInstance().hangUp(callback_hangup);
    }

    public static void start(Context context, int id, String accId, String nickName, int callTypeAudio) {
        Intent intent = new Intent(context, ActivityCall.class);
        intent.putExtra(KEY_TARGET_ID, id);
        intent.putExtra(KEY_TARGET_ACCID, accId);
        intent.putExtra(KEY_TARGET_NICKNAME, nickName);
        context.startActivity(intent);
        Log.i(TAG, "id:" + id + " accId:" + accId + " nickName:" + nickName + " callTypeAudio:" + callTypeAudio);
    }

    private void initData() {
        Intent intent = getIntent();
        String nickname = intent.getStringExtra(KEY_TARGET_NICKNAME);
        tv_nickname.setText(nickname);
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);

        source = new User();
        source.Id = sp.getInt("id", 0);
        source.Accid = sp.getString("accid", "");
        source.NickName = sp.getString("", "");

        target = new User();
        target.Id = intent.getIntExtra(KEY_TARGET_ID, 0);
        target.Accid = intent.getStringExtra(KEY_TARGET_ACCID);
        target.NickName = intent.getStringExtra(KEY_TARGET_NICKNAME);

        nimCall();

        registerObserver();
    }

    private void initView() {
        bt_hangup = (Button) findViewById(R.id.bt_hangup);
        bt_mute = (Button) findViewById(R.id.bt_mute);
        bt_free = (Button) findViewById(R.id.bt_free);
        bt_face = (Button) findViewById(R.id.bt_face);
        bt_card = (Button) findViewById(R.id.bt_card);
        ll_user = findViewById(R.id.ll_user);
        ll_ctrl = findViewById(R.id.ll_ctrl);
        rl_main = findViewById(R.id.rl_main);

        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        cm_time = (Chronometer) findViewById(R.id.cm_time);
        // cm_time.stop();

        bt_hangup.setOnClickListener(this);
        bt_mute.setOnClickListener(this);
        bt_free.setOnClickListener(this);
        bt_face.setOnClickListener(this);
        bt_card.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_hangup:
                nimHangup();
                break;

            case R.id.bt_mute: {
                if (AVChatManager.getInstance().isMute()) {
                    // isMute是否处于静音状态
                    // 关闭音频
                    AVChatManager.getInstance().setMute(false);
                } else {
                    // 打开音频
                    AVChatManager.getInstance().setMute(true);
                }
                bt_mute.setText(AVChatManager.getInstance().isMute() ? "目前静音" : "目前不静音");
            }
            break;
            case R.id.bt_free: {
                // 设置扬声器是否开启
                AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
                bt_free.setText(AVChatManager.getInstance().speakerEnabled() ? "目前外放" : "目前耳机");
            }
            break;

            case R.id.bt_card:
                if (!IS_CHATTING) {
                    CommonUtil.toast("通话还没建立,无法翻牌");
                    return;
                }

                //  CardActivity.start(ActivityCall.this, source.Accid, target.Accid);
                startActivityForResult(new Intent(this, ThemeActivity.class), requestcode_theme);
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
                        android.util.Log.i(TAG, "onSuccess: 文本消息发送成功");
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
            case R.id.bt_face:

                if (faceDialog == null) {
                    View face = getLayoutInflater().inflate(R.layout.window_face, null);
                    faceDialog = new Dialog(this);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    faceDialog.setContentView(face, params);
                    faceDialog.setCanceledOnTouchOutside(true);
                    faceDialog.getWindow().setGravity(Gravity.BOTTOM);
                }
                faceDialog.show();

                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestcode_theme) {
            switch (resultCode) {
                case FragmentThemes.RESULTCODE_CHOOSE:
                    Theme theme = gson.fromJson(data.getStringExtra("theme"), new TypeToken<Theme>() {
                    }.getType());
                    // 构造自定义通知，指定接收者
                    CustomNotification notification = new CustomNotification();
                    notification.setSessionId(target.Accid);
                    notification.setSessionType(SessionTypeEnum.P2P);

                    NimSysNotice<Theme> i = new NimSysNotice<Theme>();
                    i.info = theme;
                    notification.setContent(gson.toJson(i));
                    // 发送自定义通知
                    NIMClient.getService(MsgService.class).sendCustomNotification(notification);

                    break;

                default:
                    CommonUtil.toast("没有正确选择");
                    break;
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AVChatManager.getInstance().observeCalleeAckNotification(observerCallAck, false);
        AVChatManager.getInstance().observeHangUpNotification(observerHangUp, false);
        AVChatManager.getInstance().observeTimeoutNotification(observerTimeOut, false);
    }

    private void registerObserver() {

        // 监听网络通话被叫方的响应（接听、拒绝、忙）
        AVChatManager.getInstance().observeCalleeAckNotification(observerCallAck, true);

        // 监听网络通话对方挂断的通知,即在正常通话时,结束通话
        AVChatManager.getInstance().observeHangUpNotification(observerHangUp, true);

        // 监听呼叫或接听超时通知
        // 主叫方在拨打网络通话时，超过 45 秒被叫方还未接听来电，则自动挂断。
        // 被叫方超过 45 秒未接听来听，也会自动挂断
        // 在通话过程中网络超时 30 秒自动挂断。
        AVChatManager.getInstance().observeTimeoutNotification(observerTimeOut, true);
    }


}