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

import com.aliyun.common.qupaiokhttp.HttpRequest;
import com.aliyun.common.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.svideo.editor.R;
import com.google.gson.Gson;

/**
 * 云端草稿
 */
public class CloudDraftFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private CloudDraftAdapter mDraftAdapter;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.aliyun_svideo_draft_fragment, null);
        mRecyclerView = view.findViewById(R.id.alivc_draft_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        mDraftAdapter = new CloudDraftAdapter();
        loadData();
        return view;
    }

    public void loadData() {
        HttpRequest.get(DraftListActivity.SERVER_GET_PROJECTS_URL, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String result) {
                try {
                    CloudDraftResult cloudDraftResult = new Gson().fromJson(result, CloudDraftResult.class);
                    if (cloudDraftResult.code == 0) {
                        mDraftAdapter.setData(cloudDraftResult.data);
                        mRecyclerView.setAdapter(mDraftAdapter);
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }
}
