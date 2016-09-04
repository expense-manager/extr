package com.expensemanager.app.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Zhaolong Zhong on 8/27/16.
 */

public class OverviewFragment extends Fragment {
    private static final String TAG = OverviewFragment.class.getSimpleName();

    private ArrayList<Expense> expenses;
    private OverviewAdapter overviewAdapter;
    private String groupId;

    @BindView(R.id.overview_fragment_total_text_view_id) TextView totalTextView;
    @BindView(R.id.overview_fragment_weekly_total_text_view_id) TextView weeklyTextView;
    @BindView(R.id.overview_fragment_monthly_total_text_view_id) TextView monthlyTextView;
    @BindView(R.id.overview_fragment_monthly_label_text_view_id) TextView monthlyLabelTextView;
    @BindView(R.id.overview_fragment_weekly_average_text_view_id) TextView weeklyAverageTextView;
    @BindView(R.id.overview_fragment_monthly_average_text_view_id) TextView monthlyAverageTextView;
    @BindView(R.id.overview_fragment_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.overview_fragment_fab_id) FloatingActionButton fab;

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.overview_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        expenses = new ArrayList<>();
        overviewAdapter = new OverviewAdapter(getActivity(), expenses);
        setupRecyclerView();

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(getActivity());
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        invalidateViews();
    }

    public void invalidateViews() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);

        Calendar calendar = Calendar.getInstance();

        monthlyLabelTextView.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
        totalTextView.setText("$" + new DecimalFormat("##").format(getTotalExpense()));
        weeklyTextView.setText("$" + new DecimalFormat("##.##").format(getWeeklyExpense()));
        monthlyTextView.setText("$" + new DecimalFormat("##.##").format(getMonthlyExpense()));
        weeklyAverageTextView.setText("$" + new DecimalFormat("##.##").format(getWeeklyAverage()));
        monthlyAverageTextView.setText("$" + new DecimalFormat("##.##").format(getMonthlyAverage()));

        overviewAdapter.clear();
        overviewAdapter.addAll(Expense.getAllExpensesByGroupId(groupId));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(overviewAdapter);
    }

    private double getWeeklyExpense() {
        Date currentDate = new Date();
        Date[] weekStartEnd = Helpers.getWeekStartEndDate(currentDate);
        RealmResults<Expense> weeklyExpenses = Expense.getExpensesByRangeAndGroupId(weekStartEnd, groupId);

        double weeklyTotal = 0;
        for (Expense expense : weeklyExpenses) {
            weeklyTotal += expense.getAmount();
        }

        return weeklyTotal;
    }

    private double getMonthlyExpense() {
        Date currentDate = new Date();
        Date[] monthStartEnd = Helpers.getMonthStartEndDate(currentDate);
        RealmResults<Expense> monthlyExpenses = Expense.getExpensesByRangeAndGroupId(monthStartEnd, groupId);

        double monthlyTotal = 0;
        for (Expense expense : monthlyExpenses) {
            monthlyTotal += expense.getAmount();
        }

        return monthlyTotal;
    }

    private double getWeeklyAverage() {
        List<Date[]> allWeeks = Helpers.getAllWeeks(groupId);

        if (allWeeks == null) {
            return 0;
        }

        int weeks = allWeeks.size();
        return getTotalExpense()/weeks;
    }

    private double getMonthlyAverage() {
        List<Date[]> allMonths = Helpers.getAllMonths(groupId);

        if (allMonths == null) {
            return 0;
        }
        int months = allMonths.size();
        return getTotalExpense()/months;
    }

    private double  getTotalExpense() {
        double total = 0.0;

        RealmResults<Expense> allExpenses = Expense.getAllExpensesByGroupId(groupId);
        for (Expense expense : allExpenses) {
            total += expense.getAmount();
        }

        return total;
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());
        invalidateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
