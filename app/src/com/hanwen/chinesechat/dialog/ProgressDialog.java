package com.hanwen.chinesechat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.hanwen.chinesechat.R;

/**
 * Created by ChineseChat on 2016/12/22.
 * 创建一个只有一个进度圈圈的进度对话框，不用设置Message
 */
public class ProgressDialog {

    private Dialog dialog;

    public ProgressDialog(Context context) {
        View dialogView = View.inflate(context, R.layout.dialog_progress, null);
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(dialogView);
    }

    public void show() {
        dialog.show();
    }


    public void hide() {
        dialog.dismiss();
    }
}
