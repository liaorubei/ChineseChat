package com.newclass.woyaoxue.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newclass.woyaoxue.activity.ActivityDocsTodo;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.bean.Level;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import java.util.List;

/**
 * 文件夹-未下载
 */
public class FragmentFolderTodo extends Fragment {
    private static final String TAG = "FolderFragment";
    private MyAdapter adapter;
    private List<Folder> list;
    private ListView listview;
    private Gson gson = new Gson();
    private SwipeRefreshLayout srl;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.i(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_folder, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Level level = gson.fromJson(getArguments().getString("level"), new TypeToken<Level>() {
        }.getType());
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeColors(R.color.color_app);

        listview = (ListView) view.findViewById(android.R.id.list);
        list = level.Folders;
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Folder folder = list.get(position);
                Intent intent = new Intent(getActivity(), ActivityDocsTodo.class);
                intent.putExtra("folder", gson.toJson(folder));
                startActivity(intent);
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
                convertView = View.inflate(getActivity(), R.layout.listitem_folder, null);
                ViewHolder holder = new ViewHolder();
                holder.tv_folder = (TextView) convertView.findViewById(R.id.tv_folder);
                holder.tv_counts = (TextView) convertView.findViewById(R.id.tv_counts);
                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.tv_folder.setText(item.Name);
            holder.tv_counts.setText("课程:" + item.DocsCount);
            return convertView;
        }
    }

    private class ViewHolder {
        public TextView tv_counts;
        public TextView tv_folder;
    }
}
