package com.expensemanager.app.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.RNotification;
import com.expensemanager.app.report.ReportDetailActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= NotificationAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<RNotification> notifications;
    private Context context;

    public NotificationAdapter(Context context, ArrayList<RNotification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.notifications.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                View view = inflater.inflate(R.layout.notification_item_default, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.notification_item_default, parent, false);
                viewHolder = new ViewHolderDefault(defaultView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_DEFAULT:
                ViewHolderDefault viewHolderDefault = (ViewHolderDefault) viewHolder;
                configureViewHolderDefault(viewHolderDefault, position);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        RNotification notification = notifications.get(position);
        int notRead = ContextCompat.getColor(context, notification.getType() == RNotification.WEEKLY? R.color.colorPrimary : R.color.colorAccent);

        viewHolder.leftBarTextView.setBackgroundColor(notRead);
        viewHolder.titleTextView.setText(notification.getTitle());
        viewHolder.createdAtTextView.setText(Helpers.formatCreateAt(notification.getCreatedAt()));
        viewHolder.messageTextView.setText(notification.getMessage());

        ColorDrawable colorDrawable = new ColorDrawable(notRead);
        viewHolder.checkImageView.setImageDrawable(colorDrawable);

        // Load category data or hide
        if (notification.isChecked()) {
            viewHolder.checkImageView.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.checkImageView.setVisibility(View.VISIBLE);
        }

        // Set item click listener
        viewHolder.itemView.setOnClickListener(v -> {
            Date lastWeek = Helpers.getLastWeekOfYear(notification.getCreatedAt());
            Date[] startEnd = null;
            int requestCode = 0;
            switch (notification.getType()) {
                case RNotification.WEEKLY:
                    startEnd = Helpers.getWeekStartEndDate(lastWeek);
                    requestCode = ReportDetailActivity.WEEKLY;
                    break;
                case RNotification.MONTHLY:
                    startEnd = Helpers.getMonthStartEndDate(lastWeek);
                    requestCode = ReportDetailActivity.MONTHLY;
                    break;
            }
            ReportDetailActivity.newInstance(context, startEnd, requestCode);
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);

            checkNotification(notification);
        });

        // Delete notification
        viewHolder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_notification_title)
                .setMessage(R.string.delete_notification_message)
                .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> RNotification.delete(notification.getId()))
                .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
                .show();

            return true;
        });
    }

    private void checkNotification(RNotification notification) {
        // Save to realm
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        notification.setChecked(true);
        realm.copyToRealmOrUpdate(notification);
        realm.commitTransaction();
        realm.close();
    }

    public void clear() {
        notifications.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<RNotification> expenses) {
        this.notifications.addAll(expenses);
        removeExpiredNotification();
        notifyDataSetChanged();
    }

    private void removeExpiredNotification() {
        if (notifications == null) {
            return;
        }
        for (int i = notifications.size() - 1; i >= 0; i--) {
            RNotification notification = notifications.get(i);
            if (hasExpired(notification)) {
                notifications.remove(i);
                RNotification.delete(notification.getId());
            }
        }
    }

    private boolean hasExpired(RNotification notification) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(notification.getCreatedAt());

        int currentYear = currentCalendar.get(Calendar.YEAR);
        int targetYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int targetMonth = currentCalendar.get(Calendar.MONTH);
        int currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR);

        switch (notification.getType()) {
            case RNotification.WEEKLY:
                return targetYear < currentYear || targetWeek < currentWeek;
            case RNotification.MONTHLY:
                return targetYear < currentYear || targetMonth < currentMonth;
        }
        return false;
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.notification_item_default_left_decoration_text_view_id) TextView leftBarTextView;
        @BindView(R.id.notification_item_default_title_text_view_id) TextView titleTextView;
        @BindView(R.id.notification_item_default_created_at_text_view_id) TextView createdAtTextView;
        @BindView(R.id.notification_item_default_check_image_view_id) CircleImageView checkImageView;
        @BindView(R.id.notification_item_default_message_text_view_id) TextView messageTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
