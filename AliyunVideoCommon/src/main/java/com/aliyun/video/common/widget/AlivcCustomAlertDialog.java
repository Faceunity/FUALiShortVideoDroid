package com.aliyun.video.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.editor.aliyunvideocommon.R;

/**
 * 自定义的类似AlertDialog弹窗，可以传入dialog图标和message
 */
public class AlivcCustomAlertDialog extends Dialog {
    private ImageView ivDialogIcon;
    private TextView tvDialogMessage;
    private TextView tvCancel;
    private TextView tvConfirm;
    private OnDialogClickListener mDialogClickListener;

    public AlivcCustomAlertDialog(@NonNull Context context) {
        this(context, R.style.TipDialog);
    }

    public AlivcCustomAlertDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AlivcCustomAlertDialog(@NonNull Context context, boolean cancelable,
                                     @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private void initView() {
        ivDialogIcon = (ImageView)findViewById(R.id.iv_dialog_icon);
        tvDialogMessage = (TextView)findViewById(R.id.tv_dialog_message);
        tvCancel = (TextView)findViewById(R.id.tv_cancel);
        tvConfirm = (TextView)findViewById(R.id.tv_confirm);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mDialogClickListener != null) {
                    mDialogClickListener.onConfirm();
                }
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mDialogClickListener != null) {
                    mDialogClickListener.onCancel();
                }
            }
        });
    }

    /**
     * 确认、取消监听
     */
    public interface OnDialogClickListener {
        void onConfirm();

        void onCancel();
    }

    public static class Builder {
        private Context mContext;
        private int iconId = R.mipmap.icon_delete_tips;
        private String message = "提示";
        private String confirm = "确认";
        private String cancel = "取消";
        private OnDialogClickListener dialogClickListener;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public AlivcCustomAlertDialog create() {
            AlivcCustomAlertDialog dialog = new AlivcCustomAlertDialog(mContext);
            dialog.setContentView(R.layout.dialog_alert_custom);
            dialog.initView();
            dialog.ivDialogIcon.setImageResource(iconId);
            dialog.tvDialogMessage.setText(message);
            dialog.tvCancel.setText(cancel);
            dialog.tvConfirm.setText(confirm);
            dialog.mDialogClickListener = dialogClickListener;
            ViewGroup contentWrap = (ViewGroup)dialog.findViewById(R.id.contentWrap);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)contentWrap.getLayoutParams();
            params.width = mContext.getResources().getDimensionPixelSize(R.dimen.alert_dialog_w);
            params.height = mContext.getResources().getDimensionPixelSize(R.dimen.alert_dialog_h);
            contentWrap.setLayoutParams(params);
            return dialog;
        }

        /**
         * 设置需要显示icon的id
         * @param id
         * @return
         */
        public Builder setIconId(int id) {
            iconId = id;
            return this;
        }

        /**
         * 设置dialog显示的内容
         * @param message
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置选中回调监听
         * @param confirm 确认按钮文字提示
         * @param cancel 取消按钮文字提示
         * @param clickListener
         * @return
         */
        public Builder setDialogClickListener(String confirm, String cancel, OnDialogClickListener clickListener) {
            if (!TextUtils.isEmpty(confirm)) {
                this.confirm = confirm;
            }
            if (!TextUtils.isEmpty(cancel)) {
                this.cancel = cancel;
            }
            this.dialogClickListener = clickListener;
            return this;
        }
    }
}
