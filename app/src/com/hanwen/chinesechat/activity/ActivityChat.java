package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.CallLog;
import com.hanwen.chinesechat.bean.ChatData;
import com.hanwen.chinesechat.bean.ChatDataExtra;
import com.hanwen.chinesechat.bean.Lyric;
import com.hanwen.chinesechat.bean.MessageText;
import com.hanwen.chinesechat.bean.NimSysNotice;
import com.hanwen.chinesechat.bean.Question;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.bean.UserLite;
import com.hanwen.chinesechat.fragment.FragmentChatHskk;
import com.hanwen.chinesechat.fragment.FragmentCourse;
import com.hanwen.chinesechat.fragment.FragmentCourseShow;
import com.hanwen.chinesechat.fragment.FragmentThemes;
import com.hanwen.chinesechat.fragment.FragmentTopics;
import com.hanwen.chinesechat.fragment.FragmentTopicsShow;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.FileUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.util.SoundPlayer;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
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
import com.netease.nimlib.sdk.avchat.constant.AVChatUserQuitType;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatOptionalConfig;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 实时音视频聊天界面
 */
public class ActivityChat extends FragmentActivity implements OnClickListener {
    public static final String TAG = "ActivityChat";
    public static final int CHAT_MODE_INCOMING = 1;
    public static final int CHAT_MODE_OUTGOING = 0;

    public static final int REQUEST_CODE_IMAGE = 2;
    public static final int REQUEST_CODE_IMAGE_CAPTURE = 3;
    public static final int REQUEST_CODE_THEME = 1;
    public static final int WHAT_HANG_UP = 3;
    public static final int WHAT_PEER_BUSY = 2;
    public static final int WHAT_PLAY_SOUND = 1;
    public static final int WHAT_REFRESH = 4;
    public static final String KEY_CHAT_DATA = "KEY_CHAT_DATA";
    public static final String KEY_CHAT_MODE = "KEY_CHAT_MODE";

    private AdapterMessage adapterMessage;
    private boolean CALL_ID_RECEIVE = false;
    private boolean IS_CALL_ESTABLISHED = false;//通话是否已经建立
    public ChatData chatData;
    private ChatDataExtra chatDataExtra;
    private Chronometer cm_time;
    private EditText et_msg;
    private FrameLayout fl_content;
    private Gson gson = new Gson();
    private ImageView iv_avatar;
    private ImageView iv_icon;
    private int chatMode;

    private List<IMMessage> listImageMessage = new ArrayList<IMMessage>();
    private List<MessageText> listMessage;
    private List<String> list;
    private ListView listview;
    private ListView lv_msg;
    private AdapterThemes adapter;
    private String callId;
    private TextView tv_case;
    private TextView tv_name;
    private TextView tv_nick, tv_theme;
    private View ll_topic;
    private View bt_hangup, bt_reject, bt_accept, bt_mute, bt_free;
    private View ll_hang, ll_call;

    private View rl_theme;
    private View rl_image;

    private View ll_image;
    private View ll_text;

    private File fileImageCapture;

    private long delayMillisRefresh = 60 * 1000;//学生端第分钟计时刷新时间，第60秒刷新一次，当学币少于30时，每15秒刷新一次
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_PLAY_SOUND) {
                SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.RING);
            }
            //如果没有学币，则挂断
            else if (msg.what == WHAT_REFRESH) {
                Parameters params = new Parameters();
                params.add("callId", callId);
                HttpUtil.post(NetworkUtil.callRefresh, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "刷新成功: " + responseInfo.result);
                        Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {}.getType());
                        if (200 == resp.code) {
                            if (resp.info.Coins <= 30) {
                                if (delayMillisRefresh > 15000) {
                                    Builder builder = new Builder(ActivityChat.this);
                                    builder.setMessage(getString(R.string.ActivityChat_balance_tips, resp.info.Coins, resp.info.Coins / 10));
                                    builder.setPositiveButton(R.string.ActivityTake_confirm, null);
                                    builder.show();

                                    //当学币少于30时，刷新时间15秒提示一次
                                    delayMillisRefresh = 15 * 1000;
                                }
                            }
                        } else {
                            hangup();
                            CommonUtil.toast(R.string.ActivityCall_coins_error);
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.i(TAG, "刷新失败: " + msg + " url=" + this.getRequestUrl());
                        CommonUtil.toast(getString(R.string.network_error));
                    }
                });
                sendEmptyMessageDelayed(WHAT_REFRESH, delayMillisRefresh);
            }

            //
            else if (msg.what == WHAT_HANG_UP) {
                if (TextUtils.isEmpty(callId)) {
                    hangup();
                }
            } else if (msg.what == WHAT_PEER_BUSY) {
                finish();
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
    private int currentImageIndex = -1;//图片切换当前所在的索引
    private View ll_lyric;

    private ViewGroup ll_control;
    private Dialog dialogZoom;
    private boolean isAccept = false;
    private AlertDialog dialogCoins;
    private ProgressBar pb_loading;
    private Theme currentTheme;
    private ViewPager viewPagerImageMessage;
    private View rl_hold;
    private View rl_talk;
    private List<View> ctrls;
    private View iv_horn;
    private View iv_hang;
    private View iv_mute;
    private TextView tv_theme_center;
    private View iv_prev;
    private View iv_next;
    private TextView tv_status;
    private AlertDialog dialog_chat_image;

    //endregion

    /**
     * @param context  上下文
     * @param chatMode 通话模式,外呼还是来电
     * @param chatData 通话数据,包括昵称,头像,其中额外的数据在Extra里面
     */
    public static void start(Context context, int chatMode, ChatData chatData) {
        Intent intent = new Intent(context, ActivityChat.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CHAT_MODE, chatMode);
        intent.putExtra(KEY_CHAT_DATA, chatData);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_topic:
            case R.id.ll_lyric:
            case R.id.ll_hskk:
            case R.id.ll_image:
                //region 话题，课程，图片选项
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et_msg.getWindowToken(), 0);
                tabsSwitch(v.getId());
                //endregion
                break;
            case R.id.ll_text:
                //region 文字选项
                tabsSwitch(v.getId());
                //endregion
                break;
            case R.id.bt_pick_theme:
                //region主题发送按钮
                ActivityTheme.start(ActivityChat.this, gson.toJson(currentTheme));
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
                if (dialog_chat_image == null) {
                    Builder builder = new Builder(this);
                    View inflate = getLayoutInflater().inflate(R.layout.dialog_chat_image, null);
                    inflate.findViewById(R.id.ll_album).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(intent, REQUEST_CODE_IMAGE);
                            dialog_chat_image.dismiss();
                        }
                    });
                    inflate.findViewById(R.id.ll_camera).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File image = getExternalFilesDir("image");
                            if (image != null && image.exists()) {
                                fileImageCapture = new File(image, String.format("%1$s.jpg", UUID.randomUUID()));
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImageCapture));
                                startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
                                dialog_chat_image.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "SDCard异常，无法保存照片！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setView(inflate);
                    dialog_chat_image = builder.create();
                }
                dialog_chat_image.show();
                //endregion
                break;
            case R.id.bt_reject:
                //region拒绝接听
                hangup();
                //endregion
                break;
            case R.id.bt_accept:
                //region 同意接听
                if (!isAccept) {
                    isAccept = true;

                    rl_talk.setVisibility(View.VISIBLE);
                    rl_hold.setVisibility(View.INVISIBLE);

                    AVChatOptionalConfig config = new AVChatOptionalConfig();
                    config.enableCallProximity(false);
                    AVChatManager.getInstance().accept(null, callbackAccept);
                }
                //endregion
                break;
            case R.id.bt_hangup:
            case R.id.iv_hang:
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
            case R.id.iv_horn:
                //region是否静音
                if (IS_CALL_ESTABLISHED) {
                    AVChatManager.getInstance().muteLocalAudio(!AVChatManager.getInstance().isLocalAudioMuted());
                    iv_horn.setSelected(AVChatManager.getInstance().isLocalAudioMuted());
                }
                //endregion
                break;
            case R.id.iv_mute:
                //region 是否外放
                if (IS_CALL_ESTABLISHED) {
                    AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
                    iv_mute.setSelected(!AVChatManager.getInstance().speakerEnabled());
                }
                //endregion
                break;
            case R.id.iv_image:
                //region 点击图片放大
                if (listImageMessage.size() > 0) {
                    viewPagerImageMessage.setCurrentItem(currentImageIndex);
                    TextView tv_index = (TextView) dialogZoom.findViewById(R.id.tv_index);
                    tv_index.setText(String.format("%1$d/%2$d", currentImageIndex + 1, listImageMessage.size()));
                    dialogZoom.show();
                }
                //endregion
                break;
            case R.id.iv_prev:
                //region 图片显示控制
            {
                currentImageIndex--;
                IMMessage message = listImageMessage.get(currentImageIndex);
                ImageAttachment attachment = (ImageAttachment) message.getAttachment();
                String path = TextUtils.isEmpty(attachment.getPath()) ? attachment.getThumbPath() : attachment.getPath();
                CommonUtil.showBitmap(iv_image, path);
                iv_prev.setVisibility(currentImageIndex > 0 ? View.VISIBLE : View.INVISIBLE);
                iv_next.setVisibility(View.VISIBLE);
            }
            //endregion
            break;
            case R.id.iv_next:
                //region 图片显示控制
            {
                currentImageIndex++;
                IMMessage message = listImageMessage.get(currentImageIndex);
                ImageAttachment attachment = (ImageAttachment) message.getAttachment();
                String path = TextUtils.isEmpty(attachment.getPath()) ? attachment.getThumbPath() : attachment.getPath();
                CommonUtil.showBitmap(iv_image, path);
                iv_prev.setVisibility(View.VISIBLE);
                iv_next.setVisibility(currentImageIndex < listImageMessage.size() - 1 ? View.VISIBLE : View.INVISIBLE);
            }
            //endregion
            break;

        }
    }

    private void tabsSwitch(int identifyId) {
        int size = ctrls.size();
        for (int i = 0; i < size; i++) {
            View ctrl = ctrls.get(i);
            ctrl.setSelected(ctrl.getId() == identifyId);
            fl_content.getChildAt(i).setVisibility(ctrl.getId() == identifyId ? View.VISIBLE : View.INVISIBLE);
        }

        //隐藏图片缩放窗口
        if (identifyId != R.id.ll_image && dialogZoom != null) {
            dialogZoom.dismiss();
        }

        //隐藏HSKK图片缩放窗口
        if (identifyId != R.id.ll_hskk) {
            android.support.v4.app.Fragment fragmentChatHskk = getSupportFragmentManager().findFragmentByTag("FragmentChatHskk");
            if (fragmentChatHskk != null && fragmentChatHskk instanceof FragmentChatHskk) {
                ((FragmentChatHskk) fragmentChatHskk).dismissDialog();
            }
        }
    }

    private void hangup() {
        AVChatManager.getInstance().hangUp(callbackHangup);
        SoundPlayer.instance(ChineseChat.getContext()).stop();
    }

    private void showThemeQuestion(Theme theme) {
        ll_topic.setSelected(true);
        ll_topic.setVisibility(View.VISIBLE);
        rl_theme.setVisibility(View.VISIBLE);

        //主题名称
        tv_theme.setText(theme.Name);
        tv_theme.setVisibility(View.VISIBLE);
        //主题问题
        Parameters params = new Parameters();
        params.add("id", "" + theme.Id);
        HttpUtil.post(NetworkUtil.themeGetById, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<Theme> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<Theme>>() {}.getType());
                list.clear();
                adapter.notifyDataSetChanged();
                if (resp.code == 200) {
                    List<Question> questions = resp.info.Questions;
                    for (Question q : questions) {
                        list.add(q.Name);
                    }
                }
                adapter.notifyDataSetChanged();
                listview.smoothScrollToPosition(0);
                rl_theme.setVisibility(View.VISIBLE);
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
        Log.i(TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_chat);

        //  getActionBar().hide();

        initView();
        initData();

        registerObserver(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(broadcastReceiver, filter);
    }

    private void initView() {
        rl_hold = findViewById(R.id.rl_hold);
        rl_talk = findViewById(R.id.rl_talk);

        //未接通界面时，头像，昵称，按钮
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_nick = (TextView) findViewById(R.id.tv_nick);
        tv_status = (TextView) findViewById(R.id.tv_status);

        //控制按钮,布局
        ll_call = findViewById(R.id.ll_call);
        ll_hang = findViewById(R.id.ll_hang);
        ll_call.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.INVISIBLE);
        ll_hang.setVisibility(ChineseChat.isStudent() ? View.INVISIBLE : View.VISIBLE);

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

        //小头像,名称,学习情况
        findViewById(R.id.rl_profile).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et_msg.getWindowToken(), 0);
                return false;
            }
        });

        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_case = (TextView) findViewById(R.id.tv_case);

        fl_content = (FrameLayout) findViewById(R.id.fl_content);

        //主题,布局
        rl_theme = findViewById(R.id.rl_theme);
        tv_theme = (TextView) findViewById(R.id.tv_theme);
        listview = (ListView) findViewById(R.id.listview);
        tv_theme_center = (TextView) findViewById(R.id.tv_theme_center);
        Button bt_pick_theme = (Button) findViewById(R.id.bt_pick_theme);
        bt_pick_theme.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.INVISIBLE);
        bt_pick_theme.setOnClickListener(this);

        //文字
        lv_msg = (ListView) findViewById(R.id.lv_msg);
        et_msg = (EditText) findViewById(R.id.et_msg);
        View bt_msg = findViewById(R.id.bt_msg);
        bt_msg.setOnClickListener(this);

        lv_msg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et_msg.getWindowToken(), 0);
                return false;
            }
        });

        //图片
        rl_image = findViewById(R.id.rl_image);
        iv_image = (ImageView) findViewById(R.id.iv_image);
        iv_image.setOnClickListener(this);
        iv_prev = findViewById(R.id.iv_prev);
        iv_next = findViewById(R.id.iv_next);
        iv_prev.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_image_send = findViewById(R.id.iv_image_send);
        iv_image_send.setOnClickListener(this);

        //主题,白板,文字,图片 控制按钮
        ll_control = (ViewGroup) findViewById(R.id.ll_control);
        ll_topic = findViewById(R.id.ll_topic);
        ll_lyric = findViewById(R.id.ll_lyric);
        View ll_hskk = findViewById(R.id.ll_hskk);
        ll_image = findViewById(R.id.ll_image);
        ll_text = findViewById(R.id.ll_text);

        ll_topic.setOnClickListener(this);
        ll_lyric.setOnClickListener(this);
        ll_hskk.setOnClickListener(this);
        ll_text.setOnClickListener(this);
        ll_image.setOnClickListener(this);

        ctrls = new ArrayList<>();
        ctrls.add(ll_topic);
        ctrls.add(ll_lyric);
        ctrls.add(ll_hskk);
        ctrls.add(ll_image);
        ctrls.add(ll_text);

        iv_horn = findViewById(R.id.iv_horn);
        iv_hang = findViewById(R.id.iv_hang);
        iv_mute = findViewById(R.id.iv_mute);

        iv_horn.setOnClickListener(this);
        iv_hang.setOnClickListener(this);
        iv_mute.setOnClickListener(this);

        cm_time = (Chronometer) findViewById(R.id.cm_time);

        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
        pb_loading.setVisibility(View.INVISIBLE);

        list = new ArrayList<>();
        adapter = new AdapterThemes(list);
        listview.setAdapter(adapter);

        //region 初始化图片查看对话框
        dialogZoom = new Dialog(this, R.style.NoTitle_Fullscreen);
        dialogZoom.setContentView(R.layout.dialog_album);
        viewPagerImageMessage = (ViewPager) dialogZoom.findViewById(R.id.viewpager);
        viewPagerImageMessage.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return listImageMessage.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view.equals(object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ImageAttachment attachment = (ImageAttachment) listImageMessage.get(position).getAttachment();
                FrameLayout fl = new FrameLayout(getApplication());
                final ProgressBar pb = new ProgressBar(getApplication());
                pb.setVisibility(View.VISIBLE);

                final ImageView photoView = new ImageView(getApplicationContext());
                final PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(photoView);
                photoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float x, float y) {
                        dialogZoom.dismiss();
                        photoViewAttacher.setScale(1);
                    }
                });
                photoView.setImageResource(R.drawable.background);
                new BitmapUtils(getApplicationContext(), getCacheDir().getAbsolutePath()).display(photoView, attachment.getUrl(), new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
                        Log.i(TAG, "onLoadCompleted: ");
                        photoView.setImageBitmap(bitmap);
                        photoViewAttacher.update();
                        pb.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onLoadFailed(ImageView container, String uri, Drawable drawable) {
                        Log.i(TAG, "onLoadFailed: ");
                        photoViewAttacher.update();
                        pb.setVisibility(View.INVISIBLE);
                    }
                });
                fl.addView(photoView, new FrameLayout.LayoutParams(-1, -1));

                int v = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(v, v);
                params.gravity = Gravity.CENTER;
                fl.addView(pb, params);

                container.addView(fl);
                return fl;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
        viewPagerImageMessage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentImageIndex = position;
                ImageAttachment attachment = (ImageAttachment) listImageMessage.get(position).getAttachment();
                String path = TextUtils.isEmpty(attachment.getPath()) ? attachment.getThumbPath() : attachment.getPath();

                iv_prev.setVisibility(currentImageIndex > 0 ? View.VISIBLE : View.INVISIBLE);
                iv_next.setVisibility(currentImageIndex < listImageMessage.size() - 1 ? View.VISIBLE : View.INVISIBLE);

                CommonUtil.showBitmap(iv_image, path);
                TextView tv_index = (TextView) dialogZoom.findViewById(R.id.tv_index);
                tv_index.setText(String.format("%1$d/%2$d", position + 1, listImageMessage.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //endregion
    }

    private void initData() {
        Intent intent = getIntent();
        chatMode = intent.getIntExtra(KEY_CHAT_MODE, -1);
        chatData = (ChatData) intent.getSerializableExtra(KEY_CHAT_DATA);
        chatDataExtra = gson.fromJson(chatData.getExtra(), ChatDataExtra.class);
        if (chatDataExtra == null) {
            chatDataExtra = new ChatDataExtra();
        }


        Log.i(TAG, "initData: " + chatData);

        //region 去电
        if (chatMode == CHAT_MODE_OUTGOING) {
            SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.CONNECTING);

            //界面
            ll_call.setVisibility(View.VISIBLE);
            ll_hang.setVisibility(View.INVISIBLE);
            rl_hold.setVisibility(View.VISIBLE);
            rl_talk.setVisibility(View.INVISIBLE);

            //隐藏没有必要的界面
            tv_case.setVisibility(View.GONE);

            tv_name.setText(getString(R.string.ActivityTake_show_teacher_nickname, chatDataExtra.Teacher.Nickname));
            tv_nick.setText(chatDataExtra.Teacher.Nickname);
            tv_case.setText(getString(R.string.ActivityTake_show_teacher_summary, chatDataExtra.Teacher.Summary.duration, chatDataExtra.Teacher.Summary.count, chatDataExtra.Teacher.Summary.month));
            tv_status.setText("Waiting to response");
            CommonUtil.showIcon(getApplicationContext(), iv_icon, chatDataExtra.Teacher.Avatar);
            CommonUtil.showIcon(getApplicationContext(), iv_avatar, chatDataExtra.Teacher.Avatar);

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

            AVChatOptionalConfig params = new AVChatOptionalConfig();
            params.enableCallProximity(false);

            AVChatManager.getInstance().call(chatData.getAccount(), AVChatType.AUDIO, params, option, callback_call);

            getSupportFragmentManager().beginTransaction().replace(R.id.fl_hskk, FragmentChatHskk.newInstance(FragmentChatHskk.OPEN_MODE_PICK, 0), "FragmentChatHskk").commit();
        }
        //endregion
        //region 来电
        else if (chatMode == CHAT_MODE_INCOMING) {
            SoundPlayer.instance(ChineseChat.getContext()).play(SoundPlayer.RingerTypeEnum.RING);

            //界面
            ll_hang.setVisibility(View.VISIBLE);
            ll_call.setVisibility(View.INVISIBLE);
            rl_hold.setVisibility(View.VISIBLE);
            rl_talk.setVisibility(View.INVISIBLE);

            //隐藏没有必要的界面

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

                            tv_nick.setText(TextUtils.isEmpty(user.Nickname) ? user.Username : user.Nickname);
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
                tv_nick.setText(TextUtils.isEmpty(chatDataExtra.Student.Nickname) ? chatDataExtra.Student.Username : chatDataExtra.Student.Nickname);
                tv_name.setText(getString(R.string.ActivityTake_show_student_nickname, chatDataExtra.Student.Nickname, TextUtils.isEmpty(chatDataExtra.Student.Country) ? "未知" : chatDataExtra.Student.Country));
                tv_case.setText(getString(R.string.ActivityTake_show_student_summary, chatDataExtra.Student.Summary.duration, chatDataExtra.Student.Summary.count));

                CommonUtil.showBitmap(iv_icon, NetworkUtil.getFullPath(chatDataExtra.Student.Avatar));
                CommonUtil.showBitmap(iv_avatar, NetworkUtil.getFullPath(chatDataExtra.Student.Avatar));
            }
        }
        //endregion
        //region 异常
        else {
            CommonUtil.toast("通话异常");
            finish();
        }
        //endregion

        //region处理文档情况
        //chatDataExtra.DocumentId = 1180;
        if (chatDataExtra.DocumentId > 0) {
            tabsSwitch(R.id.tv_lyric);
            getSupportFragmentManager().beginTransaction().replace(R.id.rl_lyric, FragmentCourse.newInstance(FragmentCourse.OPEN_MODE_SHOW, chatDataExtra.DocumentId), "FragmentCourse").commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.rl_lyric, FragmentCourse.newInstance(ChineseChat.isStudent() ? FragmentCourse.OPEN_MODE_ROOT : FragmentCourse.OPEN_MODE_NONE, chatDataExtra.DocumentId), "FragmentCourse").commit();
        }
        //endregion

        //region处理hskk情况

        //endregion

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
            //region 选择主题回调
            if (resultCode == FragmentThemes.RESULTCODE_CHOOSE) {
                currentTheme = gson.fromJson(data.getStringExtra("theme"), new TypeToken<Theme>() {}.getType());

                // 构造自定义通知，指定接收者,并发送自定义通知
                NimSysNotice<Theme> notice = new NimSysNotice<>();
                notice.type = NimSysNotice.NoticeType_Card;
                notice.info = currentTheme;

                CustomNotification notification = new CustomNotification();
                notification.setSessionId(chatData.getAccount());
                notification.setSessionType(SessionTypeEnum.P2P);
                notification.setContent(gson.toJson(notice));
                NIMClient.getService(MsgService.class).sendCustomNotification(notification);

                tv_theme_center.setVisibility(View.VISIBLE);
                tv_theme_center.setText(currentTheme.Name);
            } else {
                CommonUtil.toast(R.string.ActivityCall_topic_choose_failed);
            }
            //endregion
        } else if (requestCode == REQUEST_CODE_IMAGE) {
            //region选择图片,发送图片
            if (resultCode == Activity.RESULT_OK) {
                File photoPick = new File(FileUtil.getPath(this, data.getData()));
                File newFile = new File(getExternalFilesDir("image/thumb"), photoPick.getName());
                FileOutputStream out = null;

                //图片压缩,如果选择的图片大于1M，压缩图片 1024 * 1024=1048576
                if (photoPick.exists() && photoPick.length() > 512000) {
                    try {
                        pb_loading.setVisibility(View.VISIBLE);
                        Bitmap bitmap = BitmapFactory.decodeFile(photoPick.getAbsolutePath());

                        int quality = 90;
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        long length = photoPick.length();

                        //压缩图片到内存中保存
                        while (length > 512000) {
                            stream.reset();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality > 10 ? quality : 10, stream);
                            length = stream.size();
                            quality -= 10;
                        }
                        bitmap.recycle();

                        //把内存中的图片保存到文件系统
                        out = new FileOutputStream(newFile);
                        stream.writeTo(out);
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //记录图片显示界面当前显示的图上的路径
                currentImagePath = photoPick.getAbsolutePath();

                /**创建图片消息
                 *@param accid 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                 *@param ChatType 聊天类型，单聊或群组
                 *@param file 图片文件对象
                 *@param display 文件显示名字，如果第三方 APP 不关注，可以为 null
                 */
                IMMessage message = MessageBuilder.createImageMessage(chatData.getAccount(), SessionTypeEnum.P2P, out == null ? photoPick : newFile, null);
                NIMClient.getService(MsgService.class).sendMessage(message, false);
                CommonUtil.showBitmap(iv_image, photoPick.getAbsolutePath());

                //删除临时压缩的照片，因为消息发送是一个异步动作，但是不知道什么时候消息才发送完，所以不删除临时文件

                //把图片添加到可放大图片列表
                listImageMessage.add(message);
                viewPagerImageMessage.getAdapter().notifyDataSetChanged();
                currentImageIndex = listImageMessage.size() - 1;
                iv_prev.setVisibility(currentImageIndex > 0 ? View.VISIBLE : View.INVISIBLE);
                iv_next.setVisibility(View.INVISIBLE);
            }
            //endregion
        } else if (requestCode == REQUEST_CODE_IMAGE_CAPTURE) {
            //region 从摄像头获取照片
            if (resultCode == Activity.RESULT_OK) {
                File photoPick = fileImageCapture;
                File newFile = new File(getExternalFilesDir("image/thumb"), photoPick.getName());
                FileOutputStream out = null;

                //图片压缩,如果选择的图片大于1M，压缩图片 1024 * 1024=1048576
                if (photoPick.exists() && photoPick.length() > 512000) {
                    try {
                        pb_loading.setVisibility(View.VISIBLE);
                        Bitmap bitmap = BitmapFactory.decodeFile(photoPick.getAbsolutePath());

                        int quality = 90;
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        long length = photoPick.length();

                        //压缩图片到内存中保存
                        while (length > 512000) {
                            stream.reset();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality > 10 ? quality : 10, stream);
                            length = stream.size();
                            quality -= 10;
                        }
                        bitmap.recycle();

                        //把内存中的图片保存到文件系统
                        out = new FileOutputStream(newFile);
                        stream.writeTo(out);
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Log.i(TAG, "onActivityResult:2" + out + " ," + photoPick.length() + " ," + newFile.length());

                //记录图片显示界面当前显示的图上的路径
                currentImagePath = photoPick.getAbsolutePath();

                /**创建图片消息
                 *@param accid 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                 *@param ChatType 聊天类型，单聊或群组
                 *@param file 图片文件对象
                 *@param display 文件显示名字，如果第三方 APP 不关注，可以为 null
                 */
                IMMessage message = MessageBuilder.createImageMessage(chatData.getAccount(), SessionTypeEnum.P2P, out == null ? photoPick : newFile, null);
                NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onFailed(int i) {
                        Log.i(TAG, "onFailed: " + i);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        Log.i(TAG, "onException: " + throwable.getMessage());
                    }
                });
                CommonUtil.showBitmap(iv_image, photoPick.getAbsolutePath());

                Log.i(TAG, "onActivityResult: 3");
                //删除临时压缩的照片，因为消息发送是一个异步动作，但是不知道什么时候消息才发送完，所以不删除临时文件

                //把图片添加到可放大图片列表
                listImageMessage.add(message);
                viewPagerImageMessage.getAdapter().notifyDataSetChanged();
                currentImageIndex = listImageMessage.size() - 1;
                iv_prev.setVisibility(currentImageIndex > 0 ? View.VISIBLE : View.INVISIBLE);
                iv_next.setVisibility(View.INVISIBLE);
            }
            //endregion
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

            //失败 600 点对点音视频有第三方加入

            if (11001 == arg0) {
                //9102	通道失效
                //9103	已经在他端对这个呼叫响应过了
                //11001	通话不可达，对方离线状态
                CommonUtil.toast(R.string.ActivityCall_call_failed);
            } else if (408 == arg0) {
                //408	客户端请求超时
                CommonUtil.toast(R.string.ActivityCall_call_failed);
            } else if (9103 == arg0) {
                Log.i(TAG, "onFailed: " + 9103);
                return;
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
            tv_status.setText("Connecting...");
        }

        @Override
        public void onFailed(int i) {
            Log.i(TAG, "回应失败: " + i);
            SoundPlayer.instance(ChineseChat.getContext()).stop();
            if (9103 == i) {
                Log.i(TAG, "onFailed: " + i);
                return;
            }
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
        public void onConnectionTypeChanged(int netType) {

        }


        @Override
        public void onLocalRecordEnd(String[] strings, int i) {
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
            //2016-07-21 当自己意外退出频道时,应该退出界面
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

            //当通话建立时,切换待接通接通界面和接通接通界面,切换大小头像,并显示对方资料
            rl_talk.setVisibility(View.VISIBLE);
            rl_hold.setVisibility(View.INVISIBLE);

            if (chatMode == CHAT_MODE_OUTGOING) {
                getSupportFragmentManager().beginTransaction().replace(R.id.rl_theme, FragmentTopics.newInstance(chatData.getAccount()), "FragmentTopics").commit();
            }
        }

        @Override
        public void onDeviceEvent(int code, String desc) {
            Log.i(TAG, "设备事件: " + desc);
            //当设备准备好的时候就默认打开外放
            AVChatManager.getInstance().setSpeaker(true);
            bt_free.setSelected(!AVChatManager.getInstance().speakerEnabled());
        }

        @Override
        public void onFirstVideoFrameRendered(String user) {

        }

        @Override
        public void onVideoFrameResolutionChanged(String user, int width, int height, int rotate) {

        }

        @Override
        public int onVideoFrameFilter(AVChatVideoFrame frame) {
            return 0;
        }

        @Override
        public int onAudioFrameFilter(AVChatAudioFrame frame) {
            return 0;
        }

        @Override
        public void onAudioOutputDeviceChanged(int device) {

        }

        @Override
        public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {

        }

        @Override
        public void onStartLiveResult(int code) {

        }

        @Override
        public void onStopLiveResult(int code) {

        }
    };
    //endregion

    //region回应监听
    private Observer<AVChatCalleeAckEvent> observerCalleeAck = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent event) {
            Log.i(TAG, "对方回应监听: ChatId=" + event.getChatId() + " Event=" + event.getEvent() + " account=" + event.getAccount() + " extra=" + event.getExtra());

            tv_status.setText("Connecting...");

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
                    handler.sendEmptyMessageDelayed(WHAT_HANG_UP, 60 * 1000);

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
                //Log.i(TAG, "基础消息监听: uuid=" + m.getUuid() + ",type=" + m.getMsgType() + ",status=" + m.getStatus() + ",AttachStatus=" + m.getAttachStatus());
                MsgTypeEnum msgType = m.getMsgType();

                //文本消息
                if (msgType == MsgTypeEnum.text) {
                    tabsSwitch(R.id.ll_text);//切换界面
                    MessageText messageText = new MessageText();
                    messageText.FromNickname = chatMode == CHAT_MODE_INCOMING ? chatDataExtra.Student.Nickname : chatDataExtra.Teacher.Nickname;
                    messageText.Content = m.getContent();
                    listMessage.add(messageText);

                    adapterMessage.notifyDataSetChanged();
                    lv_msg.smoothScrollToPosition(listMessage.size() - 1);

                }
                //图片消息，只做图片界面的切换，不做图片的加载，因为原图及缩略图有可能都还没有下载
                else if (m.getMsgType() == MsgTypeEnum.image) {
                    tabsSwitch(R.id.ll_image);//切换界面
                    ImageAttachment attachment = (ImageAttachment) m.getAttachment();
                    Log.i(TAG, "图片消息: uuid=" + m.getUuid() + ",ThumbPath=" + attachment.getThumbPath() + ",path=" + attachment.getPath() + ",url=" + attachment.getUrl());
                    listImageMessage.add(m);

                    currentImageIndex = listImageMessage.size() - 1;

                    iv_prev.setVisibility(currentImageIndex > 0 ? View.VISIBLE : View.INVISIBLE);
                    iv_next.setVisibility(View.INVISIBLE);

                    viewPagerImageMessage.getAdapter().notifyDataSetChanged();
                    viewPagerImageMessage.setCurrentItem(listImageMessage.size() - 1);//当收到图片时，如果正在查看大图，也刷新大图显示
                    TextView tv_index = (TextView) dialogZoom.findViewById(R.id.tv_index);//更新图片查看器索引
                    tv_index.setText(String.format("%1$d/%2$d", viewPagerImageMessage.getCurrentItem() + 1, listImageMessage.size()));
                }
            }
        }
    };
    //endregion

    //region基础消息下载监听
    private Observer<IMMessage> observerBaseMessageStatus = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            //Log.i(TAG, "状态监听: uuid=" + msg.getUuid() + ",type=" + msg.getMsgType() + ",status=" + msg.getStatus() + "，AttachStatus=" + msg.getAttachStatus());


            if (msg.getMsgType() == MsgTypeEnum.image) {
                ImageAttachment attachment = (ImageAttachment) msg.getAttachment();
                Log.i(TAG, "图片下载: uuid=" + msg.getUuid() + ",url=" + attachment.getUrl() + ",path=" + attachment.getPath() + ",thumbpath=" + attachment.getThumbPath());

                pb_loading.setVisibility(msg.getAttachStatus() == AttachStatusEnum.transferred ? View.INVISIBLE : View.VISIBLE);

                if (msg.getDirect() == MsgDirectionEnum.In) {
                    String s = TextUtils.isEmpty(attachment.getPath()) ? attachment.getThumbPath() : attachment.getPath();
                    CommonUtil.showBitmap(iv_image, s);
                }
            }
        }
    };
    //endregion

    //region附件进度监听,附件的上传与下载
    private Observer<AttachmentProgress> observerAttachmentProgress = new Observer<AttachmentProgress>() {
        @Override
        public void onEvent(AttachmentProgress attachmentProgress) {
            Log.i(TAG, "进度观察: uuid=" + attachmentProgress.getUuid() + " " + attachmentProgress.getTransferred() + "/" + attachmentProgress.getTotal());
            pb_loading.setVisibility(View.VISIBLE);
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
                    //CommonUtil.toast("对方选择了话题:" + theme.Name);

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
                    tabsSwitch(R.id.ll_topic);
                    showThemeQuestion(theme);
                }
                break;
                case NimSysNotice.NoticeType_Chat:
                    CALL_ID_RECEIVE = true;
                    callId = info;
                    break;
                case NimSysNotice.NOTICE_TYPE_TOPIC:
                    //显示学生选择的话题
                    getSupportFragmentManager().beginTransaction().replace(R.id.rl_theme, FragmentTopicsShow.newInstance(Integer.parseInt(info)), "FragmentTopicsShow").commit();
                    //保存学生选择的话题
                    Parameters params = new Parameters();
                    params.add("chatId", chatData.getChatId());
                    params.add("themeId", info);
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
                    //界面调整
                    tabsSwitch(R.id.ll_topic);
                    break;
                case NimSysNotice.NOTICE_TYPE_COURSE:
                    getSupportFragmentManager().findFragmentByTag("FragmentCourse").getChildFragmentManager().beginTransaction().replace(R.id.fl_content, FragmentCourseShow.newInstance(Integer.parseInt(info), false)).addToBackStack("FragmentCourseClear").commit();
                    tabsSwitch(R.id.ll_lyric);
                    break;
                case NimSysNotice.NoticeType_Hskk:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_hskk, FragmentChatHskk.newInstance(FragmentChatHskk.OPEN_MODE_SHOW, Integer.parseInt(info)), "FragmentChatHskk").commit();
                    tabsSwitch(R.id.ll_hskk);
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

    private class AdapterThemes extends BaseAdapter<String> {

        public AdapterThemes(List<String> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String item = getItem(position);

            View inflate = View.inflate(getApplication(), R.layout.listitem_topic, null);
            TextView textview = (TextView) inflate.findViewById(R.id.textview);
            textview.setText(item);
            return inflate;
        }
    }

    private class AdapterCourse extends BaseAdapter<Lyric> {
        public AdapterCourse(List<Lyric> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Lyric item = getItem(position);
            View inflate = View.inflate(getApplication(), R.layout.listitem_lyric, null);
            TextView viewById = (TextView) inflate.findViewById(R.id.tv_Original);
            TextView viewById1 = (TextView) inflate.findViewById(R.id.tv_Translate);

            viewById.setText(item.Original);
            viewById1.setText(item.Translate);
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
