package com.expensemanager.app.report.bar_char;

import android.content.SharedPreferences;
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
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.report.ReportDetailActivity;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;

public class ReportBarChartFragment extends Fragment {
    private static final String TAG = ReportDetailActivity.class.getSimpleName();

    public static final String START_END_DATE = "startEnd";
    public static final String REQUEST_CODE = "request_code";

    private ReportExpenseAdapter reportExpenseAdapter;
    private List<Expense> expenses;
    private Date[] startEnd;
    private int requestCode;
    Unbinder unbinder;
    private String groupId;

    @BindView(R.id.report_chart_fragment_recycler_view_id) RecyclerView recyclerView;

    // Creates a new fragment given an int and title
    public static ReportBarChartFragment newInstance(Date[] startEnd, int requestCode) {
        ReportBarChartFragment reportBarChartFragment = new ReportBarChartFragment();
        Bundle args = new Bundle();
        args.putInt(REQUEST_CODE, requestCode);
        args.putSerializable(START_END_DATE, startEnd);
        reportBarChartFragment.setArguments(args);
        return reportBarChartFragment;
    }

    // 1.onAttach(Activity) called once the fragment is associated with its activity.
    // 2.onCreate(Bundle) called to do initial creation of the fragment.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);
    }

    // 3.onCreateView() creates and returns the view hierarchy associated with the fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent,
        @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.report_chart_fragment, parent, false);
        // bind fragment with ButterKnife
        unbinder = ButterKnife.bind(this, v);

        Bundle bundle = getArguments();

        if (bundle != null) {
            startEnd = (Date[]) bundle.getSerializable(START_END_DATE);
            requestCode = bundle.getInt(REQUEST_CODE);
        }

        reportExpenseAdapter = new ReportExpenseAdapter(getContext(), startEnd, requestCode);

        setupRecyclerView();

        return v;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(reportExpenseAdapter);
    }

    public void invalidateViews() {
        reportExpenseAdapter.clear();

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
        }
        if (expenses != null) {
            reportExpenseAdapter.addAll(expenses, requestCode);
        }
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
        // unbind fragment and ButterKnife
        unbinder.unbind();
    }
}