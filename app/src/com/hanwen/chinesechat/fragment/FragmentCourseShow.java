package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.Lyric;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * create an instance of this fragment.
 */
public class FragmentCourseShow extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentCourseShow";
    private static final String KEY_DOCUMENT_ID = "KEY_DOCUMENT_ID";
    private static final String KEY_SHOW_BACK = "KEY_SHOW_BACK";
    private int documentId;
    private boolean showBack;

    private List<Lyric> data = new ArrayList<>();
    private Adapter adapter = new Adapter(data);
    private TextView tv_name;

    public static FragmentCourseShow newInstance(int documentId, boolean showBack) {
        FragmentCourseShow fragment = new FragmentCourseShow();
        Bundle args = new Bundle();
        args.putInt(KEY_DOCUMENT_ID, documentId);
        args.putBoolean(KEY_SHOW_BACK, showBack);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            documentId = getArguments().getInt(KEY_DOCUMENT_ID);
            showBack = getArguments().getBoolean(KEY_SHOW_BACK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_show, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        View iv_back = view.findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        iv_back.setVisibility(showBack ? View.VISIBLE : View.INVISIBLE);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        View iv_home = view.findViewById(R.id.iv_home);
        iv_home.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.INVISIBLE);
        iv_home.setOnClickListener(this);
        data.clear();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("id", documentId);
        HttpUtil.post(NetworkUtil.documentGetById, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<Document> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<Document>>() {}.getType());
                tv_name.setText(resp.info.Title);
                data.addAll(resp.info.Lyrics);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach: ");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                getFragmentManager().popBackStack("FragmentCourseClear", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getFragmentManager().beginTransaction().replace(R.id.fl_content, FragmentCourseNest.newInstance("", 1, ""), "FragmentCourseClear").addToBackStack("").commit();
                break;
            case R.id.iv_back:
                getFragmentManager().popBackStack();
                break;
        }
    }

    private class Adapter extends BaseAdapter<Lyric> {
        public Adapter(List<Lyric> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Lyric item = getItem(position);
            View inflate = View.inflate(getContext(), R.layout.listitem_chat_folder, null);
            inflate.findViewById(R.id.iv_cover).setVisibility(View.GONE);
            TextView tv_name = (TextView) inflate.findViewById(R.id.tv_name);
            tv_name.setText(item.Original);
            TextView tv_count = (TextView) inflate.findViewById(R.id.tv_count);
            tv_count.setText(item.Translate);
            return inflate;
        }
    }
}
