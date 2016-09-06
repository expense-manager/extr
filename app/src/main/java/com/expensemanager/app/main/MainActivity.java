package com.expensemanager.app.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.expensemanager.app.BuildConfig;
import com.expensemanager.app.R;
import com.expensemanager.app.category.CategoryActivity;
import com.expensemanager.app.expense.ExpenseActivity;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.group.GroupDetailActivity;
import com.expensemanager.app.group.NewGroupActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.DrawerItem;
import com.expensemanager.app.models.DrawerSubItem;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.RNotification;
import com.expensemanager.app.models.User;
import com.expensemanager.app.notifications.NotificationsActivity;
import com.expensemanager.app.report.ReportActivity;
import com.expensemanager.app.service.PermissionsManager;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.SyncGroup;
import com.expensemanager.app.service.SyncMember;
import com.expensemanager.app.service.SyncUser;
import com.expensemanager.app.settings.SettingsActivity;
import com.expensemanager.app.welcome.WelcomeActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActionBarDrawerToggle drawerToggle;
    private DrawerAdapter drawerAdapter;
    private GroupDrawerAdapter groupDrawerAdapter;
    private ArrayList<DrawerItem> drawerItems;
    private ArrayList<DrawerSubItem> drawerSubItems;
    private ArrayList<Member> members;
    private String loginUserId;
    private String groupId;
    private long syncTimeInMillis;
    private String syncTimeKey;
    private OverviewFragment overviewFragment;

    @BindView(R.id.main_activity_drawer_layout_id) DrawerLayout drawerLayout;
    @BindView(R.id.main_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.main_activity_drawer_recycler_view_id) RecyclerView drawRecyclerView;
    @BindView(R.id.main_activity_fab_id) FloatingActionButton fab;
    @BindView(R.id.swipeContainer_id) SwipeRefreshLayout swipeContainer;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        loginUserId = sharedPreferences.getString(User.USER_ID, null);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);
        syncTimeKey = Helpers.getSyncTimeKey(TAG, groupId);
        syncTimeInMillis = sharedPreferences.getLong(syncTimeKey, 0);

        if (loginUserId == null || groupId == null) {
            Log.i(TAG, "Error getting login user id or group id.");
        }

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);

        User currentUser = User.getUserById(loginUserId);
        drawerItems = new ArrayList<>();
        drawerSubItems = new ArrayList<>();
        members = new ArrayList<>();
        drawerAdapter = new DrawerAdapter(this, drawerItems, drawerSubItems, currentUser);
        groupDrawerAdapter = new GroupDrawerAdapter(this, members, currentUser);
        setupDrawerListItems();
        setupGroupListItems();
        setupDrawerList();

        drawerToggle = setupDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);

        drawRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            if (groupId != null) {
                NewExpenseActivity.newInstance(this);
                overridePendingTransition(R.anim.right_in, R.anim.stay);
            } else {
                Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
            }
        });

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_frame_layout_id);

        if (fragment == null) {
            overviewFragment = OverviewFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame_layout_id, overviewFragment)
                    .addToBackStack(OverviewFragment.class.getName())
                    .commit();
        }

        //SettingsActivity.loadSetting(this);

        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncUser.getLoginUser().continueWith(onGetLoginUserFinished, Task.UI_THREAD_EXECUTOR);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(this, syncTimeKey, syncTimeInMillis);
        }

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncUser.getLoginUser().continueWith(onGetLoginUserFinished, Task.UI_THREAD_EXECUTOR);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);

        if (BuildConfig.DEBUG) {
            checkExternalStoragePermission();
        }
    }

    private void setupDrawerListItems() {
        drawerAdapter.add(new DrawerItem().setIcon(R.drawable.ic_credit_card).setTitle(getString(R.string.nav_expense)));
        drawerAdapter.add(new DrawerItem().setIcon(R.drawable.ic_trending_up).setTitle(getString(R.string.nav_report)));
        drawerAdapter.add(new DrawerItem().setIcon(R.drawable.ic_buffer).setTitle(getString(R.string.nav_category)));
        drawerAdapter.add(new DrawerItem().setIcon(R.drawable.ic_account_multiple).setTitle(getString(R.string.nav_group)));
        drawerAdapter.add(new DrawerItem().setIcon(R.drawable.ic_bell).setTitle(getString(R.string.nav_notifications)));
        drawerAdapter.add(new DrawerItem().setIcon(R.drawable.ic_help_circle).setTitle(getString(R.string.nav_help)));
        drawerAdapter.add(new DrawerItem().setIcon(R.drawable.ic_settings).setTitle(getString(R.string.nav_settings)));

        DrawerSubItem drawerSubItem = new DrawerSubItem().setTitle(getString(R.string.sign_out));
        drawerAdapter.add(drawerSubItem);
        drawerSubItem = new DrawerSubItem().setTitle(getString(R.string.nav_about));
        drawerAdapter.add(drawerSubItem);
    }

    private void setupGroupListItems() {
        groupDrawerAdapter.clear();
        List<Member> newMembers = Member.getAllMembersByUserId(loginUserId);

        // Not accepted -> accepted, group name A -> Z
        Collections.sort(newMembers, new Comparator<Member>(){
            @Override
            public int compare(Member m1, Member m2) {
                if (m1.isAccepted() != m2.isAccepted()) {
                    return m1.isAccepted() ? 1 : -1;
                }
                return m1.getGroup().getName().compareTo(m2.getGroup().getName());
            }
        });

        if (groupId == null && newMembers.size() > 0) {
            int index = getFirstAcceptedGroup(newMembers, 0, newMembers.size() - 1);
            if (index != -1) {
                groupId = newMembers.get(index).getGroupId();
            }
            saveGroupId();
        }

        groupDrawerAdapter.addAll(newMembers);
    }

    private int getFirstAcceptedGroup(List<Member> members, int left, int right) {
        int mid = 0;
        while (left + 1 < right) {
            mid = left + (right - left) / 2;
            if (members.get(mid).isAccepted()) {
                right = mid;
            } else {
                left = mid;
            }
        }
        if (members.get(left).isAccepted()) {
            return left;
        } else if (members.get(right).isAccepted()) {
            return right;
        }
        Log.i(TAG, "No accepted group found.");
        return -1;
    }

    private void setupDrawerList() {
        drawRecyclerView.setAdapter(drawerAdapter);

        drawerAdapter.setOnItemClickLister(new DrawerAdapter.OnItemSelecteListener() {
            @Override
            public void onItemSelected(View v, int position) {
                switch(position) {
                    case 0:
                        setupGroupList();
                        break;
                    case 1:
                        if (groupId != null) {
                            ExpenseActivity.newInstance(MainActivity.this);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        if (groupId != null) {
                            ReportActivity.newInstance(MainActivity.this);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if (groupId != null) {
                            CategoryActivity.newInstance(MainActivity.this);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4:
                        if (groupId != null) {
                            GroupDetailActivity.newInstance(MainActivity.this, groupId);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                        }
                        // todo: remove group list activity
                        break;
                    case 5:
                        NotificationsActivity.newInstance(MainActivity.this);
                        break;
                    case 6:
                        // help
                        break;
                    case 7:
                        SettingsActivity.newInstance(MainActivity.this);
                        break;
                    case 9:
                        signOut();
                        break;
                    case 10:
                        // About
                        break;
                    default:
                        break;
                }
                if (position != 0) {
                    drawerLayout.closeDrawers();
                }
            }
        });
    }

    private void setupGroupList() {
        setupGroupListItems();
        drawRecyclerView.setAdapter(groupDrawerAdapter);

        groupDrawerAdapter.setOnItemClickLister(new GroupDrawerAdapter.OnItemSelecteListener() {
            @Override
            public void onItemSelected(View v, int position) {
                if (position == 0) {
                    setupDrawerList();
                } else if (position <= members.size() + 1) {
                    Member member = members.get(position - 2);
                    if (!member.isAccepted()) {
                        return;
                    }
                    groupId = member.getGroupId();
                    drawerLayout.closeDrawers();
                    saveGroupId();

                    // Check to sync new selected group
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
                    syncTimeKey = Helpers.getSyncTimeKey(MainActivity.TAG, groupId);
                    syncTimeInMillis = sharedPreferences.getLong(syncTimeKey, 0);
                    if (Helpers.needToSync(syncTimeInMillis)) {
                        SyncGroup.getGroupById(groupId).continueWith(onGetGroupsFinished, Task.UI_THREAD_EXECUTOR);
                        syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
                        Helpers.saveSyncTime(MainActivity.this, syncTimeKey, syncTimeInMillis);
                    }
                    overviewFragment.invalidateViews();
                } else if (position == members.size() + 2) {
                    NewGroupActivity.newInstance(MainActivity.this);
                    drawerLayout.closeDrawers();
                }
            }
        });
    }

    private void saveGroupId() {
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0).edit();
        sharedPreferencesEditor.putString(Group.ID_KEY, groupId);
        sharedPreferencesEditor.apply();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Reset to drawer menu list at close
                setupDrawerList();
            }
        };
    }

    private Continuation<Void, Void> onGetLoginUserFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            User currentUser = User.getUserById(loginUserId);
            if (currentUser != null) {
                // Sync all members after getting current user
                SyncMember.getMembersByUserId(loginUserId).continueWith(onGetGroupsFinished, Task.UI_THREAD_EXECUTOR);

                drawerAdapter.loadUser(currentUser);
                groupDrawerAdapter.loadUser(currentUser);
            }

            return null;
        }
    };

    private Continuation<Void, Void> onGetGroupsFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            if (swipeContainer != null) {
                swipeContainer.setRefreshing(false);
            }

            if (groupId != null) {
                // Sync all categories of current group
                SyncCategory.getAllCategoriesByGroupId(groupId);
                // Sync all expenses of current group
                SyncExpense.getAllExpensesByGroupId(groupId);
                // Sync all members of current group
                SyncMember.getMembersByGroupId(groupId);
            }

            return null;
        }
    };

    private void signOut() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.sign_out_message)
            .setPositiveButton(R.string.sign_out, (DialogInterface dialog, int which) -> SyncUser.logout().continueWith(logoutOnSuccess))
            .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
            .show();
    }

    private Continuation<Void, Void> logoutOnSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, task.getError().toString());
                return null;
            }

            SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
            sharedPreferences.edit().clear().apply();
            // Clear data when signout
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
            realm.close();
            // Go to welcome
            WelcomeActivity.newInstance(MainActivity.this);
            finish();
            return null;
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
        drawerToggle.onConfigurationChanged(newConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> setupGroupListItems());

        setupGroupListItems();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void checkExternalStoragePermission() {
        PermissionsManager.verifyExternalStoragePermissionGranted(this, (boolean isGranted) -> {
            if (isGranted) {
                // Nothing to do
            } else {
                Log.d(TAG, "Permission is not granted.");
            }
        });
    }
}
