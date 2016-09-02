package com.expensemanager.app.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MemberActivity extends AppCompatActivity {
    private static final String TAG = GroupActivity.class.getSimpleName();

    public static final String GROUP_ID = "group_id";

    private ArrayList<User> users;
    private MemberAdapter memberAdapter;
    private String groupId;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id)
    ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.member_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.group_activity_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context, String id) {
        Intent intent = new Intent(context, MemberActivity.class);
        intent.putExtra(GROUP_ID, id);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.stay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);
        ButterKnife.bind(this);

        setupToolbar();

        groupId = getIntent().getStringExtra(GROUP_ID);

        users = new ArrayList<>();
        memberAdapter = new MemberAdapter(this, users);
        setupRecyclerView();

        fab.setOnClickListener(v -> {
            NewGroupActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        invalidateViews();
        //todo: sync all members
        //SyncGroup.getAllExpenses();
    }

    private void invalidateViews() {
        memberAdapter.clear();
        memberAdapter.addAll(User.getAllUsersByGroupId(groupId));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(memberAdapter);
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