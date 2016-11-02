package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityDocsDone;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 已下载，要求显示所有已经下载的文件夹
 */
public class FragmentHaveDownloaded extends Fragment {

    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_have_downloaded, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Folder item = (Folder) parent.getAdapter().getItem(position);
                ActivityDocsDone.start(getActivity(), item);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Folder> folders = ChineseChat.database().folderSelectListWithDocsCount();
        List<Folder> data = new ArrayList<>();
        for (Folder f : folders) {
            if (f.DocsCount > 0) {
                data.add(f);
            }
        }
        BaseAdapter<Folder> baseAdapter = new BaseAdapter<Folder>(data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Folder item = getItem(position);
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.listitem_have_downloaded, null);
                    new ViewHolder(convertView);
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.tv_text1.setText(item.Name);
                holder.tv_text2.setText(String.format("课程：%1$d", item.DocsCount));
                return convertView;
            }
        };
        listView.setAdapter(baseAdapter);
    }

    private class ViewHolder {
        private TextView tv_text1;
        private TextView tv_text2;

        public ViewHolder(View convertView) {
            tv_text1 = (TextView) convertView.findViewById(R.id.tv_text1);
            tv_text2 = (TextView) convertView.findViewById(R.id.tv_text2);
            convertView.setTag(this);
        }
    }
}
