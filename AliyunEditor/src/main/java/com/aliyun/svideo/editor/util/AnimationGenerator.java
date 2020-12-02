package com.aliyun.svideo.editor.util;



public class AnimationGenerator {

    private float mTranslateX;
    private float mTranslateY;

    private float mScaleX;
    private float mScaleY;

    private float mAlpha;

    private float mRotate;

    private StringBuffer mAnimationConfig = new StringBuffer();


    public float getTranslateX() {
        return mTranslateX;
    }

    public void setTranslateX(float translateX) {
        this.mTranslateX = translateX;
    }

    public float getTranslateY() {
        return mTranslateY;
    }

    public void setTranslateY(float translateY) {
        this.mTranslateY = translateY;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public void setScaleX(float scaleX) {
        this.mScaleX = scaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    public void setScaleY(float scaleY) {
        this.mScaleY = scaleY;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
    }

    public float getRotate() {
        return mRotate;
    }

    public void setRotate(float rotate) {
        this.mRotate = rotate;
    }

    public void addTranslatePointer(float pointer) {
        mAnimationConfig.append(pointer).append(":")
        .append(mTranslateX)
        .append(",")
        .append(mTranslateY)
        .append(";");
    }

    public void addRotatePointer(float pointer) {
        mAnimationConfig.append(pointer).append(":")
        .append(mRotate)
        .append(";");
    }

    public void addAlphaPointer(float pointer) {
        mAnimationConfig.append(pointer).append(":")
        .append(mAlpha)
        .append(";");
    }

    public void addScalePointer(float pointer) {
        mAnimationConfig.append(pointer).append(":")
        .append(mScaleX)
        .append(",")
        .append(mScaleY)
        .append(";");
    }

    public String generateAnimationConfig() {
        return mAnimationConfig.substring(0, mAnimationConfig.length() - 1);
    }
}
