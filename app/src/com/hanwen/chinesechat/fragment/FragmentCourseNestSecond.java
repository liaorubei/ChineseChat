package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Folder;
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
 * Use the {@link FragmentCourseNestSecond#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentCourseNestSecond extends Fragment {
    private static final String TAG = "FragmentCourseNestSecond";
    private static final String KEY_PARENT = "KEY_PARENT";
    private Folder parent;
    private List<Folder> data = new ArrayList<Folder>();
    private Adapter adapter = new Adapter(data);

    public FragmentCourseNestSecond() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param folder Parameter 1.
     * @return A new instance of fragment FragmentCourseNestSecond.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentCourseNestSecond newInstance(Folder folder) {
        FragmentCourseNestSecond fragment = new FragmentCourseNestSecond();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PARENT, folder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent = getArguments().getParcelable(KEY_PARENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_nest_second, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Log.i(TAG, "onViewCreated: " + parent);
        View viewById = view.findViewById(R.id.iv_back);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_name.setText(parent.Name);
        AbsListView gridView = (AbsListView) view.findViewById(R.id.gridView);
        data.clear();
        adapter.notifyDataSetChanged();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Folder folder = data.get(position);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fl_content, folder.KidsCount > 0 ? FragmentCourseNestSecond.newInstance(folder) : FragmentCourseNestThird.newInstance(folder), folder.Name);
                fragmentTransaction.addToBackStack(folder.Name).commit();
            }
        });

        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("userId", ChineseChat.CurrentUser.Id);
        params.add("parentId", parent.TargetId > 0 ? parent.TargetId : parent.Id);
        HttpUtil.post(NetworkUtil.folderGetList, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<List<Folder>> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Folder>>>() {}.getType());
                for (Folder f : resp.info) {
                    data.add(f);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    private class Adapter extends BaseAdapter<Folder> {
        public Adapter(List<Folder> list) {
            super(list);
        }

        @Override
        public View getView(int position, View inflate, ViewGroup parent) {
            Folder item = getItem(position);
            if (inflate == null) {
                inflate = View.inflate(getContext(), R.layout.listitem_activity_folder, null);
            }
            ImageView iv_cover = (ImageView) inflate.findViewById(R.id.iv_cover);
            CommonUtil.showIcon(getContext(), iv_cover, item.Cover);

            TextView tv_name = (TextView) inflate.findViewById(R.id.tv_name);
            tv_name.setText(item.Name);

            TextView tv_name_en = (TextView) inflate.findViewById(R.id.tv_name_en);
            tv_name_en.setText(item.NameEn);
            return inflate;
        }
    }
}
