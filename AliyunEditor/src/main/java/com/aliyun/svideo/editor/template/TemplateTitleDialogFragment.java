package com.aliyun.svideo.editor.template;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.aliyun.svideo.editor.R;

public class TemplateTitleDialogFragment extends DialogFragment {
    private OnTitleListener mOnTitleListener;

    public interface OnTitleListener {
        void onTitle(String title);
    }

    public void setOnTitleListener(final OnTitleListener onTitleListener) {
        mOnTitleListener = onTitleListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alivc_editor_template_title_config, null);
        final EditText titleEdt = view.findViewById(R.id.alivc_template_title_edt);
        builder.setView(view)
               .setPositiveButton(R.string.alivc_svideo_menu_positive,
                                  new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int id) {
                                          if (mOnTitleListener != null) {
                                              mOnTitleListener.onTitle(titleEdt.getText().toString());
                                          }
                                      }
                                  }).setNegativeButton(R.string.alivc_svideo_menu_cancel, null);
        return builder.create();
    }
}
