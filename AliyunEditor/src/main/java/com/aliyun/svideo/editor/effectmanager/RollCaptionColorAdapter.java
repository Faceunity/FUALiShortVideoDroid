package com.aliyun.svideo.editor.effectmanager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aliyun.svideo.editor.R;

import java.util.ArrayList;
import java.util.List;

public class RollCaptionColorAdapter extends RecyclerView.Adapter<RollCaptionColorAdapter.RollCaptionColorViewHolder> {

    private Context mContext;
    private List<Integer> mColorList;
    private OnRollCaptionColorItemClickListener mListener;

    public RollCaptionColorAdapter(Context context){
        this.mContext = context;
        this.mColorList = initColors();
    }

    @Override
    public RollCaptionColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alivc_editor_item_color, parent, false);
        return new RollCaptionColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RollCaptionColorViewHolder holder, final int position) {
        holder.mColorImage.setColorFilter(mColorList.get(position));

        holder.mColorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null){
                    mListener.onItemClick(mColorList.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mColorList == null ? 0 : mColorList.size();
    }

    private List<Integer> initColors() {
        List<Integer> list = new ArrayList<>();
        TypedArray colors = mContext.getResources().obtainTypedArray(R.array.paint_colors);

        int size = colors.length();
        for (int i = 0; i < size; i++) {
            int color = colors.getColor(i, Color.WHITE);
            list.add(color);
        }
        colors.recycle();
        return list;
    }

    public static class RollCaptionColorViewHolder extends RecyclerView.ViewHolder{

        private ImageView mColorImage;

        public RollCaptionColorViewHolder(View itemView) {
            super(itemView);
            mColorImage = (ImageView) itemView.findViewById(R.id.paint_color_image);
        }
    }

    public interface OnRollCaptionColorItemClickListener{
        void onItemClick(int color);
    }

    public void setOnRollCaptionColorItemClickListener(OnRollCaptionColorItemClickListener listener){
        this.mListener = listener;
    }
}
