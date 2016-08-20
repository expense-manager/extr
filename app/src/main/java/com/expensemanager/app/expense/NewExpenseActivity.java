package com.expensemanager.app.expense;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.service.SyncExpense;

import org.json.JSONObject;

import java.util.UUID;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class NewExpenseActivity extends AppCompatActivity {
    private static final String TAG = NewExpenseActivity.class.getSimpleName();

    public static final String NEW_PHOTO = "Take a photo";
    public static final String LIBRARY_PHOTO = "Choose from library";

    private AlertDialog.Builder choosePhotoSource;

    private Expense expense;

    @BindView(R.id.new_expense_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.new_expense_activity_toolbar_close_image_view_id) ImageView closeImageView;
    @BindView(R.id.new_expense_activity_toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.new_expense_activity_toolbar_post_text_view_id) TextView postTextView;
    @BindView(R.id.new_expense_activity_amount_text_view_id) TextView amountTextView;
    @BindView(R.id.new_expense_activity_note_text_view_id) TextView noteTextView;
    @BindView(R.id.new_expense_activity_add_photo_image_view_id) ImageView addPhotoImageView;
    @BindView(R.id.new_expense_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NewExpenseActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_expense_activity);
        ButterKnife.bind(this);

        setupToolbar();
        expense = new Expense();
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
        setPhotoSourcePicker();
        addPhotoImageView.setOnClickListener(v -> {
            choosePhotoSource.show();
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        titleTextView.setText(getString(R.string.create_an_expense));
        titleTextView.setOnClickListener(v -> close());
        closeImageView.setOnClickListener(v -> close());
        postTextView.setOnClickListener(v -> save());
    }

    private void setPhotoSourcePicker() {
        choosePhotoSource = new AlertDialog.Builder(this);

        final ArrayAdapter<String> photoSourceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1);
        photoSourceAdapter.add(NEW_PHOTO);
        photoSourceAdapter.add(LIBRARY_PHOTO);

        choosePhotoSource.setAdapter(photoSourceAdapter, (DialogInterface dialog, int which) -> {
            String photoSource = photoSourceAdapter.getItem(which);
            if (photoSource == null) {
                //take a photo
                return;
            }

            switch (photoSource) {
                case NEW_PHOTO:
                    Log.d(TAG, "Take a photo");
                    break;
                case LIBRARY_PHOTO:
                    Log.d(TAG, "Choose photo from library");
                    break;
            }
        });
    }

    private void save() {
        String uuid = UUID.randomUUID().toString();
        expense.setId(uuid);
        expense.setAmount(Double.valueOf(amountTextView.getText().toString()));
        expense.setNote(noteTextView.getText().toString());

        progressBar.setVisibility(View.VISIBLE);
        SyncExpense.create(expense).continueWith(onCreateSuccess, Task.UI_THREAD_EXECUTOR);
        closeSoftKeyboard();
    }

    private Continuation<JSONObject, Void> onCreateSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in creating new expense.", task.getError());
            }

            JSONObject result = task.getResult();
            String expenseId = result.optString(Expense.OBJECT_ID_JSON_KEY);

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            expense.setId(expenseId);
            realm.copyToRealmOrUpdate(expense);
            realm.commitTransaction();
            realm.close();

            close();

            return null;
        }
    };

    public void closeSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (inputMethodManager != null && view != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void close() {
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        close();
    }
}
