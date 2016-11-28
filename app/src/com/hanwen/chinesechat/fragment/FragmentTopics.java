package com.hanwen.chinesechat.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.NimSysNotice;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.FlowLayout;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 话题选择界面，流式与随机方式
 */
public class FragmentTopics extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentTopics";
    private static final String KEY_ACCID = "KEY_ACCID";
    private ViewGroup fl_content;
    private List<Theme> data = new ArrayList<>();
    private int currentIndex = -1;
    private TextView tv_name;
    private TextView tv_name_en;
    private View iv_check;
    private View ll_content;
    private TextView tv_current;
    private View iv_home;
    private String accid;
    private View ll_pick;
    private View tv_random;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accid Parameter 1.
     * @return A new instance of fragment FragmentTopics.
     */
    public static FragmentTopics newInstance(String accid) {
        FragmentTopics fragment = new FragmentTopics();
        Bundle args = new Bundle();
        args.putString(KEY_ACCID, accid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_random:
                if (data.size() > 0) {
                    Random random = new Random();
                    int i = random.nextInt(data.size());
                    tv_random.setBackgroundResource(R.drawable.shape_rectangle_radius05_green);

                    switchTheme(i);
                } else {
                    Toast.makeText(getContext(), "没有数据，无法随机", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_check:
                if (currentIndex > -1) {
                    Theme theme = data.get(currentIndex);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("type", NimSysNotice.NOTICE_TYPE_TOPIC);
                    jsonObject.addProperty("info", theme.Id);

                    if (!TextUtils.isEmpty(accid)) {
                        CustomNotification custom = new CustomNotification();
                        custom.setSessionId(accid);
                        custom.setSessionType(SessionTypeEnum.P2P);
                        custom.setContent(jsonObject.toString());
                        NIMClient.getService(MsgService.class).sendCustomNotification(custom);
                    }

                    ll_content.setVisibility(View.INVISIBLE);
                    iv_check.setVisibility(View.INVISIBLE);
                    tv_current.setText(theme.Name);
                    tv_current.setVisibility(View.VISIBLE);
                    iv_home.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.iv_home:
                ll_content.setVisibility(View.VISIBLE);
                iv_check.setVisibility(View.VISIBLE);
                tv_current.setVisibility(View.INVISIBLE);
                iv_home.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void switchTheme(int i) {
        FlowLayout childAt = (FlowLayout) fl_content.getChildAt(0);

        if (currentIndex > -1) {
            childAt.getChildAt(currentIndex).setBackgroundResource(R.drawable.shape_rectangle_radius05_gray);
        }

        currentIndex = i;

        iv_check.setVisibility(View.VISIBLE);
        ll_pick.setVisibility(View.VISIBLE);
        Theme theme = data.get(currentIndex);
        tv_name.setText(theme.Name);
        tv_name_en.setText(theme.NameEn);
        tv_name_en.setVisibility(TextUtils.isEmpty(theme.NameEn) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accid = getArguments().getString(KEY_ACCID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_topics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_random = view.findViewById(R.id.tv_random);
        tv_random.setOnClickListener(this);

        iv_check = view.findViewById(R.id.iv_check);
        iv_check.setVisibility(View.INVISIBLE);
        iv_check.setOnClickListener(this);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_name_en = (TextView) view.findViewById(R.id.tv_name_en);

        ll_content = view.findViewById(R.id.ll_content);
        fl_content = (ViewGroup) view.findViewById(R.id.fl_content);
        tv_current = (TextView) view.findViewById(R.id.tv_current);
        iv_home = view.findViewById(R.id.iv_home);
        iv_home.setOnClickListener(this);
        ll_pick = view.findViewById(R.id.ll_pick);

        HttpUtil.post(NetworkUtil.ThemeSelect, null, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<List<Theme>> o = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Theme>>>() {}.getType());
                data.clear();
                data.addAll(o.info);

                FlowLayout flowLayout = new FlowLayout(getContext());
                for (Theme t : data) {
                    LinearLayout linearLayout = new LinearLayout(getContext());
                    linearLayout.setPadding(10, 10, 10, 10);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getContext().getResources().getDisplayMetrics()));
                    TextView textView = new TextView(getContext());
                    textView.setText(t.Name);
                    textView.setSingleLine();
                    textView.setLines(1);
                    textView.setEllipsize(TextUtils.TruncateAt.END);
                    textView.setTextColor(Color.WHITE);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    linearLayout.addView(textView);

                    if (!TextUtils.isEmpty(t.NameEn)) {
                        TextView textViewEn = new TextView(getContext());
                        textViewEn.setText(t.NameEn);
                        textViewEn.setGravity(Gravity.CENTER_HORIZONTAL);
                        textViewEn.setLines(1);
                        textViewEn.setEllipsize(TextUtils.TruncateAt.END);
                        textViewEn.setTextColor(Color.WHITE);
                        linearLayout.addView(textViewEn);
                    }

                    linearLayout.setGravity(Gravity.CENTER);
                    linearLayout.setBackgroundResource(R.drawable.shape_rectangle_radius05_gray);
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.setBackgroundResource(R.drawable.shape_rectangle_radius05_green);
                            tv_random.setBackgroundResource(R.drawable.shape_rectangle_radius05_gray);
                            FlowLayout parent = (FlowLayout) v.getParent();
                            for (int i = 0; i < parent.getChildCount(); i++) {
                                View childAt = parent.getChildAt(i);
                                if (v.equals(childAt)) {
                                    switchTheme(i);
                                    break;
                                }
                            }
                        }
                    });
                    flowLayout.addView(linearLayout);
                }
                fl_content.addView(flowLayout);
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }
}
