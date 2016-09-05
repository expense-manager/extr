package com.expensemanager.app.models;

import android.support.annotation.Nullable;
import android.util.Log;

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
public class Member implements RealmModel {
    private static final String TAG = Member.class.getSimpleName();

    // Keys in JSON response
    public static final String OBJECT_ID_JSON_KEY = "objectId";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String GROUP_ID_KEY = "groupId";
    public static final String USER_ID_KEY = "userId";
    public static final String IS_ACCEPTED_KEY = "isAccepted";
    public static final String CREATED_BY_KEY = "createdBy";
    public static final String CREATED_AT_KEY = "createdAt";

    @PrimaryKey
    private String id;
    private Group group;
    private User user;
    private boolean isAccepted = true;
    private User createdBy;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getGroupId() {
        return this.group.getId();
    }

    public String getUserId() {
        return this.user.getId();
    }

    public void mapFromJSON(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString(OBJECT_ID_JSON_KEY);
            this.group = new Group();
            this.user = new User();
            this.createdBy = new User();

            this.group.mapFromJSON(jsonObject.getJSONObject(GROUP_ID_KEY));
            this.user.mapFromJSON(jsonObject.getJSONObject(USER_ID_KEY));
            this.createdBy.mapFromJSON(jsonObject.getJSONObject(CREATED_BY_KEY));

            this.isAccepted = jsonObject.getBoolean(IS_ACCEPTED_KEY);

                // Parse createdAt and convert UTC time to local time
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.createdAt = simpleDateFormat.parse(jsonObject.getString(CREATED_AT_JSON_KEY));
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing member.", e);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing createdAt.", e);
        }
    }

    public static void mapFromJSONArray(JSONArray jsonArray) {
        RealmList<Member> members = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject memberJson = jsonArray.getJSONObject(i);
                Member member = new Member();
                member.mapFromJSON(memberJson);
                members.add(member);
            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing member.", e);
            }
        }

        realm.copyToRealmOrUpdate(members);
        realm.commitTransaction();
        realm.close();
    }

    public static void delete(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<Member> members = realm.where(Member.class).equalTo(ID_KEY, id).findAll();
        if (members.size() > 0) {
            members.deleteFromRealm(0);
        }
        realm.commitTransaction();
        realm.close();
    }

    /**
     * @return all members by groupId
     */
    public static RealmList<Member> getAllMembersByGroupId(String groupId) {
        RealmList<Member> realmList = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Member> members = realm.where(Member.class)
                .findAllSorted(CREATED_AT_KEY, Sort.DESCENDING);
        realm.close();

        for (Member member : members) {
            if (member.getGroupId().equals(groupId)) {
                realmList.add(member);
            }
        }

        return realmList;
    }

    /**
     * @return all members by userId
     */
    public static RealmList<Member> getAllMembersByUserId(String userId) {
        RealmList<Member> realmList = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Member> members = realm.where(Member.class)
                .findAllSorted(CREATED_AT_KEY, Sort.DESCENDING);
        realm.close();

        for (Member member : members) {
            if (member.getUserId().equals(userId)) {
                realmList.add(member);
            }
        }

        return realmList;
    }

    /**
     * @param id
     * @return Member object if exist, otherwise return null.
     */
    public static @Nullable Member getMemberById(String id) {
        Realm realm = Realm.getDefaultInstance();
        Member member = realm.where(Member.class).equalTo(ID_KEY, id).findFirst();
        realm.close();

        return member;
    }
}
