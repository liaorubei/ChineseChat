package com.hanwen.chinesechat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeRefreshTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;
import com.hanwen.chinesechat.R;

public class RefreshHeaderView extends TextView implements SwipeRefreshTrigger, SwipeTrigger {

    public RefreshHeaderView(Context context) {
        super(context);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onRefresh() {
        setText(R.string.xlistview_header_hint_loading);
    }

    @Override
    public void onPrepare() {
        setText("");
    }

    @Override
    public void onMove(int yScrolled, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            if (yScrolled >= getHeight()) {
                setText(R.string.xlistview_header_hint_ready);
            } else {
                setText(R.string.xlistview_header_hint_normal);
            }
        } else {
            setText(R.string.xlistview_header_hint_loading);
        }
    }

    @Override
    public void onRelease() {
    }

    @Override
    public void onComplete() {
        setText(R.string.xlistview_header_hint_loading);
    }

    @Override
    public void onReset() {
        setText("");
    }
}
