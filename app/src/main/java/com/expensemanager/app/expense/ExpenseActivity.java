package com.expensemanager.app.expense;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.service.SyncExpense;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ExpenseActivity extends BaseActivity {
    private static final String TAG = ExpenseActivity.class.getSimpleName();

    private ArrayList<Expense> expenses;
    private ExpenseAdapter expenseAdapter;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.expense_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.expense_activity_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_activity);
        ButterKnife.bind(this);
        setTitle(R.string.expense);

        setupToolbar();

        expenses = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenses);
        setupRecyclerView();

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        invalidateViews();
        SyncExpense.getAllExpenses();
    }

    private void invalidateViews() {
        expenseAdapter.clear();
        expenseAdapter.addAll(new ArrayList<>(Expense.getAllExpenses()));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(expenseAdapter);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.expense));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
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
