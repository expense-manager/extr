package com.expensemanager.app.service;
import com.expensemanager.app.models.Category;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class SyncCategory {
    private static final String TAG = SyncCategory.class.getSimpleName();

    public static Task<Void> getAllCategories() {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getAllCategories();
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveCategory = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in downloading all categories.", exception);
                    throw  exception;
                }

                JSONObject categories = task.getResult();
                if (categories == null) {
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Categories: \n" + categories);

                try {
                    JSONArray categoriesArray = categories.getJSONArray("results");
                    Category.mapFromJSONArray(categoriesArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting category JSONArray.", e);
                }

                return null;
            }
        };

        Log.d(TAG, "Start downloading categories");
        return networkRequest.send().continueWith(saveCategory);
    }

    public static Task<Void> getAllCategoriesByUserId(String userId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getAllCategoriesByUserId(userId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveCategory = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in downloading all categories.", exception);
                    throw  exception;
                }

                JSONObject categories = task.getResult();
                if (categories == null) {
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Categories: \n" + categories);

                try {
                    JSONArray categoriesArray = categories.getJSONArray("results");
                    Category.mapFromJSONArray(categoriesArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting category JSONArray.", e);
                }

                return null;
            }
        };

        Log.d(TAG, "Start downloading categories");
        return networkRequest.send().continueWith(saveCategory);
    }

    public static Task<Void> getAllCategoriesByGroupId(String groupId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.getAllCategoriesByGroupId(groupId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> saveCategory = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in downloading all categories.", exception);
                    throw  exception;
                }

                JSONObject categories = task.getResult();
                if (categories == null) {
                    throw new Exception("Empty response.");
                }

                Log.d(TAG, "Categories: \n" + categories);

                try {
                    JSONArray categoriesArray = categories.getJSONArray("results");
                    Category.mapFromJSONArray(categoriesArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error in getting category JSONArray.", e);
                }

                return null;
            }
        };

        Log.d(TAG, "Start downloading categories");
        return networkRequest.send().continueWith(saveCategory);
    }

    public static Task<JSONObject> create(Category category) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.createCategory(category);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, JSONObject> onCreateCategoryFinished = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in creating category.", exception);
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

        Log.d(TAG, "Start creating category.");
        return networkRequest.send().continueWith(onCreateCategoryFinished);
    }

    public static Task<Void> update(Category category) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.updateCategory(category);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onUpdateCategoryFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in updating category.", exception);
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

        Log.d(TAG, "Start updating category.");
        return networkRequest.send().continueWith(onUpdateCategoryFinished);
    }

    public static Task<Void> delete(String categoryId) {
        TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<>();
        RequestTemplate requestTemplate = RequestTemplateCreator.deleteCategory(categoryId);
        NetworkRequest networkRequest = new NetworkRequest(requestTemplate, taskCompletionSource);

        Continuation<JSONObject, Void> onUpdateCategoryFinished = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in deleting category.", exception);
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

        Log.d(TAG, "Start updating category.");
        return networkRequest.send().continueWith(onUpdateCategoryFinished);
    }
}
