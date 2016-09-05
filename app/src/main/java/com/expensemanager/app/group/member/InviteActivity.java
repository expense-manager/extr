package com.expensemanager.app.group.member;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.Constant;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.SyncMember;
import com.expensemanager.app.service.SyncUser;
import com.expensemanager.app.service.email.Mail;
import com.expensemanager.app.service.email.MailSender;
import com.expensemanager.app.service.email.Recipient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;

public class InviteActivity extends AppCompatActivity {
    private static final String TAG = InviteActivity.class.getSimpleName();

    public static final String GROUP_ID = "groupId";
    private String groupId;
    private String loginUserId;
    private ArrayList<User> users;
    private InviteAdapter inviteAdapter;
    private String userFullName;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.invite_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.invite_activity_progress_bar_id) ProgressBar progressBar;

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

        setupToolbar();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        loginUserId = sharedPreferences.getString(User.USER_ID, null);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);

        users = new ArrayList<>();
        inviteAdapter = new InviteAdapter(this, users, loginUserId, groupId);
        setupRecyclerView();

        invalidateViews();
        // Sync all members of current group
        SyncMember.getMembersByGroupId(groupId);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(inviteAdapter);
    }

    private void invalidateViews() {
        inviteAdapter.setExistingMembers(Member.getAllMembersByGroupId(groupId));
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

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.invite_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Add hint
        searchView.setQueryHint("Search full name...");
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
                searchUserFullName(query.trim());
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

    public void searchUserFullName(final String userFullName) {
        this.userFullName = userFullName;
        inviteAdapter.clear();
        if (userFullName == null || userFullName.isEmpty()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        // Sync user by full name input
        SyncUser.getAllUsersByUserFullName(userFullName).continueWith(onQueryFullNameFinished, Task.UI_THREAD_EXECUTOR);
    }

    Continuation<JSONObject, Void> onQueryFullNameFinished = new Continuation<JSONObject, Void>() {
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
        // Sync all members of current group
        SyncMember.getMembersByGroupId(groupId);
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
