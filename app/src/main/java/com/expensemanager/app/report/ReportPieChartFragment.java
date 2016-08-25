package com.expensemanager.app.report;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ReportPieChartFragment extends Fragment {
    private static final String TAG = ReportDetailActivity.class.getSimpleName();

    private ArrayList<Category> categories;
    private ArrayList<Double> amounts;
    private ReportCategoryAdapter reportCategoryAdapter;
    Unbinder unbinder;

    @BindView(R.id.report_chart_fragment_recycler_view_id) RecyclerView recyclerView;

    // Creates a new fragment given an int and title
    // todo: add search range to fragment
    public static ReportPieChartFragment newInstance() {
        ReportPieChartFragment reportPieChartFragment = new ReportPieChartFragment();
        Bundle args = new Bundle();
        reportPieChartFragment.setArguments(args);
        return reportPieChartFragment;
    }

    // 1.onAttach(Activity) called once the fragment is associated with its activity.
    // 2.onCreate(Bundle) called to do initial creation of the fragment.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categories = new ArrayList<>();
        amounts = new ArrayList<>();
        reportCategoryAdapter = new ReportCategoryAdapter(getContext(), categories, amounts);
    }

    // 3.onCreateView() creates and returns the view hierarchy associated with the fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent,
        @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.report_chart_fragment, parent, false);
        // bind fragment with ButterKnife
        unbinder = ButterKnife.bind(this, v);

        setupRecyclerView();

        invalidateViews();

        return v;
    }

    public void invalidateViews() {
        reportCategoryAdapter.clear();

        // todo: query data from real date range
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate =  calendar.getTime();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endDate =  calendar.getTime();
        // Query data from date range
        List<Expense> newExpenses = Expense.getRangeExpenses(startDate, endDate);
        reportCategoryAdapter.addAll(newExpenses);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(reportCategoryAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind frrament and ButterKnife
        unbinder.unbind();
    }
}