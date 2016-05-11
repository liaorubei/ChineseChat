package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Document;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的下载--文档列表显示界面
 *
 * @author liaorubei
 */
public class ActivityDocsDone extends Activity implements OnClickListener {
    private static final String TAG = "ActivityDocument";
    private List<ViewHelper> list;
    private int folderId;
    private MyAdapter adapter;
    private Database database;
    private ListView listview;
    private int levelId;
    protected RelativeLayout tv_folder;
    private ImageView iv_delete;
    private boolean deleteMode = false;
    private ImageView cb_delete;
    private TextView tv_name;
    private View iv_menu;

    public static void start(Context context, Folder folder) {
        Intent intent = new Intent(context, ActivityDocsDone.class);
        intent.putExtra("FolderId", folder.Id);
        intent.putExtra("FolderName", folder.Name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docsdone);

        initView();

        database = new Database(this);

        // 取得传递过来的数据
        Intent intent = getIntent();
        levelId = intent.getIntExtra("LevelId", 0);
        folderId = intent.getIntExtra("FolderId", 16);
        String folderName = intent.getStringExtra("FolderName");
        tv_name.setText(folderName);

        list = new ArrayList<ViewHelper>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
        // 其他设置
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ActivityDocsDone.this, ActivityPlay.class);
                intent.putExtra("Id", list.get(position).document.Id);
                intent.putExtra("mode", "Offline");
                startActivity(intent);
            }
        });
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }


        loadMore();
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);
        tv_folder = (RelativeLayout) findViewById(R.id.tv_folder);
        tv_name = (TextView) findViewById(R.id.tv_name);
        listview = (ListView) findViewById(R.id.listview);
        iv_menu = findViewById(R.id.iv_menu);

        iv_menu.setOnClickListener(this);

        //tv_ctrl = (TextView) findViewById(R.id.tv_ctrl);
        //tv_ctrl.setOnClickListener(this);

        iv_delete = (ImageView) findViewById(R.id.iv_delete);
        iv_delete.setVisibility(View.INVISIBLE);
        iv_delete.setOnClickListener(this);

        cb_delete = (ImageView) findViewById(R.id.cb_delete);
        cb_delete.setVisibility(View.INVISIBLE);
        cb_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                for (ViewHelper h : list) {
                    h.isChecked = v.isSelected();
                }
                adapter.notifyDataSetChanged();
                iv_delete.setImageResource(v.isSelected() ? R.drawable.dustbin_checked : R.drawable.dustbin_uncheck);
            }
        });

/*        cb_delete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (ViewHelper h : list) {
                    h.isChecked = isChecked;
                }
                adapter.notifyDataSetChanged();
                iv_delete.setImageResource(isChecked ? R.drawable.dustbin_checked : R.drawable.dustbin_uncheck);
            }
        });*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case 1:
                for (ViewHelper i : list) {
                    i.isShow = true;
                }
                adapter.notifyDataSetChanged();

                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater menuInflater = getMenuInflater();
        //menuInflater.inflate(R.menu.menu_downdocs, menu);
        menu.add(Menu.NONE, 1, 1, "删除").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.closeConnection();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.iv_menu:
                deleteMode = !deleteMode;

                for (ViewHelper i : list) {
                    i.isShow = deleteMode;
                }

                iv_menu.setSelected(deleteMode);
                iv_delete.setVisibility(deleteMode ? View.VISIBLE : View.INVISIBLE);
                cb_delete.setVisibility(deleteMode ? View.VISIBLE : View.INVISIBLE);
                adapter.notifyDataSetChanged();
                break;
            case R.id.iv_delete:
                List<ViewHelper> removeList = new ArrayList<ViewHelper>();// 被删除的集合

                for (ViewHelper viewHelper : list) {
                    if (viewHelper.isChecked) {
                        removeList.add(viewHelper);// 把被删除的对象收集到一个集合中
                    }
                }

                list.removeAll(removeList);

                // 清除数据库及文件夹里面的数据
                for (ViewHelper viewHelper : removeList) {
                    // 从数据库移除
                    database.docsDeleteById(viewHelper.document.Id);

                    // 从文件夹移除
                    File file = new File(getFilesDir(), viewHelper.document.SoundPath);
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                    Log.i("" + viewHelper.document.Title + " " + file.getAbsolutePath() + " 被移除了");
                }

                adapter.notifyDataSetChanged();

                //tv_ctrl.setText("删除");
                deleteMode = false;
                cb_delete.setVisibility(View.INVISIBLE);
                iv_delete.setVisibility(View.INVISIBLE);
                break;

        }
    }

    private void loadMore() {
        new AsyncTask<Integer, Integer, List<Document>>() {

            @Override
            protected List<Document> doInBackground(Integer... params) {
                return database.docsSelectListByFolderId(params[0]);
            }

            protected void onPostExecute(List<Document> result) {
                for (Document document : result) {
                    list.add(new ViewHelper(document, false, false));
                }
                adapter.notifyDataSetChanged();
            }
        }.execute(folderId);
    }

    private class MyAdapter extends BaseAdapter<ViewHelper> {

        public MyAdapter(List<ViewHelper> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHelper item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(ActivityDocsDone.this, R.layout.listitem_downdocs, null);
                ViewHolder holder = new ViewHolder();
                holder.tv_title_one = (TextView) convertView.findViewById(R.id.tv_title_one);
                holder.tv_title_two = (TextView) convertView.findViewById(R.id.tv_title_two);
                holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
                holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
                holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

                holder.cb_delete = (ImageView) convertView.findViewById(R.id.cb_delete);
                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.tv_title_one.setText(item.document.Title);
            holder.tv_title_two.setText(item.document.TitleTwo);
            holder.tv_date.setText(item.document.DateString);
            holder.tv_size.setText(Formatter.formatFileSize(ActivityDocsDone.this, item.document.Length));
            holder.tv_time.setText(item.document.LengthString);

            holder.cb_delete.setSelected(item.isChecked);
            holder.cb_delete.setVisibility(item.isShow ? View.VISIBLE : View.GONE);
            holder.cb_delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    item.isChecked = v.isSelected();
                    int c = 0;
                    for (ViewHelper h : list) {
                        if (h.isChecked) {
                            c++;
                        }
                    }
                    iv_delete.setImageResource(c > 0 ? R.drawable.dustbin_checked : R.drawable.dustbin_uncheck);

                    //全选按钮
                    cb_delete.setSelected(c == list.size());
                }
            });

            return convertView;
        }

    }

    private class ViewHolder {
        public TextView tv_title_one;
        public TextView tv_title_two;
        public TextView tv_date;
        public TextView tv_size;
        public TextView tv_time;
        public ImageView cb_delete;
    }

    private class ViewHelper {
        public ViewHelper(Document doc, boolean check, boolean show) {
            this.document = doc;
            this.isChecked = check;
            this.isShow = show;
        }

        public boolean isShow;
        public boolean isChecked;
        public Document document;
    }
}
