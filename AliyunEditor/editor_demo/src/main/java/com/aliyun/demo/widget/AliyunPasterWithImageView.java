/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class AliyunPasterWithImageView extends AliyunPasterView {

	public AliyunPasterWithImageView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public AliyunPasterWithImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public AliyunPasterWithImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	private int content_width;
	private int content_height;

	public void setContentWidth(int content_width) {
		this.content_width = content_width;
	}

	public void setContentHeight(int content_height) {
		this.content_height = content_height;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		validateTransform();
		int width, height;
		mMatrixUtil.decomposeTSR(mTransform);

		width = (int) (mMatrixUtil.scaleX * content_width);
		height = (int) (mMatrixUtil.scaleY * content_height);
		Log.d("EDIT", "Measure width : " + width + "scaleX : "
				+ " screen width : " + getWidth() + " 1/8 width : " + getWidth() / 8);
		int w = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int h = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureChildren(w, h);
	}

	private View _ContentView;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		_ContentView = findViewById(android.R.id.content);
	}

	@Override
	public int getContentWidth() {
		// TODO Auto-generated method stub
		return content_width;
	}

	@Override
	public int getContentHeight() {
		// TODO Auto-generated method stub
		return content_height;
	}

	@Override
	public View getContentView() {
		// TODO Auto-generated method stub
		return _ContentView;
	}

}
