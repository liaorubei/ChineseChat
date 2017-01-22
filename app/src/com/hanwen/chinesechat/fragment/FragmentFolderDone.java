package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityDocsDone;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹--已下载，课本-课程方式
 */
public class FragmentFolderDone extends Fragment {
    private static final String TAG = "FragmentFolderDone";
    private ListView listview;
    private ArrayList<Folder> list = new ArrayList<Folder>();
    private MyAdapter adapter = new MyAdapter(list);

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

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        List<Folder> folders = ChineseChat.database().folderSelectListWithDocsCount();
        list.clear();
        for (Folder folder : folders) {
            if (folder.DocsCount > 0) {
                list.add(folder);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class MyAdapter extends BaseAdapter<Folder> {

        public MyAdapter(List<Folder> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Folder item = getItem(position);
            //Log.i(TAG, "getView: " + item);
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listitem_folder_done1, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.iv_cover = (ImageView) convertView.findViewById(R.id.iv_cover);
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            CommonUtil.showIcon(getContext(), holder.iv_cover, item.Cover);
            holder.tv_title.setText(String.format("%1$s\n%3$s：%2$d", item.Name, item.DocsCount, item.LevelId == 6 ? "新闻" : "课程"));
            return convertView;
        }
    }

    public class ViewHolder {
        public TextView tv_title;
        public ImageView iv_cover;
    }
}
