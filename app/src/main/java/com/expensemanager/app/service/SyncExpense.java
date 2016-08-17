package com.expensemanager.app.service;

import android.util.Log;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by Zhaolong Zhong on 8/17/16.
 */

public class SyncExpense {
    private static final String TAG = SyncExpense.class.getSimpleName();

    public static Task<Void> getExpenses() {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getExpenses();
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveGameScores = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getExpenses.", exception);
                    throw  exception;
                }

                JSONObject expenses = task.getResult();
                if (expenses == null) {
                    //todo: throw exception
                    Log.e(TAG, "Empty response.");
                }

                Log.d(TAG, "Expense: \n" + expenses);
                return null;
            }
        };

        Log.d(TAG, "Start SyncExpense.getExpenses()");
        return networkRequest.send().continueWith(saveGameScores);
    }
}
