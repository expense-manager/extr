package com.expensemanager.app.overview;

import android.app.Fragment;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by Zhaolong Zhong on 9/16/16.
 */

public class BudgetFragment extends Fragment {
    private static final String TAG = BudgetFragment.class.getSimpleName();

    private int levelTotal = 10000;
    private int levelStatus = 10000;
    private int level;
    private int steps = 100;
    private int animationTime = 2000;
    private double budgetMonthly;
    private double amountLeftMonthly;
    private double budgetWeekly;
    private double amountLeftWeekly;
    private String groupId;

    private ClipDrawable clipDrawable;
    private Handler handler = new Handler();

    @BindView(R.id.circle_solid_view_id) View budgetView;
    @BindView(R.id.circle_amount_text_view_id) TextView circleAmountTextView;
    @BindView(R.id.monthly_amount_text_view_id) TextView monthlyAmountTextView;
    @BindView(R.id.weekly_amount_text_view_id) TextView weeklyAmountTextView;

    public static Fragment newInstance() {
        return new BudgetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.budget_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        groupId = Helpers.getCurrentGroupId();

        invalidateView();
    }

    public void invalidateView() {
        Group group = Group.getGroupById(groupId);
        if (group != null) {
            budgetMonthly = group.getMonthlyBudget();
            budgetWeekly = group.getWeeklyBudget();
        }

        monthlyAmountTextView.setText(Helpers.doubleToCurrency(budgetMonthly));
        weeklyAmountTextView.setText(Helpers.doubleToCurrency(budgetWeekly));

        double monthlyExpense = getMonthlyExpense(), weeklyExpense = getWeeklyExpense();

        amountLeftMonthly = budgetMonthly - monthlyExpense;
        amountLeftWeekly = budgetWeekly - weeklyExpense;

        circleAmountTextView.setText(Helpers.doubleToCurrency(amountLeftMonthly));

        if (budgetMonthly == 0) {
            level = levelTotal;
        } else {
            level = (int) (Math.min(monthlyExpense, budgetMonthly) / budgetMonthly * levelTotal);
        }

        clipDrawable = (ClipDrawable) budgetView.getBackground();
        clipDrawable.setLevel(levelTotal);
        handler.post(animateRunnable);
    }

    private double getWeeklyExpense() {
        Date currentDate = new Date();
        Date[] weekStartEnd = Helpers.getWeekStartEndDate(currentDate);
        RealmResults<Expense> weeklyExpenses = Expense.getExpensesByRangeAndGroupId(weekStartEnd, groupId);

        double weeklyTotal = 0;
        for (Expense expense : weeklyExpenses) {
            weeklyTotal += expense.getAmount();
        }

        return Math.round(weeklyTotal * 100.0) / 100.0;
    }

    private double getMonthlyExpense() {
        Date currentDate = new Date();
        Date[] monthStartEnd = Helpers.getMonthStartEndDate(currentDate);
        RealmResults<Expense> monthlyExpenses = Expense.getExpensesByRangeAndGroupId(monthStartEnd, groupId);

        double monthlyTotal = 0;
        for (Expense expense : monthlyExpenses) {
            monthlyTotal += expense.getAmount();
        }

        return Math.round(monthlyTotal * 100.0) / 100.0;
    }

    private Runnable animateRunnable = new Runnable() {
        @Override
        public void run() {
            animateClipDrawable(animationTime);
        }
    };

    private void animateClipDrawable(int milliseconds) {
        if (levelStatus <= level) {
            handler.removeCallbacks(animateRunnable);
            return;
        }

        int stepTime = milliseconds / steps;
        levelStatus -= level/steps;
        clipDrawable.setLevel(levelStatus);

        handler.postDelayed(animateRunnable, stepTime);
    }
}
