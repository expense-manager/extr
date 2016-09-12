package com.expensemanager.app.models;

import android.support.annotation.Nullable;
import android.util.Log;

import com.expensemanager.app.helpers.Helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

import static com.expensemanager.app.models.Expense.CREATED_AT_JSON_KEY;

/**
 * Created by Zhaolong Zhong on 8/19/16.
 */

@RealmClass
public class User implements RealmModel{
    private static final String TAG = User.class.getSimpleName();

    // Keys in JSON response
    public static final String OBJECT_ID_JSON_KEY = "objectId";
    public static final String USERNAME_JSON_KEY = "username";
    public static final String FULLNAME_JSON_KEY = "fullname";
    public static final String FIRST_NAME_JSON_KEY = "firstName";
    public static final String LAST_NAME_JSON_KEY = "lastName";
    public static final String EMAIL_JSON_KEY = "email";
    public static final String PHONE_JSON_KEY = "phone";
    public static final String PHOTO_JSON_KEY = "photo";
    public static final String URL_JSON_KEY = "url";
    public static final String PASSWORD_JSON_KEY = "password";
    public static final String RESULTS = "results";
    public static final String SESSION_TOKEN = "sessionToken";
    public static final String USER_ID = "userId";
    public static final String GROUP_JSON_KEY = "groupId";
    public static final String ERROR = "error";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String CREATED_AT_KEY = "createdAt";
    public static final String GROUP_ID_KEY = "groupId";
    public static final String FIRST_NAME_KEY = "firstName";

    // Property
    @PrimaryKey
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String photoUrl;
    private Date createdAt;
    private String groupId;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullname() {
        return this.firstName + " " + this.lastName;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void mapFromJSON(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString(OBJECT_ID_JSON_KEY);
            this.username = jsonObject.getString(USERNAME_JSON_KEY);
            this.firstName = jsonObject.optString(FIRST_NAME_JSON_KEY);
            this.lastName = jsonObject.optString(LAST_NAME_JSON_KEY);
            this.email = jsonObject.optString(EMAIL_JSON_KEY);
            this.phone = jsonObject.optString(PHONE_JSON_KEY);

            JSONObject photoJsonObj = jsonObject.optJSONObject(PHOTO_JSON_KEY);
            if (photoJsonObj != null) {
                this.photoUrl = photoJsonObj.getString(URL_JSON_KEY);
                Log.i(TAG, "photo url: " + this.photoUrl);
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.createdAt = simpleDateFormat.parse(jsonObject.getString(CREATED_AT_JSON_KEY));
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing User.", e);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing createdAt.", e);
        }
    }

    public static RealmList<User> mapFromJSONArrayWithoutSaving(JSONArray jsonArray) {
        RealmList<User> users = new RealmList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject userJson = jsonArray.getJSONObject(i);
                User user = new User();
                user.mapFromJSON(userJson);
                users.add(user);
            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing users.", e);
            }
        }

        return users;
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

    public static @Nullable User getLoginUser() {
        String userId = Helpers.getLoginUserId();
        return getUserById(userId);
    }

    /**
     * @return all userss by group id
     */
    public static RealmResults<User> getAllUsersByGroupId(String groupId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<User> users = realm.where(User.class).equalTo(GROUP_ID_KEY, groupId).findAllSorted(FIRST_NAME_KEY, Sort.ASCENDING);
        realm.close();

        return users;
    }
}
