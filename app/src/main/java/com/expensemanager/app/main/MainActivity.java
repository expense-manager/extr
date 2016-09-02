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
import com.expensemanager.app.models.RNotification;
import com.expensemanager.app.models.User;
import com.expensemanager.app.notifications.NotificationsActivity;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.report.ReportActivity;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.SyncUser;
import com.expensemanager.app.service.email.Mail;
import com.expensemanager.app.service.email.MailSender;
import com.expensemanager.app.service.email.Recipient;
import com.expensemanager.app.settings.SettingsActivity;
import com.expensemanager.app.welcome.WelcomeActivity;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActionBarDrawerToggle drawerToggle;
    private String loginUserId;

    @BindView(R.id.main_activity_drawer_layout_id) DrawerLayout drawerLayout;
    @BindView(R.id.main_activity_navigation_view_id) NavigationView navigationView;
    @BindView(R.id.main_activity_toolbar_id) Toolbar toolbar;
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

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        drawerToggle = setupDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);

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

        SyncCategory.getAllCategories();
        SyncExpense.getAllExpenses();

        FirebaseCrash.report(new Exception("My first Android non-fatal error"));
        FirebaseCrash.log("Activity created");
    }

    private void testEmail() {
        // Fill google email and password
        String email = "expensemanagers@gmail.com";
        String password = "expense_managers";
        MailSender mailSender = new MailSender(email, password);

        Mail.MailBuilder builder = new Mail.MailBuilder();
        Mail mail = builder
                .setSender(email)
                .addRecipient(new Recipient(email))
                .setSubject("Expense Manager Team")
                .setText("Hello from Expense Manager Team")
                .setHtml("<h1 style=\"color:black;\">Thank you!</h1>")
                .build();

        mailSender.sendMail(mail, new MailSender.OnMailSentListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "sendMail onSuccess");
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "sendMail onError", error);
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

    private void setupDrawerContent(NavigationView navigationView) {
        View headView = navigationView.getHeaderView(0);
        CircleImageView navHeaderCircleImageView =  (CircleImageView) headView.findViewById(R.id.nav_header_avatar_circle_image_view_id);
        TextView navHeaderTitleTextView = (TextView) headView.findViewById(R.id.nav_header_title_text_view_id);

        User currentUser = User.getUserById(loginUserId);

        if (currentUser != null) {
            Glide.with(MainActivity.this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.profile_place_holder_image)
                    .dontAnimate()
                    .into(navHeaderCircleImageView);

            navHeaderCircleImageView.setOnClickListener(v -> {
                ProfileActivity.newInstance(this, null);

                drawerLayout.closeDrawer(navigationView);
            });

            String fullname = currentUser.getFullname();

            if (fullname != null && !fullname.isEmpty()) {
                navHeaderTitleTextView.setText(fullname);
            } else {
                navHeaderTitleTextView.setText(getString(R.string.app_name));
            }
        } else {
            Glide.with(MainActivity.this)
                    .load(R.drawable.profile_place_holder_image)
                    .placeholder(R.drawable.profile_place_holder_image)
                    .into(navHeaderCircleImageView);
            SyncUser.getLoginUser().continueWith(onGetLoginUserFinished, Task.UI_THREAD_EXECUTOR);
        }

        navigationView.setNavigationItemSelectedListener((MenuItem menuItem) -> {
            selectDrawerItem(menuItem);
            return true;
        });
    }

    private Continuation<Void, Void> onGetLoginUserFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            User currentUser = User.getUserById(loginUserId);
            if (currentUser != null) {
                setupDrawerContent(navigationView);
            }

            return null;
        }
    };

    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.nav_expense:
                ExpenseActivity.newInstance(this);
                break;
            case R.id.nav_report:
                ReportActivity.newInstance(this);
                break;
            case R.id.nav_category:
                CategoryActivity.newInstance(this);
                break;
            case R.id.nav_group:
                GroupActivity.newInstance(this);
                break;
            case R.id.nav_notifications:
                NotificationsActivity.newInstance(this);
                break;
//            case R.id.nav_help:
//                break;
            case R.id.nav_settings:
                SettingsActivity.newInstance(this);
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            case R.id.nav_about:
                break;
            default:
                break;
        }

        drawerLayout.closeDrawer(navigationView);
    }

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

        unSelectedMenuItem();
    }

    private void unSelectedMenuItem() {
        // todo: any better way to handle this.
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
