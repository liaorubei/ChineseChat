package com.hanwen.chinesechat.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.FolderDoc;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.database.Database;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 本界面为通话过程中选择课文的界面，有三种打开方式
 * 1。从TEXTBOOK取得文件夹列表
 * 2。从父文件夹取得子文件夹列表
 * 3。从文件夹取得文档列表
 */
public class FragmentChatCourse extends Fragment {

    private static final String TAG = "FragmentChatCourse";
    public static final java.lang.String KEY_OPEN_MODE = "KEY_OPEN_MODE";
    public static final java.lang.String KEY_PARAMS_ID = "KEY_PARAMS_ID";
    public static final java.lang.String KEY_OPEN_NAME = "KEY_OPEN_NAME";
    public static final int OPEN_MODE_LEVEL = 0;
    public static final int OPEN_MODE_FOLDER = 1;
    public static final int OPEN_MODE_PARENT = 2;
    private Level level;
    private Folder folder;
    private Folder parent;
    private ListView listView;
    private BaseAdapter<FolderDoc> adapter;
    private List<FolderDoc> data = new ArrayList<>();
    private InteractionListener mInteractionListener;

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: ");
        if (context instanceof InteractionListener) {
            this.mInteractionListener = (InteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement InteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_course, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mInteractionListener.onFragmentInteraction(null);

        int open_mode = getArguments().getInt(KEY_OPEN_MODE, 0);
        int params_id = getArguments().getInt(KEY_PARAMS_ID, 0);
        if (open_mode == OPEN_MODE_LEVEL) {
            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("levelId", params_id);
            HttpUtil.post(NetworkUtil.folderGetListByLevelId, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Response<List<Folder>> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Folder>>>() {}.getType());
                    for (Folder f : resp.info) {
                        data.add(new FolderDoc(f));
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(HttpException error, String msg) {

                }
            });
        }

        if (open_mode == OPEN_MODE_PARENT) {
            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("folderId", params_id);
            HttpUtil.post(NetworkUtil.folderGetChildListByParentId, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Response<List<Folder>> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Folder>>>() {}.getType());
                    for (Folder f : resp.info) {
                        data.add(new FolderDoc(f));
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(HttpException error, String msg) {

                }
            });
        }

        if (open_mode == OPEN_MODE_FOLDER) {
            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("folderId", params_id);
            params.add("userId", ChineseChat.CurrentUser.Id);
            HttpUtil.post(NetworkUtil.documentGetListByFolderId, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Response<List<Document>> resp = new GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(responseInfo.result, new TypeToken<Response<List<Document>>>() {}.getType());
                    for (Document doc : resp.info) {
                        FolderDoc object = new FolderDoc();
                        object.Id = doc.Id;
                        object.Name1 = doc.Title;
                        //object.Sort=doc.s
                        object.isFolder = false;
                        object.HasChildren = false;
                        data.add(object);
                    }

                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(HttpException error, String msg) {

                }
            });
        }

        listView = (ListView) view.findViewById(R.id.listView);
        data.clear();
        adapter = new BaseAdapter<FolderDoc>(data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = new TextView(getActivity());
                textView.setPadding(25, 25, 25, 25);
                textView.setText(getItem(position).Name1);
                return textView;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FolderDoc item = (FolderDoc) parent.getAdapter().getItem(position);
                if (item.isFolder) {
                    Fragment fragment = new FragmentChatCourse();
                    Bundle args = new Bundle();
                    args.putInt(KEY_PARAMS_ID, item.Id);
                    args.putInt(KEY_OPEN_MODE, item.HasChildren ? OPEN_MODE_PARENT : OPEN_MODE_FOLDER);
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fl_content, fragment, item.Name1);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    item.selected = !item.selected;
                    mInteractionListener.onFragmentInteraction(item);
                }
            }
        });
    }

    public interface InteractionListener {
        void onFragmentInteraction(FolderDoc item);
    }
}
