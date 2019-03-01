package com.aliyun.svideo.editor.effects.transition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.sdk.external.struct.MediaType;
import com.aliyun.svideo.sdk.external.struct.common.AliyunClip;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunIThumbnailFetcher;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunThumbnailFetcherFactory;

import java.util.ArrayList;
import java.util.List;

public class TransitionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = TransitionAdapter.class.getName();
    private Context mContext;

    private AliyunIThumbnailFetcher mThumbnailFetcher;
    private TransitionEffectCache mTransitionCache;
    private ArrayList<Long> mStartTimes = new ArrayList<>();
    private int mSelectPosition = -1;
    private OnSelectListener mOnSelectListener;

    TransitionAdapter(Context context, TransitionEffectCache transitionEffectCache) {
        mContext = context;
        mTransitionCache = transitionEffectCache;
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        long startTime = 0;
        for (AliyunClip clip : mTransitionCache.getAliyunIClipConstructor().getAllClips()) {
            if (clip.getMediaType() == MediaType.ANY_IMAGE_TYPE) {
                mThumbnailFetcher.addImageSource(clip.getSource(), 100, 0);
            } else if (clip.getMediaType() == MediaType.ANY_VIDEO_TYPE) {
                mThumbnailFetcher.addVideoSource(clip.getSource(), 0, 100, 0);
            }

            mStartTimes.add(startTime);
            Log.d(TAG, "startTime is " + startTime);
            startTime = mThumbnailFetcher.getTotalDuration();
        }
        int width = (int) context.getResources().getDimension(R.dimen.alivc_svideo_effect_transition_thumb_width);
        int height = (int) context.getResources().getDimension(R.dimen.alivc_svideo_effect_transition_thumb_height);
        mThumbnailFetcher.setParameters(width, height, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams")
        View item = ((EditorActivity) mContext).getLayoutInflater().inflate(R.layout.aliyun_svideo_transition_item_view,
                    null);

        return new TransitionViewHolder(item);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (payloads == null || payloads.size() == 0) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        TransitionViewHolder transitionViewHolder = (TransitionViewHolder) holder;

        //当修改指定的条目时，调用此方法
        for (Object payload : payloads) {
            if (TransitionChooserView.TRANSITION_PAYLOAD.equals(payload)) {
                mOnSelectListener.onSelect(transitionViewHolder.mTransition, mTransitionCache.get(position), position, false);
            }
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        TransitionViewHolder transitionViewHolder = (TransitionViewHolder) holder;
        long[] time = {mStartTimes.get(position) + 1};

        Log.d(TAG, "request time is " + time[0]);

        if (position == 0) {
            //起始view设置paddingleft，使UI对齐
            int padding = DensityUtil.dip2px(mContext, 15);
            holder.itemView.setPadding(padding, padding, padding, padding);
        }

        //获取缩略图
        mThumbnailFetcher.requestThumbnailImage(time, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
            @Override
            public void onThumbnailReady(Bitmap bitmap, long l) {
                ((TransitionViewHolder) holder).mThumbnail.setImageBitmap(bitmap);
            }

            @Override
            public void onError(int errorCode) {
                Log.e(TAG, "转场获取视频缩略图异常 requestThumbnailImage error");
            }
        });

        //设置转场按钮的显示（最后一个片段的转场图标为隐藏状态）
        if (position < mTransitionCache.getCount() - 1) {
            transitionViewHolder.mTransition.setVisibility(View.VISIBLE);
        } else {
            transitionViewHolder.mTransition.setVisibility(View.GONE);
        }

        //初始化转场图标
        if (mOnSelectListener != null) {
            mOnSelectListener.onSelect(transitionViewHolder.mTransition, mTransitionCache.get(position), position,
                                       mSelectPosition == -1);
            if (mSelectPosition == -1) {
                mSelectPosition = 0;
            }
        }

        //设置选中状态
        if (mSelectPosition == position) {
            transitionViewHolder.mTransition.setSelected(true);
        } else {
            transitionViewHolder.mTransition.setSelected(false);
        }
    }

    public void setOnSelectListener(OnSelectListener listener) {
        this.mOnSelectListener = listener;
    }

    public int getSelectPosition() {
        return mSelectPosition;
    }

    public void release() {
        mThumbnailFetcher.release();
    }

    @Override
    public int getItemCount() {
        return mTransitionCache.getCount();
    }

    class TransitionViewHolder extends RecyclerView.ViewHolder {
        ImageView mThumbnail, mTransition;

        TransitionViewHolder(View itemView) {
            super(itemView);
            mThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            mTransition = itemView.findViewById(R.id.iv_transition);
            mTransition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectPosition = getAdapterPosition();
                    if (mTransitionCache != null && mOnSelectListener != null) {
                        int effectIndex = mTransitionCache.get(mSelectPosition);
                        mOnSelectListener.onSelect(mTransition, effectIndex, mSelectPosition, true);
                    }
                    notifyDataSetChanged();
                }
            });
        }

    }



    public interface OnSelectListener {
        void onSelect(ImageView iv, int effectPosition, int clipIndex, boolean isClickTransition);
    }
}
