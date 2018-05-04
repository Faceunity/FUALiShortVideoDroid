/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.actionbar;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.demo.editor.R;


public abstract class ActionBarActivity extends AppCompatActivity {
    private LinearLayout mViewContainer;
    private ActionBar mActionBar;
    private View mCustomView;
    private ViewDelegate mRightView = new ViewDelegate();
    private ViewDelegate mLeftView = new ViewDelegate();

    private ImageView mIvLeft;
    private ImageView mIvRight;
    private TextView mTvRight;
    private TextView mTvCenter;
    private TextView mTvLeft;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mViewContainer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.aliyun_svideo_activity_action_bar, null);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View mView = LayoutInflater.from(this).inflate(layoutResID, null);
        mViewContainer.addView(mView);
        super.setContentView(mViewContainer);
        setupActionBar();
    }

    @Override
    public void setContentView(View view) {
        mViewContainer.addView(view);
        super.setContentView(mViewContainer);
        setupActionBar();
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        mViewContainer.addView(view, params);
        super.setContentView(mViewContainer, params);
        setupActionBar();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) mViewContainer.findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if(mActionBar == null) {
            return;
        }
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        if(mCustomView != null) {
            mActionBar.setCustomView(mCustomView);
        } else {
            View view = LayoutInflater.from(this).inflate(R.layout.aliyun_svideo_action_bar_profile, null);
            mActionBar.setCustomView(view);
            LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            view.setLayoutParams(layoutParams);
            View actionBarView = mActionBar.getCustomView();
            mIvLeft = (ImageView) actionBarView.findViewById(R.id.iv_left);
            mTvCenter = (TextView) actionBarView.findViewById(R.id.tv_center);
            mIvRight = (ImageView) actionBarView.findViewById(R.id.iv_right);
            mTvRight = (TextView) actionBarView.findViewById(R.id.tv_right);
            mTvLeft = (TextView) actionBarView.findViewById(R.id.tv_left);
            mIvLeft.setOnClickListener(mDefaultClicKListener);
            mIvLeft.setImageResource(R.mipmap.aliyun_svideo_icon_back);
            mIvRight.setImageResource(R.mipmap.aliyun_svideo_icon_next);
            mRightView.delegate(mIvRight);
            mLeftView.delegate(mIvLeft);
            mLeftView.setOnClickListener(mDefaultClicKListener);
        }
    }

    protected void setActionBarLeftView(int resId) {
        if(mIvLeft != null) {
            mIvLeft.setImageResource(resId);
            mLeftView.delegate(mIvLeft);
        }
    }

    protected void setActionBarLeftText(String text) {
        if(mTvLeft != null) {
            mTvLeft.setText(text);
            mLeftView.delegate(mTvLeft);
        }
    }

    protected void setActionBarLeftView(Drawable drawable) {
        if(mIvLeft != null) {
            mIvLeft.setImageDrawable(drawable);
            mLeftView.delegate(mIvLeft);
        }
    }

    protected void setActionBarLeftViewVisibility(int visibility) {
        mLeftView.setVisibility(visibility);
    }

    protected void setActionBarTitle(String title) {
        if(mTvCenter != null) {
            mTvCenter.setText(title);
        }
    }

    protected void setActionBarTitleVisibility(int visibility) {
        if(mTvCenter != null) {
            mTvCenter.setVisibility(visibility);
        }
    }

    protected void setActionBarRightText(String text) {
        if(mTvRight != null) {
            mTvRight.setText(text);
            mRightView.delegate(mTvRight);
        }
    }

    protected void setActionBarRightView(int resId) {
        if(mIvRight != null) {
            mIvRight.setImageResource(resId);
            mRightView.delegate(mIvRight);
        }
    }

    protected void setActionBarRightView(Drawable drawable) {
        if(mIvRight != null) {
            mIvRight.setImageDrawable(drawable);
            mRightView.delegate(mIvRight);
        }
    }

    protected void setActionBarRightViewVisibility(int visibility) {
        mRightView.setVisibility(visibility);
    }

    protected void setActionBarRightClickListener(View.OnClickListener rightClickListener) {
        mRightView.setOnClickListener(rightClickListener);
    }

    protected void setActionBarLeftClickListener(View.OnClickListener leftClickListener) {
        mLeftView.setOnClickListener(leftClickListener);
    }

    protected void setCustomView(int layoutResID) {
        mCustomView = LayoutInflater.from(this).inflate(layoutResID, null);
    }

    protected int getActionBarRightViewID() {
        return mRightView.mView.getId();
    }

    protected int getActionBarLeftViewID() {
        return mLeftView.mView.getId();
    }

    private View.OnClickListener mDefaultClicKListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.iv_left) {
                onBackPressed();
            }
        }
    };

    class ViewDelegate {
        private View mView;
        private int mVisibility;
        private View.OnClickListener mOnClickListener;

        public void delegate(View view) {
            resetView();
            mView = view;
            mView.setVisibility(mVisibility);
            mView.setOnClickListener(mOnClickListener);
        }

        private void resetView() {
            if(mView != null) {
                mView.setVisibility(View.GONE);
                mView.setOnClickListener(null);
            }
        }

        public void setVisibility(int visibility) {
            if(mView != null) {
                mView.setVisibility(visibility);
            }
            mVisibility = visibility;
        }

        public void setOnClickListener(View.OnClickListener clickListener) {
            mOnClickListener = clickListener;
            if(mView != null) {
                mView.setOnClickListener(mOnClickListener);
            }
        }
    }
}
