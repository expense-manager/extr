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
import java.util.Date;
import java.util.concurrent.Callable;

import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/24/16.
 */

public class ReportFragment extends Fragment {
    private static final String TAG = ReportFragment.class.getSimpleName();

    public static final String DURATION_KEY = "duration";
    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;
    public static final int YEARLY = 2;


    public ReportAdapter reportAdapter;
    public ArrayList<Date[]> dates;
    public int requestCode;

    @BindView(R.id.report_fragment_recycler_view_id) RecyclerView recyclerView;

    public static Fragment newInstance(int requestCode) {
        Fragment reportFragment = new ReportFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(DURATION_KEY, requestCode);
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

        requestCode = getArguments().getInt(DURATION_KEY);
        dates = new ArrayList<>();
        reportAdapter = new ReportAdapter(getActivity(), dates, requestCode);

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
                if (requestCode == WEEKLY) {
                    reportAdapter.addAll(Helpers.getAllWeeks());
                } else if (requestCode == MONTHLY){
                    reportAdapter.addAll(Helpers.getAllMonths());
                } else {
                    reportAdapter.addAll(Helpers.getAllYears());
                }
                reportAdapter.notifyDataSetChanged();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }


}
