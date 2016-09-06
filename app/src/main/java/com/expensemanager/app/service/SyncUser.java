package com.expensemanager.app.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.User;

import org.json.JSONObject;

import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 8/19/16.
 */

public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    public static Task<JSONObject> login(String username, String password) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.login(username, password);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, JSONObject> saveCredential = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in login.", exception);
                    throw exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }
                // If is error response, return immediately
                if (result.has("error")) {
                    return result;
                }

                String sessionToken = result.optString(User.SESSION_TOKEN);
                String userId = result.optString(User.OBJECT_ID_JSON_KEY);

                if (!TextUtils.isEmpty(sessionToken)) {
                    Context context = EApplication.getInstance();
                    SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_session_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(User.SESSION_TOKEN, sessionToken);
                    editor.putString(User.USER_ID, userId);
                    editor.apply();
                }

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                User user = new User();
                user.mapFromJSON(result);
                realm.copyToRealmOrUpdate(user);
                realm.commitTransaction();
                realm.close();

                return result;
            }
        };

        return networkRequest.send().continueWith(saveCredential);
    }

    public static Task<JSONObject> signUp(String username, String password, String firstName, String lastName, String phone) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.signUp(username, password, firstName, lastName, phone);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, JSONObject> saveCredential = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in sign up.", exception);
                    throw exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }
                // If is error response, return immediately
                if (result.has("error")) {
                    return result;
                }

                String sessionToken = result.optString(User.SESSION_TOKEN);

                if (!TextUtils.isEmpty(sessionToken)) {
                    Context context = EApplication.getInstance();
                    SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_session_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(User.SESSION_TOKEN, sessionToken);
                    editor.apply();
                }

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                User user = new User();
                user.mapFromJSON(result);
                realm.copyToRealmOrUpdate(user);
                realm.commitTransaction();
                realm.close();

                return result;
            }
        };

        return networkRequest.send().continueWith(saveCredential);
    }

    public static Task<Void> logout() {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.logout();
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onLogout = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in logout", exception);
                    throw exception;
                }

                JSONObject logoutJsonObj = task.getResult();

                if (logoutJsonObj != null && logoutJsonObj.toString().equals("{}")) {
                    return null;
                } else {
                    throw new Exception("Incorrect logout response");
                }
            }
        };

        return networkRequest.send().continueWith(onLogout);
    }

    public static Task<Void> getLoginUser() {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getLoginUser();
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onResponseReturned = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getLoginUser", exception);
                    throw exception;
                }

                JSONObject result = task.getResult();

                String error = result.optString("error");
                if (error != null && !error.isEmpty()) {
                    Log.e(TAG, "Error: " + error);
                    return null;
                }

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                User user = new User();
                user.mapFromJSON(result);
                realm.copyToRealmOrUpdate(user);
                realm.commitTransaction();
                realm.close();

                return null;
            }
        };

        return networkRequest.send().continueWith(onResponseReturned);
    }

    public static Task<JSONObject> getAllUsersByUserFullName(String userFullName) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getAllUsersByUserFullName(userFullName);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Log.d(TAG, "Start downloading Expenses");
        return networkRequest.send();
    }

    public static Task<JSONObject> update(ProfileBuilder profileBuilder) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.updateUser(profileBuilder.getUser());
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, JSONObject> onUpdateUserDetailFinished = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in creating expense.", exception);
                    throw exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }

                // Example response: {"objectId":"tUfEENoHSS","createdAt":"2016-08-18T22:34:59.262Z"}
                Log.d(TAG, "Response: \n" + result);

                return result;
            }
        };

        // Update photo
        if (profileBuilder.getPhotoList().size() > 0) {
            addUserPhoto(profileBuilder.getUserId(), profileBuilder.getPhotoList().get(0));
        }

        Log.d(TAG, "Start updating user info.");
        return networkRequest.send().continueWith(onUpdateUserDetailFinished);
    }
    /**
     * Add expense photo to Photo table in server
     * @param userId
     * @param data
     * @return
     */
    public static Task<Void> addUserPhoto(final String userId, byte[] data) {
        Continuation<JSONObject, Task<JSONObject>> onAddFileFinished = new Continuation<JSONObject, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in onAddFileFinished", exception);
                    throw exception;
                }

                String fileName = task.getResult().optString("name");
                if (TextUtils.isEmpty(fileName)) {
                    throw new Exception("Empty input for addUserPhoto");
                }

                Log.d(TAG, "onAddFileFinished - new added photoName:" + fileName);
                Log.d(TAG, "onAddFileFinished - start to add photo to user table:" + fileName);

                // After we have file name, we add file name and expense id to Photo table.
                TaskCompletionSource<JSONObject> tcs = new TaskCompletionSource<>();
                RequestTemplate template = RequestTemplateCreator.addUserPhoto(userId, fileName);

                return new NetworkRequest(template, tcs).send();
            }
        };

        // Input: Create expense photo success object which contains expense photo id.
        Continuation<JSONObject, Void> onAddPhotoToUserFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in onAddPhotoToUserFinished", exception);
                    throw exception;
                }

                // Response example:{"updatedAt":"2016-09-06T03:36:55.075Z"}
                JSONObject jsonObject = task.getResult();
                Log.d(TAG, "onAddPhotoToUserFinished: - result:" + jsonObject.toString());

                String fileName = task.getResult().optString("updatedAt");

                if (fileName.length() > 0) {
                    String photoUrl = User.getUserById(userId).getPhotoUrl();
                    String oldPhotoName = photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
                    Log.d(TAG, "onAddPhotoToUserFinished - start delete user oldPhotoName: " + oldPhotoName);
                    SyncFile.deleteFile(oldPhotoName);
                    SyncUser.getLoginUser(); // Get the photo from user table.
                } else {
                    Log.d(TAG, "onAddPhotoToUserFinished - new fileName is not valid.");
                }

                return null;
            }
        };

        String photoName = Helpers.dateToString(new Date(), EApplication.getInstance()
            .getString(R.string.photo_date_format_string)) + ".jpg";

        return SyncPhoto.uploadPhoto(photoName, data) // add photo to File table, return file name
                .continueWithTask(onAddFileFinished)// return objectId(user photo id)
                .continueWith(onAddPhotoToUserFinished);

    }
}
