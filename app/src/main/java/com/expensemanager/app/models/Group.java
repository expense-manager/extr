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
    public static final String WEEKLY_BUDGET_JSON_KEY = "weeklyBudget";
    public static final String MONTHLY_BUDGET_JSON_KEY = "monthlyBudget";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String CREATED_AT_KEY = "createdAt";
    public static final String COLOR_KEY = "color";
    public static final String NAME_KEY = "name";

    @PrimaryKey
    private String id;
    private String groupname;
    private String name;
    private String about;
    private String userId;
    private Date createdAt;
    private String color;
    private double weeklyBudget;
    private double monthlyBudget;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getWeeklyBudget() {
        return weeklyBudget;
    }

    public void setWeeklyBudget(double weeklyBudget) {
        this.weeklyBudget = weeklyBudget;
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public void print() {
        Log.d(TAG, ("group id:" + this.id + "\n"
                + "groupname:" + this.groupname + "\n"
                + "name: " + this.name + "\n"
                + "about: " + this.about + "\n")
                + "userId: " + this.userId + "\n");
    }

    public void mapFromJSON(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString(OBJECT_ID_JSON_KEY);
            this.groupname = jsonObject.getString(GROUPNAME_JSON_KEY);
            this.name = jsonObject.optString(NAME_JSON_KEY, "");
            this.about = jsonObject.optString(ABOUT_JSON_KEY, "");
            this.weeklyBudget = jsonObject.optDouble(WEEKLY_BUDGET_JSON_KEY, 0);
            this.monthlyBudget = jsonObject.optDouble(MONTHLY_BUDGET_JSON_KEY, 0);

            JSONObject userIdJSON = jsonObject.getJSONObject(USER_ID_JSON_KEY);
            this.userId = userIdJSON.getString(OBJECT_ID_JSON_KEY);

            // Parse createdAt and convert UTC time to local time
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.createdAt = simpleDateFormat.parse(jsonObject.getString(CREATED_AT_JSON_KEY));

            // Get a random unused color
            this.color = Helpers.getRandomColor(null);
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing group.", e);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing createdAt.", e);
        }
    }

    public static void mapFromJSONArray(JSONArray jsonArray) {
        RealmList<Group> groups = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject expenseJson = jsonArray.getJSONObject(i);
                Group group = new Group();
                group.mapFromJSON(expenseJson);
                groups.add(group);
            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing group.", e);
            }
        }

        realm.copyToRealmOrUpdate(groups);
        realm.commitTransaction();
        realm.close();
    }

    public static void delete(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<Group> groups = realm.where(Group.class).equalTo(ID_KEY, id).findAll();
        if (groups.size() > 0) {
            groups.deleteFromRealm(0);
        }
        realm.commitTransaction();
        realm.close();
    }

    /**
     * @return all groups
     */
    public static RealmResults<Group> getAllGroups() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Group> groups = realm.where(Group.class).findAllSorted(NAME_KEY, Sort.ASCENDING);
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

    public static @Nullable Group getCurrentGroup() {
        String currentGroupId = Helpers.getCurrentGroupId();
        return getGroupById(currentGroupId);
    }
}
