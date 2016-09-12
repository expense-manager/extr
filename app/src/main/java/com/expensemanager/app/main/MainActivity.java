package com.expensemanager.app.main;

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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
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

import com.expensemanager.app.R;
import com.expensemanager.app.category.CategoryFragment;
import com.expensemanager.app.category.NewCategoryActivity;
import com.expensemanager.app.expense.ExpenseFragment;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.group.GroupDetailActivity;
import com.expensemanager.app.group.NewGroupActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.DrawerItem;
import com.expensemanager.app.models.DrawerSubItem;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.notifications.AlarmReceiver;
import com.expensemanager.app.notifications.NotificationFragment;
import com.expensemanager.app.report.ReportMainFragment;
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

    public static final int NEW_EXPENSE = 0;
    public static final int NEW_CATEGORY = 1;

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

    @BindView(R.id.main_activity_drawer_layout_id) DrawerLayout drawerLayout;
    @BindView(R.id.main_activity_toolbar_id) Toolbar toolbar;
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

        drawerToggle = setupDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);
        drawRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_frame_layout_id);

        if (fragment == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame_layout_id, OverviewFragment.newInstance())
                    .addToBackStack(OverviewFragment.class.getName())
            .commit();
        }

        fab.setOnClickListener(v -> setupFab(NEW_EXPENSE));

        isReceiverRegistered = false;
        broadcastReceiver = new AlarmReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                invalidateViews();
            }
        };

        invalidateViews();

//        // Enable storage permission for LeakCanary
//        if (BuildConfig.DEBUG) {
//            checkExternalStoragePermission();
//        }
    }

    private void invalidateViews() {
        drawerAdapter = new DrawerAdapter(this, drawerItems, drawerSubItems, currentUser);
        setupDrawerList(drawerAdapter);
        drawerAdapter.invalidate();

        groupDrawerAdapter = new GroupDrawerAdapter(this, members, currentUser);
        setupGroupListItems(groupDrawerAdapter);

        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncUser.getLoginUser().continueWith(onGetLoginUserFinished, Task.UI_THREAD_EXECUTOR);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(this, syncTimeKey, syncTimeInMillis);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Reset to drawer menu list at close
//                setupDrawerList();
            }
        };
    }

    private void setupDrawerList(DrawerAdapter drawerAdapter) {
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
                    handler.postDelayed(pendingRunnable, 0);
                    pendingRunnable = null;
                }
            }
        });
    }

    private void selectItem(int position) {
        switch(position) {
            case 0:
                setupGroupList();
                fab.setOnClickListener(va -> setupFab(NEW_EXPENSE));
                break;
            case 1:
                if (currentPosition == position) {
                    drawerLayout.closeDrawers();
                    return;
                } else {
                    currentPosition = position;
                }

                if (groupId != null) {
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out)
                            .replace(R.id.main_activity_frame_layout_id, OverviewFragment.newInstance())
                            .addToBackStack(OverviewFragment.class.getName())
                            .commit();
                    setTitle(getString(R.string.app_name));
                    fab.setOnClickListener(va -> setupFab(NEW_EXPENSE));
                } else {
                    Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (currentPosition == position) {
                    drawerLayout.closeDrawers();
                    return;
                } else {
                    currentPosition = position;
                }

                if (groupId != null) {
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out)
                            .replace(R.id.main_activity_frame_layout_id, ExpenseFragment.newInstance())
                            .addToBackStack(ExpenseFragment.class.getName())
                            .commit();
                    setTitle(getString(R.string.expense));
                    fab.setOnClickListener(va -> setupFab(NEW_EXPENSE));
                } else {
                    Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (currentPosition == position) {
                    drawerLayout.closeDrawers();
                    return;
                } else {
                    currentPosition = position;
                }

                if (groupId != null) {
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out)
                            .replace(R.id.main_activity_frame_layout_id, ReportMainFragment.newInstance())
                            .addToBackStack(ReportMainFragment.class.getName())
                            .commit();
                    setTitle(getString(R.string.report));
                    fab.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                }
                break;
            case 4:
                if (currentPosition == position) {
                    drawerLayout.closeDrawers();
                    return;
                } else {
                    currentPosition = position;
                }

                if (groupId != null) {
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out)
                            .replace(R.id.main_activity_frame_layout_id, CategoryFragment.newInstance())
                            .addToBackStack(CategoryFragment.class.getName())
                            .commit();
                    setTitle(getString(R.string.category));
                    fab.setOnClickListener(va -> setupFab(NEW_CATEGORY));
                } else {
                    Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                }
                break;
            case 5:
                if (groupId != null) {
                    GroupDetailActivity.newInstance(MainActivity.this, groupId);
                    fab.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:
                if (currentPosition == position) {
                    drawerLayout.closeDrawers();
                    return;
                } else {
                    currentPosition = position;
                }

                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out)
                        .replace(R.id.main_activity_frame_layout_id, NotificationFragment.newInstance())
                        .addToBackStack(NotificationFragment.class.getName())
                        .commit();
                setTitle(getString(R.string.notification));
                fab.setVisibility(View.INVISIBLE);
                break;
            case 7:
                // help
                break;
            case 8:
                if (currentPosition == position) {
                    drawerLayout.closeDrawers();
                    return;
                } else {
                    currentPosition = position;
                }

                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out)
                        .replace(R.id.main_activity_frame_layout_id, SettingsFragment.newInstance())
                        .addToBackStack(NotificationFragment.class.getName())
                        .commit();
                setTitle(getString(R.string.settings));
                fab.setVisibility(View.INVISIBLE);
                break;
            case 10:
                signOut();
                break;
            case 11:
                // About
                break;
            default:
                break;
        }
    }

    private void setupFab(int fabType) {
        fab.setVisibility(View.VISIBLE);

        if (groupId == null) {
            Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
            return;
        }

        switch (fabType) {
            case NEW_EXPENSE:
                NewExpenseActivity.newInstance(this);
                overridePendingTransition(R.anim.right_in, R.anim.stay);
                break;
            case NEW_CATEGORY:
                NewCategoryActivity.newInstance(this);
                overridePendingTransition(R.anim.right_in, R.anim.stay);
                break;
        }
    }

    private void setupDrawerListItems() {
        drawerItems.clear();
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_home).setTitle(getString(R.string.nav_overview)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_credit_card).setTitle(getString(R.string.nav_expense)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_trending_up).setTitle(getString(R.string.nav_report)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_buffer).setTitle(getString(R.string.nav_category)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_account_multiple).setTitle(getString(R.string.nav_group)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_bell).setTitle(getString(R.string.nav_notifications)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_help_circle).setTitle(getString(R.string.nav_help)));
        drawerItems.add(new DrawerItem().setIcon(R.drawable.ic_settings).setTitle(getString(R.string.nav_settings)));

        drawerSubItems.clear();
        drawerSubItems.add(new DrawerSubItem().setTitle(getString(R.string.sign_out)));
        drawerSubItems.add(new DrawerSubItem().setTitle(getString(R.string.nav_about)));
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

    private void setupGroupList() {
        setupGroupListItems(groupDrawerAdapter);
        drawRecyclerView.setAdapter(groupDrawerAdapter);

        groupDrawerAdapter.setOnItemClickLister(new GroupDrawerAdapter.OnItemSelecteListener() {
            @Override
            public void onItemSelected(View v, int position) {
                if (position == 0) {
                    setupDrawerList(drawerAdapter);
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

                    invalidateFragment();
                } else if (position == members.size() + 2) {
                    NewGroupActivity.newInstance(MainActivity.this);
                    drawerLayout.closeDrawers();
                }
            }
        });
    }

    private void invalidateFragment() {
        currentPosition = 1;

        if (groupId != null) {
            OverviewFragment.SLEEP_LENGTH = 300;

            getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out)
                .replace(R.id.main_activity_frame_layout_id, OverviewFragment.newInstance())
                .addToBackStack(OverviewFragment.class.getName())
                .commit();
            setTitle(getString(R.string.app_name));
            fab.setOnClickListener(va -> setupFab(NEW_EXPENSE));
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

            SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
            sharedPreferences.edit().clear().apply();
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
        realm.addChangeListener(v -> invalidateViews());

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
