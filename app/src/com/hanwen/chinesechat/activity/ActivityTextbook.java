package com.hanwen.chinesechat.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;

import android.view.View;
import android.widget.TextView;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.bean.FolderDoc;
import com.hanwen.chinesechat.fragment.FragmentChatCourse;
import com.hanwen.chinesechat.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityTextbook extends Activity implements FragmentChatCourse.InteractionListener, View.OnClickListener {

    private static final String TAG = "ActivityTextbook";
    private View iv_menu;
    private Document document;
    private FolderDoc currentDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook);
        iv_menu = findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(this);
        Level level = ChineseChat.database().levelGetByName("TEXTBOOK");
        Fragment fragment = new FragmentChatCourse();
        Bundle args = new Bundle();
        args.putInt(FragmentChatCourse.KEY_PARAMS_ID, level.Id);
        args.putInt(FragmentChatCourse.KEY_OPEN_MODE, FragmentChatCourse.OPEN_MODE_LEVEL);
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fl_content, fragment, level.Name);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(FolderDoc item) {
        iv_menu.setVisibility(item != null && item.selected ? View.VISIBLE : View.INVISIBLE);
        this.currentDocument = item;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                if (currentDocument != null) {
                    Intent data = new Intent();
                    data.putExtra("documentId", currentDocument.Id);
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
                break;
        }
    }
}
