package com.hanwen.chinesechat.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.DownloadInfo;
import com.hanwen.chinesechat.bean.Lyric;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.SpecialLyricView;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityPlay extends Activity implements OnClickListener, OnPreparedListener, OnErrorListener, OnInfoListener, OnCompletionListener {
    private static final String TAG = "ActivityPlay";
    private static final int REQUEST_CODE_RECORD_AUDIO = 1;
    private ArrayList<Integer> subTitleIcons;
    private boolean isOneLineLoop = false; // 是否单句循环
    private boolean playSingleLineState;
    private DisplayMetrics outMetrics = new DisplayMetrics();
    private File recordFile;// 录音文件对象
    private FrameLayout.LayoutParams playParams, recordParams;// 控制按钮布局的布局参数
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private ImageView iv_cover;
    private ImageView iv_line, iv_prev, iv_play, iv_next, iv_tape; //播放栏按钮
    private ImageView iv_menu;
    private ImageView iv_rec_pause, iv_rec_origin, iv_rec_prev, iv_rec_button, iv_rec_record, iv_rec_next, iv_rec_back; //录音栏按钮
    private int documentId;
    private int elapsedTime = 0;// 录音/播放已经耗费的时间,毫秒数milliseconds
    private int sideA = 0, sideB = 0;
    private int titck = 0;
    private Integer subTitleState = 0;
    private LinearLayout ll_lyrics, ll_play, ll_tape;
    private List<SpecialLyricView> specialLyricViews = new ArrayList<SpecialLyricView>();
    private MediaPlayer playerOrigin, playerRecord;// 原音,录音播放对象
    private MediaRecorder recorder;// 音频录音对象
    private MediaState currentState = MediaState.播放原音;
    private RelativeLayout rl_buffering;
    private ScrollView sv_lyrics;
    private SeekBar seekBar;
    private TextView tv_bSide, tv_aSide, tv_title, tv_title_en;
    private TextView tv_main;
    private TextView tv_play_record_time;
    private ValueAnimator toRAnimator, toLAnimator;// 控制按钮布局的向右向左属性动画
    private View iv_home; //状态栏按钮
    protected static final int REFRESH_SEEKBAR = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        subTitleIcons = new ArrayList<Integer>();
        subTitleIcons.add(R.drawable.switch_news);
        subTitleIcons.add(R.drawable.switch_news);
        subTitleIcons.add(R.drawable.switch_news);

        initView();

        tv_aSide.setText("");
        tv_bSide.setText("");
        tv_title.setText("");

        initData();

        recorder = new MediaRecorder();
        recordFile = new File(getFilesDir(), "recode.tmp");
        initMediaRecorder(recordFile.getAbsolutePath());

        // 取得屏幕尺寸
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        playParams = (FrameLayout.LayoutParams) ll_play.getLayoutParams();
        playParams.width = outMetrics.widthPixels;

        recordParams = (FrameLayout.LayoutParams) ll_tape.getLayoutParams();
        recordParams.width = outMetrics.widthPixels;
        recordParams.leftMargin = outMetrics.widthPixels;

        toRAnimator = ValueAnimator.ofInt(0, outMetrics.widthPixels);
        toRAnimator.setDuration(100).addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                playParams.leftMargin = value - outMetrics.widthPixels;
                recordParams.leftMargin = value;
            }
        });

        toLAnimator = ValueAnimator.ofInt(0, outMetrics.widthPixels);
        toLAnimator.setDuration(100).addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                playParams.leftMargin = -value;
                recordParams.leftMargin = outMetrics.widthPixels - value;
            }
        });

        //统计播放次数
        RequestBody body = new FormBody.Builder().add("Id", documentId + "").add("userId", ChineseChat.CurrentUser.Id + "").build();
        Request request = new Request.Builder().url(NetworkUtil.documentCount).post(body).build();
        new OkHttpClient.Builder().build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: 播放统计成功：" + response.toString());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerOrigin != null && playerOrigin.isPlaying()) {
            playerOrigin.pause();
            iv_play.setSelected(false);
        }
        if (playerRecord != null && playerRecord.isPlaying()) {
            playerRecord.pause();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (playerOrigin != null) {
            playerOrigin.release();
            playerOrigin = null;
        }

        if (playerRecord != null) {
            playerRecord.release();
            playerRecord = null;
        }

        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "onCompletion: 播放完毕");
        if (currentState == MediaState.播放录音) {
            //时间归零
            elapsedTime = 0;
            seekToCurrentLine();
            playerOrigin.start();
            currentState = MediaState.播放原音;
        }
    }

    private enum MediaState {
        正在录音, 播放录音, 播放原音;

        @Override
        public String toString() {
            switch (this) {
                case 正在录音:
                    return ChineseChat.getContext().getString(R.string.ActivityPlay_Recording);
                case 播放录音:
                    return ChineseChat.getContext().getString(R.string.ActivityPlay_play_record);
                case 播放原音:
                    return ChineseChat.getContext().getString(R.string.ActivityPlay_play_origin);
            }
            return "";
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_SEEKBAR:

                    if (!iv_rec_pause.isSelected()) {
                        elapsedTime += 50;
                    }

                    refresh_seekbar();
                    sendEmptyMessageDelayed(REFRESH_SEEKBAR, 50);
                    break;
            }
        }
    };

    private void fillData(Document document) {
        tv_aSide.setText("00:00");
        tv_bSide.setText(document.LengthString);
        tv_title.setText(document.Title);
        tv_title_en.setText(document.TitleEn);
        tv_title_en.setVisibility(TextUtils.isEmpty(document.TitleEn) ? View.GONE : View.VISIBLE);

        specialLyricViews.clear();
        for (Lyric lyric : document.Lyrics) {
            SpecialLyricView specialLyricView = new SpecialLyricView(ActivityPlay.this, lyric);
            specialLyricViews.add(specialLyricView);
        }

        Collections.sort(specialLyricViews);

        for (SpecialLyricView specialLyricView : specialLyricViews) {
            // 在刚开始的时候,显示中文字幕的
            specialLyricView.showEnCn(SpecialLyricView.SHOW_ENCN);
            ll_lyrics.addView(specialLyricView);
        }

        // 因为使用的是相对路径,但是在实际请求时要加上域名
        initOriginPlayer(document.SoundPath);
    }

    private int getCurrentIndex() {
        if (specialLyricViews != null && playerOrigin != null && playerOrigin.getDuration() > 0) {
            int current = playerOrigin.getCurrentPosition();
            for (int i = 0; i < specialLyricViews.size(); i++) {
                Integer timeA = specialLyricViews.get(i).getTimeLabel();
                Integer timeB = i == (specialLyricViews.size() - 1) ? playerOrigin.getDuration() : specialLyricViews.get(i + 1).getTimeLabel();
                // 含头不含尾,因为当暂停之后再seekto时,current会等于sideA,所以要含头不含尾
                if (timeA <= current && current < timeB) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void initData() {
        Intent intent = getIntent();
        documentId = intent.getIntExtra("Id", 429);
        String mode = intent.getStringExtra("mode");
        tv_main.setText(mode + " " + tv_main.getText());

        //根据mode来判断,如果Mode=Online,则只能访问网络上的资源,只有Mode=Offline时,才会使用本地资源
        if ("Offline".equals(mode)) {
            DownloadInfo info = ChineseChat.database().docsSelectById(documentId);
            if (info != null && info.IsDownload == 1 && !TextUtils.isEmpty(info.Json)) {
                Document document = gson.fromJson(info.Json, Document.class);
                fillData(document);
            }
        } else {
            HttpUtil.post(NetworkUtil.getDocById(documentId), null, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "onSuccess: " + responseInfo.result);
                    Document document = gson.fromJson(responseInfo.result, Document.class);
                    fillData(document);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "onFailure: " + msg);
                    CommonUtil.toast(R.string.network_error);
                }
            });
        }
    }

    private void initMediaRecorder(String path) {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(path);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initOriginPlayer(String path) {
        try {
            if (playerOrigin == null) {
                playerOrigin = new MediaPlayer();
                playerOrigin.setLooping(true);
                playerOrigin.setAudioStreamType(AudioManager.STREAM_MUSIC);
                playerOrigin.setOnPreparedListener(this);
                playerOrigin.setOnErrorListener(this);
                playerOrigin.setOnInfoListener(this);
            }

            // 如果本地音频文件存在,则直接使用本地路径,如果不存在才使用网络路径,因为有可能已经缓存或下载过了
            File file = new File(getFilesDir(), path);
            if (file.exists()) {
                playerOrigin.setDataSource(file.getAbsolutePath());
                playerOrigin.prepare();
            } else {
                playerOrigin.setDataSource(NetworkUtil.getFullPath(path));
                playerOrigin.prepareAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRecordPlayer(String path, boolean prepare) {
        try {
            playerRecord = new MediaPlayer();
            playerRecord.setAudioStreamType(AudioManager.STREAM_MUSIC);
            playerRecord.setOnCompletionListener(this);
            if (prepare) {
                playerRecord.setDataSource(path);
                playerRecord.prepare();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        //标题栏按钮
        iv_home = findViewById(R.id.iv_home);
        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        iv_home.setOnClickListener(this);
        iv_menu.setOnClickListener(this);
        tv_main = (TextView) findViewById(R.id.tv_main);
        View iv_call = findViewById(R.id.iv_call);
        iv_call.setOnClickListener(this);
        iv_call.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.INVISIBLE);


        // 播放栏按钮
        iv_line = (ImageView) findViewById(R.id.iv_line);
        iv_prev = (ImageView) findViewById(R.id.iv_prev);
        iv_play = (ImageView) findViewById(R.id.iv_play);
        iv_next = (ImageView) findViewById(R.id.iv_next);
        iv_tape = (ImageView) findViewById(R.id.iv_tape);
        iv_line.setOnClickListener(this);
        iv_prev.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_tape.setOnClickListener(this);

        //录音栏按钮
        iv_rec_pause = (ImageView) findViewById(R.id.iv_rec_pause);
        iv_rec_origin = (ImageView) findViewById(R.id.iv_rec_origin);
        iv_rec_prev = (ImageView) findViewById(R.id.iv_rec_prev);
        iv_rec_button = (ImageView) findViewById(R.id.iv_rec_button);
        iv_rec_next = (ImageView) findViewById(R.id.iv_rec_next);
        iv_rec_record = (ImageView) findViewById(R.id.iv_rec_record);
        iv_rec_back = (ImageView) findViewById(R.id.iv_rec_back);

        iv_rec_pause.setOnClickListener(this);
        iv_rec_origin.setOnClickListener(this);
        iv_rec_prev.setOnClickListener(this);
        iv_rec_button.setOnClickListener(this);
        iv_rec_next.setOnClickListener(this);
        iv_rec_record.setOnClickListener(this);
        iv_rec_back.setOnClickListener(this);

        // 其它控件
        ll_lyrics = (LinearLayout) findViewById(R.id.ll_lyrics);
        rl_buffering = (RelativeLayout) findViewById(R.id.rl_buffering);
        iv_cover = (ImageView) findViewById(R.id.iv_cover);

        sv_lyrics = (ScrollView) findViewById(R.id.sv_lyrics);
        tv_aSide = (TextView) findViewById(R.id.tv_aSide);
        tv_bSide = (TextView) findViewById(R.id.tv_bSide);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title_en = (TextView) findViewById(R.id.tv_title_en);

        ll_play = (LinearLayout) findViewById(R.id.ll_play);
        ll_tape = (LinearLayout) findViewById(R.id.ll_tape);
        tv_play_record_time = (TextView) findViewById(R.id.tv_play_record_time);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 如果由用户手动拖动则更改左右两边的时间标签内容
                if (fromUser && playerOrigin != null) {
                    tv_aSide.setText(millisecondsFormat(playerOrigin.getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                if (playerOrigin != null) {
                    playerOrigin.seekTo(sb.getProgress());
                }

            }
        });
    }

    protected String millisecondsFormat(int milliseconds) {
        long minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (milliseconds % (1000 * 60)) / 1000;
        return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.iv_menu:
                subTitleState++;
                int state = subTitleState % subTitleIcons.size();
                showOrHideSubtitle(state);
                iv_menu.setImageResource(subTitleIcons.get(state));
                break;
            case R.id.iv_call:
                //region 跳转到ActivityMain
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
/*                builder.setTitle("提示");
                builder.setMessage("是否呼叫在线教师一对一辅导本课程？");*/
                builder.setMessage(R.string.ActivityPlay_call_for_help);
                builder.setPositiveButton(R.string.ActivityPlay_call_for_help_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ActivityPlay.this, ActivityMain.class);
                        intent.putExtra(ActivityMain.KEY_TAB_INDEX, 0);
                        intent.putExtra(ActivityMain.KEY_DOCUMENT_ID, documentId);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(R.string.ActivityPlay_call_for_help_negative, null);
                builder.show();
            }
            //endregion
            break;
            //控制按钮事件
            case R.id.iv_line:
                iv_line.setSelected(!iv_line.isSelected());
                isOneLineLoop = iv_line.isSelected();
                if (isOneLineLoop) {
                    setSideASideB();
                }
                break;
            case R.id.iv_prev:
                seekToPrevLine();
                break;
            case R.id.iv_play:
                if (playerOrigin == null) {
                    //防空处理，啊哈哈，真纠结
                    return;
                }
                if (playerOrigin.isPlaying()) {
                    playerOrigin.pause();
                    iv_play.setSelected(true);
                    iv_play.setImageResource(R.drawable.play_btn_play_checked);
                } else {
                    playerOrigin.start();
                    iv_play.setSelected(false);
                    iv_play.setImageResource(R.drawable.play_btn_pause_checked);
                }
                break;
            case R.id.iv_next:
                seekToNextLine();
                break;
            case R.id.iv_tape:
                boolean isPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                if (isPermissionGranted) {
                    goToRecord();
                } else {
                    Log.i(TAG, "跳转: ");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO);
                }
                break;

            case R.id.iv_rec_pause: {
                iv_rec_pause.setSelected(!iv_rec_pause.isSelected());
                if (iv_rec_pause.isSelected()) {
                    //如果是暂停，那么暂停所有的，包括正在录音，播放录音，播放原音
                    if (recorder != null && currentState == MediaState.正在录音) {
                        recorder.stop();
                        iv_rec_button.setSelected(false);
                    }

                    if (playerRecord != null && currentState == MediaState.播放录音) {
                        playerRecord.pause();
                    }

                    if (playerOrigin != null && currentState == MediaState.播放原音) {
                        playerOrigin.pause();
                    }
                } else {
                    switch (currentState) {
                        case 播放原音:
                            playerOrigin.start();
                            break;
                        case 播放录音:
                            playerRecord.start();
                            break;
                        case 正在录音:
                            initMediaRecorder(recordFile.getAbsolutePath());
                            recorder.start();
                            break;
                    }
                }
            }
            break;
            case R.id.iv_rec_origin:
                if (playerRecord == null || playerOrigin == null || recorder == null) {
                    return;
                }
                switch (currentState) {
                    case 播放原音:
                        seekToCurrentLine();
                        break;
                    case 播放录音:
                        playerRecord.stop();
                        break;
                    case 正在录音:
                        recorder.stop();
                        iv_rec_button.setSelected(false);
                        iv_rec_button.setImageResource(R.drawable.play_btn_recording_uncheck);
                        break;
                }
                playerOrigin.start();
                currentState = MediaState.播放原音;
                elapsedTime = 0;
                iv_rec_pause.setSelected(false);
                break;
            case R.id.iv_rec_prev:
                if (playerOrigin == null || playerRecord == null || recorder == null)
                    return;

                switch (currentState) {
                    case 播放原音:
                        seekToPrevLine();
                        break;
                    case 播放录音:
                        playerRecord.stop();
                        break;
                    case 正在录音:
                        recorder.stop();
                        iv_rec_button.setSelected(false);
                        iv_rec_button.setImageResource(R.drawable.play_btn_recording_uncheck);
                        break;
                }

                playerOrigin.start();
                currentState = MediaState.播放原音;
                elapsedTime = 0;
                iv_rec_pause.setSelected(false);
                break;
            case R.id.iv_rec_button:
                if (playerOrigin == null || playerRecord == null || recorder == null)
                    return;

                int currentIndex = getCurrentIndex();
                Log.i(TAG, "onClick: " + currentIndex);

                //暂停键复原,并所有的声音暂停
                iv_rec_pause.setSelected(false);
                if (playerOrigin.isPlaying()) {
                    playerOrigin.pause();
                }
                if (playerRecord.isPlaying()) {
                    playerRecord.pause();
                }

                //如果暂停键没有选中
                if (iv_rec_button.isSelected() && currentState == MediaState.正在录音) {//如果正在录音状态,那么录音停止,并播放录音
                    elapsedTime = 0;
                    recorder.stop();//停止录音
                    initRecordPlayer(recordFile.getAbsolutePath(), true);
                    playerRecord.start();//播放录音文件
                    currentState = MediaState.播放录音;
                    iv_rec_button.setSelected(false);
                    iv_rec_button.setImageResource(R.drawable.play_btn_recording_uncheck);
                } else {
                    elapsedTime = 0;
                    recorder.reset();
                    initMediaRecorder(recordFile.getAbsolutePath());
                    recorder.start();
                    currentState = MediaState.正在录音;
                    iv_rec_button.setSelected(true);
                    iv_rec_button.setImageResource(R.drawable.play_btn_recording_selected);
                }

                break;
            case R.id.iv_rec_next:
                if (playerOrigin == null || playerRecord == null || recorder == null)
                    return;
                switch (currentState) {
                    case 播放原音:
                        seekToNextLine();
                        break;
                    case 播放录音:
                        playerRecord.stop();
                        break;
                    case 正在录音:
                        try {
                            recorder.stop();
                            iv_rec_button.setSelected(false);
                            iv_rec_button.setImageResource(R.drawable.play_btn_recording_uncheck);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                playerOrigin.start();
                currentState = MediaState.播放原音;
                elapsedTime = 0;
                iv_rec_pause.setSelected(false);
                break;
            case R.id.iv_rec_record:
                if (recordFile.exists() && recordFile.length() > 0) {
                    switch (currentState) {
                        case 播放原音:
                            playerOrigin.pause();
                            break;
                        case 播放录音:
                            playerRecord.seekTo(0);
                            break;
                        case 正在录音:
                            recorder.stop();
                            iv_rec_button.setSelected(false);
                            iv_rec_button.setImageResource(R.drawable.play_btn_recording_uncheck);
                            break;
                    }
                    initRecordPlayer(recordFile.getAbsolutePath(), true);
                    playerRecord.start();
                    currentState = MediaState.播放录音;
                    elapsedTime = 0;
                    iv_rec_pause.setSelected(false);
                } else {
                    CommonUtil.toast("还没有录音呢！");
                }
                break;
            case R.id.iv_rec_back:
                if (playerOrigin == null || playerRecord == null || recorder == null) {
                    return;
                }

                //播放状态下的单句循环状态复原,包括单句循环按钮
                isOneLineLoop = playSingleLineState;
                iv_line.setSelected(playSingleLineState);

                //暂停录音，暂停播放录音，并根据原音播放状态调整暂停按钮图片
                switch (currentState) {
                    case 播放录音:
                        playerRecord.stop();
                        break;
                    case 正在录音:
                        recorder.stop();
                        break;
                }
                iv_play.setSelected(!playerOrigin.isPlaying());
                iv_play.setImageResource(playerOrigin.isPlaying() ? R.drawable.play_btn_pause_checked : R.drawable.play_btn_play_checked);

                // 控制栏右移动,切换到正常模式,同时把录音播放和录音的对象停止
                toRAnimator.start();
                tv_play_record_time.setVisibility(View.INVISIBLE);
                break;
        }
    }

    /**
     * 录音界面
     */
    private void goToRecord() {
        //播放状态下的单句循环模式保存
        playSingleLineState = isOneLineLoop;

        // 控制栏左移动,切换到录音模式
        toLAnimator.start();
        isOneLineLoop = true;// 自动进入单句循环
        iv_line.setSelected(true);
        tv_play_record_time.setVisibility(View.VISIBLE);

        // 初始化音频录音对播放录音的对象,这两个对象操作的是同一个文件
        if (recordFile == null) {
            recordFile = new File(getFilesDir(), "record.tmp");
        }
        if (playerRecord == null) {
            initRecordPlayer(recordFile.getAbsolutePath(), recordFile.length() > 0);
        }
        if (recorder == null) {
            initMediaRecorder(recordFile.getAbsolutePath());
        }

        // 播放当前的原音单句,并把录音栏的暂停按钮重置为"可以暂停"
        elapsedTime = 0;
        if (playerOrigin != null && !playerOrigin.isPlaying()) {
            iv_rec_pause.setSelected(true);
        }
        tv_play_record_time.setText(currentState + ":" + millisecondsFormat(elapsedTime));
        seekToCurrentLine();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.play_activity, menu);
        return true;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "onError:" + " what=" + what + " extra=" + extra);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // MEDIA_INFO_UNKNOWN
        // MEDIA_INFO_VIDEO_TRACK_LAGGING
        // MEDIA_INFO_VIDEO_RENDERING_START
        // MEDIA_INFO_BUFFERING_START
        // MEDIA_INFO_BUFFERING_END
        // MEDIA_INFO_BAD_INTERLEAVING
        // MEDIA_INFO_NOT_SEEKABLE
        // MEDIA_INFO_METADATA_UPDATE
        // MEDIA_INFO_UNSUPPORTED_SUBTITLE
        // MEDIA_INFO_SUBTITLE_TIMED_OUT

        switch (what) {
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                Log.i("logi", "INFO=MEDIA_INFO_UNKNOWN" + what);
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                Log.i("logi", "INFO=MEDIA_INFO_VIDEO_TRACK_LAGGING" + what);
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                Log.i("logi", "INFO=MEDIA_INFO_VIDEO_RENDERING_START" + what);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.i("logi", "INFO=MEDIA_INFO_BUFFERING_START" + what);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.i("logi", "INFO=MEDIA_INFO_BUFFERING_END" + what);
                break;
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                Log.i("logi", "INFO=MEDIA_INFO_BAD_INTERLEAVING" + what);
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.i("logi", "INFO=MEDIA_INFO_METADATA_UPDATE" + what);
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.i("logi", "INFO=MEDIA_INFO_NOT_SEEKABLE" + what);
                break;
            case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                Log.i("logi", "INFO=MEDIA_INFO_UNSUPPORTED_SUBTITLE" + what);
                break;
            case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                Log.i("logi", "INFO=MEDIA_INFO_SUBTITLE_TIMED_OUT" + what);
                break;
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        rl_buffering.setVisibility(View.INVISIBLE);
        sv_lyrics.setVisibility(View.VISIBLE);
        mp.start();
        seekBar.setMax(mp.getDuration());
        tv_bSide.setText(millisecondsFormat(mp.getDuration()));
        sideB = mp.getDuration();

        // 点出播放单句,同时重置A-B两端
        if (specialLyricViews != null && playerOrigin != null && playerOrigin.getDuration() > 0) {
            for (int i = 0; i < specialLyricViews.size(); i++) {
                final Integer timeA = specialLyricViews.get(i).getTimeLabel();
                final Integer timeB = i == (specialLyricViews.size() - 1) ? playerOrigin.getDuration() : specialLyricViews.get(i + 1).getTimeLabel();
                specialLyricViews.get(i).setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        sideA = timeA;
                        sideB = timeB;
                        playerOrigin.seekTo(sideA);
                    }
                });

            }
        }

        // 定时更新歌词及SeekBar,找出当前播放的那一句
        handler.sendEmptyMessageDelayed(REFRESH_SEEKBAR, 100);
    }

    protected void refresh_seekbar() {
        Log.i(TAG, "refresh_seekbar: " + elapsedTime);
        tv_play_record_time.setText(currentState + ":" + millisecondsFormat(elapsedTime));

        long currentLineTime = 0;
        long nextLineTime = 0;

        if (playerOrigin == null) {
            return;
        }

        int currentPosition = playerOrigin.getCurrentPosition();
        seekBar.setProgress(playerOrigin.getCurrentPosition());
        tv_aSide.setText(millisecondsFormat(playerOrigin.getCurrentPosition()));

        if (isOneLineLoop && currentPosition > sideB) {
            playerOrigin.seekTo(sideA);
        }

        for (int i = 0; i < specialLyricViews.size(); i++) {
            SpecialLyricView view = specialLyricViews.get(i);
            currentLineTime = view.getTimeLabel();

            nextLineTime = (i + 1 == specialLyricViews.size()) ? playerOrigin.getDuration() : specialLyricViews.get(i + 1).getTimeLabel();

            if (currentLineTime <= currentPosition && currentPosition < nextLineTime) {
                // 高亮显示字幕
                view.highlight();

                // 高亮字幕位置自动滚动功能,即如果现在播放到某一个时刻,字幕却不在屏幕显示时
                if (sv_lyrics.getScrollY() < view.getTop() && view.getTop() < sv_lyrics.getScrollY() + 800) {
                    // Log.i("logi", "不用跳");
                } else {
                    sv_lyrics.scrollTo(0, view.getTop());
                }

            } else {
                view.resetColor();
            }
        }

        if (isOneLineLoop) {
            if (playerOrigin.getCurrentPosition() > sideB) {
                playerOrigin.seekTo(sideA);
            }
        }

    }

    private void seekToCurrentLine() {
        int index = getCurrentIndex();
        if (index > 0) {
            SpecialLyricView c = specialLyricViews.get(index);
            sideA = c.getTimeLabel();
            if (index < specialLyricViews.size() - 1) {
                SpecialLyricView n = specialLyricViews.get(index + 1);
                sideB = n.getTimeLabel();
            } else {
                sideB = playerOrigin.getDuration();
            }

            playerOrigin.seekTo(sideA);
        }
    }

    private void seekToNextLine() {
        int index = getCurrentIndex();
        if (index + 2 < specialLyricViews.size()) {
            SpecialLyricView next = specialLyricViews.get(index + 1);
            SpecialLyricView nextNext = specialLyricViews.get(index + 2);
            sideA = next.getTimeLabel();
            sideB = nextNext.getTimeLabel();
            playerOrigin.seekTo(sideA);
        }
    }

    private void seekToPrevLine() {
        int index = getCurrentIndex();
        if (index > 0) {
            SpecialLyricView prev = specialLyricViews.get(index - 1);
            SpecialLyricView curr = specialLyricViews.get(index);
            sideA = prev.getTimeLabel();
            sideB = curr.getTimeLabel();
            playerOrigin.seekTo(sideA);
        }
    }

    private void setSideASideB() {
        if (specialLyricViews != null && playerOrigin != null && playerOrigin.getDuration() > 0) {
            int currentPosition = playerOrigin.getCurrentPosition();
            for (int i = 0; i < specialLyricViews.size(); i++) {
                Integer timeA = specialLyricViews.get(i).getTimeLabel();
                Integer timeB = i == (specialLyricViews.size() - 1) ? playerOrigin.getDuration() : specialLyricViews.get(i + 1).getTimeLabel();
                if (timeA < currentPosition && currentPosition < timeB) {
                    sideA = timeA;
                    sideB = timeB;
                }
            }
        }
    }

    private void showOrHideSubtitle(int state) {
/*
        Integer integer = subTitleIcons.get(state);
        switch (integer) {
            case R.drawable.switch_none:
                for (SpecialLyricView view : specialLyricViews) {
                    view.showEnCn(SpecialLyricView.SHOW_NONE);
                    iv_cover.setVisibility(View.VISIBLE);
                }
                break;
            case R.drawable.switch_cn:
                for (SpecialLyricView view : specialLyricViews) {
                    view.showEnCn(SpecialLyricView.SHOW_CN);
                    iv_cover.setVisibility(View.INVISIBLE);
                }
                break;
            case R.drawable.switch_cnen:
                for (SpecialLyricView view : specialLyricViews) {
                    view.showEnCn(SpecialLyricView.SHOW_ENCN);
                    iv_cover.setVisibility(View.INVISIBLE);
                }
                break;
            default:

                break;
        }*/

        //20161202要求更改中英文显示方式，先显示汉字/英文（拼音），第二显示中文或者拼音，第三显示封面
        switch (state) {
            case 0:
                for (SpecialLyricView view : specialLyricViews) {
                    view.showEnCn(SpecialLyricView.SHOW_ENCN);
                    iv_cover.setVisibility(View.INVISIBLE);
                }
                break;
            case 1:
                for (SpecialLyricView view : specialLyricViews) {
                    view.showEnCn(SpecialLyricView.SHOW_CN);
                    iv_cover.setVisibility(View.INVISIBLE);
                }
                break;
            case 2:
                for (SpecialLyricView view : specialLyricViews) {
                    view.showEnCn(SpecialLyricView.SHOW_NONE);
                    iv_cover.setVisibility(View.VISIBLE);
                }
                break;
            default:

                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_RECORD_AUDIO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goToRecord();
                } else {
                    new AlertDialog
                            .Builder(this)
                            .setMessage("这个功能需要使用麦克风!")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    boolean b = ActivityCompat.shouldShowRequestPermissionRationale(ActivityPlay.this, Manifest.permission.RECORD_AUDIO);
                                    if (b) {
                                        Log.i(TAG, "请求: ");
                                        ActivityCompat.requestPermissions(ActivityPlay.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO);
                                    } else {
                                        Log.i(TAG, "跳转: ");
                                        Intent settingIntent = new Intent();
                                        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        settingIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                        //settingIntent.setAction("android.settings.APPLICATION_SETTINGS");
                                        settingIntent.setData(Uri.fromParts("package", getPackageName(), null));
                                        startActivity(settingIntent);
                                    }
                                }
                            })
                            .show();
                }
                break;
        }
    }
}
