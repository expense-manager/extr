package com.expensemanager.app.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileBuilder;

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

    public static Task<JSONObject> update(ProfileBuilder profileBuilder) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.updateUser(profileBuilder.getUser());
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, JSONObject> onCreateExpenseFinished = new Continuation<JSONObject, JSONObject>() {
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

                // Add photo
                addUserPhoto(profileBuilder.getUserId(), profileBuilder.getProfileImage());

                return result;
            }
        };

        Log.d(TAG, "Start updating user info.");
        return networkRequest.send().continueWith(onCreateExpenseFinished);
    }
    /**
     * Add expense photo to Photo table in server
     * @param userId
     * @param data
     * @return
     */
    public static Task<JSONObject> addUserPhoto(final String userId, byte[] data) {
        Continuation<JSONObject, Task<JSONObject>> addUserPhoto = new Continuation<JSONObject, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in addUserPhoto", exception);
                    throw exception;
                }

                String fileName = task.getResult().optString("name");
                if (TextUtils.isEmpty(fileName)) {
                    throw new Exception("Empty input for addUserPhoto");
                }

                Log.d(TAG, "addUserPhoto - photoName:" + fileName);
                // After we have file name, we add file name and expense id to Photo table.
                TaskCompletionSource<JSONObject> tcs = new TaskCompletionSource<>();
                RequestTemplate template = RequestTemplateCreator.addUserPhoto(userId, fileName);

                return new NetworkRequest(template, tcs).send();
            }
        };

        String photoName = Helpers.dateToString(new Date(), EApplication.getInstance()
            .getString(R.string.photo_date_format_string)) + ".jpg";

        return SyncPhoto.uploadPhoto(photoName, data) // add photo to File table, return file name
            .continueWithTask(addUserPhoto); // return objectId(user photo id)

    }
}
