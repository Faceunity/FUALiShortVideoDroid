/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.ColorViewHolder;
import com.aliyun.svideo.editor.editor.ColorViewPagerAdapter;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.util.FixedToastUtils;
import com.aliyun.svideo.editor.widget.AutoResizingEditText;
import com.aliyun.svideo.editor.widget.WheelView;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.svideo.base.widget.control.ViewStack;
import com.aliyun.svideo.sdk.external.struct.effect.ActionBase;
import com.aliyun.svideo.sdk.external.struct.effect.ActionFade;
import com.aliyun.svideo.sdk.external.struct.effect.ActionScale;
import com.aliyun.svideo.sdk.external.struct.effect.ActionTranslate;
import com.aliyun.svideo.sdk.external.struct.effect.ActionWipe;
import com.aliyun.svideo.sdk.external.struct.form.FontForm;
import com.aliyun.video.common.utils.image.ImageLoaderImpl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TextDialog extends DialogFragment {

    private AutoResizingEditText mEditView;
    private View mConfirm;
    private TextView textLimit;
    ActionBase mActionBase;
    private static final int FONT_TYPE = 1;
    List<FontForm> mFormList;
    List<WheelView.DataModel> mFontDataList;

    //字幕操作tab
    private static final int[] ID_TITLE_ARRAY = {R.string.caption_effect_bottom_keyboard,
        R.string.caption_effect_bottom_color, R.string.caption_effect_bottom_font,
        R.string.caption_effect_bottom_animation};
    private static final int[] ID_ICON_ARRAY = {R.mipmap.aliyun_svideo_icon_keyboard, R.mipmap.aliyun_svideo_icon_color,
        R.mipmap.aliyun_svideo_icon_font, R.mipmap.aliyun_svideo_icon_animation};

    //字幕动效
    public static final int EFFECT_NONE = 0, EFFECT_UP = 1, EFFECT_RIGHT = 4, EFFECT_LEFT = 3, EFFECT_DOWN = 2,
        EFFECT_LINEARWIPE = 6, EFFECT_FADE = 5,EFFECT_SCALE = 7;
    public static final int[] POSITION_FONT_ANIM_ARRAY = {EFFECT_NONE, EFFECT_UP, EFFECT_RIGHT, EFFECT_LEFT,
        EFFECT_DOWN, EFFECT_LINEARWIPE, EFFECT_FADE,EFFECT_SCALE};
    private int mAnimationSelectPosition;

    private static final String NAME_SYSTEM_FONT = "系统字体";


    GridView fontList;
    FontAdapter fontAdapter;
    private FrameLayout pageContainer;

    private EditTextInfo mEditInfo;

    private int mSelectedIndex;

    private OnStateChangeListener mOnStateChangeListener;
    private View mBack;
    private TabLayout mTabLayout;
    private ViewStack mPagerViewStack;
    private WheelView mWva;
    public static boolean sIsShowing = false;
    /**
     * 是否倒放，倒放时特效不支持
     */
    private boolean mUseInvert = false;

    public static TextDialog newInstance(EditTextInfo editInfo,boolean isInvert) {
        if (sIsShowing) {
            return null;
        }
        sIsShowing = true;
        TextDialog dialog = new TextDialog();
        Bundle b = new Bundle();
        b.putSerializable("edit", editInfo);
        b.putBoolean("invert",isInvert);
        dialog.setArguments(b);
        return dialog;
    }

    public static class EditTextInfo implements Serializable {

        public String text;
        public int textStrokeColor;
        public String font;
        public int textColor;
        public int textWidth;
        public int textHeight;
        public int dTextColor;
        public int dTextStrokeColor;
        public boolean isTextOnly;
        public ActionBase mAnimation;
        public int mAnimationSelect;//字体动画选择的selectPosition
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mEditView.removeTextChangedListener(textWatch);
        callbackResult();
        sIsShowing = false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog != null) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View contentView = View.inflate(getActivity(), R.layout.aliyun_svideo_qupai_row_text_bottom, null);

        mEditInfo = (EditTextInfo) getArguments().getSerializable("edit");
        mUseInvert = getArguments().getBoolean("invert");
        if (mEditInfo == null) {
            dismiss();
            return contentView;
        }

        initTableView(contentView);
        mEditView = contentView.findViewById(R.id.qupai_overlay_content_text);
        mConfirm = contentView.findViewById(R.id.iv_confirm);
        mBack = contentView.findViewById(R.id.iv_back);

        mEditView.setTextOnly(mEditInfo.isTextOnly);
        mEditView.setText(mEditInfo.text);
        mEditView.setFontPath(mEditInfo.font);
        mEditView.setCurrentColor(mEditInfo.textColor);
        mEditView.setTextStrokeColor(mEditInfo.textStrokeColor);
        if (!mEditInfo.isTextOnly) {
            mEditView.setTextWidth(mEditInfo.textWidth);
            mEditView.setTextHeight(mEditInfo.textHeight);
        }

        mEditView.setTextSize(
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11, getResources().getDisplayMetrics()));

        pageContainer = (FrameLayout) contentView.findViewById(R.id.container);
        mPagerViewStack = new ViewStack(View.GONE);
        mPagerViewStack.addView(new View(getActivity()));
        mPagerViewStack.addView(contentView.findViewById(R.id.color_container));
        //mPagerViewStack.addView(contentView.findViewById(R.id.font_layout));
        mPagerViewStack.addView(contentView.findViewById(R.id.font_layout_new));
        mPagerViewStack.addView(contentView.findViewById(R.id.font_animation));

        initWLView(contentView);

        TabLayout colorTabHost = (TabLayout) contentView.findViewById(R.id.color_tab_host);
        ViewPager colorViewPager = (ViewPager) contentView.findViewById(R.id.color_viewpager);
        ColorViewPagerAdapter colorPagerAdapter = new ColorViewPagerAdapter();

        initFontAnimationView(contentView);
        ColorViewHolder colorHolder = new ColorViewHolder(getActivity().getApplicationContext(),
            getString(R.string.qupai_effect_text_color), false, mEditInfo.dTextColor);
        ColorViewHolder strokeHolder = new ColorViewHolder(getActivity().getApplicationContext(),
            getString(R.string.qupai_effect_text_stroke), true, mEditInfo.dTextStrokeColor);

        colorHolder.setItemClickListener(new ColorViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(ColorViewHolder.ColorItem item) {
                mEditView.setCurrentColor(item.color);
            }
        });
        strokeHolder.setItemClickListener(new ColorViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(ColorViewHolder.ColorItem item) {
                mEditView.setTextStrokeColor(item.strokeColor);
            }
        });
        colorPagerAdapter.addViewHolder(colorHolder);
        colorPagerAdapter.addViewHolder(strokeHolder);
        colorViewPager.setAdapter(colorPagerAdapter);
        colorTabHost.setupWithViewPager(colorViewPager);

        fontList = (GridView) contentView.findViewById(R.id.font_list);
        fontAdapter = new FontAdapter();

        fontAdapter.setData(getFontList());
        fontList.setAdapter(fontAdapter);
        fontList.setItemChecked(mSelectedIndex, true);
        fontList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FontForm font = (FontForm) parent.getItemAtPosition(position);
                String path = font.getUrl();
                File f = new File(path);
                boolean ifd = f.exists() && f.isDirectory();
                if (!ifd) {
                    f = new File(path + "tmp");
                    ifd = f.exists() && f.isDirectory();
                }

                if (ifd) {
                    String[] fds = f.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".ttf");
                        }
                    });
                    if (fds.length > 0) {
                        mEditView.setFontPath(new File(f, fds[0]).getAbsolutePath());
                    }

                }

                fontList.setItemChecked(position, true);

            }
        });

        LayoutParams localLayoutParams = getDialog().getWindow().getAttributes();
        localLayoutParams.gravity = Gravity.BOTTOM;
        localLayoutParams.width = LayoutParams.MATCH_PARENT;

        SharedPreferences sp = getActivity().getSharedPreferences("AppGlobalSetting", 0);
        boolean isFontHasNew = sp.getBoolean("font_category_new", false);
        setOnClick();
        textLimit = (TextView) contentView.findViewById(R.id.message);
        requestFocusForKeyboard();
        if (isTextOnly()) {
            textLimit.setVisibility(View.GONE);
        } else {
            CharSequence text = mEditView.getText();
            if (TextUtils.isEmpty(text)) {
                textLimit.setText("0 / 10");
            } else {
                textLimit.setText(count(text.toString()) + " / 10");
            }
        }

        return contentView;
    }

    /**
     * 字体的展示
     *
     * @param contentView 根容器
     */
    private void initWLView(View contentView) {

        mFormList = getFontList();
        if (mFormList == null) {
            return;
        }
        mFontDataList = new ArrayList<>(mFormList.size());
        for (FontForm fontForm : mFormList) {
            FontDateBean fontDateBean = new FontDateBean(fontForm.getName());
            mFontDataList.add(fontDateBean);
        }

        mWva = contentView.findViewById(R.id.font_custom_view);
        mWva.setOffset(2);
        mWva.setItems(mFontDataList);

        mWva.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                if (selectedIndex == 0) {
                    //系统字体
                    mEditView.setTypeface(Typeface.DEFAULT);
                }else {
                    //非系统字体

                    FontForm font = mFormList.get(selectedIndex);
                    String path = font.getUrl();
                    File f = new File(path);
                    boolean ifd = f.exists() && f.isDirectory();
                    if (!ifd) {
                        f = new File(path + "tmp");
                        ifd = f.exists() && f.isDirectory();
                    }

                    if (ifd) {
                        String[] fds = f.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.toLowerCase().endsWith(".ttf");
                            }
                        });
                        if (fds.length > 0) {
                            mEditView.setFontPath(new File(f, fds[0]).getAbsolutePath());
                        }
                    }
                }
                Log.d("TAG", "selectedIndex: " + selectedIndex + ", item: " + item);
            }
        });
    }

    /**
     * 初始化字体动画View
     *
     * @param contentView 布局根容器
     */
    private void initFontAnimationView(View contentView) {

        RecyclerView recyclerView = contentView.findViewById(R.id.font_animation_view);
        FontAnimationAdapter fontAnimationAdapter = new FontAnimationAdapter(contentView.getContext());
        fontAnimationAdapter.setSelectPosition(mEditInfo.mAnimationSelect);
        fontAnimationAdapter.setOnItemClickListener(mOnItemClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(contentView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new SpaceItemDecoration(
            contentView.getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        recyclerView.setAdapter(fontAnimationAdapter);

    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int animPosition) {
            if (mUseInvert){
                FixedToastUtils.show(mEditView.getContext(),mEditView.getContext().getString(R.string.caption_effect_not_support_invert));
            }
            mAnimationSelectPosition = animPosition;

            switch (animPosition) {
                case EFFECT_NONE:
                    mActionBase = null;
                    break;
                case EFFECT_UP:
                    mActionBase = new ActionTranslate();
                    ((ActionTranslate) mActionBase).setToPointY(1f);
                    break;
                case EFFECT_RIGHT:
                    mActionBase = new ActionTranslate();
                    ((ActionTranslate) mActionBase).setToPointX(1f);
                    break;
                case EFFECT_LEFT:
                    mActionBase = new ActionTranslate();
                    ((ActionTranslate) mActionBase).setToPointX(-1f);
                    break;
                case EFFECT_DOWN:
                    mActionBase = new ActionTranslate();
                    ((ActionTranslate) mActionBase).setToPointY(-1f);
                    break;
                case EFFECT_SCALE:
                    mActionBase = new ActionScale();
                    ((ActionScale) mActionBase).setFromScale(1f);
                    ((ActionScale) mActionBase).setToScale(0.25f);
                    break;
                case EFFECT_LINEARWIPE:
                    mActionBase = new ActionWipe();
                    ((ActionWipe) mActionBase).setWipeMode(ActionWipe.WIPE_MODE_DISAPPEAR);
                    ((ActionWipe) mActionBase).setDirection(ActionWipe.DIRECTION_RIGHT);
                    break;
                case EFFECT_FADE:
                    mActionBase = new ActionFade();
                    ((ActionFade) mActionBase).setFromAlpha(1.0f);
                    ((ActionFade) mActionBase).setToAlpha(0.2f);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 初始化新View
     *
     * @param contentView 内容View
     */
    private void initTableView(View contentView) {
        mTabLayout = contentView.findViewById(R.id.tl_tab);

        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        int length = ID_TITLE_ARRAY.length;
        if (!mEditInfo.isTextOnly) {
            length -= 1;
        }
        for (int i = 0; i < length; i++) {
            View item = LayoutInflater.from(contentView.getContext()).inflate(R.layout.aliyun_svideo_row_text_bottom_item, (ViewGroup) contentView, false);
            ((ImageView) item.findViewById(R.id.iv_icon)).setImageResource(ID_ICON_ARRAY[i]);
            ((TextView) item.findViewById(R.id.tv_title)).setText(ID_TITLE_ARRAY[i]);
            mTabLayout.addTab(mTabLayout.newTab().setCustomView(item));
        }

        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(contentView.getContext(), R.color.effect_font_bottom_tab_indicator));
        mTabLayout.addOnTabSelectedListener(mOnTabSelectedListener);
    }

    /**
     * TabLayout的点击监听
     */
    private TabLayout.OnTabSelectedListener mOnTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int checkedIndex = tab.getPosition();
            if (checkedIndex == 0) {
                pageContainer.setVisibility(View.GONE);
                mEditView.setEnabled(true);
                openKeyboard();
            } else {
                pageContainer.setVisibility(View.VISIBLE);
                closeKeyboard();
                mPagerViewStack.setActiveIndex(checkedIndex);
                if (checkedIndex == 1) {
                    if (fontList.getCheckedItemPosition() == -1) {
                        int position = ((FontAdapter) fontList.getAdapter()).getLastCheckedPosition(null);
                        fontList.setItemChecked(position, true);
                        fontList.smoothScrollToPosition(position);
                    }
                } else {
                    mEditView.setEnabled(false);
                }
                if (checkedIndex == 2 && mWva != null){
                    mWva.setSelection(mSelectedIndex);
                }

            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        openKeyboard();
    }

    private List<FontForm> getFontList() {
        int index = 0;
        List<FileDownloaderModel> list = DownloaderManager.getInstance().getDbController().getResourceByType(FONT_TYPE);
        List<FontForm> fonts = new ArrayList<>();
        FontForm fontForm = new FontForm();
        fontForm.setName(NAME_SYSTEM_FONT);
        fonts.add(fontForm);
        for (FileDownloaderModel m : list) {
            FontForm font = new FontForm();
            font.setId(m.getId());
            font.setName(m.getName());
            font.setBanner(m.getBanner());
            font.setUrl(m.getPath());
            fonts.add(font);
            if ((m.getPath() + "/font.ttf").equals(mEditInfo.font)) {
                mSelectedIndex = index;
            }
            index++;
        }

        return fonts;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }

    ArrayList<BackgroundColorSpan> masks = new ArrayList<>();

    private String filterComposingText(Editable s) {
        StringBuilder sb = new StringBuilder();

        int composingStart = 0;
        int composingEnd = 0;

        Object[] sps = s.getSpans(0, s.length(), Object.class);
        if (sps != null) {
            for (int i = sps.length - 1; i >= 0; i--) {
                final Object o = sps[i];
                final int fl = s.getSpanFlags(o);
                Log.d("EDITTEXT", "SpanFlag : " + fl + " is composing" + (fl & Spanned.SPAN_COMPOSING));
                if ((fl & Spanned.SPAN_COMPOSING) != 0) {
                    composingStart = s.getSpanStart(o);
                    composingEnd = s.getSpanEnd(o);
                    Log.d("EDITTEXT", "startAnimation : " + composingStart + " end : " + composingEnd);
                    break;
                }
            }
        }

        sb.append(s.subSequence(0, composingStart));
        sb.append(s.subSequence(composingEnd, s.length()));
        if (composingStart == composingEnd) {
            if (masks.size() > 0) {
                for (BackgroundColorSpan mask : masks) {
                    s.removeSpan(mask);
                }
                masks.clear();
            }
        } else {
            BackgroundColorSpan mask = new BackgroundColorSpan(getResources().getColor(R.color.accent_material_dark));
            s.setSpan(mask, composingStart, composingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            masks.add(mask);
        }

        Log.d("EDITTEXT", "str : " + sb.toString());

        return sb.toString();
    }

    private int count(String text) {
        int len = text.length();
        int skip;
        int letter = 0;
        int chinese = 0;
        //		int count = 0;
        //		int sub = 0;
        for (int i = 0; i < len; i += skip) {
            int code = text.codePointAt(i);
            skip = Character.charCount(code);
            if (code == 10) {
                continue;
            }
            String s = text.substring(i, i + skip);
            if (isChinese(s)) {
                chinese++;
            } else {
                letter++;
            }

        }
        letter = letter % 2 == 0 ? letter / 2 : (letter / 2 + 1);
        int result = chinese + letter;
        return result;
    }

    // 完整的判断中文汉字和符号
    private boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    private class MyTextWatcher implements TextWatcher {

        private String text;
        private int editStart;
        private int editEnd;
        private Toast toastOutOf;

        private void showOutofCount(Context context, String text, int gravity, int xOffset, int yOffset, int duration) {
            if (toastOutOf != null) {
                toastOutOf.cancel();
                toastOutOf = null;
            }
            toastOutOf = Toast.makeText(context, text, duration);
            toastOutOf.setGravity(gravity, xOffset, yOffset);
            toastOutOf.show();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            text = filterComposingText(s);
            int count = count(text);//过滤了换行符的length
            int maxLength = 0;
            if (!isTextOnly()) {
                textLimit.setText((count > 10 ? 10 : count) + " / 10");
                maxLength = 10;
            }else {
                //纯字幕限制90个字
                maxLength = 90;
            }

            editStart = mEditView.getSelectionStart();
            editEnd = mEditView.getSelectionEnd();
            // 限定EditText只能输入10个数字
            if (count > maxLength && editStart > 0) {
                // 默认光标在最前端，所以当输入第11个数字的时候，删掉（光标位置从11-1到11）的数字，这样就无法输入超过10个以后的数字
                showOutofCount(getActivity(), getString(R.string.qupai_text_count_outof), Gravity.CENTER, 0, 0,
                    Toast.LENGTH_SHORT);

                s.delete(editStart - 1, editEnd);
                mEditView.setText(s);
                mEditView.setSelection(s.length());
            }

        }

        public String getText() {
            return text;
        }
    }

    private boolean isTextOnly() {
        return mEditInfo.isTextOnly;
    }

    private MyTextWatcher textWatch = new MyTextWatcher();

    @SuppressLint("ClickableViewAccessibility")
    private void setOnClick() {
        mEditView.addTextChangedListener(textWatch);
        mEditView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mTabLayout.getSelectedTabPosition() != 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
                    TabLayout.Tab tabAt = mTabLayout.getTabAt(0);
                    if (tabAt != null) {
                        tabAt.select();
                        return true;
                    }
                }
                return false;
            }
        });

        if (mBack != null) {

            mBack.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        mConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditInfo.mAnimation = mActionBase;
                mEditInfo.mAnimationSelect = mAnimationSelectPosition;
                mEditView.removeTextChangedListener(textWatch);
                Editable editable = mEditView.getText();
                String comment = filterComposingText(editable);

                callbackResult();
                dismiss();
            }
        });

    }

    private void callbackResult() {
        if (mOnStateChangeListener != null) {
            CharSequence text = mEditView.getText();
            mEditInfo.text = TextUtils.isEmpty(text) ? null : text.toString();
            if (TextUtils.isEmpty(text)) {
                mEditInfo.text = null;
            }else {
                mEditInfo.text = lineFeedText(text.toString());
            }
            mEditInfo.textColor = mEditView.getCurrentTextColor();
            mEditInfo.textStrokeColor = mEditView.getTextStrokeColor();
            mEditInfo.font = mEditView.getFontPath();
            mEditInfo.textWidth = mEditView.getWidth();
            mEditInfo.textHeight = mEditView.getHeight();
            mOnStateChangeListener.onTextEditCompleted(mEditInfo);
        }
    }

    /**
     * 处理十个字换行
     * @param text editText
     * @return 换行了的文字
     */
    public static String lineFeedText(String text) {
        StringBuilder buffer = new StringBuilder(text);
        char[] chars = text.toCharArray();
        int temp = 0;
        int feedCount = 0;
        int index = 0;
        while (index <= chars.length){
            if (temp>=11){
                buffer.insert(index + feedCount - 1,(char) 10);
                feedCount ++;
                temp = 1;
            }
            if (index == chars.length){
                break;
            }
            if (chars[index] != 10) {
                temp++;
            }else {
                temp = 0;
            }
            index++;
        }
        return buffer.toString();
    }

    public void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
            Context.INPUT_METHOD_SERVICE);
        if (inputManager.isActive()) {
            inputManager.hideSoftInputFromWindow(this.mEditView.getWindowToken(), 0);
        }
    }

    public void openKeyboard() {
        this.mEditView.postDelayed(this.mOpenKeyboardRunnable, 300);
    }

    private Runnable mOpenKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) {
                return;
            }
            try {
                requestFocusForKeyboard();
                InputMethodManager input = ((InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE));
                if (!input.showSoftInput(mEditView, 0)) {
                    openKeyboard();
                } else {
                    mEditView.setSelection(mEditView.getText().length());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void requestFocusForKeyboard() {
        this.mEditView.setFocusable(true);
        this.mEditView.setFocusableInTouchMode(true);
        this.mEditView.requestFocus();
        this.mEditView.requestFocusFromTouch();
    }

    @Override
    public void onActivityCreated(Bundle paramBundle) {
        super.onActivityCreated(paramBundle);
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        Log.d("Dialog", "Dialog oncreate的时间：" + System.currentTimeMillis());
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.TextDlgStyle);

    }

    @Override
    public void onResume() {
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        super.onResume();
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.mOnStateChangeListener = onStateChangeListener;
    }

    public interface OnStateChangeListener {

        void onTextEditCompleted(EditTextInfo result);

    }

    private class FontAdapter extends BaseAdapter {

        private List<FontForm> list = new ArrayList<>();

        public void setData(List<FontForm> data) {
            if (data == null || data.size() == 0) {
                return;
            }
            list.addAll(data);
            notifyDataSetChanged();
        }

        public int getLastCheckedPosition(String path) {
            return 0;
        }

        public List<FontForm> getData() {
            return list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public FontForm getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FontItemViewMediator localViewHolder;
            if (convertView == null) {
                localViewHolder = new FontItemViewMediator(parent);
                convertView = localViewHolder.getView();
                //Log.d("share_menu", "分享菜单的position：" + paramInt);
            } else {
                localViewHolder = (FontItemViewMediator) convertView.getTag();
            }
            final FontForm item = getItem(position);

            localViewHolder.setData(item);

            if (fontList.getCheckedItemPosition() == position) {
                localViewHolder.setSelected(true);
            } else {
                localViewHolder.setSelected(false);
            }

            return convertView;
        }

    }

    private class FontItemViewMediator {
        private ImageView image;
        private View select;
        private ImageView indiator;
        private TextView name;
        private View root;
        private FontForm fontInfo;

        public FontItemViewMediator(ViewGroup parent) {
            root = View.inflate(parent.getContext(), R.layout.aliyun_svideo_item_qupai_font_effect, null);
            select = root.findViewById(R.id.selected);
            image = (ImageView) root.findViewById(R.id.font_item_image);
            indiator = (ImageView) root.findViewById(R.id.indiator);
            name = (TextView) root.findViewById(R.id.item_name);
            root.setTag(this);
        }

        public void setData(FontForm font) {
            this.fontInfo = font;
            name.setText(font.getName());
            new ImageLoaderImpl().loadImage(getActivity(),font.getBanner()).into(image);
        }

        public void setSelected(boolean selected) {
            select.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        public View getView() {
            return root;
        }

    }

    public interface OnItemClickListener {
        void onItemClick(int animPosition);
    }

    public static class FontDateBean implements WheelView.DataModel {
        private String name;

        public FontDateBean(String name) {
            this.name = name;
        }

        @Override
        public String getText() {
            return name;
        }
    }

}
