/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.demo.util.ChineseUtil;
import com.aliyun.demo.util.CompatUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AutoResizingTextView extends TextView {
    private static final int NO_LINE_LIMIT = -1;

    private int currentColor;
    private Integer currentStrokeColor;
    protected float strokeWidth;
    protected Paint.Join strokeJoin;
    protected float strokeMiter;
    private boolean frozen = false;
    boolean scaleByDrawable;

//    private int videoWidth;
//    private int videoHeight;

    private RectF mTextRect = new RectF();

    private RectF mAvailableSpaceRect;

    private TextPaint mPaint;

    private float textAngle;

    private float mMaxTextSize;

    private float mSpacingMult = 1.0f;

    private float mSpacingAdd = 0.0f;

    private float mMinTextSize = 0;

    private int lastWidth; //上次保存的动图宽度
    private int lastHeight; //上次保存的动图高度

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

    private float MAX_TEXTSIZE;
    private StaticLayout layout;

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
            layout = new StaticLayout(text, mPaint,
                    mWidthLimit, Layout.Alignment.ALIGN_NORMAL, mSpacingMult,
                    mSpacingAdd, true);
            // return early if we have aliyun_svideo_more lines
            if (mMaxLines != NO_LINE_LIMIT
                    && layout.getLineCount() > mMaxLines) {
                return 1;
            }
            int tl = calculateMaxLinesByText(text);
            if (layout.getLineCount() > tl) {
                return 1;
            }
            mTextRect.bottom = layout.getHeight();
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
//        int measuredWidth = getMeasuredWidth();
//        int measuredHeight = getMeasuredHeight();
//        float ratioWidth = measuredWidth * 1.0f / videoWidth;
//        float rationHeight = measuredHeight * 1.0f / videoHeight;
        //setTextWidth((int) (mWidth * Math.min(ratioWidth, rationHeight)));
        //setTextHeight((int) (mHeight * Math.min(ratioWidth, rationHeight)));
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
            layout = this.layout;
            if(layout == null){
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
//        int originW = w, originH = h;
//        w = AliYunMathUtils.convertΩonvertFun(w);
//        h = AliYunMathUtils.convertFun(h);
//        Log.d("Math", "after convert w:"+w+", h:"+h);

        Bitmap textBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(textBmp);
//        canvas.translate((w - originW) / 2, (h - originH) / 2);
//        canvas.translate((originW - rw) / 2, (originH - rh) / 2);
        canvas.translate((w - rw) / 2, (h - rh) / 2);

        TextPaint paint = layout.getPaint();
        if (currentStrokeColor != null && currentStrokeColor != 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(strokeJoin);
            paint.setStrokeMiter(strokeMiter);
            paint.setStrokeWidth(generateStrokeWidth());
            paint.setColor(currentStrokeColor);
            layout.draw(canvas);
        }

        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.FILL);
        layout.draw(canvas);
        if (mImageView != null) {
            mImageView.setImageBitmap(textBmp);
        }


//        try {
//            generateFileFromBitmap(textBmp, "/sdcard/test.png", "image/png");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
        srcMimeType = TextUtils.isEmpty(srcMimeType) ? "jpeg":srcMimeType;
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

    public AutoResizingTextView(Context context) {
        super(context);
        initialize();
    }

    public AutoResizingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AutoResizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private int getLayoutMaxWidth() {
        int maxWidth = -1;
        for (int i = 0; i < layout.getLineCount(); i++) {
            if (maxWidth < layout.getLineWidth(i)) {
                maxWidth = (int) layout.getLineWidth(i);
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
        strokeJoin = Paint.Join.ROUND;
        strokeMiter = 10f;
        MAX_TEXTSIZE = TypedValue.applyDimension(
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
        return MAX_TEXTSIZE;
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
        lastHeight = height;
        lastWidth = width;
    }

    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
        setTextColor(currentColor);
    }

    public void setMirror(boolean isMirror) {
        this.isMirror = isMirror;
        float angle = (float) Math.toDegrees(textAngle);
        setRotation(isMirror ? -angle : angle);
        requestLayout();
    }

    public void setTextAngle(float textAngle) {
        this.textAngle = textAngle;
        setRotation((float) Math.toDegrees(textAngle));
    }

    public float getTextRotation() {
        return textAngle;
    }

    public void setTextStrokeColor(int currentStrokeColor) {
        this.currentStrokeColor = currentStrokeColor;
        invalidate();
    }

    public int getTextColor() {
        return currentColor;
    }

    public int getTextStrokeColor() {
        return currentStrokeColor;
    }

    @Override
    public void setText(final CharSequence text, BufferType type) {
        super.setText(filtrateText(text), type);
//        calculateMaxLinesByText(getText());
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

    public float getStrokeWidth() {
        return strokeWidth;
    }

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
        return binarySearch(start, end, ChineseUtil.halfToFull(text),
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

        if (lastHeight == 0 || lastWidth == 0) {
            lastHeight = h;
            lastWidth = w;
        }
        setMeasuredDimension(w, h);
        measureSelf(w, h);
    }

    protected void measureSelf(int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }

        int w, h;
        int top = 0;
        int left = 0;
        int right = 0;
        int bottom = 0;
        float rotx, roty;

        if (lastHeight == 0 || lastWidth == 0) {
            rotx = 1;
            roty = 1;
        } else {
            rotx = (float) width / (float) lastWidth;
            roty = (float) height / (float) lastHeight;
        }

        if (mWidth == 0 || mHeight == 0) {
            mWidth = lastWidth;
            mHeight = lastHeight;
        }

        w = (int) (mWidth * rotx);
        h = (int) (mHeight * roty);

        left = (int) (mLeft * rotx);
        top = (int) (mTop * roty);
        right = (int) (mRight * rotx);
        bottom = (int) (mBottom * roty);

        if (isMirror) {
            int temp = left;
            left = right;
            right = temp;
        }

        if (w == 0 || h == 0) {
            w = width;
            h = height;
        }

        mAvailableSpaceRect.right = w;
        mAvailableSpaceRect.bottom = h;

        adjustTextSize();

//        if(!isTextOnly && layout != null && mMaxLines != 1){
//            int ww = getLayoutMaxWidth();
//            left += (w - ww) / 2;
//            right += (w - ww) / 2;
//
//        }
//        int disx = (width - left - right - w) / 2;
//        int disy = (height - top - bottom - h) / 2;

        setPadding(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (scaleByDrawable) {
            int alpha = getPaint().getAlpha();
            getPaint().setAlpha(0);
            super.onDraw(canvas);
            getPaint().setAlpha(alpha);
            return;
        }
        freeze();
        int restoreColor = this.getCurrentTextColor();
        this.setShadowLayer(0, 0, 0, 0);

        if (currentStrokeColor != null && currentStrokeColor != 0) {
            TextPaint paint = this.getPaint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(strokeJoin);
            paint.setStrokeMiter(strokeMiter);
            this.setTextColor(currentStrokeColor);
            strokeWidth = generateStrokeWidth();
            paint.setStrokeWidth(strokeWidth);
            super.onDraw(canvas);
            paint.setStyle(Paint.Style.FILL);
        }
        this.setTextColor(restoreColor);

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
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
    }


    @Override
    public void requestLayout() {
        if (!frozen) super.requestLayout();
    }

    @Override
    public void postInvalidate() {
        if (!frozen) super.postInvalidate();
    }

    @Override
    public void postInvalidate(int left, int top, int right, int bottom) {
        if (!frozen) super.postInvalidate(left, top, right, bottom);
    }

    @Override
    public void invalidate() {
        if (!frozen) super.invalidate();
    }

    @Override
    public void invalidate(Rect rect) {
        if (!frozen) super.invalidate(rect);
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        if (!frozen) super.invalidate(l, t, r, b);
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
}
