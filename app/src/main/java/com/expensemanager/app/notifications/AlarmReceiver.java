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

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.RNotification;

import java.text.DecimalFormat;
import java.util.Date;

import io.realm.Realm;

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

        // Return if is null
        if (rNotification == null) {
            return;
        }

        String title = rNotification.getTitle();
        String message = rNotification.getMessage();
        String groupId = rNotification.getGroupId();
        Group group = Group.getGroupById(groupId);
        Date lastWeek = Helpers.getLastWeekOfYear(rNotification.getCreatedAt());
        Date[] startEnd = null;
        if (rNotification.getType() == RNotification.WEEKLY) {
            startEnd = Helpers.getWeekStartEndDate(lastWeek);
        } else if (rNotification.getType() == RNotification.MONTHLY) {
            startEnd = Helpers.getMonthStartEndDate(lastWeek);
        }
        double amount = 0;

        for (Expense expense : Expense.getExpensesByRangeAndGroupId(startEnd, groupId)) {
            amount += expense.getAmount();
        }

        // Delete notification from database if amount is zero
        if (amount == 0) {
            RNotification.delete(rNotification.getId());
            return;
        }

        message += " in " + group.getName() + " is $" + new DecimalFormat("##").format(amount);

        // Save to realm
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        rNotification.setMessage(message);
        realm.copyToRealmOrUpdate(rNotification);
        realm.commitTransaction();
        realm.close();

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(NotificationsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Notification notification = builder
                // Set title and message
                .setContentTitle(title)
                .setContentText(message)
                // Set styling
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).build();
        // Add default sound and vibration, or use builder.setVibrate(long[]).setSound(Uri.parse("uri://sadfasdfasdf.mp3"))
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(rNotification.getId().hashCode(), notification);
    }
}
