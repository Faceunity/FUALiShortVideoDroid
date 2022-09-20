package com.aliyun.svideo.recorder.view.borad.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.aliyun.svideo.recorder.view.borad.pens.Shapable;

public class Square extends ShapeAbstract {

	public Square(Shapable paintTool) {
		super(paintTool);
	}

	@Override
	public void draw(Canvas canvas, Paint paint) {
		super.draw(canvas, paint);
		if ((y2 > y1 && x2 > x1) || (y2 < y1 && x2 < x1)) {
			if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
				canvas.drawRect(x1, y1, x1 + y2 - y1, y2, paint);
			} else {
				canvas.drawRect(x1, y1, x2, y1 + x2 - x1, paint);
			}
		} else {
			if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
				canvas.drawRect(x1, y1, x1 + y1 - y2, y2, paint);
			} else {
				canvas.drawRect(x1, y1, x2, y1 + x1 - x2, paint);
			}
		}
	}

	@Override
	public String toString() {
		return "Square";
	}
}
