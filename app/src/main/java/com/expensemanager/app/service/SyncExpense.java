package com.expensemanager.app.service;

import android.text.TextUtils;
import android.util.Log;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.ExpenseBuilder;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.ExpensePhoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;

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
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Expenses: \n" + expenses);

                try {
                    JSONArray expenseArray = expenses.getJSONArray("results");
                    Expense.mapFromJSONArray(expenseArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting expense JSONArray.", e);
                }

                return null;
            }
        };

        Log.d(TAG, "Start downloading Expenses");
        return networkRequest.send().continueWith(saveExpense);
    }

    public static Task<Void> getExpenseById(String expenseId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getExpenseById(expenseId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveExpense = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in downloading expense.", exception);
                    throw  exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Expense: \n" + result);

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Expense expense = new Expense();
                expense.mapFromJSON(result);
                realm.copyToRealmOrUpdate(expense);
                realm.commitTransaction();
                realm.close();

                return null;
            }
        };

        Log.d(TAG, "Start downloading Expenses");
        return networkRequest.send().continueWith(saveExpense);
    }

    public static Task<JSONObject> create(ExpenseBuilder expenseBuilder) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.createExpense(expenseBuilder);
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

                Log.d(TAG, "photo size: " + expenseBuilder.getPhotoList().size());
                // Add photo
                try {
                    String expenseId = result.getString(Expense.OBJECT_ID_JSON_KEY);

                    // Sync new added expense.
                    getExpenseById(expenseId);

                    for (int i = 0; i < expenseBuilder.getPhotoList().size(); i++) {
                        Log.d(TAG, "start upload photo at index: " + i);
                        addExpensePhoto(expenseId, expenseBuilder.getPhotoList().get(i));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error in parsing Expense object.", e);
                }
                return result;
            }
        };

        Log.d(TAG, "Start creating expense.");
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
                    throw exception;
                }

                JSONObject result = task.getResult();
                if (result == null) {
                    throw new Exception("Empty response.");
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

        Continuation<JSONObject, Void> onGetExpensePhotos = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in downloading expense photo by id.", exception);
                    throw  exception;
                }

                JSONObject photos = task.getResult();
                if (photos == null) {
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Photos: \n" + photos);

                try {
                    JSONArray photosJSONArray = photos.getJSONArray("results");

                    for (int i = 0; i < photosJSONArray.length(); i++) {
                        try {
                            String expensePhotoId = photosJSONArray.getJSONObject(i).getString("objectId");
                            JSONObject photoObject = photosJSONArray.getJSONObject(i).getJSONObject("photo");
                            String fileName = photoObject.getString("name");

                            if (fileName == null || fileName.isEmpty()) {
                                continue;
                            }

                            // Delete expense photo entry
                            SyncPhoto.deleteExpensePhoto(expensePhotoId, fileName);

                        } catch (JSONException e) {
                            Log.e(TAG, "Error in parsing photo object.", e);
                        }
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting photo JSONArray.", e);
                }

                return null;
            }
        };

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
                    throw new Exception("Empty response.");
                }

                // Example response: {}
                Log.d(TAG, "Response: \n" + result);
                return null;
            }
        };

        Log.d(TAG, "Start updating expense.");

        // Delete expense photo entry
        getExpensePhotoByExpenseId(expenseId, false).continueWith(onGetExpensePhotos);
        return networkRequest.send().continueWith(onUpdateExpenseFinished);
    }

    /**
     * Add expense photo to Photo table in server
     * @param expenseId
     * @param data
     * @return
     */
    public static Task<Void> addExpensePhoto(final String expenseId, byte[] data) {
        Continuation<JSONObject, Task<JSONObject>> addExpensePhoto = new Continuation<JSONObject, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in addExpensePhoto", exception);
                    throw exception;
                }

                String fileName = task.getResult().optString("name");
                if (TextUtils.isEmpty(fileName)) {
                    throw new Exception("Empty input for addExpensePhoto");
                }

                Log.d(TAG, "addExpensePhoto - photoName:" + fileName);
                // After we have file name, we add file name and expense id to Photo table.
                TaskCompletionSource<JSONObject> tcs = new TaskCompletionSource<>();
                RequestTemplate template = RequestTemplateCreator.addExpensePhoto(expenseId, fileName);

                return new NetworkRequest(template, tcs).send();
            }
        };

        // Input: Create expense photo success object which contains expense photo id.
        Continuation<JSONObject, Task<JSONObject>> getExpensePhotoById = new Continuation<JSONObject, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getExpensePhotoById", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();
                if (jsonObject == null) {
                    throw new Exception("Empty input for getExpensePhotoById");
                }

                String expensePhotoId = jsonObject.getString("objectId");
                RequestTemplate template = RequestTemplateCreator.getExpensePhotoByPhotoId(expensePhotoId);
                TaskCompletionSource tcs = new TaskCompletionSource();

                return new NetworkRequest(template, tcs).send();
            }
        };

        // Input: Photo object
        Continuation<JSONObject, Void> addExpensePhotoToRealm = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in addExpensePhotoToRealm", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();
                if (jsonObject == null) {
                    throw new Exception("Empty input for addExpensePhotoToRealm");
                }

                Log.d(TAG, "Photo: " + jsonObject);
                //todo: add file name to expense photos, so we can use the name to build photo url

                return null;
            }
        };


        String photoName = Helpers.dateToString(new Date(), EApplication.getInstance()
                .getString(R.string.photo_date_format_string)) + ".jpg";

        SyncPhoto.uploadPhoto(photoName, data) // add photo to File table, return file name
                .continueWithTask(addExpensePhoto) // return objectId(expense photo id)
                .continueWithTask(getExpensePhotoById) // return single expense photo object
                .continueWith(addExpensePhotoToRealm); // return null

        return null;
    }

    public static Task<JSONObject> getExpensePhotoByExpenseId(final String expenseId, boolean saveToRealm) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getExpensePhotoByExpenseId(expenseId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, JSONObject> saveExpensePhotoName = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in downloading expense photo by id.", exception);
                    throw  exception;
                }

                JSONObject photos = task.getResult();
                if (photos == null) {
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Photos: \n" + photos);

                try {
                    JSONArray photosJSONArray = photos.getJSONArray("results");
                    ExpensePhoto.mapPhotoFromJSONArray(photosJSONArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting photo JSONArray.", e);
                }

                return photos;
            }
        };

        Log.d(TAG, "Start downloading Expense photo");
        if (saveToRealm) {
            return networkRequest.send().continueWith(saveExpensePhotoName);
        } else {
            return networkRequest.send();
        }
    }
}
