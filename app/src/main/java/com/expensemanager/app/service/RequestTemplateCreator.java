package com.expensemanager.app.service;

import android.util.Log;

import com.expensemanager.app.expense.ExpenseBuilder;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zhaolong Zhong on 8/17/16.
 */

public class RequestTemplateCreator {
    private static final String TAG = RequestTemplateCreator.class.getSimpleName();

    private static final String BASE_URL = "https://e-manager.herokuapp.com/parse/";

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String CONTENT = "CONTENT";

    public static RequestTemplate login(String username, String password) {
        String url = BASE_URL + "login";
        Map<String, String> params = new HashMap<>();

        params.put(User.USERNAME_JSON_KEY, username);
        params.put(User.PASSWORD_JSON_KEY, password);

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate signUp(String username, String password) {
        String url = BASE_URL + "users";
        Map<String, String> params = new HashMap<>();

        params.put(User.USERNAME_JSON_KEY, username);
        params.put(User.PASSWORD_JSON_KEY, password);

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate getAllExpenses() {
        String url = BASE_URL + "classes/Expense";
        Map<String, String> params = new HashMap<>();
        //todo: getAllExpensesByUserId

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate createExpense(ExpenseBuilder expenseBuilder) {
        String url = BASE_URL + "classes/Expense";
        Map<String, String> params = new HashMap<>();

        Expense expense = expenseBuilder.getExpense();

        params.put(Expense.AMOUNT_JSON_KEY, String.valueOf(expense.getAmount()));
        params.put(Expense.NOTE_JSON_KEY, expense.getNote());

        // todo: Build Category Pointer
//        try {
//            JSONObject categoryIdObj=new JSONObject();
//            categoryIdObj.put("__type", "Pointer");
//            categoryIdObj.put("className", "Category");
//            categoryIdObj.put("objectId", expenseBuilder.getCategoryId());
//            params.put("categoryId", categoryIdObj.toString());
//        } catch (JSONException e) {
//            Log.e(TAG, "Error creating category id pointer object for 'where' in createExpense", e);
//        }

        // todo: build User Pointer
        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateExpense(Expense expense) {
        String url = BASE_URL + "classes/Expense/" + expense.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Expense.AMOUNT_JSON_KEY, String.valueOf(expense.getAmount()));
        params.put(Expense.NOTE_JSON_KEY, expense.getNote());

        return new RequestTemplate(PUT, url, params);
    }

    public static RequestTemplate deleteExpense(String expenseId) {
        String url = BASE_URL + "classes/Expense/" + expenseId;

        return new RequestTemplate(DELETE, url, null);
    }

    public static RequestTemplate getAllCategories() {
        String url = BASE_URL + "classes/Category";
        Map<String, String> params = new HashMap<>();
        //todo: getAllCategoriesByUserId

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate createCategory(Category category) {
        String url = BASE_URL + "classes/Category";
        Map<String, String> params = new HashMap<>();

        params.put(Category.NAME_JSON_KEY, category.getName());

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateCategory(Category category) {
        String url = BASE_URL + "classes/Category/" + category.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Category.NAME_JSON_KEY, category.getName());

        return new RequestTemplate(PUT, url, params);
    }

    public static RequestTemplate deleteCategory(String categoryId) {
        String url = BASE_URL + "classes/Category/" + categoryId;

        return new RequestTemplate(DELETE, url, null);
    }

    /**
     * Upload photo to File table
     * @param photoName
     * @param content
     * @return
     */
    public static RequestTemplate uploadPhoto(String photoName, byte[] content) {
        String url = BASE_URL + "files/" + photoName;
        Map<String, byte[]> params = new HashMap<>();
        params.put(CONTENT, content);

        return new RequestTemplate(POST, url, null, params);
    }

    public static RequestTemplate getExpensePhotoByPhotoId(String photoId) {
        String url = BASE_URL + "classes/Photo/" + photoId;
        return new RequestTemplate(GET, url, null);
    }

    public static RequestTemplate getExpensePhotoByExpenseId(String expenseId) {
        String url = BASE_URL + "classes/Photo";
        Map<String, String> params = new HashMap<>();

        JSONObject expensePointerObj = new JSONObject();
        JSONObject expenseIdObj=new JSONObject();
        try {
            expensePointerObj.put("__type", "Pointer");
            expensePointerObj.put("className", "Expense");
            expensePointerObj.put("objectId", expenseId);
            expenseIdObj.put("expenseId", expensePointerObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating expense id pointer object for where in getExpensePhotoByExpenseId", e);
        }

        params.put("where", Helpers.encodeURIComponent(expenseIdObj.toString()));
        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate addExpensePhoto(String expenseId, String fileName) {
        String url = BASE_URL + "classes/Photo";
        Map<String, String> params = new HashMap<>();
        JSONObject expensePointerObject = new JSONObject();
        JSONObject photoObject = new JSONObject();

        try {
            // Build Expense pointer
            expensePointerObject.put("__type", "Pointer");
            expensePointerObject.put("className", "Expense");
            expensePointerObject.put("objectId", expenseId);

            Log.d(TAG, "expense pointer:" + expensePointerObject.toString());
            params.put("expenseId", expensePointerObject.toString());

            // Build File pointer
            photoObject.put("__type", "File");
            photoObject.put("name", fileName);

            Log.d(TAG, "photoObject:" + photoObject.toString());
            params.put("photo", photoObject.toString());

            return new RequestTemplate(POST, url, params);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating expense pointer or photo pointer for where clause in addExpensePhoto()", e);
        }

        return null;
    }
}
