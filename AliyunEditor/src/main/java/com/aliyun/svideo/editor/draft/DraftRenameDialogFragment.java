package com.aliyun.svideo.editor.draft;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.aliyun.svideo.editor.R;

public class DraftRenameDialogFragment extends DialogFragment {
    private OnRenameListener mOnRenameListener;
    private String mDraftName;

    public interface OnRenameListener {
        void onRename(String name);
    }

    public void setOnRenameListener(final OnRenameListener onRenameListener) {
        mOnRenameListener = onRenameListener;
    }

    public void setDraftName(final String draftName) {
        mDraftName = draftName;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alivc_editor_draft_rename_dialog, null);
        final EditText editText = view.findViewById(R.id.alivc_draft_rename_edit);
        editText.setText(mDraftName);
        builder.setView(view)
               .setPositiveButton("确定",
                                  new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int id) {
                                          if (mOnRenameListener != null) {
                                              mOnRenameListener.onRename(editText.getText()
                                                                                 .toString());
                                          }
                                      }
                                  }).setNegativeButton("取消", null);
        return builder.create();
    }
}
