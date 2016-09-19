package com.expensemanager.app.overview;

import android.app.Fragment;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.SyncGroup;
import com.expensemanager.app.service.SyncMember;

import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Zhaolong Zhong on 9/16/16.
 */

public class BudgetFragment extends Fragment implements FragmentLifecycle {
    private static final String TAG = BudgetFragment.class.getSimpleName();

    private int levelTotal = 10000;
    private int levelStatus;
    private int level;
    private int steps = 100;
    private int animationTime = 1500;
    private double budgetMonthly;
    private double amountLeftMonthly;
    private double budgetWeekly;
    private double amountLeftWeekly;
    private double monthlyExpense;
    private double weeklyExpense;
    private String groupId;
    private long lastTimeAnimateLevel = 0;
    private boolean isFirstAnimation = true;

    private ClipDrawable clipDrawable;
    private Handler handler = new Handler();

    @BindView(R.id.circle_solid_view_id) View budgetView;
    @BindView(R.id.circle_amount_text_view_id) TextView circleAmountTextView;
    @BindView(R.id.monthly_amount_text_view_id) TextView monthlyAmountTextView;
    @BindView(R.id.weekly_amount_text_view_id) TextView weeklyAmountTextView;
    @BindView(R.id.circle_month_text_view_id) TextView circleMonthTextView;

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
    }

    public void invalidateViews() {
        Log.d(TAG, "invalidateViews");
        if (groupId == null) {
            groupId = Helpers.getCurrentGroupId();
        }

        Group group = Group.getGroupById(groupId);

        if (group != null) {
            budgetMonthly = group.getMonthlyBudget();
            budgetWeekly = group.getWeeklyBudget();
        } else {
            if (groupId != null) {
                SyncGroup.getGroupById(groupId).continueWith(onGetGroupsFinished, Task.UI_THREAD_EXECUTOR);
            } else {
                Log.e(TAG, "groupId is null");
            }
        }

        monthlyAmountTextView.setText(Helpers.doubleToCurrency(budgetMonthly));
        weeklyAmountTextView.setText(Helpers.doubleToCurrency(budgetWeekly));

        monthlyExpense = getMonthlyExpense();
        weeklyExpense = getWeeklyExpense();

        amountLeftMonthly = budgetMonthly - monthlyExpense;
        amountLeftWeekly = budgetWeekly - weeklyExpense;

        circleAmountTextView.setText(Helpers.doubleToCurrency(amountLeftMonthly));
        circleMonthTextView.setText(Helpers.getShortMonthStringOnlyFromDate(new Date()));

        invalidateProgressBars();
    }

    private void invalidateProgressBars() {

        if (budgetMonthly == 0) {
            level = levelTotal;
        } else {
            level = (int) (Math.min(monthlyExpense, budgetMonthly) / budgetMonthly * levelTotal);
        }

        levelStatus = levelTotal;
        clipDrawable = (ClipDrawable) budgetView.getBackground();
        clipDrawable.setLevel(levelTotal);
        long currentTime = System.currentTimeMillis();

        Log.d(TAG, "level: " + level + ", level status: " + levelStatus + ", level total: "
                + levelTotal + "lastTime:" + lastTimeAnimateLevel + "currentTime:" + currentTime);
        if (currentTime - lastTimeAnimateLevel > 3000 && level != levelTotal) {
            Log.d(TAG, "start animation.");
            isFirstAnimation = false;
            lastTimeAnimateLevel = currentTime;
            handler.removeCallbacks(animateRunnable);
            handler.postDelayed(animateRunnable, 500);
        } else {
            Log.d(TAG, "last animation less than 3 seconds, cancel.");
        }
    }

    private Continuation<Void, Void> onGetGroupsFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            if (groupId != null) {
                // Sync all categories of current group
                SyncCategory.getAllCategoriesByGroupId(groupId);
                // Sync all expenses of current group
                SyncExpense.getAllExpensesByGroupId(groupId);
                // Sync all members of current group
                SyncMember.getMembersByGroupId(groupId);
            }

            invalidateViews();
            return null;
        }
    };

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

    @Override
    public void onPauseFragment() {
        Log.d(TAG, "onPauseFragment()");
        handler.removeCallbacks(animateRunnable);
    }

    @Override
    public void onResumeFragment() {
        Log.d(TAG, "onResumeFragment()");
        if (!isFirstAnimation) {
            invalidateProgressBars();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> {
            Log.d(TAG, "realmChanged");
            invalidateViews();
        });

        invalidateViews();

        Log.d(TAG, "zhaox onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();

        Log.d(TAG, "zhaox onPause: ");
    }
}
