package com.hanwen.chinesechat.view;

import com.hanwen.chinesechat.bean.Lyric;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SpecialLyricView extends LinearLayout implements Comparable<SpecialLyricView> {
    public static final int SHOW_CN = 1;
    public static final int SHOW_EN = 2;
    public static final int SHOW_ENCN = 3;
    public static final int SHOW_NONE = 0;

    private Lyric mLyric;
    private TextView originalTextView;
    private TextView translateTextView;

    public SpecialLyricView(Context context, Lyric lyric) {
        super(context);
        this.mLyric = lyric;

        // 因为是放在线性布局中的，所以要使用LinearLayout.LayoutParams
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 25, 5, 0);
        this.setLayoutParams(params);
        this.setOrientation(LinearLayout.VERTICAL);

        originalTextView = new TextView(context);
        originalTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        originalTextView.setText(lyric.Original);
        this.addView(originalTextView);

        translateTextView = new TextView(context);
        translateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        translateTextView.setText(lyric.Translate);
        this.addView(translateTextView);
    }

    @Override
    public int compareTo(SpecialLyricView another) {
        return Integer.valueOf(this.mLyric.TimeLabel).compareTo(another.mLyric.TimeLabel);
    }

    public Integer getTimeLabel() {
        return this.mLyric.TimeLabel;
    }

    public void highlight() {
        this.originalTextView.setTextColor(Color.parseColor("#00A478"));
    }

    public void resetColor() {
        this.originalTextView.setTextColor(Color.BLACK);
    }

    /**
     * @param target SHOW_CN,SHOW_EN,SHOW_CN_EN,SHOW_NONE四个中的一个
     */
    public void showEnCn(int target) {
        switch (target) {
            case SHOW_CN:
                this.setVisibility(View.VISIBLE);
                translateTextView.setVisibility(View.GONE);
                break;
            case SHOW_EN:
                this.setVisibility(View.VISIBLE);
                originalTextView.setVisibility(View.GONE);
                break;
            case SHOW_ENCN:
                this.setVisibility(View.VISIBLE);
                originalTextView.setVisibility(View.VISIBLE);
                translateTextView.setVisibility(View.VISIBLE);
                break;
            case SHOW_NONE:
                this.setVisibility(View.GONE);
                break;
        }
    }

}
