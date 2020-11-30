/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.widget;

import android.content.Context;
import android.text.*;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class AutoResizingEditText extends AutoResizingTextView {

    public AutoResizingEditText(Context context) {
        this(context, null);
    }

    public AutoResizingEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoResizingEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, getPaint());
    }

    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int, int)}.
     */
    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int)}.
     */
    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }

    /**
     * Convenience for {@link Selection#selectAll}.
     */
    public void selectAll() {
        Selection.selectAll(getText());
    }

    /**
     * Convenience for {@link Selection#extendSelection}.
     */
    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
            throw new IllegalArgumentException("EditText cannot use the ellipsize mode "
                                               + "TextUtils.TruncateAt.MARQUEE");
        }
        super.setEllipsize(ellipsis);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return EditText.class.getName();
    }

}
