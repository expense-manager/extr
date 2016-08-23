package com.expensemanager.app.notifications;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.BaseActivity;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/20/16.
 */

public class NotificationsActivity extends BaseActivity {
    private static final String TAG = NotificationsActivity.class.getSimpleName();

    private static final int BROADCAST_REQUEST_CODE = 100;
    public static final String TITLE_KEY = "titleKey";
    public static final String MESSAGE_KEY = "messageKey";

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.notifications_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.notifications_activity_empty_state_text_view_id) TextView noNotificationTextView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NotificationsActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);
        ButterKnife.bind(this);

        setupToolbar();

        invalidateViews();

        setupNotifications("Expense Manager", "Your total expense for last week was $260.");
    }

    private void invalidateViews() {
        noNotificationTextView.setVisibility(View.VISIBLE);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0, 0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.notifications));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    private void setupNotifications(String title, String message) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.addCategory("android.intent.category.DEFAULT");
        notificationIntent.putExtra(TITLE_KEY, title);
        notificationIntent.putExtra(MESSAGE_KEY, message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, BROADCAST_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set a timer to wake a alarm, for example, 10:26PM
        Calendar timeAt = Calendar.getInstance();
        timeAt.set(Calendar.HOUR_OF_DAY, 22);
        timeAt.set(Calendar.MINUTE, 26);
        timeAt.set(Calendar.SECOND, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, timeAt.getTimeInMillis(), pendingIntent);
    }
}