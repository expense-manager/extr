package com.expensemanager.app.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by Zhaolong Zhong on 8/27/16.
 */

public class OverviewFragment extends Fragment {
    private static final String TAG = OverviewFragment.class.getSimpleName();

    @BindView(R.id.overview_fragment_total_text_view_id) TextView totalTextView;
    @BindView(R.id.overview_fragment_weekly_total_text_view_id) TextView weeklyTextView;
    @BindView(R.id.overview_fragment_monthly_total_text_view_id) TextView monthlyTextView;
    @BindView(R.id.overview_fragment_monthly_label_text_view_id) TextView monthlyLabelTextView;
    @BindView(R.id.overview_fragment_weekly_average_text_view_id) TextView weeklyAverageTextView;
    @BindView(R.id.overview_fragment_monthly_average_text_view_id) TextView monthlyAverageTextView;

    public static Fragment newInstance() {
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

        invalidateViews();
    }

    private void invalidateViews() {
        //todo: uncomment out total and average when we have a large data set
        Calendar calendar = Calendar.getInstance();

        monthlyLabelTextView.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
//        totalTextView.setText("$" + new DecimalFormat("##").format(getTotalExpense()));
        weeklyTextView.setText("$" + new DecimalFormat("##.##").format(getWeeklyExpense()));
        monthlyTextView.setText("$" + new DecimalFormat("##.##").format(getMonthlyExpense()));
//        weeklyAverageTextView.setText("$" + new DecimalFormat("##.##").format(getWeeklyAverage()));
//        monthlyAverageTextView.setText("$" + new DecimalFormat("##.##").format(getMonthlyAverage()));
    }

    private double getWeeklyExpense() {
        Date currentDate = new Date();
        Date[] weekStartEnd = Helpers.getWeekStartEndDate(currentDate);
        RealmResults<Expense> weeklyExpenses = Expense.getExpensesByRange(weekStartEnd);

        double weeklyTotal = 0;
        for (Expense expense : weeklyExpenses) {
            weeklyTotal += expense.getAmount();
        }

        return weeklyTotal;
    }

    private double getMonthlyExpense() {
        Date currentDate = new Date();
        Date[] monthStartEnd = Helpers.getMonthStartEndDate(currentDate);
        RealmResults<Expense> monthlyExpenses = Expense.getExpensesByRange(monthStartEnd);

        double monthlyTotal = 0;
        for (Expense expense : monthlyExpenses) {
            monthlyTotal += expense.getAmount();
        }

        return monthlyTotal;
    }

    private double getWeeklyAverage() {
        int weeks = Helpers.getAllWeeks().size();
        return getTotalExpense()/weeks;
    }

    private double getMonthlyAverage() {
        int months = Helpers.getAllMonths().size();
        return getTotalExpense()/months;
    }

    private double getTotalExpense() {
        double total = 0.0;

        RealmResults<Expense> allExpenses = Expense.getAllExpenses();
        for (Expense expense : allExpenses) {
            total += expense.getAmount();
        }

        return total;
    }
}
