package com.hanwen.chinesechat.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityChat;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.NimSysNotice;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
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
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentCourseNestThird#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentCourseNestThird extends Fragment implements View.OnClickListener {
    private static final String KEY_FOLDER = "KEY_FOLDER";
    private static final String TAG = "FragmentCourseNestThird";
    private Folder folder;
    private List<Document> data = new ArrayList<>();
    private Adapter adapter = new Adapter(data);

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param folder Parameter 1.
     * @return A new instance of fragment FragmentCourseNestThird.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentCourseNestThird newInstance(Folder folder) {
        FragmentCourseNestThird fragment = new FragmentCourseNestThird();
        Bundle args = new Bundle();
        args.putParcelable(KEY_FOLDER, folder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.folder = getArguments().getParcelable(KEY_FOLDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_nest_third, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_name.setText(folder.Name);
        view.findViewById(R.id.iv_back).setOnClickListener(this);
        View iv_pick = view.findViewById(R.id.iv_pick);
        iv_pick.setOnClickListener(this);
        iv_pick.setVisibility(View.INVISIBLE);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        data.clear();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //通知对方
                Document document = data.get(position);
                JsonObject object = new JsonObject();
                object.addProperty("type", NimSysNotice.NOTICE_TYPE_COURSE);
                object.addProperty("info", document.Id);

                ActivityChat activity = (ActivityChat) getActivity();
                CustomNotification notification = new CustomNotification();
                notification.setSessionId(activity.chatData.getAccount());
                notification.setSessionType(SessionTypeEnum.P2P);
                notification.setContent(object.toString());
                NIMClient.getService(MsgService.class).sendCustomNotification(notification);

                //本地显示
                FragmentCourseShow fragmentCourseShow = FragmentCourseShow.newInstance(document.Id, true);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fl_content, fragmentCourseShow, document.Title);
                fragmentTransaction.addToBackStack(document.Title).commit();
            }
        });
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("folderId", folder.Id);
        HttpUtil.post(NetworkUtil.documentGetListByFolderIdWithoutCheck, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<Document>> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Document>>>() {}.getType());
                for (Document d : resp.info) {
                    data.add(d);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_pick:

                break;
        }
    }

    private class Adapter extends BaseAdapter<Document> {
        public Adapter(List<Document> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Document item = getItem(position);
            View inflate = View.inflate(getContext(), R.layout.listitem_chat_folder, null);
            inflate.findViewById(R.id.iv_cover).setVisibility(View.GONE);
            TextView tv_name = (TextView) inflate.findViewById(R.id.tv_name);
            TextView tv_count = (TextView) inflate.findViewById(R.id.tv_count);
            TextView tv_title_sub = (TextView) inflate.findViewById(R.id.tv_title_sub);
            tv_title_sub.setVisibility(View.VISIBLE);
            tv_title_sub.setText(item.TitleSubCn);
            tv_name.setText(item.TitleCn);
            tv_count.setText(item.TitleEn);
            return inflate;
        }
    }
}
