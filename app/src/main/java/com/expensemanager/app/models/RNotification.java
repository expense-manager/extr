package com.expensemanager.app.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.expensemanager.app.R;
import com.expensemanager.app.main.EApplication;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

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
    public static final String GROUP_KEY = "groupId";

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
    private String groupId;

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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public static RealmResults<RNotification> getAllUnReadNotifications() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RNotification> notifications = realm.where(RNotification.class)
                .equalTo(IS_CHECKED, false)
                .findAllSorted(CREATED_AT_KEY, Sort.DESCENDING);
        realm.close();

        return notifications;
    }

    public static int getUnReadNotificationCount() {
        return getAllUnReadNotifications().size();
    }

    public static int getUnReadValidNotificationCount() {
        RealmResults<RNotification> notifications = getAllUnReadNotifications();
        int count = notifications.size();

        Date currentDate = new Date();

        for (RNotification notification : notifications) {
            if (notification.getCreatedAt().compareTo(currentDate) > 0) {
                count--;
            }
        }

        return count;
    }

    /**
     * @param id notification id
     * @return RNotification object if exist, otherwise return null.
     */
    public static @Nullable RNotification getNotificationById(String id) {
        Realm realm = Realm.getDefaultInstance();
        RNotification notification = realm.where(RNotification.class).equalTo(ID_KEY, id).findFirst();
        realm.close();

        return notification;
    }

    /**
     * @param type notification type
     * @param createdAt date to notify
     * @return RNotification object if exist, otherwise return null.
     */
    public static @Nullable RNotification getNotificationByTypeAndDateAndGroupId(int type, Date createdAt, String groupId) {
        Realm realm = Realm.getDefaultInstance();
        RNotification notification = realm.where(RNotification.class)
            .equalTo(TYPE_KEY, type)
            .equalTo(CREATED_AT_KEY, createdAt)
            .equalTo(GROUP_KEY, groupId)
            .findFirst();
        realm.close();

        return notification;
    }

    /**
     * @param type notification type
     * @param createdAt date to notify
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
     * @param id notification id
     */
    public static void delete(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<RNotification> notifications = realm.where(RNotification.class).equalTo(ID_KEY, id).findAll();
        if (notifications.size() > 0) {
            notifications.deleteFromRealm(0);
        }
        realm.commitTransaction();
        realm.close();
    }

    /**
     * @param groupId group id
     * @param type notification type
     * @param calendar notification time
     */
    public static void delete(String groupId, int type, Calendar calendar) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<RNotification> notifications = realm.where(RNotification.class)
            .equalTo(GROUP_KEY, groupId)
            .equalTo(TYPE_KEY, type)
            .equalTo(CREATED_AT_KEY, calendar.getTime())
            .findAll();
        if (notifications.size() > 0) {
            notifications.deleteFromRealm(0);
        }
        realm.commitTransaction();
        realm.close();
    }

    /**
     * @param groupId group id
     * @param isRemote if notification is remote from server
     * @param type notification type: WEEKLY or MONTHLY
     * @param createAt date to notify
     */
    public static void setupOrUpdateNotifications(String groupId, boolean isRemote, int type, Calendar createAt) {
        if (createAt == null) {
            return;
        } else if (type != WEEKLY && type != MONTHLY) {
            return;
        }

        Context context = EApplication.getInstance();
        String title = context.getString(type == WEEKLY ? R.string.weekly_report : R.string.monthly_report);
        String message = context.getString(type == WEEKLY ? R.string.weekly_report_message : R.string.monthly_report_message);;

        // Create notification object
        RNotification notification = getNotificationByTypeAndDate(type, createAt.getTime());

        // Notification already set
        if (notification != null) {
            Log.i("SettingsFragment", "Notification already set");
            return;
        }

        Log.i("SettingsFragment", "Set new Notification");
        // Create new to realm
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        // Create new notification if not exist
        notification = new RNotification();
        String uuid = UUID.randomUUID().toString();
        notification.setId(uuid);
        notification.setType(type);
        notification.setGroupId(groupId);
        notification.setCreatedAt(createAt.getTime());

        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRemote(isRemote);

        realm.copyToRealmOrUpdate(notification);
        realm.commitTransaction();
        realm.close();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.addCategory("android.intent.category.DEFAULT");
        notificationIntent.putExtra(ID_KEY, notification.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, groupId.hashCode() + type, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, createAt.getTimeInMillis(), pendingIntent);
    }
}

