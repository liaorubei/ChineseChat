package com.hanwen.chinesechat.fragment;

import android.content.Intent;
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
import com.hanwen.chinesechat.activity.ActivityHaveDownloaded;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 已下载，要求显示所有已经下载的文件夹
 */
public class FragmentHaveDownloaded extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentHaveDownloaded";
    private TextView tv_count_course;
    private TextView tv_count_levels;
    private ArrayList<Integer> idsCourse = new ArrayList<>();
    private ArrayList<Integer> idsLevels = new ArrayList<>();
    private int sumCourse = 0;
    private int sumLevels = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_have_downloaded, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.rl_course).setOnClickListener(this);
        view.findViewById(R.id.rl_levels).setOnClickListener(this);
        tv_count_course = (TextView) view.findViewById(R.id.tv_count_course);
        tv_count_levels = (TextView) view.findViewById(R.id.tv_count_levels);
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Folder> folders = ChineseChat.database().folderSelectListWithDocsCount();
        idsCourse.clear();
        idsLevels.clear();
        sumCourse = 0;
        sumLevels = 0;

        for (Folder f : folders) {
            if (f.LevelId == 8) {
                idsCourse.add(f.Id);
                sumCourse += f.DocsCount;
            } else {
                idsLevels.add(f.Id);
                sumLevels += f.DocsCount;
            }
        }
        tv_count_course.setText(sumCourse + "");
        tv_count_levels.setText(sumLevels + "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_course:
            {    Intent intent = new Intent(getContext(), ActivityHaveDownloaded.class);
                intent.putExtra("FolderIds", idsCourse);
                startActivity(intent);}
                break;
            case R.id.rl_levels:
            {   Intent intent = new Intent(getContext(), ActivityHaveDownloaded.class);
                intent.putExtra("FolderIds", idsLevels);
                startActivity(intent);}
                break;
        }

    }
}
