package com.expensemanager.app.service;

import android.util.Log;

import com.expensemanager.app.models.Expense;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by Zhaolong Zhong on 8/17/16.
 */

public class SyncExpense {
    private static final String TAG = SyncExpense.class.getSimpleName();

    public static Task<Void> getAllExpenses() {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getAllExpenses();
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveExpense = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in downloading all expenses.", exception);
                    throw  exception;
                }

                JSONObject expenses = task.getResult();
                if (expenses == null) {
                    //todo: throw exception
                    Log.e(TAG, "Empty response.");
                }

                Log.d(TAG, "Expenses: \n" + expenses);
                return null;
            }
        };

        Log.d(TAG, "Start downloading Expenses");
        return networkRequest.send().continueWith(saveExpense);
    }

    public static Task<Void> create(Expense expense) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.createExpense(expense);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onCreateExpenseFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in creating expense.", exception);
                    throw  exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    //todo: throw exception
                    Log.e(TAG, "Empty response.");
                }

                // Example response: {"objectId":"tUfEENoHSS","createdAt":"2016-08-18T22:34:59.262Z"}
                Log.d(TAG, "Response: \n" + result);
                return null;
            }
        };

        Log.d(TAG, "Start uploading expense.");
        return networkRequest.send().continueWith(onCreateExpenseFinished);
    }

    public static Task<Void> update(Expense expense) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.updateExpense(expense);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onUpdateExpenseFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in updating expense.", exception);
                    throw  exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    //todo: throw exception
                    Log.e(TAG, "Empty response.");
                }

                // Example response: {"updatedAt":"2016-08-18T23:03:51.785Z"}
                Log.d(TAG, "Response: \n" + result);
                return null;
            }
        };

        Log.d(TAG, "Start updating expense.");
        return networkRequest.send().continueWith(onUpdateExpenseFinished);
    }

    public static Task<Void> delete(String expenseId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.deleteExpense(expenseId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onUpdateExpenseFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in deleting expense.", exception);
                    throw  exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    //todo: throw exception
                    Log.e(TAG, "Empty response.");
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
