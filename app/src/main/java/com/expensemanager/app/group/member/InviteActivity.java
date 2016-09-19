package com.expensemanager.app.group.member;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncMember;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;

public class InviteActivity extends BaseActivity {
    private static final String TAG = InviteActivity.class.getSimpleName();

    public static final String GROUP_ID = "groupId";
    private String groupId;
    private long syncTimeInMillis;
    private String syncTimeKey;
    private String loginUserId;
    private ArrayList<User> users;
    private InviteAdapter inviteAdapter;
    private String query;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.invite_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.invite_activity_progress_bar_id) ProgressBar progressBar;
    @BindView(R.id.swipeContainer_id) SwipeRefreshLayout swipeContainer;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, InviteActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.stay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_activity);
        ButterKnife.bind(this);

        loginUserId = Helpers.getLoginUserId();
        groupId = Helpers.getCurrentGroupId();
        syncTimeKey = Helpers.getSyncTimeKey(TAG, groupId);
        syncTimeInMillis = Helpers.getSyncTimeInMillis(syncTimeKey);

        users = new ArrayList<>();
        inviteAdapter = new InviteAdapter(this, users, loginUserId, groupId);

        setupToolbar();
        setupRecyclerView();
        setupSwipeToRefresh();
    }

    private void invalidateViews() {
        inviteAdapter.setExistingMembers(Member.getAllMembersByGroupId(groupId));

        // Sync all members of current group
        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncMember.getMembersByGroupId(groupId);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(this, syncTimeKey, syncTimeInMillis);
        }
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.invite_new));

        titleTextView.setOnClickListener(v -> finish());
        backImageView.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(inviteAdapter);
    }

    private void setupSwipeToRefresh() {
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncMember.getMembersByGroupId(groupId).continueWith(onGetMemberFinished, Task.UI_THREAD_EXECUTOR);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    private Continuation<Void, Void> onGetMemberFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            if (swipeContainer != null) {
                swipeContainer.setRefreshing(false);
            }

            return null;
        }
    };

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.invite_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Add hint
        searchView.setQueryHint(getString(R.string.search_hint));
        // Expande search view
        searchItem.expandActionView();
        // add actionbar search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Hide input soft keyboard
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                // perform query here
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                query = query.trim();
                searchUserQuery(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void searchUserQuery(String query) {
        this.query = query;
        inviteAdapter.clear();

        if (query.contains(" ")) {
            searchUserFullName(query);
        } else if (query.contains("@")) {
            searchUserEmail(query);
        } else {
            searchUserPhoneNumber(query);
        }
    }

    private void searchUserFullName(final String userFullName) {
        if (userFullName == null || userFullName.isEmpty()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        // Sync user by full name input
        SyncUser.getAllUsersByUserFullName(userFullName).continueWith(onQueryUserFinished, Task.UI_THREAD_EXECUTOR);
    }

    private void searchUserEmail(final String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        // Sync user by full name input
        SyncUser.getAllUsersByUserEmail(userEmail).continueWith(onQueryUserFinished, Task.UI_THREAD_EXECUTOR);
    }

    private void searchUserPhoneNumber(final String userPhoneNumber) {
        if (userPhoneNumber == null || userPhoneNumber.isEmpty()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        // Sync user by full name input
        SyncUser.getAllUsersByUserPhoneNumber(userPhoneNumber).continueWith(onQueryUserFinished, Task.UI_THREAD_EXECUTOR);
    }

    private Continuation<JSONObject, Void> onQueryUserFinished = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            if (task.isFaulted()) {
                Exception exception = task.getError();
                Log.e(TAG, "Error in downloading all users.", exception);
                throw  exception;
            }

            JSONObject result = task.getResult();
            if (result == null) {
                throw new Exception("Empty response.");
            }

            Log.d(TAG, "Users: \n" + result);

            RealmList<User> users = null;
            try {
                JSONArray userArray = result.getJSONArray("results");
                users = User.mapFromJSONArrayWithoutSaving(userArray);
            } catch (JSONException e) {
                Log.e(TAG, "Error in getting user JSONArray.", e);
            }

            progressBar.setVisibility(View.GONE);
            inviteAdapter.addAll(users);

            return null;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // todo: send invitation email option
        return super.onOptionsItemSelected(item);
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
