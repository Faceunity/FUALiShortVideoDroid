/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.http;

import com.aliyun.demo.util.Common;
import com.aliyun.qupaiokhttp.HttpRequest;
import com.aliyun.qupaiokhttp.RequestParams;
import com.aliyun.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.struct.form.IMVForm;
import com.aliyun.struct.form.ResourceForm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;


public class EffectService {
    public static final String URL_EFFECT_LIST = "/api/res/type/";
    private Gson mGson = new GsonBuilder().disableHtmlEscaping().create();
    public static final int EFFECT_TEXT = 1;        //字体
    public static final int EFFECT_PASTER = 2;      //动图
    public static final int EFFECT_MV = 3;          //MV
    public static final int EFFECT_FILTER = 4;      //滤镜
    public static final int EFFECT_MUSIC = 5;       //音乐
    public static final int EFFECT_CAPTION = 6;     //字幕
    public static final int EFFECT_FACE_PASTER = 7; //人脸动图
    public static final int EFFECT_IMG = 8;         //静态贴纸

    public void loadEffectPaster(String signature,
                                 String packageName,
                                 final HttpCallback<List<ResourceForm>> callback) {
        String url = new StringBuilder(Common.BASE_URL).append(URL_EFFECT_LIST)
                .append(EFFECT_PASTER).toString();
        RequestParams params = new RequestParams();
        params.addFormDataPart("packageName", packageName);
        params.addFormDataPart("signature", signature);
        HttpRequest.get(url, params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    List<ResourceForm> resourceList = mGson.fromJson(s, new TypeToken<List<ResourceForm>>() {
                    }.getType());
                    if (callback != null) {
                        callback.onSuccess(resourceList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });

    }

    public void loadEffectMv(
            String signature,
            String packageName,
            final HttpCallback<List<IMVForm>> callback) {
        String url = new StringBuilder(Common.BASE_URL).append(URL_EFFECT_LIST)
                .append(EFFECT_MV).toString();
        RequestParams params = new RequestParams();
        params.addFormDataPart("packageName", packageName);
        params.addFormDataPart("signature", signature);
        HttpRequest.get(url, params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    List<IMVForm> resourceList = mGson.fromJson(s, new TypeToken<List<IMVForm>>() {
                    }.getType());
                    if (callback != null) {
                        callback.onSuccess(resourceList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });

    }

    public void loadEffectCaption(int type,
                                  String signature,
                                  String packageName,
                                  final HttpCallback<List<ResourceForm>> callback) {
        String url = new StringBuilder(Common.BASE_URL).append(URL_EFFECT_LIST)
                .append(type).toString();
        RequestParams params = new RequestParams();
        params.addFormDataPart("packageName", packageName);
        params.addFormDataPart("signature", signature);
        HttpRequest.get(url, params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    List<ResourceForm> resourceList = mGson.fromJson(s, new TypeToken<List<ResourceForm>>() {
                    }.getType());
                    if (callback != null) {
                        callback.onSuccess(resourceList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });

    }
}
