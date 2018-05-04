/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.quview.control.TabGroup;
import com.aliyun.quview.control.ViewStack;
import com.aliyun.quview.control.TabbedViewStackBinding;
import com.aliyun.demo.widget.AutoResizingEditText;
import com.aliyun.struct.form.FontForm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TextDialog extends DialogFragment {

    private AutoResizingEditText mEditView;
    private TextView mSend;
    //	private View font_new;
    private TextView textLimit;
    private ViewPager mColorViewPager;
    private TabLayout mColorTabHost;
    private ColorViewPagerAdapter mColorPagerAdapter;


    GridView fontList;
    FontAdapter fontAdapter;
    private FrameLayout pageContainer;
    private TabGroup pageTabGroup;

    private EditTextInfo mEditInfo;

    private int mSelectedIndex;

    private OnStateChangeListener mOnStateChangeListener;

    public static TextDialog newInstance(EditTextInfo editInfo) {
        TextDialog dialog = new TextDialog();
        Bundle b = new Bundle();
        b.putSerializable("edit", editInfo);
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

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mEditView.removeTextChangedListener(textWatch);
        callbackResult();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog != null) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View contentView = View.inflate(
                getActivity(), R.layout.aliyun_svideo_qupai_row_text_bottom, null);

        mEditInfo = (EditTextInfo) getArguments().getSerializable("edit");
        if (mEditInfo == null) {
            dismiss();
            return contentView;
        }

//        isStroke = mEditInfo.textStrokeColor != 0;
        mEditView = (AutoResizingEditText) contentView.findViewById(R.id.qupai_overlay_content_text);
        mSend = (TextView) contentView.findViewById(R.id.send);

        mEditView.setTextOnly(mEditInfo.isTextOnly);
        mEditView.setText(mEditInfo.text);
        mEditView.setFontPath(mEditInfo.font);
        mEditView.setCurrentColor(mEditInfo.textColor);
        mEditView.setTextStrokeColor(mEditInfo.textStrokeColor);
        if (!mEditInfo.isTextOnly) {
            mEditView.setTextWidth(mEditInfo.textWidth);
            mEditView.setTextHeight(mEditInfo.textHeight);
        }

        mEditView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18,
                getResources().getDisplayMetrics()));

        pageContainer = (FrameLayout) contentView.findViewById(R.id.container);
        ViewStack pagerViewStack = new ViewStack(View.GONE);
        pagerViewStack.addView(new View(getActivity()));
        pagerViewStack.addView(contentView.findViewById(R.id.font_layout));
        pagerViewStack.addView(contentView.findViewById(R.id.color_container));

        pageTabGroup = new TabGroup();
        pageTabGroup.addView(contentView.findViewById(R.id.tab_text));
        pageTabGroup.addView(contentView.findViewById(R.id.tab_font));
        pageTabGroup.addView(contentView.findViewById(R.id.tab_color));
        TabbedViewStackBinding pagerStackBinding = new TabbedViewStackBinding() {
            @Override
            public void onCheckedChanged(TabGroup control, int checkedIndex) {
                if (checkedIndex == 0) {
                    pageContainer.setVisibility(View.GONE);
                    mEditView.setEnabled(true);
                    openKeyboard();
                } else {
                    pageContainer.setVisibility(View.VISIBLE);
                    closeKeyboard();
                    super.onCheckedChanged(control, checkedIndex);
                    if (checkedIndex == 1) {
                        if (fontList.getCheckedItemPosition() == -1) {
                            int position = ((FontAdapter) fontList.getAdapter())
                                    .getLastCheckedPosition(null);
                            fontList.setItemChecked(position, true);
                            fontList.smoothScrollToPosition(position);
                        }
                    } else {
                        mEditView.setEnabled(false);
                    }

                }

            }
        };
        pagerStackBinding.setViewStack(pagerViewStack);
        pageTabGroup.setOnCheckedChangeListener(pagerStackBinding);

        mColorTabHost = (TabLayout) contentView.findViewById(R.id.color_tab_host);
        mColorViewPager = (ViewPager) contentView.findViewById(R.id.color_viewpager);
        mColorPagerAdapter = new ColorViewPagerAdapter();


        ColorViewHolder colorHolder = new ColorViewHolder(getActivity().getApplicationContext(),
                getString(R.string.qupai_effect_text_color), false, mEditInfo.dTextColor);
        ColorViewHolder strokeHolder = new ColorViewHolder(getActivity().getApplicationContext(),
                getString(R.string.qupai_effect_text_stroke), true, mEditInfo.dTextStrokeColor);

        colorHolder.setItemClickListener(new ColorViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(ColorViewHolder.ColorItem item) {
//                mEditView.setTextStrokeColor(0);
                mEditView.setCurrentColor(item.color);
            }
        });
        strokeHolder.setItemClickListener(new ColorViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(ColorViewHolder.ColorItem item) {
                mEditView.setTextStrokeColor(item.strokeColor);
            }
        });
        mColorPagerAdapter.addViewHolder(colorHolder);
        mColorPagerAdapter.addViewHolder(strokeHolder);
        mColorViewPager.setAdapter(mColorPagerAdapter);
        mColorTabHost.setupWithViewPager(mColorViewPager);

        fontList = (GridView) contentView.findViewById(R.id.font_list);
        fontAdapter = new FontAdapter();

        fontAdapter.setData(getFontList());
        fontList.setAdapter(fontAdapter);
        fontList.setItemChecked(mSelectedIndex,true);
        fontList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FontForm font = (FontForm) parent.getItemAtPosition(position);
                String path = font.getUrl();
                File f = new File(path);
                boolean ifd = f.exists() && f.isDirectory();
                if(!ifd){
                    f = new File(path + "tmp");
                    ifd = f.exists() && f.isDirectory();
                }

                if(ifd){
                    String[] fds = f.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".ttf");
                        }
                    });
                    if(fds.length > 0){
                        mEditView.setFontPath(new File(f, fds[0]).getAbsolutePath());
                    }

                }

                fontList.setItemChecked(position, true);

            }
        });

        LayoutParams localLayoutParams = getDialog().getWindow()
                .getAttributes();
        localLayoutParams.gravity = Gravity.BOTTOM;
        localLayoutParams.width = LayoutParams.MATCH_PARENT;

//		font_new = contentview.findViewById(R.id.tab_effect_font_new);
        SharedPreferences sp = getActivity().getSharedPreferences("AppGlobalSetting", 0);
        boolean isFontHasNew = sp.getBoolean("font_category_new", false);
//		font_new.setVisibility(isFontHasNew ? View.VISIBLE : View.GONE);
        setOnClick();
        pageTabGroup.setCheckedIndex(0);
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
        contentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return contentView;
    }

    private List<FontForm> getFontList() {
        int index = 0;
        List<FileDownloaderModel> list = DownloaderManager.getInstance().getDbController().getResourceByType(1);
        List<FontForm> fonts = new ArrayList<>();
        for (FileDownloaderModel m : list) {
            FontForm font = new FontForm();
            font.setId(m.getId());
            font.setName(m.getCnname());
            font.setBanner(m.getBanner());
            font.setUrl(m.getPath());
            fonts.add(font);
            if((m.getPath() + "/font.ttf").equals(mEditInfo.font)){
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
        private Toast toast_outOf;

        private void showOutofCount(Context context, String text, int gravity, int xOffset, int yOffset, int duration) {
            if (toast_outOf != null) {
                toast_outOf.cancel();
                toast_outOf = null;
            }
            toast_outOf = Toast.makeText(context, text, duration);
            toast_outOf.setGravity(gravity, xOffset, yOffset);
            toast_outOf.show();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Log.d("EDITTEXT", "onTextChanged text : " + s + " startAnimation : " + startAnimation + " before : " + before + " count : " + count);

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            Log.d("EDITTEXT", "beforeTextChanged text : " + s + " startAnimation : " + start + " after : " + after + " count : " + count);

        }

        @Override
        public void afterTextChanged(Editable s) {

            text = filterComposingText(s);

            if (isTextOnly()) {
                return;
            }

            int count = count(text);

            if (!isTextOnly()) {
                textLimit.setText((count > 10 ? 10 : count) + " / 10");
//				Toast.makeText(getActivity(), (count > 10 ? 10 : count) + " / 10", Toast.LENGTH_SHORT).show();
            }

            editStart = mEditView.getSelectionStart();
            editEnd = mEditView.getSelectionEnd();

            // 限定EditText只能输入10个数字
            if (count > 10 && editStart > 0) {
                Log.d("TEXTDIALOG", "超过10个以后的数字");
                // 默认光标在最前端，所以当输入第11个数字的时候，删掉（光标位置从11-1到11）的数字，这样就无法输入超过10个以后的数字

                showOutofCount(getActivity(), getString(R.string.qupai_text_count_outof),
                        Gravity.CENTER, 0, 0, Toast.LENGTH_SHORT);

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

    private void setOnClick() {
        mEditView.addTextChangedListener(textWatch);

        mEditView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (pageTabGroup.getCheckedIndex() != 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
                    pageTabGroup.setCheckedIndex(0);
                    return true;
                }
                return false;
            }
        });

        mSend.setOnClickListener(new OnClickListener() {

            private void deleteWrap(Editable s) {
                boolean skip = false;
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c == '\n') {
                        if (!skip) {
                            skip = true;
                        } else {
                            s.delete(i, i + 1);
                        }
                    }
                }
            }

            @Override
            public void onClick(View v) {
                mEditView.removeTextChangedListener(textWatch);
                Editable editable = mEditView.getText();
                //deleteWrap(editable);
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
            mEditInfo.textColor = mEditView.getCurrentTextColor();
            mEditInfo.textStrokeColor = mEditView.getTextStrokeColor();
            mEditInfo.font = mEditView.getFontPath();
            mEditInfo.textWidth = mEditView.getWidth();
            mEditInfo.textHeight = mEditView.getHeight();
            mOnStateChangeListener.onTextEditCompleted(mEditInfo);
        }
    }

    public void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager.isActive()) {
            inputManager
                    .hideSoftInputFromWindow(this.mEditView.getWindowToken(), 0);
        }
    }

    public void openKeyboard() {
        this.mEditView.postDelayed(this.mOpenKeyboardRunnable, 300);
    }

    private Runnable mOpenKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            if(getActivity() == null){
                return ;
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
            getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
        }

        super.onResume();
    }

    public void setOnStateChangeListener(
            OnStateChangeListener onStateChangeListener) {
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

            Glide.with(getActivity()).load(font.getBanner()).into(image);
        }

        public void setSelected(boolean selected) {
            select.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        public View getView() {
            return root;
        }

    }

}
