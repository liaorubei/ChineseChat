package com.hanwen.chinesechat.view;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ChineseChat on 2017/2/22.
 */
public class TextViewVerify extends TextView {
    public TextViewVerify(Context context) {
        super(context);
       // this.setCompoundDrawablesWithIntrinsicBounds();
    }

    public TextViewVerify(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setText(Html.fromHtml(String.format("<font color='red'>*</font>%1$s", this.getText())));
    }


}
