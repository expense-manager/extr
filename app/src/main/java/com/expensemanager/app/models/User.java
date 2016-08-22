package com.expensemanager.app.models;

import android.support.annotation.Nullable;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/19/16.
 */

@RealmClass
public class User implements RealmModel{
    private static final String TAG = User.class.getSimpleName();

    // Keys in JSON response
    public static final String OBJECT_ID_JSON_KEY = "objectId";
    public static final String USERNAME_JSON_KEY = "username";
    public static final String PHONE_JSON_KEY = "phone";
    public static final String PASSWORD_JSON_KEY = "password";
    public static final String RESULTS = "results";
    public static final String SESSION_TOKEN = "sessionToken";
    public static final String USER_ID = "userId";
    public static final String ERROR = "error";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String CREATED_AT_KEY = "createdAt";

    // Property
    @PrimaryKey
    private String id;
    private String username;
    private String email;
    private String phone;
    private Date createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @param id
     * @return User object if exist, otherwise return null.
     */
    public static @Nullable User getUserById(String id) {
        Realm realm = Realm.getDefaultInstance();
        User user = realm.where(User.class).equalTo(ID_KEY, id).findFirst();
        realm.close();

        return user;
    }
}
