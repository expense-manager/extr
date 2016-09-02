package com.expensemanager.app.models;

import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/25/16.
 */

@RealmClass
public class GroupUser implements RealmModel {
    private static final String TAG = GroupUser.class.getSimpleName();

    // Keys in JSON response
    public static final String OBJECT_ID_JSON_KEY = "objectId";
    public static final String GROUP_ID_JSON_KEY = "groupId";
    public static final String USER_ID_JSON_KEY = "userId";
    public static final String IS_ACCEPTED_JSON_KEY = "isAccepted";

    @PrimaryKey
    private String id;
    private Group group;
    private User user;
    private boolean isAccepted;
    private Date createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
