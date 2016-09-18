package com.expensemanager.app.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.category.CategoryActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.RNotification;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.service.SyncGroup;
import com.expensemanager.app.service.SyncUser;
import com.expensemanager.app.service.font.Font;
import com.expensemanager.app.welcome.WelcomeActivity;
import com.instabug.library.Instabug;

import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Zhaolong Zhong on 9/10/16.
 */

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    public static final String SETTING_BUDGET = "setting_budget";
    public static final String SET_WEEKLY = "set_weekly";
    public static final String SET_MONTHLY = "set_monthly";
    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;

    public static boolean setWeekly;
    public static boolean setMonthly;

    private String loginUserId;
    private String groupId;
    private Group group;
    private boolean isSignOut = false;
    private double weeklyBudget;
    private double monthlyBudget;

    @BindView(R.id.setting_activity_profile_photo_image_view_id) ImageView photoImageView;
    @BindView(R.id.setting_activity_edit_profile_text_view_id) TextView editProfileTextView;
    @BindView(R.id.setting_activity_category_label_text_view_id) TextView categoryLabelTextView;
    @BindView(R.id.setting_activity_category_description_text_view_id) TextView editCategoryTextView;
    @BindView(R.id.setting_activity_budget_label_text_view_id) TextView budgetTextView;
    @BindView(R.id.setting_activity_weekly_budget_relative_layout_id) RelativeLayout weeklyBudgetRelativeLayout;
    @BindView(R.id.setting_activity_weekly_budget_text_view_id) TextView weeklyBudgetTextView;
    @BindView(R.id.setting_activity_monthly_budget_relative_layout_id) RelativeLayout monthlyBudgetRelativeLayout;
    @BindView(R.id.setting_activity_monthly_budget_text_view_id) TextView monthlyBudgetTextView;
    @BindView(R.id.setting_activity_weekly_notification_switch_id) Switch weeklyNotificationSwitch;
    @BindView(R.id.setting_activity_monthly_notification_switch_id) Switch monthlyNotificationSwitch;
    @BindView(R.id.setting_activity_signout_text_view_id) TextView signOutTextView;
    @BindView(R.id.setting_activity_profile_label_text_view_id) TextView profileLabelTextView;
    @BindView(R.id.setting_activity_notification_label_text_view_id) TextView notificationLabelTextView;
    @BindView(R.id.setting_activity_general_label_text_view_id) TextView generalLabelTextView;
    @BindView(R.id.setting_activity_weekly_send_feedback_text_view_id) TextView sendFeedbackTextView;

    public static Fragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        loginUserId = Helpers.getLoginUserId();
        groupId = Helpers.getCurrentGroupId();
        group = Group.getGroupById(groupId);

        weeklyNotificationSwitch.setOnCheckedChangeListener(
                (compoundButton, b) -> {
                    setWeekly = b;
                    saveSettings();
                    if (b && groupId != null) {
                        SettingsFragment.setWeeklyNotification(getActivity(), groupId);
                    } else if (groupId == null) {
                        weeklyNotificationSwitch.setChecked(false);
                        Toast.makeText(getActivity(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                    }
                });

        monthlyNotificationSwitch.setOnCheckedChangeListener(
                (compoundButton, b) -> {
                    setMonthly = b;
                    saveSettings();
                    if (b && groupId != null) {
                        SettingsFragment.setMonthlyNotification(getActivity(), groupId);
                    } else if (groupId == null) {
                        monthlyNotificationSwitch.setChecked(false);
                        Toast.makeText(getActivity(), R.string.select_group_hint, Toast.LENGTH_SHORT).show();
                    }
                });

        signOutTextView.setOnClickListener(v -> signOut());

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
        calendar.add(Calendar.SECOND, 5);
        RNotification.setupOrUpdateNotifications(activity, activity.getString(R.string.weekly_report), activity.getString(R.string.weekly_report_message), groupId, false, RNotification.WEEKLY, calendar.getTime());
    }

    public static void setMonthlyNotification(Activity activity, String groupId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 5);
        RNotification.setupOrUpdateNotifications(activity, activity.getString(R.string.monthly_report), activity.getString(R.string.monthly_report_message), groupId, false, RNotification.MONTHLY, calendar.getTime());
    }

    private void setupViews() {
        User user = User.getUserById(loginUserId);
        if (user != null) {
            Helpers.loadProfilePhoto(photoImageView, user.getPhotoUrl());
        }
        editProfileTextView.setOnClickListener(v -> ProfileActivity.newInstance(getActivity(), null, true));

        editCategoryTextView.setOnClickListener(v -> CategoryActivity.newInstance(getActivity()));
        weeklyNotificationSwitch.setChecked(setWeekly);
        monthlyNotificationSwitch.setChecked(setMonthly);

        Typeface boldTypeface = EApplication.getInstance().getTypeface(Font.BOLD);
        budgetTextView.setTypeface(boldTypeface);
        profileLabelTextView.setTypeface(boldTypeface);
        categoryLabelTextView.setTypeface(boldTypeface);
        notificationLabelTextView.setTypeface(boldTypeface);
        generalLabelTextView.setTypeface(boldTypeface);

        sendFeedbackTextView.setOnClickListener(v -> Instabug.invoke());

        // Budgets
        Group group = Group.getGroupById(groupId);
        if (group != null) {
            weeklyBudget = group.getWeeklyBudget();
            monthlyBudget = group.getMonthlyBudget();
        }

        weeklyBudgetTextView.setText(Helpers.doubleToCurrency(weeklyBudget));
        monthlyBudgetTextView.setText(Helpers.doubleToCurrency(monthlyBudget));
        weeklyBudgetRelativeLayout.setOnClickListener(v -> setupBudgetDialog(WEEKLY));
        monthlyBudgetRelativeLayout.setOnClickListener(v -> setupBudgetDialog(MONTHLY));
    }

    private void setupBudgetDialog(int requestCode) {
        SettingsBudgetFragment settingBudgetFragment = SettingsBudgetFragment.newInstance();
        settingBudgetFragment.setListener(settingBudgetListener);
        settingBudgetFragment.setParams(requestCode, requestCode == WEEKLY ? weeklyBudget : monthlyBudget);
        settingBudgetFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), SETTING_BUDGET);
    }

    private SettingsBudgetFragment.SettingBudgetListener settingBudgetListener = new SettingsBudgetFragment.SettingBudgetListener() {
        @Override
        public void onFinishSettingBudgetDialog(int requestCode, double amount) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            if (requestCode == WEEKLY) {
                weeklyBudget = amount;
                weeklyBudgetTextView.setText(Helpers.doubleToCurrency(amount));
                group.setWeeklyBudget(amount);
            } else {
                monthlyBudget = amount;
                monthlyBudgetTextView.setText(Helpers.doubleToCurrency(amount));
                group.setMonthlyBudget(amount);
            }

            realm.copyToRealmOrUpdate(group);
            realm.commitTransaction();
            realm.close();

            SyncGroup.update(group);
        }
    };

    private void saveSettings() {
        SharedPreferences.Editor sharedPreferencesEditor = getActivity().getSharedPreferences(getString(R.string.shared_preferences_session_key), MODE_PRIVATE).edit();
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

    private void signOut() {
        new AlertDialog.Builder(getActivity())
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
            WelcomeActivity.newInstance(getActivity());
            getActivity().finish();
            return null;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isSignOut) {
            SharedPreferences sharedPreferences =
                    getActivity().getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
            sharedPreferences.edit().clear().apply();
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
            realm.close();
        }
    }
}
