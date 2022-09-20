/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatTextView;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.svideo.editor.util.ChineseUtil;
import com.aliyun.svideo.editor.util.CompatUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AutoLayoutTextView extends AppCompatTextView {
    private static final int NO_LINE_LIMIT = -1;

    private int mCurrentColor = Color.WHITE;
    private Integer mCurrentStrokeColor;
    protected float mStrokeWidth;
    protected Paint.Join mStrokeJoin;
    protected float mStrokeMiter;
    private boolean mFrozen = false;
    boolean mScaleByDrawable;


//    private int videoWidth;
//    private int videoHeight;

    private RectF mTextRect = new RectF();

    private RectF mAvailableSpaceRect;

    private TextPaint mPaint;

    private float mTextAngle;

    private float mMaxTextSize;

    private float mSpacingMult = 1.0f;

    private float mSpacingAdd = 0.0f;

    private float mMinTextSize = 0;

    private int mLastWidth; //上次保存的动图宽度
    private int mLastHeight; //上次保存的动图高度

    private int mWidth; //动图文字配置宽度
    private int mHeight;//动图文字配置高度

    private int mTop; //动图文字配置宽度
    private int mLeft; //动图文字配置顶部偏移
    private int mRight; //动图文字配置左边偏移
    private int mBottom; //动图文字配置底部偏移

    private String mFontPath;

    private boolean isEditCompleted = false;
    private boolean isMirror;
    private boolean isTextOnly;

    private StaticLayout mLayout;

    private int mWidthLimit;
    private final SizeTester mSizeTester = new SizeTester() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public int onTestSize(int suggestedSize, RectF availableSPace, String text) {
            mPaint.setTextSize(suggestedSize);
//            boolean singleline = mMaxLines == 1;
//            if (singleline) {
//                mTextRect.bottom = mPaint.getFontSpacing();
//                mTextRect.right = mPaint.measureText(text);
//            } else {
            mLayout = new StaticLayout(text, mPaint,
                                       mWidthLimit, Layout.Alignment.ALIGN_NORMAL, mSpacingMult,
                                       mSpacingAdd, true);
            // return early if we have aliyun_svideo_more lines
            if (mMaxLines != NO_LINE_LIMIT
                    && mLayout.getLineCount() > mMaxLines) {
                return 1;
            }
            int tl = calculateMaxLinesByText(text);
            if (mLayout.getLineCount() > tl) {
                return 1;
            }
            mTextRect.bottom = mLayout.getHeight();
            mTextRect.right = getLayoutMaxWidth();
//            }

            mTextRect.offsetTo(0, 0);
            Log.d("BINARYTEXT", "suggest size : " + suggestedSize +
                  " width : " + mTextRect.right +
                  " height : " + mTextRect.bottom +
                  " match : " + availableSPace.contains(mTextRect));
            if (availableSPace.contains(mTextRect)) {
                // may be too small, don't worry we will find the best match
                return -1;
            } else {
                // too big
                return 1;
            }
        }
    };

    private ImageView mImageView;

    public void setVideoSize(int width, int height) {
//        videoWidth = width;
//        videoHeight = height;
    }

    public void setImageView(ImageView imageView) {
        mImageView = imageView;
    }

    public Bitmap layoutToBitmap() {
        int textWidth = mWidth;
        int textHeight = mHeight;
        Layout layout = getLayout();
        if (layout == null) {
            measureSelf(mWidth, mHeight);
            int w = mWidth + getPaddingLeft() + getPaddingRight();
            int h = mHeight + getPaddingTop() + getPaddingBottom();
            super.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                          MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
            layout = getLayout();
        }

        if (layout == null) {
            layout = this.mLayout;
            if (layout == null) {
                return null;
            }
        }
        int dw = getWidth();
        int dh = getHeight();

        int rw = layout.getWidth();
        int rh = layout.getHeight();

        boolean firstInit = false;
        if (dw == 0 || dh == 0) {
            dw = mWidth;
            dh = mHeight;
            firstInit = true;
        }

        int w, h;
        if (isTextOnly) {
            w = dw;
            h = dh;
        } else {
            if (firstInit) {
                w = dw;
                h = dh;
            } else {
                w = dw - getPaddingLeft() - getPaddingRight();
                h = dh - getPaddingTop() - getPaddingBottom();
            }

        }

        if (w == 0 || h == 0) {
            return null;
        }
        Bitmap textBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(textBmp);
        canvas.translate((w - rw) / 2, (h - rh) / 2);

        TextPaint paint = layout.getPaint();
        if (mCurrentStrokeColor != null && mCurrentStrokeColor != 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(mStrokeJoin);
            paint.setStrokeMiter(mStrokeMiter);
            paint.setStrokeWidth(generateStrokeWidth());
            paint.setColor(mCurrentStrokeColor);
            layout.draw(canvas);
        }

        paint.setColor(mCurrentColor);
        paint.setStyle(Paint.Style.FILL);
        layout.draw(canvas);
        if (mImageView != null) {
            mImageView.setImageBitmap(textBmp);
        }

        setTextWidth(textWidth);
        setTextHeight(textHeight);
        return textBmp;
    }

    private boolean generateFileFromBitmap(Bitmap bmp, String outputPath, String srcMimeType) throws IOException {
        File outputFile = new File(outputPath);
        if (!outputFile.exists()) {
            File dir = outputFile.getParentFile();
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
            outputFile.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        srcMimeType = TextUtils.isEmpty(srcMimeType) ? "jpeg" : srcMimeType;
        if (outputPath.endsWith("jpg") || outputPath.endsWith("jpeg")
                || srcMimeType.endsWith("jpg") || srcMimeType.endsWith("jpeg")) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } else if (outputPath.endsWith("png") || srcMimeType.endsWith("png")) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } else if (outputPath.endsWith("webp") || srcMimeType.endsWith("webp")) {
            bmp.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);
        } else {
            Log.e(AliyunTag.TAG, "not supported image format for '" + outputPath + "'");
            outputStream.flush();
            outputStream.close();
            if (outputFile.exists()) {
                outputFile.delete();
            }
            return false;
        }
        outputStream.flush();
        outputStream.close();
        return true;
    }

    private int mMaxLines;

    private boolean mInitialized;

    public AutoLayoutTextView(Context context) {
        super(context);
        initialize();
    }

    public AutoLayoutTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AutoLayoutTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private int getLayoutMaxWidth() {
        int maxWidth = -1;
        for (int i = 0; i < mLayout.getLineCount(); i++) {
            if (maxWidth < mLayout.getLineWidth(i)) {
                maxWidth = (int) mLayout.getLineWidth(i);
            }
        }
        return maxWidth;
    }

    private static int binarySearch(int start, int end, String text, SizeTester sizeTester,
                                    RectF availableSpace) {
        int lastBest = start;
        int lo = start;
        int hi = end - 1;
        int mid = 0;
        Log.d("BINARYTEXT", "start : " + start +
              " end : " + end +
              " width : " + availableSpace.right +
              " height : " + availableSpace.bottom);
        while (lo <= hi) {
            mid = (lo + hi) >>> 1;
            int midValCmp = sizeTester.onTestSize(mid, availableSpace, text);
            if (midValCmp < 0) {
                lastBest = lo;
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
                lastBest = hi;
            } else {
                return mid;
            }
        }

        Log.d("BINARYTEXT", "last best : " + lastBest);
        // make sure to return last best
        // this is what should always be returned
        return lastBest;

    }

    private int mMaxWidthWhenOutof;
    private int mMaxHeightWhenOutof;

    private void initialize() {
        mStrokeJoin = Paint.Join.ROUND;
        mStrokeMiter = 10f;
        mMaxTextSize = TypedValue.applyDimension(
                           TypedValue.COMPLEX_UNIT_SP, 180,
                           getResources().getDisplayMetrics());
        mMaxTextSize = getResources().getDisplayMetrics().widthPixels;
        mAvailableSpaceRect = new RectF();
        if (mMaxLines == 0) {
            // no value was assigned during construction
            mMaxLines = NO_LINE_LIMIT;
        }
        float[] addAndMult = new float[2];
        CompatUtil.generateSpacingmultAndSpacingadd(addAndMult, this);
        mSpacingMult = addAndMult[0];
        mSpacingAdd = addAndMult[1];
        mInitialized = true;

        post(new Runnable() {
            @Override
            public void run() {
                int pw = ((ViewGroup) getParent()).getWidth();
                int ph = ((ViewGroup) getParent()).getHeight();
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                              30, getResources().getDisplayMetrics());
                mMaxWidthWhenOutof = pw - padding;
                mMaxHeightWhenOutof = ph - padding * 3;

                setMaxWidth(mMaxWidthWhenOutof);
                setMaxHeight(mMaxHeightWhenOutof);
            }
        });
    }

    public float getMaxTextSize() {
        return mMaxTextSize;
    }

    public void setEditCompleted(boolean isEditCompleted) {
        this.isEditCompleted = isEditCompleted;
    }

    public void setTextWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public void setTextHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public void setTextOnly(boolean isTextOnly) {
        this.isTextOnly = isTextOnly;
    }

    public void setTextTop(int mTop) {
        this.mTop = mTop;
    }

    public void setTextBottom(int mBottom) {
        this.mBottom = mBottom;
    }

    public void setTextLeft(int mLeft) {
        this.mLeft = mLeft;
    }

    public void setTextRight(int mRight) {
        this.mRight = mRight;
    }

    public void setFontPath(String fontPath) {
        mFontPath = fontPath;
        if (TextUtils.isEmpty(fontPath)) {
            setTypeface(Typeface.DEFAULT);
        } else {
            if (new File(mFontPath).exists()) {
                setTypeface(Typeface.createFromFile(fontPath));
            }
        }
    }

    public String getFontPath() {
        return mFontPath;
    }

    public void restore(int width, int height) {
        mLastHeight = height;
        mLastWidth = width;
    }

    public void setCurrentColor(int mCurrentColor) {
        this.mCurrentColor = mCurrentColor;
        setTextColor(mCurrentColor);
    }

    public void setMirror(boolean isMirror) {
        this.isMirror = isMirror;
        float angle = (float) Math.toDegrees(mTextAngle);
        setRotation(isMirror ? -angle : angle);
        requestLayout();
    }

    public void setmTextAngle(float mTextAngle) {
        this.mTextAngle = mTextAngle;
        setRotation((float) Math.toDegrees(mTextAngle));
    }

    public float getTextRotation() {
        return mTextAngle;
    }

    public void setTextStrokeColor(int currentStrokeColor) {
        this.mCurrentStrokeColor = currentStrokeColor;
        invalidate();
    }

    public int getTextColor() {
        return mCurrentColor;
    }

    public int getTextStrokeColor() {
        return mCurrentStrokeColor;
    }

    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    public void setText(final CharSequence text, BufferType type) {
        super.setText(filtrateText(text), BufferType.EDITABLE);
    }

    private int calculateMaxLinesByText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return 1;
        }
        return text.toString().split("\n").length;

    }

    private CharSequence filtrateText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        if (!isTextOnly) {
            int[] result = count(text.toString());
            if (result[0] > 10) {
                return text.subSequence(0, result[1]);
            }
        }
        return text;
    }

    private int[] count(String text) {
        int len = text.length();
        int skip;
        int letter = 0;
        int chinese = 0;
        int count = 0;
        int limit = 0;
        for (int i = 0; i < len; i += skip) {
            int code = text.codePointAt(i);
            skip = Character.charCount(code);
            if (code == 10) {
                continue;
            }
            String s = text.substring(i, i + skip);
            if (ChineseUtil.isChinese(s)) {
                chinese++;
            } else {
                letter++;
            }

            count = (letter % 2 == 0 ? letter / 2 : (letter / 2 + 1)) + chinese;
            if (count == 10) {
                limit = i + 1;
            }
        }
        int[] result = new int[2];
        result[0] = count;
        result[1] = limit;
        return result;
    }

    public float getmStrokeWidth() {
        return mStrokeWidth;
    }

    @Override
    public int getMaxLines() {
        return mMaxLines;
    }

    @Override
    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        mMaxLines = maxlines;
        reAdjust();
    }

    @Override
    public void setSingleLine() {
        super.setSingleLine();
        mMaxLines = 1;
        reAdjust();
    }

    @Override
    public void setSingleLine(boolean singleLine) {
        super.setSingleLine(singleLine);
        if (singleLine) {
            mMaxLines = 1;
        } else {
            mMaxLines = NO_LINE_LIMIT;
        }
        reAdjust();
    }

    @Override
    public void setLines(int lines) {
        super.setLines(lines);
        mMaxLines = lines;
        reAdjust();
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mSpacingMult = mult;
        mSpacingAdd = add;
    }

    /**
     * Set the lower text size limit and invalidate the view
     *
     * @param minTextSize
     */
    public void setMinTextSize(float minTextSize) {
        mMinTextSize = minTextSize;
        reAdjust();
    }

    public void reAdjust() {
        adjustTextSize();
    }

    private void adjustTextSize() {
        if (!mInitialized) {
            return;
        }
        if (TextUtils.isEmpty(getText())) {
            return;
        }

        int startSize = (int) mMinTextSize;
        float maxTextSize = 0;
        if (isEditCompleted) {
            maxTextSize = mMaxTextSize;
        } else {
            float base = getTextSize();
            float minLimit = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10,
                             getResources().getDisplayMetrics());
            maxTextSize = Math.max(base, minLimit);
        }

        mWidthLimit = (int) mAvailableSpaceRect.right;
        mPaint = new TextPaint(getPaint());
        super.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            efficientTextSizeSearch(startSize, (int) maxTextSize,
                                    mSizeTester, mAvailableSpaceRect));
    }

    private int efficientTextSizeSearch(int start, int end,
                                        SizeTester sizeTester, RectF availableSpace) {
        String text = getText().toString();
        return binarySearch(start, end, text,
                            sizeTester, availableSpace);
    }

    protected boolean isNeedSelfMeasure() {
        return !isEditCompleted;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w, h;
        if (isNeedSelfMeasure()) {
            super.onMeasure(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            w = getMeasuredWidth();
            h = getMeasuredHeight();
            setMeasuredDimension(w + 30, h + 30);
            if (w != mMaxWidthWhenOutof || h != mMaxHeightWhenOutof) {
                return;
            }

        } else {
            w = MeasureSpec.getSize(widthMeasureSpec);
            h = MeasureSpec.getSize(heightMeasureSpec);
        }

        if (mLastHeight == 0 || mLastWidth == 0) {
            mLastHeight = h;
            mLastWidth = w;
        }
        setMeasuredDimension(w, h);
        measureSelf(w, h);
    }

    protected void measureSelf(int width, int height) {
        //获取当前文字的宽度
//        CharSequence text = getText() == null?"":getText();
        mPaint = new TextPaint(getPaint());
        StaticLayout layout = new StaticLayout(getText(), mPaint,
                                               mWidthLimit, Layout.Alignment.ALIGN_NORMAL, mSpacingMult,
                                               mSpacingAdd, true);
        //获取padding
        int neededWidth = (int)layout.getLineWidth(0) + getPaddingLeft() + getPaddingRight();
        if (width < neededWidth) {
            getLayoutParams().width = neededWidth;
        }
        //相加看看是否超过当前view的宽度，如果超过了，则将当前view的宽度更新成这个值
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mScaleByDrawable) {
            int alpha = getPaint().getAlpha();
            getPaint().setAlpha(0);
            super.onDraw(canvas);
            getPaint().setAlpha(alpha);
            return;
        }
        freeze();
        int restoreColor = this.getCurrentTextColor();
        this.setShadowLayer(0, 0, 0, 0);

        if (mCurrentStrokeColor != null && mCurrentStrokeColor != 0) {
            TextPaint paint = this.getPaint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(mStrokeJoin);
            paint.setStrokeMiter(mStrokeMiter);
            this.setTextColor(mCurrentStrokeColor);
            mStrokeWidth = generateStrokeWidth();
            paint.setStrokeWidth(mStrokeWidth);
            super.onDraw(canvas);
            paint.setStyle(Paint.Style.FILL);
        }
        this.setTextColor(restoreColor);
//        int textLength = getText().length();
//        for(int i = 0; i < textLength; i++){
//            Layout layout = getLayout();
//            int line = layout.getLineForOffset(i);
//
//            int top = layout.getLineTop(line);
//            int left = (int)getLayout().getPrimaryHorizontal(i);
//            int width = (int)getPaint().measureText(getText().toString(),i,i+1);
//            int height = layout.getLineDescent(line) - layout.getLineAscent(line);
//            canvas.drawLine(left,top,left,top+height,getPaint());
//            canvas.drawLine(left+width,top,left+width,top+height,getPaint());
//            canvas.drawLine(left,top,left+width,top,getPaint());
//            canvas.drawLine(left,top+height,left+width,top+height,getPaint());
//        }

        unfreeze();

        super.onDraw(canvas);
    }

    private float generateStrokeWidth() {
        float value = getTextSize();
        int size = px2sp(getContext(), value);
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        if (size <= 27) {
            return strokeWidth;
        } else {
            size -= 27;
            float width = strokeWidth * ((float) size / 15f + 1);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                if (value > 256) {
                    width /= 5;
                }
            }
            return width;
        }
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    // Keep these things locked while onDraw in processing
    public void freeze() {
        mFrozen = true;
    }

    public void unfreeze() {
        mFrozen = false;
    }


    @Override
    public void requestLayout() {
        if (!mFrozen) {
            super.requestLayout();
        }
    }

    @Override
    public void postInvalidate() {
        if (!mFrozen) {
            super.postInvalidate();
        }
    }

    @Override
    public void postInvalidate(int left, int top, int right, int bottom) {
        if (!mFrozen) {
            super.postInvalidate(left, top, right, bottom);
        }
    }

    @Override
    public void invalidate() {
        if (!mFrozen) {
            super.invalidate();
        }
    }

    @Override
    public void invalidate(Rect rect) {
        if (!mFrozen) {
            super.invalidate(rect);
        }
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        if (!mFrozen) {
            super.invalidate(l, t, r, b);
        }
    }

    public int getTextWidth() {
        Layout layout = getLayout();
        if (layout == null) {
            measureSelf(mWidth, mHeight);
            int w = mWidth + getPaddingLeft() + getPaddingRight();
            int h = mHeight + getPaddingTop() + getPaddingBottom();
            super.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                          MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
            layout = getLayout();
        }

        if (layout == null) {
            layout = this.mLayout;
            if (layout == null) {
                return 0;
            }
        }
        return layout.getWidth();
    }

    public int getTextHeight() {
        Layout layout = getLayout();
        if (layout == null) {
            measureSelf(mWidth, mHeight);
            int w = mWidth + getPaddingLeft() + getPaddingRight();
            int h = mHeight + getPaddingTop() + getPaddingBottom();
            super.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                          MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
            layout = getLayout();
        }

        if (layout == null) {
            layout = this.mLayout;
            if (layout == null) {
                return 0;
            }
        }
        return layout.getHeight();
    }

    private interface SizeTester {
        /**
         * @param suggestedSize  Size of text to be tested
         * @param availableSpace available space in which text must fit
         * @return an integer < 0 if after applying {@code suggestedSize} to
         * text, it takes less space than {@code availableSpace}, > 0
         * otherwise
         */
        public int onTestSize(int suggestedSize, RectF availableSpace, String text);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int)}.
     */
    public void setSelection(int index) {
        Selection.setSelection((Spannable) getText(), index);
    }

}
