package com.expensemanager.app.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.expensemanager.app.R;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.User;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by Zhaolong Zhong on 8/19/16.
 */

public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    public static Task<Void> login(String username, String password) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.login(username, password);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveCredential = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in login.", exception);
                    throw exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }

                String sessionToken = result.optString(User.SESSION_TOKEN);
                String userId = result.optString(User.OBJECT_ID_JSON_KEY);

                if (!TextUtils.isEmpty(sessionToken)) {
                    Context context = EApplication.getInstance();
                    SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_session_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(User.SESSION_TOKEN, sessionToken);
                    editor.putString(User.OBJECT_ID_JSON_KEY, userId);
                    editor.apply();
                } else {
                    String error = result.optString(User.ERROR);
                    if (!TextUtils.isEmpty(error)) {
                        throw new Exception(error);
                    } else {
                        throw new Exception("Incorrect login response.");
                    }
                }

                return null;
            }
        };

        return networkRequest.send().continueWith(saveCredential);
    }

    public static Task<Void> signUp(String username, String password) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.signUp(username, password);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveCredential = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in sign up.", exception);
                    throw exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }

                String sessionToken = result.optString(User.SESSION_TOKEN);

                if (!TextUtils.isEmpty(sessionToken)) {
                    Context context = EApplication.getInstance();
                    SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_session_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(User.SESSION_TOKEN, sessionToken);
                    editor.apply();
                } else {
                    String error = result.optString(User.ERROR);
                    if (!TextUtils.isEmpty(error)) {
                        throw new Exception(error);
                    } else {
                        throw new Exception("Incorrect login response.");
                    }
                }

                return null;
            }
        };

        return networkRequest.send().continueWith(saveCredential);
    }
}
