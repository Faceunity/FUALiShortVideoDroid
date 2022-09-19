package com.aliyun.svideo.editor.draft;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.aliyun.svideo.editor.R;

public class CloudDraftConfigDialogFragment extends DialogFragment {
    private OnCloudDraftConfigListener mOnCloudDraftConfigListener;
    private String mServerUrl;
    private String mUserName;

    public interface OnCloudDraftConfigListener {
        void onConfig(String serverUrl, String name);
    }

    public void setServerUrl(final String serverUrl) {
        mServerUrl = serverUrl;
    }

    public void setUserName(final String userName) {
        mUserName = userName;
    }

    public void setOnCloudDraftConfigListener(final OnCloudDraftConfigListener onCloudDraftConfigListener) {
        mOnCloudDraftConfigListener = onCloudDraftConfigListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alivc_editor_cloud_draft_config, null);
        final EditText editServerUrlText = view.findViewById(R.id.alivc_draft_server_url_edit);
        editServerUrlText.setText(mServerUrl);
        final EditText editServerUserNameText = view.findViewById(R.id.alivc_draft_user_name_edit);
        editServerUserNameText.setText(mUserName);
        builder.setView(view)
               .setPositiveButton("确定",
                                  new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int id) {
                                          if (mOnCloudDraftConfigListener != null) {
                                              mOnCloudDraftConfigListener.onConfig(editServerUrlText
                                                                                     .getText()
                                                                                     .toString(),
                                                                                   editServerUserNameText
                                                                                     .getText()
                                                                                     .toString());
                                          }
                                      }
                                  }).setNegativeButton("取消", null);
        return builder.create();
    }
}
