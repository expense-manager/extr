package com.expensemanager.app.report.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.expensemanager.app.report.bar_char.ReportBarChartFragment;
import com.expensemanager.app.report.pie_char.ReportPieChartFragment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zhaolong Zhong on 9/6/16.
 */

public class ReportPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = ReportPagerAdapter.class.getSimpleName();

    private String[] tabTitles = {"Categories", "Expenses"};
    private Map<Integer, Fragment> map = new HashMap<>();
    private Date[] startEnd;
    private int requestCode;

    // Adapter gets the manager insert or remove fragment from activity
    public ReportPagerAdapter(FragmentManager fragmentManager, Date[] startEnd, int requestCode) {
        super(fragmentManager);
        this.startEnd = startEnd;
        this.requestCode = requestCode;
    }

    // Decide fragment by tab position
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment =  ReportPieChartFragment.newInstance(startEnd, requestCode);
        } else if (position == 1) {
            fragment = ReportBarChartFragment.newInstance(startEnd, requestCode);
        }
        map.put(position, fragment);
        return fragment;
    }

    // Decide tab name by tab position
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    // get total count of fragments
    @Override
    public int getCount() {
        return tabTitles.length;
    }

    // Get fragment by position
    public Fragment getFragmentByPosition(int position) {
        return map.get(position);
    }

    // Update fragments
    public void updateFragments() {
        for (Fragment fragment : map.values()) {
            if (fragment instanceof ReportPieChartFragment) {
                ((ReportPieChartFragment) fragment).invalidateViews();
            } else if (fragment instanceof  ReportBarChartFragment) {
                ((ReportBarChartFragment) fragment).invalidateViews();
            }
        }
    }
}
