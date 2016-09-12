package com.expensemanager.app.notifications;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.RNotification;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 9/10/16.
 */

public class NotificationFragment extends Fragment {
    private static final String TAG = NotificationFragment.class.getSimpleName();

    private ArrayList<RNotification> notifications;
    private NotificationAdapter notificationAdapter;

    @BindView(R.id.notifications_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.notifications_activity_empty_state_text_view_id) TextView noNotificationTextView;

    public static Fragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notifications_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getActivity(), notifications);
        setupRecyclerView();

        invalidateViews();
    }

    private void invalidateViews() {
        noNotificationTextView.setVisibility(View.VISIBLE);

        notificationAdapter.clear();
        notificationAdapter.addAll(RNotification.getAllNotifications());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(notificationAdapter);
    }

    public void markAllRead() {
        Log.d(TAG, "markAllRead clicked.");
        boolean hasUnRead = false;
        for (RNotification notification : notifications) {
            if (!notification.isChecked()) {
                hasUnRead = true;
                break;
            }
        }
        new AlertDialog.Builder(getActivity())
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notification_menu, menu);
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_notification_mark_all_read_id:
                markAllRead();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
