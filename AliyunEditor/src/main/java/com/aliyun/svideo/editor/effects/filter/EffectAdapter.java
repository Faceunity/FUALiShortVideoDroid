package com.aliyun.svideo.editor.effects.filter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.base.Form.I18nBean;
import com.aliyun.svideo.common.utils.LanguageUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.OnItemTouchListener;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.base.widget.CircularImageView;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.AbstractImageLoaderTarget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zsy_18 data:2018/8/31
 */
public class EffectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements View.OnTouchListener {

    private final static String TAG = EffectAdapter.class.getName();
    private Context mContext;
    private OnItemClickListener mItemClick;
    private OnItemTouchListener mItemTouchListener;
    private GestureDetector mDetector;
    private List<String> mFilterList = new ArrayList<>();
    private int mGroupId;

    public EffectAdapter(Context context) {
        this.mContext = context;
        //此处用手势监听，不通过
        mDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onShowPress(MotionEvent e) {
                super.onShowPress(e);
                Log.i(TAG, "onShowPress" );
                //防止快速点击时引起的问题
                if (isAdding) {
                    return ;
                }
                if (mItemTouchListener != null && pressView != null) {
                    FilterViewHolder viewHolder = (FilterViewHolder)pressView.getTag();
                    int position = viewHolder.getAdapterPosition();
                    viewHolder.mImage.setVisibility(View.GONE);
                    viewHolder.mIvSelectState.setVisibility(View.VISIBLE);
                    viewHolder.mImage.setSelected(true);
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.FILTER_EFFECT;
                    effectInfo.setPath(mFilterList.get(position));
                    Source source = new Source(mFilterList.get(position));
                    source.setId(String.valueOf(position));
                    if (source.getPath() != null && source.getPath().contains(File.separator)) {
                        //groupID 0和1为本地资源
                        boolean isApp = mGroupId <= EditorCommon.QU_ANIMATION_SPLIT_SCREEN_FILTER_ID;
                        String name = source.getPath().substring(source.getPath().lastIndexOf(File.separator) + 1);
                        source.setURL(AlivcResUtil.getResUri(isApp ? "app" : "cloud",
                                                             AlivcResUtil.TYPE_ANIMATION_EFFECTS, String.valueOf(mGroupId), name));
                    }

                    effectInfo.setSource(source);
                    effectInfo.id = position;
                    mItemTouchListener.onTouchEvent(OnItemTouchListener.EVENT_DOWN, position, effectInfo);
                    isAdding = true;
                }
            }
        });

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alivc_editor_item_effect, parent, false);
        FilterViewHolder filterViewHolder = new FilterViewHolder(view);
        filterViewHolder.frameLayout = (FrameLayout)view.findViewById(R.id.resource_image);
        filterViewHolder.setIsRecyclable(false);
        return filterViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final FilterViewHolder filterViewHolder = (FilterViewHolder)holder;
        String name = mContext.getString(R.string.alivc_editor_dialog_animate_revoke);
        String path = mFilterList.get(position);
        if (path == null || "".equals(path)) {
            filterViewHolder.mImage.setImageResource(R.mipmap.alivc_svideo_icon_effect_cancel);
            filterViewHolder.itemView.setOnTouchListener(null);
            filterViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClick != null) {
                        int position = holder.getAdapterPosition();
                        EffectInfo effectInfo = new EffectInfo();
                        effectInfo.type = UIEditorPage.FILTER_EFFECT;
                        effectInfo.setPath(mFilterList.get(position));
                        Source source = new Source(mFilterList.get(position));
                        source.setId(String.valueOf(position));
                        effectInfo.setSource(source);
                        effectInfo.id = position;
                        mItemClick.onItemClick(effectInfo, position);
                    }
                }
            });
        } else {
            filterViewHolder.itemView.setOnTouchListener(this);
            name = getFilterName(path);
            new ImageLoaderImpl().loadImage(mContext, path + "/icon.png")
            .into(filterViewHolder.mImage, new AbstractImageLoaderTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource) {
                    filterViewHolder.mImage.setImageDrawable(resource);
                }
            });
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
        Log.d(TAG, "getAction" + event.getAction() );
        mDetector.onTouchEvent(event);
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked) {
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            if (!isAdding) {
                pressView = null;
                return true;
            }
            if (v != pressView) {
                return true;
            }
            if (mItemTouchListener != null) {
                FilterViewHolder viewHolder = (FilterViewHolder)v.getTag();
                int position = viewHolder.getAdapterPosition();
                EffectInfo effectInfo = new EffectInfo();
                effectInfo.type = UIEditorPage.FILTER_EFFECT;
                effectInfo.setPath(mFilterList.get(position));
                Source source = new Source(mFilterList.get(position));
                source.setId(String.valueOf(position));
                effectInfo.setSource(source);
                if (source.getPath() != null && source.getPath().contains(File.separator)) {
                    boolean isApp = source.getPath().contains("aliyun_svideo_animation_filter/");
                    String name = source.getPath().substring(source.getPath().lastIndexOf(File.separator) + 1);
                    source.setURL(AlivcResUtil.getResUri(isApp ? "app" : "cloud",
                                                         AlivcResUtil.TYPE_ANIMATION_EFFECTS, String.valueOf(mGroupId), name));
                }
                effectInfo.id = position;
                mItemTouchListener.onTouchEvent(OnItemTouchListener.EVENT_UP,
                                                position, effectInfo);
                viewHolder.mImage.setVisibility(View.VISIBLE);
                viewHolder.mIvSelectState.setVisibility(View.GONE);
                isAdding = false;
            }
            pressView = null;
            Log.e(TAG, "ACTION_UP");
            break;
        case MotionEvent.ACTION_DOWN:
            //如果特效正在被使用，不允许替换
            Log.e(TAG, "ACTION_DOWN");
            if (isAdding) {
                return false;
            }
            if (pressView == null) {
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

    public void setDataList(int groupId, List<String> list) {
        mGroupId = groupId;
        mFilterList.clear();
        mFilterList.addAll(list);
    }

    /**
     * 获取滤镜名称 适配系统语言/中文或其他
     * @param path 滤镜文件目录
     * @return name
     */
    private String getFilterName(String path) {
        String name = "";
        I18nBean effectI18n = EditorCommon.getCurrentEffectI18n(path, "name");
        if (effectI18n == null) {
            if (LanguageUtils.isCHEN(mContext)) {
                path = path + "/config.json";
            } else {
                String pathEn = path + "/configEn.json";
                if (new File(pathEn).exists()) {
                    path = pathEn;
                } else {
                    path = path + "/config.json";
                }
            }
            StringBuilder var2 = new StringBuilder();
            File var3 = new File(path);

            try {
                FileReader var4 = new FileReader(var3);

                int var7;
                while ((var7 = var4.read()) != -1) {
                    var2.append((char)var7);
                }

                var4.close();
            } catch (IOException var6) {
                var6.printStackTrace();
            }

            try {
                JSONObject var4 = new JSONObject(var2.toString());
                name = var4.optString("name");
            } catch (JSONException var5) {
                var5.printStackTrace();
            }

            return name;
        }

        if (LanguageUtils.isCHEN(mContext)) {
            return effectI18n.getZh_cn();
        } else {
            return effectI18n.getEn();
        }


    }
}
