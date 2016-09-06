package com.expensemanager.app.service;

import android.util.Log;

import com.expensemanager.app.models.ExpensePhoto;

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



    public static void deleteExpensePhoto(String expensePhotoId, String fileName) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate template = RequestTemplateCreator.deleteExpensePhoto(expensePhotoId);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> onPhotoDelete = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in deleteExpensePhoto", exception);
                    throw exception;
                }

                // Error response example: {"code":153,"error":"Could not delete file."}
                // File may not exist
                if (task.getResult().toString().equals("{}")) {
                    Log.d(TAG, "Expense photo entry delete success.");
                } else {
                    Log.e(TAG, "Error in deleting photo entry with id: " + expensePhotoId);
                    // force to delete entry in Photo table
                }

                ExpensePhoto.delete(expensePhotoId, fileName);

                return null;
            }
        };

        Continuation<JSONObject, Void> onFileDelete = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in deleteFile", exception);
                    throw exception;
                }

                // Error response example: {"code":153,"error":"Could not delete file."}
                // File may not exist

                if (task.getResult().toString().equals("{}")) {
                    Log.d(TAG, "File delete success.");
                } else {
                    Log.e(TAG, "Error in deleting file: " + fileName);
                    // force to delete entry in Photo table
                }

                networkRequest.send().continueWith(onPhotoDelete);

                return null;
            }
        };

        SyncFile.deleteFile(fileName).continueWith(onFileDelete);
    }
}
