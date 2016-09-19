package com.expensemanager.app.report.pie_char;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.report.ReportDetailActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;

public class ReportPieChartFragment extends Fragment {
    private static final String TAG = ReportDetailActivity.class.getSimpleName();

    public static final String START_END_DATE = "startEnd";
    public static final String REQUEST_CODE = "request_code";

    private ReportCategoryAdapter reportCategoryAdapter;
    private List<Expense> expenses;
    private ArrayList<Category> categories;
    private ArrayList<Double> amounts;
    private Date[] startEnd;
    private int requestCode;
    private String groupId;
    Unbinder unbinder;

    @BindView(R.id.report_chart_fragment_recycler_view_id) RecyclerView recyclerView;

    // Creates a new fragment given an int and title
    public static ReportPieChartFragment newInstance(Date[] startEnd, int requestCode) {
        ReportPieChartFragment reportPieChartFragment = new ReportPieChartFragment();
        Bundle args = new Bundle();
        args.putInt(REQUEST_CODE, requestCode);
        args.putSerializable(START_END_DATE, startEnd);
        reportPieChartFragment.setArguments(args);
        return reportPieChartFragment;
    }

    // 1.onAttach(Activity) called once the fragment is associated with its activity.
    // 2.onCreate(Bundle) called to do initial creation of the fragment.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupId = Helpers.getCurrentGroupId();

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
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
    }

    public void invalidateViews() {
        reportCategoryAdapter.clear();

        Bundle bundle = getArguments();

        if (bundle != null) {
            startEnd = (Date[]) bundle.getSerializable(START_END_DATE);
            requestCode = bundle.getInt(REQUEST_CODE);
        }

        if (startEnd != null) {
            Log.d(TAG, "Start: " + startEnd[0].getTime());
            Log.d(TAG, "End: " + startEnd[1].getTime());
            int[] startEndDay = Helpers.getStartEndDay(startEnd);
            if (startEndDay == null) {
                Log.i(TAG, "Invalid start end day.");
                return;
            }
            // Query data from date range
            expenses = Expense.getExpensesByRangeAndGroupId(startEnd, groupId);
            reportCategoryAdapter.setStartEnd(startEnd);
        }
        if (expenses != null) {
            reportCategoryAdapter.addAll(expenses);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(reportCategoryAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        // Listen to database updates
        realm.addChangeListener(v -> invalidateViews());

        invalidateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind frrament and ButterKnife
        unbinder.unbind();
    }
}