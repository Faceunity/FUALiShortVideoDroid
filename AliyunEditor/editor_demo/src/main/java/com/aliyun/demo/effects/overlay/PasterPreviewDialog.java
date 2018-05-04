/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.overlay;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.aliyun.demo.editor.R;

public class PasterPreviewDialog extends DialogFragment {

    private static final String KEY_URL = "url";
    private static final String KEY_NAME = "name";
    private static final String KEY_ID = "id";

    public static PasterPreviewDialog newInstance(String url, String name, int id){
        PasterPreviewDialog dialog = new PasterPreviewDialog();
        Bundle args=new Bundle();
        args.putString(KEY_URL, url);
        args.putString(KEY_NAME, name);
        args.putInt(KEY_ID, id);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onActivityCreated(arg0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ResourcePreviewStyle);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setCanceledOnTouchOutside(true);
        d.setCancelable(true);
        return d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.aliyun_svideo_layout_paster_preview, container);

        WebView webView = (WebView) view.findViewById(R.id.webview);
        View close = view.findViewById(R.id.close);

        webView.setBackgroundColor(getResources().getColor(android.R.color.black));
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(getArguments().getString(KEY_URL));

        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setCancelable(true);

        return view;
    }



    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        super.onResume();
    }

}
