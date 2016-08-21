package com.expensemanager.app.overview;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.report.ReportDetailActivity;
import com.expensemanager.app.service.SyncExpense;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class OverviewActivity extends AppCompatActivity {
    private static final String TAG = OverviewActivity.class.getSimpleName();

    private ArrayList<Expense> expenses;
    private OverviewAdapter overviewAdapter;

    @BindView(R.id.overview_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.overview_activity_fab_id) FloatingActionButton fab;
    @BindView(R.id.overview_activity_month_text_view_id) TextView monthTextView;
    @BindView(R.id.overview_activity_total_amount_text_view_id) TextView totalAmountTextView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, OverviewActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overview_activity);
        ButterKnife.bind(this);
        setTitle(R.string.overview);

        expenses = new ArrayList<>();
        overviewAdapter = new OverviewAdapter(this, expenses);

        monthTextView.setText(getCurrentMonth());
        setupRecyclerView();

        totalAmountTextView.setOnClickListener(v -> {
            ReportDetailActivity.newInstance(this);
        });

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        invalidateViews();
        SyncExpense.getAllExpenses();
    }

    private void invalidateViews() {
        overviewAdapter.clear();
        ArrayList<Expense> newExpenses = new ArrayList<>(Expense.getAllExpenses());
        double total = 0;
        for (Expense e : newExpenses) {
            total += e.getAmount();
        }
        totalAmountTextView.setText("$" + formatAccuracy(total));
        overviewAdapter.addAll(newExpenses);
    }

    private String formatAccuracy(double num) {
        return new DecimalFormat("0.00").format(num);
    }

    private String getCurrentMonth() {
        // Date format
        DateFormat format = new SimpleDateFormat("MMMM");
        // Current calendar
        Date date = new Date();

        return format.format(date);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(overviewAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
