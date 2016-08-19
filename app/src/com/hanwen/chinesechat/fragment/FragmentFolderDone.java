package com.hanwen.chinesechat.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.activity.ActivityDocsDone;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹--已下载
 */
public class FragmentFolderDone extends Fragment {
    private static final String TAG = "FragmentFolderDone";
    private ListView listview;
    private ArrayList<Folder> list = new ArrayList<Folder>();
    private MyAdapter adapter = new MyAdapter(list);


    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: " + list.size() + " list=" + list);

        List<Folder> folders = ChineseChat.database().folderSelectListWithDocsCount();
        list.clear();
        for (Folder folder : folders) {
            if (folder.DocsCount > 0) {
                list.add(folder);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_folder_done, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Level level = getArguments().getParcelable("level");
        if (level.Id > 0) {
            Log.i(TAG, "onViewCreated: " + level.Folders);
            for (Folder f : level.Folders) {
                list.add(f);
            }
            adapter.notifyDataSetChanged();
        }

        listview = (ListView) view.findViewById(android.R.id.list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActivityDocsDone.start(getActivity(), list.get(position));
            }
        });
    }

    private class MyAdapter extends BaseAdapter<Folder> {

        public MyAdapter(List<Folder> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Folder item = getItem(position);
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listitem_folder, null);
                ViewHolder holder = new ViewHolder();
                holder.tv_counts = (TextView) convertView.findViewById(R.id.tv_counts);
                holder.tv_folder = (TextView) convertView.findViewById(R.id.tv_folder);
                holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.tv_folder.setText(item.Name);
            holder.tv_counts.setText("课程:" + item.DocsCount);
            holder.iv_delete.setVisibility(View.GONE);
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);//去标题
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);//去背景
                    dialog.setContentView(R.layout.dialog_delete);
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.findViewById(R.id.bt_positive).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<Document> documents = ChineseChat.database().docsSelectListByFolderId(item.Id);
                            for (Document d : documents) {
                                ChineseChat.database().docsDeleteById(d.Id);
                                File file = new File(getActivity().getFilesDir(), d.SoundPath);
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                            list.remove(item);
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    dialog.findViewById(R.id.bt_negative).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });
            return convertView;
        }
    }

    public class ViewHolder {
        public TextView tv_counts;
        public TextView tv_folder;
        public ImageView iv_delete;
    }
}
