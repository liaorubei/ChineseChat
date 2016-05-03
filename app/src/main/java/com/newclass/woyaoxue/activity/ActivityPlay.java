package com.newclass.woyaoxue.activity;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.newclass.woyaoxue.bean.Document;
import com.newclass.woyaoxue.bean.DownloadInfo;
import com.newclass.woyaoxue.bean.Lyric;
import com.newclass.woyaoxue.bean.UrlCache;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.SpecialLyricView;
import com.voc.woyaoxue.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityPlay extends Activity implements OnClickListener, OnPreparedListener, OnErrorListener, OnInfoListener {
    private static final String TAG = "PlayActivity";
    private ArrayList<Integer> subTitleIcons;

    private enum MediaState {
        正在录音, 播放录音, 播放原音, 全部暂停
    }

    protected static final int REFRESH_SEEKBAR = 0;
    private Database database;
    private int documentId;
    private int elapsedTime = 0;// 录音/播放已经耗费的时间,毫秒数milliseconds
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_SEEKBAR:
                    refresh_seekbar();
                    break;
                default:
                    break;
            }
        }
    };
    // 是否单句循环
    private boolean isOneLineLoop = false;

    private RelativeLayout rl_buffering;
    private ImageView iv_cover;

    //状态栏按钮
    private View iv_home, bt_menu;

    //播放栏按钮
    private ImageView iv_line, iv_prev, iv_play, iv_next, iv_tape;

    //录音栏按钮
    private ImageView iv_rec_pause, iv_rec_origin, iv_rec_prev, iv_rec_button, iv_rec_record, iv_rec_next, iv_rec_back;

    private LinearLayout ll_lyrics, ll_play, ll_tape;

    private MediaRecorder recorder;// 音频录音对象
    private MediaPlayer playerOrigin, playerRecord;// 原音,录音播放对象

    private DisplayMetrics outMetrics = new DisplayMetrics();

    private FrameLayout.LayoutParams playParams, recordParams;// 控制按钮布局的布局参数
    private File recordFile;// 录音文件对象

    private SeekBar seekBar;
    private int sideA = 0, sideB = 0;
    private List<SpecialLyricView> specialLyricViews;
    private Integer subTitleState = 0;
    private ScrollView sv_lyrics;
    private ValueAnimator toRAnimator, toLAnimator;// 控制按钮布局的向右向左属性动画
    private TextView tv_bSide, tv_aSide, tv_title;
    private TextView tv_play_record_time;
    private MediaState currentState = MediaState.播放原音;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        subTitleIcons = new ArrayList<Integer>();
        subTitleIcons.add(R.drawable.switch_none);
        subTitleIcons.add(R.drawable.switch_cn);
        subTitleIcons.add(R.drawable.switch_cnen);

        initView();

        tv_aSide.setText("");
        tv_bSide.setText("");
        tv_title.setText("");

        Intent intent = getIntent();
        documentId = intent.getIntExtra("Id", 429);
        // 显示返回按钮
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        database = new Database(this);
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
    }


    private void fillData(Document document) {
        tv_aSide.setText("00:00");
        tv_bSide.setText(document.LengthString);
        tv_title.setText(document.Title);

        specialLyricViews = new ArrayList<SpecialLyricView>();

        for (Lyric lyric : document.Lyrics) {
            SpecialLyricView specialLyricView = new SpecialLyricView(ActivityPlay.this, lyric);
            specialLyricViews.add(specialLyricView);
        }

        Collections.sort(specialLyricViews);

        for (SpecialLyricView specialLyricView : specialLyricViews) {
            // 在刚开始的时候,显示中文字幕的
            specialLyricView.showEnCn(SpecialLyricView.SHOW_NONE);
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
        // 如果已经下载,那么直接使用下载的数据
        DownloadInfo info = database.docsSelectById(documentId);
        if (info != null && info.IsDownload == 1 && !TextUtils.isEmpty(info.Json)) {
            Document document = new Gson().fromJson(info.Json, Document.class);
            fillData(document);
            return;
        }

        String url = NetworkUtil.getDocById(documentId);

        UrlCache cache = database.cacheSelectByUrl(url);
        if (cache == null || (System.currentTimeMillis() - cache.UpdateAt > 6000000)) // 60分钟
        {
            new HttpUtils().send(HttpMethod.GET, url, new RequestCallBack<String>() {

                @Override
                public void onFailure(HttpException error, String msg) {

                }

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Document document = new Gson().fromJson(responseInfo.result, Document.class);
                    fillData(document);

                    UrlCache urlCache = new UrlCache();
                    urlCache.Url = this.getRequestUrl();
                    urlCache.Json = responseInfo.result;
                    urlCache.UpdateAt = System.currentTimeMillis();
                    if (database != null) {
                        database.cacheInsertOrUpdate(urlCache);
                    }
                }

            });
        } else {
            Document document = new Gson().fromJson(cache.Json, Document.class);
            fillData(document);
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
            if (playerRecord == null) {
                playerRecord = new MediaPlayer();
                playerRecord.setAudioStreamType(AudioManager.STREAM_MUSIC);
                playerRecord.setOnCompletionListener(new OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i(TAG, "onCompletion: 录音播放完毕");
                        if (currentState == MediaState.播放录音) {
                            seekToCurrentLine();
                            playerOrigin.start();
                            currentState = MediaState.播放原音;
                        }
                    }
                });
            }
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
        bt_menu = (Button) findViewById(R.id.bt_menu);
        iv_home.setOnClickListener(this);
        bt_menu.setOnClickListener(this);

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
            case R.id.bt_menu:

                subTitleState++;
                int state = subTitleState % subTitleIcons.size();
                showOrHideSubtitle(state);
                bt_menu.setBackgroundResource(subTitleIcons.get(state));
                break;


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
                if (playerOrigin.isPlaying()) {
                    playerOrigin.pause();
                    iv_play.setImageResource(R.drawable.play_btn_play_checked);
                } else {
                    playerOrigin.start();
                    iv_play.setImageResource(R.drawable.play_btn_pause_checked);
                }
                break;
            case R.id.iv_next:
                seekToNextLine();
                break;
            case R.id.iv_tape:
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
                seekToCurrentLine();
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
                stopOrPauseMedia(false, false);
                seekToCurrentLine();
                playerOrigin.start();
                currentState = MediaState.播放原音;
                break;
            case R.id.iv_rec_prev:
                stopOrPauseMedia(false, false);
                seekToPrevLine();
                playerOrigin.start();
                currentState = MediaState.播放原音;
                break;
            case R.id.iv_rec_button:
                Log.i(TAG, "onClick: iv_rec_button=" + iv_rec_button.isSelected());

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
                stopOrPauseMedia(false, false);
                seekToNextLine();
                playerOrigin.start();
                currentState = MediaState.播放原音;

                break;
            case R.id.iv_rec_record:
                stopOrPauseMedia(false, false);
                if (recordFile.exists() && recordFile.length() > 0) {
                    playerRecord.reset();
                    initRecordPlayer(recordFile.getAbsolutePath(), true);
                    playerRecord.start();
                    currentState = MediaState.播放录音;
                }
                break;
            case R.id.iv_rec_back:
                stopOrPauseMedia(false, false);
                // 控制栏右移动,切换到正常模式,同时把录音播放和录音的对象停止
                toRAnimator.start();
                tv_play_record_time.setVisibility(View.INVISIBLE);
                playerOrigin.start();
                currentState = MediaState.播放原音;
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.play_activity, menu);
        return true;
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
        if (database != null) {
            database.closeConnection();
            database = null;
        }
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

        iv_cover.setVisibility(View.VISIBLE);
        AnimationSet set = new AnimationSet(true);

        AlphaAnimation alpha = new AlphaAnimation(0F, 1F);
        alpha.setDuration(2000);

        ScaleAnimation scale = new ScaleAnimation(0F, 1F, 0F, 1F);
        scale.setDuration(2000);

        set.addAnimation(alpha);
        set.addAnimation(scale);
        iv_cover.startAnimation(alpha);

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
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                if (playerOrigin != null) // && mediaPlayer.isPlaying())
                {
                    handler.sendEmptyMessage(REFRESH_SEEKBAR);
                }

                elapsedTime += 100;// milliseconds

            }
        }, 0, 100);
    }

    protected void refresh_seekbar() {
        setTipsTextView();

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

    /**
     * 从头开始播放当前时间段的这一句
     */
    private void seekToCurrentLine() {
        int index = getCurrentIndex();
        if (index > 0) {
            SpecialLyricView c = specialLyricViews.get(index);
            sideA = c.getTimeLabel();
            if (index < specialLyricViews.size()) {
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

    private void setTipsTextView() {
        if (!iv_rec_pause.isSelected()) {
            tv_play_record_time.setText(currentState + ":" + millisecondsFormat(elapsedTime));
        }
    }

    private void showOrHideSubtitle(int state) {

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
        }

    }

    /**
     * 暂停或停止相关多媒体对象的工作,并把(录音/暂停)按钮重置为对应的值
     *
     * @param pause  暂停按钮的状态
     * @param record 录音按钮的状态
     */
    private void stopOrPauseMedia(boolean pause, boolean record) {
        elapsedTime = 0;
        iv_rec_pause.setSelected(pause);
        iv_rec_button.setSelected(record);
        if (currentState == MediaState.播放原音 && playerOrigin != null) {
            playerOrigin.pause();
        } else if (currentState == MediaState.播放录音) {
            playerRecord.pause();
        } else if (currentState == MediaState.正在录音) {
            recorder.stop();
        }
    }
}
