package com.hanwen.chinesechat.view;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.hanwen.chinesechat.R;

public abstract class ContentView extends FrameLayout {
    private View contentView;
    private ViewState CURRENT_STATE = ViewState.LOADING;
    private View emptyView;
    private View failureView;
    private View loadingView;
    private View successView;

    public ContentView(Context context) {
        super(context);
        contentView = View.inflate(context, R.layout.contentview, null);
        this.addView(contentView);
        initView();
    }

    private void initView() {
        loadingView = onCreateLoadingView();
        failureView = onCreateFailureView();
        emptyView = onCreateEmptyView();
        successView = onCreateSuccessView();
        if (successView != null) {
            this.addView(successView);
        }
        showView(ViewState.LOADING);
    }

    private View onCreateEmptyView() {
        return contentView.findViewById(R.id.rl_empty);
    }

    private View onCreateFailureView() {
        return contentView.findViewById(R.id.rl_failure);
    }

    private View onCreateLoadingView() {
        return contentView.findViewById(R.id.rl_loading);
    }

    public abstract void initData();

    public abstract View onCreateSuccessView();

    public void showView(ViewState viewState) {
        this.CURRENT_STATE = viewState;
        if (loadingView != null) {
            loadingView.setVisibility(this.CURRENT_STATE == ViewState.LOADING ? View.VISIBLE : View.INVISIBLE);
        }

        if (failureView != null) {
            failureView.setVisibility(this.CURRENT_STATE == ViewState.FAILURE ? View.VISIBLE : View.INVISIBLE);
        }

        if (emptyView != null) {
            emptyView.setVisibility(this.CURRENT_STATE == ViewState.EMPTY ? View.VISIBLE : View.INVISIBLE);
        }

        if (successView != null) {
            successView.setVisibility(this.CURRENT_STATE == ViewState.SUCCESS ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public enum ViewState {
        EMPTY, FAILURE, LOADING, SUCCESS

        // LOADING,SUCCESS,FAILURE,VACANCY
    }

}
