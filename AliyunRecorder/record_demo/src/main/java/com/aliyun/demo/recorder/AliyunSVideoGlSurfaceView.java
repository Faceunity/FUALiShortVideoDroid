package com.aliyun.demo.recorder;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import java.lang.reflect.Field;

public class AliyunSVideoGlSurfaceView extends GLSurfaceView {
    private Renderer mRenderer;
    private static final String TAG = AliyunSVideoGlSurfaceView.class.getName();
    public AliyunSVideoGlSurfaceView(Context context) {
        super(context);
    }

    public AliyunSVideoGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if(mRenderer == null) {
            return;
        }
        super.onWindowVisibilityChanged(visibility);
        Log.d(TAG, "onWindowVisibilityChanged");
    }

    @Override
    public void setRenderer(Renderer renderer) {
        super.setRenderer(renderer);
        mRenderer = renderer;
        Log.d(TAG, "setRender");
    }

}
