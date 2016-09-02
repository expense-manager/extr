package com.expensemanager.app.models;

import android.support.annotation.Nullable;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/25/16.
 */

@RealmClass
public class Group implements RealmModel {
    private static final String TAG = Group.class.getSimpleName();

    // Keys in JSON response
    public static final String OBJECT_ID_JSON_KEY = "objectId";
    public static final String GROUPNAME_JSON_KEY = "groupname";
    public static final String NAME_JSON_KEY = "name";
    public static final String ABOUT_JSON_KEY = "about";
    public static final String USER_ID_JSON_KEY = "userId";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String CREATED_AT_KEY = "createdAt";

    @PrimaryKey
    private String id;
    private String groupname;
    private String name;
    private String about;
    private String userId;
    private Date createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    /**
     * @return all groups
     */
    public static RealmResults<Group> getAllGroups() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Group> groups = realm.where(Group.class).findAllSorted(CREATED_AT_KEY, Sort.DESCENDING);
        realm.close();

        return groups;
    }

    /**
     * @param id
     * @return Group object if exist, otherwise return null.
     */
    public static @Nullable Group getGroupById(String id) {
        Realm realm = Realm.getDefaultInstance();
        Group group = realm.where(Group.class).equalTo(ID_KEY, id).findFirst();
        realm.close();

        return group;
    }
}
