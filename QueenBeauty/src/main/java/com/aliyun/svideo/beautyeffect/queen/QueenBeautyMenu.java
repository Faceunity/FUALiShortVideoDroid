package com.aliyun.svideo.beautyeffect.queen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.aliyun.svideo.base.BaseChooser;
import com.aliyun.svideo.beauty.queen.R;
import com.aliyunsdk.queen.menu.BeautyMenuPanel;

public class QueenBeautyMenu extends BaseChooser {
    private LinearLayout llBlank;
    private BeautyMenuPanel mBeautyMenuPanel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.QUDemoFullStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alivc_queen_dialog_menu_layout, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        llBlank = view.findViewById(R.id.ll_blank);
        mBeautyMenuPanel = view.findViewById(R.id.queen_menu_panel);
        mBeautyMenuPanel.onShowMenu();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        if (mBeautyMenuPanel != null) {
            mBeautyMenuPanel.onShowMenu();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        llBlank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                    解决crash:java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                    原因:after onSaveInstanceState invoke commit,而 show 会触发 commit 操作
                    fragment is added and its state has already been saved，
                    Any operations that would change saved state should not be performed if this method returns true
                */
                if (isStateSaved()) {
                    return;
                }
                // 隐藏菜单
                mBeautyMenuPanel.onHideMenu();
                QueenBeautyMenu.this.dismiss();
            }
        });
    }
}
