package com.aliyun.svideo.editor.draft;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideosdk.editor.draft.AliyunDraft;
import com.aliyun.svideosdk.editor.draft.AliyunDraftListCallback;
import com.aliyun.svideosdk.editor.draft.AliyunDraftManager;
import com.aliyun.svideosdk.editor.draft.AliyunTemplateDraftManager;

import java.util.List;

/**
 * 本地草稿箱
 */
public class LocalDraftFragment extends Fragment {
    private boolean isTemplateDraft = false;
    private RecyclerView mRecyclerView;
    private LocalDraftAdapter mDraftAdapter;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.aliyun_svideo_draft_fragment, null);
        mRecyclerView = view.findViewById(R.id.alivc_draft_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        isTemplateDraft = getArguments().getInt("TAB_INDEX") == 1;
        mDraftAdapter = new LocalDraftAdapter(isTemplateDraft);
        loadData();
        return view;
    }

    public void loadData() {
        if (isTemplateDraft) {
            //模板草稿
            AliyunTemplateDraftManager.getInstance(mRecyclerView.getContext())
                    .getDraftListByAsync(new AliyunDraftListCallback() {
                        @Override
                        public void onFailure(final String msg) {

                        }

                        @Override
                        public void onSuccess(final List<AliyunDraft> list) {
                            mDraftAdapter.setData(list);
                            mRecyclerView.setAdapter(mDraftAdapter);
                        }
                    });
        } else {
            //普通草稿
            AliyunDraftManager.getInstance(mRecyclerView.getContext())
                    .getDraftListByAsync(new AliyunDraftListCallback() {
                        @Override
                        public void onFailure(final String msg) {

                        }

                        @Override
                        public void onSuccess(final List<AliyunDraft> list) {
                            mDraftAdapter.setData(list);
                            mRecyclerView.setAdapter(mDraftAdapter);
                        }
                    });
        }
    }
}
