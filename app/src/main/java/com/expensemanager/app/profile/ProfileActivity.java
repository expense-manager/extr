package com.expensemanager.app.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 8/22/16.
 */

public class ProfileActivity extends BaseActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private static final String USER_ID = "userId";

    private User currentUser;
    private byte[] profileImage;
    private boolean isEditable = false;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.profile_activity_profile_photo_image_view_id) ImageView profilePhotoImageView;
    @BindView(R.id.profile_activity_fullname_edit_text_id) EditText fullnameEditText;
    @BindView(R.id.profile_activity_cancel_button_id) Button cancelButton;
    @BindView(R.id.profile_activity_edit_button_id) Button editButton;
    @BindView(R.id.profile_activity_save_button_id) Button saveButton;
    @BindView(R.id.profile_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context, String userId) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);

        String userId = getIntent().getStringExtra(USER_ID);

        if (userId == null || userId.isEmpty()) {
            userId = loginUserId;
        }

        currentUser = User.getUserById(userId);

        if (currentUser != null) {
            Log.d(TAG, "current user name: " + currentUser.getFullname());
        }

        setupToolbar();

        invalidateViews();

        SyncUser.getLoginUser().continueWith(onResponseReturned, Task.UI_THREAD_EXECUTOR);
    }

    private void invalidateViews() {
        if (currentUser == null) {
            return;
        }

        editButton.setOnClickListener(v -> setEditMode(true));
        cancelButton.setOnClickListener(v -> setEditMode(false));
        saveButton.setOnClickListener(v -> save());

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        cancelButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);

        Glide.with(this)
                .load(currentUser.getPhotoUrl())
                .fitCenter()
                .into(profilePhotoImageView);

        fullnameEditText.setText(currentUser.getFullname());

        setupEditableViews(isEditable);

        profilePhotoImageView.setOnClickListener(v -> {
            updateProfileImage();
        });
    }

    private void updateProfileImage() {
        if (isEditable) {
            // todo: allow take photo or selet image
        }
    }

    private void setupEditableViews(boolean isEditable) {
        fullnameEditText.setFocusable(isEditable);
        fullnameEditText.setFocusableInTouchMode(isEditable);
        fullnameEditText.setClickable(isEditable);

        if (isEditable) {
            fullnameEditText.requestFocus();
            fullnameEditText.setSelection(fullnameEditText.length());
        }
    }

    private void setEditMode(boolean isEditable) {
        this.isEditable = isEditable;
        invalidateViews();
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        titleTextView.setText(getString(R.string.profile));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }

        return super.onOptionsItemSelected(item);
    }

    private void save() {
        String name = fullnameEditText.getText().toString();

        // Check name input
        if (name == null || name.length() == 0) {
            Toast.makeText(this, "Invalid name.", Toast.LENGTH_SHORT).show();
            return;
        }
/*
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        currentUser.setFullname(name);
        realm.copyToRealmOrUpdate(currentUser);
        realm.commitTransaction();
        realm.close();

        ProfileBuilder profileBuilder = new ProfileBuilder()
            .setUser(currentUser)
            .setProfileImage(profileImage);
        // todo:update user with profile image
        //SyncUser.update(profileBuilder).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);
*/
        progressBar.setVisibility(View.VISIBLE);
        isEditable = !isEditable;
        invalidateViews();
    }

    private Continuation<JSONObject, Void> onUpdateSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in updating expense.", task.getError());
            }

            Log.d(TAG, "Update current user success.");

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            // todo: update profile image url
            //currentUser.setPhotoUrl();
            realm.copyToRealmOrUpdate(currentUser);
            realm.commitTransaction();
            realm.close();

            return null;
        }
    };

    private Continuation<Void, Void> onResponseReturned = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, task.getError().toString());
                return null;
            }

            invalidateViews();
            return null;
        }
    };
}
