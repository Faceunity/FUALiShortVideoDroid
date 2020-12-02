/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.race.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.utils.DensityUtil;

import java.util.Locale;

public class PagerSlidingTabStrip extends HorizontalScrollView {

    public interface IconTabProvider {
        int getPageIconResId(int position);
    }

    // @formatter:off
    private static final int[] ATTRS = new int[] { android.R.attr.textSize,
            android.R.attr.textColor
                                                 };
    // @formatter:on

    private LinearLayout.LayoutParams mDefaultTabLayoutParams;
    private LinearLayout.LayoutParams mExpandedTabLayoutParams;

    private final PageListener mPageListener = new PageListener();
    public OnPageChangeListener mDelegatePageListener;

    private TabClickListener mTabClickListener;
    private LinearLayout mTabsContainer;
    private ViewPager mPager;

    private int mTabCount;
    /**
     * Only draw more than 1 tab
     */
    private static final int TAB_DIVIDER_LINT = 1;

    private int mCurrentPosition = 0;
    private float mCurrentPositionOffset = 0f;

    private Paint mRectPaint;
    private Paint mDividerPaint;

    private int mIndicatorColor;
    private int mUnderlineColor;
    private int mDividerColor;

    private boolean mShouldExpand = false;
    private boolean mTextAllCaps = true;

    private int mScrollOffset = 52;
    private int mIndicatorHeight = 4;
    private int mUnderlineHeight = 1;
    private int mDividerPadding = 16;
    private int mTabPadding = 2;
    private int mDividerWidth = 0;

    private int mTabTextSize = 36;
    private int mTabTextColor;
    private Typeface mTabTypeface = null;
    private int mTabTypefaceStyle = Typeface.NORMAL;

    private int mLastScrollX = 0;

    private int mTabBackgroundResId;

    private int mTabViewId;

    private Locale mLocale;
    private Context mContext;

    public PagerSlidingTabStrip(Context context) {
        this(context, null);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        mContext = context;
        mTabsContainer = new LinearLayout(context);
        mTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabsContainer.setLayoutParams(new LayoutParams(
                                          LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mTabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        mScrollOffset = (int) TypedValue.applyDimension(
                           TypedValue.COMPLEX_UNIT_DIP, mScrollOffset, dm);
        mIndicatorHeight = (int) TypedValue.applyDimension(
                              TypedValue.COMPLEX_UNIT_DIP, mIndicatorHeight, dm);
        mUnderlineHeight = (int) TypedValue.applyDimension(
                              TypedValue.COMPLEX_UNIT_DIP, mUnderlineHeight, dm);
        mDividerPadding = (int) TypedValue.applyDimension(
                             TypedValue.COMPLEX_UNIT_DIP, mDividerPadding, dm);
        mTabPadding = (int) TypedValue.applyDimension(
                         TypedValue.COMPLEX_UNIT_DIP, mTabPadding, dm);
        mDividerWidth = (int) TypedValue.applyDimension(
                           TypedValue.COMPLEX_UNIT_DIP, mDividerWidth, dm);
        mTabTextSize = (int) TypedValue.applyDimension(
                          TypedValue.COMPLEX_UNIT_SP, mTabTextSize, dm);

        // get system attrs (android:textSize and android:textColor)

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        mTabTextSize = a.getDimensionPixelSize(0, mTabTextSize);
        mTabTextColor = a.getColor(1, mTabTextColor);

        a.recycle();

        // get custom attrs

        a = context.obtainStyledAttributes(attrs,
                                           R.styleable.QuViewPagerSlidingTabStrip);

        mIndicatorColor = a.getColor(
                             R.styleable.QuViewPagerSlidingTabStrip_pstsIndicatorColor,
                mIndicatorColor);
        mUnderlineColor = a.getColor(
                             R.styleable.QuViewPagerSlidingTabStrip_pstsUnderlineColor,
                mUnderlineColor);
        mDividerColor = a
                       .getColor(R.styleable.QuViewPagerSlidingTabStrip_pstsDividerColor,
                               mDividerColor);
        mIndicatorHeight = a.getDimensionPixelSize(
                              R.styleable.QuViewPagerSlidingTabStrip_pstsIndicatorHeight,
                mIndicatorHeight);
        mUnderlineHeight = a.getDimensionPixelSize(
                              R.styleable.QuViewPagerSlidingTabStrip_pstsUnderlineHeight,
                mUnderlineHeight);
        mDividerPadding = a.getDimensionPixelSize(
                             R.styleable.QuViewPagerSlidingTabStrip_pstsDividerPadding,
                mDividerPadding);
        mTabPadding = a.getDimensionPixelSize(
                         R.styleable.QuViewPagerSlidingTabStrip_pstsTabPaddingLeftRight,
                mTabPadding);
        mTabBackgroundResId = a.getResourceId(
                                 R.styleable.QuViewPagerSlidingTabStrip_pstsTabBackground,
                mTabBackgroundResId);
        mShouldExpand = a
                       .getBoolean(R.styleable.QuViewPagerSlidingTabStrip_pstsShouldExpand,
                               mShouldExpand);
        mScrollOffset = a
                       .getDimensionPixelSize(
                           R.styleable.QuViewPagerSlidingTabStrip_pstsScrollOffset,
                               mScrollOffset);
        mTextAllCaps = a.getBoolean(
                          R.styleable.QuViewPagerSlidingTabStrip_pstsTextAllCaps, mTextAllCaps);

        a.recycle();

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Style.FILL);

        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStrokeWidth(mDividerWidth);

        mDefaultTabLayoutParams = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mExpandedTabLayoutParams = new LinearLayout.LayoutParams(0,
                LayoutParams.MATCH_PARENT, 1.0f);

        if (mLocale == null) {
            mLocale = getResources().getConfiguration().locale;
        }
    }



    public void setViewPager(ViewPager pager) {
        this.mPager = pager;

        if (pager.getAdapter() == null) {
            throw new IllegalStateException(
                "ViewPager does not have adapter instance.");
        }
        pager.setOnPageChangeListener(mPageListener);
        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mDelegatePageListener = listener;
    }

    public void setTabClickListener(TabClickListener tabClickListener) {
        this.mTabClickListener = tabClickListener;
    }

    public void notifyDataSetChanged() {

        mTabsContainer.removeAllViews();

        PagerAdapter adapter = mPager.getAdapter();

        mTabCount = adapter.getCount();

        for (int i = 0; i < mTabCount; i++) {
            CharSequence title = mPager.getAdapter().getPageTitle(i);
            if (mPager.getAdapter() instanceof IconTabProvider) {
                addTabWithIcon(i, title,
                               ((IconTabProvider) mPager.getAdapter())
                               .getPageIconResId(i));
            } else {
                addTextTab(i, title.toString());
            }

        }

        updateTabStyles(mCurrentPosition);

//        getViewTreeObserver().addOnGlobalLayoutListener(
//                new OnGlobalLayoutListener() {
//
//                    @Override
//                    public void onGlobalLayout() {
//                        ViewTreeObserverUtil.removeOnGlobalLayoutListener(
//                                getViewTreeObserver(), this);
//
//                        mCurrentPosition = mPager.getCurrentItem();
//                        scrollToChild(mCurrentPosition, 0);
//                    }
//                });

    }

    private void addTabWithIcon(int index, CharSequence text, int iconResId) {
        TabView tab = new TabView(mTabsContainer, index);
        tab.title.setText(text);
        tab.title.setGravity(Gravity.CENTER);
        if (iconResId != 0) {
            tab.title.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0,
                    0);
            tab.title.setCompoundDrawablePadding(DensityUtil.dip2px(mContext, 5));
        }
        addTab(index, tab.getView());
    }

    private void addTextTab(final int position, String title) {

        // TextView tab = new TextView(getContext());
        // tab.setText(title);
        // tab.setGravity(Gravity.CENTER);
        // tab.setSingleLine();
        TabView tab = new TabView(mTabsContainer, position);
        tab.title.setText(title);
        tab.title.setGravity(Gravity.CENTER);
        // tab.msgCount.setText(title);
        addTab(position, tab.getView());
    }

    private void addIconTab(final int position, int resId) {

        ImageButton tab = new ImageButton(getContext());
        tab.setImageResource(resId);
        addTab(position, tab);

    }

    private void   addTab(final int position, View tab) {
        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(position);
            }
        });

        tab.setPadding(mTabPadding, 0, mTabPadding, 0);
        mTabsContainer
        .addView(tab, position, mShouldExpand ? mExpandedTabLayoutParams
                 : mDefaultTabLayoutParams);
    }

    private void updateTabStyles(int pos) {

        for (int i = 0; i < mTabCount; i++) {

            View v = mTabsContainer.getChildAt(i);

            v.setBackgroundResource(mTabBackgroundResId);

            if (v instanceof TextView) {

                TextView tab = (TextView) v;
                tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
                tab.setTypeface(mTabTypeface, mTabTypefaceStyle);
                tab.setTextColor(mTabTextColor);

                // setAllCaps() is only available from API 14, so the upper case
                // is made manually if we are on a
                // pre-ICS-build
                if (mTextAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab.setAllCaps(true);
                    } else {
                        tab.setText(tab.getText().toString()
                                    .toUpperCase(mLocale));
                    }
                }
            } else {
                Object obj = v.getTag();
                if (obj != null && obj instanceof TabView) {
                    TabView tab = (TabView) obj;
                    // tab.title.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    // mTabTextSize);
                    // tab.title.setTypeface(mTabTypeface, mTabTypefaceStyle);
                    if (i == pos) {
                        tab.title.setSelected(true);
                    } else {
                        tab.title.setSelected(false);
                    }

                    if (mTextAllCaps) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            tab.title.setAllCaps(true);
                        } else {
                            tab.title.setText(tab.title.getText().toString()
                                              .toUpperCase(mLocale));
                        }
                    }
                }
            }
        }

    }

    private void scrollToChild(int position, int offset) {

        if (mTabCount == 0) {
            return;
        }

        int newScrollX = mTabsContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= mScrollOffset;
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || mTabCount <= TAB_DIVIDER_LINT) {
            return;
        }

        final int height = getHeight();

        // default: line below current tab
        View currentTab = mTabsContainer.getChildAt(mCurrentPosition);
        final int tabPadding = currentTab.getWidth() / 3;
        float lineLeft = currentTab.getLeft() + tabPadding;
        float lineRight = currentTab.getRight()  - tabPadding;

        // if there is an offset, start interpolating left and right coordinates
        // between current and next tab
        float padding = 0;
        if (mCurrentPositionOffset > 0f && mCurrentPosition < mTabCount - 1) {

            View nextTab = mTabsContainer.getChildAt(mCurrentPosition + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();

            lineLeft = (mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset) * lineLeft + tabPadding);
            lineRight = (mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset) * lineRight - tabPadding);
        }

        // draw underline

        mRectPaint.setColor(mUnderlineColor);
        //canvas.drawRect(0, height - mUnderlineHeight, mTabsContainer.getWidth(),
        //        height, mRectPaint);

        mRectPaint.setColor(mIndicatorColor);
        canvas.drawRoundRect(new RectF(lineLeft + padding, height - mIndicatorHeight, lineRight - padding, height), 5f, 5f, mRectPaint);
//        canvas.drawRect(lineLeft + padding, height - mIndicatorHeight, lineRight - padding, height,
//                mRectPaint);

        // draw divider

        mDividerPaint.setColor(mDividerColor);

        for (int i = 0; i < mTabCount - 1; i++) {
            View tab = mTabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), mDividerPadding, tab.getRight(),
                            height - mDividerPadding, mDividerPaint);
        }

    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {

            mCurrentPosition = position;
            mCurrentPositionOffset = positionOffset;

            scrollToChild(position, (int) (positionOffset * mTabsContainer
                                           .getChildAt(position).getWidth()));

            invalidate();

            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageScrolled(position, positionOffset,
                                                    positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(mPager.getCurrentItem(), 0);
            }

            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageSelected(position);
            }

            updateTabStyles(position);
        }

    }

    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.mIndicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public int getIndicatorColor() {
        return this.mIndicatorColor;
    }

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.mIndicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public int getIndicatorHeight() {
        return mIndicatorHeight;
    }

    public void setUnderlineColor(int underlineColor) {
        this.mUnderlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId) {
        this.mUnderlineColor = getResources().getColor(resId);
        invalidate();
    }

    public int getUnderlineColor() {
        return mUnderlineColor;
    }

    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId) {
        this.mDividerColor = getResources().getColor(resId);
        invalidate();
    }

    public int getDividerColor() {
        return mDividerColor;
    }

    public void setUnderlineHeight(int underlineHeightPx) {
        this.mUnderlineHeight = underlineHeightPx;
        invalidate();
    }

    public int getUnderlineHeight() {
        return mUnderlineHeight;
    }

    public void setDividerPadding(int dividerPaddingPx) {
        this.mDividerPadding = dividerPaddingPx;
        invalidate();
    }

    public int getDividerPadding() {
        return mDividerPadding;
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.mScrollOffset = scrollOffsetPx;
        invalidate();
    }

    public int getScrollOffset() {
        return mScrollOffset;
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.mShouldExpand = shouldExpand;
        requestLayout();
    }

    public boolean getShouldExpand() {
        return mShouldExpand;
    }

    public boolean isTextAllCaps() {
        return mTextAllCaps;
    }

    public void setAllCaps(boolean textAllCaps) {
        this.mTextAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx) {
        this.mTabTextSize = textSizePx;
        updateTabStyles(mCurrentPosition);
    }

    public int getTextSize() {
        return mTabTextSize;
    }

    public void setTextColor(int textColor) {
        this.mTabTextColor = textColor;
        updateTabStyles(mCurrentPosition);
    }

    public void setTextColorResource(int resId) {
        this.mTabTextColor = getResources().getColor(resId);
        updateTabStyles(mCurrentPosition);
    }

    public int getTextColor() {
        return mTabTextColor;
    }

    public void setTypeface(Typeface typeface, int style) {
        this.mTabTypeface = typeface;
        this.mTabTypefaceStyle = style;
        updateTabStyles(mCurrentPosition);
    }

    public void setTabBackground(int resId) {
        this.mTabBackgroundResId = resId;
    }

    public int getTabBackground() {
        return mTabBackgroundResId;
    }

    public void setTabPaddingLeftRight(int paddingPx) {
        this.mTabPadding = paddingPx;
        updateTabStyles(mCurrentPosition);
    }

    public int getTabPaddingLeftRight() {
        return mTabPadding;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = mCurrentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }










    public interface TabClickListener {

        void onTabClickListener(int position);

        void onTabDoubleClickListener(int position);

    }

    public void setTabViewId(int id) {
        this.mTabViewId = id;
    }

    public class TabView {

        private final TextView title;

        private final View _Root;


        public TabView(ViewGroup container, final int id) {
            _Root = View.inflate(container.getContext(),
                    mTabViewId, null);
            _Root.setTag(this);

            title = (TextView) _Root.findViewById(R.id.aliyun_tv_title);

            final GestureDetectorCompat gesture = new GestureDetectorCompat(
                _Root.getContext(),
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // if(mTabClickListener != null){
                    // mTabClickListener.onTabDoubleClickListener(id);
                    // }
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (mTabClickListener != null) {
                        Log.d("onSingleTapUp", "id" + id);
                        mTabClickListener.onTabClickListener(id);
                    }
                    return super.onSingleTapUp(e);
                }

            });
            _Root.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gesture.onTouchEvent(event);
                    return false;
                }
            });
        }


        public View getView() {
            return _Root;
        }

    }

}
