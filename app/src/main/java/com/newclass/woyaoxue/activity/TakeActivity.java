package com.newclass.woyaoxue.activity;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.NimSysNotice;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.Theme;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * 电话接听界面
 *
 * @author liaorubei
 */
public class TakeActivity extends Activity implements OnClickListener {

    public static String KEY_CHATDATA = "KEY_CHATDATA";
    protected static final String TAG = "TakeActivity";

    private Button bt_hangup, bt_accept, bt_finish, bt_mute, bt_free, bt_face, bt_text, bt_card, bt_more;
    private LinearLayout ll_others;
    private Chronometer cm_time;
    private Theme currentTheme = null;
    private Gson gson = new Gson();
    private ImageView iv_icon;
    private AlertDialog ratingDialog;
    private boolean isAccept = false;
    private boolean IS_CHATTING = false;

    private AVChatCallback<Void> callbackAccept = new AVChatCallback<Void>() {

        @Override
        public void onException(Throwable arg0) {

        }

        @Override
        public void onFailed(int arg0) {

        }

        @Override
        public void onSuccess(Void arg0) {
            cm_time.setBase(SystemClock.elapsedRealtime());
            cm_time.start();
            isAccept = true;
            IS_CHATTING = true;
            bt_finish.setVisibility(View.VISIBLE);
            ll_others.setVisibility(View.INVISIBLE);
        }
    };

    private AVChatCallback<Void> callbackHangup = new AVChatCallback<Void>() {

        @Override
        public void onException(Throwable arg0) {

        }

        @Override
        public void onFailed(int arg0) {

        }

        @Override
        public void onSuccess(Void arg0) {
            if (ratingDialog == null) {
                createRatingDialog();
            }
            if (isAccept) {
                ratingDialog.show();

                return;
            }

            finish();
        }
    };


    private Observer<AVChatCommonEvent> observerHangup = new Observer<AVChatCommonEvent>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void onEvent(AVChatCommonEvent event) {
            Log.i("logi", "对方已挂断 ChatId:" + event.getChatId());

            cm_time.stop();


            if (ratingDialog == null) {
                createRatingDialog();
            }
            if (isAccept && IS_CHATTING) {
                //如果接听了之后,才会有结束
                Parameters parameters = new Parameters();
                parameters.add("chatId", event.getChatId() + "");
                HttpUtil.post(NetworkUtil.callFinish, parameters, null);

                //只有接听了之后,才会有打分
                ratingDialog.show();
            } else {
                finish();
            }
        }
    };

    private Observer<AVChatTimeOutEvent> observerTimeout = new Observer<AVChatTimeOutEvent>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void onEvent(AVChatTimeOutEvent timeOutEvent) {
            CommonUtil.toast("超时");
            finish();
        }
    };

    // 自定义系统通知的广播接收者
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 从 intent 中取出自定义通知， intent 中只包含了一个 CustomNotification 对象
            CustomNotification notification = (CustomNotification) intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);

            Log.i(TAG, "Content:" + notification.getContent());

            NimSysNotice<Theme> notice = gson.fromJson(notification.getContent(), new TypeToken<NimSysNotice<Theme>>() {
            }.getType());

            if (notice.NoticeType == NimSysNotice.NoticeType_Card) {
                CommonUtil.toast("对方点击了:" + notice.info.Name);
            }
            currentTheme = notice.info;
            // showThemeQuestion();
        }
    };

    private User target;
    private TextView tv_nickname;
    private AVChatData avChatData;

    public static void start(Context context, AVChatData avChatData) {
        Intent intent = new Intent(context, TakeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CHATDATA, avChatData);
        context.startActivity(intent);
    }

    private void initData() {
        Intent intent = getIntent();
        avChatData = (AVChatData) intent.getSerializableExtra(KEY_CHATDATA);
        target = new User();
        target.Accid = avChatData.getAccount();

        Parameters parameters = new Parameters();
        parameters.add("accid", target.Accid);
        HttpUtil.post(NetworkUtil.userGetByAccId, parameters, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException error, String msg) {
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());
                if (resp.code == 200) {
                    target.Id = resp.info.Id;
                    target.NickName = resp.info.NickName;
                    tv_nickname.setText(target.NickName);
                }
            }
        });

        registerObserver(true);
    }

    protected void createRatingDialog() {
        Builder builder = new AlertDialog.Builder(TakeActivity.this);
        View inflate = View.inflate(TakeActivity.this, R.layout.dialog_rating, null);
        View bt_positive = inflate.findViewById(R.id.bt_positive);
        final RatingBar rb_score = (RatingBar) inflate.findViewById(R.id.rb_score);

        bt_positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Parameters parameters = new Parameters();
                parameters.add("chatId", avChatData.getChatId() + "");
                parameters.add("score", (int) rb_score.getRating() + "");
                HttpUtil.post(NetworkUtil.calllogRating, parameters, new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        CommonUtil.toast("评分成功");
                        finish();
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast("评分失败");
                        finish();
                    }
                });
                ratingDialog.dismiss();
            }
        });

        builder.setView(inflate);
        builder.setCancelable(false);
        ratingDialog = builder.create();
    }

    private void initView() {
        bt_hangup = (Button) findViewById(R.id.bt_hangup);
        bt_accept = (Button) findViewById(R.id.bt_accept);
        bt_finish = (Button) findViewById(R.id.bt_finish);
        bt_mute = (Button) findViewById(R.id.bt_mute);
        bt_free = (Button) findViewById(R.id.bt_free);
        bt_face = (Button) findViewById(R.id.bt_face);
        bt_text = (Button) findViewById(R.id.bt_text);
        bt_card = (Button) findViewById(R.id.bt_card);
        bt_more = (Button) findViewById(R.id.bt_more);

        ll_others = (LinearLayout) findViewById(R.id.ll_others);

        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        cm_time = (Chronometer) findViewById(R.id.cm_time);
        cm_time.stop();

        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        iv_icon.setOnClickListener(this);

        bt_hangup.setOnClickListener(this);
        bt_accept.setOnClickListener(this);
        bt_finish.setOnClickListener(this);
        bt_mute.setOnClickListener(this);
        bt_free.setOnClickListener(this);
        bt_face.setOnClickListener(this);
        bt_text.setOnClickListener(this);
        bt_card.setOnClickListener(this);
        bt_more.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_hangup:
                AVChatManager.getInstance().hangUp(callbackHangup);
                break;

            case R.id.bt_accept:
                AVChatManager.getInstance().accept(null, callbackAccept);
                break;
            case R.id.bt_finish:
                AVChatManager.getInstance().hangUp(callbackHangup);
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
                if (ratingDialog == null) {
                    createRatingDialog();
                }
                ratingDialog.show();
                break;

            case R.id.iv_icon:
                break;

            case R.id.bt_face: {
                HistoryActivity.start(TakeActivity.this, avChatData.getAccount());
            }
            break;

            case R.id.bt_text: {
                showThemeQuestion();
            }
            break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take);

        initView();
        initData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(this.getPackageName() + NimIntent.ACTION_RECEIVE_CUSTOM_NOTIFICATION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        registerObserver(false);
    }

    private void registerObserver(boolean register) {

        // 监听网络通话被叫方的响应（接听、拒绝、忙）
        // AVChatManager.getInstance().observeCalleeAckNotification(observerCallack, register);

        // 监听网络通话对方挂断的通知,即在正常通话时,结束通话
        AVChatManager.getInstance().observeHangUpNotification(observerHangup, register);

        // 监听呼叫或接听超时通知
        // 主叫方在拨打网络通话时，超过 45 秒被叫方还未接听来电，则自动挂断。
        // 被叫方超过 45 秒未接听来听，也会自动挂断
        // 在通话过程中网络超时 30 秒自动挂断。
        AVChatManager.getInstance().observeTimeoutNotification(observerTimeout, register);
    }

    private void showThemeQuestion() {
        if (currentTheme == null) {
            CommonUtil.toast("对方还没有选择学习主题");
            return;
        }
        Intent intent = new Intent(getApplication(), QuestionActivity.class);
        intent.putExtra("themeId", currentTheme.Id);
        startActivity(intent);
    }


    private class MyAdapter extends BaseAdapter<String> {

        public MyAdapter(List<String> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String item = getItem(position);
            View inflate = View.inflate(getApplication(), R.layout.griditem_card, null);
            inflate.findViewById(R.id.iv_card).setVisibility(View.VISIBLE);
            TextView tv_theme = (TextView) inflate.findViewById(R.id.tv_theme);
            tv_theme.setText("主题:" + item);

            return inflate;
        }
    }
}
