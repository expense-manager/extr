package com.expensemanager.app.category;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.service.SyncCategory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class CategoryDetailActivity extends AppCompatActivity
    implements ColorPickerFragment.ColorPickerListener {
    private static final String TAG = CategoryDetailActivity.class.getSimpleName();

    private static final String CATEGORY_ID = "CATEGORY_ID";

    private Category category;
    private boolean isEditable = false;
    private Set<String> usedColors;
    private String currentColor;

    @BindView(R.id.category_detail_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.category_detail_activity_cancel_button_id) Button cancelButton;
    @BindView(R.id.category_detail_activity_delete_button_id) Button deleteButton;
    @BindView(R.id.category_detail_activity_edit_button_id) Button editButton;
    @BindView(R.id.category_detail_activity_color_image_view_id) ImageView colorImageView;
    @BindView(R.id.category_detail_activity_save_button_id) Button saveButton;
    @BindView(R.id.category_detail_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context, String id) {
        Intent intent = new Intent(context, CategoryDetailActivity.class);
        intent.putExtra(CATEGORY_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_detail_activity);
        ButterKnife.bind(this);

        String categoryId = getIntent().getStringExtra(CATEGORY_ID);
        category = Category.getCategoryById(categoryId);

        usedColors = Helpers.getUsedColorSet();

        invalidateViews();
    }

    private void invalidateViews() {
        nameEditText.setText(category.getName());
        currentColor = category.getColor();
        colorImageView.setBackgroundColor(Color.parseColor(category.getColor()));

        colorImageView.setOnClickListener(v -> selectColor());
        editButton.setOnClickListener(v -> setEditMode(true));
        cancelButton.setOnClickListener(v -> setEditMode(false));
        deleteButton.setOnClickListener(v -> delete());
        saveButton.setOnClickListener(v -> save());

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        cancelButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);

        setupEditableViews(isEditable);
    }

    private void selectColor() {
        if (isEditable) {
            ColorPickerFragment colorPickerFragment = ColorPickerFragment
                .newInstance(currentColor);
            // Pass listener
            colorPickerFragment.setListener(this);
            colorPickerFragment.show(getSupportFragmentManager(), ColorPickerFragment.class.getSimpleName());
        }
    }

    @Override
    public void onFinishCategoryColorDialog(String color) {
        usedColors.remove(currentColor);
        usedColors.add(color);
        currentColor = color;
        colorImageView.setBackgroundColor(Color.parseColor(color));
    }

    private void setupEditableViews(boolean isEditable) {
        nameEditText.setFocusable(isEditable);
        nameEditText.setFocusableInTouchMode(isEditable);
        nameEditText.setClickable(isEditable);

        if (isEditable) {
            nameEditText.requestFocus();
            nameEditText.setSelection(nameEditText.length());
        }
    }

    private void setEditMode(boolean isEditable) {
        this.isEditable = isEditable;
        invalidateViews();
    }

    private Continuation<Void, Void> onUpdateSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in updating category.", task.getError());
            }

            Log.d(TAG, "Update category success.");

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(category);
            realm.commitTransaction();
            realm.close();

            return null;
        }
    };

    private void save() {
        String name = nameEditText.getText().toString();

        if (name.length() == 0) {
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        category.setName(name);
        category.setColor(currentColor);
        realm.copyToRealmOrUpdate(category);
        realm.commitTransaction();
        realm.close();

        SyncCategory.update(category).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);

        progressBar.setVisibility(View.VISIBLE);
        isEditable = !isEditable;
        invalidateViews();
    }

    private Continuation<Void, Void> onDeleteSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in deleting category.", task.getError());
            }

            Category.delete(category.getId());
            Log.d(TAG, "Delete category success.");
            close();
            return null;
        }
    };

    private void delete() {
        progressBar.setVisibility(View.VISIBLE);
        SyncCategory.delete(category.getId()).continueWith(onDeleteSuccess, Task.UI_THREAD_EXECUTOR);
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
