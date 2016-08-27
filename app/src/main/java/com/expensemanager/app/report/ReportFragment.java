package com.expensemanager.app.report;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Callable;

import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/24/16.
 */

public class ReportFragment extends Fragment {
    private static final String TAG = ReportFragment.class.getSimpleName();

    public static final String IS_WEEKLY_KEY = "isWeekly";

    public ReportAdapter reportAdapter;
    public ArrayList<Date[]> dates;
    public boolean isWeekly = true;

    @BindView(R.id.report_fragment_recycler_view_id) RecyclerView recyclerView;

    public static Fragment newInstance(boolean isWeekly) {
        Fragment reportFragment = new ReportFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_WEEKLY_KEY, isWeekly);
        reportFragment.setArguments(bundle);

        return reportFragment;
    }

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
        dates = new ArrayList<>();
        reportAdapter = new ReportAdapter(getActivity(), dates, isWeekly);

        setupRecyclerView();
        invalidateViews();
    }

    private void invalidateViews() {
        getAllMonthsWeeksAsync();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(reportAdapter);
    }

    private void getAllMonthsWeeksAsync() {
        Task.call(new Callable<Void>() {
            public Void call() {
                if (isWeekly) {
                    dates.addAll(getAllWeeks());
                } else {
                    dates.addAll(getAllMonths());
                }
                reportAdapter.notifyDataSetChanged();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public ArrayList<Date[]> getAllMonths() {
        // todo: pass the first expense date as start date
        ArrayList<Date[]> months = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        Date startDate = calendar.getTime();

        // Current calendar
        Calendar currentCalendar = Calendar.getInstance();
        Date currentMonth = currentCalendar.getTime();

        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(startDate);

        while (gcal.getTime().compareTo(currentMonth) <= 0) {
            Date[] startEndDate = Helpers.getMonthStartEndDate(gcal.getTime());
            months.add(startEndDate);
            gcal.add(Calendar.MONTH, 1);
        }

        return months;
    }

    public ArrayList<Date[]> getAllWeeks() {
        // todo: pass the first expense date as start date
        ArrayList<Date[]> weeks = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        Date startDate = calendar.getTime();

        Calendar currentCalendar = Calendar.getInstance();
        Date currentMonth = currentCalendar.getTime();

        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(startDate);

        while (gcal.getTime().compareTo(currentMonth) <= 0) {
            Date[] startEnd = Helpers.getWeekStartEndDate(gcal.getTime());
            weeks.add(startEnd);
            gcal.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return weeks;
    }
}
