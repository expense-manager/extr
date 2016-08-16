package com.expensemanager.app.expense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Expense;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExpenseActivity extends AppCompatActivity {
    private static final String TAG = ExpenseActivity.class.getSimpleName();

    private ArrayList<Expense> expenses;
    private ExpenseAdapter expenseAdapter;

    @BindView(R.id.expense_activity_recycler_view_id) RecyclerView recyclerView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_activity);
        ButterKnife.bind(this);

        expenses = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenses);
        setupRecyclerView();

        invalidateViews();
    }

    private void invalidateViews() {
        expenseAdapter.clear();
        expenseAdapter.addAll(new ArrayList<>(Expense.getAllExpenses()));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(expenseAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateViews();
    }
}
