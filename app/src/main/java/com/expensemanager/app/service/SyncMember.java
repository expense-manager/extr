package com.expensemanager.app.service;

import android.util.Log;

import com.expensemanager.app.models.Member;

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

public class SyncMember {
    private static final String TAG = SyncMember.class.getSimpleName();

    private static final String NAME = "name";
    private static final String OBJECT_ID = "objectId";
    private static final String RESULTS = "results";

    public static Task<Void> getMembersByGroupId(String groupId) {
        TaskCompletionSource tcs = new TaskCompletionSource();
        RequestTemplate template = RequestTemplateCreator.getMembersByGroupId(groupId);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

        Continuation<JSONObject, Void> addMemberToRealm = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getMembersByGroupId", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    throw new Exception("Empty response");
                }

                Log.d(TAG, "Member: " + jsonObject.toString());
                try {
                    JSONArray memberArray = jsonObject.getJSONArray("results");
                    Member.mapFromJSONArray(memberArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting member JSONArray.", e);
                }

                return null;
            }
        };

        Log.d(TAG, "start getMembersByGroupId: " + groupId);
        return networkRequest.send().continueWith(addMemberToRealm);
    }

    public static Task<Void> getMembersByUserId(String userId) {
        TaskCompletionSource tcs = new TaskCompletionSource();
        RequestTemplate template = RequestTemplateCreator.getMembersByUserId(userId);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

        Continuation<JSONObject, Void> addMemberToRealm = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getMembersByUserId", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    throw new Exception("Empty response");
                }

                Log.d(TAG, "getMembersByUserId: Member: " + jsonObject.toString());
                try {
                    JSONArray memberArray = jsonObject.getJSONArray("results");
                    Member.mapFromJSONArray(memberArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting member JSONArray.", e);
                }

                return null;
            }
        };

        Log.d(TAG, "start getMembersByUserId: " + userId);
        return networkRequest.send().continueWith(addMemberToRealm);
    }

    public static Task<Void> getMemberByMemberId(String memberId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getMemberByMemberId(memberId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveMember = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in downloading member.", exception);
                    throw  exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Member: \n" + result);

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Member member = new Member();
                member.mapFromJSON(result);
                realm.copyToRealmOrUpdate(member);
                realm.commitTransaction();
                realm.close();

                return null;
            }
        };

        Log.d(TAG, "Start to download member");
        return networkRequest.send().continueWith(saveMember);
    }

    public static Task<JSONObject> create(Member member) {
        TaskCompletionSource<JSONObject> tcs = new TaskCompletionSource<>();
        RequestTemplate template = RequestTemplateCreator.createMember(member);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

        Continuation<JSONObject, JSONObject> onCreateMemberFinished = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                Log.d(TAG, "onCreateMemberFinished");
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in create member", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    throw new Exception("Empty response");
                }

                String memberId = jsonObject.getString(Member.OBJECT_ID_JSON_KEY);
                // Sync new added member.
                getMemberByMemberId(memberId);

                return null;
            }
        };

        Log.d(TAG, "start to create member.");
        return networkRequest.send().continueWith(onCreateMemberFinished);
    }

    public static Task<JSONObject> update(Member member) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.updateMember(member);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        final String memberId = member.getId();

        Continuation<JSONObject, JSONObject> onUpdateMemberFinished = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                Log.d(TAG, "onUpdateMemberFinished before check task.isFaulted().");
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in updating member.", exception);
                    throw exception;
                }

                JSONObject result = task.getResult();

                if (result == null) {
                    throw new Exception("Empty response.");
                }

                // Example response: {"updatedAt":"2016-08-18T23:03:51.785Z"}
                Log.d(TAG, "onUpdateMemberFinished Response: \n" + result);

                // Sync new added member.
                getMemberByMemberId(memberId);
                return result;
            }
        };

        Log.d(TAG, "Start to update member.");
        return networkRequest.send().continueWith(onUpdateMemberFinished);
    }

    public static Task<Void> delete(String memberId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.deleteMember(memberId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onUpdateExpenseFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in deleting member.", exception);
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

        Log.d(TAG, "Start to delete member.");
        return networkRequest.send().continueWith(onUpdateExpenseFinished);
    }
}
