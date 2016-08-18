package com.expensemanager.app.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.expensemanager.app.R;
import com.expensemanager.app.category.CategoryActivity;
import com.expensemanager.app.expense.ExpenseActivity;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.welcome.WelcomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.main_activity_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
        });

        SyncExpense.getAllExpenses();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_expense_activity_id:
                ExpenseActivity.newInstance(this);
                return true;
            case R.id.menu_item_category_activity_id:
                CategoryActivity.newInstance(this);
                return true;
            case R.id.menu_item_welcome_activity_id:
                WelcomeActivity.newInstance(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
