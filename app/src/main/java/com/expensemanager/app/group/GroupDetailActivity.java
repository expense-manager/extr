package com.expensemanager.app.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.group.member.MemberActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncGroup;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class GroupDetailActivity extends BaseActivity {
    private static final String TAG = GroupDetailActivity.class.getSimpleName();

    private static final String GROUP_ID = "group_id";

    private Group group;
    private User createdBy;
    private String loginUserId;
    private boolean isEditable = false;
    private String groupId;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.group_detail_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.group_detail_activity_group_edit_text_id) EditText groupEditText;
    @BindView(R.id.group_detail_activity_about_edit_text_id) EditText aboutEditText;
    @BindView(R.id.group_detail_activity_delete_button_id) Button deleteButton;
    @BindView(R.id.group_detail_activity_member_relative_layout_id) RelativeLayout memberRelativeLayout;
    @BindView(R.id.group_detail_activity_created_by_photo_image_view_id) CircleImageView createdByPhotoImageView;
    @BindView(R.id.group_detail_activity_created_by_name_text_view_id) TextView createdByNameTextView;
    @BindView(R.id.group_detail_activity_created_by_email_text_view_id) TextView createdByEmailTextView;
    @BindView(R.id.group_detail_activity_created_at_text_view_id) TextView createdAtTextView;
    @BindView(R.id.group_detail_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context, String id) {
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra(GROUP_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_detail_activity);
        ButterKnife.bind(this);

        setupToolbar();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        loginUserId = sharedPreferences.getString(User.USER_ID, null);

        groupId = getIntent().getStringExtra(GROUP_ID);

        memberRelativeLayout.setOnClickListener(v -> {
            MemberActivity.newInstance(this, groupId);
        });

        invalidateViews();
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.title_activity_group_detail));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
        editTextView.setOnClickListener(v -> setEditMode(true));
        saveTextView.setOnClickListener(v -> save());
    }

    private void invalidateViews() {
        group = Group.getGroupById(groupId);
        if (group == null) {
            return;
        }
        group.print();
        nameEditText.setText(group.getName());
        groupEditText.setText(group.getGroupname());
        aboutEditText.setText(group.getAbout());
        createdAtTextView.setText(Helpers.formatCreateAt(group.getCreatedAt()));

        createdBy = User.getUserById(group.getUserId());
        if (createdBy != null) {
            Glide.with(this)
                .load(createdBy.getPhotoUrl())
                .placeholder(R.drawable.profile_place_holder_image)
                .dontAnimate()
                .into(createdByPhotoImageView);

            createdByNameTextView.setText(createdBy.getFullname());
            createdByEmailTextView.setText(createdBy.getEmail());
        }

        if (group.getUserId().equals(loginUserId)) {
            editTextView.setVisibility(isEditable ? View.GONE : View.VISIBLE);
            saveTextView.setVisibility(isEditable ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
            deleteButton.setOnClickListener(v -> delete());
        }

        setupEditableViews(isEditable);
    }

    private void setupEditableViews(boolean isEditable) {
        nameEditText.setFocusable(isEditable);
        nameEditText.setFocusableInTouchMode(isEditable);
        nameEditText.setClickable(isEditable);

        groupEditText.setFocusable(isEditable);
        groupEditText.setFocusableInTouchMode(isEditable);
        groupEditText.setClickable(isEditable);

        aboutEditText.setFocusable(isEditable);
        aboutEditText.setFocusableInTouchMode(isEditable);
        aboutEditText.setClickable(isEditable);

        if (isEditable) {
            nameEditText.requestFocus();
            nameEditText.setSelection(nameEditText.length());
        }
    }

    private void setEditMode(boolean isEditable) {
        this.isEditable = isEditable;
        invalidateViews();
    }

    private void save() {
        String name = nameEditText.getText().toString();
        String groupName = groupEditText.getText().toString();
        String about = aboutEditText.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(this, "Group cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        group.setName(name);
        group.setGroupname(groupName);
        group.setAbout(about);
        realm.copyToRealmOrUpdate(group);
        realm.commitTransaction();
        realm.close();

        progressBar.setVisibility(View.VISIBLE);
        SyncGroup.update(group).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);

        closeSoftKeyboard();
        isEditable = false;
        invalidateViews();
    }

    private Continuation<Void, Void> onUpdateSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, "onUpdateSuccess");

            if (task.isFaulted()) {
                Log.e(TAG, "Error in updating group.", task.getError());
            }

            Log.d(TAG, "Update group success.");

            return null;
        }
    };

    private void delete() {
        progressBar.setVisibility(View.VISIBLE);
        SyncGroup.delete(groupId).continueWith(onDeleteSuccess, Task.UI_THREAD_EXECUTOR);

    }

    private Continuation<Void, Void> onDeleteSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in deleting expense.", task.getError());
            }

            Group.delete(groupId);
            Log.d(TAG, "Delete group success.");
            close();
            return null;
        }
    };

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