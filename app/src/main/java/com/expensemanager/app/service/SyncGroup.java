package com.expensemanager.app.service;

import android.text.TextUtils;
import android.util.Log;

import com.expensemanager.app.models.Group;

import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

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

        Log.d(TAG, "start getGroupByUserId: " + userId);
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
                // todo: persist data in realm

                return null;
            }
        };

        return networkRequest.send().continueWith(addGroupItemToRealm);
    }

    public static Task<Void> create(Group group) {
        TaskCompletionSource<JSONObject> tcs = new TaskCompletionSource<>();
        RequestTemplate template = RequestTemplateCreator.createGroup(group);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

        Log.d(TAG, "start create group");

        Continuation<JSONObject, Void> onCreateGroupFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
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

                try {
                    if(TextUtils.isEmpty(jsonObject.getString(Group.OBJECT_ID_JSON_KEY))) {
                        throw new Exception("Incorrect response");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing group object", e);
                }

                return null;
            }
        };

        return networkRequest.send().continueWith(onCreateGroupFinished);
    }
}
