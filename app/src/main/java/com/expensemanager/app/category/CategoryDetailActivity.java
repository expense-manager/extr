package com.expensemanager.app.category;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.category.color_picker.ColorPickerFragment;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncCategory;

import java.util.Set;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class CategoryDetailActivity extends AppCompatActivity
    implements ColorPickerFragment.ColorPickerListener {
    private static final String TAG = CategoryDetailActivity.class.getSimpleName();

    private static final String CATEGORY_ID = "CATEGORY_ID";

    private Category category;
    private boolean isEditable = false;
    private Set<String> usedColors;
    private String currentColor;
    private String groupId;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.category_detail_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.category_detail_activity_delete_button_id) Button deleteButton;
    @BindView(R.id.category_detail_activity_color_image_view_id) CircleImageView colorImageView;
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

        setupToolbar();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);

        String categoryId = getIntent().getStringExtra(CATEGORY_ID);
        category = Category.getCategoryById(categoryId);

        usedColors = Helpers.getUsedColorSet(groupId);

        invalidateViews();
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.title_activity_category_detail));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
        editTextView.setOnClickListener(v -> setEditMode(true));
        saveTextView.setOnClickListener(v -> save());
    }

    private void invalidateViews() {
        nameEditText.setText(category.getName());
        currentColor = category.getColor();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
        colorImageView.setImageDrawable(colorDrawable);

        colorImageView.setOnClickListener(v -> selectColor());
        deleteButton.setOnClickListener(v -> delete());

        editTextView.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveTextView.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);

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

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);
        String groupId = sharedPreferences.getString(Group.ID_KEY, null);

        if (loginUserId == null || groupId == null) {
            Log.i(TAG, "Error getting login user id or group id.");
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        category.setName(name);
        category.setColor(currentColor);
        category.setGroupId(groupId);
        realm.copyToRealmOrUpdate(category);
        realm.commitTransaction();
        realm.close();

        SyncCategory.update(category).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);

        progressBar.setVisibility(View.VISIBLE);
        closeSoftKeyboard();
        isEditable = false;
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

    public void closeSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (inputMethodManager != null && view != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

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
