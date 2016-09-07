package com.expensemanager.app.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Zhaolong Zhong on 8/27/16.
 */

public class OverviewFragment extends Fragment {
    private static final String TAG = OverviewFragment.class.getSimpleName();

    private Handler handler = new Handler();
    private int totalStatus = 0;
    private int weeklyStatus = 0;
    private int monthlyStatus = 0;
    private ArrayList<Expense> expenses;
    private OverviewAdapter overviewAdapter;
    private String groupId;
    private double totalExpense = 0.0;
    private double weeklyExpense = 0.0;
    private double weeklyAve = 0.0;
    private double monthlyExpense = 0.0;
    private double monthlyAve = 0.0;

    @BindView(R.id.overview_fragment_total_text_view_id) TextView totalTextView;
    @BindView(R.id.overview_fragment_weekly_total_text_view_id) TextView weeklyTextView;
    @BindView(R.id.overview_fragment_monthly_total_text_view_id) TextView monthlyTextView;
    @BindView(R.id.overview_fragment_weekly_average_text_view_id) TextView weeklyAverageTextView;
    @BindView(R.id.overview_fragment_monthly_average_text_view_id) TextView monthlyAverageTextView;
    @BindView(R.id.overview_fragment_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.overview_fragment_fab_id) FloatingActionButton fab;
    @BindView(R.id.weekly_circular_progress_text) TextView weeklyProgressTextView;
    @BindView(R.id.monthly_circular_progress_text) TextView monthlyProgressTextView;
    @BindView(R.id.overview_total_progressBar) ProgressBar totalProgressBar;
    @BindView(R.id.overview_weekly_progressBar) ProgressBar weeklyProgressBar;
    @BindView(R.id.overview_monthly_progressBar) ProgressBar monthlyProgressBar;

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

        totalExpense = getTotalExpense();
        weeklyExpense = getWeeklyExpense();
        weeklyAve = getWeeklyAverage();
        monthlyExpense = getMonthlyExpense();
        monthlyAve = getMonthlyAverage();

//        totalTextView.setText("$" + new DecimalFormat("##").format(getTotalExpense()));
        weeklyTextView.setText("$" + new DecimalFormat("##.##").format(weeklyExpense));
        monthlyTextView.setText("$" + new DecimalFormat("##.##").format(monthlyExpense));
        weeklyAverageTextView.setText("$" + new DecimalFormat("##.##").format(weeklyAve));
        monthlyAverageTextView.setText("$" + new DecimalFormat("##.##").format(monthlyAve));

        overviewAdapter.clear();
        if (Member.getAllAcceptedMembersByGroupId(groupId).size() > 1) {
            overviewAdapter.setShowMember(true);
        } else {
            overviewAdapter.setShowMember(false);
        }
        overviewAdapter.addAll(Expense.getAllExpensesByGroupId(groupId));

        Log.d(TAG, "zhaox: weeklyExpense: " + weeklyExpense);
        Log.d(TAG, "zhaox: weeklyAve: " + weeklyAve);

        int totalProgress = 100;
        int weeklyProgress = (int)(weeklyExpense/weeklyAve * 100);
        int monthlyProgress = (int)(monthlyExpense/monthlyAve * 100);

        setupTotalProgress(totalExpense);
        setupWeeklyProgress(weeklyProgress);
        setupMonthlyProgress(monthlyProgress);
    }

    private void setupTotalProgress(final double totalProgress) {
        totalProgressBar.setMax((int)totalProgress);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int step = 1;
                if (totalProgress >= 100) {
                    step = (int) (totalProgress/100);
                }

                final int finalStep = step;
                while (totalStatus < totalProgress) {
                    totalStatus += step;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            totalProgressBar.setProgress(totalStatus);

                            if (totalStatus + finalStep >= totalProgress) {
                                totalTextView.setText("$" + totalProgress);
                            } else {
                                totalTextView.setText("$" + totalStatus);
                            }
                        }
                    });
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void setupWeeklyProgress(int weeklyProgress) {

        Log.d(TAG, "zhaox: weeklyProcess: " + weeklyProgress);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (weeklyStatus < weeklyProgress) {
                    weeklyStatus += 1;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            weeklyProgressBar.setProgress(weeklyStatus);
                            weeklyProgressTextView.setText(weeklyStatus + "%");
                        }
                    });
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

//        if(android.os.Build.VERSION.SDK_INT >= 11){
//            // will update the "progress" propriety of seekbar until it reaches progress
//
//            ObjectAnimator animation2 = ObjectAnimator.ofInt(circularProgress, "progress", 80);
//            animation2.setDuration(1500); // 0.5 second
//            animation2.setInterpolator(new DecelerateInterpolator());
//            animation2.start();
//        } else {
//            seekBar.setProgress(80);
//        }
    }

    private void setupMonthlyProgress(int monthlyProgress) {

        Log.d(TAG, "zhaox: monthlyProgress: " + monthlyProgress);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                while (monthlyStatus < monthlyProgress) {
                    monthlyStatus += 1;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            monthlyProgressBar.setProgress(monthlyStatus);
                            monthlyProgressTextView.setText(monthlyStatus + "%");
                        }
                    });
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

//        if(android.os.Build.VERSION.SDK_INT >= 11){
//            // will update the "progress" propriety of seekbar until it reaches progress
//
//            ObjectAnimator animation2 = ObjectAnimator.ofInt(circularProgress, "progress", 80);
//            animation2.setDuration(1500); // 0.5 second
//            animation2.setInterpolator(new DecelerateInterpolator());
//            animation2.start();
//        } else {
//            seekBar.setProgress(80);
//        }
    }

    private int getWeeklyProcess() {
        if (weeklyAve > 1) {
            return (int)(weeklyExpense / weeklyAve);
        }

        return 0;
    }

    private int getMonthlyProcess() {
//        if (getMonthlyAverage() > 1) {
//            return (int)(getMonthlyExpense() / getMonthlyAverage());
//        }

        return 0;
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
