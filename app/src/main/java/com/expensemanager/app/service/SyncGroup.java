package com.expensemanager.app.service;

import android.util.Log;

import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 9/1/16.
 */

public class SyncGroup {
    private static final String TAG = SyncGroup.class.getSimpleName();

    private static final String NAME = "name";
    private static final String OBJECT_ID = "objectId";
    private static final String RESULTS = "results";

    public static Task<Void> getGroupByUserId(String userId) {

        TaskCompletionSource tcs = new TaskCompletionSource();
        RequestTemplate template = RequestTemplateCreator.getAllGroupByUserId(userId);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

        Continuation<JSONObject, Void> addGroupItemToRealm = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getGroupByUserId", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    throw new Exception("Empty response");
                }

                Log.d(TAG, "Groups: " + jsonObject.toString());
                try {
                    JSONArray groupArray = jsonObject.getJSONArray("results");
                    Group.mapFromJSONArray(groupArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting expense JSONArray.", e);
                }

                return null;
            }
        };

        Log.d(TAG, "start getGroupByUserId: " + userId);
        return networkRequest.send().continueWith(addGroupItemToRealm);
    }

    public static Task<Void> getGroupById(String groupId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getGroupById(groupId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveGroup = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getGroupById.", exception);
                    throw  exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Group: \n" + result);

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Group group = new Group();
                group.mapFromJSON(result);
                realm.copyToRealmOrUpdate(group);
                realm.commitTransaction();
                realm.close();

                return null;
            }
        };

        Log.d(TAG, "Start getGroupById: " + groupId);
        return networkRequest.send().continueWith(saveGroup);
    }

    public static Task<JSONObject> create(Group group) {
        TaskCompletionSource<JSONObject> tcs = new TaskCompletionSource<>();
        RequestTemplate template = RequestTemplateCreator.createGroup(group);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

        Log.d(TAG, "start create group");

        Continuation<JSONObject, JSONObject> onCreateGroupFinished = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                Log.d(TAG, "onCreateGroupFinished");
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in create group", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    throw new Exception("Empty response");
                }

                String groupId = jsonObject.getString(Group.OBJECT_ID_JSON_KEY);
                // Sync new added group.
                getGroupById(groupId);

                // Self invite
                group.setId(groupId);
                User user = new User();
                user.setId(group.getUserId());

                Member member = new Member();
                member.setGroup(group);
                member.setCreatedBy(user);
                member.setUser(user);
                member.setAccepted(true);

                SyncMember.create(member);

                return jsonObject;
            }
        };

        return networkRequest.send().continueWith(onCreateGroupFinished);
    }

    public static Task<Void> update(Group group) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.updateGroup(group);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onUpdateGroupFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                Log.d(TAG, "onUpdateGroupFinished before check task.isFaulted().");
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in updating group.", exception);
                    throw exception;
                }

                JSONObject result = task.getResult();

                if (result == null) {
                    throw new Exception("Empty response.");
                }

                // Example response: {"updatedAt":"2016-08-18T23:03:51.785Z"}
                Log.d(TAG, "onUpdateGroupFinished Response: \n" + result);
                return null;
            }
        };

        Log.d(TAG, "Start updating group.");
        return networkRequest.send().continueWith(onUpdateGroupFinished);
    }

    public static Task<Void> delete(String groupId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.deleteGroup(groupId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onUpdateExpenseFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in deleting group.", exception);
                    throw  exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }

                // Example response: {}
                Log.d(TAG, "Response: \n" + result);
                return null;
            }
        };

        Log.d(TAG, "Start updating expense.");
        return networkRequest.send().continueWith(onUpdateExpenseFinished);
    }
}
