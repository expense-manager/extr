package com.expensemanager.app.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.category.CategoryActivity;
import com.expensemanager.app.expense.ExpenseActivity;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.group.GroupActivity;
import com.expensemanager.app.models.DrawerItem;
import com.expensemanager.app.models.DrawerSubItem;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.RNotification;
import com.expensemanager.app.models.User;
import com.expensemanager.app.notifications.NotificationsActivity;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.report.ReportActivity;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.SyncUser;
import com.expensemanager.app.settings.SettingsActivity;
import com.expensemanager.app.welcome.WelcomeActivity;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActionBarDrawerToggle drawerToggle;
    private DrawerAdapter drawerAdapter;
    private GroupDrawerAdapter groupDrawerAdapter;
    public static String groupId;
    private ArrayList<DrawerItem> drawerItems;
    private ArrayList<DrawerSubItem> drawerSubItems;
    private ArrayList<Group> groups;
    private String loginUserId;

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

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        loginUserId = sharedPreferences.getString(User.USER_ID, null);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);

        User currentUser = User.getUserById(loginUserId);
        drawerItems = new ArrayList<>();
        drawerSubItems = new ArrayList<>();
        groups = new ArrayList<>();
        drawerAdapter = new DrawerAdapter(this, drawerItems, drawerSubItems, currentUser);
        groupDrawerAdapter = new GroupDrawerAdapter(this, groups, currentUser);
        setupDrawerListItems();
        setupDrawerList();

        drawerToggle = setupDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);

        drawRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_frame_layout_id);

        if (fragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame_layout_id, OverviewFragment.newInstance())
                    .addToBackStack(OverviewFragment.class.getName())
                    .commit();
        }

        SettingsActivity.loadSetting(this);

        SyncUser.getLoginUser().continueWith(onGetLoginUserFinished, Task.UI_THREAD_EXECUTOR);
        SyncCategory.getAllCategories();
        SyncExpense.getAllExpenses();

        FirebaseCrash.report(new Exception("My first Android non-fatal error"));
        FirebaseCrash.log("Activity created");
    }

    private void setupDrawerListItems() {
        drawerAdapter.add( new DrawerItem().setIcon(R.drawable.ic_credit_card).setTitle(getString(R.string.nav_expense)));
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
                        ExpenseActivity.newInstance(MainActivity.this);
                        break;
                    case 2:
                        ReportActivity.newInstance(MainActivity.this);
                        break;
                    case 3:
                        CategoryActivity.newInstance(MainActivity.this);
                        break;
                    case 4:
                        GroupActivity.newInstance(MainActivity.this);
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
                    case 8:
                        signOut();
                        break;
                    case 9:
                        // About
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void setupGroupList() {
        drawRecyclerView.setAdapter(groupDrawerAdapter);

        groupDrawerAdapter.setOnItemClickLister(new GroupDrawerAdapter.OnItemSelecteListener() {
            @Override
            public void onItemSelected(View v, int position) {
                if (position == 0) {
                    setupDrawerList();
                } else {
                    groupId = groups.get(position).getId();
                }
            }
        });
    }

    private void testNotifications() {
        Calendar calendar = Calendar.getInstance();
        // todo:set notification fime according to setting millis
        calendar.add(Calendar.MINUTE, 1);
        RNotification.setupOrUpdateNotifications(this, getString(R.string.weekly_report), getString(R.string.weekly_report_message), false, RNotification.WEEKLY, calendar.getTime());
        calendar.add(Calendar.MINUTE, 1);
        RNotification.setupOrUpdateNotifications(this, getString(R.string.monthly_report), getString(R.string.monthly_report_message), false, RNotification.MONTHLY, calendar.getTime());
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private Continuation<Void, Void> onGetLoginUserFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            User currentUser = User.getUserById(loginUserId);
            if (currentUser != null) {
                drawerAdapter.loadUser(currentUser);
                groupDrawerAdapter.loadUser(currentUser);
            }

            return null;
        }
    };

    private void signOut() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.sign_out_message)
                .setPositiveButton(R.string.sign_out, (DialogInterface dialog, int which) -> {
                    SyncUser.logout();
                    SharedPreferences sharedPreferences =
                            getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
                    sharedPreferences.edit().clear().apply();
                    WelcomeActivity.newInstance(MainActivity.this);
                    finish();
                })
                .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
                .show();
    }

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
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
