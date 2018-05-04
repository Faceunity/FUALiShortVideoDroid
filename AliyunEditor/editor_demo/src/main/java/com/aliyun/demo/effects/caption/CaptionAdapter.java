/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.caption;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.aliyun.common.logger.Logger;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.demo.util.Common;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.qupaiokhttp.HttpRequest;
import com.aliyun.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.quview.CircularImageView;
import com.aliyun.struct.form.FontForm;
import com.aliyun.struct.form.PasterForm;
import com.aliyun.struct.form.ResourceForm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CaptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final int CAPTION_TYPE = 6;
    private static final int FONT_TYPE = 1;
    private Context mContext;
    private OnItemClickListener mItemClick;
    private ArrayList<PasterForm> data = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();
    private CopyOnWriteArrayList<FontForm> fontData = new CopyOnWriteArrayList<>();
    private ResourceForm mResourceForm;

    public CaptionAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(ResourceForm data) {
        if (data == null || data.getPasterList() == null) {
            return;
        }
        mResourceForm = data;
        this.data = (ArrayList<PasterForm>) data.getPasterList();
        fontData.clear();
        getFontFromLocal();
        for (PasterForm form : this.data) {
            getFont(form.getFontId());
        }
        notifyDataSetChanged();
    }

    public void clearData(){
        data.clear();
        fontData.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_resources_item_view, parent, false);
        CaptionViewHolder filterViewHolder = new CaptionViewHolder(view);
        filterViewHolder.frameLayout = (FrameLayout) view.findViewById(R.id.resource_image);
        return filterViewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < data.size()) {
            return CAPTION_TYPE;
        } else {
            return FONT_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CaptionViewHolder captionViewHolder = (CaptionViewHolder) holder;
        String iconPath = "";
        if (getItemViewType(position) == CAPTION_TYPE) {
            iconPath = data.get(position).getIcon();
        } else if (getItemViewType(position) == FONT_TYPE) {
            iconPath = fontData.get(position - data.size()).getIcon();
        }
        Glide.with(mContext).load(iconPath).into(new ViewTarget<CircularImageView, GlideDrawable>(captionViewHolder.mImage) {
            @Override
            public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                captionViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
            }
        });
        captionViewHolder.itemView.setTag(holder);
        captionViewHolder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return data.size() + fontData.size();
    }

    private static class CaptionViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;
        CircularImageView mImage;
        TextView mName;

        public CaptionViewHolder(View itemView) {
            super(itemView);
            mImage = (CircularImageView) itemView.findViewById(R.id.resource_image_view);
            mName = (TextView) itemView.findViewById(R.id.resource_name);
            mName.setVisibility(View.GONE);
        }
    }

    private void downloadPaster(final PasterForm form, final int index) {
        FileDownloaderModel model = new FileDownloaderModel();
        model.setEffectType(CAPTION_TYPE);
        model.setIcon(mResourceForm.getIcon());
        model.setId(mResourceForm.getId());
        model.setDescription(mResourceForm.getDescription());
        model.setIsnew(mResourceForm.getIsNew());
        model.setName(mResourceForm.getName());
        model.setLevel(mResourceForm.getLevel());
        model.setPreview(mResourceForm.getPreviewUrl());
        model.setSubname(form.getName());
        model.setSubicon(form.getIcon());
        model.setUrl(form.getDownloadUrl());
        model.setSubid(form.getId());
        model.setFontid(form.getFontId());
        model.setSort(form.getSort());
        model.setSubpreview(form.getPreviewUrl());
        model.setMd5(form.getMD5());
        model.setIsunzip(1);
        FileDownloaderModel fileMode = DownloaderManager.getInstance().addTask(model, model.getUrl());
        DownloaderManager.getInstance().startTask(fileMode.getTaskId(), new FileDownloaderCallback() {
            @Override
            public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, final int progress) {
                Logger.getDefaultLogger().d("downloadId..." + downloadId + "  progress..." + progress);

            }

            @Override
            public void onFinish(int downloadId, String path) {
                Logger.getDefaultLogger().d("downloadId..." + downloadId + "  path..." + path);
                if (mItemClick != null) {
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.CAPTION;
                    effectInfo.setPath(path);
                    effectInfo.fontPath = getFontByPaster(form).getUrl();
                    mItemClick.onItemClick(effectInfo, index);
                }

            }
        });
    }
    private void getFontFromLocal(){
        List<FileDownloaderModel> fileDownloaderModels = DownloaderManager.getInstance().getDbController().getResourceByType(FONT_TYPE);
        for(FileDownloaderModel model : fileDownloaderModels){
            FontForm fontForm = new FontForm();
            fontForm.setLevel(model.getLevel());
            fontForm.setIcon(model.getIcon());
            fontForm.setBanner(model.getBanner());
            fontForm.setId(model.getId());
            fontForm.setMd5(model.getMd5());
            fontForm.setName(model.getName());
            fontForm.setType(model.getEffectType());
            fontForm.setUrl(model.getUrl());
            fontForm.setSort(model.getSort());
            fontData.add(fontForm);
            ids.add(fontForm.getId());
        }
    }
    private void getFont(int fontId) {
        String api = Common.BASE_URL + "/api/res/get/1/" + fontId;
        String category = "?packageName=" + mContext.getApplicationInfo().packageName;
        Logger.getDefaultLogger().d("pasterUrl url = " + api + category);
        HttpRequest.get(api + category,
                new StringHttpRequestCallback() {
                    @Override
                    protected void onSuccess(String s) {
                        super.onSuccess(s);
                        JSONSupportImpl jsonSupport = new JSONSupportImpl();
                        FontForm fontForm = null;
                        try {
                            fontForm = jsonSupport.readValue(s, FontForm.class);
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        if (fontForm != null) {
                            if(!ids.contains(fontForm.getId())){
                                fontData.add(fontForm);
                                ids.add(fontForm.getId());
                            }
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        super.onFailure(errorCode, msg);

                    }
                });
    }

//    private void getFont(int fontId, final String pasterPath, final int index) {
//        String api = EditorActivity.RESOURCE_URL + "/api/res/get/1/" + fontId;
//        String category = "?bundleId=" + mContext.getApplicationInfo().packageName;
//        Logger.getDefaultLogger().d("pasterUrl url = " + api + category);
//        HttpRequest.get(api + category,
//                new StringHttpRequestCallback() {
//                    @Override
//                    protected void onSuccess(String s) {
//                        super.onSuccess(s);
//                        JSONSupportImpl jsonSupport = new JSONSupportImpl();
//                        FontForm fontForm = null;
//                        try {
//                            fontForm = jsonSupport.readValue(s, FontForm.class);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//
//                        }
//                        if (fontForm != null) {
//                            if (mItemClick != null) {
//                                EffectInfo effectInfo = new EffectInfo();
//                                effectInfo.type = UIEditorPage.CAPTION;
//                                effectInfo.setPath(pasterPath);
//                                effectInfo.fontPath = fontForm.getUrl();
//                                mItemClick.onItemClick(effectInfo, index);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(int errorCode, String msg) {
//                        super.onFailure(errorCode, msg);
//                        if (mItemClick != null) {
//                            EffectInfo effectInfo = new EffectInfo();
//                            effectInfo.type = UIEditorPage.CAPTION;
//                            effectInfo.setPath(pasterPath);
//                            effectInfo.fontPath = null;
//                            mItemClick.onItemClick(effectInfo, index);
//                        }
//                    }
//                });
//    }

    private void downloadFont(FontForm form,final int index){
        FileDownloaderModel model = new FileDownloaderModel();
        model.setEffectType(FONT_TYPE);
        model.setName(form.getName());
        model.setUrl(form.getUrl());
        model.setId(form.getId());
        model.setLevel(form.getLevel());
        model.setSort(form.getSort());
        model.setMd5(form.getMd5());
        model.setBanner(form.getBanner());
        model.setIcon(form.getIcon());
        model.setIsunzip(1);
        FileDownloaderModel fileMode = DownloaderManager.getInstance().addTask(model, model.getUrl());
        DownloaderManager.getInstance().startTask(fileMode.getTaskId(),new FileDownloaderCallback(){
            @Override
            public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, final int progress) {
                Logger.getDefaultLogger().d("downloadId..." + downloadId +"  progress..." + progress);

            }

            @Override
            public void onFinish(int downloadId, String path) {
                Logger.getDefaultLogger().d("downloadId..." + downloadId +"  path..." + path);
                if(mItemClick != null){
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.FONT;
                    effectInfo.setPath(null);
                    effectInfo.fontPath = path;
                    mItemClick.onItemClick(effectInfo,index);
                }
            }
        });
    }
    private FontForm getFontByPaster(PasterForm form){
        FontForm fontForm = null;
        for(FontForm form1 : fontData){
            if(form1.getId() ==  form.getFontId()){
                fontForm = form1;
                break;
            }
        }
        return fontForm;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }

    @Override
    public void onClick(View view) {
        final CaptionViewHolder holder = (CaptionViewHolder) view.getTag();
        final int position = holder.getAdapterPosition();
        int type = getItemViewType(position);
        if (type == CAPTION_TYPE) {
            PasterForm form = data.get(position);
            String path = DownloaderManager.getInstance().getDbController().getPathByUrl(form.getDownloadUrl());
            if(path != null && !path.isEmpty()){
                if (mItemClick != null) {
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.CAPTION;
                    effectInfo.setPath(path);
                    FontForm fontForm = getFontByPaster(form);
                    if(fontForm == null){
                        effectInfo.fontPath = null;
                    }else{
                        effectInfo.fontPath = fontForm.getUrl();
                    }
                    mItemClick.onItemClick(effectInfo, position);
                }
            }else{
                downloadPaster(form, position);
            }
        }else if(type == FONT_TYPE){
            FontForm form = fontData.get(position - data.size());
            String path = DownloaderManager.getInstance().getDbController().getPathByUrl(form.getUrl());
            if(path != null && !path.isEmpty()){
                if (mItemClick != null) {
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.FONT;
                    effectInfo.setPath(null);
                    effectInfo.fontPath = path;
                    mItemClick.onItemClick(effectInfo, position);
                }
            }else{
                downloadFont(form,position);
            }
        }

//        if(mItemClick != null) {
//            CaptionViewHolder viewHolder = (CaptionViewHolder) view.getTag();
//            EffectInfo effectInfo = new EffectInfo();
//            effectInfo.isCategory = true;
//            mItemClick.onItemClick(effectInfo, viewHolder.getAdapterPosition());
//        }
    }
}
