package com.hanwen.chinesechat.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentCourseNest#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentCourseNest extends Fragment {
    private static final String KEY_GET_URL = "KEY_GET_URL";
    private static final String KEY_URL_PARAMS = "KEY_URL_PARAMS";
    private static final String KEY_URL_PARAMS_NAME = "KEY_URL_PARAMS_NAME";
    private static final String TAG = "FragmentCourseNest";


    private List<Folder> data = new ArrayList<>();
    private Adapterd adapter = new Adapterd(data);

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param url    Parameter 1.
     * @param params Parameter 2.
     * @return A new instance of fragment FragmentCourseNest.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentCourseNest newInstance(String url, int params, String paramsName) {
        FragmentCourseNest fragment = new FragmentCourseNest();
        Bundle args = new Bundle();
        args.putString(KEY_GET_URL, url);
        args.putInt(KEY_URL_PARAMS, params);
        args.putString(KEY_URL_PARAMS_NAME, paramsName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_nest, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated: ");
        GridView gridView = (GridView) view.findViewById(R.id.gridView);

        data.clear();
        adapter.notifyDataSetChanged();

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Folder folder = data.get(position);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fl_content, folder.HasChildren ? FragmentCourseNestSecond.newInstance(folder) : FragmentCourseNestThird.newInstance(folder), folder.Name);
                fragmentTransaction.addToBackStack("FragmentCourseClear").commit();
            }
        });
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("levelId", 8);
        HttpUtil.post(NetworkUtil.folderGetListByLevelId, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + this.getRequestUrl() + "   " + responseInfo.result);
                Response<List<Folder>> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Folder>>>() {}.getType());
                for (Folder f : resp.info) {
                    data.add(f);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + this.getRequestUrl() + "   " + msg);
            }
        });
    }

    private class Adapterd extends BaseAdapter<Folder> {
        public Adapterd(List<Folder> list) {
            super(list);
        }

        @Override
        public View getView(int position, View inflate, ViewGroup parent) {
            Folder item = getItem(position);
            if (inflate == null) {
                inflate = View.inflate(getActivity(), R.layout.listitem_chat_folder, null);
            }
            ImageView iv_cover = (ImageView) inflate.findViewById(R.id.iv_cover);
            CommonUtil.showIcon(getContext(), iv_cover, item.Cover);
            TextView tv_name = (TextView) inflate.findViewById(R.id.tv_name);
            tv_name.setText(item.Name);
            tv_name.setGravity(Gravity.CENTER);
            TextView tv_name_sub = (TextView) inflate.findViewById(R.id.tv_count);
            tv_name_sub.setText(item.NameSubCn);
            tv_name_sub.setGravity(Gravity.CENTER);
            //tv_name_sub.setVisibility(TextUtils.isEmpty(item.NameSubCn) ? View.GONE : View.VISIBLE);
            inflate.findViewById(R.id.tv_title_sub).setVisibility(View.GONE);
            return inflate;
        }
    }
}
