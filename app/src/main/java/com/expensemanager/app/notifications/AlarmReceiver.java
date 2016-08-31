package com.expensemanager.app.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.RNotification;

import java.util.Date;

/**
 * Created by Zhaolong Zhong on 8/22/16.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, NotificationsActivity.class);

        String notificationId = intent.getStringExtra(RNotification.ID_KEY);
        RNotification rNotification = RNotification.getNotificationById(notificationId);
        if (rNotification == null) {
            return;
        }
        String title = rNotification.getTitle();
        String message = rNotification.getMessage();
        Date lastWeek = Helpers.getLastWeekOfYear(rNotification.getCreatedAt());
        Date[] startEnd = null;
        if (rNotification.getType() == RNotification.WEEKLY) {
            startEnd = Helpers.getWeekStartEndDate(lastWeek);
        } else if (rNotification.getType() == RNotification.MONTHLY) {
            startEnd = Helpers.getMonthStartEndDate(lastWeek);
        }
        double amount = 0;
        for (Expense e : Expense.getExpensesByRange(startEnd)) {
            amount += e.getAmount();
        }

        // todo: delete notification if amount is 0
        message += "$" + amount;

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(NotificationsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Notification notification = builder
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentIntent(pendingIntent).build();

        Log.i(TAG, "notify new notification: " + message);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
