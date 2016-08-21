package com.expensemanager.app.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by Zhaolong Zhong on 8/21/16.
 */

public class SyncPhoto {
    private static final String TAG = SyncPhoto.class.getSimpleName();

    public static Task<JSONObject> uploadPhoto(String photoName, byte[] data) {

        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate template = RequestTemplateCreator.uploadPhoto(photoName, data);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, JSONObject> uploadingPhoto = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in uploadPhoto", exception);
                    throw exception;
                }

                if (task.getResult() == null) {
                    throw new Exception("Empty response");
                }

                try {
                    String photoName = task.getResult().getString("name");
                    Log.d(TAG, "uploaded photo name:" + photoName);
                    return task.getResult();

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing photo object", e);
                }

                return null;
            }
        };

        return networkRequest.send().onSuccess(uploadingPhoto);
    }
}
