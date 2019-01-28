/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.alivcsolution;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * @author Mulberry
 */
public class SplashActivity extends Activity{
    /**
     * 动画时间 2000ms
     */
    private static final int ANIMATOR_DURATION = 2000;

    /**
     * 动画样式-- 透明度动画
     */
    private static final String ANIMATOR_STYLE = "alpha";

    /**
     * 动画起始值
     */
    private static final float ANIMATOR_VALUE_START = 0f;

    /**
     * 动画结束值
     */
    private static final float ANIMATOR_VALUE_END = 1f;
    private ObjectAnimator alphaAnimIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo 排查错误
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_spalash);
        LinearLayout splashView = findViewById(R.id.splash_view);

        alphaAnimIn = ObjectAnimator.ofFloat(splashView, ANIMATOR_STYLE, ANIMATOR_VALUE_START, ANIMATOR_VALUE_END);

        alphaAnimIn.setDuration(ANIMATOR_DURATION);

        alphaAnimIn.start();
        alphaAnimIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setJumpToMain();
            }
        });
    }

    private void setJumpToMain(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.splash_fade_in, R.anim.splash_fade_out);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (alphaAnimIn != null) {

            alphaAnimIn.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alphaAnimIn != null) {
            alphaAnimIn.cancel();
            alphaAnimIn.removeAllListeners();
            alphaAnimIn = null;
        }
    }
}
