package com.expensemanager.app.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.RNotification;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class NotificationsActivity extends BaseActivity {
    private static final String TAG = NotificationsActivity.class.getSimpleName();

    private ArrayList<RNotification> notifications;
    private NotificationAdapter notificationAdapter;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_right_title_text_view_id) TextView rightTitleTextView;
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

        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notifications);

        setupToolbar();
        setupRecyclerView();
    }

    private void invalidateViews() {
        noNotificationTextView.setVisibility(View.VISIBLE);

        notificationAdapter.clear();
        notificationAdapter.addAll(RNotification.getAllNotifications());
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
        rightTitleTextView.setText(R.string.mark_all_read);
        rightTitleTextView.setVisibility(View.VISIBLE);
        rightTitleTextView.setOnClickListener(v -> markAllRead());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(notificationAdapter);
    }

    public void markAllRead() {
        boolean hasUnRead = false;
        for (RNotification notification : notifications) {
            if (!notification.isChecked()) {
                hasUnRead = true;
                break;
            }
        }
        new AlertDialog.Builder(this)
            .setTitle(hasUnRead ? R.string.read_all_notification : R.string.no_unread_notification)
            .setMessage(hasUnRead ? R.string.read_all_notification_message : R.string.no_unread_notification_message)
            .setPositiveButton(R.string.ok, hasUnRead ? (DialogInterface dialog, int which) -> {
                // Save to realm
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                for (RNotification notification : notifications) {
                    notification.setChecked(true);
                    realm.copyToRealmOrUpdate(notification);
                }
                realm.commitTransaction();
                realm.close();
            } : (DialogInterface dialog, int which) -> dialog.dismiss())
            .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
            .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());

        invalidateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}