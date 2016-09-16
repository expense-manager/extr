package com.expensemanager.app.group;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.group.member.MemberActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.service.SyncGroup;
import com.expensemanager.app.service.font.Font;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 9/15/16.
 */

public class GroupFragment extends Fragment {
    private static final String TAG = GroupFragment.class.getSimpleName();

    private static final String GROUP_ID = "group_id";

    private Group group;
    private User createdBy;
    private String loginUserId;
    private boolean isEditable = false;
    private String groupId;

    private Toolbar toolbar;
    private TextView titleTextView;
    private TextView saveTextView;

    @BindView(R.id.group_detail_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.group_detail_activity_group_edit_text_id) EditText groupEditText;
    @BindView(R.id.group_detail_activity_about_edit_text_id) EditText aboutEditText;
    @BindView(R.id.group_detail_activity_delete_button_id) Button deleteButton;
    @BindView(R.id.group_detail_activity_member_relative_layout_id) RelativeLayout memberRelativeLayout;
    @BindView(R.id.group_detail_activity_created_by_photo_image_view_id) ImageView createdByPhotoImageView;
    @BindView(R.id.group_detail_activity_created_by_name_text_view_id) TextView createdByNameTextView;
    @BindView(R.id.group_detail_activity_created_by_email_text_view_id) TextView createdByEmailTextView;
    @BindView(R.id.group_detail_activity_created_at_text_view_id) TextView createdAtTextView;
    @BindView(R.id.group_detail_activity_progress_bar_id) ProgressBar progressBar;
    @BindView(R.id.group_detail_activity_created_by_relative_layout_id) RelativeLayout createdByRelativeLayout;
    @BindView(R.id.group_detail_activity_created_by_label_text_view_id) TextView createdByLabel;

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupId = Helpers.getCurrentGroupId();
        loginUserId = Helpers.getLoginUserId();

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_detail_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        memberRelativeLayout.setOnClickListener(v -> {
            MemberActivity.newInstance(getActivity(), groupId);
        });

        setupToolbar();
        invalidateViews();
    }

    public void invalidateViews() {
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
        if (createdBy != null && Member.getAllAcceptedMembersByGroupId(groupId).size() > 1) {
            Helpers.loadIconPhoto(createdByPhotoImageView, createdBy.getPhotoUrl());
            createdByNameTextView.setText(createdBy.getFullname());
            createdByEmailTextView.setText(createdBy.getEmail());
            createdByRelativeLayout.setOnClickListener(v -> ProfileActivity.newInstance(getActivity(), createdBy.getId()));
        } else {
            createdByRelativeLayout.setVisibility(View.GONE);
            createdByLabel.setVisibility(View.GONE);
        }

        if (group.getUserId().equals(loginUserId)) {
            saveTextView.setText(getString(R.string.save));
            saveTextView.setVisibility(isEditable ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
            deleteButton.setOnClickListener(v -> delete());
        } else {
            saveTextView.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
        }

        setupEditableViews(isEditable);
    }
    private void setupToolbar() {
        this.toolbar = (Toolbar) getActivity().findViewById(R.id.main_activity_toolbar_id);
        this.titleTextView = (TextView) toolbar.findViewById(R.id.main_activity_toolbar_title_text_view_id);
        this.titleTextView.setText(getString(R.string.group));
        this.saveTextView = (TextView) toolbar.findViewById(R.id.main_activity_toolbar_right_title_text_view_id);

        titleTextView.setTypeface(EApplication.getInstance().getTypeface(Font.REGULAR));
        saveTextView.setOnClickListener(v -> save());
        saveTextView.setTypeface(EApplication.getInstance().getTypeface(Font.REGULAR));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (group.getUserId().equals(loginUserId)) {
            inflater.inflate(R.menu.group_menu, menu);
        }

        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_group_edit:
                setEditMode(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            Toast.makeText(getActivity(), "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(getActivity(), "Group cannot be empty.", Toast.LENGTH_SHORT).show();
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

        Helpers.closeSoftKeyboard(getActivity());
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

            // todo: switch to another group
            Group.delete(groupId);
            Log.d(TAG, "Delete group success.");
            return null;
        }
    };
}