package com.aliyun.svideo.recorder.view.borad.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.aliyun.svideo.recorder.view.borad.pens.Shapable;

public class Curv extends ShapeAbstract {

	public Curv(Shapable paintTool) {
		super(paintTool);
	}

	@Override
	public void draw(Canvas canvas, Paint paint) {
		super.draw(canvas, paint);
		canvas.drawPath(mPath, paint);
	}

	@Override
	public String toString() {
		return "curv";
	}
}
