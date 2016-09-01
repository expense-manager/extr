package com.expensemanager.app.settings;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.TimePickerFragment;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.service.SyncUser;
import com.expensemanager.app.welcome.WelcomeActivity;

import android.app.Activity;
import android.app.TimePickerDialog;
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
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    public static final int WEEKLY_TIME_PICKER = 0;
    public static final int MONTHLY_TIME_PICKER = 1;
    public static final String TIME_PICKER = "time_picker";
    public static final String SET_WEEKLY = "set_weekly";
    public static final String WEEKLY_NOTIFICATION = "weekly_notification";
    public static final String SET_MONTHLY = "set_monthly";
    public static final String MONTHLY_NOTIFICATION = "monthly_notification";

    public static boolean setWeekly;
    public static long weeklyNotificationInMillis;
    public static boolean setMonthly;
    public static long monthlyNotificationInMillis;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.setting_activity_weekly_notification_switch_id) Switch weeklyNotificationSwitch;
    @BindView(R.id.setting_activity_weekly_notification_time_label_text_view_id) TextView weeklyTimeLabelTextView;
    @BindView(R.id.setting_activity_weekly_notification_time_text_view_id) TextView weeklyTimeTextView;
    @BindView(R.id.setting_activity_monthly_notification_switch_id) Switch monthlyNotificationSwitch;
    @BindView(R.id.setting_activity_monthly_notification_time_label_text_view_id) TextView monthlyTimeLabelTextView;
    @BindView(R.id.setting_activity_monthly_notification_time_text_view_id) TextView monthlyTimeTextView;

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
        SettingsActivity.setWeekly = sharedPreferences.getBoolean(SET_WEEKLY, true);
        SettingsActivity.weeklyNotificationInMillis = sharedPreferences.getLong(WEEKLY_NOTIFICATION, getDefaultWeeklyNotification());
        SettingsActivity.setMonthly = sharedPreferences.getBoolean(SET_MONTHLY, true);
        SettingsActivity.monthlyNotificationInMillis = sharedPreferences.getLong(MONTHLY_NOTIFICATION, getDefaultMonthlyNotification());

        weeklyNotificationSwitch.setOnCheckedChangeListener(
            (compoundButton, b) -> {
                setWeekly = b;
                setupTimeSetting(b, weeklyTimeLabelTextView, weeklyTimeTextView);
                saveSettings();
            });

        monthlyNotificationSwitch.setOnCheckedChangeListener(
            (compoundButton, b) -> {
                setMonthly = b;
                setupTimeSetting(b, monthlyTimeLabelTextView, monthlyTimeTextView);
                saveSettings();
            });

        weeklyTimeTextView.setOnClickListener(v -> {setupTimePicker(weeklyNotificationInMillis, true);});
        monthlyTimeTextView.setOnClickListener(v -> {setupTimePicker(monthlyNotificationInMillis, false);});


        setupViews();
    }

    private void setupTimePicker(long notificationInMillis, boolean isWeekly) {
        if ((isWeekly && !setWeekly) || (!isWeekly && !setMonthly)) {
            return;
        }

        TimePickerFragment.TIME_PICKER = isWeekly ? WEEKLY_TIME_PICKER : MONTHLY_TIME_PICKER;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(notificationInMillis);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerFragment timePickerFragment = TimePickerFragment
            .newInstance(hour, minute);
        timePickerFragment.setListener(this);
        timePickerFragment.show(getSupportFragmentManager(), TIME_PICKER);
    }

    private void setupTimeSetting(boolean isCheck, TextView timeLabel, TextView time) {
        int gray = ContextCompat.getColor(this, R.color.gray);
        int black = ContextCompat.getColor(this, R.color.black);
        if (isCheck) {
            timeLabel.setTextColor(black);
            time.setTextColor(black);
        } else {
            timeLabel.setTextColor(gray);
            time.setTextColor(gray);
        }
    }

    private void setupViews() {
        weeklyNotificationSwitch.setChecked(setWeekly);
        monthlyNotificationSwitch.setChecked(setMonthly);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

        calendar.setTimeInMillis(weeklyNotificationInMillis);
        weeklyTimeTextView.setText(timeFormat.format(calendar.getTime()));

        calendar.setTimeInMillis(monthlyNotificationInMillis);
        monthlyTimeTextView.setText(timeFormat.format(calendar.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();

        if (TimePickerFragment.TIME_PICKER == WEEKLY_TIME_PICKER) {
            calendar.setTimeInMillis(weeklyNotificationInMillis);
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            weeklyNotificationInMillis = calendar.getTimeInMillis();
        } else {
            calendar.setTimeInMillis(monthlyNotificationInMillis);
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            monthlyNotificationInMillis = calendar.getTimeInMillis();
        }

        saveSettings();
    }

    private void saveSettings() {
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getString(R.string.shared_preferences_session_key), MODE_PRIVATE).edit();
        sharedPreferencesEditor.putBoolean(SET_WEEKLY, setWeekly);
        sharedPreferencesEditor.putLong(WEEKLY_NOTIFICATION, weeklyNotificationInMillis);
        sharedPreferencesEditor.putBoolean(SET_MONTHLY, setMonthly);
        sharedPreferencesEditor.putLong(MONTHLY_NOTIFICATION, monthlyNotificationInMillis);
        sharedPreferencesEditor.apply();
    }

    private long getDefaultWeeklyNotification() {
        Calendar calendar = Calendar.getInstance();
        // Go to next week
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        // Go to Sunday
        calendar.set(Calendar.DAY_OF_WEEK, 1);
        // Set 10:00:00 am
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    private long getDefaultMonthlyNotification() {
        Calendar calendar = Calendar.getInstance();
        // Go to next month
        calendar.add(Calendar.MONTH, 1);
        // Go to 1st of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_edit_account_id:
                // todo: go to ProfileActivity in edit mode
                ProfileActivity.newInstance(this, null);
                return true;
            case R.id.menu_item_sign_out_id:
                signOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
            WelcomeActivity.newInstance(SettingsActivity.this);
            finish();
            return null;
        }
    };
}
