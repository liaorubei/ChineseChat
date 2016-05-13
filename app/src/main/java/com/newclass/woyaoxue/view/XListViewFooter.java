/**
 * @file XFooterView.java
 * @create Mar 31, 2012 9:33:43 PM
 * @author Maxwin
 * @description XListView's footer
 */
package com.newclass.woyaoxue.view;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.voc.woyaoxue.R;

public class XListViewFooter extends LinearLayout {
    public final static int STATE_READY = 1;// 已经准备好了,如果松开就会请求数据,并加载更多
    public final static int STATE_LOADING = 2;// 正在加载中,如果加载完了,会显示正常
    public final static int STATE_NOMORE = 4;// 数据到底了,没有更多数据了
    public final static int STATE_NORMAL = 0;// 已经加载完毕,一切正常
    public static final int STATE_ERRORS = 5;// 表明连接出错了

    private int mState;

    private View mContentView;

    private TextView mHintView;
    private View mProgressBar;

    public XListViewFooter(Context context) {
        super(context);
        LinearLayout view = (LinearLayout) View.inflate(context, R.layout.xlistview_footer, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.addView(view, params);
        mContentView = view.findViewById(R.id.xlistview_footer_content);
        mProgressBar = view.findViewById(R.id.xlistview_footer_progressbar);
        mHintView = (TextView) view.findViewById(R.id.xlistview_footer_hint_textview);
    }

    public int getBottomMargin() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        return lp.bottomMargin;
    }

    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        lp.height = 0;
        mContentView.setLayoutParams(lp);
    }

    /**
     * loading status
     */
    /*
     * public void loading() { mHintView.setVisibility(View.GONE); mProgressBar.setVisibility(View.VISIBLE); }
	 */

    /**
     * normal status
     */
	/*
	 * public void normal() { mHintView.setVisibility(View.VISIBLE); mProgressBar.setVisibility(View.GONE); }
	 */
    public void setBottomMargin(int height) {
        if (height < 0)
            return;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        lp.bottomMargin = height;
        mContentView.setLayoutParams(lp);
    }

    public void setState(int state) {
        this.mState = state;
        mHintView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        if (state == STATE_READY) {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.xlistview_footer_hint_ready);
        } else if (state == STATE_LOADING) {
            mProgressBar.setVisibility(View.VISIBLE);
            mHintView.setVisibility(View.INVISIBLE);
        } else if (state == STATE_NOMORE) {
            mHintView.setText(R.string.xlistview_footer_hint_nomore);
            mHintView.setVisibility(View.VISIBLE);
        } else {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.xlistview_footer_hint_normal);
        }
    }

    public int getState() {
        return this.mState;
    }

    /**
     * show footer
     */
    public void show() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        mContentView.setLayoutParams(lp);
    }

}
