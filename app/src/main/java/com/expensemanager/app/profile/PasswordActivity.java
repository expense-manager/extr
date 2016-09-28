package com.expensemanager.app.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class PasswordActivity extends BaseActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private String loginUserId;
    private User loginUser;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_right_title_text_view_id) TextView saveTextView;
    @BindView(R.id.password_activity_profile_photo_image_view_id) ImageView profilePhotoImageView;
    @BindView(R.id.password_activity_curernt_password_edit_text_id) EditText currentPasswordEditText;
    @BindView(R.id.password_activity_new_password_edit_text_id) EditText newPasswordEditText;
    @BindView(R.id.password_activity_confirm_password_edit_text_id) EditText confirmPasswordEditText;
    @BindView(R.id.password_activity_error_text_view_id) TextView errorMessageTextView;
    @BindView(R.id.password_activity_error_relative_layout_id) RelativeLayout errorMessageRelativeLayout;
    @BindView(R.id.password_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, PasswordActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_activity);
        ButterKnife.bind(this);

        loginUserId = Helpers.getLoginUserId();
        loginUser = User.getUserById(loginUserId);
        Drawable cameraIconHolder = new BitmapDrawable(getResources(), Helpers.getCameraIconBitmap(this));
        Helpers.loadProfilePhoto(profilePhotoImageView, loginUser.getPhotoUrl(), cameraIconHolder);

        setupToolbar();
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        saveTextView.setText(getString(R.string.save));
        saveTextView.setVisibility(View.VISIBLE);
        saveTextView.setOnClickListener(v -> save());
        titleTextView.setText(getString(R.string.password));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    private void save() {
        Helpers.closeSoftKeyboard(this);

        String email = loginUser.getEmail();
        String currentPassword = currentPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            errorMessageTextView.setText(getString(R.string.input_cannot_be_empty));
            errorMessageRelativeLayout.setVisibility(View.VISIBLE);
            clearPassword();
        } else if (currentPassword.length() < 6 || newPassword.length() < 6 || confirmPassword.length() < 6) {
            errorMessageTextView.setText(getString(R.string.input_too_short));
            errorMessageRelativeLayout.setVisibility(View.VISIBLE);
            clearPassword();
        } else if (!newPassword.equals(confirmPassword)) {
            errorMessageTextView.setText(getString(R.string.password_mismatch));
            errorMessageRelativeLayout.setVisibility(View.VISIBLE);
            clearPassword();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            SyncUser.login(email, currentPassword)
                .onSuccess(onLoginSuccess, Task.UI_THREAD_EXECUTOR);
        }
    }

    private Continuation<JSONObject, Void> onLoginSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in login. ", task.getError());
            }

            if (task.getResult().has("error")) {
                // Show error message
                errorMessageTextView.setText(getString(R.string.invalid_password));
                errorMessageRelativeLayout.setVisibility(View.VISIBLE);
                clearPassword();
            } else {
                String newPassword = newPasswordEditText.getText().toString();
                SyncUser.updatePassword(loginUserId, newPassword)
                    .onSuccess(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);
            }
            return null;
        }
    };

    private Continuation<JSONObject, Void> onUpdateSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in login. ", task.getError());
            }

            if (task.getResult().has("error")) {
                // Show error message
                errorMessageTextView.setText(task.getResult().getString("error"));
                errorMessageRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
            clearPassword();
            return null;
        }
    };

    private void clearPassword() {
        // Clear password
        currentPasswordEditText.setText("");
        newPasswordEditText.setText("");
        confirmPasswordEditText.setText("");
    }
}
