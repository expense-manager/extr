package com.expensemanager.app.report;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/24/16.
 */

public class ReportFragment extends Fragment {
    private static final String TAG = ReportFragment.class.getSimpleName();

    public static final String IS_WEEKLY_KEY = "isWeekly";

    public ReportAdapter reportAdapter;
    public ArrayList<Date> dates;
    public boolean isWeekly = true;

    public static Fragment newInstance(boolean isWeekly) {
        Fragment reportFragment = new ReportFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_WEEKLY_KEY, isWeekly);
        reportFragment.setArguments(bundle);

        return reportFragment;
    }

    @BindView(R.id.report_fragment_recycler_view_id) RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.report_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        isWeekly = getArguments().getBoolean(IS_WEEKLY_KEY);

        Log.d(TAG, "isWeekly: " + isWeekly);

        dates = new ArrayList<>();
        reportAdapter = new ReportAdapter(getActivity(), dates, isWeekly);

        setupRecyclerView();
        invalidateViews();

        if (isWeekly) {
            dates.addAll(getAllWeeks());
        } else {
            dates.addAll(getAllMonths());
        }
    }

    private void invalidateViews() {
        if (!Helpers.isOnline()) {
            Toast.makeText(getActivity(), "Cannot retrieve messages at this time. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(reportAdapter);
    }

    public ArrayList<Date> getAllMonths() {
        ArrayList<Date> months = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        Date startDate = calendar.getTime();

        // Current calendar
        Calendar currentCalendar = Calendar.getInstance();
        Date currentMonth = currentCalendar.getTime();

        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(startDate);

        while (gcal.getTime().compareTo(currentMonth) <= 0) {
            months.add(gcal.getTime());
            gcal.add(Calendar.MONTH, 1);
        }

        return months;
    }

    public ArrayList<Date> getAllWeeks() {
        ArrayList<Date> weeks = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        Date startDate = calendar.getTime();

        Calendar currentCalendar = Calendar.getInstance();
        Date currentMonth = currentCalendar.getTime();

        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(startDate);

        while (gcal.getTime().compareTo(currentMonth) <= 0) {
            weeks.add(gcal.getTime());
            gcal.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return weeks;
    }
}
