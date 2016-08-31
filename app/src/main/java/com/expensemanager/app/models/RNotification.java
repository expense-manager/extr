package com.expensemanager.app.models;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/23/16.
 */

@RealmClass
public class RNotification implements RealmModel {
    private static final String TAG = RNotification.class.getSimpleName();

    public static final String TITLE_JSON_KEY = "title";
    public static final String MESSAGE_JSON_KEY = "message";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String TITLE_KEY = "title";
    public static final String MESSAGE_KEY = "message";
    public static final String IS_REMOTE = "isRemote";
    public static final String IS_CHECKED = "isChecked";
    public static final String CREATED_AT_KEY= "createdAt";
    public static final String TYPE_KEY= "type";

    private static final int BROADCAST_REQUEST_CODE = 100;
    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;

    @PrimaryKey
    private String id;
    private String title;
    private String message;
    private boolean isRemote;
    private boolean isChecked;
    private Date createdAt;
    private int type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return all notifications
     */
    public static RealmResults<RNotification> getAllNotifications() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RNotification> notifications = realm.where(RNotification.class)
            .lessThanOrEqualTo(CREATED_AT_KEY, new Date())
            .findAllSorted(CREATED_AT_KEY, Sort.DESCENDING);
        realm.close();

        return notifications;
    }

    /**
     * @param id
     * @return RNotification object if exist, otherwise return null.
     */
    public static @Nullable RNotification getNotificationById(String id) {
        Realm realm = Realm.getDefaultInstance();
        RNotification notification = realm.where(RNotification.class).equalTo(ID_KEY, id).findFirst();
        realm.close();

        return notification;
    }

    /**
     * @param type
     * @param createdAt
     * @return RNotification object if exist, otherwise return null.
     */
    public static @Nullable RNotification getNotificationByTypeAndDate(int type, Date createdAt) {
        Realm realm = Realm.getDefaultInstance();
        RNotification notification = realm.where(RNotification.class)
            .equalTo(TYPE_KEY, type)
            .equalTo(CREATED_AT_KEY, createdAt)
            .findFirst();
        realm.close();

        return notification;
    }

    /**
     * @param activity
     * @param title
     * @param message
     * @param isRemote
     * @param type
     * @param date
     */
    public static void setupOrUpdateNotifications(Activity activity, String title, String message, boolean isRemote, int type, Date date) {
        if (title == null || message == null || date == null) {
            return;
        } else if (type != WEEKLY && type != MONTHLY) {
            return;
        }

        // Create notification object
        boolean isNew = false;
        RNotification notification = getNotificationByTypeAndDate(type, date);
        if (notification == null) {
            isNew = true;
            // Create new notification if not exist
            notification = new RNotification();
            String uuid = UUID.randomUUID().toString();
            notification.setId(uuid);
            notification.setType(type);
            notification.setCreatedAt(date);
        }

        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRemote(isRemote);

        // Save to realm
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(notification);
        realm.commitTransaction();
        realm.close();

        if (isNew) {
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

            Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
            notificationIntent.addCategory("android.intent.category.DEFAULT");
            notificationIntent.putExtra(ID_KEY, notification.getId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, type, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}

