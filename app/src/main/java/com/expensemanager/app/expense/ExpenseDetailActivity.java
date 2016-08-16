package com.expensemanager.app.expense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Expense;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ExpenseDetailActivity extends AppCompatActivity {
    private static final String TAG = ExpenseDetailActivity.class.getSimpleName();

    private static final String EXPENSE_ID = "EXPENSE_ID";

    private Expense expense;

    @BindView(R.id.expense_detail_activity_amount_text_view_id) TextView amountTextView;
    @BindView(R.id.expense_detail_activity_note_text_view_id) TextView noteTextView;

    public static void newInstance(Context context, String id) {
        Intent intent = new Intent(context, ExpenseDetailActivity.class);
        intent.putExtra(EXPENSE_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_detail_activity);
        ButterKnife.bind(this);

        String expenseId = getIntent().getStringExtra(EXPENSE_ID);
        expense = Expense.getExpenseById(expenseId);

        invalidateViews();
    }

    private void invalidateViews() {
        amountTextView.setText(String.valueOf(expense.getAmount()));
        noteTextView.setText(String.valueOf(expense.getNote()));

        amountTextView.setOnClickListener(v -> updateAmount());
        noteTextView.setOnClickListener(v -> updateNote());
    }

    private void updateAmount() {
        Log.d(TAG, "amountTextView clicked");
        // todo: implement logic
        // steps:
        // 1. popup a dialog
        // 2. set default amount in dialog
        // 3. click save button in dialog to save

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
//        expense.setAmount(NEW_VALUE);
        realm.copyToRealmOrUpdate(expense);
        realm.commitTransaction();
        realm.close();
    }

    private void updateNote() {
        Log.d(TAG, "noteTextView clicked");
        // todo: implement logic
    }
}
