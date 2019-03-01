package com.aliyun.svideo.editor.effects.filter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.OnItemTouchListener;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.base.widget.CircularImageView;
import com.aliyun.svideo.sdk.external.struct.effect.EffectFilter;
import com.aliyun.video.common.utils.image.ImageLoaderImpl;
import com.aliyun.video.common.utils.image.AbstractImageLoaderTarget;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zsy_18 data:2018/8/31
 */
public class EffectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements View.OnClickListener, View.OnTouchListener {

    private final static String TAG = EffectAdapter.class.getName();
    private Context mContext;
    private OnItemClickListener mItemClick;
    private OnItemTouchListener mItemTouchListener;
    private GestureDetector mDetector;
    private int mSelectedPos = -1;
    private FilterViewHolder mSelectedHolder;
    private List<String> mFilterList = new ArrayList<>();

    public EffectAdapter(Context context) {
        this.mContext = context;
        //此处用手势监听，不通过
        mDetector = new GestureDetector(mContext,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onShowPress(MotionEvent e) {
                super.onShowPress(e);
                Log.e(TAG,"onShowPress" );
                //防止快速点击时引起的问题
                if (isAdding) {
                    return ;
                }
                if (mItemTouchListener != null&&pressView!=null) {
                    FilterViewHolder viewHolder = (FilterViewHolder)pressView.getTag();
                    int position = viewHolder.getAdapterPosition();
                    viewHolder.mImage.setVisibility(View.GONE);
                    viewHolder.mIvSelectState.setVisibility(View.VISIBLE);
                    viewHolder.mImage.setSelected(true);
                    mSelectedPos = position;
                    mSelectedHolder = viewHolder;
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.FILTER_EFFECT;
                    effectInfo.setPath(mFilterList.get(position));
                    effectInfo.id = position;
                    mItemTouchListener.onTouchEvent(OnItemTouchListener.EVENT_DOWN, position, effectInfo);
                    isAdding=true;
                }
            }
        });

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_resources_item_view, parent, false);
        FilterViewHolder filterViewHolder = new FilterViewHolder(view);
        filterViewHolder.frameLayout = (FrameLayout)view.findViewById(R.id.resource_image);
        return filterViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FilterViewHolder filterViewHolder = (FilterViewHolder)holder;
        String name = mContext.getString(R.string.alivc_svide_revoke);
        String path = mFilterList.get(position);
        if (path == null || "".equals(path)) {
            filterViewHolder.mImage.setImageResource(R.mipmap.alivc_svideo_icon_effect_cancel);
            filterViewHolder.itemView.setOnClickListener(this);
        } else {
            filterViewHolder.itemView.setOnTouchListener(this);
            EffectFilter effectFilter = new EffectFilter(path);
            if (effectFilter != null) {
                name = effectFilter.getName();
                if (filterViewHolder != null) {

                    new ImageLoaderImpl().loadImage(mContext,effectFilter.getPath() + "/icon.png")
                        .into(filterViewHolder.mImage, new AbstractImageLoaderTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource) {
                                filterViewHolder.mImage.setImageDrawable(resource);
                            }
                        });
                }
            }
        }

        if (mSelectedPos > mFilterList.size()) {
            mSelectedPos = 0;
        }
        if (mSelectedPos == position) {
            filterViewHolder.mImage.setVisibility(View.GONE);
            filterViewHolder.mIvSelectState.setVisibility(View.VISIBLE);
            mSelectedHolder = filterViewHolder;
        } else {
            filterViewHolder.mImage.setVisibility(View.VISIBLE);
            filterViewHolder.mIvSelectState.setVisibility(View.GONE);
        }
        filterViewHolder.mName.setText(name);
        filterViewHolder.itemView.setTag(holder);
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    private boolean isAdding = false;
    //当前正在被长按使用特效的view
    private View pressView;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.e(TAG,"getAction"+event.getAction() );
        mDetector.onTouchEvent(event);
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (!isAdding){
                    pressView=null;
                    return true;
                }
                if (v!=pressView){
                    return true;
                }
                if (mItemTouchListener != null) {
                    FilterViewHolder viewHolder = (FilterViewHolder)v.getTag();
                    int position = viewHolder.getAdapterPosition();
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.FILTER_EFFECT;
                    effectInfo.setPath(mFilterList.get(position));
                    effectInfo.id = position;
                    mItemTouchListener.onTouchEvent(OnItemTouchListener.EVENT_UP,
                        position, effectInfo);
                    viewHolder.mImage.setVisibility(View.VISIBLE);
                    viewHolder.mIvSelectState.setVisibility(View.GONE);
                    isAdding=false;
                }
                pressView=null;
                Log.e(TAG, "ACTION_UP");
                break;
            case MotionEvent.ACTION_DOWN:
                //如果特效正在被使用，不允许替换
                Log.e(TAG, "ACTION_DOWN");
                if (isAdding){
                    return false;
                }
                if (pressView==null){
                    pressView = v;
                }

            default:
                break;

        }
        return true;
    }
    private static class FilterViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;
        ImageView mIvSelectState;
        CircularImageView mImage;
        TextView mName;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mImage = (CircularImageView)itemView.findViewById(R.id.resource_image_view);
            mName = (TextView)itemView.findViewById(R.id.resource_name);
            mIvSelectState = itemView.findViewById(R.id.iv_select_state);
            mIvSelectState.setImageResource(R.drawable.alivc_svideo_shape_effect_press_state);
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }

    public void setOnItemTouchListener(OnItemTouchListener li) {
        mItemTouchListener = li;
    }

    @Override
    public void onClick(View view) {
        if (mItemClick != null) {
            FilterViewHolder viewHolder = (FilterViewHolder)view.getTag();
            int position = viewHolder.getAdapterPosition();
            EffectInfo effectInfo = new EffectInfo();
            effectInfo.type = UIEditorPage.FILTER_EFFECT;
            effectInfo.setPath(mFilterList.get(position));
            effectInfo.id = position;
            mItemClick.onItemClick(effectInfo, position);
        }
    }

    public void setDataList(List<String> list) {
        mFilterList.clear();
        mFilterList.add(null);
        mFilterList.addAll(list);
    }
}
