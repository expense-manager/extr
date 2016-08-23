package com.expensemanager.app.models;

import java.util.Date;

import io.realm.RealmModel;
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
    public static final String CEATED_AT_KEY= "createdAt";

    @PrimaryKey
    private String id;
    private String title;
    private String message;
    private boolean isRemote;
    private boolean isChecked;
    private Date createdAt;

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
}

