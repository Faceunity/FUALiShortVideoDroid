/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.quview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.CopyOnWriteArrayList;

public class RecordTimelineView extends View {
    private int maxDuration;
    private int minDuration;
    private CopyOnWriteArrayList<DrawInfo> clipDurationList = new CopyOnWriteArrayList<>();
    private DrawInfo currentClipDuration = new DrawInfo();
    private Paint paint = new Paint();
    private int durationColor;
    private int selectColor;
    private int offsetColor;
    private int backgroundColor;
    private boolean isSelected = false;
    public RecordTimelineView(Context context) {
        super(context);
        init();
    }

    public RecordTimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordTimelineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        paint.setAntiAlias(true);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(backgroundColor != 0){
            canvas.drawColor(getResources().getColor(backgroundColor));
        }
        int lastTotalDuration = 0;
        for(int i = 0 ;i < clipDurationList.size() ;i++){
            DrawInfo info = clipDurationList.get(i);
            switch (info.drawType){
                case OFFSET:
                    paint.setColor(getResources().getColor(offsetColor));
                    break;
                case DURATION:
                    paint.setColor(getResources().getColor(durationColor));
                    break;
                case SELECT:
                    paint.setColor(getResources().getColor(selectColor));
                    break;
            }
            canvas.drawRect(lastTotalDuration/(float)maxDuration * getWidth(),0f,(lastTotalDuration + info.length) / (float)maxDuration * getWidth(),getHeight(),paint);
            lastTotalDuration += info.length;
        }
        if(currentClipDuration != null && currentClipDuration.length != 0){
            paint.setColor(getResources().getColor(durationColor));
            canvas.drawRect(lastTotalDuration/(float)maxDuration * getWidth(),0f,(lastTotalDuration + currentClipDuration.length)/ (float)maxDuration * getWidth(),getHeight(),paint);
        }
        if(lastTotalDuration + currentClipDuration.length < minDuration){
            paint.setColor(getResources().getColor(offsetColor));
            canvas.drawRect(minDuration / (float)maxDuration * getWidth(),0f,(minDuration + maxDuration / 200) / (float)maxDuration * getWidth(),getHeight(),paint);
        }
    }

    public void clipComplete(){
        clipDurationList.add(currentClipDuration);
        DrawInfo info = new DrawInfo();
        info.length = maxDuration / 400;
        info.drawType = DrawType.OFFSET;
        clipDurationList.add(info);
        currentClipDuration = new DrawInfo();
        invalidate();
    }

    public void deleteLast(){
        if(clipDurationList.size() >= 2){
            clipDurationList.remove(clipDurationList.size() - 1);
            clipDurationList.remove(clipDurationList.size() - 1);
        }
        invalidate();
    }

    public void selectLast(){
        if(clipDurationList.size() >= 2){
            DrawInfo info = clipDurationList.get(clipDurationList.size() - 2);
            info.drawType = DrawType.SELECT;
            invalidate();
            isSelected = true;
        }
    }


    public void setMaxDuration(int maxDuration){
        this.maxDuration = maxDuration;
    }

    public void setMinDuration(int minDuration){
        this.minDuration = minDuration;
    }

    public void setDuration(int duration){
        if(isSelected){
            for(DrawInfo info : clipDurationList){
                if(info.drawType == DrawType.SELECT){
                    info.drawType = DrawType.DURATION;
                    isSelected = false;
                    break;
                }
            }
        }
        this.currentClipDuration.drawType = DrawType.DURATION;
        this.currentClipDuration.length = duration;
        invalidate();
    }

    public void setColor(int duration,int select,int offset,int backgroundColor){
        this.durationColor = duration;
        this.selectColor = select;
        this.offsetColor = offset;
        this.backgroundColor = backgroundColor;

    }

    class DrawInfo{
        int length;
        DrawType drawType = DrawType.DURATION;
    }
    enum  DrawType{
        OFFSET,
        DURATION,
        SELECT
    }
}
