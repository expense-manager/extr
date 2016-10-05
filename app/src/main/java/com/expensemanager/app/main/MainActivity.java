package com.expensemanager.app.main;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.ExpenseFragment;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.group.GroupFragment;
import com.expensemanager.app.group.NewGroupActivity;
import com.expensemanager.app.help.HelpActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.DrawerItem;
import com.expensemanager.app.models.DrawerSubItem;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.RNotification;
import com.expensemanager.app.models.User;
import com.expensemanager.app.notifications.AlarmReceiver;
import com.expensemanager.app.notifications.NotificationFragment;
import com.expensemanager.app.overview.OverviewMainFragment;
import com.expensemanager.app.report.main.ReportMainFragment;
import com.expensemanager.app.service.Constant;
import com.expensemanager.app.service.PermissionsManager;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.SyncGroup;
import com.expensemanager.app.service.SyncMember;
import com.expensemanager.app.service.SyncUser;
import com.expensemanager.app.settings.SettingsFragment;
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

    public static String NOTIFICATION_KEY = "notificationKey";
    public static final String IS_FIRST_TIME = "is_first_time";
    private static final int OVERVIEW_POSITION = 1;
    private static final int EXPENSE_POSITION = 2;
    private static final int REPORT_POSITION = 3;
    private static final int GROUP_POSITION = 4;
    private static final int NOTIFICATION_POSITION = 5;
    private static final int SETTINGS_POSITION = 6;
    private static final int SIGN_OUT_POSITION = 8;

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
    private int currentPosition = 1;

    private User currentUser;
    private Runnable pendingRunnable;
    private Handler handler;
    private BroadcastReceiver broadcastReceiver;
    private boolean isReceiverRegistered;
    private boolean isSignOut = false;
    private boolean isNotification = false;
    private boolean isFirstTime;

    @BindView(R.id.main_activity_drawer_layout_id) DrawerLayout drawerLayout;
    @BindView(R.id.main_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.main_activity_toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.main_activity_drawer_recycler_view_id) RecyclerView drawRecyclerView;
    @BindView(R.id.main_activity_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        isNotification = getIntent().getBooleanExtra(NOTIFICATION_KEY, false);
        isFirstTime = getIntent().getBooleanExtra(IS_FIRST_TIME, false);

        // Show help guide for first time login after sign up
        if (isFirstTime) {
            HelpActivity.newInstance(this);
        }

        loginUserId = Helpers.getLoginUserId();
        groupId = Helpers.getCurrentGroupId();
        syncTimeKey = Helpers.getSyncTimeKey(TAG, groupId);
        syncTimeInMillis = Helpers.getSyncTimeInMillis(syncTimeKey);

        setupToolbar();

        currentUser = User.getUserById(loginUserId);
        handler = new Handler();
        drawerItems = new ArrayList<>();
        drawerSubItems = new ArrayList<>();
        members = new ArrayList<>();
        setupDrawerListItems(); // Get drawer menus and sub menus

        drawerAdapter = new DrawerAdapter(this, drawerItems, drawerSubItems, currentUser);
        groupDrawerAdapter = new GroupDrawerAdapter(this, members, currentUser);

        drawerToggle = setupDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);
        drawRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        drawRecyclerView.setFitsSystemWindows(true);

//        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.cyan_deep));

        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_activity_frame_layout_id);

        if (fragment == null && !isNotification) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame_layout_id, OverviewMainFragment.newInstance())
                    .addToBackStack(OverviewMainFragment.class.getName())
            .commit();
        } else if (isNotification) {
            currentPosition = NOTIFICATION_POSITION;
            removeAllBackStackFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame_layout_id, NotificationFragment.newInstance())
                    .addToBackStack(OverviewMainFragment.class.getName())
                    .commit();
        }

        fab.setOnClickListener(v -> setupFab());

        isReceiverRegistered = false;
        broadcastReceiver = new AlarmReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                invalidateViews();
            }
        };

        setupDrawerList();

        checkIfNeedToSyncGroup();

//        // Enable storage permission for LeakCanary
//        if (BuildConfig.DEBUG) {
//            checkExternalStoragePermission();
//        }
    }

    public void invalidateViews() {
        drawerAdapter.invalidate();

        setupGroupListItems(groupDrawerAdapter);

        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncUser.getLoginUser().continueWith(onGetLoginUserFinished, Task.UI_THREAD_EXECUTOR);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(this, syncTimeKey, syncTimeInMillis);
        }

        checkIfNeedToSyncGroup();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        titleTextView.setText(R.string.app_name);
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

    private void setupDrawerList() {
        drawRecyclerView.setAdapter(drawerAdapter);

        drawerAdapter.setOnItemClickLister(new DrawerAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(View v, int position) {
                pendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        selectItem(position);
                    }
                };

                if (position != 0) {
                    drawerLayout.closeDrawers();
                }

                if (pendingRunnable != null) {
                    handler.postDelayed(pendingRunnable, 50);
                    pendingRunnable = null;
                }
            }
        });
    }

    private void selectItem(int position) {
        switch(position) {
            case 0:
                setupGroupList();
                break;
            case OVERVIEW_POSITION:
                if (currentPosition == OVERVIEW_POSITION) {
                    if (drawerLayout.isDrawerOpen(drawRecyclerView)) {
                        drawerLayout.closeDrawers();
                    }

                    break;
                } else {
                    currentPosition = position;
                }

                removeAllBackStackFragment();
                getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.right_in, R.animator.left_out, 0, R.animator.left_out)
                    .replace(R.id.main_activity_frame_layout_id, OverviewMainFragment.newInstance())
                    .addToBackStack(OverviewMainFragment.class.getName())
                    .commit();
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(va -> setupFab());

                break;
            case EXPENSE_POSITION:
                if (!Helpers.hasGroup()) {
                    break;
                }

                if (currentPosition == EXPENSE_POSITION) {
                    if (drawerLayout.isDrawerOpen(drawRecyclerView)) {
                        drawerLayout.closeDrawers();
                    }

                    break;
                } else {
                    currentPosition = position;
                }

                removeAllBackStackFragment();
                getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.right_in, R.animator.left_out, 0, R.animator.left_out)
                    .replace(R.id.main_activity_frame_layout_id, ExpenseFragment.newInstance())
                    .addToBackStack(ExpenseFragment.class.getName())
                    .commit();
                titleTextView.setText(getString(R.string.expense));
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(v -> setupFab());
                break;
            case REPORT_POSITION:
                if (!Helpers.hasGroup()) {
                    break;
                }

                if (currentPosition == REPORT_POSITION) {
                    if (drawerLayout.isDrawerOpen(drawRecyclerView)) {
                        drawerLayout.closeDrawers();
                    }

                    break;
                } else {
                    currentPosition = position;
                }

                removeAllBackStackFragment();
                getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.right_in, R.animator.left_out, 0, R.animator.left_out)
                    .replace(R.id.main_activity_frame_layout_id, ReportMainFragment.newInstance())
                    .addToBackStack(ReportMainFragment.class.getName())
                    .commit();
                titleTextView.setText(getString(R.string.report));
                fab.setVisibility(View.INVISIBLE);
                break;
            case GROUP_POSITION:
                if (!Helpers.hasGroup()) {
                    break;
                }

                if (currentPosition == GROUP_POSITION) {
                    if (drawerLayout.isDrawerOpen(drawRecyclerView)) {
                        drawerLayout.closeDrawers();
                    }

                    break;
                } else {
                    currentPosition = position;
                }

                removeAllBackStackFragment();
                getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.right_in, R.animator.left_out, 0, R.animator.left_out)
                    .replace(R.id.main_activity_frame_layout_id, GroupFragment.newInstance())
                    .addToBackStack(ReportMainFragment.class.getName())
                    .commit();
                titleTextView.setText(getString(R.string.group));
                fab.setVisibility(View.INVISIBLE);
                break;
            case NOTIFICATION_POSITION:
                if (currentPosition == NOTIFICATION_POSITION) {
                    if (drawerLayout.isDrawerOpen(drawRecyclerView)) {
                        drawerLayout.closeDrawers();
                    }

                    break;
                } else {
                    currentPosition = position;
                }

                removeAllBackStackFragment();

                getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.right_in, R.animator.left_out, 0, R.animator.left_out)
                    .replace(R.id.main_activity_frame_layout_id, NotificationFragment.newInstance())
                    .addToBackStack(NotificationFragment.class.getName())
                    .commit();
                titleTextView.setText(getString(R.string.notification));
                fab.setVisibility(View.INVISIBLE);
                break;
            case SETTINGS_POSITION:
                if (currentPosition == SETTINGS_POSITION) {
                    if (drawerLayout.isDrawerOpen(drawRecyclerView)) {
                        drawerLayout.closeDrawers();
                    }

                    break;
                } else {
                    currentPosition = position;
                }

                removeAllBackStackFragment();
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.right_in, R.animator.left_out, 0, R.animator.left_out)
                        .replace(R.id.main_activity_frame_layout_id, SettingsFragment.newInstance())
                        .addToBackStack(NotificationFragment.class.getName())
                        .commit();
                titleTextView.setText(getString(R.string.settings));
                fab.setVisibility(View.INVISIBLE);
                break;
            case SIGN_OUT_POSITION:
                signOut();
                break;
            case 9:
                // About
                break;
            default:
                break;
        }
    }

    private void removeAllBackStackFragment() {
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void setupFab() {
        if (!Helpers.hasGroup()) {
            return;
        }

        NewExpenseActivity.newInstance(this);
        overridePendingTransition(R.anim.right_in, R.anim.stay);
    }

    private void setupDrawerListItems() {
        drawerItems.clear();
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_home).setTitle(getString(R.string.nav_overview)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_credit_card).setTitle(getString(R.string.nav_expense)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_trending_up).setTitle(getString(R.string.nav_report)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_account_multiple).setTitle(getString(R.string.nav_group)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_bell).setTitle(getString(R.string.nav_notifications)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_settings).setTitle(getString(R.string.nav_settings)));

        drawerSubItems.clear();
        drawerSubItems.add(new DrawerSubItem().setTitle(getString(R.string.sign_out)));
    //    drawerSubItems.add(new DrawerSubItem().setTitle(getString(R.string.nav_about)));
    }

    private void setupGroupListItems(GroupDrawerAdapter groupDrawerAdapter) {
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
                saveGroupId();
            }
        }
        if (groupId != null) {
            // Load notification settings
            SettingsFragment.loadSetting();
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

    private void setupGroupList() {
        setupGroupListItems(groupDrawerAdapter);
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
                    // Load notification for selected group
                    SettingsFragment.loadSetting();

                    checkIfNeedToSyncGroup();
                    invalidateFragment();
                } else if (position == members.size() + 2) {
                    NewGroupActivity.newInstance(MainActivity.this);
                    drawerLayout.closeDrawers();
                }
            }
        });
    }

    private void checkIfNeedToSyncGroup() {
        Log.d(TAG, "checkIfNeedToSyncGroup groupId:" + groupId);
        if (TextUtils.isEmpty(groupId)) {
            Log.e(TAG, "Error: groupId is null.");
            return;
        }

        Log.d(TAG, "checkIfNeedToSyncGroup start.");
        // Check to sync new selected group
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        syncTimeKey = Helpers.getSyncTimeKey(MainActivity.TAG, groupId);
        syncTimeInMillis = sharedPreferences.getLong(syncTimeKey, 0);
        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncGroup.getGroupById(groupId).continueWith(onGetGroupsFinished, Task.UI_THREAD_EXECUTOR);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(MainActivity.this, syncTimeKey, syncTimeInMillis);
        }
    }

    private void invalidateFragment() {
        currentPosition = 1;

        if (groupId != null) {
            OverviewMainFragment.SLEEP_LENGTH = 300;

            getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.right_in, R.animator.left_out, 0, R.animator.left_out)
                .replace(R.id.main_activity_frame_layout_id, OverviewMainFragment.newInstance())
                .addToBackStack(OverviewMainFragment.class.getName())
                .commit();
            fab.setOnClickListener(v -> setupFab());
        } else {
            Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGroupId() {
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0).edit();
        sharedPreferencesEditor.putString(Group.ID_KEY, groupId);
        sharedPreferencesEditor.apply();
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

            isSignOut = true;
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
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> MainActivity.this.invalidateViews());

        invalidateViews();

        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                    new IntentFilter(Constant.NOTIFICATION_BROADCAST_INTENT));
            isReceiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();

        if (isReceiverRegistered && broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            isReceiverRegistered = false;
        }

        if (drawerLayout.isDrawerOpen(drawRecyclerView)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isSignOut) {
            SharedPreferences sharedPreferences =
                    getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
            sharedPreferences.edit().clear().apply();
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
            realm.close();
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawRecyclerView)) {
            closeDrawer();
        } else if (getFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            getFragmentManager().popBackStack();
        }
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
