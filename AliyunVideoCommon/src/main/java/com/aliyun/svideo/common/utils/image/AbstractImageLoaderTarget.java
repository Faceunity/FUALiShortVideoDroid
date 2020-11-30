package com.aliyun.svideo.common.utils.image;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author cross_ly
 * @date 2018/12/06
 * @param <R> The type of resource the target can display.
 */
public abstract class AbstractImageLoaderTarget<R> {


    public void onLoadStarted() {}

    public void onLoadFailed(@Nullable Drawable errorDrawable) {}

    public abstract void onResourceReady(@NonNull R resource);

    public void onLoadCleared(@Nullable Drawable placeholder) {}

}
