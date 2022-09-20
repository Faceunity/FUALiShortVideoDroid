package com.aliyun.svideo.recorder.view.borad.pens;

import android.graphics.Paint;

import com.aliyun.svideo.recorder.view.borad.comm.CommHandwriting;

//普通画笔
public class PlainPen extends PenAbstract implements CommHandwriting {
	public PlainPen(int size, int penColor) {
		this(size,penColor,Paint.Style.STROKE);
	}

	public PlainPen(int size, int penColor,Paint.Style style ) {
		super(size, penColor,style);
	}

	@Override
	public String toString() {
		return "\tplainPen: " + "\tshap: " + currentShape + "\thasDraw: "
				+ hasDraw() + "\tsize: " + penSize + "\tstyle:" +style;
	}
}
