package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Question;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 实时语音通话界面话题显示界面，当学生选择话题之后，在老师一端显示话题时便是这个界面
 */
public class FragmentTopicsShow extends Fragment {
    private static final String KEY_THEME_ID = "KEY_THEME_ID";
    private static final String TAG = "FragmentTopicsShow";
    private int themeId = 0;
    private TextView tv_name;
    private ListView listView;
    private List<Question> listQuestion = new ArrayList<>();

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param themeId 话题的Id.
     * @return A new instance of fragment FragmentTopicsShow.
     */
    public static FragmentTopicsShow newInstance(int themeId) {
        FragmentTopicsShow fragment = new FragmentTopicsShow();
        Bundle args = new Bundle();
        args.putInt(KEY_THEME_ID, themeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: " + this);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            themeId = getArguments().getInt(KEY_THEME_ID, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_topics_show, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(new BaseAdapter<Question>(listQuestion) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.listitem_topic, null);
                }
                TextView textview = (TextView) convertView.findViewById(R.id.textview);
                textview.setText(getItem(position).Name);
                return convertView;
            }
        });
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("id", themeId);
        HttpUtil.post(NetworkUtil.themeGetById, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<Theme> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<Theme>>() {}.getType());
                if (resp.code == 200) {
                    Theme info = resp.info;
                    tv_name.setText(info.Name);
                    listQuestion.addAll(info.Questions);
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: " + this);
    }
}
