package com.expensemanager.app.report;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.service.font.Font;

import butterknife.BindView;
import butterknife.ButterKnife;

;

/**
 * Created by Zhaolong Zhong on 9/10/16.
 */

public class ReportMainFragment extends Fragment {
    private static final String TAG = ReportFragment.class.getSimpleName();

    @BindView(R.id.report_activity_tab_layout_id) TabLayout tabLayout;
    @BindView(R.id.report_activity_view_pager_id) ViewPager viewPager;

    public static Fragment newInstance() {
        return new ReportMainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.report_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.main_activity_toolbar_id);
        ViewCompat.setElevation(toolbar, 10);
        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ReportFragmentAdapter adapter = new ReportFragmentAdapter(getFragmentManager());

        adapter.addFragment(ReportFragment.newInstance(ReportFragment.WEEKLY), "Weekly");
        adapter.addFragment(ReportFragment.newInstance(ReportFragment.MONTHLY), "Monthly");
        adapter.addFragment(ReportFragment.newInstance(ReportFragment.YEARLY), "Yearly");
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont();

        viewPager.setCurrentItem(ReportFragment.MONTHLY);
    }

    private void changeTabsFont() {
        Typeface typeface = EApplication.getInstance().getTypeface(Font.REGULAR);
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();

        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(typeface, Typeface.NORMAL);
                }
            }
        }
    }
}
