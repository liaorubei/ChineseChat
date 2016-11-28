package com.hanwen.chinesechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.database.Database;
import com.hanwen.chinesechat.util.CommonUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActivityHaveDownloaded extends Activity implements View.OnClickListener {

    private static final String TAG = "ActivityHaveDownloaded";
    private ExpandableListView listView;
    private List<Group> groups = new ArrayList<>();
    private ImageView iv_menu;
    private View float_delete;
    private BaseExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_have_downloaded);
        Intent intent = getIntent();
        ArrayList<Integer> folderIds = intent.getIntegerArrayListExtra("FolderIds");

        for (Integer id : folderIds) {
            Group group = new Group();
            Database database = ChineseChat.database();
            group.folder = database.folderGetById(id);
            group.Childs = new ArrayList<>();
            List<Document> documents = database.docsGetDownloadedListByFolderId(group.folder.Id);
            for (Document d : documents) {
                Child child = new Child();
                child.Document = d;
                group.Childs.add(child);
            }
            groups.add(group);
        }

        findViewById(R.id.iv_home).setOnClickListener(this);
        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(this);
        float_delete = findViewById(R.id.iv_delete);
        float_delete.setOnClickListener(this);

        listView = (ExpandableListView) findViewById(R.id.listView);
        adapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                return groups.size();
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                return groups.get(groupPosition).Childs.size();
            }

            @Override
            public Object getGroup(int groupPosition) {
                return groups.get(groupPosition);
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return groups.get(groupPosition).Childs.get(childPosition);
            }

            @Override
            public long getGroupId(int groupPosition) {
                return groups.get(groupPosition).folder.Id;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return groups.get(groupPosition).Childs.get(childPosition).Document.Id;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                Group group = (Group) getGroup(groupPosition);
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_have_download_folder, parent, false);
                }
                ImageView iv_cover = (ImageView) convertView.findViewById(R.id.iv_cover);
                CommonUtil.showIcon(getApplicationContext(), iv_cover, group.folder.Cover);

                TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                TextView tv_name_en = (TextView) convertView.findViewById(R.id.tv_name_en);
                TextView tv_name_sub_cn = (TextView) convertView.findViewById(R.id.tv_name_sub_cn);

                tv_name.setText(group.folder.Name);
                tv_name_en.setText(group.folder.NameEn);
                tv_name_en.setVisibility(TextUtils.isEmpty(group.folder.NameEn) ? View.GONE : View.VISIBLE);
                tv_name_sub_cn.setText(group.folder.NameSubCn);
                tv_name_sub_cn.setVisibility(TextUtils.isEmpty(group.folder.NameSubCn) ? View.GONE : View.VISIBLE);

                View iv_delete = convertView.findViewById(R.id.iv_delete);
                iv_delete.setVisibility(group.visible ? View.VISIBLE : View.INVISIBLE);

                int selected = 0;
                for (Child c : group.Childs) {
                    if (c.isSelected()) {
                        selected++;
                    }
                }
                iv_delete.setSelected(group.Childs.size() == selected);
                iv_delete.setTag(group);
                iv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Group tag = (Group) v.getTag();
                        tag.setSelected(!tag.isSelected());
                        for (Child c : tag.Childs) {
                            c.setSelected(tag.isSelected());
                        }
                        notifyDataSetChanged();
                        float_delete.setEnabled(false);
                        for (Group g : groups) {
                            for (Child c : g.Childs) {
                                if (c.isSelected()) {
                                    float_delete.setEnabled(true);
                                    return;
                                }
                            }
                        }

                    }
                });
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                Child child = (Child) getChild(groupPosition, childPosition);
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_have_download_document, parent, false);
                }
                View ll_name = convertView.findViewById(R.id.ll_name);
                ll_name.setTag(child);
                ll_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Child tag = (Child) v.getTag();
                        Intent play = new Intent(getApplicationContext(), ActivityPlay.class);
                        play.putExtra("Id", tag.Document.Id);
                        play.putExtra("mode", "Offline");
                        startActivity(play);
                    }
                });
                TextView tv_title_cn = (TextView) convertView.findViewById(R.id.tv_title_cn);
                TextView tv_title_en = (TextView) convertView.findViewById(R.id.tv_title_en);
                TextView tv_title_sub_cn = (TextView) convertView.findViewById(R.id.tv_title_sub_cn);

                tv_title_cn.setText(child.Document.TitleCn);
                tv_title_en.setText(child.Document.TitleEn);
                tv_title_en.setVisibility(TextUtils.isEmpty(child.Document.TitleEn) ? View.GONE : View.VISIBLE);
                tv_title_sub_cn.setText(child.Document.TitleSubCn);
                tv_title_sub_cn.setVisibility(TextUtils.isEmpty(child.Document.TitleSubCn) ? View.GONE : View.VISIBLE);

                View iv_delete = convertView.findViewById(R.id.iv_delete);
                iv_delete.setVisibility(child.visible ? View.VISIBLE : View.INVISIBLE);
                iv_delete.setSelected(child.isSelected());
                iv_delete.setTag(child);
                iv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Child tag = (Child) v.getTag();
                        tag.setSelected(!tag.isSelected());
                        notifyDataSetChanged();

                        float_delete.setEnabled(false);
                        for (Group g : groups) {
                            for (Child c : g.Childs) {
                                if (c.isSelected()) {
                                    float_delete.setEnabled(true);
                                    return;
                                }
                            }
                        }
                    }
                });


                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return false;
            }
        };
        listView.setAdapter(adapter);
        int groupCount = adapter.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            listView.expandGroup(i);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                finish();
                break;
            case R.id.iv_menu:
                iv_menu.setSelected(!iv_menu.isSelected());
                if (iv_menu.isSelected()) {
                    float_delete.setVisibility(View.VISIBLE);
                    for (Group g : groups) {
                        g.visible = true;
                        g.setSelected(false);
                        for (Child c : g.Childs) {
                            c.visible = true;
                            c.setSelected(false);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    float_delete.setVisibility(View.INVISIBLE);
                    for (Group g : groups) {
                        g.visible = false;
                        g.setSelected(false);
                        for (Child c : g.Childs) {
                            c.visible = false;
                            c.setSelected(false);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                float_delete.setEnabled(false);
                break;
            case R.id.iv_delete:
                List<Group> dg = new ArrayList<>();
                for (Group g : groups) {
                    List<Child> dc = new ArrayList<>();
                    for (Child c : g.Childs) {
                        if (c.isSelected()) {
                            dc.add(c);
                            ChineseChat.database().docsDeleteById(c.Document.Id);
                        }
                    }
                    g.Childs.removeAll(dc);
                    if (g.Childs.size() == 0) {
                        dg.add(g);
                    }
                }
                groups.removeAll(dg);
                adapter.notifyDataSetChanged();
                break;
        }
    }

    private class Group {
        private boolean selected = false;
        public Folder folder;
        public List<Child> Childs;
        public boolean visible = false;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    private class Child {
        private boolean selected = false;
        public Document Document;
        public boolean visible = false;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
}
