package com.newclass.woyaoxue.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.DbUtils;
import com.newclass.woyaoxue.ActivityListDelte;
import com.newclass.woyaoxue.ListViewCompat;
import com.newclass.woyaoxue.SlideView;
import com.newclass.woyaoxue.activity.ActivityDocsDone;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹--已下载
 */
public class FragmentFolderDone extends Fragment {

    private static final String TAG = "FragmentFolderDone";

    private ListViewCompat listview;
    private Database database;
    private ArrayList<ViewHolder> list;
    private MyAdapter adapter;
    private SwipeRefreshLayout srl;
    private SlideView mLastSlideViewWithStatusOn;

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        Log.i(TAG, "onResume: " + list.size() + " list=" + list);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.closeConnection();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_folder_done, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        database = new Database(getActivity());
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                srl.setRefreshing(false);
            }
        });
        listview = (ListViewCompat) view.findViewById(android.R.id.list);
        list = new ArrayList<>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActivityDocsDone.start(getActivity(), list.get(position).folder);
            }
        });


    }

    private void refresh() {
        List<Folder> folders = database.folderSelectListWithDocsCount();
        list.clear();
        for (Folder folder : folders) {
            if (folder.DocsCount > 0) {
                ViewHolder holder = new ViewHolder();
                holder.folder = folder;
                list.add(holder);
            }
        }
        adapter.notifyDataSetChanged();
    }


    private class MyAdapter extends BaseAdapter<ViewHolder> implements SlideView.OnSlideListener {

        public MyAdapter(List<ViewHolder> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SlideView slideView = (SlideView) convertView;
            if (slideView == null) {
                View inflate = getActivity().getLayoutInflater().inflate(R.layout.listitem_folder_done, null);
                slideView = new SlideView(getActivity());
                slideView.setContentView(inflate);
                slideView.setOnSlideListener(this);
            }

            slideView.shrink();
            ViewHolder item = getItem(position);
            item.slideView = slideView;
            return slideView;
        }

        @Override
        public void onSlide(View view, int status) {
            if (mLastSlideViewWithStatusOn != null && mLastSlideViewWithStatusOn != view) {
                mLastSlideViewWithStatusOn.shrink();
            }

            if (status == SLIDE_STATUS_ON) {
                mLastSlideViewWithStatusOn = (SlideView) view;
            }



        }
    }


    public class ViewHolder {
        Folder folder;
        public SlideView slideView;
        public CheckBox cb_delete;
        public TextView tv_counts;
        public TextView tv_folder;
    }
}
