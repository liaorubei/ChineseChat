package com.newclass.woyaoxue.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.newclass.woyaoxue.MyApplication;
import com.newclass.woyaoxue.activity.ActivityDocsDone;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

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

        List<Folder> folders = MyApplication.getDatabase().folderSelectListWithDocsCount();
        list.clear();
        for (Folder folder : folders) {
            if (folder.DocsCount > 0) {
                list.add(folder);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_folder_done, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
            Folder item = getItem(position);
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listitem_folder, null);
                ViewHolder holder = new ViewHolder();
                holder.tv_counts = (TextView) convertView.findViewById(R.id.tv_counts);
                holder.tv_folder = (TextView) convertView.findViewById(R.id.tv_folder);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.tv_folder.setText(item.Name);
            holder.tv_counts.setText("课程:" + item.DocsCount);
            return convertView;
        }
    }


    public class ViewHolder {
        public TextView tv_counts;
        public TextView tv_folder;
    }
}
