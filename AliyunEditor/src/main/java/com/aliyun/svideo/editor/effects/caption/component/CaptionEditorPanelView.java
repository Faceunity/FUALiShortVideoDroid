package com.aliyun.svideo.editor.effects.caption.component;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.adapter.holder.CaptionAnimationPanelViewHolder;
import com.aliyun.svideo.editor.effects.caption.adapter.holder.CaptionBubblePanelViewHolder;
import com.aliyun.svideo.editor.effects.caption.adapter.holder.CaptionCoolTextPanelViewHolder;
import com.aliyun.svideo.editor.effects.caption.adapter.holder.CaptionStylePanelViewHolder;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionEditorViewPagerAdapter;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;
import com.google.android.material.tabs.TabLayout;


public class CaptionEditorPanelView extends FrameLayout {
    private static final String TAG = "CaptionEditorPanelView";

    private View parentView;
    private EditText mEditView;
    private View mBack;
    private View mConfirm;
    private TabLayout mTabLayout;
    private ViewPager mViewPage;
    private OnCaptionChooserStateChangeListener mOnCaptionChooserStateChangeListener;
    private final MyTextWatcher textWatch = new MyTextWatcher();


    //字幕操作tab
    private static final int[] ID_TITLE_ARRAY = {
            R.string.alivc_editor_dialog_caption_style,
            R.string.alivc_editor_effect_text_bubble,
            R.string.alivc_editor_effect_text_flourish,
            R.string.alivc_editor_dialog_caption_animation
    };

    private CaptionStylePanelViewHolder stylePanelViewHolder;
    private CaptionBubblePanelViewHolder captionBubblePanelViewHolder;
    private CaptionAnimationPanelViewHolder captionAnimationPanelViewHolder;
    private CaptionCoolTextPanelViewHolder captionCoolTextPanelViewHolder;
    private CaptionEditorViewPagerAdapter captionEditorViewPagerAdapter;
    private int realDisplayHeight;

    public CaptionEditorPanelView(Context context, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context);
        mOnCaptionChooserStateChangeListener = onCaptionChooserStateChangeListener;
        initView();
    }

    private void initView() {
        parentView = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_text_panel, this, true);
        mEditView = parentView.findViewById(R.id.fl_editText);
        mBack = parentView.findViewById(R.id.iv_back);
        mConfirm = parentView.findViewById(R.id.iv_confirm);
        realDisplayHeight = ScreenUtils.getDisplayHeight((Activity) getContext());

        initViewPage();
        initTableView();
        setOnClick();
        showKeyboard();
    }

    private void initViewPage() {
        mViewPage = parentView.findViewById(R.id.viewpager);
        mViewPage.setOffscreenPageLimit(3);
        captionEditorViewPagerAdapter = new CaptionEditorViewPagerAdapter();
        mViewPage.setAdapter(captionEditorViewPagerAdapter);
        stylePanelViewHolder = new CaptionStylePanelViewHolder(getContext(),
                getContext().getString(R.string.alivc_editor_dialog_caption_style), mOnCaptionChooserStateChangeListener);
        captionBubblePanelViewHolder = new CaptionBubblePanelViewHolder(getContext(),
                getContext().getString(R.string.alivc_editor_effect_text_bubble), mOnCaptionChooserStateChangeListener);
        captionCoolTextPanelViewHolder = new CaptionCoolTextPanelViewHolder(getContext(), getContext().getString(R.string.alivc_editor_effect_text_flourish),
                mOnCaptionChooserStateChangeListener);
        captionAnimationPanelViewHolder = new CaptionAnimationPanelViewHolder(getContext(),
                getContext().getString(R.string.alivc_editor_dialog_caption_animation), mOnCaptionChooserStateChangeListener);

        captionEditorViewPagerAdapter.addViewHolder(stylePanelViewHolder);
        captionEditorViewPagerAdapter.addViewHolder(captionBubblePanelViewHolder);
        captionEditorViewPagerAdapter.addViewHolder(captionCoolTextPanelViewHolder);
        captionEditorViewPagerAdapter.addViewHolder(captionAnimationPanelViewHolder);
        captionEditorViewPagerAdapter.notifyDataSetChanged();
    }


    private void setSoftInputAdjustNothing() {
        ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    /*软键盘以顶起当前界面的形式出现, 注意这种方式会使得当前布局的高度发生变化，触发当前布局onSizeChanged方法回调，这里前后高度差就是软键盘的高度了*/
    private void setSoftInputAdjustResize() {
        ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnClick() {
        mEditView.addTextChangedListener(textWatch);
        addOnSoftKeyBoardVisbleListener();
        if (mBack != null) {
            mBack.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setVisibility(GONE);
                }
            });
        }

        mConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnCaptionChooserStateChangeListener != null) {
                    mOnCaptionChooserStateChangeListener.onCaptionConfirm();
                }
            }
        });

    }

    public void refreshData() {
        if (mOnCaptionChooserStateChangeListener != null) {
            AliyunPasterControllerCompoundCaption aliyunPasterController = mOnCaptionChooserStateChangeListener.getAliyunPasterController();
            if (aliyunPasterController != null) {
                String text = aliyunPasterController.getText();
                mEditView.removeTextChangedListener(textWatch);
                mEditView.setText(text);
                mEditView.addTextChangedListener(textWatch);
            }
        }

    }

    private final Runnable lazyGetTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mEditView == null) {
                return;
            }
            Editable editable = mEditView.getText();
            String content = null;
            if (editable != null) {
                content = editable.toString();
            }
            if (TextUtils.isEmpty(content)) {
                content = getContext().getString(R.string.alivc_editor_effect_text_default);
            }
            int maxLength = 0;
            if (!isTextOnly()) {
                maxLength = 10;
            }
//            else {
//                //纯字幕限制90个字
//                maxLength = 90;
//            }
            // 限定EditText只能输入maxLength个数字
            if (maxLength != 0 && content.length() > maxLength) {
                // 默认光标在最前端，所以当输入第11个数字的时候，删掉（光标位置从11-1到11）的数字，这样就无法输入超过10个以后的数字
                ToastUtils.show(getContext(), R.string.alivc_editor_dialog_caption_tip_text_limit);
                int deleteCount = content.length() - maxLength;
                int selIndex = mEditView.getSelectionStart();
                int startIndex = selIndex - deleteCount;
                int endIndex = selIndex;
                //添加条件判断，防止出现崩溃
                if (startIndex < 0) {
                    startIndex = 0;
                }
                if (endIndex > content.length()) {
                    endIndex = content.length();
                }
                if (startIndex > endIndex) {
                    startIndex = endIndex;
                }
                if (editable != null) {
                    editable.delete(startIndex, endIndex);
                }

            }

            if (mOnCaptionChooserStateChangeListener != null) {
                mOnCaptionChooserStateChangeListener.onCaptionTextChanged(content);
            }
        }
    };

    /**
     * 气泡下载页返回刷新数据
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode：" + resultCode);
        if (captionBubblePanelViewHolder != null) {
            captionBubblePanelViewHolder.resourceChanged();
        }
        if (stylePanelViewHolder != null) {
            stylePanelViewHolder.resourceChanged();
        }
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            ThreadUtils.removeCallbacks(lazyGetTextRunnable);
            ThreadUtils.runOnUiThread(lazyGetTextRunnable, 100);

        }


    }


    /**
     * 初始化新View
     */
    private void initTableView() {
        mTabLayout = parentView.findViewById(R.id.tl_tab);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        for (int i = 0; i < ID_TITLE_ARRAY.length; i++) {
            View item = LayoutInflater.from(parentView.getContext()).inflate(R.layout.alivc_editor_dialog_text_item_tab, (ViewGroup) parentView, false);
            ((TextView) item.findViewById(R.id.tv_title)).setText(ID_TITLE_ARRAY[i]);
            mTabLayout.addTab(mTabLayout.newTab().setCustomView(item));
        }
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(parentView.getContext(), R.color.alivc_common_bg_cyan_light));
        mTabLayout.setupWithViewPager(mViewPage);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                closeKeyboard();
                switch (position) {
                    case 0:
                        stylePanelViewHolder.onTabClick();
                        break;
                    case 1:
                        captionBubblePanelViewHolder.onTabClick();
                        break;
                    case 2:
                        captionCoolTextPanelViewHolder.onTabClick();
                        break;
                    case 3:
                        captionAnimationPanelViewHolder.onTabClick();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private boolean isTextOnly() {
        if (mOnCaptionChooserStateChangeListener != null) {
            AliyunPasterControllerCompoundCaption aliyunPasterController = mOnCaptionChooserStateChangeListener.getAliyunPasterController();
            return CaptionManager.isTextOnly(aliyunPasterController);
        }
        return true;
    }

    @Override
    public void setVisibility(int visibility) {
        closeKeyboard();
        if (visibility == GONE) {
            removeSoftKeyBoardVisbleListener();
        }
        if (getVisibility() == VISIBLE && visibility == GONE) {
            if (mOnCaptionChooserStateChangeListener != null) {
                mOnCaptionChooserStateChangeListener.onCaptionCancel();
            }
        }
        notifyCaptionControllerChanged(visibility);
        super.setVisibility(visibility);

    }

    /**
     * 刷新数据
     *
     * @param visibility
     */
    private void notifyCaptionControllerChanged(int visibility) {
        if (getVisibility() == GONE && visibility == VISIBLE) {
            if (mTabLayout != null && captionEditorViewPagerAdapter != null) {
                captionEditorViewPagerAdapter.notifyDataSetChanged(mTabLayout.getSelectedTabPosition());
            }

        }
    }

    public void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && inputManager.isActive() && mEditView != null) {
            inputManager.hideSoftInputFromWindow(mEditView.getWindowToken(), 0);
        }
    }

    public void showKeyboard() {
        final InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && mEditView != null) {
            mEditView.post(new Runnable() {
                @Override
                public void run() {
                    mEditView.requestFocus();
                    inputManager.showSoftInput(mEditView, 0);
                }
            });

        }
    }


    /**
     * 监听键盘高度变化,设置文字编辑面板高度
     */
    private void addOnSoftKeyBoardVisbleListener() {
        final View decorView = ((Activity) getContext()).getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

    }

    private void removeSoftKeyBoardVisbleListener() {
        Activity activity = (Activity) getContext();
        activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    private final ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Activity activity = (Activity) getContext();
            int displayHeight  =ScreenUtils.getDisplayHeight(activity);
            final int keyboardHeight = realDisplayHeight - displayHeight;
            final ViewGroup.LayoutParams layoutParams = mViewPage.getLayoutParams();
            if (keyboardHeight > 130 && keyboardHeight != layoutParams.height) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSoftInputAdjustNothing();
                        layoutParams.height = keyboardHeight;
                        mViewPage.setLayoutParams(layoutParams);
                    }
                },200);

            }
        }
    };


}
