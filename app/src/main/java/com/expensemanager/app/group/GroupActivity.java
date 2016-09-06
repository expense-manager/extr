package com.expensemanager.app.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.service.SyncGroup;
import com.expensemanager.app.service.SyncMember;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;

public class GroupActivity extends AppCompatActivity {
    private static final String TAG = GroupActivity.class.getSimpleName();

    private ArrayList<Group> groups;
    private GroupAdapter groupAdapter;
    private String loginUserId;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.group_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.group_activity_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, GroupActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.stay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);
        ButterKnife.bind(this);

        setupToolbar();
        loginUserId = Helpers.getLoginUserId();

        groups = new ArrayList<>();
        groupAdapter = new GroupAdapter(this, groups);
        setupRecyclerView();

        fab.setOnClickListener(v -> {
            NewGroupActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        invalidateViews();
        SyncGroup.getGroupByUserId(loginUserId);
        SyncMember.getMembersByUserId(loginUserId);
        SyncMember.getMembersByGroupId("YDf9fuLGze");
    }

    private void invalidateViews() {
        groupAdapter.clear();
        groupAdapter.addAll(Group.getAllGroups());

        RealmList<Member> membersByUserId = Member.getAllMembersByUserId(loginUserId);
        Log.d(TAG, "members size by userId: " + membersByUserId.size());

        RealmList<Member> membersByGroupId = Member.getAllMembersByGroupId("YDf9fuLGze");
        Log.d(TAG, "members size by groupId: " + membersByGroupId.size());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(groupAdapter);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.group));
        titleTextView.setOnClickListener(v -> finish());
        backImageView.setOnClickListener(v -> finish());
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());

        invalidateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
