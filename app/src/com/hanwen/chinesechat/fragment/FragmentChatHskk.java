package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityChat;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Hskk;
import com.hanwen.chinesechat.bean.HskkQuestion;
import com.hanwen.chinesechat.bean.NimSysNotice;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * 实时通话界面HSKK部分，该界面只提供一个ChildFragmentManager和FrameLayout
 */
public class FragmentChatHskk extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    public static final String TAG = "FragmentChatHskk";
    public static final int OPEN_MODE_PICK = 1;
    public static final int OPEN_MODE_SHOW = 2;
    private static final int WHAT_NEXT_HSKK = 1;
    private static final String KEY_HSKK_ID = "KEY_HSKK_ID";
    private static final String KEY_OPEN_MODE = "KEY_OPEN_MODE";
    private GridView gridView;
    private int currentHskkPosition;
    private List<Hskk> listHskk = new ArrayList<>();
    private List<HskkQuestion> listQuestion = new ArrayList<>();
    private ListView listView;
    private RadioButton tv_part1;
    private RadioButton tv_part2;
    private RadioButton tv_part3;
    private RadioGroup rg_part;
    private TextView tv_part;
    private TextView tv_position;
    private TextView tv_rank;
    private TextView tv_tips;
    private View iv_check;
    private View iv_next;
    private View ll_part;
    private View rl_rank;
    private View rl_tips;
    private int rank;
    private int part;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_NEXT_HSKK:
                    iv_next.setEnabled(true);
                    break;
            }
        }
    };

    public static FragmentChatHskk newInstance(int openMode, int hskkId) {
        FragmentChatHskk fragmentChatHskk = new FragmentChatHskk();
        Bundle args = new Bundle();
        args.putInt(KEY_OPEN_MODE, openMode);
        args.putInt(KEY_HSKK_ID, hskkId);
        fragmentChatHskk.setArguments(args);
        return fragmentChatHskk;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_hskk, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int openMode = getArguments().getInt(KEY_OPEN_MODE, OPEN_MODE_SHOW);
        int hskkId = getArguments().getInt(KEY_HSKK_ID, 0);
        rl_rank = view.findViewById(R.id.rl_rank);
        view.findViewById(R.id.tv_rank1).setOnClickListener(this);
        view.findViewById(R.id.tv_rank2).setOnClickListener(this);
        view.findViewById(R.id.tv_rank3).setOnClickListener(this);

        ll_part = view.findViewById(R.id.ll_part);
        view.findViewById(R.id.iv_back1).setOnClickListener(this);
        tv_rank = (TextView) view.findViewById(R.id.tv_rank);
        iv_check = view.findViewById(R.id.iv_check);
        iv_check.setOnClickListener(this);
        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                iv_check.setVisibility(View.VISIBLE);
                currentHskkPosition = position;
                ((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();
            }
        });

        gridView.setAdapter(new BaseAdapter<Hskk>(listHskk) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.listitem_chat_hskk_hskk, null);
                    new ViewHolder(convertView);
                }

                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.tv_name.setText(String.format("NO.%03d", position + 1));
                holder.tv_name.setSelected(currentHskkPosition == position);
                return convertView;
            }
        });

        rg_part = (RadioGroup) view.findViewById(R.id.rg_part);
        rg_part.setOnCheckedChangeListener(this);

        tv_part1 = (RadioButton) view.findViewById(R.id.tv_part1);
        tv_part2 = (RadioButton) view.findViewById(R.id.tv_part2);
        tv_part3 = (RadioButton) view.findViewById(R.id.tv_part3);
        tv_part = (TextView) view.findViewById(R.id.tv_part);

        rl_tips = view.findViewById(R.id.rl_tips);
        View iv_back2 = view.findViewById(R.id.iv_back2);
        iv_back2.setOnClickListener(this);
        tv_tips = (TextView) view.findViewById(R.id.tv_tips);
        iv_next = view.findViewById(R.id.iv_next);
        iv_next.setOnClickListener(this);
        tv_position = (TextView) view.findViewById(R.id.tv_position);

        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(new BaseAdapter<HskkQuestion>(listQuestion) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                HskkQuestion item = getItem(position);
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.listitem_chat_hskk_question, null);
                    new ViewHolder(convertView);
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.tv_name.setText(item.TextCN);
                if (item.Hskk.Category == 2) {
                    holder.tv_name.setVisibility(View.GONE);
                    holder.iv_image.setVisibility(View.VISIBLE);
                    new BitmapUtils(getContext(), getContext().getCacheDir().getAbsolutePath()).display(holder.iv_image, NetworkUtil.getFullPath(item.Image));
                } else {
                    holder.iv_image.setVisibility(View.GONE);
                }
                return convertView;
            }
        });

        if (openMode == OPEN_MODE_SHOW) {
            rl_rank.setVisibility(View.INVISIBLE);
            ll_part.setVisibility(View.INVISIBLE);
            iv_next.setVisibility(View.INVISIBLE);
            iv_back2.setVisibility(View.INVISIBLE);
            rl_tips.setVisibility(View.VISIBLE);
            tv_position.setVisibility(View.INVISIBLE);

            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("id", hskkId);
            HttpUtil.post(NetworkUtil.hskkGetById, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Response<Hskk> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<Hskk>>() {}.getType());
                    Hskk info = resp.info;
                    CommonUtil.hskkDesc(info);
                    tv_part.setText(String.format("%1$s/%2$s", info.PartName, info.RankName));
                    tv_tips.setText(info.Desc);
                    List<HskkQuestion> questions = info.Questions;
                    if (questions != null) {
                        for (HskkQuestion hq : questions) {
                            hq.Hskk = info;
                            listQuestion.add(hq);
                        }
                    }
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                }

                @Override
                public void onFailure(HttpException error, String msg) {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_rank1:
            case R.id.tv_rank2:
            case R.id.tv_rank3: {
                if (v.getId() == R.id.tv_rank1) {
                    rank = 1;
                } else if (v.getId() == R.id.tv_rank2) {
                    rank = 2;
                } else if (v.getId() == R.id.tv_rank3) {
                    rank = 3;
                }
                rl_rank.setVisibility(View.INVISIBLE);
                ll_part.setVisibility(View.VISIBLE);

                //Part界面的标题和tabs
                tv_rank.setText(CommonUtil.hskkRank(rank));
                tv_part1.setText(CommonUtil.hskkPart(rank, 1));
                tv_part2.setText(CommonUtil.hskkPart(rank, 2));
                tv_part3.setText(CommonUtil.hskkPart(rank, 3));

                listHskk.clear();
                ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();

                rg_part.check(-1);
                tv_part1.setChecked(true);
            }
            break;
            case R.id.iv_back1:
                //界面切换
                ll_part.setVisibility(View.INVISIBLE);
                rl_rank.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_check:
                if (currentHskkPosition > -1) {
                    ll_part.setVisibility(View.INVISIBLE);
                    rl_tips.setVisibility(View.VISIBLE);

                    Hskk hskk = listHskk.get(currentHskkPosition);
                    sendQuestion(hskk);
                    showQuestion(hskk);
                }
                break;
            case R.id.iv_back2:
                //界面切换
                rl_tips.setVisibility(View.INVISIBLE);
                ll_part.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_next:
                //如果后面没有了，不要切换
                currentHskkPosition++;
                if (currentHskkPosition >= listHskk.size()) {
                    Toast.makeText(getContext(), "后面没有了", Toast.LENGTH_SHORT).show();
                    return;
                }

                //显示HSKK题型和题目
                Hskk hskk = listHskk.get(currentHskkPosition);
                sendQuestion(hskk);
                showQuestion(hskk);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        part = 0;
        if (checkedId == R.id.tv_part1 && tv_part1.isChecked()) {
            part = 1;
        } else if (checkedId == R.id.tv_part2 && tv_part2.isChecked()) {
            part = 2;
        } else if (checkedId == R.id.tv_part3 && tv_part3.isChecked()) {
            part = 3;
        }

        if (part > 0) {
            //数据复位,清除
            currentHskkPosition = -1;
            iv_check.setVisibility(View.INVISIBLE);
            listHskk.clear();
            ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();

            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("rank", rank);
            params.add("part", part);
            HttpUtil.post(NetworkUtil.hskkGetListByRankAndPart, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "onSuccess: " + responseInfo.result);
                    Response<List<Hskk>> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Hskk>>>() {}.getType());
                    for (Hskk h : resp.info) {
                        listHskk.add(h);
                    }
                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                }

                @Override
                public void onFailure(HttpException error, String msg) {

                }
            });
        }
    }

    private void sendQuestion(Hskk hskk) {
        iv_next.setEnabled(false);
        handler.sendEmptyMessageDelayed(WHAT_NEXT_HSKK, 5 * 1000);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", NimSysNotice.NoticeType_Hskk);
        jsonObject.addProperty("info", hskk.Id);

        ActivityChat activity = (ActivityChat) getActivity();
        CustomNotification notification = new CustomNotification();
        notification.setSessionId(activity.chatData.getAccount());
        notification.setSessionType(SessionTypeEnum.P2P);
        notification.setContent(jsonObject.toString());
        NIMClient.getService(MsgService.class).sendCustomNotification(notification);
    }

    private void showQuestion(Hskk hskk) {
        //数据显示
        CommonUtil.hskkDesc(hskk);
        tv_part.setText(String.format("%1$s/%2$s", hskk.PartName, hskk.RankName));
        tv_tips.setText(hskk.Desc);
        tv_position.setText(String.format("No.%1$03d", currentHskkPosition + 1));

        listQuestion.clear();
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

        if (hskk.Visible == 1) {
            listView.setVisibility(View.VISIBLE);
            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("id", hskk.Id);
            HttpUtil.post(NetworkUtil.hskkGetById, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Response<Hskk> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<Hskk>>() {}.getType());
                    Hskk info = resp.info;
                    List<HskkQuestion> questions = info.Questions;
                    if (questions != null) {
                        for (HskkQuestion hq : questions) {
                            hq.Hskk = info;
                            listQuestion.add(hq);
                        }
                    }
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                }

                @Override
                public void onFailure(HttpException error, String msg) {

                }
            });
        } else {
            listView.setVisibility(ChineseChat.isStudent() ? View.GONE : View.VISIBLE);
        }
    }

    private class ViewHolder {
        public TextView tv_name;
        public ImageView iv_image;

        public ViewHolder(View convertView) {
            this.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            this.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
            convertView.setTag(this);
        }
    }

}
