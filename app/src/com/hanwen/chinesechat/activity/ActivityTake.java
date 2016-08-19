package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
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
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.CallLog;
import com.hanwen.chinesechat.bean.ChatData;
import com.hanwen.chinesechat.bean.ChatDataExtra;
import com.hanwen.chinesechat.bean.MessageText;
import com.hanwen.chinesechat.bean.NimAttachment;
import com.hanwen.chinesechat.bean.NimSysNotice;
import com.hanwen.chinesechat.bean.Question;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.bean.UserLite;
import com.hanwen.chinesechat.fragment.FragmentThemes;
import com.hanwen.chinesechat.rts.ActionTypeEnum;
import com.hanwen.chinesechat.rts.doodle.DoodleView;
import com.hanwen.chinesechat.rts.doodle.SupportActionType;
import com.hanwen.chinesechat.rts.doodle.TransactionCenter;
import com.hanwen.chinesechat.rts.doodle.action.MyPath;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.FileUtil;
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
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserQuitType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatOptionalParam;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.rts.RTSCallback;
import com.netease.nimlib.sdk.rts.RTSChannelStateObserver;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.constant.RTSEventType;
import com.netease.nimlib.sdk.rts.constant.RTSTunType;
import com.netease.nimlib.sdk.rts.model.RTSCalleeAckEvent;
import com.netease.nimlib.sdk.rts.model.RTSData;
import com.netease.nimlib.sdk.rts.model.RTSNotifyOption;
import com.netease.nimlib.sdk.rts.model.RTSOptions;
import com.netease.nimlib.sdk.rts.model.RTSTunData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 电话接听界面
 *
 * @author liaorubei
 */
public class ActivityTake extends Activity implements OnClickListener {

    public static final String TAG = "ActivityTake";
    public static final String KEY_CHAT_MODE = "KEY_CHAT_MODE";
    public static final String KEY_CHAT_DATA = "KEY_CHAT_DATA";
    public static final int CHAT_MODE_OUTGOING = 0;
    public static final int CHAT_MODE_INCOMING = 1;
    public static final int WHAT_PLAY_SOUND = 1;
    public static final int WHAT_PEER_BUSY = 2;
    public static final int WHAT_HANG_UP = 3;
    public static final int WHAT_REFRESH = 4;
    public static final int REQUEST_CODE_THEME = 1;
    public static final int REQUEST_CODE_IMAGE = 2;

    private View bt_hangup, bt_reject, bt_accept, bt_mute, bt_free;
    private Gson gson = new Gson();
    private ImageView iv_icon;
    private boolean IS_CALL_ESTABLISHED = false;//通话是否已经建立
    private TextView tv_nickname, tv_time, tv_theme;
    private ChatData chatData;
    private LinearLayout ll_theme;
    private Chronometer cm_time;
    private ListView listview;
    private View ll_hang, ll_call;
    private List<String> list;
    private MyAdapter adapter;
    private User student;
    private String callId;
    private boolean CALL_ID_RECEIVE = false;
    private ChatDataExtra chatDataExtra;

    private DoodleView dv_board;
    private View rl_board;
    private View rl_image;
    private View ll_user;
    private View ll_profile;
    private View fl_content;
    private String sessionId;
    private ImageView iv_avatar;
    private TextView tv_name;
    private TextView tv_case;
    private boolean boardEstablished = false;
    private ListView lv_msg;
    private EditText et_msg;
    private List<MessageText> listMessage;
    private AdapterMessage adapterMessage;
    private View rl_texts;
    private View tv_board;
    private View tv_texts;
    private View tv_image;
    private int chatMode;
    private TextView tv_topic;
    private Theme currentTheme;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_PLAY_SOUND) {
                SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.RING);
            }
        }
    };

    //region 来电广播
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
                        notification.setSessionId(chatData.getAccount());
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
    private ImageView iv_image;
    private View iv_image_send;
    private String currentImagePath = null;

    //endregion

    /**
     * @param context  上下文
     * @param chatMode 通话模式,外呼还是来电
     * @param chatData 通话数据,包括昵称,头像,其中额外的数据在Extra里面
     */
    public static void start(Context context, int chatMode, ChatData chatData) {
        Intent intent = new Intent(context, ActivityTake.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CHAT_MODE, chatMode);
        intent.putExtra(KEY_CHAT_DATA, chatData);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_topic:
                //region 主题选项
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et_msg.getWindowToken(), 0);

                if (chatMode == CHAT_MODE_OUTGOING) {
                    ActivityTheme.start(ActivityTake.this, gson.toJson(currentTheme));
                }

                tv_topic.setSelected(!tv_topic.isSelected());
                // tv_board.setSelected(false);
                tv_texts.setSelected(false);
                tv_image.setSelected(false);

                ll_theme.setVisibility(tv_topic.isSelected() ? View.VISIBLE : View.INVISIBLE);
                rl_board.setVisibility(View.INVISIBLE);
                rl_texts.setVisibility(View.INVISIBLE);
                rl_image.setVisibility(View.INVISIBLE);
                //endregion
                break;
            case R.id.tv_board:
                //region 白板选项
                tv_board.setSelected(!tv_board.isSelected());
                tv_texts.setSelected(false);
                tv_image.setSelected(false);

                rl_board.setVisibility(tv_board.isSelected() ? View.VISIBLE : View.INVISIBLE);
                ll_theme.setVisibility(View.INVISIBLE);
                rl_texts.setVisibility(View.INVISIBLE);
                rl_image.setVisibility(View.INVISIBLE);
                // start(java.lang.String account,java.util.List<RTSTunType> tunTypes,RTSOptions options,RTSNotifyOption notifyOption,RTSCallback<RTSData> callback)
                /*(发送方)发起会话
                参数:
                account - 对方帐号
                tunTypes - 通道类型集合：语音、TCP、UDP
                options - 可选参数: 是否录制通道数据
                notifyOption - 可选参数: 推送相关参数控制
                callback - 回调函数，返回RTSData
                返回:
                会话ID，若返回null，表示发起失败，注意：音频通道同时只能有一个会话开启。 回调onFailed错误码： 200:成功 414:参数错误 509:通道失效 11001:无可送达的被叫方,主叫方可直接挂断 501:数据库失败 514:服务不可用
               */
                if (false)//!boardEstablished)//
                {
                    List<RTSTunType> types = new ArrayList<>(1);
                    types.add(RTSTunType.TCP);
                    String pushContent = "发起一个会话";
                    String extra = "extra_data";
                    RTSOptions rtsOptions = new RTSOptions();
                    RTSNotifyOption rtsNotifyOption = new RTSNotifyOption();
                    sessionId = RTSManager.getInstance().start(chatData.getAccount(), types, rtsOptions, rtsNotifyOption, new RTSCallback<RTSData>() {
                        @Override
                        public void onSuccess(RTSData rtsData) {
                            Log.i(TAG, "白板 onSuccess: ");
                        }

                        @Override
                        public void onFailed(int i) {
                            Log.i(TAG, "白板 onFailed: ");
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            Log.i(TAG, "白板 onException: ");
                        }
                    });

                    RTSManager.getInstance().observeCalleeAckNotification(sessionId, calleeAckEventObserver, true);
                    RTSManager.getInstance().observeReceiveData(sessionId, receiveDataObserver, true);
                    RTSManager.getInstance().observeChannelState(sessionId, observerRTSChannelState, true);
                }
                //endregion
                break;
            case R.id.tv_text:
                //region 文字选项
                tv_texts.setSelected(!tv_texts.isSelected());
                // tv_board.setSelected(false);
                tv_image.setSelected(false);
                tv_topic.setSelected(false);

                rl_texts.setVisibility(tv_texts.isSelected() ? View.VISIBLE : View.INVISIBLE);
                ll_theme.setVisibility(View.INVISIBLE);
                rl_board.setVisibility(View.INVISIBLE);
                rl_image.setVisibility(View.INVISIBLE);
                //endregion
                break;
            case R.id.tv_image:
                //region 图片选项
                InputMethodManager systemService1 = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                systemService1.hideSoftInputFromWindow(et_msg.getWindowToken(), 0);

                tv_image.setSelected(!tv_image.isSelected());
                //  tv_board.setSelected(false);
                tv_texts.setSelected(false);
                tv_topic.setSelected(false);

                rl_image.setVisibility(tv_image.isSelected() ? View.VISIBLE : View.INVISIBLE);
                rl_board.setVisibility(View.INVISIBLE);
                rl_texts.setVisibility(View.INVISIBLE);
                ll_theme.setVisibility(View.INVISIBLE);
                //endregion
                break;
            case R.id.bt_msg:
                //region 文本消息发送按钮
                String text = et_msg.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {

                    MessageText messageText = new MessageText();
                    messageText.FromNickname = ChineseChat.CurrentUser.Nickname;
                    messageText.Content = text;
                    listMessage.add(messageText);
                    adapterMessage.notifyDataSetChanged();
                    et_msg.setText("");
                    lv_msg.smoothScrollToPosition(listMessage.size() - 1);

                    /**
                     * 创建文本消息
                     * @param accid
                     * @param chatType
                     * @param message
                     */
                    IMMessage message = MessageBuilder.createTextMessage(chatData.getAccount(), SessionTypeEnum.P2P, text);
                    // 发送消息。如果需要关心发送结果，可设置回调函数。发送完成时，会收到回调。如果失败，会有具体的错误码。
                    NIMClient.getService(MsgService.class).sendMessage(message, false);
                }
                //endregion
                break;
            case R.id.iv_image_send:
                //region图片消息发送按钮

/*                *//** 创建图片消息
             * // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
             * // 聊天类型，单聊或群组
             *  // 图片文件对象
             *   // 文件显示名字，如果第三方 APP 不关注，可以为 null
             *//*
                IMMessage message = MessageBuilder.createImageMessage(chatData.getAccount(), SessionTypeEnum.P2P,
                        new File(Environment.getExternalStorageDirectory(), "DCIM/QQPhoto/IMG20151214092728.jpg"), null);
                *//**
             *@param msg - 带发送的消息体，由MessageBuilder构造
             *@param resend - 如果是发送失败后重发，标记为true，否则填false
             *//*
                NIMClient.getService(MsgService.class).sendMessage(message, false);*/

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
                //endregion
                break;
            case R.id.bt_reject:
                //region拒绝接听
            {
                Animation animation = new ScaleAnimation(1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F);
                animation.setDuration(500);
                ll_hang.startAnimation(animation);
                hangup();
            }
            //endregion
            break;
            case R.id.bt_accept:
                //region 同意接听
            {
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

                ll_user.setVisibility(View.INVISIBLE);
                ll_profile.setVisibility(View.VISIBLE);


                AVChatOptionalParam params = new AVChatOptionalParam();
                params.enableCallProximity(false);
                AVChatManager.getInstance().accept(params, callbackAccept);
            }
            //endregion
            break;
            case R.id.bt_hangup:
                //region 挂断
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
                            hangup();
                        }
                    });
                    builder.setMessage(R.string.ActivityTake_hangup);
                    builder.show();
                } else {
                    hangup();
                }
                //endregion
                break;
            case R.id.bt_mute:
                //region是否静音
                if (IS_CALL_ESTABLISHED) {
                    AVChatManager.getInstance().muteLocalAudio(!AVChatManager.getInstance().isLocalAudioMuted());
                    bt_mute.setSelected(AVChatManager.getInstance().isLocalAudioMuted());
                }
                //endregion
                break;
            case R.id.bt_free:
                //region 是否外放
                if (IS_CALL_ESTABLISHED) {
                    AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
                    bt_free.setSelected(!AVChatManager.getInstance().speakerEnabled());
                }
                //endregion
                break;
            case R.id.tv_clear:
                Log.i(TAG, "onClick: clear");
                break;
            case R.id.iv_image:
                //region 点击图片放大
                if (!TextUtils.isEmpty(currentImagePath)) {
                    ActivityAlbum.start(this, new String[]{currentImagePath}, 0);
                }
                //endregion
                break;

        }
    }

    private void hangup() {
        AVChatManager.getInstance().hangUp(callbackHangup);
        SoundPlayer.instance(ChineseChat.getContext()).stop();
    }

    private void showThemeQuestion(Theme theme) {
        tv_topic.setSelected(true);
        tv_topic.setVisibility(View.VISIBLE);
        ll_theme.setVisibility(View.VISIBLE);

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

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(broadcastReceiver, filter1);
    }

    private void initView() {
        //头像
        ll_user = findViewById(R.id.ll_user);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        tv_time = (TextView) findViewById(R.id.tv_time);

        //小头像,名称,学习情况
        ll_profile = findViewById(R.id.ll_profile);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_case = (TextView) findViewById(R.id.tv_case);

        //主题,白板,文字,图片 控制按钮
        tv_topic = (TextView) findViewById(R.id.tv_topic);
        tv_board = findViewById(R.id.tv_board);
        tv_texts = findViewById(R.id.tv_text);
        tv_image = findViewById(R.id.tv_image);
        tv_topic.setOnClickListener(this);
        tv_board.setOnClickListener(this);
        tv_texts.setOnClickListener(this);
        tv_image.setOnClickListener(this);

        fl_content = findViewById(R.id.fl_content);

        //主题,布局
        ll_theme = (LinearLayout) findViewById(R.id.ll_theme);
        tv_theme = (TextView) findViewById(R.id.tv_theme);
        listview = (ListView) findViewById(R.id.listview);

        //白板
        rl_board = findViewById(R.id.rl_board);
        dv_board = (DoodleView) findViewById(R.id.dv_board);
        View tv_prev = findViewById(R.id.tv_prev);
        View tv_clear = findViewById(R.id.tv_clear);
        tv_clear.setOnClickListener(this);

        //文字
        rl_texts = findViewById(R.id.rl_texts);
        lv_msg = (ListView) findViewById(R.id.lv_msg);
        et_msg = (EditText) findViewById(R.id.et_msg);
        View bt_msg = findViewById(R.id.bt_msg);
        rl_texts.setOnClickListener(this);
        bt_msg.setOnClickListener(this);

        //图片
        rl_image = findViewById(R.id.rl_image);
        iv_image = (ImageView) findViewById(R.id.iv_image);
        iv_image.setOnClickListener(this);
        iv_image_send = findViewById(R.id.iv_image_send);
        iv_image_send.setOnClickListener(this);

        //控制按钮,布局
        ll_call = findViewById(R.id.ll_call);
        ll_hang = findViewById(R.id.ll_hang);

        bt_mute = findViewById(R.id.bt_mute);
        bt_hangup = findViewById(R.id.bt_hangup);
        bt_free = findViewById(R.id.bt_free);
        bt_reject = findViewById(R.id.bt_reject);
        bt_accept = findViewById(R.id.bt_accept);

        bt_mute.setOnClickListener(this);
        bt_hangup.setOnClickListener(this);
        bt_free.setOnClickListener(this);
        bt_reject.setOnClickListener(this);
        bt_accept.setOnClickListener(this);

        cm_time = (Chronometer) findViewById(R.id.cm_time);


        list = new ArrayList<>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
    }

    private void initData() {
        Intent intent = getIntent();
        chatMode = intent.getIntExtra(KEY_CHAT_MODE, -1);
        chatData = (ChatData) intent.getSerializableExtra(KEY_CHAT_DATA);
        chatDataExtra = gson.fromJson(chatData.getExtra(), ChatDataExtra.class);

        //如果是去电
        if (chatMode == CHAT_MODE_OUTGOING) {
            SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.CONNECTING);
            //界面
            ll_call.setVisibility(View.VISIBLE);
            ll_hang.setVisibility(View.INVISIBLE);

            //隐藏没有必要的界面
            tv_topic.setVisibility(View.VISIBLE);
            tv_image.setVisibility(View.VISIBLE);
            tv_texts.setVisibility(View.VISIBLE);
            tv_board.setVisibility(View.GONE);
            tv_case.setVisibility(View.GONE);

            tv_name.setText(getString(R.string.ActivityTake_show_teacher_nickname, chatDataExtra.Teacher.Nickname));
            tv_nickname.setText(chatDataExtra.Teacher.Nickname);
            tv_case.setText(getString(R.string.ActivityTake_show_teacher_summary, chatDataExtra.Teacher.Summary.duration, chatDataExtra.Teacher.Summary.count, chatDataExtra.Teacher.Summary.month));

            CommonUtil.showBitmap(iv_icon, NetworkUtil.getFullPath(chatDataExtra.Teacher.Avatar));
            CommonUtil.showBitmap(iv_avatar, NetworkUtil.getFullPath(chatDataExtra.Teacher.Avatar));

            //呼出
            JsonObject student = new JsonObject();
            student.addProperty("Id", ChineseChat.CurrentUser.Id);
            student.addProperty("Avatar", ChineseChat.CurrentUser.Avatar);
            student.addProperty("Nickname", ChineseChat.CurrentUser.Nickname);
            JsonObject summary = new JsonObject();
            summary.addProperty("month", 0);
            summary.addProperty("count", 0);
            summary.addProperty("duration", 0);
            student.add("Summary", summary);

            AVChatNotifyOption option = new AVChatNotifyOption();
            option.apnsBadge = false;
            option.apnsInuse = true;
            option.pushSound = "pushRing.aac";//Push
            option.extendMessage = chatData.getExtra();// student.toString();//把呼叫者的用户名,头像发送过去

            AVChatOptionalParam params = new AVChatOptionalParam();
            params.enableCallProximity(false);
            AVChatManager.getInstance().call(chatData.getAccount(), chatData.getChatType(), params, option, callback_call);
        }
        //如果是来电
        else if (chatMode == CHAT_MODE_INCOMING) {
            SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.RING);

            //界面
            ll_hang.setVisibility(View.VISIBLE);
            ll_call.setVisibility(View.INVISIBLE);

            //隐藏没有必要的界面
            tv_topic.setVisibility(View.VISIBLE);
            tv_board.setVisibility(View.GONE);
            tv_texts.setVisibility(View.VISIBLE);
            tv_image.setVisibility(View.VISIBLE);

            //2016-07-21 如果没有对方数据,那么请求网络
            if (chatDataExtra.Student == null || chatDataExtra.Teacher == null) {
                Log.i(TAG, "请求网络更新数据");

                //来电数据兼容处理
                if (chatDataExtra.Student == null) {
                    chatDataExtra.Student = new UserLite();
                    chatDataExtra.Student.Id = chatDataExtra.Id;

                    chatDataExtra.Teacher = new UserLite();
                    chatDataExtra.Teacher.Id = ChineseChat.CurrentUser.Id;
                }

                Parameters params = new Parameters();
                params.add("accid", chatData.getAccount());
                HttpUtil.post(NetworkUtil.nimUserGetUserChatDataByAccid, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {}.getType());
                        if (resp.code == 200) {
                            User user = resp.info;

                            tv_nickname.setText(TextUtils.isEmpty(user.Nickname) ? user.Username : user.Nickname);
                            tv_name.setText(getString(R.string.ActivityTake_show_student_nickname, user.Nickname, user.Country));
                            tv_case.setText(getString(R.string.ActivityTake_show_student_summary, user.Summary.duration, user.Summary.count));
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                    }
                });
            }
            //如果是新版本的呼叫,携带了数据,那么直接显示
            else {
                tv_nickname.setText(TextUtils.isEmpty(chatDataExtra.Student.Nickname) ? chatDataExtra.Student.Username : chatDataExtra.Student.Nickname);
                tv_name.setText(getString(R.string.ActivityTake_show_student_nickname, chatDataExtra.Student.Nickname, TextUtils.isEmpty(chatDataExtra.Student.Country) ? "未知" : chatDataExtra.Student.Country));
                tv_case.setText(getString(R.string.ActivityTake_show_student_summary, chatDataExtra.Student.Summary.duration, chatDataExtra.Student.Summary.count));

                CommonUtil.showBitmap(iv_icon, NetworkUtil.getFullPath(chatDataExtra.Student.Avatar));
                CommonUtil.showBitmap(iv_avatar, NetworkUtil.getFullPath(chatDataExtra.Student.Avatar));
            }

        } else {
            CommonUtil.toast("通话异常");
            finish();
        }

        //如果拨打方云信SDK版本过低,不支持avChatData.getExtra(),那就不显示头像吧
/*        try {
            student = gson.fromJson(chatData.getExtra(), new TypeToken<User>() {}.getType());
            tv_nickname.setText(TextUtils.isEmpty(student.Nickname) ? student.Username : student.Nickname);
            tv_name.setText(TextUtils.isEmpty(student.Nickname) ? student.Username : student.Nickname);
            tv_case.setText("学习情况");

            CommonUtil.showIcon(getApplicationContext(), iv_icon, student.Avatar);
            CommonUtil.showIcon(getApplicationContext(), iv_avatar, student.Avatar);

        } catch (Exception ex) {
            Log.i(TAG, "initData: 对方云信SDK版本过低");
        }*/

        listMessage = new ArrayList<>();
        adapterMessage = new AdapterMessage(listMessage);
        lv_msg.setAdapter(adapterMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObserver(false);
        unregisterReceiver(broadcastReceiver);
        handler.removeCallbacksAndMessages(null);//避免出现拨出又马上挂断的情况,回铃声会在5秒之后响起
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_THEME) {
            if (resultCode == FragmentThemes.RESULTCODE_CHOOSE) {
                tv_topic.setText(R.string.ActivityTake_tab_topic_switch);
                currentTheme = gson.fromJson(data.getStringExtra("theme"), new TypeToken<Theme>() {
                }.getType());

                // 构造自定义通知，指定接收者,并发送自定义通知
                NimSysNotice<Theme> notice = new NimSysNotice<>();
                notice.type = NimSysNotice.NoticeType_Card;
                notice.info = currentTheme;

                CustomNotification notification = new CustomNotification();
                notification.setSessionId(chatData.getAccount());
                notification.setSessionType(SessionTypeEnum.P2P);
                notification.setContent(gson.toJson(notice));
                NIMClient.getService(MsgService.class).sendCustomNotification(notification);
            } else {
                CommonUtil.toast(R.string.ActivityCall_topic_choose_failed);
            }
        }
        //选择图片,发送图片
        else if (requestCode == REQUEST_CODE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                File photoPick = new File(FileUtil.getPath(this, data.getData()));

                currentImagePath = photoPick.getAbsolutePath();

                /**创建图片消息
                 *@param accid 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                 *@param ChatType 聊天类型，单聊或群组
                 *@param file 图片文件对象
                 *@param display 文件显示名字，如果第三方 APP 不关注，可以为 null
                 */
                IMMessage message = MessageBuilder.createImageMessage(chatData.getAccount(), SessionTypeEnum.P2P, photoPick, null);
                /**
                 *@param msg - 带发送的消息体，由MessageBuilder构造
                 *@param resend - 如果是发送失败后重发，标记为true，否则填false
                 */
                NIMClient.getService(MsgService.class).sendMessage(message, false);
                CommonUtil.showBitmap(iv_image, photoPick.getAbsolutePath());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void registerObserver(boolean register) {

        AVChatManager.getInstance().observeCalleeAckNotification(observerCalleeAck, register);

        //监听通话过程中状态变化
        AVChatManager.getInstance().observeAVChatState(observerChatState, register);

        // 监听网络通话对方挂断的通知,即在正常通话时,结束通话
        AVChatManager.getInstance().observeHangUpNotification(observerHangup, register);

        // 监听呼叫或接听超时通知
        // 主叫方在拨打网络通话时，超过 45 秒被叫方还未接听来电，则自动挂断。
        // 被叫方超过 45 秒未接听来听，也会自动挂断
        // 在通话过程中网络超时 30 秒自动挂断。
        AVChatManager.getInstance().observeTimeoutNotification(observerTimeout, register);

        // 如果有自定义通知是作用于全局的，不依赖某个特定的 Activity，那么这段代码应该在 Application 的 onCreate 中就调用
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(observerCustomNotification, register);

        //监听基础消息来临
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(observerBaseMessage, register);

        //监听消息状态变化
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(observerBaseMessageStatus, register);

        // 如果发送的多媒体文件消息，还需要监听文件的上传进度。
        NIMClient.getService(MsgServiceObserve.class).observeAttachmentProgress(observerAttachmentProgress, true);
    }

    //region拨打回调
    private AVChatCallback<AVChatData> callback_call = new AVChatCallback<AVChatData>() {

        @Override
        public void onException(Throwable arg0) {
            Log.i(TAG, "拨打异常回调: " + arg0.getMessage());
            SoundPlayer.instance(ChineseChat.getContext()).stop();
            CommonUtil.toast(R.string.ActivityCall_call_error);

            //拨打异常就让老师下线
            Parameters params = new Parameters();
            params.add("id", chatDataExtra.Teacher.Id);
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

            //拨打异常就让老师下线
            Parameters params = new Parameters();
            params.add("id", chatDataExtra.Teacher.Id);
            HttpUtil.post(NetworkUtil.teacherDequeue, params, null);
            finish();
        }

        @Override
        public void onSuccess(AVChatData avChatData) {

            Log.i(TAG, "拨打成功回调: ChatId=" + avChatData.getChatId());
            chatData.setChatId(avChatData.getChatId());
            Log.i(TAG, "onSuccess: " + chatData.getAccount() + " " + chatData.getExtra());
            Log.i(TAG, "onSuccess: " + avChatData.getAccount() + " " + avChatData.getExtra());

            //记录下ChatId,如果对方还没有接听就直接挂断,帮对方上线并入队,如果拨打失败则暂时不上线
            //chatId = avChatData.getChatId();
            handler.sendEmptyMessageDelayed(WHAT_PLAY_SOUND, 5000);
        }
    };
    //endregion

    //region接听回调
    private AVChatCallback<Void> callbackAccept = new AVChatCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Log.i(TAG, "回应成功: ");
            SoundPlayer.instance(ChineseChat.getContext()).stop();

            //创建记录
            chatHistoryCreate();

            //定时挂断
        }

        @Override
        public void onFailed(int i) {
            Log.i(TAG, "回应失败: " + i);
            SoundPlayer.instance(ChineseChat.getContext()).stop();
            finish();
        }

        @Override
        public void onException(Throwable throwable) {
            Log.i(TAG, "回应异常: " + throwable.getMessage());
            SoundPlayer.instance(ChineseChat.getContext()).stop();
            finish();
        }
    };
    //endregion

    //region挂断回调
    private AVChatCallback<Void> callbackHangup = new AVChatCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Log.i(TAG, "挂断成功回调");
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
    //endregion

    //region 状态监听
    private AVChatStateObserver observerChatState = new AVChatStateObserver() {

        @Override
        public void onTakeSnapshotResult(String s, boolean b, String s1) {

        }

        @Override
        public void onConnectionTypeChanged(int i, int i1) {

        }

        @Override
        public void onLocalRecordEnd(String[] strings, int i) {
            Log.i(TAG, "onLocalRecordEnd: " + strings.toString() + " i=" + i);
        }

        @Override
        public void onFirstVideoFrameAvailable(String s) {

        }

        @Override
        public void onVideoFpsReported(String s, int i) {

        }

        @Override
        public void onJoinedChannel(int i, String s, String s1) {
            Log.i(TAG, "进入频道: ");
        }

        @Override
        public void onLeaveChannel() {

            //// TODO: 2016-07-21 当自己意外退出频道时,应该退出界面

            Log.i(TAG, "离开频道: ");
        }

        @Override
        public void onUserJoined(String s) {
            Log.i(TAG, "对方加入: " + s);
        }

        @Override
        public void onUserLeave(String s, int i) {
            Log.i(TAG, "对方离开: account=" + s + " event=" + (AVChatUserQuitType.TIMEOUT == i ? "TIMEOUT" : "NORMAL"));

            //当对方意外掉线时,当事人在检测到对方已经离开时,自动挂断,免得时间太长了
            AVChatManager.getInstance().hangUp(null);
            chatHistoryFinish();
            finish();
        }

        @Override
        public void onProtocolIncompatible(int i) {

        }

        @Override
        public void onDisconnectServer() {

        }

        @Override
        public void onNetworkQuality(String s, int i) {

        }

        @Override
        public void onCallEstablished() {
            Log.i(TAG, "通话建立: ");
            cm_time.setBase(SystemClock.elapsedRealtime());
            cm_time.start();
            IS_CALL_ESTABLISHED = true;

            //当通话建立时,切换大小头像,并显示对方资料
            ll_user.setVisibility(View.INVISIBLE);
            ll_profile.setVisibility(View.VISIBLE);
        }

        @Override
        public void onDeviceEvent(String s, int i, String s1) {
            Log.i(TAG, "设备事件: " + s1);
            //当设备准备好的时候就默认打开外放
            AVChatManager.getInstance().setSpeaker(true);
            bt_free.setSelected(!AVChatManager.getInstance().speakerEnabled());
        }

    };
    //endregion

    //region回应监听
    private Observer<AVChatCalleeAckEvent> observerCalleeAck = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent event) {
            Log.i(TAG, "对方回应监听: ChatId=" + event.getChatId() + " Event=" + event.getEvent() + " account=" + event.getAccount() + " extra=" + event.getExtra());

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
                // handler.sendEmptyMessageDelayed(WHAT_PEER_BUSY, 5000);
                finish();
                return;
            }

            if (event.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                // 对方同意接听
                if (event.isDeviceReady()) {
                    // 设备初始化成功，开始通话,关闭回铃声
                    CommonUtil.toast(R.string.ActivityCall_device_ready);

                    //创建记录
                    chatHistoryCreate();

                    //挂断定时
                    handler.sendEmptyMessageDelayed(WHAT_HANG_UP, 1000);

                    //刷新定时
                    handler.sendEmptyMessageDelayed(WHAT_REFRESH, 1000);
                } else {
                    // 设备初始化失败，无法进行通话
                    CommonUtil.toast(R.string.ActivityCall_device_error);
                    finish();
                }
            }
        }

    };
    //endregion

    //region 挂断监听
    private Observer<AVChatCommonEvent> observerHangup = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent event) {
            Log.i(TAG, "对方挂断监听: ChatId=" + event.getChatId());
            if (TextUtils.isEmpty(callId)) {
                SoundPlayer.instance(ChineseChat.getContext()).stop();
            } else {
                chatHistoryFinish();
            }
            finish();
        }
    };
    //endregion

    //region 超时监听
    private Observer<AVChatTimeOutEvent> observerTimeout = new Observer<AVChatTimeOutEvent>() {
        @Override
        public void onEvent(AVChatTimeOutEvent avChatTimeOutEvent) {
            Log.i(TAG, "超时监听=" + avChatTimeOutEvent.name());
            SoundPlayer.instance(ChineseChat.getContext()).stop();
            finish();
        }
    };
    //endregion

    //region 基础消息监听
    private Observer<List<IMMessage>> observerBaseMessage = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            for (IMMessage m : messages) {
                Log.i(TAG, "收到基础消息: " + m.getFromNick() + " from: " + m.getFromAccount());
                MsgTypeEnum msgType = m.getMsgType();

                //文本消息
                if (msgType == MsgTypeEnum.text) {
                    MessageText messageText = new MessageText();
                    messageText.FromNickname = chatMode == CHAT_MODE_INCOMING ? chatDataExtra.Student.Nickname : chatDataExtra.Teacher.Nickname;
                    messageText.Content = m.getContent();
                    listMessage.add(messageText);

                    adapterMessage.notifyDataSetChanged();
                    lv_msg.smoothScrollToPosition(listMessage.size() - 1);

                    tv_texts.setSelected(true);
                    tv_image.setSelected(false);
                    tv_topic.setSelected(false);
                    tv_board.setSelected(false);
                    tv_texts.setVisibility(View.VISIBLE);

                    rl_texts.setVisibility(View.VISIBLE);
                    ll_theme.setVisibility(View.INVISIBLE);
                    rl_board.setVisibility(View.INVISIBLE);
                    rl_image.setVisibility(View.INVISIBLE);
                }
                //图片消息
                else if (m.getMsgType() == MsgTypeEnum.image) {
                    tv_image.setSelected(true);
                    tv_topic.setSelected(false);
                    tv_texts.setSelected(false);
                    tv_board.setSelected(false);
                    tv_image.setVisibility(View.VISIBLE);

                    rl_image.setVisibility(View.VISIBLE);
                    rl_texts.setVisibility(View.INVISIBLE);
                    ll_theme.setVisibility(View.INVISIBLE);
                    rl_board.setVisibility(View.INVISIBLE);
                }
            }
        }
    };
    //endregion

    //region基础消息下载监听
    private Observer<IMMessage> observerBaseMessageStatus = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            // 1、根据sessionId判断是否是自己的消息
            if (TextUtils.equals(chatData.getAccount(), msg.getSessionId())) {

                if (msg.getMsgType() == MsgTypeEnum.image) {
                    NimAttachment nimAttachment = gson.fromJson(msg.getAttachment().toJson(true), NimAttachment.class);
                    CommonUtil.showBitmap(iv_image, nimAttachment.url);
                    currentImagePath = nimAttachment.url;
                }
            }
        }
    };
    //endregion

    //region附件进度监听
    private Observer<AttachmentProgress> observerAttachmentProgress = new Observer<AttachmentProgress>() {
        @Override
        public void onEvent(AttachmentProgress attachmentProgress) {
        }
    };
    //endregion

    //region 白板回应监听
    private Observer<RTSCalleeAckEvent> calleeAckEventObserver = new Observer<RTSCalleeAckEvent>() {
        @Override
        public void onEvent(RTSCalleeAckEvent rtsCalleeAckEvent) {
            Log.i(TAG, "onEvent: " + rtsCalleeAckEvent.getEvent() + "" + " 对方回应");
            if (rtsCalleeAckEvent.getEvent() == RTSEventType.CALLEE_ACK_AGREE) {

                Log.i(TAG, "onEvent: rtsCalleeAckEvent.isTunReady()=" + rtsCalleeAckEvent.isTunReady());
                // 判断SDK自动开启通道是否成功
                if (!rtsCalleeAckEvent.isTunReady()) {
                    return;
                }
                // add support ActionType
                SupportActionType.getInstance().addSupportActionType(ActionTypeEnum.Path.getValue(), MyPath.class);

                dv_board.init(rtsCalleeAckEvent.getSessionId(), rtsCalleeAckEvent.getAccount(), DoodleView.Mode.BOTH, Color.WHITE, ActivityTake.this);
                dv_board.setPaintSize(5);
                dv_board.setPaintType(ActionTypeEnum.Path.getValue());
                dv_board.setPaintOffset(0, fl_content.getTop());

                // 进入会话界面
            } else if (rtsCalleeAckEvent.getEvent() == RTSEventType.CALLEE_ACK_REJECT) {
                // 被拒绝，结束会话
            }
        }
    };
    //endregion

    //region 白板数据监听
    Observer<RTSTunData> receiveDataObserver = new Observer<RTSTunData>() {
        @Override
        public void onEvent(RTSTunData rtsTunData) {
            Log.i(TAG, "onEvent: " + rtsTunData.getSessionId());
            String data = "[parse bytes error]";
            try {
                data = new String(rtsTunData.getData(), 0, rtsTunData.getLength(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            TransactionCenter.getInstance().onReceive(sessionId, data);
        }
    };
    //endregion

    //region 白板状态监听
    private RTSChannelStateObserver observerRTSChannelState = new RTSChannelStateObserver() {
        @Override
        public void onConnectResult(RTSTunType rtsTunType, int i) {

        }

        @Override
        public void onChannelEstablished(RTSTunType rtsTunType) {
            boardEstablished = true;
        }

        @Override
        public void onDisconnectServer(RTSTunType rtsTunType) {

        }

        @Override
        public void onRecordInfo(RTSTunType rtsTunType, String s) {

        }

        @Override
        public void onError(RTSTunType rtsTunType, int i) {

        }

        @Override
        public void onNetworkStatusChange(RTSTunType rtsTunType, int i) {

        }
    };
    //endregion

    //region 自定义通知监听
    private Observer<CustomNotification> observerCustomNotification = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification message) {
            Log.i(TAG, "收到自定义通知: " + message.getContent());
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
                case NimSysNotice.NoticeType_Card: {
                    Theme theme = gson.fromJson(info, Theme.class);
                    CommonUtil.toast("对方选择了话题:" + theme.Name);

                    Parameters params = new Parameters();
                    params.add("chatId", chatData.getChatId());
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

                    tv_topic.setSelected(true);
                    tv_texts.setSelected(false);
                    tv_image.setSelected(false);

                    ll_theme.setVisibility(View.VISIBLE);
                    rl_texts.setVisibility(View.INVISIBLE);
                    rl_image.setVisibility(View.INVISIBLE);
                    showThemeQuestion(theme);
                }
                break;
                case NimSysNotice.NoticeType_Chat:
                    CALL_ID_RECEIVE = true;
                    callId = info;
                    break;
            }

        }
    };

    //endregion

    private void chatHistoryCreate() {
        Parameters parameters = new Parameters();
        parameters.add("source", chatDataExtra.Student.Id);
        parameters.add("target", chatDataExtra.Teacher.Id);
        parameters.add("chatId", chatData.getChatId());
        parameters.add("chatType", chatData.getChatType().getValue());
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
                    notification.setSessionId(chatData.getAccount());
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

                //如果是学生端,保存学币信息,如果是教师端,保存当月课时统计
                Response<CallLog> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<CallLog>>() {
                }.getType());

                if (resp.code == 200) {
                    if (ChineseChat.isStudent()) {
                        ChineseChat.CurrentUser.Coins = resp.info.Student.Coins;
                        CommonUtil.saveUserToSP(ChineseChat.getContext(), ChineseChat.CurrentUser, false);

                    } else {
                        ChineseChat.CurrentUser.Summary = resp.info.Teacher.Summary;
                    }
                    getSharedPreferences("user", MODE_PRIVATE).edit().putString("userJson", gson.toJson(ChineseChat.CurrentUser)).commit();
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "记录结束失败:" + msg);
            }
        });
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

    private class AdapterMessage extends BaseAdapter<MessageText> {
        public AdapterMessage(List<MessageText> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MessageText item = getItem(position);
            View inflate = View.inflate(getApplication(), R.layout.listitem_text_message, null);
            TextView nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
            TextView message = (TextView) inflate.findViewById(R.id.tv_message);
            nickname.setText(item.FromNickname);
            message.setText(item.Content);
            return inflate;
        }
    }
}
