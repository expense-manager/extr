package com.expensemanager.app.settings;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.RNotification;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.service.SyncUser;
import com.expensemanager.app.welcome.WelcomeActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    public static final String TIME_PICKER = "time_picker";
    public static final String SET_WEEKLY = "set_weekly";
    public static final String SET_MONTHLY = "set_monthly";

    public static boolean setWeekly;
    public static boolean setMonthly;

    private String loginUserId;
    private String groupId;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.setting_activity_profile_photo_image_view_id) ImageView photoImageView;
    @BindView(R.id.setting_activity_edit_profile_text_view_id) TextView editProfileTextView;
    @BindView(R.id.setting_activity_weekly_notification_switch_id) Switch weeklyNotificationSwitch;
    @BindView(R.id.setting_activity_monthly_notification_switch_id) Switch monthlyNotificationSwitch;
    @BindView(R.id.setting_activity_signout_text_view_id) TextView signoutTextView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ButterKnife.bind(this);

        setupToolbar();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), MODE_PRIVATE);
        loginUserId = sharedPreferences.getString(User.USER_ID, null);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);

        weeklyNotificationSwitch.setOnCheckedChangeListener(
            (compoundButton, b) -> {
                setWeekly = b;
                saveSettings();
                if (b && groupId != null) {
                    SettingsActivity.setWeeklyNotification(this, groupId);
                } else if (groupId == null) {
                    weeklyNotificationSwitch.setChecked(false);
                    Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                }
            });

        monthlyNotificationSwitch.setOnCheckedChangeListener(
            (compoundButton, b) -> {
                setMonthly = b;
                saveSettings();
                if (b && groupId != null) {
                    SettingsActivity.setMonthlyNotification(this, groupId);
                } else if (groupId == null) {
                    monthlyNotificationSwitch.setChecked(false);
                    Toast.makeText(getApplicationContext(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                }
            });

        signoutTextView.setOnClickListener(v -> signOut());

        setupViews();
    }

    public static void loadSetting(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getString(R.string.shared_preferences_session_key), MODE_PRIVATE);
        String groupId = sharedPreferences.getString(Group.ID_KEY, null);
        SettingsActivity.setWeekly = sharedPreferences.getBoolean(SET_WEEKLY, true);
        SettingsActivity.setMonthly = sharedPreferences.getBoolean(SET_MONTHLY, true);

        if (groupId != null) {
            setWeeklyNotification(activity, groupId);
            setMonthlyNotification(activity, groupId);
        }
    }

    public static void setWeeklyNotification(Activity activity, String groupId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 15);
        RNotification.setupOrUpdateNotifications(activity, activity.getString(R.string.weekly_report), activity.getString(R.string.weekly_report_message), groupId, false, RNotification.WEEKLY, calendar.getTime());
    }

    public static void setMonthlyNotification(Activity activity, String groupId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 15);
        RNotification.setupOrUpdateNotifications(activity, activity.getString(R.string.monthly_report), activity.getString(R.string.monthly_report_message), groupId, false, RNotification.MONTHLY, calendar.getTime());
    }

    private void setupViews() {
        User user = User.getUserById(loginUserId);
        if (user != null) {
            Helpers.loadProfilePhoto(photoImageView, user.getPhotoUrl());
        }
        editProfileTextView.setOnClickListener(v -> ProfileActivity.newInstance(this, null, true));

        weeklyNotificationSwitch.setChecked(setWeekly);
        monthlyNotificationSwitch.setChecked(setMonthly);
    }

    private void saveSettings() {
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getString(R.string.shared_preferences_session_key), MODE_PRIVATE).edit();
        sharedPreferencesEditor.putBoolean(SET_WEEKLY, setWeekly);
        sharedPreferencesEditor.putBoolean(SET_MONTHLY, setMonthly);
        sharedPreferencesEditor.apply();
    }

    public static long getDefaultWeeklyNotification() {
        Calendar calendar = Calendar.getInstance();
        // Check if pass the notification already
        // todo: change daily week notification to weekly
        if (calendar.get(Calendar.HOUR_OF_DAY) > 10
            || calendar.get(Calendar.MINUTE) > 0 || calendar.get(Calendar.SECOND) > 0) {
            // Go to next week
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        // Go to Sunday
        //calendar.set(Calendar.DAY_OF_WEEK, 1);
        // Set 10:00:00 am
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getDefaultMonthlyNotification() {
        Calendar calendar = Calendar.getInstance();
        // Check if pass the notification already
        // todo: change daily month notification to monthly
        if (calendar.get(Calendar.HOUR_OF_DAY) > 10
            || calendar.get(Calendar.MINUTE) > 0 || calendar.get(Calendar.SECOND) > 0) {
            // Go to next month
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        // Go to 1st of the month
        //calendar.set(Calendar.DAY_OF_MONTH, 1);
        // Set 10:00:00 am
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        titleTextView.setText(getString(R.string.settings));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

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
            WelcomeActivity.newInstance(SettingsActivity.this);
            finish();
            return null;
        }
    };
}
