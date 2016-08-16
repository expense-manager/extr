package com.expensemanager.app.expense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Expense;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class NewExpenseActivity extends AppCompatActivity {
    private static final String TAG = NewExpenseActivity.class.getSimpleName();

    private Expense expense;

    @BindView(R.id.new_expense_activity_amount_text_view_id) TextView amountTextView;
    @BindView(R.id.new_expense_activity_note_text_view_id) TextView noteTextView;
    @BindView(R.id.new_expense_activity_save_button_id) Button saveButton;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NewExpenseActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_expense_activity);
        ButterKnife.bind(this);

        expense = new Expense();
        saveButton.setOnClickListener(v -> save());
    }

    private void save() {
        String uuid = UUID.randomUUID().toString();
        expense.setId(uuid);
        expense.setAmount(Double.valueOf(amountTextView.getText().toString()));
        expense.setNote(noteTextView.getText().toString());

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(expense);
        realm.commitTransaction();
        realm.close();

        close();
    }

    private void close() {
        finish();
    }
}
