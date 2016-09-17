package com.expensemanager.app.overview;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Zhaolong Zhong on 9/17/2016.
 */
public class CustomPageIndicator implements ViewPager.OnPageChangeListener {
    private static final String TAG = CustomPageIndicator.class.getSimpleName();

    private Context context;
    private LinearLayout linearLayout;
    private int drawable;
    private int spacing;
    private int size;
    private ViewPager viewPager;
    private int pageCount;
    private int initialPage = 0;
    private int defaultSizeInDp = 12;
    private int defaultSpacingInDp = 12;

    public CustomPageIndicator(Context context, LinearLayout containerView, ViewPager viewPager, int drawableRes) {
        this.context = context;
        this.linearLayout = containerView;
        this.viewPager = viewPager;
        this.drawable = drawableRes;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setInitialPage(int page) {
        initialPage = page;
    }

    public void setDrawable(@DrawableRes int drawable) {
        this.drawable = drawable;
    }

    public void setSpacingRes(@DimenRes int spacingRes) {
        spacing = spacingRes;
    }

    public void setSize(@DimenRes int dimenRes) {
        size = dimenRes;
    }

    public void show() {
        initIndicators();
        setIndicatorAsSelected(initialPage);
    }

    private void initIndicators() {
        if (linearLayout == null || pageCount <= 0) {
            return;
        }

        viewPager.addOnPageChangeListener(this);
        Resources res = context.getResources();
        linearLayout.removeAllViews();
        for (int i = 0; i < pageCount; i++) {
            View view = new View(context);

            int dimen = size != 0 ? res.getDimensionPixelSize(size) : ((int) res.getDisplayMetrics().density * defaultSizeInDp);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dimen, dimen);
            int margin = spacing != 0 ? res.getDimensionPixelSize(spacing) : ((int) res.getDisplayMetrics().density * defaultSpacingInDp);
            layoutParams.setMargins(i == 0 ? 0 : margin, 0, 0, 0);

            view.setLayoutParams(layoutParams);
            view.setBackgroundResource(drawable);
            view.setSelected(i == 0);
            linearLayout.addView(view);
        }
    }

    private void setIndicatorAsSelected(int index) {
        if (linearLayout == null) {
            return;
        }
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            view.setSelected(i == index);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int index = position % pageCount;
        setIndicatorAsSelected(index);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void cleanup() {
        viewPager.clearOnPageChangeListeners();
    }
}
