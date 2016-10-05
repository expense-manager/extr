package com.expensemanager.app.group;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.group.member.InviteActivity;
import com.expensemanager.app.group.member.MemberActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 9/15/16.
 */

public class GroupFragment extends Fragment {
    private static final String TAG = GroupFragment.class.getSimpleName();

    private static final String GROUP_ID = "group_id";

    private Group group;
    private User createdBy;
    private String loginUserId;
    private String groupId;

    private Toolbar toolbar;
    private TextView titleTextView;

    @BindView(R.id.group_fragment_name_edit_text_id) TextView nameEditText;
    @BindView(R.id.group_fragment_group_edit_text_id) TextView groupEditText;
    @BindView(R.id.group_fragment_about_edit_text_id) TextView aboutEditText;
    @BindView(R.id.group_fragment_created_by_photo_image_view_id) ImageView createdByPhotoImageView;
    @BindView(R.id.group_fragment_created_at_text_view_id) TextView createdAtTextView;
    @BindView(R.id.group_fragment_invite_relative_layout_id) RelativeLayout inviteRelativeLayout;
    @BindView(R.id.group_fragment_member_relative_layout_id) RelativeLayout memberRelativeLayout;
    @BindView(R.id.group_fragment_edit_relative_layout_id) RelativeLayout editRelativeLayout;
    @BindView(R.id.group_fragment_leave_relative_layout_id) RelativeLayout leaveRelativeLayout;
    @BindView(R.id.group_fragment_member_number_text_view_id) TextView memberNumberTextView;
    @BindView(R.id.group_fragment_progress_bar_id) ProgressBar progressBar;

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
        return inflater.inflate(R.layout.group_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        setupToolbar();
        invalidateViews();
        setupActions();
    }

    public void invalidateViews() {
        group = Group.getGroupById(groupId);

        if (group == null) {
            return;
        }

        group.print();
        nameEditText.setText(group.getName());
        groupEditText.setText("@" + group.getGroupname());
        aboutEditText.setText(group.getAbout());

        createdBy = User.getUserById(group.getUserId());
        if (createdBy != null) {
            Helpers.loadIconPhoto(createdByPhotoImageView, createdBy.getPhotoUrl());
            createdByPhotoImageView.setOnClickListener(v -> ProfileActivity.newInstance(getActivity(), createdBy.getId()));
            createdAtTextView.setText("" + createdBy.getFullname() + " created this group on " + Helpers.getMonthDayYear(group.getCreatedAt()));
        }

        memberNumberTextView.setText(String.valueOf(Member.getAllAcceptedMembersByGroupId(groupId).size()));
        inviteRelativeLayout.setVisibility(group.getUserId().equals(loginUserId)? View.VISIBLE : View.GONE);
        editRelativeLayout.setVisibility(group.getUserId().equals(loginUserId)? View.VISIBLE : View.GONE);
    }

    private void setupToolbar() {
        this.toolbar = (Toolbar) getActivity().findViewById(R.id.main_activity_toolbar_id);
        ViewCompat.setElevation(toolbar, getResources().getInteger(R.integer.toolbar_elevation));
        this.titleTextView = (TextView) toolbar.findViewById(R.id.main_activity_toolbar_title_text_view_id);
        this.titleTextView.setText(getString(R.string.group));
    }

    private void setupActions() {
        memberRelativeLayout.setOnClickListener(v -> {
            MemberActivity.newInstance(getActivity(), groupId);
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        inviteRelativeLayout.setOnClickListener(v -> {
            InviteActivity.newInstance(getActivity());
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        editRelativeLayout.setOnClickListener(v -> {
            GroupDetailActivity.newInstance(getActivity(), groupId);
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        leaveRelativeLayout.setOnClickListener(v -> {
            Log.d(TAG, "Leave onClicked");
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        if (group.getUserId().equals(loginUserId)) {
//            inflater.inflate(R.menu.group_menu, menu);
//        }

        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_group_edit:
                GroupDetailActivity.newInstance(getActivity(), groupId);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.stay);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();;
    }
}