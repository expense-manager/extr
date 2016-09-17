package com.expensemanager.app.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncGroup;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewGroupActivity extends AppCompatActivity {
    private static final String TAG = NewGroupActivity.class.getSimpleName();

    private Group group;
    private double monthlyBudget;
    private double weeklyBudget;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.new_group_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.new_group_activity_group_edit_text_id) EditText groupEditText;
    @BindView(R.id.new_group_activity_about_edit_text_id) EditText aboutEditText;
    @BindView(R.id.new_group_activity_monthly_budget_edit_text_id) EditText monthlyBudgetEditText;
    @BindView(R.id.new_group_activity_weekly_budget_edit_text_id) EditText weeklyBudgetEditText;
    @BindView(R.id.progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NewGroupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_activity);
        ButterKnife.bind(this);

        String loginUserId = Helpers.getLoginUserId();
        group = new Group();
        group.setUserId(loginUserId);

        setupToolbar();
        invalidateViews();
    }

    private void invalidateViews() {
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.title_activity_new_group));
        saveTextView.setVisibility(View.VISIBLE);
        backImageView.setImageResource(R.drawable.ic_window_close);
        saveTextView.setText(R.string.create);

        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
        saveTextView.setOnClickListener(v -> save());
    }

    private void save() {
        if (!isValidateInput()) {
            return;
        }

        closeSoftKeyboard();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);
        if (loginUserId == null) {
            Log.i(TAG, "Error getting login user id.");
            return;
        }
        group.setUserId(loginUserId);
        String uuid = UUID.randomUUID().toString();
        group.setId(uuid);
        group.setName(nameEditText.getText().toString());
        group.setGroupname(groupEditText.getText().toString().toLowerCase());
        group.setAbout(aboutEditText.getText().toString());

        group.setMonthlyBudget(monthlyBudget);
        group.setWeeklyBudget(weeklyBudget);

        progressBar.setVisibility(View.VISIBLE);
        SyncGroup.create(group);
        closeSoftKeyboard();
        close();
    }

    private boolean isValidateInput() {
        String monthlyBudgetString = monthlyBudgetEditText.getText().toString();
        String weeklyBudgetString = weeklyBudgetEditText.getText().toString();

        if (monthlyBudgetString.length() > 0) {

            if (monthlyBudgetEditText.getText().charAt(0) == '$') {
                monthlyBudgetString = monthlyBudgetString.substring(1);
            }

            try {
                monthlyBudget = Double.parseDouble(monthlyBudgetString);
            } catch (NumberFormatException e) {
                Log.d(TAG, "Incorrect monthly input.");
                Toast.makeText(this, "Incorrect monthly number.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (weeklyBudgetString.length() > 0) {

            if (weeklyBudgetString.charAt(0) == '$') {
                weeklyBudgetString = weeklyBudgetString.substring(1);
            }

            try {
                weeklyBudget = Double.parseDouble(weeklyBudgetString);
            } catch (NumberFormatException e) {
                Log.d(TAG, "Incorrect weekly budget number.");
                Toast.makeText(this, "Incorrect weekly budget number.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

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