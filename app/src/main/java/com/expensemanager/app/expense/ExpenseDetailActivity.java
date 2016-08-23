package com.expensemanager.app.expense;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.service.SyncExpense;

import java.util.ArrayList;
import java.util.Arrays;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class ExpenseDetailActivity extends BaseActivity {
    private static final String TAG = ExpenseDetailActivity.class.getSimpleName();

    private static final String EXPENSE_ID = "EXPENSE_ID";

    private Expense expense;
    private Category category;
    private double amount;
    private boolean isEditable = false;

    private ArrayList<String> photoNameList;
    private ExpensePhotoAdapter expensePhotoAdapter;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.expense_detail_activity_amount_text_view_id) EditText amountTextView;
    @BindView(R.id.expense_detail_activity_note_text_view_id) EditText noteTextView;
    @BindView(R.id.expense_detail_activity_created_at_text_view_id) TextView createdAtTextView;
    @BindView(R.id.expense_detail_activity_grid_view_id) GridView photoGridView;
    @BindView(R.id.expense_detail_activity_cancel_button_id) Button cancelButton;
    @BindView(R.id.expense_detail_activity_delete_button_id) Button deleteButton;
    @BindView(R.id.expense_detail_activity_edit_button_id) Button editButton;
    @BindView(R.id.expense_detail_activity_save_button_id) Button saveButton;
    @BindView(R.id.expense_detail_activity_progress_bar_id) ProgressBar progressBar;
    @BindView(R.id.expense_detail_activity_category_relative_layout_id) RelativeLayout categoryRelativeLayout;
    @BindView(R.id.expense_detail_activity_category_color_image_view_id) CircleImageView categoryColorImageView;
    @BindView(R.id.expense_detail_activity_category_name_text_view_id) TextView categoryNameTextView;
    @BindView(R.id.expense_detail_activity_category_amount_text_view_id) TextView categoryAmountTextView;

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
        category = Category.getCategoryById(expense.getCategoryId());

        //todo: fix photo not found in realm
        Log.d(TAG, "onCreate expense photo:" + expense.getPhotos());

        setupToolbar();
        invalidateViews();

        SyncExpense.getExpensePhotoByExpenseId(expenseId).continueWith(onGetExpensePhotoSuccess, Task.UI_THREAD_EXECUTOR);
    }

    private void invalidateViews() {
        amountTextView.setText(String.valueOf(expense.getAmount()));
        setupCategory();
        noteTextView.setText(String.valueOf(expense.getNote()));
        createdAtTextView.setText(Helpers.formatCreateAt(expense.getCreatedAt()));

        editButton.setOnClickListener(v -> setEditMode(true));
        cancelButton.setOnClickListener(v -> setEditMode(false));
        deleteButton.setOnClickListener(v -> delete());
        saveButton.setOnClickListener(v -> save());

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        cancelButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);

        setupExpensePhoto();
        setupEditableViews(isEditable);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.expense_detail));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    private void setupExpensePhoto() {
        String photos = expense.getPhotos();
        Log.d(TAG, "setupExpensePhoto: " + photos);
        if (photos == null || photos.isEmpty()) {
            return;
        }
        photoNameList = new ArrayList<>();
        photoNameList.addAll(Arrays.asList(photos.split(",")));
        expensePhotoAdapter = new ExpensePhotoAdapter(this, null, photoNameList);
        photoGridView.setAdapter(expensePhotoAdapter);
        photoGridView.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            Log.d(TAG, "Photo clicked at position: " + position);
        });

        photoGridView.setOnItemLongClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            Log.d(TAG, "Photo long clicked at position: " + position);
            return true;
        });
    }

    private void setupCategory() {
        amount = getCategoryAmount();
        loadCategory();
        categoryRelativeLayout.setOnClickListener(v -> selectCategory());
    }

    private void selectCategory() {
        if (isEditable) {
            CategoryPickerFragment categoryPickerFragment = CategoryPickerFragment
                .newInstance();
            categoryPickerFragment.setListener(categoryPickerListener);
            categoryPickerFragment.show(getSupportFragmentManager(), CategoryPickerFragment.class.getSimpleName());
        }
    }

    private CategoryPickerFragment.CategoryPickerListener categoryPickerListener = new CategoryPickerFragment.CategoryPickerListener() {
        @Override
        public void onFinishExpenseCategoryDialog(Category category) {
            ExpenseDetailActivity.this.category = category;
            loadCategory();
        }
    };

    private void loadCategory() {
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
        categoryColorImageView.setImageDrawable(colorDrawable);
        categoryNameTextView.setText(category.getName());
    }

    private double getCategoryAmount() {
        double total = 0;
        for (Expense e : Expense.getAllExpenses()) {
            if (e.getCategoryId().equals(expense.getCategoryId())) {
                total += e.getAmount();
            }
        }

        return total;
    }

    private void setupEditableViews(boolean isEditable) {
        amountTextView.setFocusable(isEditable);
        amountTextView.setFocusableInTouchMode(isEditable);
        amountTextView.setClickable(isEditable);

        noteTextView.setFocusable(isEditable);
        noteTextView.setFocusableInTouchMode(isEditable);
        noteTextView.setClickable(isEditable);

        if (isEditable) {
            amountTextView.requestFocus();
            amountTextView.setSelection(amountTextView.length());
        }
    }

    private void setEditMode(boolean isEditable) {
        this.isEditable = isEditable;
        invalidateViews();
    }

    private Continuation<Void, Void> onGetExpensePhotoSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in getting expense photo.", task.getError());
            }

            Log.d(TAG, "onGetExpensePhotoSuccess Expense photos: " + expense.getPhotos());
            invalidateViews();
            return null;
        }
    };

    private Continuation<Void, Void> onUpdateSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in updating expense.", task.getError());
            }

            Log.d(TAG, "Update expense success.");

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            expense.setSynced(true);
            realm.copyToRealmOrUpdate(expense);
            realm.commitTransaction();
            realm.close();

            return null;
        }
    };

    private void save() {
        double amount;
        try {
            amount = Double.valueOf(amountTextView.getText().toString());
        } catch (Exception e) {
            Log.e(TAG, "Cannot convert amount to double.", e);
            Toast.makeText(this, "Incorrect amount format.", Toast.LENGTH_SHORT).show();
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        expense.setAmount(amount);
        expense.setNote(noteTextView.getText().toString());
        expense.setCategoryId(category.getId());
        expense.setSynced(false);
        realm.copyToRealmOrUpdate(expense);
        realm.commitTransaction();
        realm.close();

        SyncExpense.update(expense).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);

        progressBar.setVisibility(View.VISIBLE);
        isEditable = !isEditable;
        invalidateViews();
    }

    private Continuation<Void, Void> onDeleteSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in deleting expense.", task.getError());
            }

            Expense.delete(expense.getId());
            Log.d(TAG, "Delete expense success.");
            close();
            return null;
        }
    };

    private void delete() {
        progressBar.setVisibility(View.VISIBLE);
        SyncExpense.delete(expense.getId()).continueWith(onDeleteSuccess, Task.UI_THREAD_EXECUTOR);
    }

    @Override
    public void close() {
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        close();
    }
}
