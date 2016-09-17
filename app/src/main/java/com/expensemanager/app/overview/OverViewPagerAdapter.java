package com.expensemanager.app.overview;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zhaolong Zhong on 9/6/16.
 */

public class OverViewPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = OverViewPagerAdapter.class.getSimpleName();

    private String[] tabTitles = {"Weekly", "Monthly", "Average"};
    private Map<Integer, Fragment> map = new HashMap<>();
    private Date[] startEnd;
    private int requestCode;

    // Adapter gets the manager insert or remove fragment from activity
    public OverViewPagerAdapter(FragmentManager fragmentManager, Date[] startEnd, int requestCode) {
        super(fragmentManager);
        this.startEnd = startEnd;
        this.requestCode = requestCode;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment =  AverageFragment.newInstance();
        } else if (position == 1) {

        }

        map.put(position, fragment);

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    public Fragment getFragmentByPosition(int position) {
        return map.get(position);
    }
}
