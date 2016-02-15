package com.newclass.woyaoxue.fragment;

import com.newclass.woyaoxue.activity.DownDocsActivity;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.FolderUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.view.ContentViewDownload;
import com.voc.woyaoxue.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FragmentDownload extends Fragment implements View.OnClickListener {

    private static final String TAG = "FragmentDownload";
    private TextView tv_cancel;
    private TextView tv_delete;
    private CheckBox cb_Invert;
    private CheckBox cb_select;
    private View ll_ctrl;
    private ListView listview;
    private Database database;
    private ArrayList<ViewHelper> list;
    private MyAdapter adapter;

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        List<Folder> folders = database.folderSelectListWithDocsCount();
        list.clear();
        for (Folder folder : folders) {
            if (folder.DocsCount > 0) {
                list.add(new ViewHelper(folder, false, false));
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        database.closeConnection();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu: ");
        menu.add(Menu.NONE, 1, 1, "删除").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            ll_ctrl.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.activity_down, null);
        listview = (ListView) view.findViewById(R.id.listview);

        ll_ctrl = view.findViewById(R.id.ll_ctrl);
        cb_select = (CheckBox) view.findViewById(R.id.cb_select);
        cb_Invert = (CheckBox) view.findViewById(R.id.cb_Invert);
        tv_delete = (TextView) view.findViewById(R.id.tv_delete);
        tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);

        cb_select.setOnClickListener(this);
        cb_Invert.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);

        database = new Database(getActivity());


        list = new ArrayList<ViewHelper>();


        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("点击了3");
                Intent intent = new Intent(getActivity(), DownDocsActivity.class);
                intent.putExtra("FolderId", list.get(position).folder.Id);
                intent.putExtra("FolderName", list.get(position).folder.Name);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_select:
                cb_Invert.setChecked(false);
                for (ViewHelper i : list) {
                    i.isChecked = cb_select.isChecked();
                    i.isVisible = true;
                }
                adapter.notifyDataSetChanged();
                break;

            case R.id.cb_Invert:
                cb_Invert.setChecked(true);
                cb_select.setChecked(false);
                for (ViewHelper i : list) {
                    i.isChecked = !i.isChecked;
                    i.isVisible = true;
                }
                adapter.notifyDataSetChanged();

                break;

            case R.id.tv_delete:
                List<ViewHelper> removeList = new ArrayList<ViewHelper>();// 被删除的集合

                for (ViewHelper viewHelper : list) {
                    if (viewHelper.isChecked) {
                        removeList.add(viewHelper);// 把被删除的对象收集到一个集合中
                    }
                }

                list.removeAll(removeList);

/*                // 清除数据库及文件夹里面的数据
                for (ViewHelper viewHelper : removeList) {
                    // 从数据库移除
                    database.docsDeleteById(viewHelper.document.Id);

                    // 从文件夹移除
                    File file = new File(FolderUtil.rootDir(getActivity()), viewHelper.document.SoundPath);
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                }*/

                adapter.notifyDataSetChanged();
                break;

            case R.id.tv_cancel:
                // 按钮重置
                cb_select.setChecked(false);
                cb_Invert.setChecked(false);
                ll_ctrl.setVisibility(View.GONE);

                for (ViewHelper i : list) {
                    i.isChecked = false;
                    i.isVisible = false;
                }
                adapter.notifyDataSetChanged();
                break;
        }
    }

    private class MyAdapter extends BaseAdapter<ViewHelper> {

        public MyAdapter(List<ViewHelper> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHelper item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.listitem_folder, null);
                ViewHolder holder = new ViewHolder();
                holder.cb_delete = (CheckBox) convertView.findViewById(R.id.cb_delete);
                holder.tv_folder = (TextView) convertView.findViewById(R.id.tv_folder);
                holder.tv_counts = (TextView) convertView.findViewById(R.id.tv_counts);
                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.cb_delete.setVisibility(item.isVisible ? View.VISIBLE : View.GONE);
            holder.cb_delete.setChecked(item.isChecked);
            holder.cb_delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    item.isChecked = ((CheckBox) v).isChecked();
                }
            });
            holder.tv_folder.setText(item.folder.Name);
            holder.tv_counts.setText("课程:" + item.folder.DocsCount);
            return convertView;
        }
    }

    private class ViewHelper {
        public Folder folder;
        public boolean isChecked;
        public boolean isVisible;

        public ViewHelper(Folder pFolder, boolean checked, boolean visible) {
            this.folder = pFolder;
            this.isChecked = checked;
            this.isVisible = visible;
        }
    }

    private class ViewHolder {
        public CheckBox cb_delete;
        public TextView tv_counts;
        public TextView tv_folder;
    }
}
