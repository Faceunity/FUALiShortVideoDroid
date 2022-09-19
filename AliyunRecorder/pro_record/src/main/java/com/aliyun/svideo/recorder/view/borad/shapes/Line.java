package com.aliyun.svideo.recorder.view.borad.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.aliyun.svideo.recorder.view.borad.pens.Shapable;

public class Line extends ShapeAbstract {

	public Line(Shapable paintTool) {
		super(paintTool);
	}

	@Override
	public void draw(Canvas canvas,Paint paint) {
		super.draw(canvas, paint);
		canvas.drawLine(x1, y1, x2, y2, paint);
	}

	@Override
	public String toString() {
		return " line";
	}
}
